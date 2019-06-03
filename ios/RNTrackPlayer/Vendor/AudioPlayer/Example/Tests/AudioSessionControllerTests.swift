import Quick
import Nimble
import AVFoundation

@testable import SwiftAudio

class AudioSessionControllerTests: QuickSpec {
    
    override func spec() {
        
        describe("An AudioSessionController") {
            let audioSessionController: AudioSessionController = AudioSessionController(audioSession: NonFailingAudioSession())
            
            it("should be inactive", closure: {
                expect(audioSessionController.audioSessionIsActive).to(beFalse())
            })
            
            context("when session is activated", {
                beforeEach {
                    try? audioSessionController.activateSession()
                }
                
                it("should be active", closure: {
                    expect(audioSessionController.audioSessionIsActive).to(beTrue())
                })
                
                context("when deactivating session", {
                    beforeEach {
                        try? audioSessionController.deactivateSession()
                    }
                    
                    it("should be inactive", closure: {
                        expect(audioSessionController.audioSessionIsActive).to(beFalse())
                    })
                })
            })
            
            describe("its isObservingForInterruptions", {
                it("should be true", closure: {
                    expect(audioSessionController.isObservingForInterruptions).to(beTrue())
                })
                
                context("when isObservingForInterruptions is set to false", {
                    beforeEach {
                        audioSessionController.isObservingForInterruptions = false
                    }
                    
                    it("should be false", closure: {
                        expect(audioSessionController.isObservingForInterruptions).to(beFalse())
                    })
                })
            })
            
            describe("its delegate", {
                context("when a interruption arrives", {
                    var delegate: AudioSessionControllerDelegateImplementation!
                    beforeEach {
                        let notification = Notification(name: AVAudioSession.interruptionNotification, object: nil, userInfo: [
                            AVAudioSessionInterruptionTypeKey: UInt(0)
                            ])
                        delegate = AudioSessionControllerDelegateImplementation()
                        audioSessionController.delegate = delegate
                        audioSessionController.handleInterruption(notification: notification)
                    }
                    
                    it("should eventually be updated with the interruption type", closure: {
                        expect(delegate.interruptionType).toEventuallyNot(beNil())
                    })
                    
                })
            })
        }
        
        describe("An AudioSessionController with a failing AudioSession") {
            var audioSessionController: AudioSessionController!
            beforeEach {
                audioSessionController = AudioSessionController(audioSession: FailingAudioSession())
            }
            
            context("when activated", {
                beforeEach {
                    try? audioSessionController.activateSession()
                }
                
                it("should be inactive", closure: {
                    expect(audioSessionController.audioSessionIsActive).to(beFalse())
                })
            })
        }
    }
}

class AudioSessionControllerDelegateImplementation: AudioSessionControllerDelegate {
    
    var interruptionType: AVAudioSession.InterruptionType? = nil
    
    func handleInterruption(type: AVAudioSession.InterruptionType) {
        self.interruptionType = type
    }
}
