import Foundation

class EventEmitter {

    public static var shared = EventEmitter()

    private var eventEmitter: RNTrackPlayer!

    func register(eventEmitter: RNTrackPlayer) {
        self.eventEmitter = eventEmitter
    }

    func emit(event: EventType, body: Any?) {
        self.eventEmitter.sendEvent(withName: event.rawValue, body: body)
    }

    var allEvents = [
        EventType.PlaybackMetadataReceived.rawValue,
        EventType.PlaybackError.rawValue,
        EventType.PlaybackQueueEnded.rawValue,
        EventType.PlaybackTrackChanged.rawValue,
        EventType.PlaybackActiveTrackChanged.rawValue,
        EventType.PlaybackState.rawValue,
        EventType.PlaybackProgressUpdated.rawValue,
        EventType.PlaybackPlayWhenReadyChanged.rawValue,
        EventType.SleepTimerChanged.rawValue,
        EventType.SleepTimerComplete.rawValue,
        EventType.RemoteDuck.rawValue,
        EventType.RemoteSeek.rawValue,
        EventType.RemoteNext.rawValue,
        EventType.RemotePrevious.rawValue,
        EventType.RemoteStop.rawValue,
        EventType.RemotePause.rawValue,
        EventType.RemotePlay.rawValue,
        EventType.RemoteJumpForward.rawValue,
        EventType.RemoteJumpBackward.rawValue,
        EventType.RemoteLike.rawValue,
        EventType.RemoteDislike.rawValue,
        EventType.RemoteBookmark.rawValue
    ]
}
