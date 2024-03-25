//
//  AVPlayerWrapper.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 06/03/2018.
//  Copyright © 2018 Jørgen Henrichsen. All rights reserved.
//

import Foundation
import AVFoundation
import MediaPlayer

public enum PlaybackEndedReason: String {
    case playedUntilEnd
    case playerStopped
    case skippedToNext
    case skippedToPrevious
    case jumpedToIndex
    case cleared
    case failed
}

class AVPlayerWrapper: AVPlayerWrapperProtocol {
    // MARK: - Properties
    
    fileprivate var avPlayer = AVPlayer()
    private let playerObserver = AVPlayerObserver()
    internal let playerTimeObserver: AVPlayerTimeObserver
    private let playerItemNotificationObserver = AVPlayerItemNotificationObserver()
    private let playerItemObserver = AVPlayerItemObserver()
    fileprivate var timeToSeekToAfterLoading: TimeInterval?
    fileprivate var asset: AVAsset? = nil
    fileprivate var item: AVPlayerItem? = nil
    fileprivate var url: URL? = nil
    fileprivate var urlOptions: [String: Any]? = nil
    fileprivate let stateQueue = DispatchQueue(
        label: "AVPlayerWrapper.stateQueue",
        attributes: .concurrent
    )

    public init() {
        playerTimeObserver = AVPlayerTimeObserver(periodicObserverTimeInterval: timeEventFrequency.getTime())

        playerObserver.delegate = self
        playerTimeObserver.delegate = self
        playerItemNotificationObserver.delegate = self
        playerItemObserver.delegate = self

        setupAVPlayer();
    }
    
    // MARK: - AVPlayerWrapperProtocol

    fileprivate(set) var playbackError: AudioPlayerError.PlaybackError? = nil
    
    var _state: AVPlayerWrapperState = AVPlayerWrapperState.idle
    var state: AVPlayerWrapperState {
        get {
            var state: AVPlayerWrapperState!
            stateQueue.sync {
                state = _state
            }

            return state
        }
        set {
            stateQueue.async(flags: .barrier) { [weak self] in
                guard let self = self else { return }
                let currentState = self._state
                if (currentState != newValue) {
                    self._state = newValue
                    self.delegate?.AVWrapper(didChangeState: newValue)
                }
            }
        }
    }

    fileprivate(set) var lastPlayerTimeControlStatus: AVPlayer.TimeControlStatus = AVPlayer.TimeControlStatus.paused

    /**
     Whether AVPlayer should start playing automatically when the item is ready.
     */
    public var playWhenReady: Bool = false {
        didSet {
            if (playWhenReady == true && (state == .failed || state == .stopped)) {
                reload(startFromCurrentTime: state == .failed)
            }

            applyAVPlayerRate()
            
            if oldValue != playWhenReady {
                delegate?.AVWrapper(didChangePlayWhenReady: playWhenReady)
            }
        }
    }
    
    var currentItem: AVPlayerItem? {
        avPlayer.currentItem
    }

    var playbackActive: Bool {
        switch state {
        case .idle, .stopped, .ended, .failed:
            return false
        default: return true
        }
    }
    
    var currentTime: TimeInterval {
        let seconds = avPlayer.currentTime().seconds
        return seconds.isNaN ? 0 : seconds
    }
    
    var duration: TimeInterval {
        if let seconds = currentItem?.asset.duration.seconds, !seconds.isNaN {
            return seconds
        }
        else if let seconds = currentItem?.duration.seconds, !seconds.isNaN {
            return seconds
        }
        else if let seconds = currentItem?.seekableTimeRanges.last?.timeRangeValue.duration.seconds,
                !seconds.isNaN {
            return seconds
        }
        return 0.0
    }
    
    var bufferedPosition: TimeInterval {
        currentItem?.loadedTimeRanges.last?.timeRangeValue.end.seconds ?? 0
    }

    var reasonForWaitingToPlay: AVPlayer.WaitingReason? {
        avPlayer.reasonForWaitingToPlay
    }

