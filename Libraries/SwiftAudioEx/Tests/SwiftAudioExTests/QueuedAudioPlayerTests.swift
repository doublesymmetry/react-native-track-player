import XCTest
@testable import SwiftAudioEx

class QueuedAudioPlayerTests: XCTestCase {
    
    var audioPlayer: QueuedAudioPlayer!
    var currentItemEventListener: QueuedAudioPlayer.CurrentItemEventListener!
    var playbackEndEventListener: QueuedAudioPlayer.PlaybackEndEventListener!
    var playerStateEventListener: QueuedAudioPlayer.PlayerStateEventListener!

    override func setUp() {
        super.setUp()
        audioPlayer = QueuedAudioPlayer()

        currentItemEventListener = QueuedAudioPlayer.CurrentItemEventListener()
        audioPlayer.event.currentItem.addListener(
            currentItemEventListener,
            currentItemEventListener.handleEvent
        )

        playbackEndEventListener = QueuedAudioPlayer.PlaybackEndEventListener()
        audioPlayer.event.playbackEnd.addListener(
            playbackEndEventListener,
            playbackEndEventListener.handleEvent
        )

        playerStateEventListener = QueuedAudioPlayer.PlayerStateEventListener()
        audioPlayer.event.stateChange.addListener(
            playerStateEventListener,
            playerStateEventListener.handleEvent
        )

        audioPlayer.volume = 0.0
    }

    override func tearDown() {
        audioPlayer.event.currentItem.removeListener(currentItemEventListener)
        currentItemEventListener = nil
        
        audioPlayer.event.playbackEnd.removeListener(playbackEndEventListener)
        playbackEndEventListener = nil
        
        audioPlayer.event.stateChange.removeListener(playerStateEventListener)
        playerStateEventListener = nil
        
        audioPlayer = nil
        super.tearDown()
    }
    
    // MARK: - Current Item

    func testCurrentItemOnCreate() {
        XCTAssertNil(audioPlayer.currentItem)
    }

    func testAddingOneItem() {
        audioPlayer.add(item: FiveSecondSource.getAudioItem())
        XCTAssertNotNil(audioPlayer.currentItem)
    }
    
    func testLoadItemAfterAdding() {
        testAddingOneItem()
        let item = Source.getAudioItem()
        audioPlayer.load(item: item)

        XCTAssertEqual(audioPlayer.currentItem?.getSourceUrl(), item.getSourceUrl())
    }

