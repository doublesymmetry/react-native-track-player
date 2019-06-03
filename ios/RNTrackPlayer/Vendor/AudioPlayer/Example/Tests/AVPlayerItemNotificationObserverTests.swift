import Quick
import Nimble
import AVFoundation

@testable import SwiftAudio


class AVPlayerItemNotificationObserverTests: QuickSpec {
    
    override func spec() {
        
        describe("A notification observer") {
            
            var item: AVPlayerItem!
            var observer: AVPlayerItemNotificationObserver!
        
            beforeEach {
                item = AVPlayerItem(url: URL(fileURLWithPath: Source.path))
                observer = AVPlayerItemNotificationObserver()
            }
            
            context("when started observing", {
                beforeEach {
                    observer.startObserving(item: item)
                }
                
                it("should have an observed item", closure: {
                    expect(observer.observingItem).toNot(beNil())
                })
                
                context("when ended observing", {
                    
                    beforeEach {
                        observer.stopObservingCurrentItem()
                    }
                    
                    it("should have no observed item", closure: {
                        expect(observer.observingItem).to(beNil())
                    })
                    
                })
            })
            
        }

    }
    
}
