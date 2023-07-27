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
}

extension RemoteCommand {
    static func fromRNArray(data: [[String: Any]]) -> [RemoteCommand] {
        var ret: [RemoteCommand] = []
        
        // iOS has a special remote command for handling play/pause.
        // We enable that only if both are present in the capabilities.
        let hasBothPlayAndPause = data.filter({
            $0["constant"] as? String == "play" || $0["constant"] as? String == "pause"
        }).count == 2
        
        if hasBothPlayAndPause {
            ret.append(.togglePlayPause)
        }
        
        // Map RN's data into remote actions we can pass on to SwiftAudioEx's player.
        for dict in data {
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
