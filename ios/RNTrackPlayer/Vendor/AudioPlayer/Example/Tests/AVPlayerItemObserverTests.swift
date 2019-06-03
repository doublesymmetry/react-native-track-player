import Quick
import Nimble
import AVFoundation

@testable import SwiftAudio

class AVPlayerItemObserverTests: QuickSpec {
    
    override func spec() {
        
        describe("An AVPlayerItemObserver") {
            var observer: AVPlayerItemObserver!
            beforeEach {
                observer = AVPlayerItemObserver()
            }
            describe("observed item", {
                context("when observing", {
                    var item: AVPlayerItem!
                    beforeEach {
                        item = AVPlayerItem(url: URL(fileURLWithPath: Source.path))
                        observer.startObserving(item: item)
                    }
                    
                    it("should exist", closure: {
                        expect(observer.observingItem).toEventuallyNot(beNil())
                    })
                })
            })
            
            describe("observing status", {
                it("should not be observing", closure: {
                    expect(observer.isObserving).toEventuallyNot(beTrue())
                })
                context("when observing", {
                    var item: AVPlayerItem!
                    beforeEach {
                        item = AVPlayerItem(url: URL(fileURLWithPath: Source.path))
                        observer.startObserving(item: item)
                    }
                    it("should be observing", closure: {
                        expect(observer.isObserving).toEventually(beTrue())
                    })
                })
            })
        }
    }
}

class AVPlayerItemObserverDelegateHolder: AVPlayerItemObserverDelegate {
    
    var updateDuration: ((_ duration: Double) -> Void)?
    
    func item(didUpdateDuration duration: Double) {
        updateDuration?(duration)
    }
}
