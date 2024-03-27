import XCTest
import AVFoundation
@testable import SwiftAudioEx

class AVPlayerTimeObserverTests: XCTestCase {

    var player: AVPlayer!
    var observer: AVPlayerTimeObserver!

    override func setUp() {
        super.setUp()
        player = AVPlayer()
        player.automaticallyWaitsToMinimizeStalling = false
        player.volume = 0
        observer = AVPlayerTimeObserver(periodicObserverTimeInterval: TimeEventFrequency.everyQuarterSecond.getTime())
        observer.player = player
    }

    override func tearDown() {
        player = nil
        observer = nil
        super.tearDown()
    }

    func testObserverHasBoundaryTokenWhenStartedBoundaryTimeObserving() {
        observer.registerForBoundaryTimeEvents()
        XCTAssertNotNil(observer.boundaryTimeStartObserverToken)
    }

    func testObserverHasNoBoundaryTokenWhenEndedBoundaryTimeObserving() {
        observer.registerForBoundaryTimeEvents()
        observer.unregisterForBoundaryTimeEvents()
        XCTAssertNil(observer.boundaryTimeStartObserverToken)
    }

    func testObserverHasPeriodicTokenWhenStartedPeriodicTimeObserving() {
        observer.registerForPeriodicTimeEvents()
        XCTAssertNotNil(observer.periodicTimeObserverToken)
    }

    func testObserverHasNoPeriodicTokenWhenEndedPeriodicTimeObserving() {
        observer.registerForPeriodicTimeEvents()
        observer.unregisterForPeriodicEvents()
        XCTAssertNil(observer.periodicTimeObserverToken)
    }
}
