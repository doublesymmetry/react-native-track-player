//
//  SimpleAudioPlayer.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 24/03/2018.
//

import Foundation
import MediaPlayer

/**
 A simple audio player that keeps on item at a time.
 */
public class SimpleAudioPlayer: AudioPlayer {
    
    public override init(infoCenter: MPNowPlayingInfoCenter = MPNowPlayingInfoCenter.default(), remoteCommandController: RemoteCommandController? = nil) {
        super.init(infoCenter: infoCenter, remoteCommandController: remoteCommandController)
    }
    
    /**
     Load an AudioItem into the manager.
     
     - parameter item: The AudioItem to load. The info given in this item is the one used for the InfoCenter.
     - parameter playWhenReady: Immediately start playback when the item is ready. Default is `true`. If you disable this you have to call play() or togglePlay() when the `state` switches to `ready`.
     */
    public func load(item: AudioItem, playWhenReady: Bool = true) throws {
        try self.loadItem(item, playWhenReady: playWhenReady)
    }
    
}
