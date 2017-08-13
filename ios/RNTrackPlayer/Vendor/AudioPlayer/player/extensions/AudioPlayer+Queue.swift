//
//  AudioPlayer+Queue.swift
//  AudioPlayer
//
//  Created by Kevin DELANNOY on 29/03/16.
//  Copyright © 2016 Kevin Delannoy. All rights reserved.
//

extension AudioPlayer {
    /// The items in the queue if any.
    public var items: [AudioItem]? {
        return queue?.queue
    }

    /// The current item index in queue.
    public var currentItemIndexInQueue: Int? {
        return currentItem.flatMap { queue?.items.index(of: $0) }
    }

    /// A boolean value indicating whether there is a next item to play or not.
    public var hasNext: Bool {
        return queue?.hasNextItem ?? false
    }

    /// A boolean value indicating whether there is a previous item to play or not.
    public var hasPrevious: Bool {
        return queue?.hasPreviousItem ?? false
    }

    /// Plays an item.
    ///
    /// - Parameter item: The item to play.
    public func play(item: AudioItem) {
        play(items: [item])
    }

    /// Creates a queue according to the current mode and plays it.
    ///
    /// - Parameters:
    ///   - items: The items to play.
    ///   - index: The index to start the player with.
    public func play(items: [AudioItem], startAtIndex index: Int = 0) {
        if !items.isEmpty {
            queue = AudioItemQueue(items: items, mode: mode)
            if let realIndex = queue?.queue.index(of: items[index]) {
                queue?.nextPosition = realIndex
            }
            currentItem = queue?.nextItem()
        } else {
            stop()
            queue = nil
        }
    }

    /// Adds an item at the end of the queue. If queue is empty and player isn't playing, the behaviour will be similar
    /// to `play(item:)`.
    ///
    /// - Parameter item: The item to add.
    public func add(item: AudioItem) {
        add(items: [item])
    }

    /// Adds items at the end of the queue. If the queue is empty and player isn't playing, the behaviour will be
    /// similar to `play(items:)`.
    ///
    /// - Parameter items: The items to add.
    public func add(items: [AudioItem]) {
        if let queue = queue {
            queue.add(items: items)
        } else {
            play(items: items)
        }
    }

    /// Removes an item at a specific index in the queue.
    ///
    /// - Parameter index: The index of the item to remove.
    public func removeItem(at index: Int) {
        queue?.remove(at: index)
    }
}
