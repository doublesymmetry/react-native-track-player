//
//  AudioPlayer.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 15/03/2018.
//

import Foundation
import MediaPlayer

public typealias AudioPlayerState = AVPlayerWrapperState

public class AudioPlayer: AVPlayerWrapperDelegate {
    /// The wrapper around the underlying AVPlayer
    let wrapper: AVPlayerWrapperProtocol = AVPlayerWrapper()

    public let nowPlayingInfoController: NowPlayingInfoControllerProtocol
    public let remoteCommandController: RemoteCommandController
    public let event = EventHolder()

    private(set) var currentItem: AudioItem?

    /**
     Set this to false to disable automatic updating of now playing info for control center and lock screen.
     */
    public var automaticallyUpdateNowPlayingInfo: Bool = true

    /**
     Controls the time pitch algorithm applied to each item loaded into the player.
     If the loaded `AudioItem` conforms to `TimePitcher`-protocol this will be overriden.
     */
    public var audioTimePitchAlgorithm: AVAudioTimePitchAlgorithm = AVAudioTimePitchAlgorithm.timeDomain

    /**
     Default remote commands to use for each playing item
     */
    public var remoteCommands: [RemoteCommand] = [] {
        didSet {
            if let item = currentItem {
                self.enableRemoteCommands(forItem: item)
            }
        }
    }

    /**
     Handles the `playWhenReady` setting while executing a given action.

     This method takes an optional `Bool` value and a closure representing an action to execute.
     If the `Bool` value is not `nil`, `self.playWhenReady` is set accordingly either before or
     after executing the action.

     - Parameters:
       - playWhenReady: Optional `Bool` to set `self.playWhenReady`.
                        - If `true`, `self.playWhenReady` will be set after executing the action.
                        - If `false`, `self.playWhenReady` will be set before executing the action.
                        - If `nil`, `self.playWhenReady` will not be changed.
       - action: A closure representing the action to execute. This closure can throw an error.

     - Throws: This function will propagate any errors thrown by the `action` closure.
    */
    internal func handlePlayWhenReady(_ playWhenReady: Bool?, action: () throws -> Void) rethrows {
        if playWhenReady == false {
            self.playWhenReady = false
        }
        
        try action()
        
        if playWhenReady == true {
            self.playWhenReady = true
        }
    }

    // MARK: - Getters from AVPlayerWrapper

    public var playbackError: AudioPlayerError.PlaybackError? {
        wrapper.playbackError
    }
    
    /**
     The elapsed playback time of the current item.
     */
    public var currentTime: Double {
        wrapper.currentTime
    }

    /**
     The duration of the current AudioItem.
     */
    public var duration: Double {
        wrapper.duration
    }

    /**
     The bufferedPosition of the current AudioItem.
     */
    public var bufferedPosition: Double {
        wrapper.bufferedPosition
    }

    /**
     The current state of the underlying `AudioPlayer`.
     */
    public var playerState: AudioPlayerState {
        wrapper.state
    }

    // MARK: - Setters for AVPlayerWrapper

    /**
     Whether the player should start playing automatically when the item is ready.
     */
    public var playWhenReady: Bool {
        get { wrapper.playWhenReady }
        set {
            wrapper.playWhenReady = newValue
        }
    }
    
    /**
     The amount of seconds to be buffered by the player. Default value is 0 seconds, this means the AVPlayer will choose an appropriate level of buffering. Setting `bufferDuration` to larger than zero automatically disables `automaticallyWaitsToMinimizeStalling`. Setting it back to zero automatically enables `automaticallyWaitsToMinimizeStalling`.

     [Read more from Apple Documentation](https://developer.apple.com/documentation/avfoundation/avplayeritem/1643630-preferredforwardbufferduration)
     */
    public var bufferDuration: TimeInterval {
        get { wrapper.bufferDuration }
        set {
            wrapper.bufferDuration = newValue
            wrapper.automaticallyWaitsToMinimizeStalling = wrapper.bufferDuration == 0
        }
    }

    /**
     Indicates whether the player should automatically delay playback in order to minimize stalling. Setting this to true will also set `bufferDuration` back to `0`.

     [Read more from Apple Documentation](https://developer.apple.com/documentation/avfoundation/avplayer/1643482-automaticallywaitstominimizestal)
     */
    public var automaticallyWaitsToMinimizeStalling: Bool {
        get { wrapper.automaticallyWaitsToMinimizeStalling }
        set {
            if (newValue) {
                wrapper.bufferDuration = 0
            }
            wrapper.automaticallyWaitsToMinimizeStalling = newValue
        }
    }
    
