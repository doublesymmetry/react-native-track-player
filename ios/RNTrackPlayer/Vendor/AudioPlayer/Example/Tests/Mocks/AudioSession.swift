//
//  AudioSession.swift
//  SwiftAudio_Tests
//
//  Created by Jørgen Henrichsen on 31/10/2018.
//  Copyright © 2018 CocoaPods. All rights reserved.
//

import Foundation
import AVFoundation

@testable import SwiftAudio


class NonFailingAudioSession: AudioSession {
    
    var category: AVAudioSession.Category = AVAudioSession.Category.playback
    
    var mode: AVAudioSession.Mode = AVAudioSession.Mode.default
    
    var categoryOptions: AVAudioSession.CategoryOptions = []
    
    var availableCategories: [AVAudioSession.Category] = []
    
    var isOtherAudioPlaying: Bool = false
    
    func setCategory(_ category: AVAudioSession.Category, mode: AVAudioSession.Mode, options: AVAudioSession.CategoryOptions) throws {}
    
    func setCategory(_ category: AVAudioSession.Category, mode: AVAudioSession.Mode, policy: AVAudioSession.RouteSharingPolicy, options: AVAudioSession.CategoryOptions) throws {}
    
    func setActive(_ active: Bool) throws {}
    
    func setActive(_ active: Bool, options: AVAudioSession.SetActiveOptions) throws {}

}

class FailingAudioSession: AudioSession {
    
    var category: AVAudioSession.Category = AVAudioSession.Category.playback
    
    var mode: AVAudioSession.Mode = AVAudioSession.Mode.default
    
    var categoryOptions: AVAudioSession.CategoryOptions = AVAudioSession.CategoryOptions.allowBluetooth
    
    var availableCategories: [AVAudioSession.Category] = []
    
    var isOtherAudioPlaying: Bool = false
    
    func setCategory(_ category: AVAudioSession.Category, mode: AVAudioSession.Mode, options: AVAudioSession.CategoryOptions) throws {
        throw AVError(AVError.unknown)
    }
    
    func setCategory(_ category: AVAudioSession.Category, mode: AVAudioSession.Mode, policy: AVAudioSession.RouteSharingPolicy, options: AVAudioSession.CategoryOptions) throws {
        throw AVError(AVError.unknown)
    }
    
    func setActive(_ active: Bool) throws {
        throw AVError(AVError.unknown)
    }
    
    func setActive(_ active: Bool, options: AVAudioSession.SetActiveOptions) throws {
        throw AVError(AVError.unknown)
    }
    
    
}
