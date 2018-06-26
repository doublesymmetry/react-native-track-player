//
//  Collections.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 16.04.18.
//  Copyright © 2018 David Chavez. All rights reserved.
//

import Foundation

extension Collection {
    /// Returns the element at the specified index iff it is within bounds, otherwise nil.
    subscript (safe index: Index) -> Element? {
        return indices.contains(index) ? self[index] : nil
    }
}

extension Array {
    public func filter(_ isIncluded: (Int, Element) -> Bool) -> [Element] {
        var newArray: [Element] = []
        for (index, item) in self.enumerated() {
            if isIncluded(index, item) { newArray.append(item) }
        }
        
        return newArray
    }
}
