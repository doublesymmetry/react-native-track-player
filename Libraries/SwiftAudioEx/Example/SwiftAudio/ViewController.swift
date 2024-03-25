//
//  ViewController.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 03/11/2018.
//  Copyright (c) 2018 Jørgen Henrichsen. All rights reserved.
//

import UIKit
import SwiftAudioEx
import AVFoundation
import MediaPlayer


class ViewController: UIViewController {

    @IBOutlet weak var playButton: UIButton!
    @IBOutlet weak var slider: UISlider!
    @IBOutlet weak var imageView: UIImageView!
    @IBOutlet weak var remainingTimeLabel: UILabel!
    @IBOutlet weak var elapsedTimeLabel: UILabel!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var artistLabel: UILabel!
    @IBOutlet weak var loadIndicator: UIActivityIndicatorView!
    @IBOutlet weak var errorLabel: UILabel!
    
    private var isScrubbing: Bool = false
    private let controller = AudioController.shared
    
    override func viewDidLoad() {
        super.viewDidLoad()
        controller.player.event.playWhenReadyChange.addListener(self, handlePlayWhenReadyChange)
        controller.player.event.stateChange.addListener(self, handleAudioPlayerStateChange)
        controller.player.event.playbackEnd.addListener(self, handleAudioPlayerPlaybackEnd(data:))
        controller.player.event.secondElapse.addListener(self, handleAudioPlayerSecondElapsed)
        controller.player.event.seek.addListener(self, handleAudioPlayerDidSeek)
        controller.player.event.updateDuration.addListener(self, handleAudioPlayerUpdateDuration)
        controller.player.event.didRecreateAVPlayer.addListener(self, handleAVPlayerRecreated)
        handleAudioPlayerStateChange(data: controller.player.playerState)
        DispatchQueue.main.async {
            self.render()
        }
    }
    
    // MARK: - Actions
    
    @IBAction func togglePlay(_ sender: Any) {
        if !controller.audioSessionController.audioSessionIsActive {
            try? controller.audioSessionController.activateSession()
        }
        controller.player.playWhenReady = playButton.currentTitle == "Play"
    }
    
    @IBAction func previous(_ sender: Any) {
        controller.player.previous()
    }
    
    @IBAction func next(_ sender: Any) {
        controller.player.next()
    }
    
    @IBAction func startScrubbing(_ sender: UISlider) {
        isScrubbing = true
    }
    
    @IBAction func scrubbing(_ sender: UISlider) {
        controller.player.seek(to: Double(slider.value))
    }
    
    @IBAction func scrubbingValueChanged(_ sender: UISlider) {
        let value = Double(slider.value)
        elapsedTimeLabel.text = value.secondsToString()
        remainingTimeLabel.text = (controller.player.duration - value).secondsToString()
    }
    
    // MARK: - Render
    
    func renderTimeValues() {
        self.slider.maximumValue = Float(self.controller.player.duration)
        self.slider.setValue(Float(self.controller.player.currentTime), animated: true)
        self.elapsedTimeLabel.text = self.controller.player.currentTime.secondsToString()
        self.remainingTimeLabel.text = (self.controller.player.duration - self.controller.player.currentTime).secondsToString()
    }

    func render() {
        let player = self.controller.player
        
        // Render play button
        self.playButton.setTitle(
            !player.playWhenReady || player.playerState == .failed
                ? "Play"
                : "Pause",
            for: .normal
        )

        // Render metadata
        if let item = player.currentItem {
            self.titleLabel.text = item.getTitle()
            self.artistLabel.text = item.getArtist()
            item.getArtwork({ (image) in
                self.imageView.image = image
            })
        }

        // Render time values
        self.renderTimeValues()

        // Render error label
        if (player.playerState == .failed) {
            self.errorLabel.isHidden = false
            self.errorLabel.text = "Playback failed."
        } else {
            self.errorLabel.text = ""
            self.errorLabel.isHidden = true
        }

        // Render load indicator:
        if (
            (player.playerState == .loading || player.playerState == .buffering)
            && self.controller.player.playWhenReady // Avoid showing indicator before user has pressed play
        ) {
            self.loadIndicator.startAnimating()
        } else {
            self.loadIndicator.stopAnimating()
        }
    }
    
    // MARK: - AudioPlayer Event Handlers
    
    func handleAudioPlayerStateChange(data: AudioPlayer.StateChangeEventData) {
        print("state=\(data)")
        DispatchQueue.main.async {
            self.render()
        }
    }
    
    func handlePlayWhenReadyChange(data: AudioPlayer.PlayWhenReadyChangeData) {
        print("playWhenReady=\(data)")
        DispatchQueue.main.async {
            self.render()
        }
    }
    
    func handleAudioPlayerPlaybackEnd(data: AudioPlayer.PlaybackEndEventData) {
        print("playEndReason=\(data)")
    }
    
    func handleAudioPlayerSecondElapsed(data: AudioPlayer.SecondElapseEventData) {
        if !isScrubbing {
            DispatchQueue.main.async {
                self.renderTimeValues()
            }
        }
    }
    
    func handleAudioPlayerDidSeek(data: AudioPlayer.SeekEventData) {
        isScrubbing = false
    }
    
    func handleAudioPlayerUpdateDuration(data: AudioPlayer.UpdateDurationEventData) {
        DispatchQueue.main.async {
            self.renderTimeValues()
        }
    }
    
    func handleAVPlayerRecreated() {
        try? controller.audioSessionController.set(category: .playback)
    }
}
