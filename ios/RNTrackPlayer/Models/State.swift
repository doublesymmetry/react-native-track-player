//
//  State.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 02.12.21.
//

import Foundation
import SwiftAudioEx

enum State: String {
    case none, ready, playing, paused, stopped, buffering, connecting

    static func fromPlayerState(state: AVPlayerWrapperState) -> State {
        switch state {
        case .paused: return .paused
        case .buffering: return .buffering
        case .idle: return .none
        case .loading: return .connecting
        case .playing: return .playing
        case .ready: return .ready
        }
    }
}
