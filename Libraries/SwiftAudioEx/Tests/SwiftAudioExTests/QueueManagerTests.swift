import XCTest
@testable import SwiftAudioEx

class QueueManagerTests: XCTestCase {
    
    let dummyItem = 0
    let items: [Int] = [0, 1, 2]
    var queue: QueueManager<Int>!
    
    override func setUp() {
        super.setUp()
        queue = QueueManager()
    }
    
    override func tearDown() {
        queue = nil
        super.tearDown()
    }
    
    // MARK: - Current Item
    
    func testCurrentItemOnStart() {
        XCTAssertNil(queue.current)
    }
    
    func testOneItemAdded() {
        queue.add(dummyItem)
        XCTAssertNil(queue.current)
    }
    
    func testOneItemAddedAndJumping() {
        queue.add(dummyItem)
        try! queue.jump(to: 0)
    }
    
    func testOneItemAddedAndJumpingAndReplacing() {
        queue.add(self.dummyItem)
        try! queue.jump(to: 0)
        queue.replaceCurrentItem(with: 1)
        XCTAssertEqual(queue.current, 1)
    }
    
    func testReplacingCurrentItemWithEmptyQueue() {
        queue.replaceCurrentItem(with: 1)
        XCTAssertNotNil(queue.current)
    }
    
    func testAddingItemsAndJumpingToLast() {
        queue.add(self.items)
        try! queue.jump(to: queue.items.count - 1)
        XCTAssertNotNil(queue.current)
    }
    
    // MARK: - Adding At Index
    
    func testAddingItemAtIndexZeroWhenQueueIsEmpty() {
        try! queue.add([3], at: 0)
        XCTAssertEqual(queue.items.first, 3)
        XCTAssertNil(queue.current)
        XCTAssertEqual(queue.currentIndex, -1)
    }
    
    func testAddingItemAtIndexAndJumpingToFirstItem() {
        queue.add([1, 2])
        try! queue.jump(to: 0)
        XCTAssertEqual(queue.items.last, 2)
    }
    
    func testAddingItemAtCurrentElementCount() {
        queue.add([1, 2])
        try! queue.jump(to: 0)
        try! queue.add([3, 4, 5], at: queue.items.count)
        XCTAssertEqual(queue.items.last, 5)
    }
    
    func testAddingItemBeforeTheFirstItem() {
        queue.add([1, 2])
        try! queue.jump(to: 0)
        try! queue.add([-1], at: 0)
        XCTAssertEqual(queue.items.first, -1)
    }
    
    func testAddingItemAfterTheLastItem() {
        queue.add([1, 2])
        try! queue.jump(to: 0)
        try! queue.add([6], at: queue.items.count)
        XCTAssertEqual(queue.items.last, 6)
    }
    
    func testAddingItemAtCurrentIndex() {
        queue.add([1, 2])
        try! queue.jump(to: 0)
        
        queue.next()
        try! queue.add([5], at: queue.currentIndex)
        
        XCTAssertEqual(queue.current, 2)
        XCTAssertEqual(queue.currentIndex, 2)
    }
    
    // MARK: - Add Item (No Jump)
    
    func testAddOneItemWithoutJumping() {
        queue.add(0)
        XCTAssertEqual(queue.items.count, 1)
    }
    
    func testReplaceItem() {
        queue.add(0)
        queue.replaceCurrentItem(with: 1)
        XCTAssertEqual(queue.items.count, 2)
        XCTAssertEqual(queue.current, 1)
        XCTAssertEqual(queue.currentIndex, 1)
    }
    
    func testCallingNextAfterReplacement() {
        queue.add(0)
        queue.replaceCurrentItem(with: 1)
        let item = queue.next()
        XCTAssertEqual(item, 1)
    }
    
    func testCallingPreviousAfterReplacement() {
        queue.add(0)
        queue.replaceCurrentItem(with: 1)
        let item = queue.previous()
        XCTAssertEqual(item, 0)
    }
    
    func testCallingNext() {
        queue.add(0)
        queue.next()
        let item = queue.next()
        XCTAssertNil(item)
    }
    
    func testCallingPrevious() {
        queue.add(0)
        queue.previous()
        let item = queue.previous()
        XCTAssertNil(item)
    }
    