    /**
     Set this to decide how often the player should call the delegate with time progress events.
     */
    public var timeEventFrequency: TimeEventFrequency {
        get { wrapper.timeEventFrequency }
        set { wrapper.timeEventFrequency = newValue }
    }

    public var volume: Float {
        get { wrapper.volume }
        set { wrapper.volume = newValue }
    }

    public var isMuted: Bool {
        get { wrapper.isMuted }
        set { wrapper.isMuted = newValue }
    }

    public var rate: Float {
        get { wrapper.rate }
        set {
            wrapper.rate = newValue
            if (automaticallyUpdateNowPlayingInfo) {
                updateNowPlayingPlaybackValues()
            }
        }
    }

    // MARK: - Init

    /**
     Create a new AudioPlayer.

     - parameter infoCenter: The InfoCenter to update. Default is `MPNowPlayingInfoCenter.default()`.
     */
    public init(nowPlayingInfoController: NowPlayingInfoControllerProtocol = NowPlayingInfoController(),
                remoteCommandController: RemoteCommandController = RemoteCommandController()) {
        self.nowPlayingInfoController = nowPlayingInfoController
        self.remoteCommandController = remoteCommandController

        wrapper.delegate = self
        self.remoteCommandController.audioPlayer = self
    }

    // MARK: - Player Actions

    /**
     Load an AudioItem into the manager.

     - parameter item: The AudioItem to load. The info given in this item is the one used for the InfoCenter.
     - parameter playWhenReady: Optional, whether to start playback when the item is ready.
     */
    public func load(item: AudioItem, playWhenReady: Bool? = nil) {
        handlePlayWhenReady(playWhenReady) {
            currentItem = item

            if (automaticallyUpdateNowPlayingInfo) {
                // Reset playback values without updating, because that will happen in
                // the loadNowPlayingMetaValues call straight after:
                nowPlayingInfoController.setWithoutUpdate(keyValues: [
                    MediaItemProperty.duration(nil),
                    NowPlayingInfoProperty.playbackRate(nil),
                    NowPlayingInfoProperty.elapsedPlaybackTime(nil)
                ])
                loadNowPlayingMetaValues()
            }
            
            enableRemoteCommands(forItem: item)
            
            wrapper.load(
                from: item.getSourceUrl(),
                type: item.getSourceType(),
                playWhenReady: self.playWhenReady,
                initialTime: (item as? InitialTiming)?.getInitialTime(),
                options:(item as? AssetOptionsProviding)?.getAssetOptions()
            )
        }
    }

    /**
     Toggle playback status.
     */
    public func togglePlaying() {
        wrapper.togglePlaying()
    }

    /**
     Start playback
     */
    public func play() {
        wrapper.play()
    }

    /**
     Pause playback
     */
    public func pause() {
        wrapper.pause()
    }

    /**
     Stop playback
     */
    public func stop() {
        let wasActive = wrapper.playbackActive
        wrapper.stop()
        if (wasActive) {
            event.playbackEnd.emit(data: .playerStopped)
        }
    }

    /**
     Reload the current item.
     */
    public func reload(startFromCurrentTime: Bool) {
        wrapper.reload(startFromCurrentTime: startFromCurrentTime)
    }
    
    /**
     Seek to a specific time in the item.
     */
    public func seek(to seconds: TimeInterval) {
        wrapper.seek(to: seconds)
    }

    /**
     Seek by relative a time offset in the item.
     */
    public func seek(by offset: TimeInterval) {
        wrapper.seek(by: offset)
    }
    
    // MARK: - Remote Command Center

    func enableRemoteCommands(_ commands: [RemoteCommand]) {
        remoteCommandController.enable(commands: commands)
    }

    func enableRemoteCommands(forItem item: AudioItem) {
        if let item = item as? RemoteCommandable {
            self.enableRemoteCommands(item.getCommands())
        }
        else {
            self.enableRemoteCommands(remoteCommands)
        }
    }

