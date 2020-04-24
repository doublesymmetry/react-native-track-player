//
//  RNTrackPlayerAudioPlayer.swift
//  RNTrackPlayer
//
//  Created by Dustin Bahr on 24/04/2020.
//

import Foundation
import MediaPlayer

public class RNTrackPlayerAudioPlayer: QueuedAudioPlayer {

	// MARK: - Remote Command Center
    
	/**
	* Override this method in order to prevent re-enabling remote commands every
	* time a track loads.
	*
	* React Native Track Player does not use this feature of SwiftAudio which
	* allows defining remote commands per track.
	*
	* Because of the asychronous nature of controlling the queue from the JS
	* side, re-enabling commands in this way causes the lock screen controls to
	* behave poorly.
	*/
    override func enableRemoteCommands(forItem item: AudioItem) {
        if let item = item as? RemoteCommandable {
            self.enableRemoteCommands(item.getCommands())
        }
		else {
			// React Native Track Player does this manually in
			// RNTrackPlayer.updateOptions()
			// self.enableRemoteCommands(remoteCommands)
        }
    }
}
