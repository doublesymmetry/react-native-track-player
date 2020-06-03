//
//  MediaURL.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 12.08.17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

import Foundation

struct MediaURL {
    let value: URL
    let isLocal: Bool
    private let originalObject: Any
    
    init?(object: Any?) {
        guard let object = object else { return nil }
        originalObject = object
        
        // This is based on logic found in RCTConvert NSURLRequest, 
        // and uses RCTConvert NSURL to create a valid URL from various formats
        if let localObject = object as? [String: Any] {
            var url = localObject["uri"] as? String ?? localObject["url"] as! String
            
            if let bundleName = localObject["bundle"] as? String {
                url = String(format: "%@.bundle/%@", bundleName, url)
            }
            
            isLocal = url.lowercased().hasPrefix("http") ? false : true
            value = RCTConvert.nsurl(url)
        } else {
            let url = object as! String
            isLocal = url.lowercased().hasPrefix("file://")
            value = RCTConvert.nsurl(url)
        }
    }
}
