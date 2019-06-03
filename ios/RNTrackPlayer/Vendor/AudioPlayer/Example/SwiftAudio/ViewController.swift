//
//  ViewController.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 03/11/2018.
//  Copyright (c) 2018 Jørgen Henrichsen. All rights reserved.
//

import UIKit
import SwiftAudio
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
    
    var isScrubbing: Bool = false
    let controller = AudioController.shared
    
    override func viewDidLoad() {
        super.viewDidLoad()
        controller.player.event.stateChange.addListener(self, handleAudioPlayerStateChange)
        controller.player.event.secondElapse.addListener(self, handleAudioPlayerSecondElapsed)
        controller.player.event.seek.addListener(self, handleAudioPlayerDidSeek)
        controller.player.event.updateDuration.addListener(self, handleAudioPlayerUpdateDuration)
        controller.player.event.didRecreateAVPlayer.addListener(self, handleAVPlayerRecreated)
    }
    
    @IBAction func togglePlay(_ sender: Any) {
        if (!controller.audioSessionController.audioSessionIsActive) {
            try? controller.audioSessionController.activateSession()
        }
        controller.player.togglePlaying()
    }
    
    @IBAction func previous(_ sender: Any) {
        try? controller.player.previous()
    }
    
    @IBAction func next(_ sender: Any) {
        try? controller.player.next()
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
    
    func updateTimeValues() {
        self.slider.maximumValue = Float(self.controller.player.duration)
        self.slider.setValue(Float(self.controller.player.currentTime), animated: true)
        self.elapsedTimeLabel.text = self.controller.player.currentTime.secondsToString()
        self.remainingTimeLabel.text = (self.controller.player.duration - self.controller.player.currentTime).secondsToString()
    }
    
    func updateMetaData() {
        if let item = controller.player.currentItem {
            titleLabel.text = item.getTitle()
            artistLabel.text = item.getArtist()
            item.getArtwork({ (image) in
                self.imageView.image = image
            })
        }
    }
    
    func setPlayButtonState(forAudioPlayerState state: AudioPlayerState) {
        playButton.setTitle(state == .playing ? "Pause" : "Play", for: .normal)
    }
    
    // MARK: - AudioPlayer Event Handlers
    
    func handleAudioPlayerStateChange(data: AudioPlayer.StateChangeEventData) {
        DispatchQueue.main.async {
            self.setPlayButtonState(forAudioPlayerState: data)
            switch data {
            case .ready:
                self.updateMetaData()
                self.updateTimeValues()
            case .loading, .playing, .paused, .idle:
                self.updateTimeValues()
            }
        }
    }
    
    func handleAudioPlayerSecondElapsed(data: AudioPlayer.SecondElapseEventData) {
        if !isScrubbing {
            DispatchQueue.main.async {
                self.updateTimeValues()
            }
        }
    }
    
    func handleAudioPlayerDidSeek(data: AudioPlayer.SeekEventData) {
        isScrubbing = false
    }
    
    func handleAudioPlayerUpdateDuration(data: AudioPlayer.UpdateDurationEventData) {
        DispatchQueue.main.async {
            self.updateTimeValues()
        }
    }
    
    func handleAVPlayerRecreated() {
        try? controller.audioSessionController.set(category: .playback)
    }
    
}
