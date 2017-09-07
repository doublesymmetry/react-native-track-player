//
//  MediaWrapperDelegate.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 14.08.17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

import Foundation

protocol MediaWrapperDelegate: class {
    func playerUpdatedState()
    func playerSwitchedTracks(trackId: String?)
    func playerTrackEnded(trackId: String?, time: TimeInterval?)
    func playerExhaustedQueue()
    func playbackFailed(error: Error)
    func playbackUpdatedProgress(to time: TimeInterval)
}
