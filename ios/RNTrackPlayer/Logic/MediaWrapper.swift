//
//  MediaWrapper.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 11.08.17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

import Foundation
import MediaPlayer

@objc(MediaWrapper)
class MediaWrapper: NSObject, AudioPlayerDelegate {
    private var queue: [Track]
    private var currentIndex: Int
    private let player: AudioPlayer
    private var trackImageTask: URLSessionDataTask?
    
    
    // MARK: - Init/Deinit
    
    override init() {
        self.queue = []
        self.currentIndex = 0
        self.player = AudioPlayer()
        super.init()
        
        self.player.delegate = self
        UIApplication.shared.beginReceivingRemoteControlEvents()
    }
    
    
    // MARK: - Public API
    
    @objc func queueContainsTrack(trackId: String) -> Bool {
        return queue.contains(where: { $0.id == trackId })
    }
    
    @objc func addTracks(_ tracks: [Track], before trackId: String) {
        if let trackIndex = queue.index(where: { $0.id == trackId }) {
            queue.insert(contentsOf: tracks, at: trackIndex)
        } else {
            queue.append(contentsOf: tracks)
        }
    }
    
    @objc func removeTracks(ids: [String]) {
        var removedCurrentTrack = false
        
        for (index, track) in queue.enumerated() {
            if ids.contains(track.id) {
                if (index == currentIndex) { removedCurrentTrack = true }
                else if (index < currentIndex) { currentIndex = currentIndex - 1 }
            }
        }
        
        if (removedCurrentTrack) {
            if (currentIndex > queue.count - 1) {
                currentIndex = queue.count
                stop()
            } else {
                play()
            }
        }
        
        queue = queue.filter { ids.contains($0.id) }
    }
    
    @objc func skipToTrack(id: String) {
        if let trackIndex = queue.index(where: { $0.id == id }) {
            currentIndex = trackIndex
            play()
        }
    }
    
    @objc func playNext() -> Bool {
        if queue.indices.contains(currentIndex + 1) {
            currentIndex = currentIndex + 1
            play()
            return true
        }
        
        stop()
        return false
    }
    
    @objc func playPrevious() -> Bool {
        if queue.indices.contains(currentIndex - 1) {
            currentIndex = currentIndex - 1
            play()
            return true
        }
        
        stop()
        return false
    }
    
    @objc func reset() {
        currentIndex = 0
        queue.removeAll()
        stop()
    }
    
    @objc func play() {
        // resume playback if it was paused
        if player.state == .paused {
            player.resume()
            return
        }
        
        let track = queue[currentIndex]
        
        // go to next track if item cannot be played
        guard let audioItem = AudioItem(mediumQualitySoundURL: track.url.value) else {
            _ = playNext()
            return
        }
        
        audioItem.title = track.title
        audioItem.artist = track.artist
        audioItem.album = track.album
        player.play(item: audioItem)
        
        // fetch artwork and cancel any previous requests
        trackImageTask?.cancel()
        if let artworkURL = track.artwork?.value {
            trackImageTask = URLSession.shared.dataTask(with: artworkURL, completionHandler: { (data, _, error) in
                if let data = data, let artwork = UIImage(data: data), error == nil {
                    audioItem.artwork = MPMediaItemArtwork(image: artwork)
                }
            })
        }
        
        trackImageTask?.resume()
    }
    
    @objc func pause() {
        player.pause()
    }
    
    @objc func stop() {
        player.stop()
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
    }
    
    @objc func seek(to time: Double) {
        self.player.seek(to: time)
    }
    
    @objc func setVolume(_ level: Float) {
        player.volume = level
    }
    
    @objc func currentTrack() -> Track {
        return queue[currentIndex];
    }
    
    @objc func duration() -> Double {
        return player.currentItemDuration ?? 0
    }
    
    @objc func position() -> Double {
        return player.currentItemProgression ?? 0
    }
    
    @objc func state() -> String {
        switch player.state {
        case .playing:
            return "STATE_PLAYING"
        case .paused:
            return "STATE_PAUSED"
        case .stopped:
            return "STATE_STOPPED"
        case .buffering:
            return "STATE_BUFFERING"
        default:
            return "STATE_NONE"
        }
    }
    
    
    // MARK: - AudioPlayerDelegate
    
    func audioPlayer(_ audioPlayer: AudioPlayer, didChangeStateFrom from: AudioPlayerState, to state: AudioPlayerState) {
        if from == .playing && state == .stopped { _ = playNext() }
    }
}
