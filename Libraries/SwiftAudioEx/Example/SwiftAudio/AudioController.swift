//
//  AudioController.swift
//  SwiftAudio_Example
//
//  Created by Jørgen Henrichsen on 25/03/2018.
//  Copyright © 2018 CocoaPods. All rights reserved.
//

import Foundation
import SwiftAudioEx


class AudioController {
    
    static let shared = AudioController()
    let player: QueuedAudioPlayer
    let audioSessionController = AudioSessionController.shared
    
    let sources: [AudioItem] = [
        DefaultAudioItem(audioUrl: "https://rntp.dev/example/Longing.mp3", artist: "David Chavez", title: "Longing", sourceType: .stream, artwork: #imageLiteral(resourceName: "22AMI")),
        DefaultAudioItem(audioUrl: "https://rntp.dev/example/Soul%20Searching.mp3", artist: "David Chavez", title: "Soul Searching (Demo)", sourceType: .stream, artwork: #imageLiteral(resourceName: "22AMI")),
        DefaultAudioItem(audioUrl: "https://rntp.dev/example/Lullaby%20(Demo).mp3", artist: "David Chavez", title: "Lullaby (Demo)", sourceType: .stream, artwork: #imageLiteral(resourceName: "22AMI")),
        DefaultAudioItem(audioUrl: "https://rntp.dev/example/Rhythm%20City%20(Demo).mp3", artist: "David Chavez", title: "Rhythm City (Demo)", sourceType: .stream, artwork: #imageLiteral(resourceName: "22AMI")),
        DefaultAudioItem(audioUrl: "https://rntp.dev/example/hls/whip/playlist.m3u8", title: "Whip", sourceType: .stream, artwork: #imageLiteral(resourceName: "22AMI")),
        DefaultAudioItem(audioUrl: "https://ais-sa5.cdnstream1.com/b75154_128mp3", artist: "New York, NY", title: "Smooth Jazz 24/7", sourceType: .stream, artwork: #imageLiteral(resourceName: "22AMI")),
        DefaultAudioItem(audioUrl: "https://traffic.libsyn.com/atpfm/atp545.mp3", title: "Chapters", sourceType: .stream, artwork: #imageLiteral(resourceName: "22AMI")),
    ]
    
    init() {
        let controller = RemoteCommandController()
        player = QueuedAudioPlayer(remoteCommandController: controller)
        player.remoteCommands = [
            .stop,
            .play,
            .pause,
            .togglePlayPause,
            .next,
            .previous,
            .changePlaybackPosition
        ]
        try? audioSessionController.set(category: .playback)
        player.repeatMode = .queue
        DispatchQueue.main.async {
            self.player.add(items: self.sources)
        }
    }
    
}
