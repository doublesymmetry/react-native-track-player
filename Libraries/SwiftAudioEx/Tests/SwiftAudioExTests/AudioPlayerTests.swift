import XCTest
@testable import SwiftAudioEx

class AudioPlayerTests: XCTestCase {
    
    var audioPlayer: AudioPlayer!
    var listener: AudioPlayerEventListener!
    var playerStateEventListener: PlayerStateEventListener!

    override func setUp() {
        super.setUp()
        audioPlayer = AudioPlayer()
        audioPlayer.volume = 0.0
        listener = AudioPlayerEventListener(audioPlayer: audioPlayer)
        playerStateEventListener = PlayerStateEventListener()
        audioPlayer.event.stateChange.addListener(playerStateEventListener, playerStateEventListener.handleEvent)
    }

    override func tearDown() {
        audioPlayer = nil
        listener = nil
        super.tearDown()
    }

    // MARK: - Load

    func testLoadAudioItemNeverMutatesPlayWhenReadyToFalse() {
        audioPlayer.playWhenReady = true
        audioPlayer.load(item: Source.getAudioItem())
        XCTAssertTrue(audioPlayer.playWhenReady)
    }

    func testLoadAudioItemNeverMutatesPlayWhenReadyToTrue() {
        audioPlayer.playWhenReady = false
        audioPlayer.load(item: Source.getAudioItem())
        XCTAssertFalse(audioPlayer.playWhenReady)
    }

    func testLoadAudioItemMutatesPlayWhenReadyToFalse() {
        audioPlayer.playWhenReady = true
        XCTAssertTrue(audioPlayer.playWhenReady)
        audioPlayer.load(item: Source.getAudioItem(), playWhenReady: false)
        XCTAssertFalse(audioPlayer.playWhenReady)
    }

    func testLoadAudioItemMutatesPlayWhenReadyToTrue() {
        audioPlayer.playWhenReady = false
        audioPlayer.load(item: Source.getAudioItem(), playWhenReady: true)
        XCTAssertTrue(audioPlayer.playWhenReady)
    }

    func testLoadAudioItemSeeksWhenInitialTimeIsSet() {
        let expectation = XCTestExpectation(description: "Seek completion")
        
        var seekCompleted = false
        listener.onSeekCompletion = {
            seekCompleted = true
            expectation.fulfill()
        }
        
        audioPlayer.playWhenReady = false
        XCTAssertFalse(audioPlayer.playWhenReady)
        audioPlayer.load(item: FiveSecondSourceWithInitialTimeOfFourSeconds.getAudioItem())
        
        wait(for: [expectation], timeout: 5)
        
        XCTAssertTrue(seekCompleted)
        XCTAssertTrue(audioPlayer.currentTime >= 4)
    }
    
    // MARK: - Duration

    func testSetDurationAfterLoading() {
        audioPlayer.load(item: FiveSecondSource.getAudioItem())
        waitEqual(self.audioPlayer.duration, 5, accuracy: 0.1, timeout: 5)
    }

    func testOnUpdateDurationReceivedAfterLoading() {
        let expectation = XCTestExpectation(description: "Update duration received")
        
        var receivedUpdateDuration = false
        listener.onUpdateDuration = { duration in
            receivedUpdateDuration = true
            XCTAssertEqual(duration, 5, accuracy: 0.1)
            expectation.fulfill()
        }
        
        audioPlayer.load(item: FiveSecondSource.getAudioItem())
        
        wait(for: [expectation], timeout: 5) // Adjust the timeout as needed
        
        XCTAssertTrue(receivedUpdateDuration)
    }

    func testResetDurationAfterLoadingAgain() {
        audioPlayer.load(item: FiveSecondSource.getAudioItem())
        XCTAssertEqual(audioPlayer.duration, 0)
        waitEqual(self.audioPlayer.duration, 5, accuracy: 0.1, timeout: 5)

        audioPlayer.load(item: FiveSecondSource.getAudioItem())
        XCTAssertEqual(audioPlayer.duration, 0)
        waitEqual(self.audioPlayer.duration, 5, accuracy: 0.1, timeout: 5)
    }

    func testResetDurationAfterReset() {
        audioPlayer.load(item: FiveSecondSource.getAudioItem())
        XCTAssertEqual(audioPlayer.duration, 0)
        waitEqual(self.audioPlayer.duration, 5, accuracy: 0.1, timeout: 5)
        audioPlayer.clear()
        XCTAssertEqual(audioPlayer.duration, 0)
    }
    
    // MARK: - Failure
    
