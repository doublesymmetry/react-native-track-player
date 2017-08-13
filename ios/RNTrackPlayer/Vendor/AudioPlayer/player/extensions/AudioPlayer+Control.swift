//
//  AudioPlayer+Control.swift
//  AudioPlayer
//
//  Created by Kevin DELANNOY on 29/03/16.
//  Copyright © 2016 Kevin Delannoy. All rights reserved.
//

import CoreMedia
#if os(iOS) || os(tvOS)
    import UIKit
#endif

extension AudioPlayer {
    /// Resumes the player.
    public func resume() {
        //Ensure pause flag is no longer set
        pausedForInterruption = false
        
        player?.rate = rate

        //We don't wan't to change the state to Playing in case it's Buffering. That
        //would be a lie.
        if !state.isPlaying && !state.isBuffering {
            state = .playing
        }

        retryEventProducer.startProducingEvents()
    }

    /// Pauses the player.
    public func pause() {
        //We ensure the player actually pauses
        player?.rate = 0
        state = .paused

        retryEventProducer.stopProducingEvents()

        //Let's begin a background task for the player to keep buffering if the app is in
        //background. This will mimic the default behavior of `AVPlayer` when pausing while the
        //app is in foreground.
        backgroundHandler.beginBackgroundTask()
    }
    
