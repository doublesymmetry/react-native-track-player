import AVFoundation
import XCTest
@testable import SwiftAudioEx

class AVPlayerWrapperTests: XCTestCase {

    var wrapper: AVPlayerWrapper!
    var holder: AVPlayerWrapperDelegateHolder!

    override func setUp() {
        super.setUp()
        wrapper = AVPlayerWrapper()
        wrapper.volume = 0.0
        wrapper.automaticallyWaitsToMinimizeStalling = false
        holder = AVPlayerWrapperDelegateHolder()
        wrapper.delegate = holder
    }

    override func tearDown() {
        wrapper = nil
        holder = nil
        super.tearDown()
    }

    // MARK: - State tests

    func testAVPlayerWrapperStateShouldBeIdle() {
        XCTAssertEqual(wrapper.state, AVPlayerWrapperState.idle)
    }

    func testAVPlayerWrapperStateWhenLoadingSourceShouldBeLoading() {
        wrapper.load(from: Source.url, playWhenReady: false)
        XCTAssertEqual(wrapper.state, AVPlayerWrapperState.loading)
    }

    func testAVPlayerWrapperStateWhenLoadingSourceShouldEventuallyBeReady() {
        let expectation = XCTestExpectation()
        holder.stateUpdate = { state in
            if state == .ready {
                expectation.fulfill()
            }
        }
        wrapper.load(from: Source.url, playWhenReady: false)
        wait(for: [expectation], timeout: 20.0)
    }

    func testAVPlayerWrapperStateWhenPlayingSourceShouldBePlaying() {
        let expectation = XCTestExpectation()
        holder.stateUpdate = { state in
            if state == .playing {
                expectation.fulfill()
            }
        }
        wrapper.load(from: Source.url, playWhenReady: true)
        wait(for: [expectation], timeout: 20.0)
    }

    func testAVPlayerWrapperStateWhenPausingSourceShouldBePaused() {
        let expectation = XCTestExpectation()
        holder.stateUpdate = { state in
            switch state {
            case .playing:
                self.wrapper.pause()
            case .paused:
                expectation.fulfill()
            default:
                break
            }
        }
        wrapper.load(from: Source.url, playWhenReady: true)
        wait(for: [expectation], timeout: 20.0)
    }

    func testAVPlayerWrapperStateWhenTogglingFromPlayShouldBePaused() {
        let expectation = XCTestExpectation()
        holder.stateUpdate = { state in
            switch state {
            case .playing:
                self.wrapper.togglePlaying()
            case .paused:
                expectation.fulfill()
            default:
                break
            }
        }
        wrapper.load(from: Source.url, playWhenReady: true)
        wait(for: [expectation], timeout: 20.0)
    }

    func testAVPlayerWrapperStateWhenStoppingShouldBeStopped() {
        let expectation = XCTestExpectation()
        holder.stateUpdate = { state in
            switch state {
            case .playing:
                self.wrapper.stop()
            case .stopped:
                expectation.fulfill()
            default:
                break
            }
        }
        wrapper.load(from: Source.url, playWhenReady: true)
        wait(for: [expectation], timeout: 20.0)
    }

    func testAVPlayerWrapperStateLoadingWithInitialTimeShouldBePlaying() {
        let expectation = XCTestExpectation()
        holder.stateUpdate = { state in
            switch state {
            case .playing:
                expectation.fulfill()
            default:
                break
            }
        }
        wrapper.load(from: LongSource.url, playWhenReady: true, initialTime: 4.0)
        wait(for: [expectation], timeout: 20.0)
    }

    // MARK: - Duration tests

    func testAVPlayerWrapperDurationShouldBeZero() {
        XCTAssertEqual(wrapper.duration, 0.0)
    }

    func testAVPlayerWrapperDurationLoadingSourceShouldNotBeZero() {
        let expectation = XCTestExpectation()
        holder.stateUpdate = { _ in
            if self.wrapper.duration > 0 {
                expectation.fulfill()
            }
        }
        wrapper.load(from: Source.url, playWhenReady: false)
        wait(for: [expectation], timeout: 20.0)
    }

    // MARK: - Current time tests

    func testAVPlayerWrapperCurrentTimeShouldBeZero() {
        XCTAssertEqual(wrapper.currentTime, 0)
    }

    // MARK: - Seeking

    func testAVPlayerWrapperSeekingShouldSeek() {
        let seekTime: TimeInterval = 5.0
        let expectation = XCTestExpectation()
        holder.stateUpdate = { state in
            self.wrapper.seek(to: seekTime)
        }
        holder.didSeekTo = { seconds in
            expectation.fulfill()
        }
        wrapper.load(from: Source.url, playWhenReady: false)
        wait(for: [expectation], timeout: 20.0)
    }

