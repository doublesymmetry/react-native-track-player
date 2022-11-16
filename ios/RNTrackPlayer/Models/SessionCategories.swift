//
//  SessionCategory.swift
//  RNTrackPlayer
//
//  Created by Thomas Hessler on 3/12/19.
//  Copyright © 2019 David Chavez. All rights reserved.
//

import Foundation
import MediaPlayer
import AVFoundation

enum SessionCategory : String {
    
    case playAndRecord, multiRoute, playback, ambient, soloAmbient
    
    func mapConfigToAVAudioSessionCategory() -> AVAudioSession.Category {
        switch self {
        case .playAndRecord:
            return .playAndRecord
        case .multiRoute:
            return .multiRoute
        case .playback:
            return .playback
        case .ambient:
            return .ambient
        case .soloAmbient:
            return .soloAmbient
        }
    }
}

enum SessionCategoryOptions : String {
    
    case mixWithOthers, duckOthers, interruptSpokenAudioAndMixWithOthers, allowBluetooth, allowBluetoothA2DP, allowAirPlay, defaultToSpeaker

    func mapConfigToAVAudioSessionCategoryOptions() -> AVAudioSession.CategoryOptions? {
        switch self {
        case .mixWithOthers:
            return .mixWithOthers
        case .duckOthers:
            return .duckOthers
        case .interruptSpokenAudioAndMixWithOthers:
            return .interruptSpokenAudioAndMixWithOthers
        case .allowBluetooth:
            return .allowBluetooth
        case .allowBluetoothA2DP:
            return .allowBluetoothA2DP
        case .allowAirPlay:
            return .allowAirPlay
        case .defaultToSpeaker:
            return .defaultToSpeaker
        }
    }
}

enum SessionCategoryMode : String {
    
    case `default`, gameChat, measurement, moviePlayback, spokenAudio, videoChat, videoRecording, voiceChat, voicePrompt
    
    func mapConfigToAVAudioSessionCategoryMode() -> AVAudioSession.Mode {
        switch self {
        case .default:
            return .default
        case .gameChat:
            return .gameChat
        case .measurement:
            return .measurement
        case .moviePlayback:
            return .moviePlayback
        case .spokenAudio:
            return .spokenAudio
        case .videoChat:
            return .videoChat
        case .videoRecording:
            return .videoRecording
        case .voiceChat:
            return .voiceChat
        case .voicePrompt:
            if #available(iOS 12.0, *) {
                return .voicePrompt
            } else {
                // Do Nothing
                return .default
            }
        }
    }
}

enum SessionCategoryPolicy : String {
    
    case `default`, longFormAudio, longFormVideo, independent
    
    func mapConfigToAVAudioSessionCategoryPolicy() -> AVAudioSession.RouteSharingPolicy {
        switch self {
        case .default:
            return .default
        case .longFormAudio:
            if #available(iOS 13.0, *) {
                return .longFormAudio
            } else if #available(iOS 11.0, *) {
                return .longForm
            } else {
                return .default
            }
        case .longFormVideo:
            if #available(iOS 13.0, *) {
                return .longFormVideo
            } else {
                return .default
            }
        case .independent:
            return .independent
        }
    }
}