    func testFailEventOnLoadWithNonMalformedURL() {
        let expectation = XCTestExpectation(description: "Fail event received on load with non-malformed URL")
        
        var didReceiveFail = false
        listener.onReceiveFail = { error in
            didReceiveFail = true
            expectation.fulfill()
        }
        
        let item = DefaultAudioItem(
            audioUrl: "", // malformed url
            artist: "Artist",
            title: "Title",
            albumTitle: "AlbumTitle",
            sourceType: .stream
        )
        audioPlayer.load(item: item, playWhenReady: true)
        
        wait(for: [expectation], timeout: 5) // Adjust the timeout as needed
        
        XCTAssertNotNil(audioPlayer.playbackError)
        XCTAssertEqual(audioPlayer.playerState, .failed)
        XCTAssertTrue(didReceiveFail)
    }

    func testFailEventOnLoadWithNonExistingResource() {
        let expectation = XCTestExpectation(description: "Fail event received on load with non-existing resource")
        
        var didReceiveFail = false
        listener.onReceiveFail = { error in
            didReceiveFail = true
            expectation.fulfill()
        }
        
        let nonExistingUrl = "https://\(String.random(length: 100)).com/\(String.random(length: 100)).mp3"
        let item = DefaultAudioItem(audioUrl: nonExistingUrl, artist: "Artist", title: "Title", albumTitle: "AlbumTitle", sourceType: .stream)
        audioPlayer.load(item: item, playWhenReady: true)
        
        wait(for: [expectation], timeout: 10) // Adjust the timeout as needed
        
        XCTAssertNotNil(audioPlayer.playbackError)
        XCTAssertEqual(audioPlayer.playerState, .failed)
        XCTAssertTrue(didReceiveFail)
    }

    func testRetryLoadingAfterFailure() {
        let nonExistingUrl = "https://\(String.random(length: 100)).com/\(String.random(length: 100)).mp3"
        let item = DefaultAudioItem(
            audioUrl: nonExistingUrl,
            artist: "Artist",
            title: "Title",
            albumTitle: "AlbumTitle",
            sourceType: .stream
        )
        
        audioPlayer.load(item: item, playWhenReady: true)
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, [.loading, .failed], timeout: 5)
        