    private var _rate: Float = 1.0;
    var rate: Float {
        get { _rate }
        set {
            _rate = newValue
            applyAVPlayerRate()
        }
    }

    weak var delegate: AVPlayerWrapperDelegate? = nil
    
    var bufferDuration: TimeInterval = 0

    var timeEventFrequency: TimeEventFrequency = .everySecond {
        didSet {
            playerTimeObserver.periodicObserverTimeInterval = timeEventFrequency.getTime()
        }
    }
    
    var volume: Float {
        get { avPlayer.volume }
        set { avPlayer.volume = newValue }
    }
    
    var isMuted: Bool {
        get { avPlayer.isMuted }
        set { avPlayer.isMuted = newValue }
    }

    var automaticallyWaitsToMinimizeStalling: Bool {
        get { avPlayer.automaticallyWaitsToMinimizeStalling }
        set { avPlayer.automaticallyWaitsToMinimizeStalling = newValue }
    }
    
    func play() {
        playWhenReady = true
    }
    
    func pause() {
        playWhenReady = false
    }
    
    func togglePlaying() {
        switch avPlayer.timeControlStatus {
        case .playing, .waitingToPlayAtSpecifiedRate:
            pause()
        case .paused:
            play()
        @unknown default:
            fatalError("Unknown AVPlayer.timeControlStatus")
        }
    }
    
    func stop() {
        state = .stopped
        clearCurrentItem()
        playWhenReady = false
    }
    
    func seek(to seconds: TimeInterval) {
       // if the player is loading then we need to defer seeking until it's ready.
        if (avPlayer.currentItem == nil) {
         timeToSeekToAfterLoading = seconds
       } else {
           let time = CMTimeMakeWithSeconds(seconds, preferredTimescale: 1000)
           avPlayer.seek(to: time, toleranceBefore: CMTime.zero, toleranceAfter: CMTime.zero) { (finished) in
             self.delegate?.AVWrapper(seekTo: Double(seconds), didFinish: finished)
         }
       }
     }

    func seek(by seconds: TimeInterval) {
        if let currentItem = avPlayer.currentItem {
            let time = currentItem.currentTime().seconds + seconds
            avPlayer.seek(
                to: CMTimeMakeWithSeconds(time, preferredTimescale: 1000)
            ) { (finished) in
                  self.delegate?.AVWrapper(seekTo: Double(time), didFinish: finished)
            }
        } else {
            if let timeToSeekToAfterLoading = timeToSeekToAfterLoading {
                self.timeToSeekToAfterLoading = timeToSeekToAfterLoading + seconds
            } else {
                timeToSeekToAfterLoading = seconds
            }
        }
    }
    
    private func playbackFailed(error: AudioPlayerError.PlaybackError) {
        state = .failed
        self.playbackError = error
        self.delegate?.AVWrapper(failedWithError: error)
    }
    
