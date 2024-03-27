import XCTest
import MediaPlayer
@testable import SwiftAudioEx

class NowPlayingInfoControllerTests: XCTestCase {
    
    var nowPlayingController: NowPlayingInfoController!
    var infoCenter: NowPlayingInfoCenter_Mock!
    
    override func setUp() {
        super.setUp()
        infoCenter = NowPlayingInfoCenter_Mock()
        nowPlayingController = NowPlayingInfoController(dispatchQueue: MockDispatchQueue(), infoCenter: infoCenter)
    }
    
    override func tearDown() {
        infoCenter = nil
        nowPlayingController = nil
        super.tearDown()
    }
    
    func testInfoDictionaryNotEmpty() {
        nowPlayingController.set(keyValue: MediaItemProperty.title("Some title"))
        XCTAssertGreaterThan(nowPlayingController.info.count, 0)
    }
    
    func testInfoDictionaryEmptyAfterClear() {
        nowPlayingController.set(keyValue: MediaItemProperty.title("Some title"))
        nowPlayingController.clear()
        XCTAssertEqual(nowPlayingController.info.count, 0)
    }
    
    func testInfoCenterNotNil() {
        nowPlayingController.set(keyValue: MediaItemProperty.title("Some title"))
        XCTAssertNotNil(nowPlayingController.infoCenter.nowPlayingInfo)
    }
    
    func testInfoCenterNotEmpty() {
        nowPlayingController.set(keyValue: MediaItemProperty.title("Some title"))
        XCTAssertGreaterThan(nowPlayingController.infoCenter.nowPlayingInfo?.count ?? 0, 0)
    }
    
    func testInfoCenterEmptyAfterClear() {
        nowPlayingController.set(keyValue: MediaItemProperty.title("Some title"))
        nowPlayingController.clear()
        XCTAssertNil(nowPlayingController.infoCenter.nowPlayingInfo)
    }
}
