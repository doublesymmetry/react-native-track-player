//
//  AudioPlayer.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 15/03/2018.
//

import Foundation
import MediaPlayer

public typealias AudioPlayerState = AVPlayerWrapperState

public protocol AudioPlayerDelegate: class {
    
    func audioPlayer(playerDidChangeState state: AudioPlayerState)
    
    func audioPlayer(itemPlaybackEndedWithReason reason: PlaybackEndedReason)
    
    func audioPlayer(secondsElapsed seconds: Double)
    
    func audioPlayer(failedWithError error: Error?)
    
    func audioPlayer(seekTo seconds: Int, didFinish: Bool)
    
    func audioPlayer(didUpdateDuration duration: Double)

}

/**
 The main AudioPlayer.
 - warning: DO NOT USE THIS CLASS, use `SimpleAudioPlayer` or `QueuedAudioPlayer`
 */
public class AudioPlayer: AVPlayerWrapperDelegate {
    
    let wrapper: AVPlayerWrapper
    let nowPlayingInfoController: NowPlayingInfoController
    public let remoteCommandController: RemoteCommandController
    
    var _currentItem: AudioItem?
    
    public weak var delegate: AudioPlayerDelegate?
    public var currentItem: AudioItem? {
        return _currentItem
    }
    
    /**
     Set this to false to disable automatic updating of now playing info for control center and lock screen.
     */
    public var automaticallyUpdateNowPlayingInfo: Bool = true
    
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
     Indicates wether the player should automatically delay playback in order to minimize stalling.
     [Read more from Apple Documentation](https://developer.apple.com/documentation/avfoundation/avplayer/1643482-automaticallywaitstominimizestal)
     */
    public var automaticallyWaitsToMinimizeStalling: Bool {
        get { return wrapper.automaticallyWaitsToMinimizeStalling }
        set { wrapper.automaticallyWaitsToMinimizeStalling = newValue }
    }
    
    /**
     The amount of seconds to be buffered by the player. Default value is 0 seconds, this means the AVPlayer will choose an appropriate level of buffering.
     
     [Read more from Apple Documentation](https://developer.apple.com/documentation/avfoundation/avplayeritem/1643630-preferredforwardbufferduration)
     
     - Important: This setting will have no effect if `automaticallyWaitsToMinimizeStalling` is set to `true`
     */
    public var bufferDuration: TimeInterval {
        get { return wrapper.bufferDuration }
        set { wrapper.bufferDuration = newValue }
    }
    
    /**
     Set this to decide how often the player should call the delegate with time progress events.
     */
    public var timeEventFrquency: TimeEventFrequency {
        get { return wrapper.timeEventFrequency }
        set { wrapper.timeEventFrequency = newValue }
    }
    
    /**
     The player volume, from 0.0 to 1.0
     Default is 1.0
     */
    public var volume: Float {
        get { return wrapper.volume }
        set { wrapper.volume = newValue }
    }
    
    /**
     The player rate
     Default is 1.0
     */
    public var rate: Float {
        get { return wrapper.rate }
        set { wrapper.rate = newValue }
    }
    
    // MARK: - Init
    
    /**
     Create a new AudioPlayer.
     
     - parameter infoCenter: The InfoCenter to update. Default is `MPNowPlayingInfoCenter.default()`.
     */
    init(infoCenter: MPNowPlayingInfoCenter = MPNowPlayingInfoCenter.default(), remoteCommandController: RemoteCommandController? = nil) {
        self.wrapper = AVPlayerWrapper()
        self.nowPlayingInfoController = NowPlayingInfoController(infoCenter: infoCenter)
        self.remoteCommandController = remoteCommandController ?? RemoteCommandController()
        
        self.wrapper.delegate = self
        self.remoteCommandController.audioPlayer = self
    }
    
    // MARK: - Player Actions
    
    /**
     Load an AudioItem into the manager.
     
     - parameter item: The AudioItem to load. The info given in this item is the one used for the InfoCenter.
     - parameter playWhenReady: Immediately start playback when the item is ready. Default is `true`. If you disable this you have to call play() or togglePlay() when the `state` switches to `ready`.
     */
    func loadItem(_ item: AudioItem, playWhenReady: Bool = true) throws {
        print("Loading: \(item)")
        switch item.getSourceType() {
        case .stream:
            try self.wrapper.load(fromUrlString: item.getSourceUrl(), playWhenReady: playWhenReady)
        case .file:
            try self.wrapper.load(fromFilePath: item.getSourceUrl(), playWhenReady: playWhenReady)
        }
        
        wrapper.currentItem?.audioTimePitchAlgorithm = item.getPitchAlgorithmType() as String
        
        self._currentItem = item
        set(item: item)
        setArtwork(forItem: item)
        enableRemoteCommands(forItem: item)
    }
    
    /**
     Toggle playback status.
     */
    public func togglePlaying() throws {
        try self.wrapper.togglePlaying()
    }
    
    /**
     Start playback
     */
    public func play() throws {
        try self.wrapper.play()
    }
    
    /**
     Pause playback
     */
    public func pause() throws {
        try self.wrapper.pause()
    }
    
    /**
     Stop playback, resetting the player.
     */
    public func stop() {
        AVWrapper(itemPlaybackDoneWithReason: .playerStopped)
        self.reset()
        self.wrapper.stop()
    }
    
    /**
     Seek to a specific time in the item.
     */
    public func seek(to seconds: TimeInterval) throws {
        try self.wrapper.seek(to: seconds)
    }
    
    // MARK: - Remote Command Center
    
    /**
     Set the remote commands that should be activated and handled.
     Calling this will disable all earlier enabled commands, so include all commands you need.
     */
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
     Reloads the NowPlayingInfo from the current AudioItem.
     */
    public func reloadNowPlayingInfo() {
        guard let item = currentItem else { return }
        set(item: item)
        setArtwork(forItem: item)
        updatePlaybackValues()
    }
    
    public func add(property: NowPlayingInfoKeyValue) {
        self.nowPlayingInfoController.set(keyValue: property)
    }
    
    func set(item: AudioItem) {
        guard automaticallyUpdateNowPlayingInfo else { return }

        nowPlayingInfoController.set(keyValues: [
            MediaItemProperty.artist(item.getArtist()),
            MediaItemProperty.title(item.getTitle()),
            MediaItemProperty.albumTitle(item.getAlbumTitle()),
            ])
    }
    
    func setArtwork(forItem item: AudioItem) {
        guard automaticallyUpdateNowPlayingInfo else { return }
        item.getArtwork { (image) in
            if let image = image {
                
                let artwork = MPMediaItemArtwork(boundsSize: image.size, requestHandler: { (size) -> UIImage in
                    return image
                })
                
                self.nowPlayingInfoController.set(keyValue: MediaItemProperty.artwork(artwork))
            }
        }
    }
    
    func updatePlaybackValues() {
        guard automaticallyUpdateNowPlayingInfo else { return }
        nowPlayingInfoController.set(keyValue: NowPlayingInfoProperty.elapsedPlaybackTime(wrapper.currentTime))
        nowPlayingInfoController.set(keyValue: MediaItemProperty.duration(wrapper.duration))
        nowPlayingInfoController.set(keyValue: NowPlayingInfoProperty.playbackRate(Double(wrapper.rate)))
    }
    
    // MARK: - Private
    
    private func reset() {
        self._currentItem = nil
    }
    
    // MARK: - AVPlayerWrapperDelegate
    
    func AVWrapper(didChangeState state: AVPlayerWrapperState) {
        switch state {
        case .playing, .paused: updatePlaybackValues()
        default: break
        }
        self.delegate?.audioPlayer(playerDidChangeState: state)
    }
    
    func AVWrapper(itemPlaybackDoneWithReason reason: PlaybackEndedReason) {
        self.delegate?.audioPlayer(itemPlaybackEndedWithReason: reason)
    }
    
    func AVWrapper(secondsElapsed seconds: Double) {
        self.delegate?.audioPlayer(secondsElapsed: seconds)
    }
    
    func AVWrapper(failedWithError error: Error?) {
        self.delegate?.audioPlayer(failedWithError: error)
    }
    
    func AVWrapper(seekTo seconds: Int, didFinish: Bool) {
        self.updatePlaybackValues()
        self.delegate?.audioPlayer(seekTo: seconds, didFinish: didFinish)
    }
    
    func AVWrapper(didUpdateDuration duration: Double) {
        self.delegate?.audioPlayer(didUpdateDuration: duration)
    }
    
}
