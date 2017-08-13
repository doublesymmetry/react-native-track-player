//
//  AudioPlayer+RetryEvent.swift
//  AudioPlayer
//
//  Created by Kevin DELANNOY on 15/04/16.
//  Copyright © 2016 Kevin Delannoy. All rights reserved.
//

import Foundation

extension AudioPlayer {
    /// Handles retry events.
    ///
    /// - Parameters:
    ///   - producer: The event producer that generated the retry event.
    ///   - event: The retry event.
    func handleRetryEvent(from producer: EventProducer, with event: RetryEventProducer.RetryEvent) {
        switch event {
        case .retryAvailable:
            retryOrPlayNext()

        case .retryFailed:
            state = .failed(.maximumRetryCountHit)
            producer.stopProducingEvents()
        }
    }
}
