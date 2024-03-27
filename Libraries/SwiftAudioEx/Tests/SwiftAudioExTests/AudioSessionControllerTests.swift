import XCTest
import AVFoundation
@testable import SwiftAudioEx

class AudioSessionControllerTests: XCTestCase {
    var audioSessionController: AudioSessionController!
    var delegate: AudioSessionControllerDelegateImplementation!

    override func setUp() {
        super.setUp()
        audioSessionController = AudioSessionController(audioSession: NonFailingAudioSession())
        delegate = AudioSessionControllerDelegateImplementation()
    }

    override func tearDown() {
        audioSessionController = nil
        delegate = nil
        super.tearDown()
    }

    func testAudioSessionIsInactive() {
        XCTAssertFalse(audioSessionController.audioSessionIsActive)
    }

    func testActivateSession() {
        do {
            try audioSessionController.activateSession()
            XCTAssertTrue(audioSessionController.audioSessionIsActive)
        } catch {
            XCTFail("Failed to activate session: \(error)")
        }
    }

    func testDeactivateSession() {
        do {
            try audioSessionController.activateSession()
            try audioSessionController.deactivateSession()
            XCTAssertFalse(audioSessionController.audioSessionIsActive)
        } catch {
            XCTFail("Failed to deactivate session: \(error)")
        }
    }

    func testIsObservingForInterruptions() {
        XCTAssertTrue(audioSessionController.isObservingForInterruptions)
    }

    func testIsObservingForInterruptionsFalse() {
        audioSessionController.isObservingForInterruptions = false
        XCTAssertFalse(audioSessionController.isObservingForInterruptions)
    }

    func testInterruptionEnded() {
        let notification = Notification(
            name: AVAudioSession.interruptionNotification,
            object: nil,
            userInfo: [
                AVAudioSessionInterruptionTypeKey: UInt(0),
                AVAudioSessionInterruptionOptionKey: UInt(1),
            ]
        )
        audioSessionController.delegate = delegate
        audioSessionController.handleInterruption(notification: notification)
        XCTAssertEqual(delegate.interruptionType, .ended(shouldResume: true))
    }

    func testInterruptionBegan() {
        let notification = Notification(
            name: AVAudioSession.interruptionNotification,
            object: nil,
            userInfo: [AVAudioSessionInterruptionTypeKey: UInt(1)]
        )
        audioSessionController.delegate = delegate
        audioSessionController.handleInterruption(notification: notification)
        XCTAssertEqual(delegate.interruptionType, .began)
    }

    func testAudioSessionIsInactiveWithFailingAudioSession() {
        audioSessionController = AudioSessionController(audioSession: FailingAudioSession())
        try? audioSessionController.activateSession()
        XCTAssertFalse(audioSessionController.audioSessionIsActive)
    }
}

class AudioSessionControllerDelegateImplementation: AudioSessionControllerDelegate {
    var interruptionType: InterruptionType? = nil

    func handleInterruption(type: InterruptionType) {
        self.interruptionType = type
    }
}
