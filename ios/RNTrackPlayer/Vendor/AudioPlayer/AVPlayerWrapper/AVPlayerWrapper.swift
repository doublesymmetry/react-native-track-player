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
}

protocol AVPlayerWrapperDelegate: class {
    
    func AVWrapper(didChangeState state: AVPlayerWrapperState)
    func AVWrapper(itemPlaybackDoneWithReason reason: PlaybackEndedReason)
    func AVWrapper(secondsElapsed seconds: Double)
    func AVWrapper(failedWithError error: Error?)
    func AVWrapper(seekTo seconds: Int, didFinish: Bool)
    func AVWrapper(didUpdateDuration duration: Double)
    
}

class AVPlayerWrapper {
    
    struct Constants {
        static let assetPlayableKey = "playable"
    }
    
    // MARK: - Properties
    
    let avPlayer: AVPlayer
    let playerObserver: AVPlayerObserver
    let playerTimeObserver: AVPlayerTimeObserver
    let playerItemNotificationObserver: AVPlayerItemNotificationObserver
    let playerItemObserver: AVPlayerItemObserver
    
    /**
     True if the last call to load(from:playWhenReady) had playWhenReady=true.
     Cannot be set directly.
     */
    var playWhenReady: Bool { return _playWhenReady }
    
    fileprivate var _playWhenReady: Bool = true
    
    /**
     The current `AudioPlayerState` of the player.
     */
    var state: AVPlayerWrapperState { return _state }
    
    fileprivate var _state: AVPlayerWrapperState = AVPlayerWrapperState.idle {
        didSet {
            if oldValue != _state {
                self.delegate?.AVWrapper(didChangeState: _state)
            }
        }
    }
    
    /**
     The delegate receiving events.
     */
    weak var delegate: AVPlayerWrapperDelegate?
    
    // MARK: - AVPlayer Get Properties
    
    /**
     The AVAsset for the currentItem.
    */
    var currentAsset: AVAsset? {
        return currentItem?.asset
    }
    
    /**
     The current item of the AVPlayer.
     */
    var currentItem: AVPlayerItem? {
        return avPlayer.currentItem
    }
    
    /**
     The duration of the current item.
     */
    var duration: Double {
        if let seconds = currentItem?.duration.seconds, !seconds.isNaN {
            return seconds
        }
        return 0
    }
    
    /**
     The current time of the item in the player.
     */
    var currentTime: Double {
        let seconds = avPlayer.currentTime().seconds
        return seconds.isNaN ? 0 : seconds
    }
    
    /**
     The rate of the AVPlayer
     Default is 1.0
     */
    var rate: Float {
        get { return avPlayer.rate }
        set { avPlayer.rate = newValue }
    }
    
    // MARK: - AVPlayer Config Properties
    
    /**
     Indicates wether the player should automatically delay playback in order to minimize stalling.
     [Read more from Apple Documentation](https://developer.apple.com/documentation/avfoundation/avplayer/1643482-automaticallywaitstominimizestal)
     */
    var automaticallyWaitsToMinimizeStalling: Bool {
        get { return avPlayer.automaticallyWaitsToMinimizeStalling }
        set { avPlayer.automaticallyWaitsToMinimizeStalling = newValue }
    }
    
    /**
     The amount of seconds to be buffered by the player. Default value is 0 seconds, this means the AVPlayer will choose an appropriate level of buffering.
     
     [Read more from Apple Documentation](https://developer.apple.com/documentation/avfoundation/avplayeritem/1643630-preferredforwardbufferduration)
     
     - Important: This setting will have no effect if `automaticallyWaitsToMinimizeStalling` is set to `true`
     */
    var bufferDuration: TimeInterval
    
    /**
     Set this to decide how often the player should call the delegate with time progress events.
     */
    var timeEventFrequency: TimeEventFrequency {
        didSet {
            playerTimeObserver.periodicObserverTimeInterval = timeEventFrequency.getTime()
        }
    }
    
    /**
     The player volume, from 0.0 to 1.0
     Default is 1.0
     */
    public var volume: Float {
        get { return avPlayer.volume }
        set { avPlayer.volume = newValue }
    }
    
    // MARK: - Public Methods
    
    public init(timeEventFrequency: TimeEventFrequency =  .everySecond) {
        
        self.avPlayer = AVPlayer()
        self.playerObserver = AVPlayerObserver(player: avPlayer)
        self.playerTimeObserver = AVPlayerTimeObserver(player: avPlayer, periodicObserverTimeInterval: timeEventFrequency.getTime())
        self.playerItemNotificationObserver = AVPlayerItemNotificationObserver()
        self.playerItemObserver = AVPlayerItemObserver()

        self.bufferDuration = 0
        self.timeEventFrequency = timeEventFrequency
        
        self.playerObserver.delegate = self
        self.playerTimeObserver.delegate = self
        self.playerItemNotificationObserver.delegate = self
        self.playerItemObserver.delegate = self
        
        playerTimeObserver.registerForPeriodicTimeEvents()
    }
    
    /**
     Start playback.
     
     - throws: APError.PlaybackError
     */
    func play() throws {
        guard currentItem != nil else {
            throw APError.PlaybackError.noLoadedItem
        }
        
        guard avPlayer.timeControlStatus == .paused else {
            return
        }
        
        avPlayer.play()
    }
    