    func load() {
        if (state == .failed) {
            recreateAVPlayer()
        } else {
            clearCurrentItem()
        }
        if let url = url {
            let pendingAsset = AVURLAsset(url: url, options: urlOptions)
            asset = pendingAsset
            state = .loading
            
            // Load metadata keys asynchronously and separate from playable, to allow that to execute as quickly as it can
            let metdataKeys = ["commonMetadata", "availableChapterLocales", "availableMetadataFormats"]
            pendingAsset.loadValuesAsynchronously(forKeys: metdataKeys, completionHandler: { [weak self] in
                guard let self = self else { return }
                if (pendingAsset != self.asset) { return; }
                
                let commonData = pendingAsset.commonMetadata
                if (!commonData.isEmpty) {
                    self.delegate?.AVWrapper(didReceiveCommonMetadata: commonData)
                }
                
                if pendingAsset.availableChapterLocales.count > 0 {
                    for locale in pendingAsset.availableChapterLocales {
                        let chapters = pendingAsset.chapterMetadataGroups(withTitleLocale: locale, containingItemsWithCommonKeys: nil)
                        self.delegate?.AVWrapper(didReceiveChapterMetadata: chapters)
                    }
                } else {
                    for format in pendingAsset.availableMetadataFormats {
                        let timeRange = CMTimeRange(start: CMTime(seconds: 0, preferredTimescale: 1000), end: pendingAsset.duration)
                        let group = AVTimedMetadataGroup(items: pendingAsset.metadata(forFormat: format), timeRange: timeRange)
                        self.delegate?.AVWrapper(didReceiveTimedMetadata: [group])
                    }
                }
            })
            
            // Load playable portion of the track and commence when ready
            let playableKeys = ["playable"]
            pendingAsset.loadValuesAsynchronously(forKeys: playableKeys, completionHandler: { [weak self] in
                guard let self = self else { return }
                
                DispatchQueue.main.async {
                    if (pendingAsset != self.asset) { return; }
                    
                    for key in playableKeys {
                        var error: NSError?
                        let keyStatus = pendingAsset.statusOfValue(forKey: key, error: &error)
                        switch keyStatus {
                        case .failed:
                            self.playbackFailed(error: AudioPlayerError.PlaybackError.failedToLoadKeyValue)
                            return
                        case .cancelled, .loading, .unknown:
                            return
                        case .loaded:
                            break
                        default: break
                        }
                    }
                    
                    if (!pendingAsset.isPlayable) {
                        self.playbackFailed(error: AudioPlayerError.PlaybackError.itemWasUnplayable)
                        return;
                    }
                    
                    let item = AVPlayerItem(
                        asset: pendingAsset,
                        automaticallyLoadedAssetKeys: playableKeys
                    )
                    self.item = item;
                    item.preferredForwardBufferDuration = self.bufferDuration
                    self.avPlayer.replaceCurrentItem(with: item)
                    self.startObservingAVPlayer(item: item)
                    self.applyAVPlayerRate()
                    
                    if let initialTime = self.timeToSeekToAfterLoading {
                        self.timeToSeekToAfterLoading = nil
                        self.seek(to: initialTime)
                    }
                }
            })
        }
    }
    
    func load(from url: URL, playWhenReady: Bool, options: [String: Any]? = nil) {
        self.playWhenReady = playWhenReady
        self.url = url
        self.urlOptions = options
        self.load()
    }
    
    func load(
        from url: URL,
        playWhenReady: Bool,
        initialTime: TimeInterval? = nil,
        options: [String : Any]? = nil
    ) {
        self.load(from: url, playWhenReady: playWhenReady, options: options)
        if let initialTime = initialTime {
            self.seek(to: initialTime)
        }
    }

    func load(
        from url: String,
        type: SourceType = .stream,
        playWhenReady: Bool = false,
        initialTime: TimeInterval? = nil,
        options: [String : Any]? = nil
    ) {
        if let itemUrl = type == .file
            ? URL(fileURLWithPath: url)
            : URL(string: url)
        {
            self.load(from: itemUrl, playWhenReady: playWhenReady, options: options)
            if let initialTime = initialTime {
                self.seek(to: initialTime)
            }
        } else {
            clearCurrentItem()
            playbackFailed(error: AudioPlayerError.PlaybackError.invalidSourceUrl(url))
        }
    }

    func unload() {
        clearCurrentItem()
        state = .idle
    }

    func reload(startFromCurrentTime: Bool) {
        var time : Double? = nil
        if (startFromCurrentTime) {
            if let currentItem = currentItem {
                if (!currentItem.duration.isIndefinite) {
                    time = currentItem.currentTime().seconds
                }
            }
        }
        load()
        if let time = time {
            seek(to: time)
        }
    }
    
    // MARK: - Util

    private func clearCurrentItem() {
        guard let asset = asset else { return }
        stopObservingAVPlayerItem()
        
        asset.cancelLoading()
        self.asset = nil
        
        avPlayer.replaceCurrentItem(with: nil)
    }
    
    private func startObservingAVPlayer(item: AVPlayerItem) {
        playerItemObserver.startObserving(item: item)
        playerItemNotificationObserver.startObserving(item: item)
    }

