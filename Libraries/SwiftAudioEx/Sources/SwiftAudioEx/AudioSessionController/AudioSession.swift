//
//  AudioSession.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 02/11/2018.
//

import Foundation
import AVFoundation


protocol AudioSession {
    
    var isOtherAudioPlaying: Bool { get }
    
    var category: AVAudioSession.Category { get }
    
    var mode: AVAudioSession.Mode { get }
    
    var categoryOptions: AVAudioSession.CategoryOptions { get }
    
    var availableCategories: [AVAudioSession.Category] { get }
    
    func setCategory(_ category: AVAudioSession.Category, mode: AVAudioSession.Mode, options: AVAudioSession.CategoryOptions) throws
    
    func setCategory(_ category: AVAudioSession.Category, mode: AVAudioSession.Mode, policy: AVAudioSession.RouteSharingPolicy, options: AVAudioSession.CategoryOptions) throws
    
    func setActive(_ active: Bool, options: AVAudioSession.SetActiveOptions) throws
    
}

extension AVAudioSession: AudioSession {}
