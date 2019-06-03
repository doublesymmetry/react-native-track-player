import Quick
import Nimble
import AVFoundation

@testable import SwiftAudio


class AVPlayerObserverTests: QuickSpec, AVPlayerObserverDelegate {
    
    var status: AVPlayer.Status?
    var timeControlStatus: AVPlayer.TimeControlStatus?
    
    override func spec() {
        
        describe("A player observer") {
            
            var player: AVPlayer!
            var observer: AVPlayerObserver!
            
            beforeEach {
                player = AVPlayer()
                player.volume = 0.0
                observer = AVPlayerObserver()
                observer.player = player
                observer.delegate = self
            }
            
            it("should not be observing", closure: {
                expect(observer.isObserving).to(beFalse())
            })
            
            context("when observing has started", {
                beforeEach {
                    observer.startObserving()
                }
                
                it("should be observing", closure: {
                    expect(observer.isObserving).toEventually(beTrue())
                })
                
                context("when player has started", {
                    beforeEach {
                        player.replaceCurrentItem(with: AVPlayerItem(url: URL(fileURLWithPath: Source.path)))
                        player.play()
                    }
                    
                    it("it should update the delegate", closure: {
                        expect(self.status).toEventuallyNot(beNil())
                        expect(self.timeControlStatus).toEventuallyNot(beNil())
                    })
                })
                
                context("when observing again", {
                    beforeEach {
                        observer.startObserving()
                    }
                    
                    it("should be observing", closure: {
                        expect(observer.isObserving).toEventually(beTrue())
                    })
                })
                
                context("when stopping observing", closure: {
                    
                    beforeEach {
                        observer.stopObserving()
                    }
                    
                    it("should not be observing", closure: {
                        expect(observer.isObserving).to(beFalse())
                    })
                })
            })
            
        }
    }
    
    func player(statusDidChange status: AVPlayer.Status) {
        self.status = status
    }
    
    func player(didChangeTimeControlStatus status: AVPlayer.TimeControlStatus) {
        self.timeControlStatus = status
    }
    
}
