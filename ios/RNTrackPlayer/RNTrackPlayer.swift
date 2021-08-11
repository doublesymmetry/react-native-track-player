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
public class RNTrackPlayer: RCTEventEmitter {

    // MARK: - Attributes

    private var hasInitialized = false
    private let player = QueuedAudioPlayer()

    // MARK: - Lifecycle Methods

    public override init() {
        super.init()

        player.event.playbackEnd.addListener(self, handleAudioPlayerPlaybackEnded)
        player.event.receiveMetadata.addListener(self, handleAudioPlayerMetadataReceived)
        player.event.stateChange.addListener(self, handleAudioPlayerStateChange)
        player.event.fail.addListener(self, handleAudioPlayerFailed)
        player.event.queueIndex.addListener(self, handleAudioPlayerQueueIndexChange)
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
            "STATE_NONE": AVPlayerWrapperState.idle.rawValue,
            "STATE_READY": AVPlayerWrapperState.ready.rawValue,
            "STATE_PLAYING": AVPlayerWrapperState.playing.rawValue,
            "STATE_PAUSED": AVPlayerWrapperState.paused.rawValue,
            "STATE_STOPPED": AVPlayerWrapperState.idle.rawValue,
            "STATE_BUFFERING": AVPlayerWrapperState.loading.rawValue,

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

    func setupInterruptionHandling() {
        let notificationCenter = NotificationCenter.default
        notificationCenter.removeObserver(self)
        notificationCenter.addObserver(self,
                                       selector: #selector(handleInterruption),
                                       name: AVAudioSession.interruptionNotification,
                                       object: nil)
    }

    @objc func handleInterruption(notification: Notification) {
        guard let userInfo = notification.userInfo,
              let typeValue = userInfo[AVAudioSessionInterruptionTypeKey] as? UInt,
              let type = AVAudioSession.InterruptionType(rawValue: typeValue) else {
            return
        }
        if type == .began {
            // Interruption began, take appropriate actions (save state, update user interface)
            self.sendEvent(withName: "remote-duck", body: [
                "paused": true
            ])
        }
        else if type == .ended {
            guard let optionsValue =
                    userInfo[AVAudioSessionInterruptionOptionKey] as? UInt else {
                return
            }
            let options = AVAudioSession.InterruptionOptions(rawValue: optionsValue)
            if options.contains(.shouldResume) {
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
            resolve(NSNull())
            return
        }

        setupInterruptionHandling();

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
        var sessionCategoryOptions: AVAudioSession.CategoryOptions = []
        var sessionCategoryMode: AVAudioSession.Mode = .default

        if
            let sessionCategoryStr = config["iosCategory"] as? String,
            let mappedCategory = SessionCategory(rawValue: sessionCategoryStr) {
            sessionCategory = mappedCategory.mapConfigToAVAudioSessionCategory()
        }

        let sessionCategoryOptsStr = config["iosCategoryOptions"] as? [String]
        let mappedCategoryOpts = sessionCategoryOptsStr?.compactMap { SessionCategoryOptions(rawValue: $0)?.mapConfigToAVAudioSessionCategoryOptions() } ?? []
        sessionCategoryOptions = AVAudioSession.CategoryOptions(mappedCategoryOpts)

        if
            let sessionCategoryModeStr = config["iosCategoryMode"] as? String,
            let mappedCategoryMode = SessionCategoryMode(rawValue: sessionCategoryModeStr) {
            sessionCategoryMode = mappedCategoryMode.mapConfigToAVAudioSessionCategoryMode()
        }

        // Progressively opt into AVAudioSession policies for background audio
        // and AirPlay 2.
        if #available(iOS 13.0, *) {
            try? AVAudioSession.sharedInstance().setCategory(sessionCategory, mode: sessionCategoryMode, policy: .longFormAudio, options: sessionCategoryOptions)
        } else if #available(iOS 11.0, *) {
            try? AVAudioSession.sharedInstance().setCategory(sessionCategory, mode: sessionCategoryMode, policy: .longForm, options: sessionCategoryOptions)
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

    @objc(destroy)
    public func destroy() {
        print("Destroying player")
        self.player.stop()
        self.player.nowPlayingInfoController.clear()
        try? AVAudioSession.sharedInstance().setActive(false)
    }

    @objc(updateOptions:resolver:rejecter:)
    public func update(options: [String: Any], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {

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

        resolve(NSNull())
    }

    @objc(add:before:resolver:rejecter:)
    public func add(trackDicts: [[String: Any]], before trackIndex: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
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

        if (trackIndex.intValue > player.items.count) {
            reject("index_out_of_bounds", "The track index is out of bounds", nil)
        } else if trackIndex.intValue == -1 { // -1 means no index was passed and therefore should be inserted at the end.
            try? player.add(items: tracks, playWhenReady: false)
        } else {
            try? player.add(items: tracks, at: trackIndex.intValue)
        }

        resolve(NSNull())
    }

    @objc(remove:resolver:rejecter:)
    public func remove(tracks indexes: [Int], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Removing tracks:", indexes)

        for index in indexes {
            // we do not allow removal of the current item
            if index == player.currentIndex { continue }
            try? player.removeItem(at: index)
        }

        resolve(NSNull())
    }

    @objc(removeUpcomingTracks:rejecter:)
    public func removeUpcomingTracks(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Removing upcoming tracks")
        player.removeUpcomingItems()
        resolve(NSNull())
    }

    @objc(skip:resolver:rejecter:)
    public func skip(to trackIndex: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if (trackIndex.intValue < 0 || trackIndex.intValue >= player.items.count) {
            reject("index_out_of_bounds", "The track index is out of bounds", nil)
            return
        }

        print("Skipping to track:", trackIndex)
        try? player.jumpToItem(atIndex: trackIndex.intValue, playWhenReady: player.playerState == .playing)
        resolve(NSNull())
    }

    @objc(skipToNext:rejecter:)
    public func skipToNext(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Skipping to next track")
        do {
            try player.next()
            resolve(NSNull())
        } catch (_) {
            reject("queue_exhausted", "There is no tracks left to play", nil)
        }
    }

    @objc(skipToPrevious:rejecter:)
    public func skipToPrevious(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Skipping to next track")
        do {
            try player.previous()
            resolve(NSNull())
        } catch (_) {
            reject("no_previous_track", "There is no previous track", nil)
        }
    }

    @objc(reset:rejecter:)
    public func reset(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Resetting player.")
        player.stop()
        resolve(NSNull())
        DispatchQueue.main.async {
            UIApplication.shared.endReceivingRemoteControlEvents();
        }
    }

    @objc(play:rejecter:)
    public func play(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Starting/Resuming playback")
        try? AVAudioSession.sharedInstance().setActive(true)
        player.play()
        resolve(NSNull())
    }

    @objc(pause:rejecter:)
    public func pause(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Pausing playback")
        player.pause()
        resolve(NSNull())
    }

    @objc(stop:rejecter:)
    public func stop(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Stopping playback")
        player.stop()
        resolve(NSNull())
    }

    @objc(seekTo:resolver:rejecter:)
    public func seek(to time: Double, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Seeking to \(time) seconds")
        player.seek(to: time)
        resolve(NSNull())
    }

    @objc(setRepeatMode:resolver:rejecter:)
    public func setRepeatMode(repeatMode: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        player.repeatMode = SwiftAudioEx.RepeatMode(rawValue: repeatMode.intValue) ?? .off
        resolve(NSNull())
    }

    @objc(getRepeatMode:rejecter:)
    public func getRepeatMode(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Getting current repeatMode")
        resolve(player.repeatMode.rawValue)
    }

    @objc(setVolume:resolver:rejecter:)
    public func setVolume(level: Float, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Setting volume to \(level)")
        player.volume = level
        resolve(NSNull())
    }

    @objc(getVolume:rejecter:)
    public func getVolume(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Getting current volume")
        resolve(player.volume)
    }

    @objc(setRate:resolver:rejecter:)
    public func setRate(rate: Float, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Setting rate to \(rate)")
        player.rate = rate
        resolve(NSNull())
    }

    @objc(getRate:rejecter:)
    public func getRate(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Getting current rate")
        resolve(player.rate)
    }

    @objc(getTrack:resolver:rejecter:)
    public func getTrack(index: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if (index.intValue >= 0 && index.intValue < player.items.count) {
            let track = player.items[index.intValue]
            resolve((track as? Track)?.toObject())
        } else {
            resolve(nil)
        }
    }

    @objc(getQueue:rejecter:)
    public func getQueue(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        let serializedQueue = player.items.map { ($0 as! Track).toObject() }
        resolve(serializedQueue)
    }

    @objc(getCurrentTrack:rejecter:)
    public func getCurrentTrack(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        let index = player.currentIndex
        let adjustedIndex = index < 0 || index >= player.items.count ? nil : index;

        resolve(adjustedIndex)
    }

    @objc(getDuration:rejecter:)
    public func getDuration(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        resolve(player.duration)
    }

    @objc(getBufferedPosition:rejecter:)
    public func getBufferedPosition(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        resolve(player.bufferedPosition)
    }

    @objc(getPosition:rejecter:)
    public func getPosition(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        resolve(player.currentTime)
    }

    @objc(getState:rejecter:)
    public func getState(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        resolve(player.playerState.rawValue)
    }

    @objc(updateMetadataForTrack:metadata:resolver:rejecter:)
    public func updateMetadata(for trackIndex: NSNumber, metadata: [String: Any], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
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
        player.nowPlayingInfoController.clear()
    }

    @objc(updateNowPlayingMetadata:resolver:rejecter:)
    public func updateNowPlayingMetadata(metadata: [String: Any], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        Metadata.update(for: player, with: metadata)
    }

    // MARK: - QueuedAudioPlayer Event Handlers

    func handleAudioPlayerStateChange(state: AVPlayerWrapperState) {
        sendEvent(withName: "playback-state", body: ["state": state.rawValue])
    }

    func handleAudioPlayerMetadataReceived(metadata: [AVMetadataItem]) {
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
        if player.nextItems.count == 0 {
            sendEvent(withName: "playback-queue-ended", body: [
                "track": player.currentIndex,
                "position": player.currentTime,
            ])
        }

        // fire an event for the same track starting again
        switch player.repeatMode {
        case .track:
            handleAudioPlayerQueueIndexChange(previousIndex: player.currentIndex, nextIndex: player.currentIndex)
        default: break
        }
    }

    func handleAudioPlayerQueueIndexChange(previousIndex: Int?, nextIndex: Int?) {
        var dictionary: [String: Any] = [ "position": player.currentTime ]

        if let previousIndex = previousIndex { dictionary["track"] = previousIndex }
        if let nextIndex = nextIndex { dictionary["nextTrack"] = nextIndex }

        sendEvent(withName: "playback-track-changed", body: dictionary)
    }
}
