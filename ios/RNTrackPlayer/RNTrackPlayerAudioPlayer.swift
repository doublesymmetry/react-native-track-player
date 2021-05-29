//
//  RNTrackPlayerAudioPlayer.swift
//  RNTrackPlayer
//
//  Created by Dustin Bahr on 24/04/2020.
//

import Foundation
import MediaPlayer

/**
* An audio player that sends React Native events at appropriate times.
*
* This custom player was implemented to overcome issues that are caused by the
* asynchronous events emitted by SwiftAudio.
*
* Because these events are asynchronous, properties such as currentItem did not
* always contain the expected values. This led to events being sent to React
* Native with incorrect information.
*
* Additionally overriding the behavior of enableRemoteCommands fixes issues with
* lock screen controls.
*/

public class RNTrackPlayerAudioPlayer: QueuedAudioPlayer, QueueManagerDelegate {

	public var reactEventEmitter: RCTEventEmitter

	// Used to store the rate that is given in TrackPlayer.setRate() so that we
	// can maintain the same rate in cases when SwiftAudio would not.
	private var _rate: Float

	// Override rate so that we can maintain the same rate on future tracks.
	override public var rate: Float {
        get { return _rate }
        set { 
			_rate = newValue

			// Only set the rate on the wrapper if it is already playing.
			if wrapper.rate > 0 {
				wrapper.rate = newValue
			}
		}
    }

	// Override init to include a reference to the React Event Emitter.
	public init(reactEventEmitter: RCTEventEmitter) {
        self._rate = 1.0
		self.reactEventEmitter = reactEventEmitter
		super.init()
        self.queueManager.delegate = self
    }

    public override func stop() {
        super.stop()
        onTrackUpdate(previousIndex: currentIndex, nextIndex: nil)
    }

	// MARK: - AVPlayerWrapperDelegate
    
    override func AVWrapper(didChangeState state: AVPlayerWrapperState) {
        super.AVWrapper(didChangeState: state)
		
		// When a track starts playing, reset the rate to the stored rate
		switch state {
		case .playing:
			self.rate = _rate;
		default: break
		}

		self.reactEventEmitter.sendEvent(withName: "playback-state", body: ["state": state.rawValue])
    }
    
    override func AVWrapper(failedWithError error: Error?) {
        super.AVWrapper(failedWithError: error)
        self.reactEventEmitter.sendEvent(withName: "playback-error", body: ["error": error?.localizedDescription])
    }
    
    override func AVWrapperItemDidPlayToEndTime() {
        // fire an event for the queue ending
        if self.nextItems.count == 0 {
            self.reactEventEmitter.sendEvent(withName: "playback-queue-ended", body: [
                "track": currentIndex,
                "position": currentTime,
            ])
		} 
		super.AVWrapperItemDidPlayToEndTime()
    }

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

    // MARK: - Private Helpers

    private func onTrackUpdate(previousIndex: Int?, nextIndex: Int?) {
        reactEventEmitter.sendEvent(withName: "playback-track-changed", body: [
            "track": previousIndex,
            "position": currentTime,
            "nextTrack": nextIndex,
        ])
    }

    // MARK: - QueueManagerDelegate

    func onCurrentIndexChanged(oldIndex: Int, newIndex: Int) {
        onTrackUpdate(previousIndex: oldIndex, nextIndex: newIndex)
    }

    func onReceivedFirstItem() {
        onTrackUpdate(previousIndex: nil, nextIndex: 0)
    }
}
