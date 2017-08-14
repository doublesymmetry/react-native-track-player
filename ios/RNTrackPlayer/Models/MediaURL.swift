//
//  MediaURL.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 12.08.17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

import Foundation

struct MediaURL {
    var value: URL
    
    init?(object: Any?) {
        guard let object = object else { return nil }
        
        if let localObject = object as? [String: Any] {
            value = URL(string: localObject["uri"] as! String)!
        } else {
            value = URL(string: object as! String)!
        }
    }
}
