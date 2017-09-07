//
//  MediaWrapper.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 11.08.17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

import Foundation
import MediaPlayer

protocol MediaWrapperDelegate: class {
    func playerUpdatedState()
    func playerSwitchedTracks(trackId: String?)
    func playerTrackEnded(trackId: String?, time: TimeInterval?)
    func playerExhaustedQueue()
    func playbackFailed(error: Error)
    func playbackUpdatedProgress(to time: TimeInterval)    
}

class MediaWrapper: AudioPlayerDelegate {
    private var queue: [Track]
    private var currentIndex: Int
    private let player: AudioPlayer
    private var trackImageTask: URLSessionDataTask?
    
    weak var delegate: MediaWrapperDelegate?
    
    var volume: Float {
        get {
            return player.volume
        }
        set {
            player.volume = newValue
        }
    }
    
    var currentTrack: Track? {
        return queue.indices.contains(0) ? queue[currentIndex] : nil
    }
    
    var bufferedPosition: Double {
        return player.currentItemLoadedRange?.latest ?? 0
    }
    
    var currentTrackDuration: Double {
        return player.currentItemDuration ?? 0
    }
    
    var currentTrackProgression: Double {
        return player.currentItemProgression ?? 0
    }
    
    var state: String {
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
    
    
    // MARK: - Init/Deinit
    
    init() {
        self.queue = []
        self.currentIndex = 0
        self.player = AudioPlayer()
        
        self.player.delegate = self
        self.player.bufferingStrategy = .playWhenBufferNotEmpty
        
        DispatchQueue.main.async {
            UIApplication.shared.beginReceivingRemoteControlEvents()
        }
    }
    
    
    // MARK: - Public API
    
    func queueContainsTrack(trackId: String) -> Bool {
        return queue.contains(where: { $0.id == trackId })
    }
    
    func addTracks(_ tracks: [Track]) {
        queue.append(contentsOf: tracks)
    }
    
    func addTracks(_ tracks: [Track], before trackId: String?) {
        if let trackIndex = queue.index(where: { $0.id == trackId }) {
            queue.insert(contentsOf: tracks, at: trackIndex)
        } else {
            addTracks(tracks)
        }
    }
    
    func removeTracks(ids: [String]) {
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
    
    func skipToTrack(id: String) {
        if let trackIndex = queue.index(where: { $0.id == id }) {
            currentIndex = trackIndex
            play()
        }
    }
    
    func playNext() -> Bool {
        if queue.indices.contains(currentIndex + 1) {
            currentIndex = currentIndex + 1
            play()
            return true
        }
        
        stop()
        return false
    }
    
    func playPrevious() -> Bool {
        if queue.indices.contains(currentIndex - 1) {
            currentIndex = currentIndex - 1
            play()
            return true
        }
        
        stop()
        return false
    }
    
    func play() {
        // resume playback if it was paused
        if player.state == .paused {
            player.resume()
            return
        }
        
        let track = queue[currentIndex]
        player.play(track: track)
        
        // fetch artwork and cancel any previous requests
        trackImageTask?.cancel()
        if let artworkURL = track.artworkURL?.value {
            trackImageTask = URLSession.shared.dataTask(with: artworkURL, completionHandler: { (data, _, error) in
                if let data = data, let artwork = UIImage(data: data), error == nil {
                    track.artwork = MPMediaItemArtwork(image: artwork)
                }
            })
        }

        trackImageTask?.resume()
    }
    
    func pause() {
        player.pause()
    }
    
    func stop() {
        player.stop()
    }
    
    func seek(to time: Double) {
        self.player.seek(to: time)
    }
    
    func reset() {
        currentIndex = 0
        queue.removeAll()
        stop()
    }
    
    
    // MARK: - AudioPlayerDelegate
    
    func audioPlayer(_ audioPlayer: AudioPlayer, willStartPlaying item: Track) {
        delegate?.playerSwitchedTracks(trackId: currentTrack?.id)
    }
    
    func audioPlayer(_ audioPlayer: AudioPlayer, didFinishPlaying item: Track) {
        guard fabs(currentTrackProgression.distance(to: currentTrackDuration)) <= 1e-1 else { return }
        
        delegate?.playerTrackEnded(trackId: currentTrack?.id, time: currentTrackProgression)
        
        if (!playNext()) {
            delegate?.playerExhaustedQueue()
        }
    }
    
    func audioPlayer(_ audioPlayer: AudioPlayer, didChangeStateFrom from: AudioPlayerState, to state: AudioPlayerState) {
        switch state {
        case .failed(let error):
            delegate?.playbackFailed(error: error)
        default:
            delegate?.playerUpdatedState()
        }
    }
    
    func audioPlayer(_ audioPlayer: AudioPlayer, didUpdateProgressionTo time: TimeInterval, percentageRead: Float) {
        delegate?.playbackUpdatedProgress(to: time)
    }    
}
