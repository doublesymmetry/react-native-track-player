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
        
        if let localObject = object as? [String: Any] {
            let uri = localObject["uri"] as! String
            isLocal = uri.contains("http") ? false : true
            let encodedURI = uri.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)!
            value = URL(string: encodedURI.replacingOccurrences(of: "file://", with: ""))!
        } else {
            let url = object as! String
            isLocal = url.lowercased().hasPrefix("file://")
            if isLocal {
                // When the provided url is local,
                // remove the file:// prefix,
                // and attempt to remove any existing encoding.
                // Both of these can both cause URL(fileURLWithPath: string) to fail
                var path = url.replacingOccurrences(of: "file://", with: "")
                path = path.removingPercentEncoding ?? path
                value = URL(fileURLWithPath: path)
            } else {
                // When the provided url is not local, attempt to use it without any modifications
                if let urlValue = URL(string: url) {
                    value = urlValue
                } else {
                    // If url could not be initialized, try encoding it
                    print("Warning: The provided url could not be parsed. Attempting to encode url instead.")
                    value = URL(string: url.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)!)!
                }
            }
        }
    }
}
