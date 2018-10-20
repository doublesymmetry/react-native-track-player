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
    func playerSwitchedTracks(trackId: String?, time: TimeInterval?, nextTrackId: String?)
    func playerExhaustedQueue(trackId: String?, time: TimeInterval?)
    func playbackFailed(error: Error)
}

class MediaWrapper: AudioPlayerDelegate {
    private(set) var queue: [Track]
    private var currentIndex: Int
    private let player: AudioPlayer
    private var trackImageTask: URLSessionDataTask?
    
    weak var delegate: MediaWrapperDelegate?
    
    enum PlaybackState: String {
        case playing, paused, stopped, buffering, none
    }
    
    var volume: Float {
        get {
            return player.getVolume()
        }
        set {
            player.volume = newValue
        }
    }
    var rate: Float {
        get {
            return player.getRate()
        }
        set {
            player.rate = newValue
        }
    }
    
    var currentTrack: Track? {
        return queue[safe: currentIndex]
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
    
    var mappedState: PlaybackState {
        switch player.state {
        case .playing:
            return .playing
        case .paused:
            return .paused
        case .stopped:
            return .stopped
        case .buffering:
            return .buffering
        default:
            return .none
        }
    }
    
    
    // MARK: - Init/Deinit
    
    init() {
        self.queue = []
        self.currentIndex = -1
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
            if (currentIndex >= trackIndex) { currentIndex = currentIndex + tracks.count }
        } else {
            addTracks(tracks)
        }
    }
    
    func removeTracks(ids: [String]) {
        var actionAfterRemovals = "none"
        for id in ids {
            if let trackIndex = queue.index(where: { $0.id == id }) {
                if trackIndex < currentIndex { currentIndex = currentIndex - 1 }
                else if id == queue.last?.id { actionAfterRemovals = "stop" }
                else if trackIndex == currentIndex { actionAfterRemovals = "play" }
                
                queue.remove(at: trackIndex)
            }
        }
        
        switch actionAfterRemovals {
            case "play": play()
            case "stop": stop()
            default: break;
        }
    }
    
    func removeUpcomingTracks() {
        queue = queue.filter { $0.0 <= currentIndex }
    }
    
    func skipToTrack(id: String) {
        if let trackIndex = queue.index(where: { $0.id == id }) {
            currentTrack?.skipped = true
            currentIndex = trackIndex
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
        guard queue.count > 0 else { return }
        if (currentIndex == -1) { currentIndex = 0 }
        
        // resume playback if it was paused and check currentIndex wasn't changed by a skip/previous
        if player.state == .paused && currentTrack?.id == queue[currentIndex].id {
            player.resume()
            return
        }
        
        let track = queue[currentIndex]
        player.play(track: track)
        
        setPitchAlgorithm(for: track)
        
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
    
    func setPitchAlgorithm(for track: Track) {
        if let pitchAlgorithm = track.pitchAlgorithm {
            switch pitchAlgorithm {
            case PitchAlgorithm.linear.rawValue:
                player.player?.currentItem?.audioTimePitchAlgorithm = AVAudioTimePitchAlgorithmVarispeed
            case PitchAlgorithm.music.rawValue:
                player.player?.currentItem?.audioTimePitchAlgorithm = AVAudioTimePitchAlgorithmSpectral
            case PitchAlgorithm.voice.rawValue:
                player.player?.currentItem?.audioTimePitchAlgorithm = AVAudioTimePitchAlgorithmTimeDomain
            default:
                player.player?.currentItem?.audioTimePitchAlgorithm = AVAudioTimePitchAlgorithmLowQualityZeroLatency
            }
        }
    }
    
    func pause() {
        player.pause()
    }
    
    func stop() {
        currentIndex = -1
        player.stop()
    }
    
    func seek(to time: Double) {
        self.player.seek(to: time)
    }
    
    func reset() {
        rate = 1
        queue.removeAll()
        stop()
    }
    
    // MARK: - AudioPlayerDelegate
    
    func audioPlayer(_ audioPlayer: AudioPlayer, willChangeTrackFrom from: Track?, at position: TimeInterval?, to track: Track?) {
        guard from?.id != track?.id else { return }
        delegate?.playerSwitchedTracks(trackId: from?.id, time: position, nextTrackId: track?.id)
    }
    
    func audioPlayer(_ audioPlayer: AudioPlayer, didFinishPlaying item: Track, at position: TimeInterval?) {
        if item.skipped { return }
        if (!playNext()) {
            delegate?.playerExhaustedQueue(trackId: item.id, time: position)
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
}
