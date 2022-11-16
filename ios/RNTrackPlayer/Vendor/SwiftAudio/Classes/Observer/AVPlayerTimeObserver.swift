//
//  AudioPlayerTimeEventObserver.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 09/03/2018.
//  Copyright © 2018 Jørgen Henrichsen. All rights reserved.
//

import Foundation
import AVFoundation

protocol AVPlayerTimeObserverDelegate: class {
    func audioDidStart()
    func timeEvent(time: CMTime)
}

/**
 Class for observing time-based events from the AVPlayer
 */
class AVPlayerTimeObserver {
    
    /// The time to use as start boundary time. Cannot be zero.
    private static let startBoundaryTime: CMTime = CMTime(value: 1, timescale: 1000)
    
    var boundaryTimeStartObserverToken: Any?
    var periodicTimeObserverToken: Any?
    
    weak var player: AVPlayer? {
        willSet {
            unregisterForBoundaryTimeEvents()
            unregisterForPeriodicEvents()
        }
    }
    
    /// The frequence to receive periodic time events.
    /// Setting this to a new value will trigger a re-registering to the periodic events of the player.
    var periodicObserverTimeInterval: CMTime {
        didSet {
            if oldValue != periodicObserverTimeInterval {
                registerForPeriodicTimeEvents()
            }
        }
    }
    
    weak var delegate: AVPlayerTimeObserverDelegate?
    
    init(periodicObserverTimeInterval: CMTime) {
        self.periodicObserverTimeInterval = periodicObserverTimeInterval
    }
    
    deinit {
        unregisterForPeriodicEvents()
        unregisterForBoundaryTimeEvents()
    }
    
    /**
     Will register for the AVPlayer BoundaryTimeEvents, to trigger start and complete events.
     */
    func registerForBoundaryTimeEvents() {
        guard let player = player else {
            return
        }
        unregisterForBoundaryTimeEvents()
        let startBoundaryTimes: [NSValue] = [AVPlayerTimeObserver.startBoundaryTime].map({NSValue(time: $0)})
        boundaryTimeStartObserverToken = player.addBoundaryTimeObserver(forTimes: startBoundaryTimes, queue: nil, using: { [weak self] in
            self?.delegate?.audioDidStart()
        })
    }
    
    /**
     Unregister from the boundary events of the player.
     */
    func unregisterForBoundaryTimeEvents() {
        guard let player = player, let boundaryTimeStartObserverToken = boundaryTimeStartObserverToken else {
            return
        }
        player.removeTimeObserver(boundaryTimeStartObserverToken)
        self.boundaryTimeStartObserverToken = nil
    }
    
    /**
     Start observing periodic time events.
     Will trigger unregisterForPeriodicEvents() first to avoid multiple subscriptions.
     */
    func registerForPeriodicTimeEvents() {
        guard let player = player else {
            return
        }
        unregisterForPeriodicEvents()
        periodicTimeObserverToken = player.addPeriodicTimeObserver(forInterval: periodicObserverTimeInterval, queue: nil, using: { (time) in
            self.delegate?.timeEvent(time: time)
        })
    }
    
    /**
     Unregister for periodic events.
     */
    func unregisterForPeriodicEvents() {
        guard let player = player, let periodicTimeObserverToken = periodicTimeObserverToken else {
            return
        }
        player.removeTimeObserver(periodicTimeObserverToken)
        self.periodicTimeObserverToken = nil
    }
    
}
