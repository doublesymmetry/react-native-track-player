//
//  MediaURL.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 12.08.17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

import Foundation

@objc(MediaURL)
class MediaURL: NSObject {
    var value: URL
    let isLocal: Bool
    
    init?(object: Any?) {
        guard let object = object else { return nil }
        
        if let localObject = object as? [String: Any] {
            isLocal = true
            value = URL(string: localObject["uri"] as! String)!
        } else {
            isLocal = false
            value = URL(string: object as! String)!
        }
        
        super.init()
    }
}
