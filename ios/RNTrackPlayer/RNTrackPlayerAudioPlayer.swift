//
//  RNTrackPlayerAudioPlayer.swift
//  RNTrackPlayer
//
//  Created by Dustin Bahr on 10/04/2020.
//

import Foundation
import MediaPlayer

/**
 An audio player that sends React Native events at appropriate times.

 This custom player was implemented to overcome issues that are caused by the 
 asynchronous events emitted by SwiftAudio.

 Because these events are asynchronous, properties such as currentItem did not
 always contain the expected values. This led to events being sent to React Native
 with incorrect information.

 Additionally for some reason reacting to asynchronous events by trying to go to
 the next track, would sometimes cause the controls on the lock screen to behave
 poorly, and the queue to not advance properly.
 */
public class RNTrackPlayerAudioPlayer: QueuedAudioPlayer {

	public var reactEventEmitter: RCTEventEmitter

	// Override _currentItem so that we can send an event when it changes.
	override var _currentItem: AudioItem? {
		willSet(newCurrentItem) {
			if ((newCurrentItem as? Track) === (_currentItem as? Track)) {
				return
			}

			self.reactEventEmitter.sendEvent(withName: "playback-track-changed", body: [
				"track": (_currentItem as? Track)?.id ?? nil,
				"position": self.currentTime,
				"nextTrack": (newCurrentItem as? Track)?.id ?? nil,
				])
		}
	}

	// Override init to include a reference to the React Event Emitter.
	public init(reactEventEmitter: RCTEventEmitter) {
        self.reactEventEmitter = reactEventEmitter
		super.init()
    }

	// MARK: - AVPlayerWrapperDelegate
    
    override func AVWrapper(didChangeState state: AVPlayerWrapperState) {
        super.AVWrapper(didChangeState: state)
		self.reactEventEmitter.sendEvent(withName: "playback-state", body: ["state": state.rawValue])
    }
    
    override func AVWrapper(failedWithError error: Error?) {
        super.AVWrapper(failedWithError: error)
        self.reactEventEmitter.sendEvent(withName: "playback-error", body: ["error": error?.localizedDescription])
    }
    
    override func AVWrapperItemDidPlayToEndTime() {
        if self.nextItems.count == 0 {
			// For consistency sake, send an event for the track changing to nothing
			self.reactEventEmitter.sendEvent(withName: "playback-track-changed", body: [
				"track": (self.currentItem as? Track)?.id ?? nil,
				"position": self.currentTime,
				"nextTrack": nil,
				])

			// fire an event for the queue ending
			self.reactEventEmitter.sendEvent(withName: "playback-queue-ended", body: [
				"track": (self.currentItem as? Track)?.id,
				"position": self.currentTime,
				])
		} 
		super.AVWrapperItemDidPlayToEndTime()
    }

}
