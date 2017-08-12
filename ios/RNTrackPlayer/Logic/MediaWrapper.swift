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
class MediaWrapper: NSObject {
    private var queue: [Track]
    private var currentIndex: Int
    private let player: STKAudioPlayer
    private var trackURLTask: URLSessionDataTask?
    private var trackImageTask: URLSessionDataTask?
    
    
    // MARK: - Init/Deinit
    
    override init() {
        self.queue = []
        self.currentIndex = 0
        self.player = STKAudioPlayer()
        
        UIApplication.shared.beginReceivingRemoteControlEvents()
    }
    
    deinit { player.dispose() }
    
    
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
        
        // cancel any previous tasks
        trackURLTask?.cancel()
        trackImageTask?.cancel()
        
        // setup now playing info center data and download album artwork
        var infoCenterMetadata: [String: Any] = [
            MPMediaItemPropertyTitle: track.title,
            MPMediaItemPropertyArtist: track.artist,
            MPMediaItemPropertyGenre: track.genre ?? "",
            MPMediaItemPropertyAlbumTitle: track.album ?? "",
            MPMediaItemPropertyReleaseDate: track.date ?? "",
            MPMediaItemPropertyMediaType: MPMediaType.music.rawValue,
            MPMediaItemPropertyPlaybackDuration: track.duration ?? 0.0,
            MPNowPlayingInfoPropertyElapsedPlaybackTime: player.progress,
            ]
        
        MPNowPlayingInfoCenter.default().nowPlayingInfo = infoCenterMetadata
        
        if let artworkURL = track.artwork?.value {
            trackImageTask = URLSession.shared.dataTask(with: artworkURL, completionHandler: { (data, _, error) in
                if let data = data, let artwork = UIImage(data: data), error == nil {
                    infoCenterMetadata[MPMediaItemPropertyArtwork] = MPMediaItemArtwork(image: artwork)
                    MPNowPlayingInfoCenter.default().nowPlayingInfo = infoCenterMetadata
                }
            })
            
            trackImageTask?.resume()
        }
        
        
        // fetch possible 302 redirection
        if (!track.url.isLocal) {
            var request = URLRequest(url: track.url.value, cachePolicy: .returnCacheDataElseLoad, timeoutInterval: 5)
            request.setValue("HEAD", forHTTPHeaderField: "HTTPMethod")
            
            trackURLTask = URLSession.shared.dataTask(with: request, completionHandler: { [unowned self] (_, response, error) in
                if let response = response, error == nil {
                    // TODO: Add way to remember we've already fetched the 302 redirect
                    track.url.value = response.url!
                    self.player.play(track.url.value)
                }
            })
            
            trackURLTask?.resume()
        } else {
            player.play(track.url.value)
        }
    }
    
    @objc func pause() {
        player.pause()
    }
    
    @objc func stop() {
        player.stop()
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
    }
    
    @objc func seek(to time: Double) {
        self.player.seek(toTime: time)
    }
    
    @objc func setVolume(_ level: Float) {
        player.volume = level
    }
    
    @objc func currentTrack() -> Track {
        return queue[currentIndex];
    }
    
    @objc func duration() -> Double {
        return player.duration
    }
    
    @objc func position() -> Double {
        return player.progress
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
}