    /// Starts playing the current item immediately. Works on iOS/tvOS 10+ and macOS 10.12+
    func playImmediately() {
        if #available(iOS 10.0, tvOS 10.0, OSX 10.12, *) {
            self.state = .playing
            player?.playImmediately(atRate: rate)
            
            retryEventProducer.stopProducingEvents()
            backgroundHandler.endBackgroundTask()
        }
    }

    /// Plays previous item in the queue or rewind current item.
    public func previous() {
        if hasPrevious {
            currentItem = queue?.previousItem()
        } else {
            seek(to: 0)
        }
    }

    /// Plays next item in the queue.
    public func next() {
        if hasNext {
            currentItem = queue?.nextItem()
        }
    }

    /// Plays the next item in the queue and if there isn't, the player will stop.
    public func nextOrStop() {
        if hasNext {
            next()
        } else {
            stop()
        }
    }

    /// Stops the player and clear the queue.
    public func stop() {
        retryEventProducer.stopProducingEvents()

        if let _ = player {
            player?.rate = 0
            player = nil
        }
        if let _ = currentItem {
            currentItem = nil
        }
        if let _ = queue {
            queue = nil
        }

        setAudioSession(active: false)
        state = .stopped
    }

    /// Seeks to a specific time.
    ///
    /// - Parameters:
    ///   - time: The time to seek to.
    ///   - byAdaptingTimeToFitSeekableRanges: A boolean value indicating whether the time should be adapted to current
    ///         seekable ranges in order to be bufferless.
    ///   - toleranceBefore: The tolerance allowed before time.
    ///   - toleranceAfter: The tolerance allowed after time.
    ///   - completionHandler: The optional callback that gets executed upon completion with a boolean param indicating
    ///         if the operation has finished.
    public func seek(to time: TimeInterval,
                     byAdaptingTimeToFitSeekableRanges: Bool = false,
                     toleranceBefore: CMTime = kCMTimePositiveInfinity,
                     toleranceAfter: CMTime = kCMTimePositiveInfinity,
                     completionHandler: ((Bool) -> Void)? = nil) {
        guard let earliest = currentItemSeekableRange?.earliest,
            let latest = currentItemSeekableRange?.latest else {
                //In case we don't have a valid `seekableRange`, although this *shouldn't* happen
                //let's just call `AVPlayer.seek(to:)` with given values.
                seekSafely(to: time, toleranceBefore: toleranceBefore, toleranceAfter: toleranceAfter,
                           completionHandler: completionHandler)
                return
        }

        if !byAdaptingTimeToFitSeekableRanges || (time >= earliest && time <= latest) {
            //Time is in seekable range, there's no problem here.
            seekSafely(to: time, toleranceBefore: toleranceBefore, toleranceAfter: toleranceAfter,
                 completionHandler: completionHandler)
        } else if time < earliest {
            //Time is before seekable start, so just move to the most early position as possible.
            seekToSeekableRangeStart(padding: 1, completionHandler: completionHandler)
        } else if time > latest {
            //Time is larger than possibly, so just move forward as far as possible.
            seekToSeekableRangeEnd(padding: 1, completionHandler: completionHandler)
        }
    }

    /// Seeks backwards as far as possible.
    ///
    /// - Parameter padding: The padding to apply if any.
    /// - completionHandler: The optional callback that gets executed upon completion with a boolean param indicating
    ///     if the operation has finished.
    public func seekToSeekableRangeStart(padding: TimeInterval, completionHandler: ((Bool) -> Void)? = nil) {
        guard let range = currentItemSeekableRange else {
                completionHandler?(false)
                return
        }
        let position = min(range.latest, range.earliest + padding)
        seekSafely(to: position, completionHandler: completionHandler)
    }

    /// Seeks forward as far as possible.
    ///
    /// - Parameter padding: The padding to apply if any.
    /// - completionHandler: The optional callback that gets executed upon completion with a boolean param indicating
    ///     if the operation has finished.
    public func seekToSeekableRangeEnd(padding: TimeInterval, completionHandler: ((Bool) -> Void)? = nil) {
        guard let range = currentItemSeekableRange else {
                completionHandler?(false)
                return
        }
        let position = max(range.earliest, range.latest - padding)
        seekSafely(to: position, completionHandler: completionHandler)
    }

    #if os(iOS) || os(tvOS)
    //swiftlint:disable cyclomatic_complexity
    /// Handle events received from Control Center/Lock screen/Other in UIApplicationDelegate.
    ///
    /// - Parameter event: The event received.
    public func remoteControlReceived(with event: UIEvent) {
        guard event.type == .remoteControl else {
            return
        }

        switch event.subtype {
        case .remoteControlBeginSeekingBackward:
            seekingBehavior.handleSeekingStart(player: self, forward: false)
        case .remoteControlBeginSeekingForward:
            seekingBehavior.handleSeekingStart(player: self, forward: true)
        case .remoteControlEndSeekingBackward:
            seekingBehavior.handleSeekingEnd(player: self, forward: false)
        case .remoteControlEndSeekingForward:
            seekingBehavior.handleSeekingEnd(player: self, forward: true)
        case .remoteControlNextTrack:
            next()
        case .remoteControlPause,
             .remoteControlTogglePlayPause where state.isPlaying:
            pause()
        case .remoteControlPlay,
             .remoteControlTogglePlayPause where state.isPaused:
            resume()
        case .remoteControlPreviousTrack:
            previous()
        case .remoteControlStop:
            stop()
        default:
            break
        }
    }
    #endif
}

extension AudioPlayer {
    
    fileprivate func seekSafely(to time: TimeInterval,
              toleranceBefore: CMTime = kCMTimePositiveInfinity,
              toleranceAfter: CMTime = kCMTimePositiveInfinity,
              completionHandler: ((Bool) -> Void)?) {
        guard let completionHandler = completionHandler else {
            player?.seek(to: CMTime(timeInterval: time), toleranceBefore: toleranceBefore,
                         toleranceAfter: toleranceAfter)
            updateNowPlayingInfoCenter()
            return
        }
        guard player?.currentItem?.status == .readyToPlay else {
            completionHandler(false)
            return
        }
        player?.seek(to: CMTime(timeInterval: time), toleranceBefore: toleranceBefore, toleranceAfter: toleranceAfter,
                     completionHandler: { [weak self] finished in
                        completionHandler(finished)
                        self?.updateNowPlayingInfoCenter()
        })
    }
}