    /**
     Will pause playback.
     
     - throws: APError.PlaybackError
     */
    func pause() throws {
        guard currentItem != nil else {
            throw APError.PlaybackError.noLoadedItem
        }
        
        guard avPlayer.timeControlStatus == .playing || avPlayer.timeControlStatus == .waitingToPlayAtSpecifiedRate else {
            return
        }
        
        avPlayer.pause()
    }
    
    /**
     Will toggle playback.
     */
    func togglePlaying() throws {
        switch avPlayer.timeControlStatus {
        case .playing, .waitingToPlayAtSpecifiedRate:
            try pause()
        case .paused:
            try play()
        }
    }
    
    /**
     Stop the player and remove the currently playing item.
     */
    func stop() {
        try? pause()
        reset(soft: false)
    }
    
    /**
     Seek to a point in the item.
     
     - parameter seconds: The point to move the player head, in seconds. If the given value is less than 0, 0 is used. If the value is larger than the duration, the duration is used.
     - throws: `APError.PlaybackError`
    */
    func seek(to seconds: TimeInterval) throws {
        guard currentItem != nil else {
            throw APError.PlaybackError.noLoadedItem
        }
        
        avPlayer.seek(to: CMTimeMakeWithSeconds(seconds, 1)) { (finished) in
            self.delegate?.AVWrapper(seekTo: Int(seconds), didFinish: finished)
        }
    }
    
    /**
     Load an item from a URL string. Use this when streaming sound.
     
     - parameter urlString: The AudioSource to load the item from.
     - parameter playWhenReady: Whether playback should start immediately when the item is ready. Default is `true`
     */
    func load(fromUrlString urlString: String, playWhenReady: Bool = true) throws {
        guard let url = URL(string: urlString) else {
            throw APError.LoadError.invalidSourceUrl(urlString)
        }
        
        self.load(from: url, playWhenReady: playWhenReady)
    }
    
    /**
     Load an item from a file. Use this when playing local.
     
     - parameter filePath: The path to the sound file.
     - parameter playWhenReady: Whether playback should start immediately when the item is ready. Default is `true`
     */
    func load(fromFilePath filePath: String, playWhenReady: Bool = true) throws {
        let url = URL(fileURLWithPath: filePath)
        self.load(from: url, playWhenReady: playWhenReady)
    }
    
    // MARK: - Private
    
    private func load(from url: URL, playWhenReady: Bool) {
        
        reset(soft: true)
        _playWhenReady = playWhenReady
        _state = .loading
        
        // Set item
        let currentAsset = AVURLAsset(url: url)
        let currentItem = AVPlayerItem(asset: currentAsset, automaticallyLoadedAssetKeys: [Constants.assetPlayableKey])
        currentItem.preferredForwardBufferDuration = bufferDuration
        avPlayer.replaceCurrentItem(with: currentItem)
        
        // Register for events
        playerTimeObserver.registerForBoundaryTimeEvents()
        playerObserver.startObserving()
        playerItemNotificationObserver.startObserving(item: currentItem)
        playerItemObserver.startObserving(item: currentItem)
    }
    
    /**
     Reset to get ready for playing from a different source.
     */
    private func reset(soft: Bool) {
        if !soft {
            avPlayer.replaceCurrentItem(with: nil)
        }
        
        playerTimeObserver.unregisterForBoundaryTimeEvents()
        playerItemNotificationObserver.stopObservingCurrentItem()
    }
    
}

extension AVPlayerWrapper: AVPlayerObserverDelegate {
    
    // MARK: - AVPlayerObserverDelegate
    
    func player(didChangeTimeControlStatus status: AVPlayerTimeControlStatus) {
        switch status {
        case .paused:
            if currentItem == nil {
                _state = .idle
            }
            else {
                self._state = .paused
            }
        case .waitingToPlayAtSpecifiedRate:
            self._state = .loading
        case .playing:
            self._state = .playing
        }
    }
    
    func player(statusDidChange status: AVPlayerStatus) {
        switch status {

        case .readyToPlay:
            self._state = .ready
            if _playWhenReady {
                try? self.play()
            }
            break

        case .failed:
            self.delegate?.AVWrapper(failedWithError: avPlayer.error)
            break
            
        case .unknown:
            break
        }
    }
    
}

extension AVPlayerWrapper: AVPlayerTimeObserverDelegate {
    
    // MARK: - AVPlayerTimeObserverDelegate
    
    func audioDidStart() {
        self._state = .playing
    }
    
    func timeEvent(time: CMTime) {
        self.delegate?.AVWrapper(secondsElapsed: time.seconds)
    }
    
}

extension AVPlayerWrapper: AVPlayerItemNotificationObserverDelegate {
    
    // MARK: - AVPlayerItemNotificationObserverDelegate
    
    func itemDidPlayToEndTime() {
        delegate?.AVWrapper(itemPlaybackDoneWithReason: .playedUntilEnd)
    }
    
}

extension AVPlayerWrapper: AVPlayerItemObserverDelegate {
    
    // MARK: - AVPlayerItemObserverDelegate
    
    func item(didUpdateDuration duration: Double) {
        self.delegate?.AVWrapper(didUpdateDuration: duration)
    }
    
}
