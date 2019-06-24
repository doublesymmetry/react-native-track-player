//
//  Capabilities.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 07.09.17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

import Foundation

enum Capability: String {
    case play, pause, stop, next, previous, jumpForward, jumpBackward, seek, like, dislike, bookmark
    
    func mapToPlayerCommand(jumpInterval: NSNumber?,
                            likeOptions: [String: Any]?,
                            dislikeOptions: [String: Any]?,
                            bookmarkOptions: [String: Any]?) -> RemoteCommand {
        switch self {
        case .stop:
            return .stop
        case .play:
            return .play
        case .pause:
            return .pause
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
        case .like:
            return .like(isActive: likeOptions["isActive"] ?? false,
                         localizedTitle: likeOptions["title"] ?? "Like",
                         localizedShortTitle: likeOptions["title"] ?? "Like")
        case .dislike:
            return .dislike(isActive: likeOptions["isActive"] ?? false,
                            localizedTitle: likeOptions["title"] ?? "Dislike",
                            localizedShortTitle: likeOptions["title"] ?? "Dislike")
        case .bookmark:
            return .bookmark(isActive: likeOptions["isActive"] ?? false,
                             localizedTitle: likeOptions["title"] ?? "Bookmark",
                             localizedShortTitle: likeOptions["title"] ?? "Bookmark")
        }
    }
}
