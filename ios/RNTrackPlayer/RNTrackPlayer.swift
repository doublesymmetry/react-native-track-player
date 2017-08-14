//
//  RNTrackPlayer.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 13.08.17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

import Foundation
import AVFoundation

@objc(RNTrackPlayer)
class RNTrackPlayer: NSObject {
    private let mediaWrapper = MediaWrapper()
    
    // MARK: - Required Methods
    
    @objc(constantsToExport)
    func constantsToExport() -> [String: Any] {
        return [
            "STATE_NONE": AudioPlayerState.stopped,
            "STATE_PLAYING": AudioPlayerState.playing,
            "STATE_PAUSED": AudioPlayerState.paused,
            "STATE_STOPPED": AudioPlayerState.stopped,
            "STATE_BUFFERING": AudioPlayerState.buffering
        ]
    }
    
    
    // MARK: - Bridged Methods
    
    @objc(setupPlayer:resolver:rejecter:)
    func setupPlayer(config: [String: Any], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        do {
            try AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            reject("setup_audio_session_failed", "Failed to setup audio session", error)
        }
        
        resolve(NSNull())
    }
    
    @objc(destroy)
    func destroy() {
        print("Destroying player")
    }
    
    @objc(updateOptions:)
    func update(options: [String: Any]) {
        // TODO: - Implement
    }
    
    @objc(add:before:resolver:rejecter:)
    func add(trackDicts: [[String: Any]], before trackId: String?, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if let trackId = trackId, !mediaWrapper.queueContainsTrack(trackId: trackId) {
            reject("track_not_in_queue", "Given track ID was not found in queue", nil)
            return
        }
        
        var tracks = [Track]()
        for trackDict in trackDicts {
            guard let track = Track(dictionary: trackDict) else {
                reject("invalid_track_object", "Track is missing a required key", nil)
                return
            }
            
            tracks.append(track)
        }
        
        print("Adding tracks:", tracks)
        mediaWrapper.addTracks(tracks, before: trackId)
        resolve(NSNull())
    }
    
    @objc(remove:resolver:rejecter:)
    func remove(tracks ids: [String], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Removing tracks:", ids)
        mediaWrapper.removeTracks(ids: ids)
        
        resolve(NSNull())
    }
    
    @objc(skip:resolver:rejecter:)
    func skip(to trackId: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        if !mediaWrapper.queueContainsTrack(trackId: trackId) {
            reject("track_not_in_queue", "Given track ID was not found in queue", nil)
            return
        }
        
        print("Skipping to track:", trackId)
        mediaWrapper.skipToTrack(id: trackId)
        resolve(NSNull())
    }
    
    @objc(skipToNext:rejecter:)
    func skipToNext(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Skipping to next track")
        if (mediaWrapper.playNext()) {
            resolve(NSNull())
        } else {
            reject("queue_exhausted", "There is no tracks left to play", nil)
        }
    }
    
    @objc(skipToPrevious:rejecter:)
    func skipToPrevious(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        print("Skipping to next track")
        if (mediaWrapper.playPrevious()) {
            resolve(NSNull())
        } else {
            reject("no_previous_track", "There is no previous track", nil)
        }
    }
    
    @objc(reset)
    func reset() {
        print("Resetting player.")
        mediaWrapper.reset()
    }
    
    @objc(play)
    func play() {
        print("Starting/Resuming playback")
        mediaWrapper.play()
    }
    
    @objc(pause)
    func pause() {
        print("Pausing playback")
        mediaWrapper.pause()
    }
    
    @objc(stop)
    func stop() {
        print("Stopping playback")
        mediaWrapper.stop()
    }
    
    @objc(seekTo:)
    func seek(to time: Double) {
        print("Seeking to \(time) seconds")
        mediaWrapper.seek(to: time)
    }
    
    @objc(setVolume:)
    func setVolume(level: Float) {
        print("Setting volume to \(level)")
        mediaWrapper.volume = level
    }
    
    @objc(getCurrentTrack:rejecter:)
    func getCurrentTrack(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        resolve(mediaWrapper.currentTrack.id)
    }
    
    @objc(getDuration:rejecter:)
    func getDuration(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        resolve(mediaWrapper.currentTrackDuration)
    }
    
    @objc(getBufferedPosition:rejecter:)
    func getBufferedPosition(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        // TODO: Implement
        resolve(0)
    }
    
    @objc(getPosition:rejecter:)
    func getPosition(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        resolve(mediaWrapper.currentTrackProgression)
    }
    
    @objc(getState:rejecter:)
    func getState(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        resolve(mediaWrapper.state)
    }
}
