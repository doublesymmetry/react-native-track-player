import XCTest
import AVFoundation
@testable import SwiftAudioEx

class AVPlayerItemNotificationObserverTests: XCTestCase {

    var item: AVPlayerItem!
    var observer: AVPlayerItemNotificationObserver!

    override func setUp() {
        super.setUp()
        item = AVPlayerItem(url: URL(fileURLWithPath: Source.path))
        observer = AVPlayerItemNotificationObserver()
    }

    override func tearDown() {
        item = nil
        observer = nil
        super.tearDown()
    }

    func testObserverHasObservedItemWhenStartedObserving() {
        observer.startObserving(item: item)
        XCTAssertNotNil(observer.observingItem)
    }

    func testObserverHasNoObservedItemWhenEndedObserving() {
        observer.startObserving(item: item)
        observer.stopObservingCurrentItem()
        XCTAssertNil(observer.observingItem)
    }
}
