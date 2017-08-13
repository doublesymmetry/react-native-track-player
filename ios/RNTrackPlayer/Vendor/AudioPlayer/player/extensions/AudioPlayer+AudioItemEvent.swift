//
//  AudioPlayer+AudioItemEvent.swift
//  AudioPlayer
//
//  Created by Kevin DELANNOY on 03/04/16.
//  Copyright © 2016 Kevin Delannoy. All rights reserved.
//

extension AudioPlayer {
    /// Handles audio item events.
    ///
    /// - Parameters:
    ///   - producer: The event producer that generated the audio item event.
    ///   - event: The audio item event.
    func handleAudioItemEvent(from producer: EventProducer, with event: AudioItemEventProducer.AudioItemEvent) {
        updateNowPlayingInfoCenter()
    }
}