        audioPlayer.play()
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, [.loading, .failed, .loading, .failed], timeout: 5)
    }

    func testRetryLoadingAfterFailureWithPlayWhenReady() {
        let nonExistingUrl = "https://\(String.random(length: 100)).com/\(String.random(length: 100)).mp3"
        let item = DefaultAudioItem(
            audioUrl: nonExistingUrl,
            artist: "Artist",
            title: "Title",
            albumTitle: "AlbumTitle",
            sourceType: .stream
        )
        
        audioPlayer.load(item: item, playWhenReady: true)
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, [.loading, .failed], timeout: 5)
        
        audioPlayer.playWhenReady = true
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, [.loading, .failed, .loading, .failed], timeout: 5)
    }

    func testRetryLoadingAfterFailureWithReload() {
        let nonExistingUrl = "https://\(String.random(length: 100)).com/\(String.random(length: 100)).mp3"
        let item = DefaultAudioItem(
            audioUrl: nonExistingUrl,
            artist: "Artist",
            title: "Title",
            albumTitle: "AlbumTitle",
            sourceType: .stream
        )
        
        audioPlayer.load(item: item, playWhenReady: true)
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, [.loading, .failed], timeout: 5)
        
        audioPlayer.reload(startFromCurrentTime: true)
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, [.loading, .failed, .loading, .failed], timeout: 5)
    }

    func testLoadResourceSucceedsAfterPreviousFailure() {
        var didReceiveFail = false
        listener.onReceiveFail = { error in
            didReceiveFail = true
        }
        
        let nonExistingUrl = "https://\(String.random(length: 100)).com/\(String.random(length: 100)).mp3"
        let failItem = DefaultAudioItem(audioUrl: nonExistingUrl, artist: "Artist", title: "Title", albumTitle: "AlbumTitle", sourceType: .stream)
        
        audioPlayer.load(item: failItem, playWhenReady: false)
        waitTrue(didReceiveFail, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .failed, timeout: 5)
        waitEqual(self.playerStateEventListener.states, [.loading, .failed], timeout: 5)
        
        self.audioPlayer.load(item: Source.getAudioItem(), playWhenReady: true)
        waitTrue(self.audioPlayer.playbackError == nil, timeout: 5)
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, [.loading, .failed, .loading, .playing], timeout: 5)
    }

    func testLoadResourceSucceedsAfterPreviousFailureWithPlayWhenReady() {
        var didReceiveFail = false
        listener.onReceiveFail = { error in
            didReceiveFail = true
        }
        
        let nonExistingUrl = "https://\(String.random(length: 100)).com/\(String.random(length: 100)).mp3"
        let item = DefaultAudioItem(audioUrl: nonExistingUrl, artist: "Artist", title: "Title", albumTitle: "AlbumTitle", sourceType: .stream)
        
        audioPlayer.load(item: item, playWhenReady: true)
        waitTrue(didReceiveFail, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .failed, timeout: 5)
        
        self.audioPlayer.load(item: Source.getAudioItem(), playWhenReady: true)
        waitTrue(self.audioPlayer.playbackError == nil, timeout: 5)
    }
    
    // MARK: - States
    
    func testInitialStateIsIdle() {
        XCTAssertEqual(audioPlayer.playerState, .idle)
    }

    func testLoadingStateAfterLoadSource() {
        audioPlayer.load(item: Source.getAudioItem(), playWhenReady: false)
        XCTAssertEqual(audioPlayer.playerState, .loading)
    }

    func testReadyStateAfterLoadSource() {
        audioPlayer.load(item: Source.getAudioItem(), playWhenReady: false)
        waitEqual(self.audioPlayer.playerState, .ready, timeout: 5)
    }

    func testPlayingStateAfterLoadSourceWithPlayWhenReady() {
        audioPlayer.load(item: Source.getAudioItem(), playWhenReady: true)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
    }

    func testReliableOrderOfEvents() {
        audioPlayer.load(item: Source.getAudioItem(), playWhenReady: true)
        var expectedEvents: [AVPlayerWrapperState] = [.loading, .playing]
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, expectedEvents, timeout: 5)
        
        audioPlayer.pause()
        expectedEvents.append(.paused)
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, expectedEvents, timeout: 5)
        
        audioPlayer.play()
        expectedEvents.append(.playing)
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, expectedEvents, timeout: 5)
        
        audioPlayer.clear()
        expectedEvents.append(.idle)
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, expectedEvents, timeout: 5)
    }

    func testUpdatePlayWhenReadyAfterExternalPause() {
        audioPlayer.load(item: Source.getAudioItem(), playWhenReady: true)
        var expectedEvents: [AVPlayerWrapperState] = [.loading, .playing]
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, expectedEvents, timeout: 5)
        waitTrue(self.audioPlayer.currentTime > 0, timeout: 5)
        
        // Simulate AVPlayer becoming paused due to external reason:
        audioPlayer.wrapper.rate = 0
        expectedEvents.append(.paused)
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, expectedEvents, timeout: 5)
        XCTAssertFalse(self.audioPlayer.playWhenReady)
    }

    func testReliableOrderOfEventsAtEndCallStop() {
        audioPlayer.load(item: Source.getAudioItem(), playWhenReady: true)
        var expectedEvents: [AVPlayerWrapperState] = [.loading, .playing]
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, expectedEvents, timeout: 5)
        
        audioPlayer.pause()
        expectedEvents.append(.paused)
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, expectedEvents, timeout: 5)
        
        expectedEvents.append(.playing)
        audioPlayer.play()
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, expectedEvents, timeout: 5)
        
        audioPlayer.stop()
        expectedEvents.append(.stopped)
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, expectedEvents, timeout: 5)
    }

    func testReliableOrderOfEventsAfterLoadingAfterReset() {
        audioPlayer.load(item: Source.getAudioItem(), playWhenReady: true)
        var expectedEvents: [AVPlayerWrapperState] = [.loading, .playing]
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, expectedEvents, timeout: 5)
        
        audioPlayer.clear()
        expectedEvents.append(.idle)
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, expectedEvents, timeout: 5)
        
        audioPlayer.load(item: Source.getAudioItem())
        expectedEvents.append(contentsOf: [.loading, .playing])
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, expectedEvents, timeout: 5)
    }

    func testPlayingStateAfterPlay() {
        audioPlayer.load(item: Source.getAudioItem(), playWhenReady: false)
        waitEqual(self.audioPlayer.playerState, .ready, timeout: 5)
        
        audioPlayer.play()
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
    }

    func testPausedStateAfterPause() {
        audioPlayer.load(item: Source.getAudioItem(), playWhenReady: true)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
        
        audioPlayer.pause()
        waitEqual(self.audioPlayer.playerState, .paused, timeout: 5)
    }

    func testPausedStateAfterSettingPlayWhenReadyToFalse() {
        audioPlayer.load(item: Source.getAudioItem(), playWhenReady: true)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
        
        audioPlayer.playWhenReady = false
        waitEqual(self.audioPlayer.playerState, .paused, timeout: 5)
    }

    func testPlayingStateAfterSettingPlayWhenReadyToTrue() {
        audioPlayer.load(item: Source.getAudioItem(), playWhenReady: false)
        waitEqual(self.audioPlayer.playerState, .ready, timeout: 5)
        
        audioPlayer.playWhenReady = true
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
    }

    func testStoppedStateAfterStop() {
        audioPlayer.load(item: Source.getAudioItem(), playWhenReady: true)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
        
        audioPlayer.stop()
        waitEqual(self.audioPlayer.playerState, .stopped, timeout: 5)
    }
    
    // MARK: - State (Current Time)
    
    func testInitialCurrentTime() {
        XCTAssertEqual(audioPlayer.currentTime, 0.0)
    }
    
    func testSecondsElapseEventEmittedWhenPlaying() {
        var onSecondsElapseTime = 0.0
        
        audioPlayer.timeEventFrequency = .everyQuarterSecond
        listener.onSecondsElapse = { time in
            onSecondsElapseTime = time
        }
        
        audioPlayer.load(item: LongSource.getAudioItem(), playWhenReady: true)
        waitTrue(onSecondsElapseTime > 0, timeout: 5)
    }
    
    // MARK: - Buffer
    
    func testAutomaticallyWaitsToMinimizeStalling() {
        XCTAssertTrue(audioPlayer.automaticallyWaitsToMinimizeStalling)
    }

    func testBufferDurationZero() {
        XCTAssertEqual(audioPlayer.bufferDuration, 0)
    }

    func testBufferDurationDisablesAutomaticallyWaitsToMinimizeStalling() {
        audioPlayer.bufferDuration = 1
        XCTAssertEqual(audioPlayer.bufferDuration, 1)
        XCTAssertFalse(audioPlayer.automaticallyWaitsToMinimizeStalling)
    }

    func testEnablingAutomaticallyWaitsToMinimizeStallingSetsBufferDurationToZero() {
        audioPlayer.automaticallyWaitsToMinimizeStalling = true
        XCTAssertEqual(audioPlayer.bufferDuration, 0)
    }
    
    // MARK: - Seek
    
    func testSeekingBeforeLoadingComplete() {
        audioPlayer.load(item: FiveSecondSource.getAudioItem(), playWhenReady: true)
        XCTAssertTrue(audioPlayer.playerState == .loading)
        audioPlayer.seek(to: 4.75)
        waitTrue(self.audioPlayer.currentTime > 4.75, timeout: 5)
    }

    func testSeekingAfterLoadingComplete() {
        audioPlayer.load(item: FiveSecondSource.getAudioItem(), playWhenReady: true)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
        audioPlayer.seek(to: 4.75)
        waitTrue(self.audioPlayer.currentTime > 4.75, timeout: 5)
    }

    func testSeekingWhenPaused() {
        audioPlayer.load(item: FiveSecondSource.getAudioItem(), playWhenReady: false)
        audioPlayer.seek(to: 4.75)
        waitEqual(self.audioPlayer.currentTime, 4.75, timeout: 5)
    }

    func testSeekingWhenStopped() {
        audioPlayer.load(item: FiveSecondSource.getAudioItem(), playWhenReady: false)
        audioPlayer.play()
        waitForSeek(audioPlayer, to: 2)
        audioPlayer.stop()
        audioPlayer.seek(to: 4.75)
        waitEqual(self.audioPlayer.currentTime, 0, timeout: 5)
    }
    
    // MARK: - Rate
    
    func testRateInitially() {
        XCTAssertEqual(audioPlayer.rate, 1)
    }

    func testSpeedUpPlayback() {
        var start: Date? = nil
        var end: Date? = nil

        listener.onPlaybackEnd = { reason in
            if reason == .playedUntilEnd {
                end = Date()
            }
        }

        listener.onStateChange = { state in
            switch state {
            case .playing:
                if start == nil {
                    start = Date()
                }
            default: break
            }
        }

        audioPlayer.load(item: FiveSecondSource.getAudioItem(), playWhenReady: true)
        audioPlayer.rate = 10
        waitEqual(self.audioPlayer.playerState, .ended, timeout: 5)
        
        if let start = start, let end = end {
            let duration = end.timeIntervalSince(start)
            XCTAssertLessThan(duration, 1, "Duration should be less than 1 second")
        }
    }

    func testSlowDownPlayback() {
        var start: Date? = nil
        var end: Date? = nil

        listener.onPlaybackEnd = { reason in
            if reason == .playedUntilEnd {
                end = Date()
            }
        }

        audioPlayer.rate = 0.5
        audioPlayer.load(item: FiveSecondSource.getAudioItem(), playWhenReady: true)

        listener.onStateChange = { state in
            switch state {
            case .playing:
                if start == nil {
                    start = Date()
                }
            default: break
            }
        }

        audioPlayer.seek(to: 4.75)
        waitEqual(self.audioPlayer.playerState, .ended, timeout: 5)
        
        if let start = start, let end = end {
            let duration = end.timeIntervalSince(start)
            XCTAssertLessThanOrEqual(duration, 1, "Duration should be less than or equal to 1 second")
        }
    }
    
    // MARK: - Current Item

    func testCurrentItemInitially() {
        XCTAssertNil(audioPlayer.currentItem, "Current item should be nil initially")
    }

    func testCurrentItemAfterLoading() {
        audioPlayer.load(item: Source.getAudioItem(), playWhenReady: false)
        XCTAssertEqual(audioPlayer.currentItem?.getSourceUrl(), Source.getAudioItem().getSourceUrl(), "Current item should not be nil after loading")
    }
}

