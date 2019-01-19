//
//  APError.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 25/03/2018.
//

import Foundation


public struct APError {
    
    enum LoadError: Error {
        case invalidSourceUrl(String)
    }
    
    enum PlaybackError: Error {
        case noLoadedItem
    }
    
    enum QueueError: Error {
        case noPreviousItem
        case noNextItem
        case invalidIndex(index: Int, message: String)
    }
    
}
