import Foundation
import XCTest

@testable import SwiftAudioEx

extension XCTestCase {
    func waitForSeek(_ audioPlayer: AudioPlayer, to time: Double) {
        let seekEventListener = QueuedAudioPlayer.SeekEventListener()
        audioPlayer.event.seek.addListener(seekEventListener, seekEventListener.handleEvent)
        audioPlayer.seek(to: time)
        
        waitEqual(seekEventListener.eventResult.0, time, accuracy: 0.1, timeout: 5)
        waitEqual(seekEventListener.eventResult.1, true, timeout: 5)
    }
    
    func waitTrue(_ expression: @autoclosure @escaping () -> Bool, timeout: TimeInterval) {
        let expectation = XCTestExpectation(description: "Value should eventually equal expected value")
        
        DispatchQueue.global().async {
            while !expression() {
                usleep(100_000)  // Sleep for 100 milliseconds
            }
            expectation.fulfill()
        }
        
        wait(for: [expectation], timeout: timeout)
    }
    
    func waitEqual<T: Equatable>(_ expression1: @autoclosure @escaping () -> T, _ expression2: @autoclosure @escaping () -> T, timeout: TimeInterval) {
        let expectation = XCTestExpectation(description: "Value should eventually equal expected value")
        
        DispatchQueue.global().async {
            while expression1() != expression2() {
                usleep(100_000)  // Sleep for 100 milliseconds
            }
            expectation.fulfill()
        }
        
        wait(for: [expectation], timeout: timeout)
    }
    
    func waitEqual<T: Equatable>(_ expression1: @autoclosure @escaping () -> T, _ expression2: @autoclosure @escaping () -> T, accuracy: T, timeout: TimeInterval) where T: FloatingPoint {
        let expectation = XCTestExpectation(description: "Value should eventually equal expected value with accuracy")
        
        DispatchQueue.global().async {
            let startTime = Date()
            while abs(expression1() - expression2()) > accuracy {
                if Date().timeIntervalSince(startTime) >= timeout {
                    break
                }
                usleep(100_000)  // Sleep for 100 milliseconds
            }
            expectation.fulfill()
        }
        
        return wait(for: [expectation], timeout: timeout)
    }
    
    func waitEqual<T1: Equatable, T2: Equatable>(_ expression1: @autoclosure @escaping () -> (T1, T2), _ expression2: @autoclosure @escaping () -> (T1, T2), timeout: TimeInterval) {
        let expectation = XCTestExpectation(description: "Values should eventually be equal")
        
        DispatchQueue.global().async {
            while expression1() != expression2() {
                usleep(100_000)  // Sleep for 100 milliseconds
            }
            expectation.fulfill()
        }
        
        wait(for: [expectation], timeout: timeout)
    }
}
