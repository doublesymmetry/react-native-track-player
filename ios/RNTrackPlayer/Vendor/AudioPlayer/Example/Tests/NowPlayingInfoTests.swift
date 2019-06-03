import Quick
import Nimble
import MediaPlayer

@testable import SwiftAudio

/// Tests that the AudioPlayer is automatically updating the values it should update in the NowPlayingInfoController.
class NowPlayingInfoTests: QuickSpec {
    
    override func spec() {
        
        describe("An AudioPlayer") {
            
            var audioPlayer: AudioPlayer!
            var nowPlayingController: NowPlayingInfoController_Mock!
            
            beforeEach {
                nowPlayingController = NowPlayingInfoController_Mock()
                audioPlayer = AudioPlayer(nowPlayingInfoController: nowPlayingController)
                audioPlayer.automaticallyUpdateNowPlayingInfo = true
                audioPlayer.volume = 0
            }
            
            describe("its NowPlayingInfoController", {
                
                context("when loading an AudioItem", {
                    
                    var item: AudioItem!
                    
                    beforeEach {
                        item = Source.getAudioItem()
                        try? audioPlayer.load(item: item, playWhenReady: false)
                    }
                    
                    it("should eventually be updated with meta data", closure: {
                        expect(nowPlayingController.getTitle()).toEventuallyNot(beNil())
                        expect(nowPlayingController.getTitle()).toEventually(equal(item.getTitle()!))
                        
                        expect(nowPlayingController.getArtist()).toEventuallyNot(beNil())
                        expect(nowPlayingController.getArtist()).toEventually(equal(item.getArtist()!))
                        
                        expect(nowPlayingController.getAlbumTitle()).toEventuallyNot(beNil())
                        expect(nowPlayingController.getAlbumTitle()).toEventually(equal(item.getAlbumTitle()!))
                        
                        expect(nowPlayingController.getArtwork()).toEventuallyNot(beNil())
                    })
                    
                })
                
                context("when playing an AudioItem", {
                    
                    var item: AudioItem!
                    
                    beforeEach {
                        item = LongSource.getAudioItem()
                        try? audioPlayer.load(item: item, playWhenReady: true)
                    }
                    
                    it("should eventually be updated with playback values", closure: {
                        expect(nowPlayingController.getRate()).toEventuallyNot(beNil())
                        expect(nowPlayingController.getDuration()).toEventuallyNot(beNil())
                        expect(nowPlayingController.getCurrentTime()).toEventuallyNot(beNil())
                    })
                    
                })
                
            })
            
        }
        
    }
    
}