class PlayerStateEventListener {
    private let lockQueue = DispatchQueue(
        label: "PlayerStateEventListener.lockQueue",
        target: .global()
    )
    var _states: [AudioPlayerState] = []
    var states: [AudioPlayerState] {
        get {
            return lockQueue.sync {
                return _states
            }
        }

        set {
            lockQueue.sync {
                _states = newValue
            }
        }
    }
    private var _statesWithoutBuffering: [AudioPlayerState] = []
    var statesWithoutBuffering: [AudioPlayerState] {
        get {
            return lockQueue.sync {
                return _statesWithoutBuffering
            }
        }

        set {
            lockQueue.sync {
                _statesWithoutBuffering = newValue
            }
        }
    }
    func handleEvent(state: AudioPlayerState) {
        states.append(state)
        if (state != .ready && state != .buffering && (statesWithoutBuffering.isEmpty || statesWithoutBuffering.last != state)) {
            statesWithoutBuffering.append(state)
        }
    }
}

class AudioPlayerEventListener {

    var state: AudioPlayerState?

    var onStateChange: ((_ state: AudioPlayerState) -> Void)?
    var onSecondsElapse: ((_ seconds: TimeInterval) -> Void)?
    var onSeekCompletion: (() -> Void)?
    var onReceiveFail: ((_ error: Error?) -> Void)?
    var onPlaybackEnd: ((_: AudioPlayer.PlaybackEndEventData) -> Void)?
    var onUpdateDuration: ((_: AudioPlayer.UpdateDurationEventData) -> Void)?

