import XCTest
import AVFoundation
@testable import SwiftAudioEx

class AVPlayerObserverTests: XCTestCase, AVPlayerObserverDelegate {

    var status: AVPlayer.Status?
    var timeControlStatus: AVPlayer.TimeControlStatus?

    var player: AVPlayer!
    var observer: AVPlayerObserver!

    override func setUp() {
        super.setUp()
        player = AVPlayer()
        player.volume = 0.0
        observer = AVPlayerObserver()
        observer.player = player
        observer.delegate = self
    }

    override func tearDown() {
        player = nil
        observer = nil
        super.tearDown()
    }

    func testObserverIsNotObserving() {
        XCTAssertFalse(observer.isObserving)
    }

    func testObserverIsObservingWhenObservingStarted() {
        observer.startObserving()
        XCTAssertTrue(observer.isObserving)
    }

    func testObserverUpdatesDelegateWhenPlayerStarted() {
        observer.startObserving()
        player.replaceCurrentItem(with: AVPlayerItem(url: URL(fileURLWithPath: Source.path)))
        player.play()
        
        XCTAssertNotNil(self.status)
        XCTAssertNotNil(self.timeControlStatus)
    }

    func testObserverIsObservingWhenObservingAgain() {
        observer.startObserving()
        observer.startObserving()
        XCTAssertTrue(observer.isObserving)
    }

    func testObserverIsNotObservingWhenObservingStopped() {
        observer.startObserving()
        observer.stopObserving()
        XCTAssertFalse(observer.isObserving)
    }
    
    // MARK: - AVPlayerObserverDelegate

    func player(statusDidChange status: AVPlayer.Status) {
        self.status = status
    }

    func player(didChangeTimeControlStatus status: AVPlayer.TimeControlStatus) {
        self.timeControlStatus = status
    }
}

