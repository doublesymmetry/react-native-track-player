import Quick
import Nimble
import AVFoundation

@testable import SwiftAudio


class AVPlayerWrapperTests: QuickSpec {

    override func spec() {

        describe("An AVPlayerWrapper") {

            var wrapper: AVPlayerWrapper!

            beforeEach {
                wrapper = AVPlayerWrapper()
                wrapper.automaticallyWaitsToMinimizeStalling = false
                wrapper.volume = 0.0
                wrapper.bufferDuration = 0.0001
            }

            describe("its state", {
                it("should be idle", closure: {
                    expect(wrapper.state).to(equal(AVPlayerWrapperState.idle))
                })

                context("when loading a source", {
                    beforeEach {
                        wrapper.load(from: URL(fileURLWithPath: Source.path), playWhenReady: false)
                    }

                    it("should eventually be ready", closure: {
                        expect(wrapper.state).toEventually(equal(AVPlayerWrapperState.ready))
                    })
                })
                
                context("when playing with no source", {
                    beforeEach {
                        wrapper.play()
                    }
                    it("should be idle", closure: {
                        expect(wrapper.state).to(equal(AVPlayerWrapperState.idle))
                    })
                })

                context("when playing a source", {
                    beforeEach {
                        wrapper.load(from: URL(fileURLWithPath: Source.path), playWhenReady: true)
                    }

                    it("should eventually be playing", closure: {
                        expect(wrapper.state).toEventually(equal(AVPlayerWrapperState.playing))
                    })

                })

                context("when pausing the source", {

                    let holder = AVPlayerWrapperDelegateHolder()

                    beforeEach {
                        wrapper.delegate = holder
                        holder.stateUpdate = { (state) in
                            if state == .playing {
                                wrapper.pause()
                            }
                        }
                        wrapper.load(from: URL(fileURLWithPath: Source.path), playWhenReady: true)
                    }

                    it("should eventually be paused", closure: {
                        expect(wrapper.state).toEventually(equal(AVPlayerWrapperState.paused))
                    })
                })
                
                context("when toggling the source from play", {
                    let holder = AVPlayerWrapperDelegateHolder()
                    beforeEach {
                        wrapper.delegate = holder
                        holder.stateUpdate = { (state) in
                            if state == .playing {
                                wrapper.togglePlaying()
                            }
                        }
                        wrapper.load(from: URL(fileURLWithPath: Source.path), playWhenReady: true)
                    }
                    it("should eventually be paused", closure: {
                        expect(wrapper.state).toEventually(equal(AVPlayerWrapperState.paused))
                    })
                })

                context("when stopping the source", {

                    var holder: AVPlayerWrapperDelegateHolder!
                    var receivedIdleUpdate: Bool = false

                    beforeEach {
                        holder = AVPlayerWrapperDelegateHolder()
                        wrapper.delegate = holder
                        holder.stateUpdate = { (state) in
                            if state == .playing {
                                wrapper.stop()
                            }
                            if state == .idle {
                                receivedIdleUpdate = true
                            }
                        }
                        wrapper.load(from: URL(fileURLWithPath: Source.path), playWhenReady: true)
                    }

                    it("should eventually be 'idle'", closure: {
                        expect(receivedIdleUpdate).toEventually(beTrue())
                    })

                })
                
                context("when seeking before loading", {
                    beforeEach {
                        wrapper.seek(to: 10)
                    }
                    it("should be idle", closure: {
                        expect(wrapper.state).to(equal(AVPlayerWrapperState.idle))
                    })
                })
                
                context("when loading source with initial time", closure: {
                    let initialTime: TimeInterval = 4.0
                    beforeEach {
                        wrapper.load(from: LongSource.url, playWhenReady: true, initialTime: initialTime)
                    }
                    
                    it("should eventually be playing", closure: {
                        expect(wrapper.state).toEventually(equal(AVPlayerWrapperState.playing))
                    })
                })
            })
            
            describe("its duration", {
                it("should be 0", closure: {
                    expect(wrapper.duration).to(equal(0))
                })
                
                context("when loading source", {
                    beforeEach {
                        wrapper.load(from: URL(fileURLWithPath: LongSource.path), playWhenReady: false)
                    }
                    it("should eventually not be 0", closure: {
                        expect(wrapper.duration).toEventuallyNot(equal(0))
                    })
                })
            })
            
            describe("its current time", {
                it("should be 0", closure: {
                    expect(wrapper.currentTime).to(equal(0))
                })
                
                context("when seeking to a time", {
                    let holder = AVPlayerWrapperDelegateHolder()
                    let seekTime: TimeInterval = 0.5
                    beforeEach {
                        wrapper.delegate = holder
                        wrapper.load(from: Source.url, playWhenReady: false)
                        wrapper.seek(to: seekTime)
                    }
                    
                    it("should eventually be equal to the seeked time", closure: {
                        expect(wrapper.currentTime).toEventually(equal(seekTime))
                    })
                })
                
                context("when playing from initial time", closure: {
                    let initialTime: TimeInterval = 4.0
                    beforeEach {
                        wrapper.load(from: LongSource.url, playWhenReady: false, initialTime: initialTime)
                    }
                    
                    it("should eventuallt be equal to the initial time", closure: {
                        expect(wrapper.currentTime).toEventually(equal(initialTime))
                    })
                })
            })
            
            describe("its rate", {
                it("should be 0", closure: {
                    expect(wrapper.rate).to(equal(0.0))
                })
                
                context("when playing a source", {
                    beforeEach {
                        wrapper.load(from: URL(fileURLWithPath: Source.path), playWhenReady: true)
                    }
                    
                    it("should eventually be 1.0", closure: {
                        expect(wrapper.rate).toEventually(equal(1.0))
                    })
                    
                })
            })
            
            describe("its automaticallyWaitsToMinimizeStalling option", {
                it("should be false", closure: {
                    expect(wrapper.automaticallyWaitsToMinimizeStalling).to(beFalse())
                })
                
                context("when setting it to true", {
                    beforeEach {
                        wrapper.automaticallyWaitsToMinimizeStalling = true
                    }
                    
                    it("should be true", closure: {
                        expect(wrapper.automaticallyWaitsToMinimizeStalling).to(beTrue())
                    })
                })
            })
            
            describe("its timeEventFrequency", {
                context("when updated", {
                    beforeEach {
                        wrapper.timeEventFrequency = .everyHalfSecond
                    }
                    
                    it("should update the playerTimeObservers periodicObserverTimeInterval", closure: {
                        expect(wrapper.playerTimeObserver.periodicObserverTimeInterval).to(equal(TimeEventFrequency.everyHalfSecond.getTime()))
                    })
                })
            })
            
        }

    }

}

class AVPlayerWrapperDelegateHolder: AVPlayerWrapperDelegate {
    func AVWrapperDidRecreateAVPlayer() {
        
    }
    
    func AVWrapperItemDidPlayToEndTime() {
        
    }
    
    var state: AVPlayerWrapperState? {
        didSet {
            if let state = state {
                self.stateUpdate?(state)
            }
        }
    }
    
    var stateUpdate: ((_ state: AVPlayerWrapperState) -> Void)?
    var itemDidComplete: (() -> Void)?
    
    func AVWrapper(didChangeState state: AVPlayerWrapperState) {
        self.state = state
    }
    
    func AVWrapper(secondsElapsed seconds: Double) {
        
    }
    
    func AVWrapper(failedWithError error: Error?) {
        
    }
    
    var seekCompletion: (() -> Void)?
    func AVWrapper(seekTo seconds: Int, didFinish: Bool) {
         seekCompletion?()
    }
    
    func AVWrapper(didUpdateDuration duration: Double) {
        if let state = self.state {
            self.stateUpdate?(state)

        }
    }
    
}
