//
//  AudioPlayer.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 15/03/2018.
//

import Foundation
import MediaPlayer

public typealias AudioPlayerState = AVPlayerWrapperState

@available(*, deprecated, message: "Delegates will be removed in future versions of SwiftAudio. Use event handlers instead.")
public protocol AudioPlayerDelegate: class {
    
    func audioPlayer(playerDidChangeState state: AudioPlayerState)
    
    func audioPlayer(itemPlaybackEndedWithReason reason: PlaybackEndedReason)
    
    func audioPlayer(secondsElapsed seconds: Double)
    
    func audioPlayer(failedWithError error: Error?)
    
    func audioPlayer(seekTo seconds: Int, didFinish: Bool)
    
    func audioPlayer(didUpdateDuration duration: Double)

}

public class AudioPlayer: AVPlayerWrapperDelegate {
    
    private var _wrapper: AVPlayerWrapperProtocol
    
    /// The wrapper around the underlying AVPlayer
    var wrapper: AVPlayerWrapperProtocol {
        return _wrapper
    }
    
    public let nowPlayingInfoController: NowPlayingInfoControllerProtocol
    public let remoteCommandController: RemoteCommandController
    
    public let event = EventHolder()
    public weak var delegate: AudioPlayerDelegate?
    
    var _currentItem: AudioItem?
    public var currentItem: AudioItem? {
        return _currentItem
    }
    
    /**
     Set this to false to disable automatic updating of now playing info for control center and lock screen.
     */
    public var automaticallyUpdateNowPlayingInfo: Bool = true
    
    /**
     Controls the time pitch algorithm applied to each item loaded into the player.
     If the loaded `AudioItem` conforms to `TimePitcher`-protocol this will be overriden.
     */
    public var audioTimePitchAlgorithm: AVAudioTimePitchAlgorithm = AVAudioTimePitchAlgorithm.lowQualityZeroLatency
    
    /**
     Default remote commands to use for each playing item
     */
    public var remoteCommands: [RemoteCommand] = []
    
    
    // MARK: - Getters from AVPlayerWrapper
    
    /**
     The elapsed playback time of the current item.
     */
    public var currentTime: Double {
        return wrapper.currentTime
    }
    
    /**
     The duration of the current AudioItem.
     */
    public var duration: Double {
        return wrapper.duration
    }
    
    /**
     The current state of the underlying `AudioPlayer`.
     */
    public var playerState: AudioPlayerState {
        return wrapper.state
    }
    
    // MARK: - Setters for AVPlayerWrapper
    
    /**
     The amount of seconds to be buffered by the player. Default value is 0 seconds, this means the AVPlayer will choose an appropriate level of buffering.
     
     [Read more from Apple Documentation](https://developer.apple.com/documentation/avfoundation/avplayeritem/1643630-preferredforwardbufferduration)
     
     - Important: This setting will have no effect if `automaticallyWaitsToMinimizeStalling` is set to `true` in the AVPlayer
     */
    public var bufferDuration: TimeInterval {
        get { return wrapper.bufferDuration }
        set { _wrapper.bufferDuration = newValue }
    }
    
    /**
     Set this to decide how often the player should call the delegate with time progress events.
     */
    public var timeEventFrequency: TimeEventFrequency {
        get { return wrapper.timeEventFrequency }
        set { _wrapper.timeEventFrequency = newValue }
    }
    
    /**
     Indicates whether the player should automatically delay playback in order to minimize stalling
     */
    public var automaticallyWaitsToMinimizeStalling: Bool {
        get { return wrapper.automaticallyWaitsToMinimizeStalling }
        set { _wrapper.automaticallyWaitsToMinimizeStalling = newValue }
    }
    
    public var volume: Float {
        get { return wrapper.volume }
        set { _wrapper.volume = newValue }
    }
    
    public var isMuted: Bool {
        get { return wrapper.isMuted }
        set { _wrapper.isMuted = newValue }
    }

    public var rate: Float {
        get { return wrapper.rate }
        set { _wrapper.rate = newValue }
    }
    
    // MARK: - Init
    
    /**
     Create a new AudioPlayer.
     
     - parameter infoCenter: The InfoCenter to update. Default is `MPNowPlayingInfoCenter.default()`.
     */
    public init(avPlayer: AVPlayer = AVPlayer(),
                nowPlayingInfoController: NowPlayingInfoControllerProtocol = NowPlayingInfoController(),
                remoteCommandController: RemoteCommandController = RemoteCommandController()) {
        self._wrapper = AVPlayerWrapper(avPlayer: avPlayer)
        self.nowPlayingInfoController = nowPlayingInfoController
        self.remoteCommandController = remoteCommandController
        
        self._wrapper.delegate = self
        self.remoteCommandController.audioPlayer = self
    }
    
    // MARK: - Player Actions
    
    /**
     Load an AudioItem into the manager.
     
     - parameter item: The AudioItem to load. The info given in this item is the one used for the InfoCenter.
     - parameter playWhenReady: Immediately start playback when the item is ready. Default is `true`. If you disable this you have to call play() or togglePlay() when the `state` switches to `ready`.
     */
    public func load(item: AudioItem, playWhenReady: Bool = true) throws {
        let url: URL
        switch item.getSourceType() {
        case .stream:
            if let itemUrl = URL(string: item.getSourceUrl()) {
                url = itemUrl
            }
            else {
                throw APError.LoadError.invalidSourceUrl(item.getSourceUrl())
            }
        case .file:
            url = URL(fileURLWithPath: item.getSourceUrl())
        }
        
        wrapper.load(from: url,
                     playWhenReady: playWhenReady,
                     initialTime: (item as? InitialTiming)?.getInitialTime())
        
        if let item = item as? TimePitching {
            wrapper.currentItem?.audioTimePitchAlgorithm = item.getPitchAlgorithmType()
        }
        else {
            wrapper.currentItem?.audioTimePitchAlgorithm = audioTimePitchAlgorithm
        }
        
        self._currentItem = item
        
        if (automaticallyUpdateNowPlayingInfo) {
            self.loadNowPlayingMetaValues()
        }
        enableRemoteCommands(forItem: item)
    }
    