    private func stopObservingAVPlayerItem() {
        playerItemObserver.stopObservingCurrentItem()
        playerItemNotificationObserver.stopObservingCurrentItem()
    }
    
    private func recreateAVPlayer() {
        playbackError = nil
        playerTimeObserver.unregisterForBoundaryTimeEvents()
        playerTimeObserver.unregisterForPeriodicEvents()
        playerObserver.stopObserving()
        stopObservingAVPlayerItem()
        clearCurrentItem()

        avPlayer = AVPlayer();
        setupAVPlayer()

        delegate?.AVWrapperDidRecreateAVPlayer()
    }
    
    private func setupAVPlayer() {
        // disabled since we're not making use of video playback
        avPlayer.allowsExternalPlayback = false;

        playerObserver.player = avPlayer
        playerObserver.startObserving()

        playerTimeObserver.player = avPlayer
        playerTimeObserver.registerForBoundaryTimeEvents()
        playerTimeObserver.registerForPeriodicTimeEvents()

        applyAVPlayerRate()
    }
    
    private func applyAVPlayerRate() {
        avPlayer.rate = playWhenReady ? _rate : 0
    }
}

extension AVPlayerWrapper: AVPlayerObserverDelegate {
    
    // MARK: - AVPlayerObserverDelegate
    
    func player(didChangeTimeControlStatus status: AVPlayer.TimeControlStatus) {
        switch status {
        case .paused:
            let state = self.state
            if self.asset == nil && state != .stopped {
                self.state = .idle
            } else if (state != .failed && state != .stopped) {
                // Playback may have become paused externally for example due to a bluetooth device disconnecting:
                if (self.playWhenReady) {
                    // Only if we are not on the boundaries of the track, otherwise itemDidPlayToEndTime will handle it instead.
                    if (self.currentTime > 0 && self.currentTime < self.duration) {
                        self.playWhenReady = false;
                    }
                } else {
                    self.state = .paused
                }
            }
        case .waitingToPlayAtSpecifiedRate:
            if self.asset != nil {
                self.state = .buffering
            }
        case .playing:
            self.state = .playing
        @unknown default:
            break
        }
    }
    
    func player(statusDidChange status: AVPlayer.Status) {
        if (status == .failed) {
            let error = item!.error as NSError?
            playbackFailed(error: error?.code == URLError.notConnectedToInternet.rawValue
                 ? AudioPlayerError.PlaybackError.notConnectedToInternet
                 : AudioPlayerError.PlaybackError.playbackFailed
            )
        }
    }
}

extension AVPlayerWrapper: AVPlayerTimeObserverDelegate {
    
    // MARK: - AVPlayerTimeObserverDelegate
    
    func audioDidStart() {
        state = .playing
    }
    
    func timeEvent(time: CMTime) {
        delegate?.AVWrapper(secondsElapsed: time.seconds)
    }
    
}

extension AVPlayerWrapper: AVPlayerItemNotificationObserverDelegate {
    // MARK: - AVPlayerItemNotificationObserverDelegate

    func itemFailedToPlayToEndTime() {
        playbackFailed(error: AudioPlayerError.PlaybackError.playbackFailed)
        delegate?.AVWrapperItemFailedToPlayToEndTime()
    }
    
    func itemPlaybackStalled() {
        delegate?.AVWrapperItemPlaybackStalled()
    }
    
    func itemDidPlayToEndTime() {
        delegate?.AVWrapperItemDidPlayToEndTime()
    }
    
}

extension AVPlayerWrapper: AVPlayerItemObserverDelegate {
    // MARK: - AVPlayerItemObserverDelegate

    func item(didUpdatePlaybackLikelyToKeepUp playbackLikelyToKeepUp: Bool) {
        if (playbackLikelyToKeepUp && state != .playing) {
            state = .ready
        }
    }
        
    func item(didUpdateDuration duration: Double) {
        delegate?.AVWrapper(didUpdateDuration: duration)
    }
    
    func item(didReceiveTimedMetadata metadata: [AVTimedMetadataGroup]) {
        delegate?.AVWrapper(didReceiveTimedMetadata: metadata)
    }
}