    func testRemovingItemAfterAdding() {
        audioPlayer.add(item: FiveSecondSource.getAudioItem())
        audioPlayer.repeatMode = RepeatMode.track
        audioPlayer.play()
        audioPlayer.seek(to: 4)
        try? audioPlayer.removeItem(at: audioPlayer.currentIndex)
        
        XCTAssertNil(audioPlayer.currentItem)
        XCTAssertEqual(audioPlayer.playerState, AudioPlayerState.idle)
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, [.loading, .idle], timeout: 5)
    }
    
    func testLoadAfterRemoval() {
        testRemovingItemAfterAdding()
        
        audioPlayer.load(item: Source.getAudioItem())
        XCTAssertNotEqual(audioPlayer.currentItem?.getSourceUrl(), FiveSecondSource.getAudioItem().getSourceUrl())
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, [.loading, .idle, .loading, .playing], timeout: 5)
        XCTAssertEqual(audioPlayer.playerState, AudioPlayerState.playing)
    }

    func testAddingMultipleItems() {
        audioPlayer.add(items: [FiveSecondSource.getAudioItem(), ShortSource.getAudioItem()], playWhenReady: false)
        XCTAssertNotNil(audioPlayer.currentItem)
        XCTAssertEqual(audioPlayer.currentIndex, 0)
    }
    
    func testRemoveItemAfterAddingMultiple() {
        testAddingMultipleItems()
        
        try? audioPlayer.removeItem(at: 0)
        XCTAssertEqual(audioPlayer.items.count, 1)
        XCTAssertEqual(audioPlayer.currentItem?.getSourceUrl(), ShortSource.getAudioItem().getSourceUrl())
    }
    
    // MARK: - Next Items

    func testNextItemsEmptyOnCreate() {
        XCTAssertTrue(audioPlayer.nextItems.isEmpty)
    }

    func testNextItemsAfterAddingTwoItems() {
        audioPlayer.add(items: [Source.getAudioItem(), Source.getAudioItem()])
        XCTAssertEqual(audioPlayer.nextItems.count, 1)
    }
    
    func testNextItemsWhileNavigationAfterAddingTwoItems() {
        testNextItemsAfterAddingTwoItems()
        
        // Test next
        audioPlayer.next()
        XCTAssertEqual(audioPlayer.nextItems.count, 0)
        
        // Test previous
        audioPlayer.previous()
        XCTAssertEqual(audioPlayer.nextItems.count, 1)
    }

    func testRemovingOneItem() {
        let queue = [Source.getAudioItem(), Source.getAudioItem()]
        audioPlayer.add(items: queue)
        try? audioPlayer.removeItem(at: queue.count - 1)
        XCTAssertEqual(audioPlayer.nextItems.count, queue.count - 2)
    }

    func testJumpingToLastItem() {
        let queue = [Source.getAudioItem(), Source.getAudioItem()]
        audioPlayer.add(items: queue)
        try? audioPlayer.jumpToItem(atIndex: queue.count - 1)
        XCTAssertTrue(audioPlayer.nextItems.isEmpty)
    }

    func testRemovingUpcomingItems() {
        audioPlayer.add(items: [Source.getAudioItem(), Source.getAudioItem()])
        audioPlayer.removeUpcomingItems()
        XCTAssertTrue(audioPlayer.nextItems.isEmpty)
    }

    func testStopping() {
        audioPlayer.add(items: [Source.getAudioItem(), Source.getAudioItem()])
        audioPlayer.stop()
        XCTAssertEqual(audioPlayer.nextItems.count, 1)
    }
    
    // MARK: - Previous Items
    
    func testPreviousItemsEmptyOnCreate() {
        XCTAssertTrue(audioPlayer.previousItems.isEmpty)
    }

    func testPreviousItemsAfterAddingTwoItems() {
        audioPlayer.add(items: [FiveSecondSource.getAudioItem(), FiveSecondSource.getAudioItem()])
        XCTAssertTrue(audioPlayer.previousItems.isEmpty)
    }

    func testPreviousItemsWhileNavigationAfterAddingTwoItems() {
        testPreviousItemsAfterAddingTwoItems()
        
        // Test next
        audioPlayer.next()
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, [.loading, .paused, .loading, .paused], timeout: 5)
        XCTAssertEqual(audioPlayer.previousItems.count, 1)
        waitEqual(self.playbackEndEventListener.lastReason, .skippedToNext, timeout: 5)
        
        // Test stop
        audioPlayer.stop()
        waitEqual(self.audioPlayer.playerState, .stopped, timeout: 5)
        waitEqual(self.playbackEndEventListener.reasons, [.skippedToNext, .playerStopped], timeout: 5)
        
        // Test stop again
        audioPlayer.stop()
        waitEqual(self.audioPlayer.playerState, .stopped, timeout: 5)
        waitEqual(self.playbackEndEventListener.reasons, [.skippedToNext, .playerStopped], timeout: 5)
        
        // Test previous
        audioPlayer.previous()
        waitEqual(self.audioPlayer.playerState, .loading, timeout: 5)
        // should not have emitted playbackEnd .skippedToPrevious because playback was already stopped previously
        waitEqual(self.playbackEndEventListener.reasons, [.skippedToNext, .playerStopped], timeout: 5)
        
    }

    func testRemoveAllPreviousItems() {
        testPreviousItemsAfterAddingTwoItems()
        audioPlayer.removePreviousItems()
        XCTAssertEqual(audioPlayer.previousItems.count, 0)
    }
    
    // MARK: - Pause
    
    func testPauseWithPlayWhenReadyTrue() {
        audioPlayer.playWhenReady = true
        XCTAssertTrue(audioPlayer.playWhenReady)
    }

    func testPauseOnEmptyQueue() {
        audioPlayer.playWhenReady = true
        audioPlayer.pause()
        XCTAssertFalse(audioPlayer.playWhenReady)
        
        // It should not have mutated player state to .paused because playback was already idle
        XCTAssertEqual(playerStateEventListener.states, [])
    }

    func testPauseWithItemAndPausingDirectly() {
        audioPlayer.playWhenReady = true
        
        // Adding an item and pausing directly
        audioPlayer.add(items: [FiveSecondSource.getAudioItem()])
        audioPlayer.pause()
        
        // It should have gone into .paused state from .loading and then into .ready because playback can be started
        waitEqual(self.playerStateEventListener.states, [.loading, .paused, .ready], timeout: 5)
    }

    // MARK: - Stop
    
    func testStopOnEmptyQueue() {
        audioPlayer.stop()
        waitEqual(self.playerStateEventListener.states, [.stopped], timeout: 5)
        
        // It should not have emitted a playbackEnd event
        XCTAssertNil(playbackEndEventListener.lastReason)
    }

    func testStopWithTwoItems() {
        audioPlayer.add(items: [
            FiveSecondSource.getAudioItem(),
            FiveSecondSource.getAudioItem()
        ])
        audioPlayer.stop()
        
        // It should have emitted a playbackEnd .playerStopped event
        waitEqual(self.playbackEndEventListener.lastReason, .playerStopped, timeout: 5)
        
        // It should have mutated player state from .loading to .stopped
        waitEqual(self.playerStateEventListener.states, [.loading, .stopped], timeout: 5)
    }

    // MARK: - Load
    
    func testLoadItemOnEmptyQueue() {
        // Calling load(item) on an empty queue should set currentItem
        audioPlayer.load(item: FiveSecondSource.getAudioItem())
        XCTAssertNotNil(audioPlayer.currentItem)
        
        // It should have started loading, but not playing yet
        waitEqual(self.playerStateEventListener.states, [.loading, .paused, .ready], timeout: 5)
    }

    func testLoadItemAfterPlaying() {
        audioPlayer.play()
        audioPlayer.load(item: FiveSecondSource.getAudioItem())
        XCTAssertNotNil(audioPlayer.currentItem)
        
        // It should have started playing
        waitEqual(self.playerStateEventListener.statesWithoutBuffering, [.loading, .playing], timeout: 5)
        audioPlayer.load(item: Source.getAudioItem())
        
        XCTAssertEqual(audioPlayer.items.count, 1)
        XCTAssertEqual(audioPlayer.currentItem?.getSourceUrl(), Source.getAudioItem().getSourceUrl())
        waitEqual(self.playerStateEventListener.statesWithoutBuffering.prefix(4), [.loading, .playing, .loading, .playing], timeout: 5)
    }
    
    // MARK: - Next
    
    func testNextOnEmptyQueue() {
        audioPlayer.next()
        // should not have emitted a playbackEnd event
        XCTAssertNil(playbackEndEventListener.lastReason)
    }

    func testNextWhenPaused() {
        audioPlayer.add(items: [FiveSecondSource.getAudioItem(), FiveSecondSource.getAudioItem()])
        audioPlayer.next()
        
        waitEqual(self.audioPlayer.nextItems.count, 0, timeout: 5)
        waitEqual(self.audioPlayer.currentIndex, 1, timeout: 5)
        // should go to previous item and not play
        waitEqual(self.audioPlayer.playerState, AudioPlayerState.ready, timeout: 5)
    }

    func testNextWhenPausedWithoutPlaying() {
        audioPlayer.add(items: [FiveSecondSource.getAudioItem(), FiveSecondSource.getAudioItem()])
        audioPlayer.pause()
        audioPlayer.next()
        
        waitEqual(self.audioPlayer.nextItems.count, 0, timeout: 5)
        waitEqual(self.audioPlayer.currentIndex, 1, timeout: 5)
        // should go to previous item and not play
        waitEqual(self.audioPlayer.playerState, AudioPlayerState.ready, timeout: 5)
    }

    func testNextWhenPlaying() {
        audioPlayer.play()
        audioPlayer.add(items: [FiveSecondSource.getAudioItem(), FiveSecondSource.getAudioItem()])
        audioPlayer.next()
        
        waitEqual(self.audioPlayer.nextItems.count, 0, timeout: 5)
        waitEqual(self.audioPlayer.currentIndex, 1, timeout: 5)
        // should go to previous item and play
        waitEqual(self.audioPlayer.playerState, AudioPlayerState.playing, timeout: 5)
    }
    
    // MARK: - Previous
    
    func testPreviousOnEmptyQueue() {
        audioPlayer.previous()
        // should not have emitted a playbackEnd event
        XCTAssertNil(playbackEndEventListener.lastReason)
    }

    func testPreviousWhenPlaying() {
        audioPlayer.add(items: [FiveSecondSource.getAudioItem(), FiveSecondSource.getAudioItem()], playWhenReady: true)
        audioPlayer.next()
        audioPlayer.previous()
        
        waitEqual(self.audioPlayer.nextItems.count, 1, timeout: 5)
        waitEqual(self.audioPlayer.previousItems.count, 0, timeout: 5)
        waitEqual(self.audioPlayer.currentIndex, 0, timeout: 5)
        // should go to previous item and play
        waitEqual(self.audioPlayer.playerState, AudioPlayerState.playing, timeout: 5)
    }

    func testPreviousWhenPaused() {
        audioPlayer.add(items: [FiveSecondSource.getAudioItem(), FiveSecondSource.getAudioItem()])
        audioPlayer.next()
        audioPlayer.pause()
        audioPlayer.previous()
        
        waitEqual(self.audioPlayer.nextItems.count, 1, timeout: 5)
        waitEqual(self.audioPlayer.previousItems.count, 0, timeout: 5)
        waitEqual(self.audioPlayer.currentIndex, 0, timeout: 5)
        // should go to previous item and not play
        waitEqual(self.audioPlayer.playerState, AudioPlayerState.ready, timeout: 5)
    }

    // MARK: - Move
    
    func testMoveItemsRepeatModeOff() {
        audioPlayer.play()
        audioPlayer.add(items: [FiveSecondSource.getAudioItem(), FiveSecondSource.getAudioItem()])
        
        // Move the first (currently playing track) above the second and seek to near the end of the track
        try? audioPlayer.moveItem(fromIndex: 0, toIndex: 1)
        audioPlayer.repeatMode = RepeatMode.off
        waitForSeek(audioPlayer, to: 4.6)

        waitEqual(self.audioPlayer.playerState, AudioPlayerState.ended, timeout: 5)
    }
    
    func testMoveItemsRepeatModeQueue() {
        audioPlayer.play()
        audioPlayer.add(items: [FiveSecondSource.getAudioItem(), FiveSecondSource.getAudioItem()])
        
        // Move the first (currently playing track) above the second and seek to near the end of the track
        try? audioPlayer.moveItem(fromIndex: 0, toIndex: 1)
        audioPlayer.repeatMode = RepeatMode.queue
        waitForSeek(audioPlayer, to: 4.6)
    
        waitEqual(self.audioPlayer.currentIndex, 0, timeout: 5)
        waitTrue(self.audioPlayer.currentTime > 0, timeout: 5)
        waitEqual(self.audioPlayer.playerState, AudioPlayerState.playing, timeout: 5)
    }
    
    func testMoveItemsRepeatModeTrack() {
        audioPlayer.play()
        audioPlayer.add(items: [FiveSecondSource.getAudioItem(), FiveSecondSource.getAudioItem()])
        
        // Move the first (currently playing track) above the second and seek to near the end of the track
        try? audioPlayer.moveItem(fromIndex: 0, toIndex: 1)
        audioPlayer.repeatMode = RepeatMode.track
        waitForSeek(audioPlayer, to: 4.6)
        
        waitTrue(self.audioPlayer.currentTime < 4.6, timeout: 5)
        waitTrue(self.audioPlayer.currentTime > 0, timeout: 5)
        XCTAssertEqual(audioPlayer.currentIndex, 1)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
    }
    
    // MARK: - Repeat Mode (Off - Two Items)
        
    func setupRepeatModeOffTests() {
        audioPlayer.play()
        audioPlayer.add(items: [FiveSecondSource.getAudioItem(), FiveSecondSource.getAudioItem()])
        audioPlayer.repeatMode = .off
    }
    
    func testTrackEndWhenRepeatModeOff() {
        setupRepeatModeOffTests()
        waitForSeek(audioPlayer, to: 4.6)

        waitEqual(self.audioPlayer.nextItems.count, 0, timeout: 5)
        waitEqual(self.audioPlayer.currentIndex, 1, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
        waitEqual(self.currentItemEventListener.lastIndex, 0, timeout: 5)
        
        // Allow final track to end
        waitForSeek(audioPlayer, to: 4.6)
        waitEqual(self.audioPlayer.currentTime, 5, accuracy: 0.1, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .ended, timeout: 5)
        waitEqual(self.currentItemEventListener.index, 1, timeout: 5)
    }
    
    func testNextWhenRepeatModeOff() {
        setupRepeatModeOffTests()
        audioPlayer.play()
        audioPlayer.next()
        
        waitEqual(self.audioPlayer.nextItems.count, 0, timeout: 5)
        waitEqual(self.audioPlayer.currentIndex, 1, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
        waitEqual(self.currentItemEventListener.lastIndex, 0, timeout: 5)
        
        // Calling next on the final track
        audioPlayer.next()
        waitEqual(self.audioPlayer.currentIndex, 1, timeout: 5)
        waitEqual(self.audioPlayer.currentTime, 5, accuracy: 0.1, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .ended, timeout: 5)
    }
    
    // MARK: - Repeat Mode (Track - Two Items)

    func setupRepeatModeTrackTests() {
        audioPlayer.play()
        audioPlayer.add(items: [FiveSecondSource.getAudioItem(), FiveSecondSource.getAudioItem()])
        audioPlayer.repeatMode = .track
    }

    func testRestartTrackWhenRepeatModeTrack() {
        setupRepeatModeTrackTests()
        waitForSeek(audioPlayer, to: 4.6)
        
        waitEqual(self.audioPlayer.currentTime, 0, timeout: 5)
        waitEqual(self.audioPlayer.nextItems.count, 1, timeout: 5)
        waitEqual(self.audioPlayer.currentIndex, 0, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
    }

    func testNextWhenRepeatModeTrack() {
        setupRepeatModeTrackTests()
        audioPlayer.next()
        
        waitEqual(self.audioPlayer.nextItems.count, 0, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
        waitEqual(self.currentItemEventListener.lastIndex, 0, timeout: 5)
    }


    // MARK: - Repeat Mode (Queue - Two Items)

    func setupRepeatModeQueueTests() {
        audioPlayer.play()
        audioPlayer.add(items: [FiveSecondSource.getAudioItem(), FiveSecondSource.getAudioItem()])
        audioPlayer.repeatMode = .queue
    }
    
    func testSeekToEndWhenRepeatModeQueue() {
        setupRepeatModeQueueTests()
        waitForSeek(audioPlayer, to: 4.6)
        
        waitEqual(self.audioPlayer.nextItems.count, 0, timeout: 5)
        waitEqual(self.audioPlayer.currentIndex, 1, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
        waitEqual(self.currentItemEventListener.lastIndex, 0, timeout: 5)
        
        // Allow the final track to end
        waitEqual(self.audioPlayer.currentIndex, 1, timeout: 5)
        waitForSeek(audioPlayer, to: 4.6)
        waitEqual(self.audioPlayer.nextItems.count, 1, timeout: 5)
        waitEqual(self.audioPlayer.currentIndex, 0, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
        waitEqual(self.currentItemEventListener.lastIndex, 1, timeout: 5)
    }

    func testNextWhenRepeatModeQueue() {
        setupRepeatModeQueueTests()
        audioPlayer.next()
        
        waitEqual(self.audioPlayer.nextItems.count, 0, timeout: 5)
        waitEqual(self.audioPlayer.currentIndex, 1, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
        waitEqual(self.currentItemEventListener.lastIndex, 0, timeout: 5)
    }

    func testNextTwiceWhenRepeatModeQueue() {
        setupRepeatModeQueueTests()
        XCTAssertEqual(audioPlayer.currentIndex, 0)
        XCTAssertNil(currentItemEventListener.lastIndex)
        
        audioPlayer.next()
        XCTAssertEqual(audioPlayer.currentIndex, 1)
        waitEqual(self.currentItemEventListener.lastIndex, 0, timeout: 5)
        
        audioPlayer.next()
        XCTAssertEqual(audioPlayer.currentIndex, 0)
        waitEqual(self.currentItemEventListener.lastIndex, 1, timeout: 5)
        waitEqual(self.audioPlayer.nextItems.count, 1, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
    }
    
    // MARK: - Repeat Mode (Off - One Item)
            
    func setupRepeatModeOffOneItemTests() {
        audioPlayer.add(item: FiveSecondSource.getAudioItem(), playWhenReady: true)
        audioPlayer.repeatMode = .off
    }

    func testTrackEndWhenRepeatModeOffOneItem() {
        setupRepeatModeOffOneItemTests()
        waitForSeek(audioPlayer, to: 4.6)

        waitEqual(self.audioPlayer.nextItems.count, 0, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .ended, timeout: 5)
    }

    func testNextWhenRepeatModeOffOneItem() {
        setupRepeatModeOffOneItemTests()
        audioPlayer.next()

        waitEqual(self.audioPlayer.currentIndex, 0, timeout: 5)
        // TODO: Test this more thoroughly?
    }

    // MARK: - Repeat Mode (Track - One Item)

    func setupRepeatModeTrackOneItemTests() {
        audioPlayer.add(item: FiveSecondSource.getAudioItem(), playWhenReady: true)
        audioPlayer.repeatMode = .track
    }

    func testRestartTrackWhenRepeatModeTrackOneItem() {
        setupRepeatModeTrackOneItemTests()
        waitForSeek(audioPlayer, to: 4.6)
        
        waitEqual(self.audioPlayer.currentTime, 0, timeout: 5)
        waitEqual(self.audioPlayer.currentIndex, 0, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
        waitEqual(self.currentItemEventListener.lastIndex, nil, timeout: 5)
    }

    func testNextWhenRepeatModeTrackOneItem() {
        setupRepeatModeTrackOneItemTests()
        audioPlayer.next()
        
        waitEqual(self.audioPlayer.currentTime, 0, timeout: 5)
        waitEqual(self.audioPlayer.nextItems.count, 0, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
    }

    // MARK: - Repeat Mode (Queue - One Item)

    func setupRepeatModeQueueOneItemTests() {
        audioPlayer.add(item: FiveSecondSource.getAudioItem(), playWhenReady: true)
        audioPlayer.repeatMode = .queue
    }

    func testSeekToEndWhenRepeatModeQueueOneItem() {
        setupRepeatModeQueueOneItemTests()
        waitForSeek(audioPlayer, to: 4.6)
        
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
        waitTrue(self.audioPlayer.currentTime > 4.5, timeout: 5)
        waitTrue(self.audioPlayer.currentTime < 1, timeout: 5)
        waitEqual(self.audioPlayer.currentIndex, 0, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
    }

    func testNextWhenRepeatModeQueueOneItem() {
        setupRepeatModeQueueOneItemTests()
        waitForSeek(audioPlayer, to: 2)
        audioPlayer.next()
        
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
        waitTrue(self.audioPlayer.currentTime < 1.9, timeout: 5)
        waitEqual(self.audioPlayer.currentIndex, 0, timeout: 5)
        waitEqual(self.audioPlayer.playerState, .playing, timeout: 5)
    }
}

extension QueuedAudioPlayer {

    class SeekEventListener {
        private let lockQueue = DispatchQueue(
            label: "SeekEventListener.lockQueue",
            target: .global()
        )
        var _eventResult: (Double, Bool) = (-1, false)
        var eventResult: (Double, Bool) {
            get {
                return lockQueue.sync {
                    _eventResult
                }
            }
        }
        func handleEvent(seconds: Double, didFinish: Bool) {
            lockQueue.sync {
                _eventResult = (seconds, didFinish)
            }
        }
    }

    class CurrentItemEventListener {
        private let lockQueue = DispatchQueue(
            label: "CurrentItemEventListener.lockQueue",
            target: .global()
        )
        var _item: AudioItem? = nil
        var _index: Int? = nil
        var _lastItem: AudioItem? = nil
        var _lastIndex: Int? = nil
        var _lastPosition: Double? = nil

        var item: AudioItem? {
            get {
                return lockQueue.sync {
                    return _item
                }
            }
        }
        var index: Int? {
            return lockQueue.sync {
                return _index
            }
        }
        var lastItem: AudioItem? {
            return lockQueue.sync {
                return _lastItem
            }
        }
        var lastIndex: Int? {
            return lockQueue.sync {
                return _lastIndex
            }
        }
        var lastPosition: Double? {
            return lockQueue.sync {
                return _lastPosition
            }
        }


        func handleEvent(
            item: AudioItem?,
            index: Int?,
            lastItem: AudioItem?,
            lastIndex: Int?,
            lastPosition: Double?
        ) {
            lockQueue.sync {
                _item = item
                _index = index
                _lastItem = lastItem
                _lastIndex = lastIndex
                _lastPosition = lastPosition
            }
        }
    }
    
    class PlaybackEndEventListener {
        private let lockQueue = DispatchQueue(
            label: "PlaybackEndEventListener.lockQueue",
            target: .global()
        )
        var _lastReason: PlaybackEndedReason? = nil
        var lastReason: PlaybackEndedReason? {
            get {
                return lockQueue.sync {
                    return _lastReason
                }
            }
        }
        var _reasons: [PlaybackEndedReason] = []
        var reasons: [PlaybackEndedReason] {
            get {
                return lockQueue.sync {
                    return _reasons
                }
            }
        }

        func handleEvent(reason: PlaybackEndedReason) {
            lockQueue.sync {
                _lastReason = reason
                _reasons.append(reason)
            }
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
}
