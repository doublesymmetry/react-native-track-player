import XCTest
import MediaPlayer
@testable import SwiftAudioEx

class AudioPlayerEventTests: XCTestCase {
    
    class EventListener {
        var handleEvent: ((Void)) -> Void = { _ in }
    }
    
    var event: AudioPlayer.Event<(Void)>!
    
    override func setUp() {
        super.setUp()
        event = AudioPlayer.Event()
    }
    
    override func tearDown() {
        event = nil
        super.tearDown()
    }

    func testEventAddListener() {
        let listener = EventListener()
        event.addListener(listener, listener.handleEvent)
        waitTrue(self.event.invokers.count > 0, timeout: 5)
    }

    func testEventRemoveListener() {
        var listener: EventListener! = EventListener()
        event.addListener(listener, listener.handleEvent)
        listener = nil
        event.emit(data: ())
        
        waitEqual(self.event.invokers.count, 0, timeout: 5)
    }

    func testEventAddMultipleListeners() {
        var listeners = [EventListener]()
        
        listeners = (0..<15).map { _ in
            let listener = EventListener()
            event.addListener(listener, listener.handleEvent)
            return listener
        }
        
        waitEqual(self.event.invokers.count, listeners.count, timeout: 5)
    }
    
    func testEventRemoveOneListener() {
        var listeners = [EventListener]()
        
        listeners = (0..<15).map { _ in
            let listener = EventListener()
            event.addListener(listener, listener.handleEvent)
            return listener
        }
        
        let listenerToRemove = listeners[listeners.count / 2]
        event.removeListener(listenerToRemove)
        
        waitEqual(self.event.invokers.count, listeners.count - 1, timeout: 5)
    }
}
