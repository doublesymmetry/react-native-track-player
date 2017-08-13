//
//  URL+Offline.swift
//  AudioPlayer
//
//  Created by Kevin DELANNOY on 03/04/16.
//  Copyright © 2016 Kevin Delannoy. All rights reserved.
//

import Foundation

extension URL {
    //swiftlint:disable variable_name
    /// A boolean value indicating whether a resource should be considered available when internet connection is down
    /// or not.
    var ap_isOfflineURL: Bool {
        return isFileURL || scheme == "ipod-library" || host == "localhost" || host == "127.0.0.1"
    }
}
