import Quick
import Nimble
import AVFoundation

@testable import SwiftAudio

class AudioPlayerTests: QuickSpec {
    
    override func spec() {
        describe("An AudioPlayer") {
            var audioPlayer: AudioPlayer!
            
            beforeEach {
                audioPlayer = AudioPlayer()
                audioPlayer.bufferDuration = 0.0001
                audioPlayer.automaticallyWaitsToMinimizeStalling = false
                audioPlayer.volume = 0.0
            }
            
            describe("its state", {
                
                it("should be idle", closure: {
                    expect(audioPlayer.playerState).to(equal(AudioPlayerState.idle))
                })
                
                context("when audio item is loaded", {
                    beforeEach {
                        try? audioPlayer.load(item: Source.getAudioItem(), playWhenReady: false)
                    }
                    
                    it("it should eventually be ready", closure: {
                        expect(audioPlayer.playerState).toEventually(equal(AudioPlayerState.ready))
                    })
                })
                
                context("when an item is loaded (playWhenReady=true)", {
                    beforeEach {
                        try? audioPlayer.load(item: Source.getAudioItem(), playWhenReady: true)
                    }
                    
                    it("it should eventually be playing", closure: {
                        expect(audioPlayer.playerState).toEventually(equal(AudioPlayerState.playing))
                    })
                })
                
                context("when playing an item", {
                    var listener: AudioPlayerEventListener!
                    beforeEach {
                        listener = AudioPlayerEventListener(audioPlayer: audioPlayer)
                        listener.stateUpdate = { state in
                            if state == .ready {
                                audioPlayer.play()
                            }
                        }
                        try? audioPlayer.load(item: Source.getAudioItem(), playWhenReady: false)
                    }
                    
                    it("should eventually be playing", closure: {
                        expect(audioPlayer.playerState).toEventually(equal(AudioPlayerState.playing))
                    })
                })
                
                context("when pausing an item", {
                    var listener: AudioPlayerEventListener!
                    beforeEach {
                        listener = AudioPlayerEventListener(audioPlayer: audioPlayer)
                        listener.stateUpdate = { (state) in
                            if state == .playing {
                                audioPlayer.pause()
                            }
                        }
                        try? audioPlayer.load(item: Source.getAudioItem(), playWhenReady: true)
                    }
                    
                    it("should eventually be paused", closure: {
                        expect(audioPlayer.playerState).toEventually(equal(AudioPlayerState.paused))
                    })
                })
                
                context("when stopping an item", {
                    var listener: AudioPlayerEventListener!
                    beforeEach {
                        listener = AudioPlayerEventListener(audioPlayer: audioPlayer)
                        listener.stateUpdate = { (state) in
                            if state == .playing {
                                audioPlayer.stop()
                            }
                        }
                        try? audioPlayer.load(item: Source.getAudioItem(), playWhenReady: true)
                    }
                    
                    it("should eventually be idle", closure: {
                        expect(audioPlayer.playerState).toEventually(equal(AudioPlayerState.idle))
                    })
                })
                
            })
            
            describe("its current time", {
                it("should be 0", closure: {
                    expect(audioPlayer.currentTime).to(equal(0))
                })
                
                context("when seeking to a time", {
                    let seekTime: TimeInterval = 1.0
                    beforeEach {
                        try? audioPlayer.load(item: Source.getAudioItem(), playWhenReady: false)
                        audioPlayer.seek(to: seekTime)
                    }
                    
                    it("should eventually be equal to the seeked time", closure: {
                        expect(audioPlayer.currentTime).toEventually(equal(seekTime))
                    })
                })
                
                context("when playing an item with an initial time", {
                    var item: DefaultAudioItemInitialTime!
                    beforeEach {
                        item = DefaultAudioItemInitialTime(audioUrl: LongSource.path, artist: nil, title: nil, albumTitle: nil, sourceType: .file, artwork: nil, initialTime: 4.0)
                        try? audioPlayer.load(item: item, playWhenReady: false)
                    }
                    
                    it("should eventaully be equal to the initial time", closure: {
                        expect(audioPlayer.currentTime).toEventually(equal(item.getInitialTime()))
                    })
                })
            })
            
            describe("its rate", {
                it("should be 0", closure: {
                    expect(audioPlayer.rate).to(equal(0))
                })
                
                context("when playing an item", {
                    beforeEach {
                        try? audioPlayer.load(item: Source.getAudioItem(), playWhenReady: true)
                    }
                    
                    it("should eventually be 1.0", closure: {
                        expect(audioPlayer.rate).toEventually(equal(1.0))
                    })
                })
            })
            
            describe("its currentItem", {
                it("should be nil", closure: {
                    expect(audioPlayer.currentItem).to(beNil())
                })
                
                context("when loading an item", {
                    beforeEach {
                        try? audioPlayer.load(item: Source.getAudioItem(), playWhenReady: false)
                    }
                    
                    it("should not be nil", closure: {
                        expect(audioPlayer.currentItem).toNot(beNil())
                    })
                })
                
                context("when setting the timePitchAlgorithm", {
                    
                    beforeEach {
                        audioPlayer.audioTimePitchAlgorithm = .timeDomain
                    }
                    
                    context("then loading an item", {
                        beforeEach {
                            try? audioPlayer.load(item: Source.getAudioItem(), playWhenReady: false)
                        }
                        
                        it("should have the applied timePitchAlgorithm", closure: {
                            expect(audioPlayer.wrapper.currentItem?.audioTimePitchAlgorithm).to(equal(AVAudioTimePitchAlgorithm.timeDomain))
                        })
                    })
                    
                    context("then loading a timepitching item", {
                        beforeEach {
                            let item = DefaultAudioItemTimePitching(audioUrl: Source.path, artist: nil, title: nil, albumTitle: nil, sourceType: .file, artwork: nil, audioTimePitchAlgorithm: AVAudioTimePitchAlgorithm.spectral)
                            try? audioPlayer.load(item: item, playWhenReady: false)
                        }
                        
                        it("should have the applied timePitchAlgorithm", closure: {
                            expect(audioPlayer.wrapper.currentItem?.audioTimePitchAlgorithm).to(equal(AVAudioTimePitchAlgorithm.spectral))
                        })
                    })
                })
            })
        }
    }
    
}

class AudioPlayerEventListener {
    
    var state: AudioPlayerState? {
        didSet {
            if let state = state {
                stateUpdate?(state)
            }
        }
    }
    
    var stateUpdate: ((_ state: AudioPlayerState) -> Void)?
    var seekCompletion: (() -> Void)?
    
    init(audioPlayer: AudioPlayer) {
        audioPlayer.event.stateChange.addListener(self, handleDidUpdateState)
        audioPlayer.event.seek.addListener(self, handleSeek)
    }
    
    func handleDidUpdateState(state: AudioPlayerState) {
        self.state = state
    }
    
    func handleSeek(data: AudioPlayer.SeekEventData) {
        seekCompletion?()
    }
    
}