    /**
     Syncs the current remoteCommands with the iOS command center.
     Can be used to update item states - e.g. like, dislike and bookmark.
     */
    @available(*, deprecated, message: "Directly set .remoteCommands instead")
    public func syncRemoteCommandsWithCommandCenter() {
        self.enableRemoteCommands(remoteCommands)
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
    func updateNowPlayingPlaybackValues() {
        nowPlayingInfoController.set(keyValues: [
            MediaItemProperty.duration(wrapper.duration),
            NowPlayingInfoProperty.playbackRate(wrapper.playWhenReady ? Double(wrapper.rate) : 0),
            NowPlayingInfoProperty.elapsedPlaybackTime(wrapper.currentTime)
        ])
    }

    public func clear() {
        let playbackWasActive = wrapper.playbackActive
        currentItem = nil
        wrapper.unload()
        nowPlayingInfoController.clear()
        if (playbackWasActive) {
            event.playbackEnd.emit(data: .cleared)
        }
    }

    // MARK: - Private

    private func setNowPlayingCurrentTime(seconds: Double) {
        nowPlayingInfoController.set(
            keyValue: NowPlayingInfoProperty.elapsedPlaybackTime(seconds)
        )
    }

    private func loadArtwork(forItem item: AudioItem) {
        item.getArtwork { (image) in
            if let image = image {
                let artwork = MPMediaItemArtwork(boundsSize: image.size, requestHandler: { _ in image })
                self.nowPlayingInfoController.set(keyValue: MediaItemProperty.artwork(artwork))
            } else {
                self.nowPlayingInfoController.set(keyValue: MediaItemProperty.artwork(nil))
            }
        }
    }

    private func setTimePitchingAlgorithmForCurrentItem() {
        if let item = currentItem as? TimePitching {
            wrapper.currentItem?.audioTimePitchAlgorithm = item.getPitchAlgorithmType()
        } else {
            wrapper.currentItem?.audioTimePitchAlgorithm = audioTimePitchAlgorithm
        }
    }

    // MARK: - AVPlayerWrapperDelegate

    func AVWrapper(didChangeState state: AVPlayerWrapperState) {
        switch state {
        case .ready, .loading:
            setTimePitchingAlgorithmForCurrentItem()
        default: break
        }

        switch state {
        case .ready, .loading, .playing, .paused:
            if (automaticallyUpdateNowPlayingInfo) {
                updateNowPlayingPlaybackValues()
            }
        default: break
        }
        event.stateChange.emit(data: state)
    }

    func AVWrapper(secondsElapsed seconds: Double) {
        event.secondElapse.emit(data: seconds)
    }

    func AVWrapper(failedWithError error: Error?) {
        event.fail.emit(data: error)
        event.playbackEnd.emit(data: .failed)
    }

    func AVWrapper(seekTo seconds: Double, didFinish: Bool) {
        if automaticallyUpdateNowPlayingInfo {
            setNowPlayingCurrentTime(seconds: Double(seconds))
        }
        event.seek.emit(data: (seconds, didFinish))
    }

    func AVWrapper(didUpdateDuration duration: Double) {
        event.updateDuration.emit(data: duration)
    }
    
    func AVWrapper(didReceiveCommonMetadata metadata: [AVMetadataItem]) {
        event.receiveCommonMetadata.emit(data: metadata)
    }
    
    func AVWrapper(didReceiveChapterMetadata metadata: [AVTimedMetadataGroup]) {
        event.receiveChapterMetadata.emit(data: metadata)
    }
    
    func AVWrapper(didReceiveTimedMetadata metadata: [AVTimedMetadataGroup]) {
        event.receiveTimedMetadata.emit(data: metadata)
    }

    func AVWrapper(didChangePlayWhenReady playWhenReady: Bool) {
        event.playWhenReadyChange.emit(data: playWhenReady)
    }
    
    func AVWrapperItemDidPlayToEndTime() {
        event.playbackEnd.emit(data: .playedUntilEnd)
        wrapper.state = .ended
    }

    func AVWrapperItemFailedToPlayToEndTime() {
        AVWrapper(failedWithError: AudioPlayerError.PlaybackError.playbackFailed)
    }

    func AVWrapperItemPlaybackStalled() {

    }
    
    func AVWrapperDidRecreateAVPlayer() {
        event.didRecreateAVPlayer.emit(data: ())
    }
}
