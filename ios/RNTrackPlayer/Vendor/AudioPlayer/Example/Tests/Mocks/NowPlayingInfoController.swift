//
//  NowPlayingInfoController.swift
//  SwiftAudio_Tests
//
//  Created by Jørgen Henrichsen on 03/03/2019.
//  Copyright © 2019 CocoaPods. All rights reserved.
//

import Foundation
import MediaPlayer

@testable import SwiftAudio

class NowPlayingInfoController_Mock: NowPlayingInfoControllerProtocol {
    
    var info: [String: Any] = [:]
    
    required public init() {
    }
    
    required public init(infoCenter: NowPlayingInfoCenter) {
    }
    
    public func set(keyValues: [NowPlayingInfoKeyValue]) {
        keyValues.forEach { (keyValue) in
            info[keyValue.getKey()] = keyValue.getValue()
        }
    }
    
    public func set(keyValue: NowPlayingInfoKeyValue) {
        info[keyValue.getKey()] = keyValue.getValue()
    }
    
    func getTitle() -> String? {
        return info[MediaItemProperty.title(nil).getKey()] as? String
    }
    
    func getArtist() -> String? {
        return info[MediaItemProperty.artist(nil).getKey()] as? String
    }
    
    func getAlbumTitle() -> String? {
        return info[MediaItemProperty.albumTitle(nil).getKey()] as? String
    }
    
    func getRate() -> Double? {
        return info[NowPlayingInfoProperty.playbackRate(nil).getKey()] as? Double
    }
    
    func getDuration() -> Double? {
        return info[MediaItemProperty.duration(nil).getKey()] as? Double
    }
    
    func getCurrentTime() -> Double? {
        return info[NowPlayingInfoProperty.elapsedPlaybackTime(nil).getKey()] as? Double
    }
    
    func getArtwork() -> MPMediaItemArtwork? {
        return info[MediaItemProperty.artwork(nil).getKey()] as? MPMediaItemArtwork
    }
    
    func clear() {
        info = [:]
    }
    
}
