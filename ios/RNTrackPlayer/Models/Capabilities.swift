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
    case play, pause, togglePlayPause, stop, next, previous, jumpForward, jumpBackward, seek, like, dislike, bookmark

    func mapToPlayerCommand(forwardJumpInterval: NSNumber?,
                            backwardJumpInterval: NSNumber?,
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
        case .togglePlayPause:
            return .togglePlayPause
        case .next:
            return .next
        case .previous:
            return .previous
        case .seek:
            return .changePlaybackPosition
        case .jumpForward:
            return .skipForward(preferredIntervals: [(forwardJumpInterval ?? backwardJumpInterval) ?? 15])
        case .jumpBackward:
            return .skipBackward(preferredIntervals: [(backwardJumpInterval ?? forwardJumpInterval) ?? 15])
        case .like:
            return .like(isActive: likeOptions?["isActive"] as? Bool ?? false,
                         localizedTitle: likeOptions?["title"] as? String ?? "Like",
                         localizedShortTitle: likeOptions?["title"] as? String ?? "Like")
        case .dislike:
            return .dislike(isActive: dislikeOptions?["isActive"] as? Bool ?? false,
                            localizedTitle: dislikeOptions?["title"] as? String ?? "Dislike",
                            localizedShortTitle: dislikeOptions?["title"] as? String ?? "Dislike")
        case .bookmark:
            return .bookmark(isActive: bookmarkOptions?["isActive"] as? Bool ?? false,
                             localizedTitle: bookmarkOptions?["title"] as? String ?? "Bookmark",
                             localizedShortTitle: bookmarkOptions?["title"] as? String ?? "Bookmark")
        }
    }
}
