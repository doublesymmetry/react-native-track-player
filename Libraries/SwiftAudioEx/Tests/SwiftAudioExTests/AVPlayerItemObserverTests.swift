import XCTest
import AVFoundation
@testable import SwiftAudioEx

class AVPlayerItemObserverTests: XCTestCase {
    var observer: AVPlayerItemObserver!

    override func setUp() {
        super.setUp()
        observer = AVPlayerItemObserver()
    }

    override func tearDown() {
        observer = nil
        super.tearDown()
    }

    func testObservingItem() {
        let item = AVPlayerItem(url: URL(fileURLWithPath: Source.path))
        observer.startObserving(item: item)
        XCTAssertNotNil(observer.observingItem)
    }

    func testIsObserving() {
        XCTAssertFalse(observer.isObserving)

        let item = AVPlayerItem(url: URL(fileURLWithPath: Source.path))
        observer.startObserving(item: item)
        XCTAssertTrue(observer.isObserving)
    }
    
    func testObservingInQuickSucccession() {
        for _ in 0...1000 {
            let item = AVPlayerItem(url: URL(fileURLWithPath: Source.path))
            observer.startObserving(item: item)
        }
    }
}