    weak var audioPlayer: AudioPlayer?

    init(audioPlayer: AudioPlayer) {
        audioPlayer.event.updateDuration.addListener(self, handleUpdateDuration)
        audioPlayer.event.stateChange.addListener(self, handleStateChange)
        audioPlayer.event.seek.addListener(self, handleSeek)
        audioPlayer.event.secondElapse.addListener(self, handleSecondsElapse)
        audioPlayer.event.fail.addListener(self, handleFail)
        audioPlayer.event.playbackEnd.addListener(self, handlePlaybackEnd)
    }

    deinit {
        audioPlayer?.event.stateChange.removeListener(self)
        audioPlayer?.event.seek.removeListener(self)
        audioPlayer?.event.secondElapse.removeListener(self)
    }

    func handleStateChange(state: AudioPlayerState) {
        self.state = state
        onStateChange?(state)
    }

    func handleSeek(data: AudioPlayer.SeekEventData) {
        onSeekCompletion?()
    }

    func handleSecondsElapse(data: AudioPlayer.SecondElapseEventData) {
        self.onSecondsElapse?(data)
    }

    func handleFail(error: Error?) {
        self.onReceiveFail?(error)
    }

    func handlePlaybackEnd(_ data: AudioPlayer.PlaybackEndEventData) {
        self.onPlaybackEnd?(data)
    }

    func handleUpdateDuration(_ data: AudioPlayer.UpdateDurationEventData) {
        self.onUpdateDuration?(data)
    }
}

extension String {
    static func random(length: Int = 20) -> String {
        let base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var randomString: String = ""

        for _ in 0..<length {
            let randomValue = arc4random_uniform(UInt32(base.count))
            randomString += "\(base[base.index(base.startIndex, offsetBy: Int(randomValue))])"
        }
        return randomString
    }
}
