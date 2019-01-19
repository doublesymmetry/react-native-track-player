//
//  AudioPlayerObserver.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 09/03/2018.
//  Copyright © 2018 Jørgen Henrichsen. All rights reserved.
//

import Foundation
import AVFoundation

protocol AVPlayerObserverDelegate: class {
    
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
    
    let player: AVPlayer
    private let statusChangeOptions: NSKeyValueObservingOptions = [.new, .initial]
    private let timeControlStatusChangeOptions: NSKeyValueObservingOptions = [.new]
    var isObserving: Bool = false
    
    weak var delegate: AVPlayerObserverDelegate?
    
    init(player: AVPlayer) {
        self.player = player
    }
    
    deinit {
        if self.isObserving {
            self.player.removeObserver(self, forKeyPath: AVPlayerKeyPath.status, context: &AVPlayerObserver.context)
            self.player.removeObserver(self, forKeyPath: AVPlayerKeyPath.timeControlStatus, context: &AVPlayerObserver.context)
        }
    }
    
    /**
     Start receiving events from this observer.
     
     - Important: If this observer is already receiving events, it will first be removed. Never remove this observer manually.
     */
    func startObserving() {
        main.async {
            if self.isObserving {
                self.player.removeObserver(self, forKeyPath: AVPlayerKeyPath.status, context: &AVPlayerObserver.context)
                self.player.removeObserver(self, forKeyPath: AVPlayerKeyPath.timeControlStatus, context: &AVPlayerObserver.context)
            }
            self.isObserving = true
            self.player.addObserver(self, forKeyPath: AVPlayerKeyPath.status, options: self.statusChangeOptions, context: &AVPlayerObserver.context)
            self.player.addObserver(self, forKeyPath: AVPlayerKeyPath.timeControlStatus, options: self.timeControlStatusChangeOptions, context: &AVPlayerObserver.context)
        }
    }
    
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        guard context == &AVPlayerObserver.context, let observedKeyPath = keyPath else {
            super.observeValue(forKeyPath: keyPath, of: object, change: change, context: context)
            return
        }
        
        switch observedKeyPath {
            
        case AVPlayerKeyPath.status:
            self.handleStatusChange(change)
            
        case AVPlayerKeyPath.timeControlStatus:
            self.handleTimeControlStatusChange(change)
            
        default:
            break
            
        }
    }
    
    private func handleStatusChange(_ change: [NSKeyValueChangeKey: Any]?) {
        let status: AVPlayer.Status
        if let statusNumber = change?[.newKey] as? NSNumber {
            status = AVPlayer.Status(rawValue: statusNumber.intValue)!
        }
        else {
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
