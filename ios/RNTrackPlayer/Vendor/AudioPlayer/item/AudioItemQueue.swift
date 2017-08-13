//
//  AudioItemQueue.swift
//  AudioPlayer
//
//  Created by Kevin DELANNOY on 11/03/16.
//  Copyright © 2016 Kevin Delannoy. All rights reserved.
//

import Foundation

// MARK: - Array+Shuffe

private extension Array {
    /// Shuffles the element in the array and returns the new array.
    ///
    /// - Returns: A shuffled array.
    func ap_shuffled() -> [Element] {
        return sorted { element1, element2 in
            arc4random() % 2 == 0
        }
    }
}

// MARK: - AudioItemQueue

/// `AudioItemQueue` handles queueing items with a playing mode.
class AudioItemQueue {
    /// The original items, keeping the same order.
    private(set) var items: [AudioItem]

    /// The items stored in the way the mode requires.
    private(set) var queue: [AudioItem]

    /// The historic of items played in the queue.
    private(set) var historic: [AudioItem]

    /// The current position in the queue.
    var nextPosition = 0

    /// The player mode. It will affect the queue.
    var mode: AudioPlayerMode {
        didSet {
            adaptQueue(oldMode: oldValue)
        }
    }

    /// Initializes a queue with a list of items and the mode.
    ///
    /// - Parameters:
    ///   - items: The list of items to play.
    ///   - mode: The mode to play items with.
    init(items: [AudioItem], mode: AudioPlayerMode) {
        self.items = items
        self.mode = mode
        queue = mode.contains(.shuffle) ? items.ap_shuffled() : items
        historic = []
    }

    /// Adapts the queue to the new mode.
    ///
    /// Behaviour is:
    /// - `oldMode` contains .Repeat, `mode` doesn't and last item played == nextItem, we increment position.
    /// - `oldMode` contains .Shuffle, `mode` doesnt. We should set the queue to `items` and set current position to the
    ///     current item index in the new queue.
    /// - `mode` contains .Shuffle, `oldMode` doesn't. We should shuffle the leftover items in queue.
    ///
    /// Also, the items already played should also be shuffled. Current implementation has a limitation which is that
    /// the "already played items" will be shuffled at the begining of the queue while the leftovers will be shuffled at
    /// the end of the array.
    ///
    /// - Parameter oldMode: The mode before it changed.
    private func adaptQueue(oldMode: AudioPlayerMode) {
        //Early exit if queue is empty
        guard queue.count > nextPosition else {
            return
        }

        if oldMode.contains(.repeat) && !mode.contains(.repeat) &&
            historic.last == queue[nextPosition] {
            nextPosition += 1
        }
        if oldMode.contains(.shuffle) && !mode.contains(.shuffle) {
            queue = items
            if let last = historic.last, let index = queue.index(of: last) {
                nextPosition = index + 1
            }
        } else if mode.contains(.shuffle) && !oldMode.contains(.shuffle) {
            let alreadyPlayed = queue.prefix(upTo: nextPosition)
            let leftovers = queue.suffix(from: nextPosition)
            queue = Array(alreadyPlayed).ap_shuffled() + Array(leftovers).ap_shuffled()
        }
    }

    /// Returns the next item in the queue.
    ///
    /// - Returns: The next item in the queue.
    func nextItem() -> AudioItem? {
        //Early exit if queue is empty
        guard !queue.isEmpty else {
            return nil
        }

        if nextPosition < queue.count {
            let item = queue[nextPosition]
            if !mode.contains(.repeat) {
                nextPosition += 1
            }
            historic.append(item)
            return item
        }

        if mode.contains(.repeatAll) {
            nextPosition = 0
            return nextItem()
        }
        return nil
    }

    /// A boolean value indicating whether the queue has a next item to play or not.
    var hasNextItem: Bool {
        if !queue.isEmpty &&
            (queue.count > nextPosition || mode.contains(.repeat) || mode.contains(.repeatAll)) {
            return true
        }
        return false
    }

    /// Returns the previous item in the queue.
    ///
    /// - Returns: The previous item in the queue.
    func previousItem() -> AudioItem? {
        //Early exit if queue is empty
        guard !queue.isEmpty else {
            return nil
        }

        let previousPosition = nextPosition - 1

        if mode.contains(.repeat) {
            let position = max(previousPosition, 0)
            let item = queue[position]
            historic.append(item)
            return item
        }

        if previousPosition > 0 {
            let item = queue[previousPosition - 1]
            nextPosition = previousPosition
            historic.append(item)
            return item
        }

        if mode.contains(.repeatAll) {
            nextPosition = queue.count + 1
            return previousItem()
        }
        return nil
    }

    /// A boolean value indicating whether the queue has a previous item to play or not.
    var hasPreviousItem: Bool {
        if !queue.isEmpty &&
            (nextPosition > 0 || mode.contains(.repeat) || mode.contains(.repeatAll)) {
            return true
        }
        return false
    }

    /// Adds a list of items to the queue.
    ///
    /// - Parameter items: The items to add to the queue.
    func add(items: [AudioItem]) {
        self.items.append(contentsOf: items)
        self.queue.append(contentsOf: items)
    }

    /// Removes an item from the queue.
    ///
    /// - Parameter index: The index of the item to remove.
    func remove(at index: Int) {
        let item = queue.remove(at: index)
        if let index = items.index(of: item) {
            items.remove(at: index)
        }
    }
}
