//
//  NowPlayingInfoCenter.swift
//  SwiftAudio_Tests
//
//  Created by Jørgen Henrichsen on 03/03/2019.
//  Copyright © 2019 CocoaPods. All rights reserved.
//

import Foundation
import AVFoundation

@testable import SwiftAudio

class NowPlayingInfoCenter_Mock: NowPlayingInfoCenter {
    
    var nowPlayingInfo: [String : Any]? = nil
    
}
