//
//  AVPlayerWrapperState.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 10/03/2018.
//  Copyright © 2018 Jørgen Henrichsen. All rights reserved.
//

import Foundation


/**
 The current state of the AudioPlayer.
 */
public enum AVPlayerWrapperState: String {
    
    /// The current item is set, and the player is ready to start loading (buffering).
    case ready
    
    /// The current item is loading, getting ready to play.
    case loading
    
    /// The player is paused.
    case paused
    
    /// The player is playing.
    case playing
    
    /// No item loaded, the player is stopped.
    case idle
    
}
