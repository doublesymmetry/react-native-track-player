//
//  Track.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 12.08.17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

import Foundation
import MediaPlayer
import AVFoundation

class Track: NSObject {
    let id: String
    let url: MediaURL
    dynamic let title: String
    dynamic let artist: String
    
    let date: String?
    let desc: String?
    let genre: String?
    let pitchAlgorithm: String?
    let duration: Double?
    let artworkURL: MediaURL?
    dynamic let album: String?
    dynamic var artwork: MPMediaItemArtwork?
    
    private let originalObject: [String: Any]
    
    init?(dictionary: [String: Any]) {
        guard let id = dictionary["id"] as? String,
            let title = dictionary["title"] as? String,
            let artist = dictionary["artist"] as? String,
            let url = MediaURL(object: dictionary["url"])
            else { return nil }
        
        self.id = id
        self.url = url
        self.title = title
        self.artist = artist
        
        self.date = dictionary["date"] as? String
        self.album = dictionary["album"] as? String
        self.genre = dictionary["genre"] as? String
        self.desc = dictionary["description"] as? String
        self.pitchAlgorithm = dictionary["pitchAlgorithm"] as? String
        self.duration = dictionary["duration"] as? Double
        self.artworkURL = MediaURL(object: dictionary["artwork"])
        
        self.originalObject = dictionary
    }
    
    
    // MARK: - Public Interface
    
    func toObject() -> [String: Any] {
        return originalObject
    }
}
