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
}
