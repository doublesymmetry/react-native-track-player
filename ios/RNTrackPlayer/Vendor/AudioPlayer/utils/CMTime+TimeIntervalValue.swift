//
//  CMTime+TimeIntervalValue.swift
//  AudioPlayer
//
//  Created by Kevin DELANNOY on 11/03/16.
//  Copyright © 2016 Kevin Delannoy. All rights reserved.
//

import CoreMedia

extension CMTime {
    /// Initializes a `CMTime` instance from a time interval.
    ///
    /// - Parameter timeInterval: The time in seconds.
    init(timeInterval: TimeInterval) {
        self.init(seconds: timeInterval, preferredTimescale: 1000000000)
    }

    //swiftlint:disable variable_name
    /// Returns the TimerInterval value of CMTime (only if it's a valid value).
    var ap_timeIntervalValue: TimeInterval? {
        if flags.contains(.valid) {
            let seconds = CMTimeGetSeconds(self)
            if !seconds.isNaN {
                return TimeInterval(seconds)
            }
        }
        return nil
    }
}
