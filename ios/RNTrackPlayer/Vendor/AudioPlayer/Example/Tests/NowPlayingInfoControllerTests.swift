import Quick
import Nimble
import MediaPlayer

@testable import SwiftAudio

class NowPlayingInfoControllerTests: QuickSpec {
    
    override func spec() {
        describe("An NowPlayingInfoController") {
            
            var nowPlayingController: NowPlayingInfoController!
            
            beforeEach {
                nowPlayingController = NowPlayingInfoController(infoCenter: NowPlayingInfoCenter_Mock())
            }
            
            describe("its info dictionary") {
                
                context("when setting a value") {
                    beforeEach {
                        nowPlayingController.set(keyValue: MediaItemProperty.title("Some title"))
                    }
                    
                    it("should not be empty") {
                        expect(nowPlayingController.info.count).toNot(equal(0))
                    }
                    
                    context("then calling clear()") {
                        beforeEach {
                            nowPlayingController.clear()
                        }
                        
                        it("should be empty", closure: {
                            expect(nowPlayingController.info.count).to(equal(0))
                        })
                    }
                }
            }
            
            describe("its info center") {
                
                context("when setting a value") {
                    
                    beforeEach {
                        nowPlayingController.set(keyValue: MediaItemProperty.title("Some title"))
                    }
                    
                    it("should not be nil") {
                        expect(nowPlayingController.infoCenter.nowPlayingInfo).toNot(beNil())
                    }
                    
                    it("should not be empty") {
                        expect(nowPlayingController.infoCenter.nowPlayingInfo?.count).toNot(equal(0))
                    }
                    
                    context("then calling clear()") {
                        
                        beforeEach {
                            nowPlayingController.clear()
                        }
                        
                        it("should be empty", closure: {
                            expect(nowPlayingController.infoCenter.nowPlayingInfo?.count).to(equal(0))
                        })
                    }
                }
            }
        }
    }
    
}
