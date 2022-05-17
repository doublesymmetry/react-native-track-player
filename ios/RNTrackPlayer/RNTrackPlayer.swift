//
//  RNTrackPlayer.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 13.08.17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

import Foundation
import MediaPlayer
import SwiftAudioEx

@objc(RNTrackPlayer)
public class RNTrackPlayer: RCTEventEmitter, AudioSessionControllerDelegate {

    // MARK: - Attributes

    private var hasInitialized = false
    private let player = QueuedAudioPlayer()
    private let audioSessionController = AudioSessionController.shared
    private var shouldEmitUpdateEventInterval: Bool = false

    // MARK: - Lifecycle Methods

    public override init() {
        super.init()

        audioSessionController.delegate = self
        player.event.playbackEnd.addListener(self, handleAudioPlayerPlaybackEnded)
        player.event.receiveMetadata.addListener(self, handleAudioPlayerMetadataReceived)
        player.event.stateChange.addListener(self, handleAudioPlayerStateChange)
        player.event.fail.addListener(self, handleAudioPlayerFailed)
        player.event.queueIndex.addListener(self, handleAudioPlayerQueueIndexChange)
        player.event.secondElapse.addListener(self, handleAudioPlayerSecondElapse)
    }

    deinit {
        reset(resolve: { _ in }, reject: { _, _, _  in })
    }

    // MARK: - RCTEventEmitter

    override public static func requiresMainQueueSetup() -> Bool {
        return true;
    }

    @objc(constantsToExport)
    override public func constantsToExport() -> [AnyHashable: Any] {
        return [
            "STATE_NONE": State.none.rawValue,
            "STATE_READY": State.ready.rawValue,
            "STATE_PLAYING": State.playing.rawValue,
            "STATE_PAUSED": State.paused.rawValue,
            "STATE_STOPPED": State.stopped.rawValue,
            "STATE_BUFFERING": State.buffering.rawValue,
            "STATE_CONNECTING": State.connecting.rawValue,

            "TRACK_PLAYBACK_ENDED_REASON_END": PlaybackEndedReason.playedUntilEnd.rawValue,
            "TRACK_PLAYBACK_ENDED_REASON_JUMPED": PlaybackEndedReason.jumpedToIndex.rawValue,
            "TRACK_PLAYBACK_ENDED_REASON_NEXT": PlaybackEndedReason.skippedToNext.rawValue,
            "TRACK_PLAYBACK_ENDED_REASON_PREVIOUS": PlaybackEndedReason.skippedToPrevious.rawValue,
            "TRACK_PLAYBACK_ENDED_REASON_STOPPED": PlaybackEndedReason.playerStopped.rawValue,

            "PITCH_ALGORITHM_LINEAR": PitchAlgorithm.linear.rawValue,
            "PITCH_ALGORITHM_MUSIC": PitchAlgorithm.music.rawValue,
            "PITCH_ALGORITHM_VOICE": PitchAlgorithm.voice.rawValue,

            "CAPABILITY_PLAY": Capability.play.rawValue,
            "CAPABILITY_PLAY_FROM_ID": "NOOP",
            "CAPABILITY_PLAY_FROM_SEARCH": "NOOP",
            "CAPABILITY_PAUSE": Capability.pause.rawValue,
            "CAPABILITY_STOP": Capability.stop.rawValue,
            "CAPABILITY_SEEK_TO": Capability.seek.rawValue,
            "CAPABILITY_SKIP": "NOOP",
            "CAPABILITY_SKIP_TO_NEXT": Capability.next.rawValue,
            "CAPABILITY_SKIP_TO_PREVIOUS": Capability.previous.rawValue,
            "CAPABILITY_SET_RATING": "NOOP",
            "CAPABILITY_JUMP_FORWARD": Capability.jumpForward.rawValue,
            "CAPABILITY_JUMP_BACKWARD": Capability.jumpBackward.rawValue,
            "CAPABILITY_LIKE": Capability.like.rawValue,
            "CAPABILITY_DISLIKE": Capability.dislike.rawValue,
            "CAPABILITY_BOOKMARK": Capability.bookmark.rawValue,

            "REPEAT_OFF": RepeatMode.off.rawValue,
            "REPEAT_TRACK": RepeatMode.track.rawValue,
            "REPEAT_QUEUE": RepeatMode.queue.rawValue,
        ]
    }

