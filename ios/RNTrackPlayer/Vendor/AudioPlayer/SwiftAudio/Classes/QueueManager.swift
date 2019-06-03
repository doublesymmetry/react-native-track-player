//
//  QueueManager.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 24/03/2018.
//

import Foundation


class QueueManager<T> {
    
    private var _items: [T] = []
    
    /**
     All items held by the queue.
     */
    public var items: [T] {
        return _items
    }
    
    public var nextItems: [T] {
        guard _currentIndex + 1 < _items.count else {
            return []
        }
        return Array(_items[_currentIndex + 1..<_items.count])
    }
    
    public var previousItems: [T] {
        if (_currentIndex == 0) {
            return []
        }
        return Array(_items[0..<_currentIndex])
    }
    
    private var _currentIndex: Int = 0
    
    /**
     The index of the current item.
     Will be populated event though there is no current item (When the queue is empty).
     */
    public var currentIndex: Int {
        return _currentIndex
    }
    
    /**
     The current item for the queue.
     */
    public var current: T? {
        if _items.count > _currentIndex {
            return _items[_currentIndex]
        }
        return nil
    }
    
    /**
     Add a single item to the queue.
     
     - parameter item: The `AudioItem` to be added.
     */
    public func addItem(_ item: T) {
        _items.append(item)
    }
    
    /**
     Add an array of items to the queue.
     
     - parameter items: The `AudioItem`s to be added.
     */
    public func addItems(_ items: [T]) {
        _items.append(contentsOf: items)
    }
    
    /**
     Add an array of items to the queue at a given index.
     
     - parameter items: The `AudioItem`s to be added.
     - parameter at: The index to insert the items at.
     */
    public func addItems(_ items: [T], at index: Int) throws {
        guard index >= 0 && _items.count > index else {
            throw APError.QueueError.invalidIndex(index: index, message: "Index for addition has to be positive and smaller than the count of current items (\(_items.count))")
        }
        
        _items.insert(contentsOf: items, at: index)
        if (_currentIndex >= index) { _currentIndex = _currentIndex + items.count }
    }
    
    /**
     Get the next item in the queue, if there are any.
     Will update the current item.
     
     - throws: `APError.QueueError`
     - returns: The next item.
     */
    @discardableResult
    public func next() throws -> T {
        let nextIndex = _currentIndex + 1
        guard _items.count > nextIndex else {
            throw APError.QueueError.noNextItem
        }
        _currentIndex = nextIndex
        return _items[nextIndex]
    }
    
    /**
     Get the previous item in the queue, if there are any.
     Will update the current item.

     - throws: `APError.QueueError`
     - returns: The previous item.
     */
    @discardableResult
    public func previous() throws -> T {
        let previousIndex = _currentIndex - 1
        guard previousIndex >= 0 else {
            throw APError.QueueError.noPreviousItem
        }
        _currentIndex = previousIndex
        return _items[previousIndex]
    }
    
    /**
     Jump to a position in the queue.
     Will update the current item.
     
     - parameter index: The index to jump to.
     - throws: `APError.QueueError`
     - returns: The item at the index.
     */
    @discardableResult
    func jump(to index: Int) throws -> T {
        guard index != currentIndex else {
            throw APError.QueueError.invalidIndex(index: index, message: "Cannot jump to the current item")
        }
        
        guard index >= 0 && _items.count > index else {
            throw APError.QueueError.invalidIndex(index: index, message: "The jump index has to be positive and smaller thant the count of current items (\(_items.count))")
        }

        _currentIndex = index
        return _items[index]
    }
    
    /**
     Move an item in the queue.
     
     - parameter fromIndex: The index of the item to be moved.
     - parameter toIndex: The index to move the item to.
     - throws: `APError.QueueError`
     */
    func moveItem(fromIndex: Int, toIndex: Int) throws {
        
        guard fromIndex != _currentIndex else {
            throw APError.QueueError.invalidIndex(index: fromIndex, message: "The fromIndex cannot be equal to the current index.")
        }
        
        guard fromIndex >= 0 && fromIndex < _items.count else {
            throw APError.QueueError.invalidIndex(index: fromIndex, message: "The fromIndex has to be positive and smaller than the count of current items (\(_items.count)).")
        }
        
        guard toIndex >= 0 && toIndex < _items.count else {
            throw APError.QueueError.invalidIndex(index: toIndex, message: "The toIndex has to be positive and smaller than the count of current items (\(_items.count)).")
        }
        
        let item = try removeItem(at: fromIndex)
        try addItems([item], at: toIndex)
    }
    
    /**
     Remove an item.
     
     - parameter index: The index of the item to remove.
     - throws: APError.QueueError
     - returns: The removed item.
     */
    @discardableResult
    public func removeItem(at index: Int) throws -> T {
        guard index != _currentIndex else {
            throw APError.QueueError.invalidIndex(index: index, message: "Cannot remove the current item!")
        }
        
        guard index >= 0 && _items.count > index else {
            throw APError.QueueError.invalidIndex(index: index, message: "Index for removal has to be positive and smaller than the count of current items (\(_items.count)).")
        }
        
        if index < _currentIndex {
            _currentIndex = _currentIndex - 1
        }

        return _items.remove(at: index)
    }
    
    /**
     Replace the current item with a new one. If there is no current item, it is equivalent to calling add(item:).
     
     - parameter item: The item to set as the new current item.
     */
    public func replaceCurrentItem(with item: T) {
        if current == nil  {
            self.addItem(item)
        }
        
        self._items[_currentIndex] = item
    }
    
    /**
     Remove all previous items in the queue.
     If no previous items exist, no action will be taken.
     */
    public func removePreviousItems() {
        guard currentIndex > 0 else { return }
        _items.removeSubrange(0..<_currentIndex)
        _currentIndex = 0
    }

    /**
     Remove upcoming items.
     If no upcoming items exist, no action will be taken.
     */
    public func removeUpcomingItems() {
        let nextIndex = _currentIndex + 1
        guard nextIndex < _items.count else { return }
        _items.removeSubrange(nextIndex..<_items.count)
    }
    
    /**
     Removes all items for queue
     */
    public func clearQueue() {
        _currentIndex = 0
        _items.removeAll()
    }

}
