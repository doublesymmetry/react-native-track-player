//
//  AVPlayerItemObserver.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 28/07/2018.
//

import Foundation
import AVFoundation

protocol AVPlayerItemObserverDelegate: AnyObject {
    
    /**
     Called when the duration of the observed item is updated.
     */
    func item(didUpdateDuration duration: Double)
    
    /**
     Called when the playback of the observed item is or is no longer likely to keep up.
     */
    func item(didUpdatePlaybackLikelyToKeepUp playbackLikelyToKeepUp: Bool)
    /**
     Called when the observed item receives metadata
     */
    func item(didReceiveTimedMetadata metadata: [AVTimedMetadataGroup])
    
}

/**
 Observing an AVPlayers status changes.
 */
class AVPlayerItemObserver: NSObject {
    
    private static var context = 0
    private var currentMetadataOutput: AVPlayerItemMetadataOutput?
    
    private struct AVPlayerItemKeyPath {
        static let duration = #keyPath(AVPlayerItem.duration)
        static let loadedTimeRanges = #keyPath(AVPlayerItem.loadedTimeRanges)
        static let playbackLikelyToKeepUp = #keyPath(AVPlayerItem.isPlaybackLikelyToKeepUp)
    }
    
    private(set) var isObserving: Bool = false
    
    private(set) weak var observingItem: AVPlayerItem?
    weak var delegate: AVPlayerItemObserverDelegate?
    
    override init() {
        super.init()
    }
    
    deinit {
        stopObservingCurrentItem()
    }
    
    /**
     Start observing an item. Will remove self as observer from old item, if any.
     
     - parameter item: The player item to observe.
     */
    func startObserving(item: AVPlayerItem) {
        stopObservingCurrentItem()
        
        self.isObserving = true
        self.observingItem = item
        item.addObserver(self, forKeyPath: AVPlayerItemKeyPath.duration, options: [.new], context: &AVPlayerItemObserver.context)
        item.addObserver(self, forKeyPath: AVPlayerItemKeyPath.loadedTimeRanges, options: [.new], context: &AVPlayerItemObserver.context)
        item.addObserver(self, forKeyPath: AVPlayerItemKeyPath.playbackLikelyToKeepUp, options: [.new], context: &AVPlayerItemObserver.context)
        
        // Create and add a new metadata output to the item.
        let metadataOutput = AVPlayerItemMetadataOutput()
        metadataOutput.setDelegate(self, queue: .main)
        item.add(metadataOutput)
        self.currentMetadataOutput = metadataOutput
    }
    
    func stopObservingCurrentItem() {
        guard let observingItem = observingItem, isObserving else {
            return
        }
        
        observingItem.removeObserver(self, forKeyPath: AVPlayerItemKeyPath.duration, context: &AVPlayerItemObserver.context)
        observingItem.removeObserver(self, forKeyPath: AVPlayerItemKeyPath.loadedTimeRanges, context: &AVPlayerItemObserver.context)
        observingItem.removeObserver(self, forKeyPath: AVPlayerItemKeyPath.playbackLikelyToKeepUp, context: &AVPlayerItemObserver.context)
        
        // Remove all metadata outputs from the item.
        observingItem.removeAllMetadataOutputs()
        
        isObserving = false
        self.observingItem = nil
        self.currentMetadataOutput = nil
    }
    
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        guard context == &AVPlayerItemObserver.context, let observedKeyPath = keyPath else {
            super.observeValue(forKeyPath: keyPath, of: object, change: change, context: context)
            return
        }
        
        switch observedKeyPath {
        case AVPlayerItemKeyPath.duration:
            if let duration = change?[.newKey] as? CMTime {
                delegate?.item(didUpdateDuration: duration.seconds)
            }
            
        case AVPlayerItemKeyPath.loadedTimeRanges:
            if let ranges = change?[.newKey] as? [NSValue], let duration = ranges.first?.timeRangeValue.duration {
                delegate?.item(didUpdateDuration: duration.seconds)
            }
            
        case AVPlayerItemKeyPath.playbackLikelyToKeepUp:
            if let playbackLikelyToKeepUp = change?[.newKey] as? Bool {
                delegate?.item(didUpdatePlaybackLikelyToKeepUp: playbackLikelyToKeepUp)
            }
            
        default: break
            
        }
    }
}

extension AVPlayerItemObserver: AVPlayerItemMetadataOutputPushDelegate {
    func metadataOutput(_ output: AVPlayerItemMetadataOutput, didOutputTimedMetadataGroups groups: [AVTimedMetadataGroup], from track: AVPlayerItemTrack?) {
        if output == currentMetadataOutput {
            delegate?.item(didReceiveTimedMetadata: groups)
        }
    }
}

extension AVPlayerItem {
    func removeAllMetadataOutputs() {
        for output in self.outputs.filter({ $0 is AVPlayerItemMetadataOutput }) {
            self.remove(output)
        }
    }
}
