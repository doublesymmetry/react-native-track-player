//
//  AudioPlayerObserver.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 09/03/2018.
//  Copyright © 2018 Jørgen Henrichsen. All rights reserved.
//

import Foundation
import AVFoundation

protocol AVPlayerObserverDelegate: AnyObject {
    
    /**
     Called when the AVPlayer.status changes.
     */
    func player(statusDidChange status: AVPlayer.Status)
    
    /**
     Called when the AVPlayer.timeControlStatus changes.
     */
    func player(didChangeTimeControlStatus status: AVPlayer.TimeControlStatus)
}

/**
 Observing an AVPlayers status changes.
 */
class AVPlayerObserver: NSObject {

    private static var context = 0
    private let main: DispatchQueue = .main

    private struct AVPlayerKeyPath {
        static let status = #keyPath(AVPlayer.status)
        static let timeControlStatus = #keyPath(AVPlayer.timeControlStatus)
    }

    private let statusChangeOptions: NSKeyValueObservingOptions = [.new, .initial]
    private let timeControlStatusChangeOptions: NSKeyValueObservingOptions = [.new]
    private(set) var isObserving: Bool = false

    weak var delegate: AVPlayerObserverDelegate?
    weak var player: AVPlayer? {
        willSet {
            stopObserving()
        }
    }

    deinit {
        stopObserving()
    }

    /**
     Start receiving events from this observer.
     */
    func startObserving() {
        if (isObserving) { return };
        guard let player = player else {
            return
        }
        isObserving = true
        player.addObserver(
            self,
            forKeyPath: AVPlayerKeyPath.status,
            options: statusChangeOptions,
            context: &AVPlayerObserver.context
        )
        player.addObserver(
            self,
            forKeyPath: AVPlayerKeyPath.timeControlStatus,
            options: timeControlStatusChangeOptions,
            context: &AVPlayerObserver.context
        )
    }

    func stopObserving() {
        guard let player = player, isObserving else {
            return
        }
        player.removeObserver(self, forKeyPath: AVPlayerKeyPath.status, context: &AVPlayerObserver.context)
        player.removeObserver(self, forKeyPath: AVPlayerKeyPath.timeControlStatus, context: &AVPlayerObserver.context)
        isObserving = false
    }

    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey: Any]?, context: UnsafeMutableRawPointer?) {
        guard context == &AVPlayerObserver.context, let observedKeyPath = keyPath else {
            super.observeValue(forKeyPath: keyPath, of: object, change: change, context: context)
            return
        }

        switch observedKeyPath {
        case AVPlayerKeyPath.status:
            handleStatusChange(change)

        case AVPlayerKeyPath.timeControlStatus:
            handleTimeControlStatusChange(change)

        default:
            break
        }
    }

    private func handleStatusChange(_ change: [NSKeyValueChangeKey: Any]?) {
        let status: AVPlayer.Status
        if let statusNumber = change?[.newKey] as? NSNumber {
            status = AVPlayer.Status(rawValue: statusNumber.intValue)!
        } else {
            status = .unknown
        }
        delegate?.player(statusDidChange: status)
    }

    private func handleTimeControlStatusChange(_ change: [NSKeyValueChangeKey: Any]?) {
        let status: AVPlayer.TimeControlStatus
        if let statusNumber = change?[.newKey] as? NSNumber {
            status = AVPlayer.TimeControlStatus(rawValue: statusNumber.intValue)!
            delegate?.player(didChangeTimeControlStatus: status)
        }
    }
}
