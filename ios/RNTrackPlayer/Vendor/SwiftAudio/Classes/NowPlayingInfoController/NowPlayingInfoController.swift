//
//  MediaInfoController.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 15/03/2018.
//

import Foundation
import MediaPlayer

public class NowPlayingInfoController: NowPlayingInfoControllerProtocol {
    private let concurrentInfoQueue: DispatchQueueType

    private var _infoCenter: NowPlayingInfoCenter
    private var _info: [String: Any] = [:]
    
    var infoCenter: NowPlayingInfoCenter {
        return _infoCenter
    }
    
    var info: [String: Any] {
        return _info
    }
    
    public required init() {
        self.concurrentInfoQueue = DispatchQueue(label: "com.doublesymmetry.nowPlayingInfoQueue", attributes: .concurrent)
        self._infoCenter = MPNowPlayingInfoCenter.default()
    }

    /// Used for testing purposes.
    public required init(dispatchQueue: DispatchQueueType, infoCenter: NowPlayingInfoCenter) {
        self.concurrentInfoQueue = dispatchQueue
        self._infoCenter = infoCenter
    }
    
    public required init(infoCenter: NowPlayingInfoCenter) {
        self.concurrentInfoQueue = DispatchQueue(label: "com.doublesymmetry.nowPlayingInfoQueue", attributes: .concurrent)
        self._infoCenter = infoCenter
    }
    
    public func set(keyValues: [NowPlayingInfoKeyValue]) {
        concurrentInfoQueue.async(flags: .barrier) { [weak self] in
            guard let self = self else { return }

            keyValues.forEach { (keyValue) in
                self._info[keyValue.getKey()] = keyValue.getValue()
            }

            self._infoCenter.nowPlayingInfo = self._info
        }
    }
    
    public func set(keyValue: NowPlayingInfoKeyValue) {
        concurrentInfoQueue.async(flags: .barrier) { [weak self] in
            guard let self = self else { return }

            self._info[keyValue.getKey()] = keyValue.getValue()
            self._infoCenter.nowPlayingInfo = self._info
        }
    }
    
    public func clear() {
        concurrentInfoQueue.async(flags: .barrier) { [weak self] in
            guard let self = self else { return }

            self._info = [:]
            self._infoCenter.nowPlayingInfo = self._info
        }
    }
    
}
