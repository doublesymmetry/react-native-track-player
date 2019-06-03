//
//  AVPlayerItemNotificationObserver.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 12/03/2018.
//

import Foundation
import AVFoundation


protocol AVPlayerItemNotificationObserverDelegate: class {
    func itemDidPlayToEndTime()
}

/**
 Observes notifications posted by an AVPlayerItem.
 
 Currently only listening for the AVPlayerItemDidPlayToEndTime notification.
 */
class AVPlayerItemNotificationObserver {
    
    private let notificationCenter: NotificationCenter = NotificationCenter.default
    
    weak var observingItem: AVPlayerItem?
    weak var delegate: AVPlayerItemNotificationObserverDelegate?
    
    /**
     Will start observing notifications from an item.
     
     - parameter item: The item to observe.
     - important: Cannot observe more than one item at a time.
     */
    func startObserving(item: AVPlayerItem) {
        stopObservingCurrentItem()
        observingItem = item
        notificationCenter.addObserver(self, selector: #selector(itemDidPlayToEndTime), name: NSNotification.Name.AVPlayerItemDidPlayToEndTime, object: item)
    }
    
    /**
     Stop receiving notifications for the current item.
     */
    func stopObservingCurrentItem() {
        if let observingItem = observingItem {
            notificationCenter.removeObserver(self, name: NSNotification.Name.AVPlayerItemDidPlayToEndTime, object: observingItem)
        }
        observingItem = nil
    }
    
    @objc private func itemDidPlayToEndTime() {
        delegate?.itemDidPlayToEndTime()
    }
    
}
