//
//  Capabilities.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 07.09.17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

import Foundation

enum Capability: String {
    case play, pause, togglePlayPause, stop, next, previous, jumpForward, jumpBackward, seek
    
    func mapToPlayerCommand(jumpInterval: NSNumber?) -> RemoteCommand {
        switch self {
        case .stop:
            return .stop
        case .play:
            return .play
        case .pause:
            return .pause
        case .togglePlayPause:
            return .togglePlayPause
        case .next:
            return .next
        case .previous:
            return .previous
        case .seek:
            return .changePlaybackPosition
        case .jumpForward:
            return .skipForward(preferredIntervals: [jumpInterval ?? 15])
        case .jumpBackward:
            return .skipBackward(preferredIntervals: [jumpInterval ?? 15])
        }
    }
}
