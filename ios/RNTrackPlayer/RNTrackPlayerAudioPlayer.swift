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

public class RNTrackPlayerAudioPlayer: QueuedAudioPlayer {

	public var reactEventEmitter: RCTEventEmitter

	// Used to store the rate that is given in TrackPlayer.setRate() so that we
	// can maintain the same rate in cases when SwiftAudio would not.
	private var _rate: Float

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

    override func AVWrapper(didReceiveMetadata metadata: [AVMetadataItem]) {
        func getMetadataItem(forIdentifier:AVMetadataIdentifier) -> String {
            return AVMetadataItem.metadataItems(from: metadata, filteredByIdentifier: forIdentifier).first?.stringValue ?? ""
        }

        super.AVWrapper(didReceiveMetadata: metadata)
        var source: String {
            switch metadata.first?.keySpace {
            case AVMetadataKeySpace.id3:
                return "id3"
            case AVMetadataKeySpace.icy:
                return "icy"
            case AVMetadataKeySpace.quickTimeMetadata:
                return "quicktime"
            case AVMetadataKeySpace.common:
                return "unknown"
            default: return "unknown"
            }
        }

        let album = getMetadataItem(forIdentifier: .commonIdentifierAlbumName)
        var artist = getMetadataItem(forIdentifier: .commonIdentifierArtist)
        var title = getMetadataItem(forIdentifier: .commonIdentifierTitle)
        var date = getMetadataItem(forIdentifier: .commonIdentifierCreationDate)
        var url = "";
        var genre = "";
        if (source == "icy") {
            url = getMetadataItem(forIdentifier: .icyMetadataStreamURL)
        } else if (source == "id3") {
            if (date.isEmpty) {
              date = getMetadataItem(forIdentifier: .id3MetadataDate)
            }
            genre = getMetadataItem(forIdentifier: .id3MetadataContentType)
            url = getMetadataItem(forIdentifier: .id3MetadataOfficialAudioSourceWebpage)
            if (url.isEmpty) {
              url = getMetadataItem(forIdentifier: .id3MetadataOfficialAudioFileWebpage)
            }
            if (url.isEmpty) {
              url = getMetadataItem(forIdentifier: .id3MetadataOfficialArtistWebpage)
            }
        } else if (source == "quicktime") {
            genre = getMetadataItem(forIdentifier: .quickTimeMetadataGenre)
        }

        // Detect ICY metadata and split title into artist & title:
        // - source should be either "unknown" (pre iOS 14) or "icy" (iOS 14 and above)
        // - we have a title, but no artist
        if ((source == "unknown" || source == "icy") && !title.isEmpty && artist.isEmpty) {
            if let index = title.range(of: " - ")?.lowerBound {
                artist = String(title.prefix(upTo: index));
                title = String(title.suffix(from: title.index(index, offsetBy: 3)));
            }
        }
        var data : [String : String?] = [
            "title": title.isEmpty ? nil : title,
            "url": url.isEmpty ? nil : url,
            "artist": artist.isEmpty ? nil : artist,
            "album": album.isEmpty ? nil : album,
            "date": date.isEmpty ? nil : date,
            "genre": genre.isEmpty ? nil : genre
        ]
        if (data.values.contains { $0 != nil }) {
            data["source"] = source
            self.reactEventEmitter.sendEvent(withName: "playback-metadata-received", body: data)
        }
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
