//
//  Capabilities.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 07.09.17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

import Foundation
import SwiftAudioEx

enum Capability: String {
    case play, pause, stop, next, previous, jumpForward, jumpBackward, seek, like, dislike, bookmark
    
    static func fromDictionaryArray(dicts: [[String: Any]]) -> [RemoteCommand] {
        var ret: [RemoteCommand] = []
        
        let hasBothPlayAndPause = dicts.filter({
            $0["constant"] as? String == "play" || $0["constant"] as? String == "pause"
        }).count == 2
        
        if hasBothPlayAndPause {
            ret.append(.togglePlayPause)
        }
        
        for dict in dicts {
            let cap = Capability(rawValue: dict["constant"] as! String)
            switch cap {
            case .play: ret.append(.play)
            case .pause: ret.append(.pause)
            case .stop: ret.append(.stop)
            case .seek: ret.append(.changePlaybackPosition)
            case .next: ret.append(.next)
            case .previous: ret.append(.previous)
            case .jumpForward:
                let forwardJumpInterval = dict["jumpInterval"] as? NSNumber ?? 15
                ret.append(.skipForward(preferredIntervals: [forwardJumpInterval]))
            case .jumpBackward:
                let backwardJumpInterval = dict["jumpInterval"] as? NSNumber ?? 15
                ret.append(.skipBackward(preferredIntervals: [backwardJumpInterval]))
            case .like:
                let isActive = dict["isActive"] as? Bool ?? false
                let title = dict["title"] as? String ?? "Like"
                ret.append(.like(isActive: isActive, localizedTitle: title, localizedShortTitle: title))
            case .dislike:
                let isActive = dict["isActive"] as? Bool ?? false
                let title = dict["title"] as? String ?? "Like"
                ret.append(.dislike(isActive: isActive, localizedTitle: title, localizedShortTitle: title))
            case .bookmark:
                let isActive = dict["isActive"] as? Bool ?? false
                let title = dict["title"] as? String ?? "Like"
                ret.append(.bookmark(isActive: isActive, localizedTitle: title, localizedShortTitle: title))
            case .none:
                break
            }
        }
        
        return ret
    }
}