    func testJumpToZeroAndCallNextWithWrap() {
        queue.add(0)
        try! queue.jump(to: 0)
        let nextIndex = queue.next(wrap: true)
        XCTAssertEqual(nextIndex, 0)
    }
    
    func testJumpToZeroAndCallPreviousWithWrap() {
        queue.add(0)
        try! queue.jump(to: 0)
        let previousIndex = queue.previous(wrap: true)
        XCTAssertEqual(previousIndex, 0)
    }
    
    // MARK: - Adding Multiple Items
    
    func testAddMultipleItems() {
        queue.add(items)
        XCTAssertEqual(queue.items.count, items.count)
        XCTAssertNil(queue.current)
        XCTAssertEqual(queue.nextItems.count, 0)
    }
    
    func testQueueNavigation() {
        queue.add(items)
        try! queue.jump(to: 0)
        let nextItem = queue.next()
        
        XCTAssertEqual(nextItem, items[1])
        XCTAssertEqual(queue.current, items[1])
        XCTAssertNotNil(queue.previousItems)
        
        // Previous
        XCTAssertEqual(queue.previous(), 0)
        XCTAssertEqual(queue.current, items.first)
        
        // Previous at start of queue
        XCTAssertEqual(queue.previous(), 0)
        
        // Previous at start of queue with wrap
        let index3 = queue.previous(wrap: true)
        XCTAssertEqual(index3, items.count - 1)
        XCTAssertEqual(queue.currentIndex, items.count - 1)
        XCTAssertEqual(queue.current, items.last)
        
        // Next at end of queue
        let index4 = queue.next()
        XCTAssertEqual(index4, items.count - 1)
        
        // Next at end of queue with wrap
        let index5 = queue.next(wrap: true)
        XCTAssertEqual(index5, 0)
        XCTAssertEqual(queue.currentIndex, 0)
        XCTAssertEqual(queue.current, items.first)
    }
    
    func testRemovePreviousItemsAfterNext() {
        queue.add(items)
        try! queue.jump(to: 0)
        queue.next()
        queue.removePreviousItems()
        
        XCTAssertEqual(queue.previousItems.count, 0)
        XCTAssertEqual(queue.currentIndex, 0)
    }
    
    func testAddMoreItems() {
        queue.add(items)
        let initialItemCount = queue.items.count
        try? queue.add([10, 11, 12, 13], at: queue.items.endIndex - 1)
        XCTAssertEqual(queue.items.count, initialItemCount + 4)
    }
    
    func testAddMoreItemsAtSmallerIndex() {
        queue.add(items)
        try! queue.jump(to: 0)
        let initialCurrentIndex = queue.currentIndex
        try? queue.add([10, 11, 12, 13], at: initialCurrentIndex)
        XCTAssertEqual(queue.currentIndex, initialCurrentIndex + 4)
    }
    
    // MARK: - Remove
    
    func testRemoveItemAtIndexLessThanCurrent() {
        queue.add(items)
        try! queue.jump(to: 1)
        
        let initialCurrentIndex = queue.currentIndex
        let removed = try? queue.removeItem(at: initialCurrentIndex - 1)
        
        XCTAssertEqual(removed, 0)
        XCTAssertEqual(initialCurrentIndex, 1)
        XCTAssertEqual(queue.currentIndex, 0)
    }
    
    func testRemoveSecondItem() {
        queue.add(items)
        let removed = try? queue.removeItem(at: 1)
        XCTAssertNotNil(removed)
        XCTAssertEqual(queue.items.count, items.count - 1)
    }
    
    func testRemoveLastItem() {
        queue.add(items)
        let removed = try? queue.removeItem(at: items.count - 1)
        XCTAssertNotNil(removed)
        XCTAssertEqual(queue.items.count, items.count - 1)
    }
    
    func testRemoveCurrentItemWhenFirstItem() {
        queue.add(items)
        try! queue.jump(to: 0)
        let removed = try? queue.removeItem(at: queue.currentIndex)
        
        XCTAssertNotNil(removed)
        XCTAssertEqual(queue.items.count, items.count - 1)
        XCTAssertEqual(queue.currentIndex, 0)
        XCTAssertEqual(queue.current, 1)
    }
    