    func testAVPlayerWrapperSeekingShouldSeekWhileNotYetLoaded() {
        let seekTime: TimeInterval = 5.0
        let expectation = XCTestExpectation()
        holder.didSeekTo = { seconds in
            expectation.fulfill()
        }
        wrapper.load(from: Source.url, playWhenReady: false)
        wrapper.seek(to: seekTime)
        wait(for: [expectation], timeout: 20.0)
    }

    func testAVPlayerWrapperSeekByShouldSeek() {
        let seekTime: TimeInterval = 5.0
        let expectation = XCTestExpectation()
        holder.stateUpdate = { state in
            self.wrapper.seek(by: seekTime)
        }
        holder.didSeekTo = { seconds in
            expectation.fulfill()
        }
        wrapper.load(from: Source.url, playWhenReady: false)
        wait(for: [expectation], timeout: 20.0)
    }

    func testAVPlayerWrapperLoadingSourceWithInitialTimeShouldSeek() {
        let expectation = XCTestExpectation()
        holder.didSeekTo = { seconds in
            expectation.fulfill()
        }
        wrapper.load(from: LongSource.url, playWhenReady: false, initialTime: 4.0)
        wait(for: [expectation], timeout: 20.0)
    }

    // MARK: - Rate tests

    func testAVPlayerWrapperRateShouldBe1() {
        XCTAssertEqual(wrapper.rate, 1)
    }

    func testAVPlayerWrapperRatePlayingSourceShouldBe1() {
        let expectation = XCTestExpectation()
        holder.stateUpdate = { state in
            if self.wrapper.rate == 1.0 {
                expectation.fulfill()
            }
        }
        wrapper.load(from: Source.url, playWhenReady: true)
        wait(for: [expectation], timeout: 20.0)
    }

    func testAVPlayerWrapperTimeObserverWhenUpdatedShouldUpdateTheObserversPeriodicObserverTimeInterval() {
        wrapper.timeEventFrequency = .everySecond
        XCTAssertEqual(wrapper.playerTimeObserver.periodicObserverTimeInterval, TimeEventFrequency.everySecond.getTime())
        wrapper.timeEventFrequency = .everyHalfSecond
        XCTAssertEqual(wrapper.playerTimeObserver.periodicObserverTimeInterval, TimeEventFrequency.everyHalfSecond.getTime())
    }
}

class AVPlayerWrapperDelegateHolder: AVPlayerWrapperDelegate {
    private let lockQueue = DispatchQueue(
        label: "AVPlayerWrapperDelegateHolder.lockQueue",
        target: .global()
    )

    func AVWrapperItemPlaybackStalled() {
    }

    func AVWrapperItemFailedToPlayToEndTime() {
    }

    func AVWrapper(didChangePlayWhenReady playWhenReady: Bool) {
    }

    func AVWrapper(didReceiveTimedMetadata metadata: [AVTimedMetadataGroup]) {
    }

    func AVWrapper(didReceiveCommonMetadata metadata: [AVMetadataItem]) {
    }

    func AVWrapper(didReceiveChapterMetadata metadata: [AVTimedMetadataGroup]) {
    }

    func AVWrapperDidRecreateAVPlayer() {
    }

    func AVWrapperItemDidPlayToEndTime() {
    }

    private var _state: AVPlayerWrapperState? = nil
    var state: AVPlayerWrapperState? {
        get {
            return lockQueue.sync {
                return _state
            }
        }

        set {
            lockQueue.async(flags: .barrier) { [weak self] in
                guard let self = self else { return }
                if let newValue = newValue {
                    let changed = self._state != newValue;
                    if (changed) {
                        self._state = newValue
                        self.stateUpdate?(newValue)
                    }
                }
            }
        }
    }

    var stateUpdate: ((_ state: AVPlayerWrapperState) -> Void)?
    var didUpdateDuration: ((_ duration: Double) -> Void)?
    var didSeekTo: ((_ seconds: Double) -> Void)?
    var itemDidComplete: (() -> Void)?

    func AVWrapper(didChangeState state: AVPlayerWrapperState) {
        self.state = state
    }

    func AVWrapper(secondsElapsed seconds: Double) {
    }

    func AVWrapper(failedWithError error: Error?) {
    }

    func AVWrapper(seekTo seconds: Double, didFinish: Bool) {
        didSeekTo?(seconds)
    }

    func AVWrapper(didUpdateDuration duration: Double) {
        if let state = self.state {
            self.stateUpdate?(state)
        }
        didUpdateDuration?(duration)
    }
}
