//
//  AudioController.swift
//  SwiftAudio_Example
//
//  Created by Jørgen Henrichsen on 25/03/2018.
//  Copyright © 2018 CocoaPods. All rights reserved.
//

import Foundation
import SwiftAudio


class AudioController {
    
    static let shared = AudioController()
    let player: QueuedAudioPlayer
    let audioSessionController = AudioSessionController.shared
    
    let sources: [AudioItem] = [
        DefaultAudioItem(audioUrl: "https://p.scdn.co/mp3-preview/67b51d90ffddd6bb3f095059997021b589845f81?cid=d8a5ed958d274c2e8ee717e6a4b0971d", artist: "Bon Iver", title: "33 \"GOD\"", albumTitle: "22, A Million", sourceType: .stream, artwork: #imageLiteral(resourceName: "22AMI")),
        DefaultAudioItem(audioUrl: "https://p.scdn.co/mp3-preview/081447adc23dad4f79ba4f1082615d1c56edf5e1?cid=d8a5ed958d274c2e8ee717e6a4b0971d", artist: "Bon Iver", title: "8 (circle)", albumTitle: "22, A Million", sourceType: .stream, artwork: #imageLiteral(resourceName: "22AMI")),
        DefaultAudioItem(audioUrl: "https://p.scdn.co/mp3-preview/6f9999d909b017eabef97234dd7a206355720d9d?cid=d8a5ed958d274c2e8ee717e6a4b0971d", artist: "Bon Iver", title: "715 - CRΣΣKS", albumTitle: "22, A Million", sourceType: .stream, artwork: #imageLiteral(resourceName: "22AMI")),
        DefaultAudioItem(audioUrl: "https://p.scdn.co/mp3-preview/bf9bdd403c67fdbe06a582e7b292487c8cfd1f7e?cid=d8a5ed958d274c2e8ee717e6a4b0971d", artist: "Bon Iver", title: "____45_____", albumTitle: "22, A Million", sourceType: .stream, artwork: #imageLiteral(resourceName: "22AMI"))
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
        try? player.add(items: sources, playWhenReady: false)
    }
    
}
