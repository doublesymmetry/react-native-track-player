//
//  AVPlayerWrapperDelegate.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 26/10/2018.
//

import Foundation
import MediaPlayer


protocol AVPlayerWrapperDelegate: AnyObject {
    
    func AVWrapper(didChangeState state: AVPlayerWrapperState)
    func AVWrapper(secondsElapsed seconds: Double)
    func AVWrapper(failedWithError error: Error?)
    func AVWrapper(seekTo seconds: Double, didFinish: Bool)
    func AVWrapper(didUpdateDuration duration: Double)
    func AVWrapper(didReceiveCommonMetadata metadata: [AVMetadataItem])
    func AVWrapper(didReceiveChapterMetadata metadata: [AVTimedMetadataGroup])
    func AVWrapper(didReceiveTimedMetadata metadata: [AVTimedMetadataGroup])
    func AVWrapper(didChangePlayWhenReady playWhenReady: Bool)
    func AVWrapperItemDidPlayToEndTime()
    func AVWrapperItemFailedToPlayToEndTime()
    func AVWrapperItemPlaybackStalled()
    func AVWrapperDidRecreateAVPlayer()
}
