//
//  MediaInfoController.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 15/03/2018.
//

import Foundation
import MediaPlayer


public protocol NowPlayingInfoKeyValue {
    func getKey() -> String
    func getValue() -> Any?
}

public class NowPlayingInfoController {
    
    let infoCenter: MPNowPlayingInfoCenter
    
    var info: [String: Any]
    
    /**
     Create a new NowPlayingInfoController.
     
     - parameter infoCenter: The MPNowPlayingInfoCenter to use. Default is `MPNowPlayingInfoCenter.default()`
     */
    public init(infoCenter: MPNowPlayingInfoCenter = MPNowPlayingInfoCenter.default()) {
        self.infoCenter = infoCenter
        self.info = [:]
    }
    
    /**
     This updates a set of values in the now playing info.
     
     - Warning: This will reset the now playing info completely! Use this function when starting playback of a new item.
     */
    public func set(keyValues: [NowPlayingInfoKeyValue]) {
        self.info = [:]
        keyValues.forEach { (keyValue) in
            info[keyValue.getKey()] = keyValue.getValue()
        }
    }
    
    /**
     This updates a single value in the now playing info.
     */
    public func set(keyValue: NowPlayingInfoKeyValue) {
        info[keyValue.getKey()] = keyValue.getValue()
        self.infoCenter.nowPlayingInfo = info
    }
    
}