    func testRemoveCurrentItemWhenLastItem() {
        queue.add(items)
        try! queue.jump(to: items.count - 1)
        let removed = try? queue.removeItem(at: queue.currentIndex)
        
        XCTAssertNotNil(removed)
        XCTAssertEqual(queue.items.count, items.count - 1)
        XCTAssertEqual(queue.currentIndex, 0)
    }
    
    func testRemoveWithTooLargeIndex() {
        queue.add(items)
        let removed = try? queue.removeItem(at: items.count)
        XCTAssertNil(removed)
        XCTAssertEqual(queue.items.count, items.count)
    }
    
    func testRemoveWithTooSmallIndex() {
        queue.add(items)
        let removed = try? queue.removeItem(at: -1)
        XCTAssertNil(removed)
        XCTAssertEqual(queue.items.count, items.count)
    }
    
    func testRemoveUpcomingItems() {
        queue.add(items)
        queue.removeUpcomingItems()
        XCTAssertEqual(queue.nextItems.count, 0)
    }
    
    // MARK: - Jump
    
    // Test case 22: jumping to the current item
    func testJumpToCurrentItem() {
        queue.add(items)
        try! queue.jump(to: 0)

        let item = try! queue.jump(to: queue.currentIndex)
        XCTAssertNotNil(item)
    }
    
    func testJumpToSecondItem() {
        queue.add(items)
        let _ = try? queue.jump(to: 1)
        let jumped = try? queue.jump(to: 1)
        XCTAssertNotNil(jumped)
        XCTAssertEqual(jumped, queue.current)
        XCTAssertEqual(queue.currentIndex, 1)
    }
    
    func testJumpToLastItem() {
        queue.add(items)
        let jumped = try? queue.jump(to: items.count - 1)
        XCTAssertNotNil(jumped)
        XCTAssertEqual(jumped, queue.current)
        XCTAssertEqual(queue.currentIndex, items.count - 1)
    }
    
    func testJumpToNegativeIndex() {
        queue.add(items)
        try! queue.jump(to: 0)
        let jumped = try? queue.jump(to: -1)
        XCTAssertNil(jumped)
        XCTAssertEqual(queue.currentIndex, 0)
    }
    
    func testJumpWithTooLargeIndex() {
        queue.add(items)
        try! queue.jump(to: 0)
        let jumped = try? queue.jump(to: items.count)
        XCTAssertNil(jumped)
        XCTAssertEqual(queue.currentIndex, 0)
    }
    
    // MARK: - Moving
    
    func testMoveItemUpOne() {
        queue.add(items)
        try! queue.jump(to: 0)
        try! queue.moveItem(fromIndex: queue.currentIndex, toIndex: queue.currentIndex + 1)
        XCTAssertEqual(queue.currentIndex, 1)
    }
    
    func testMoveFromNegativeIndex() {
        queue.add(items)
        try! queue.jump(to: 0)
        
        XCTAssertThrowsError(try queue.moveItem(fromIndex: -1, toIndex: queue.currentIndex + 1))
    }
    
    func testMoveFromTooLargeIndex() {
        queue.add(items)
        try! queue.jump(to: 0)
        
        XCTAssertThrowsError(try queue.moveItem(fromIndex: queue.items.count, toIndex: queue.currentIndex + 1))
    }
    
    func testMoveToNegativeIndex() {
        queue.add(items)
        try! queue.jump(to: 0)
        
        XCTAssertThrowsError(try queue.moveItem(fromIndex: queue.currentIndex + 1, toIndex: -1))
    }
    
    func testMoveToTooLargeIndex() {
        queue.add(items)
        try! queue.moveItem(fromIndex: 0, toIndex: queue.items.count)
        XCTAssertEqual(queue.items.last, 0)
        XCTAssertEqual(queue.items.first, 1)
    }
    
    func testMoveSecondToThird() {
        queue.add(items)
        try? queue.moveItem(fromIndex: 1, toIndex: 3)
        XCTAssertEqual(queue.items, [0, 2, 1])
    }
    
    // MARK: - Clear
    
    func testClearQueue() {
        queue.add(items)
        queue.clearQueue()
        XCTAssertEqual(queue.currentIndex, -1)
        XCTAssertEqual(queue.items.count, 0)
    }
}
