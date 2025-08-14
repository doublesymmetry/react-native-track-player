//
//  Metadata.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 23.06.19.
//  Copyright © 2019 David Chavez. All rights reserved.
//

import Foundation
import MediaPlayer
import SwiftAudioEx

struct Metadata {
    private static var currentImageTask: URLSessionDataTask?

    // Patches existing active track metadata with given new metadata updates.
    static func update(for player: AudioPlayer, with metadata: [String: Any]) {
        currentImageTask?.cancel()
        var ret: [NowPlayingInfoKeyValue] = []
        
        // Get current track for existing metadata
        guard let currentIndex = (player as? QueuedAudioPlayer)?.currentIndex,
              currentIndex >= 0,
              let items = (player as? QueuedAudioPlayer)?.items,
              currentIndex < items.count,
              let currentTrack = items[currentIndex] as? Track else {
            return
        }
        
        let title = metadata["title"] as? String ?? currentTrack.title
        if let title = title {
            ret.append(MediaItemProperty.title(title))
        }
        
        let artist = metadata["artist"] as? String ?? currentTrack.artist
        if let artist = artist {
            ret.append(MediaItemProperty.artist(artist))
        }
        
        let album = metadata["album"] as? String ?? currentTrack.album
        if let album = album {
            ret.append(MediaItemProperty.albumTitle(album))
        }
        
        let duration = metadata["duration"] as? Double ?? currentTrack.duration
        if let duration = duration {
            ret.append(MediaItemProperty.duration(duration))
        }
        
        if let elapsedTime = metadata["elapsedTime"] as? Double ?? (player.currentTime != 0 ? player.currentTime : nil) {
            ret.append(NowPlayingInfoProperty.elapsedPlaybackTime(elapsedTime))
        }

        let isLiveStream = metadata["isLiveStream"] as? Bool ?? currentTrack.isLiveStream
        if let isLiveStream = isLiveStream {
            ret.append(NowPlayingInfoProperty.isLiveStream(isLiveStream))
        }
        
        player.nowPlayingInfoController.set(keyValues: ret)
        
        // Handle artwork updates:
        // - If artwork is undefined/null, keep existing artwork.
        // - If artwork is an empty string, explicitly, remove artwork.
        // - If artwork is defined and valid, update artwork.
        if let artworkValue = metadata["artwork"] as? String {
            if artworkValue.isEmpty {
                player.nowPlayingInfoController.set(keyValue: MediaItemProperty.artwork(nil))
            } else if let artworkURL = MediaURL(object: artworkValue) {
                currentImageTask = URLSession.shared.dataTask(with: artworkURL.value, completionHandler: { [weak player] (data, _, error) in
                    if let data = data, let image = UIImage(data: data), error == nil {
                        let artwork = MPMediaItemArtwork(boundsSize: image.size, requestHandler: { (size) -> UIImage in
                            return image
                        })
                        player?.nowPlayingInfoController.set(keyValue: MediaItemProperty.artwork(artwork))
                    }
                })
                
                currentImageTask?.resume()
            }
        }
    }
}