    /**
     Toggle playback status.
     */
    public func togglePlaying() {
        self.wrapper.togglePlaying()
    }
    
    /**
     Start playback
     */
    public func play() {
        self.wrapper.play()
    }
    
    /**
     Pause playback
     */
    public func pause() {
        self.wrapper.pause()
    }
    
    /**
     Stop playback, resetting the player.
     */
    public func stop() {
        self.reset()
        self.wrapper.stop()
        self.event.playbackEnd.emit(data: .playerStopped)
        self.delegate?.audioPlayer(itemPlaybackEndedWithReason: .playerStopped)
    }
    
    /**
     Seek to a specific time in the item.
     */
    public func seek(to seconds: TimeInterval) {
        if automaticallyUpdateNowPlayingInfo {
            self.updateNowPlayingCurrentTime(seconds)
        }
        self.wrapper.seek(to: seconds)
    }
    
    // MARK: - Remote Command Center
    
    func enableRemoteCommands(_ commands: [RemoteCommand]) {
        self.remoteCommandController.enable(commands: commands)
    }
    
    func enableRemoteCommands(forItem item: AudioItem) {
        if let item = item as? RemoteCommandable {
            self.enableRemoteCommands(item.getCommands())
        }
        else {
            self.enableRemoteCommands(remoteCommands)
        }
    }
    
    // MARK: - NowPlayingInfo
    
    /**
     Loads NowPlayingInfo-meta values with the values found in the current `AudioItem`. Use this if a change to the `AudioItem` is made and you want to update the `NowPlayingInfoController`s values.
     
     Reloads:
     - Artist
     - Title
     - Album title
     - Album artwork
     */
    public func loadNowPlayingMetaValues() {
        guard let item = currentItem else { return }
        
        nowPlayingInfoController.set(keyValues: [
            MediaItemProperty.artist(item.getArtist()),
            MediaItemProperty.title(item.getTitle()),
            MediaItemProperty.albumTitle(item.getAlbumTitle()),
        ])
        
        loadArtwork(forItem: item)
    }
    
    /**
     Resyncs the playbackvalues of the currently playing `AudioItem`.
     
     Will resync:
     - Current time
     - Duration
     - Playback rate
     */
    public func updateNowPlayingPlaybackValues() {
        updateNowPlayingDuration(duration)
        updateNowPlayingCurrentTime(currentTime)
        updateNowPlayingRate(rate)
    }
    
    private func updateNowPlayingDuration(_ duration: Double) {
        nowPlayingInfoController.set(keyValue: MediaItemProperty.duration(duration))
    }
    
    private func updateNowPlayingRate(_ rate: Float) {
        nowPlayingInfoController.set(keyValue: NowPlayingInfoProperty.playbackRate(Double(rate)))
    }
    
    private func updateNowPlayingCurrentTime(_ currentTime: Double) {
        nowPlayingInfoController.set(keyValue: NowPlayingInfoProperty.elapsedPlaybackTime(currentTime))
    }
    
    private func loadArtwork(forItem item: AudioItem) {
        item.getArtwork { (image) in
            if let image = image {
                let artwork = MPMediaItemArtwork(boundsSize: image.size, requestHandler: { (size) -> UIImage in
                    return image
                })
                self.nowPlayingInfoController.set(keyValue: MediaItemProperty.artwork(artwork))
            }
        }
    }
    
    // MARK: - Private
    
    func reset() {
        self._currentItem = nil
    }
    
    // MARK: - AVPlayerWrapperDelegate
    
    func AVWrapper(didChangeState state: AVPlayerWrapperState) {
        switch state {
        case .ready:
            if (automaticallyUpdateNowPlayingInfo) {
                updateNowPlayingPlaybackValues()
            }
        case .playing, .paused:
            if (automaticallyUpdateNowPlayingInfo) {
                updateNowPlayingCurrentTime(currentTime)
                updateNowPlayingRate(rate)
            }
        default: break
        }
        self.event.stateChange.emit(data: state)
        self.delegate?.audioPlayer(playerDidChangeState: state)
    }
    
    func AVWrapper(secondsElapsed seconds: Double) {
        self.event.secondElapse.emit(data: seconds)
        self.delegate?.audioPlayer(secondsElapsed: seconds)
    }
    
    func AVWrapper(failedWithError error: Error?) {
        self.event.fail.emit(data: error)
        self.delegate?.audioPlayer(failedWithError: error)
    }
    
    func AVWrapper(seekTo seconds: Int, didFinish: Bool) {
        if !didFinish && automaticallyUpdateNowPlayingInfo {
            updateNowPlayingCurrentTime(currentTime)
        }
        self.event.seek.emit(data: (seconds, didFinish))
        self.delegate?.audioPlayer(seekTo: seconds, didFinish: didFinish)
    }
    
    func AVWrapper(didUpdateDuration duration: Double) {
        self.event.updateDuration.emit(data: duration)
        self.delegate?.audioPlayer(didUpdateDuration: duration)
    }
    
    func AVWrapperItemDidPlayToEndTime() {
        self.event.playbackEnd.emit(data: .playedUntilEnd)
        self.delegate?.audioPlayer(itemPlaybackEndedWithReason: .playedUntilEnd)
    }
    
}