    @objc(supportedEvents)
    override public func supportedEvents() -> [String] {
        return [
            "playback-queue-ended",
            "playback-state",
            "playback-error",
            "playback-track-changed",
            "playback-metadata-received",
            "playback-progress-updated",

            "remote-stop",
            "remote-pause",
            "remote-play",
            "remote-duck",
            "remote-next",
            "remote-seek",
            "remote-previous",
            "remote-jump-forward",
            "remote-jump-backward",
            "remote-like",
            "remote-dislike",
            "remote-bookmark",
        ]
    }

    // MARK: - AudioSessionControllerDelegate

    public func handleInterruption(type: InterruptionType) {
        switch type {
        case .began:
            // Interruption began, take appropriate actions (save state, update user interface)
            self.sendEvent(withName: "remote-duck", body: [
                "paused": true
            ])
        case let .ended(shouldResume):
            if shouldResume {
                // Interruption Ended - playback should resume
                self.sendEvent(withName: "remote-duck", body: [
                    "paused": false
                ])
            } else {
                // Interruption Ended - playback should NOT resume
                self.sendEvent(withName: "remote-duck", body: [
                    "paused": true,
                    "permanent": true
                ])
            }
        }
    }

    // MARK: - Bridged Methods

    @objc(setupPlayer:resolver:rejecter:)
    public func setupPlayer(config: [String: Any], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if hasInitialized {
            reject("player_already_initialized", "The player has already been initialized via setupPlayer.", nil)
            return
        }

        // configure if player waits to play
        let autoWait: Bool = config["waitForBuffer"] as? Bool ?? false
        player.automaticallyWaitsToMinimizeStalling = autoWait

        // configure buffer size
        let minBuffer: TimeInterval = config["minBuffer"] as? TimeInterval ?? 0
        player.bufferDuration = minBuffer

        // configure if control center metdata should auto update
        let autoUpdateMetadata: Bool = config["autoUpdateMetadata"] as? Bool ?? true
        player.automaticallyUpdateNowPlayingInfo = autoUpdateMetadata

        // configure audio session - category, options & mode
        var sessionCategory: AVAudioSession.Category = .playback
        var sessionCategoryMode: AVAudioSession.Mode = .default
        var sessionCategoryPolicy: AVAudioSession.RouteSharingPolicy = .default
        var sessionCategoryOptions: AVAudioSession.CategoryOptions = []

        if
            let sessionCategoryStr = config["iosCategory"] as? String,
            let mappedCategory = SessionCategory(rawValue: sessionCategoryStr) {
            sessionCategory = mappedCategory.mapConfigToAVAudioSessionCategory()
        }

        if
            let sessionCategoryModeStr = config["iosCategoryMode"] as? String,
            let mappedCategoryMode = SessionCategoryMode(rawValue: sessionCategoryModeStr) {
            sessionCategoryMode = mappedCategoryMode.mapConfigToAVAudioSessionCategoryMode()
        }

        if
            let sessionCategoryPolicyStr = config["iosCategoryPolicy"] as? String,
            let mappedCategoryPolicy = SessionCategoryPolicy(rawValue: sessionCategoryPolicyStr) {
            sessionCategoryPolicy = mappedCategoryPolicy.mapConfigToAVAudioSessionCategoryPolicy()
        }

        let sessionCategoryOptsStr = config["iosCategoryOptions"] as? [String]
        let mappedCategoryOpts = sessionCategoryOptsStr?.compactMap { SessionCategoryOptions(rawValue: $0)?.mapConfigToAVAudioSessionCategoryOptions() } ?? []
        sessionCategoryOptions = AVAudioSession.CategoryOptions(mappedCategoryOpts)

        if #available(iOS 13.0, *) {
            try? AVAudioSession.sharedInstance().setCategory(sessionCategory, mode: sessionCategoryMode, policy: sessionCategoryPolicy, options: sessionCategoryOptions)
        } else if #available(iOS 11.0, *) {
            try? AVAudioSession.sharedInstance().setCategory(sessionCategory, mode: sessionCategoryMode, policy: sessionCategoryPolicy, options: sessionCategoryOptions)
        } else {
            try? AVAudioSession.sharedInstance().setCategory(sessionCategory, mode: sessionCategoryMode, options: sessionCategoryOptions)
        }

        // setup event listeners
        player.remoteCommandController.handleChangePlaybackPositionCommand = { [weak self] event in
            if let event = event as? MPChangePlaybackPositionCommandEvent {
                self?.sendEvent(withName: "remote-seek", body: ["position": event.positionTime])
                return MPRemoteCommandHandlerStatus.success
            }

            return MPRemoteCommandHandlerStatus.commandFailed
        }

        player.remoteCommandController.handleNextTrackCommand = { [weak self] _ in
            self?.sendEvent(withName: "remote-next", body: nil)
            return MPRemoteCommandHandlerStatus.success
        }

        player.remoteCommandController.handlePauseCommand = { [weak self] _ in
            self?.sendEvent(withName: "remote-pause", body: nil)
            return MPRemoteCommandHandlerStatus.success
        }

        player.remoteCommandController.handlePlayCommand = { [weak self] _ in
            self?.sendEvent(withName: "remote-play", body: nil)
            return MPRemoteCommandHandlerStatus.success
        }

        player.remoteCommandController.handlePreviousTrackCommand = { [weak self] _ in
            self?.sendEvent(withName: "remote-previous", body: nil)
            return MPRemoteCommandHandlerStatus.success
        }

        player.remoteCommandController.handleSkipBackwardCommand = { [weak self] event in
            if let command = event.command as? MPSkipIntervalCommand,
               let interval = command.preferredIntervals.first {
                self?.sendEvent(withName: "remote-jump-backward", body: ["interval": interval])
                return MPRemoteCommandHandlerStatus.success
            }

            return MPRemoteCommandHandlerStatus.commandFailed
        }

        player.remoteCommandController.handleSkipForwardCommand = { [weak self] event in
            if let command = event.command as? MPSkipIntervalCommand,
               let interval = command.preferredIntervals.first {
                self?.sendEvent(withName: "remote-jump-forward", body: ["interval": interval])
                return MPRemoteCommandHandlerStatus.success
            }

            return MPRemoteCommandHandlerStatus.commandFailed
        }

        player.remoteCommandController.handleStopCommand = { [weak self] _ in
            self?.sendEvent(withName: "remote-stop", body: nil)
            return MPRemoteCommandHandlerStatus.success
        }

        player.remoteCommandController.handleTogglePlayPauseCommand = { [weak self] _ in
            if self?.player.playerState == .paused {
                self?.sendEvent(withName: "remote-play", body: nil)
                return MPRemoteCommandHandlerStatus.success
            }

            self?.sendEvent(withName: "remote-pause", body: nil)
            return MPRemoteCommandHandlerStatus.success
        }

        player.remoteCommandController.handleLikeCommand = { [weak self] _ in
            self?.sendEvent(withName: "remote-like", body: nil)
            return MPRemoteCommandHandlerStatus.success
        }

        player.remoteCommandController.handleDislikeCommand = { [weak self] _ in
            self?.sendEvent(withName: "remote-dislike", body: nil)
            return MPRemoteCommandHandlerStatus.success
        }

        player.remoteCommandController.handleBookmarkCommand = { [weak self] _ in
            self?.sendEvent(withName: "remote-bookmark", body: nil)
            return MPRemoteCommandHandlerStatus.success
        }

        hasInitialized = true
        resolve(NSNull())
    }

    @objc(isServiceRunning:rejecter:)
    public func isServiceRunning(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        // TODO That is probably always true
        resolve(player != nil)
    }

    @objc(destroy:rejecter:)
    public func destroy(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        print("Destroying player")
        self.player.stop()
        self.player.nowPlayingInfoController.clear()
        try? AVAudioSession.sharedInstance().setActive(false)
        hasInitialized = false
    }

    @objc(updateOptions:resolver:rejecter:)
    public func update(options: [String: Any], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        var capabilitiesStr = options["capabilities"] as? [String] ?? []
        if (capabilitiesStr.contains("play") && capabilitiesStr.contains("pause")) {
            capabilitiesStr.append("togglePlayPause");
        }
        let capabilities = capabilitiesStr.compactMap { Capability(rawValue: $0) }

        player.remoteCommands = capabilities.map { capability in
            capability.mapToPlayerCommand(forwardJumpInterval: options["forwardJumpInterval"] as? NSNumber,
                                          backwardJumpInterval: options["backwardJumpInterval"] as? NSNumber,
                                          likeOptions: options["likeOptions"] as? [String: Any],
                                          dislikeOptions: options["dislikeOptions"] as? [String: Any],
                                          bookmarkOptions: options["bookmarkOptions"] as? [String: Any])
        }

        if let interval = options["progressUpdateEventInterval"] as? NSNumber, interval.intValue > 0 {
            shouldEmitUpdateEventInterval = true
            configureProgressUpdateEvent(interval: interval.doubleValue)
        } else {
            shouldEmitUpdateEventInterval = false
        }

        resolve(NSNull())
    }

    private func configureProgressUpdateEvent(interval: Double) {
        let time = CMTime(seconds: interval, preferredTimescale: 1)
        self.player.timeEventFrequency = .custom(time: time)
    }

    @objc(add:before:resolver:rejecter:)
    public func add(trackDicts: [[String: Any]], before trackIndex: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            UIApplication.shared.beginReceivingRemoteControlEvents();
        }

        var tracks = [Track]()
        for trackDict in trackDicts {
            guard let track = Track(dictionary: trackDict) else {
                reject("invalid_track_object", "Track is missing a required key", nil)
                return
            }

            tracks.append(track)
        }

        var index: Int = 0
        if (trackIndex.intValue < -1 || trackIndex.intValue > player.items.count) {
            reject("index_out_of_bounds", "The track index is out of bounds", nil)
        } else if trackIndex.intValue == -1 { // -1 means no index was passed and therefore should be inserted at the end.
            index = player.items.count
            try? player.add(items: tracks, playWhenReady: false)
        } else {
            index = trackIndex.intValue
            try? player.add(items: tracks, at: trackIndex.intValue)
        }

        resolve(index)
    }

    @objc(remove:resolver:rejecter:)
    public func remove(tracks indexes: [Int], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        for index in indexes {
            // we do not allow removal of the current item
            if index == player.currentIndex { continue }
            try? player.removeItem(at: index)
        }

        resolve(NSNull())
    }

    @objc(removeUpcomingTracks:rejecter:)
    public func removeUpcomingTracks(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        player.removeUpcomingItems()
        resolve(NSNull())
    }

    @objc(skip:initialTime:resolver:rejecter:)
    public func skip(
        to trackIndex: NSNumber,
        initialTime: Double,
        resolve: RCTPromiseResolveBlock,
        reject: RCTPromiseRejectBlock
    ) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        if (trackIndex.intValue < 0 || trackIndex.intValue >= player.items.count) {
            reject("index_out_of_bounds", "The track index is out of bounds", nil)
            return
        }

        print("Skipping to track:", trackIndex)
        try? player.jumpToItem(atIndex: trackIndex.intValue, playWhenReady: player.playerState == .playing)

        // if an initialTime is passed the seek to it
        if (initialTime >= 0) {
            self.seek(to: initialTime, resolve: resolve, reject: reject)
        } else {
            resolve(NSNull())
        }
    }

    @objc(skipToNext:resolver:rejecter:)
    public func skipToNext(
        initialTime: Double,
        resolve: RCTPromiseResolveBlock,
        reject: RCTPromiseRejectBlock
    ) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        do {
            try player.next()

            // if an initialTime is passed the seek to it
            if (initialTime >= 0) {
                self.seek(to: initialTime, resolve: resolve, reject: reject)
            } else {
                resolve(NSNull())
            }
        } catch (_) {
            reject("queue_exhausted", "There is no tracks left to play", nil)
        }
    }

    @objc(skipToPrevious:resolver:rejecter:)
    public func skipToPrevious(
        initialTime: Double,
        resolve: RCTPromiseResolveBlock,
        reject: RCTPromiseRejectBlock
    ) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        do {
            try player.previous()

            // if an initialTime is passed the seek to it
            if (initialTime >= 0) {
                self.seek(to: initialTime, resolve: resolve, reject: reject)
            } else {
                resolve(NSNull())
            }
        } catch (_) {
            reject("no_previous_track", "There is no previous track", nil)
        }
    }

    @objc(reset:rejecter:)
    public func reset(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        player.stop()
        player.nowPlayingInfoController.clear()
        resolve(NSNull())
        DispatchQueue.main.async {
            UIApplication.shared.endReceivingRemoteControlEvents();
        }
    }

    @objc(play:rejecter:)
    public func play(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        try? AVAudioSession.sharedInstance().setActive(true)
        player.play()
        resolve(NSNull())
    }

    @objc(pause:rejecter:)
    public func pause(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        player.pause()
        resolve(NSNull())
    }

    // NOTE: this method is really just an alias for pause. It should NOT call `player.stop` as
    // that will reset the player, which is not the API intent.
    @objc(stop:rejecter:)
    public func stop(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        self.pause(resolve: resolve, reject: reject)
    }

    @objc(seekTo:resolver:rejecter:)
    public func seek(to time: Double, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        player.seek(to: time)
        resolve(NSNull())
    }

    @objc(setRepeatMode:resolver:rejecter:)
    public func setRepeatMode(repeatMode: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        player.repeatMode = SwiftAudioEx.RepeatMode(rawValue: repeatMode.intValue) ?? .off
        resolve(NSNull())
    }

    @objc(getRepeatMode:rejecter:)
    public func getRepeatMode(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        resolve(player.repeatMode.rawValue)
    }

    @objc(setVolume:resolver:rejecter:)
    public func setVolume(level: Float, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        player.volume = level
        resolve(NSNull())
    }

    @objc(getVolume:rejecter:)
    public func getVolume(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        resolve(player.volume)
    }

    @objc(setRate:resolver:rejecter:)
    public func setRate(rate: Float, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        player.rate = rate
        resolve(NSNull())
    }

    @objc(getRate:rejecter:)
    public func getRate(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        resolve(player.rate)
    }

    @objc(getTrack:resolver:rejecter:)
    public func getTrack(index: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        if (index.intValue >= 0 && index.intValue < player.items.count) {
            let track = player.items[index.intValue]
            resolve((track as? Track)?.toObject())
        } else {
            resolve(NSNull())
        }
    }

    @objc(getQueue:rejecter:)
    public func getQueue(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        let serializedQueue = player.items.map { ($0 as! Track).toObject() }
        resolve(serializedQueue)
    }

    @objc(getCurrentTrack:rejecter:)
    public func getCurrentTrack(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        let index = player.currentIndex
        if index < 0 || index >= player.items.count {
            resolve(NSNull())
        } else {
            resolve(index)
        }
    }

    @objc(getDuration:rejecter:)
    public func getDuration(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        resolve(player.duration)
    }

    @objc(getBufferedPosition:rejecter:)
    public func getBufferedPosition(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        resolve(player.bufferedPosition)
    }

    @objc(getPosition:rejecter:)
    public func getPosition(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        resolve(player.currentTime)
    }

    @objc(getState:rejecter:)
    public func getState(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        resolve(State.fromPlayerState(state: player.playerState).rawValue)
    }

    @objc(updateMetadataForTrack:metadata:resolver:rejecter:)
    public func updateMetadata(for trackIndex: NSNumber, metadata: [String: Any], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        if (trackIndex.intValue < 0 || trackIndex.intValue >= player.items.count) {
            reject("index_out_of_bounds", "The track index is out of bounds", nil)
            return
        }

        let track = player.items[trackIndex.intValue] as! Track
        track.updateMetadata(dictionary: metadata)

        if (player.currentIndex == trackIndex.intValue) {
            Metadata.update(for: player, with: metadata)
        }

        resolve(NSNull())
    }

    @objc(clearNowPlayingMetadata:rejecter:)
    public func clearNowPlayingMetadata(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        player.nowPlayingInfoController.clear()
        resolve(NSNull())
    }

    @objc(updateNowPlayingMetadata:resolver:rejecter:)
    public func updateNowPlayingMetadata(metadata: [String: Any], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !hasInitialized {
            reject("player_not_initialized", "The player is not initialized. Call setupPlayer first.", nil)
            return
        }

        Metadata.update(for: player, with: metadata)
        resolve(NSNull())
    }

    // MARK: - QueuedAudioPlayer Event Handlers

    func handleAudioPlayerStateChange(state: AVPlayerWrapperState) {
        sendEvent(withName: "playback-state", body: ["state": State.fromPlayerState(state: state).rawValue])
    }

    func handleAudioPlayerMetadataReceived(metadata: [AVTimedMetadataGroup]) {
        // SwiftAudioEx was updated to return the array of timed metadata
        // Until we have support for that in RNTP, we take the first item to keep existing behaviour.
        let metadata = metadata.first?.items ?? []

        func getMetadataItem(forIdentifier: AVMetadataIdentifier) -> String {
            return AVMetadataItem.metadataItems(from: metadata, filteredByIdentifier: forIdentifier).first?.stringValue ?? ""
        }

        var source: String {
            switch metadata.first?.keySpace {
            case AVMetadataKeySpace.id3:
                return "id3"
            case AVMetadataKeySpace.icy:
                return "icy"
            case AVMetadataKeySpace.quickTimeMetadata:
                return "quicktime"
            case AVMetadataKeySpace.common:
                return "unknown"
            default: return "unknown"
            }
        }

        let album = getMetadataItem(forIdentifier: .commonIdentifierAlbumName)
        var artist = getMetadataItem(forIdentifier: .commonIdentifierArtist)
        var title = getMetadataItem(forIdentifier: .commonIdentifierTitle)
        var date = getMetadataItem(forIdentifier: .commonIdentifierCreationDate)
        var url = "";
        var genre = "";
        if (source == "icy") {
            url = getMetadataItem(forIdentifier: .icyMetadataStreamURL)
        } else if (source == "id3") {
            if (date.isEmpty) {
                date = getMetadataItem(forIdentifier: .id3MetadataDate)
            }
            genre = getMetadataItem(forIdentifier: .id3MetadataContentType)
            url = getMetadataItem(forIdentifier: .id3MetadataOfficialAudioSourceWebpage)
            if (url.isEmpty) {
                url = getMetadataItem(forIdentifier: .id3MetadataOfficialAudioFileWebpage)
            }
            if (url.isEmpty) {
                url = getMetadataItem(forIdentifier: .id3MetadataOfficialArtistWebpage)
            }
        } else if (source == "quicktime") {
            genre = getMetadataItem(forIdentifier: .quickTimeMetadataGenre)
        }

        // Detect ICY metadata and split title into artist & title:
        // - source should be either "unknown" (pre iOS 14) or "icy" (iOS 14 and above)
        // - we have a title, but no artist
        if ((source == "unknown" || source == "icy") && !title.isEmpty && artist.isEmpty) {
            if let index = title.range(of: " - ")?.lowerBound {
                artist = String(title.prefix(upTo: index));
                title = String(title.suffix(from: title.index(index, offsetBy: 3)));
            }
        }
        var data : [String : String?] = [
            "title": title.isEmpty ? nil : title,
            "url": url.isEmpty ? nil : url,
            "artist": artist.isEmpty ? nil : artist,
            "album": album.isEmpty ? nil : album,
            "date": date.isEmpty ? nil : date,
            "genre": genre.isEmpty ? nil : genre
        ]
        if (data.values.contains { $0 != nil }) {
            data["source"] = source
            sendEvent(withName: "playback-metadata-received", body: data)
        }
    }

    func handleAudioPlayerFailed(error: Error?) {
        sendEvent(withName: "playback-error", body: ["error": error?.localizedDescription])
    }

    func handleAudioPlayerPlaybackEnded(reason: PlaybackEndedReason) {
        // fire an event for the queue ending
        if player.nextItems.count == 0 && reason == PlaybackEndedReason.playedUntilEnd {
            sendEvent(withName: "playback-queue-ended", body: [
                "track": player.currentIndex,
                "position": player.currentTime,
            ])
        }

        // fire an event for the same track starting again
        if player.items.count != 0 && player.repeatMode == .track {
            handleAudioPlayerQueueIndexChange(previousIndex: player.currentIndex, nextIndex: player.currentIndex)
        }
    }

    func handleAudioPlayerQueueIndexChange(previousIndex: Int?, nextIndex: Int?) {
        var dictionary: [String: Any] = [ "position": player.currentTime ]

        if let previousIndex = previousIndex { dictionary["track"] = previousIndex }
        if let nextIndex = nextIndex { dictionary["nextTrack"] = nextIndex }

        // Load isLiveStream option for track
        var isTrackLiveStream = false
        if let nextIndex = nextIndex {
            let track = player.items[nextIndex]
            isTrackLiveStream = (track as? Track)?.isLiveStream ?? false
        }

        if player.automaticallyUpdateNowPlayingInfo {
            player.nowPlayingInfoController.set(keyValue: NowPlayingInfoProperty.isLiveStream(isTrackLiveStream))
        }

        sendEvent(withName: "playback-track-changed", body: dictionary)
    }

    func handleAudioPlayerSecondElapse(seconds: Double) {
        // because you cannot prevent the `event.secondElapse` from firing
        // do not emit an event if `progressUpdateEventInterval` is nil
        // additionally, there are certain instances in which this event is emitted
        // _after_ a manipulation to the queu causing no currentItem to exist (see reset)
        // in which case we shouldn't emit anything or we'll get an exception.
        if shouldEmitUpdateEventInterval == false || player.currentItem == nil { return }

        sendEvent(
            withName: "playback-progress-updated",
            body: [
                "position": player.currentTime,
                "duration": player.duration,
                "buffered": player.bufferedPosition,
                "track": player.currentIndex,
            ]
        )
    }
}
