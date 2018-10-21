//
//  RNTrackPlayer.swift
//  exampleTests
//
//  Created by David Chavez on 13.04.18.
//  Copyright © 2018 Facebook. All rights reserved.
//

import Quick
import Nimble
import MediaPlayer
@testable import RNTrackPlayer

let correctTrack = [
    "id": "test-correct",
    "title": "test-title",
    "artist": "test-artist",
    "url": "http://test.com"
]

let anotherCorrectTrack = [
    "id": "test-correct-2",
    "title": "test-title",
    "artist": "test-artist",
    "url": "http://test.com"
]

let incompleteTrack = [
    "id": "test-wrong",
    "title": "test-title",
    "artist": "test-artist",
]

class RNTrackPlayerSpec: QuickSpec {
    override func spec() {
        describe(".constantsToExport") {
            it("has 20 exported constants") {
                let module = RNTrackPlayer()
                let constants = module.constantsToExport()
                
                expect(constants).to(haveCount(20))
                expect(constants).to(allPass { $0?.value as? String != nil })
            }
        }
        
        describe(".supportedEvents") {
            it("supports 12 events") {
                let module = RNTrackPlayer()
                let events = module.supportedEvents()
                
                expect(events).to(haveCount(12))
            }
        }
        
        describe(".update") {
            it("handles empty capabilities correctly: 1") {
                let module = RNTrackPlayer()
                let remoteCenter = MPRemoteCommandCenter.shared()
                
                module.update(options: [:], resolve: { _ in }, reject: { _ in fail() })
                
                expect(remoteCenter.playCommand.isEnabled).to(beFalse())
                expect(remoteCenter.pauseCommand.isEnabled).to(beFalse())
                expect(remoteCenter.stopCommand.isEnabled).to(beFalse())
                expect(remoteCenter.nextTrackCommand.isEnabled).to(beFalse())
                expect(remoteCenter.previousTrackCommand.isEnabled).to(beFalse())
            }
            it("handles empty capabilities correctly: 2") {
                let module = RNTrackPlayer()
                let remoteCenter = MPRemoteCommandCenter.shared()
                
                module.update(options: [
                    "capabilities": []
                ], resolve: { _ in }, reject: { _ in fail() })
                
                expect(remoteCenter.playCommand.isEnabled).to(beFalse())
                expect(remoteCenter.pauseCommand.isEnabled).to(beFalse())
                expect(remoteCenter.stopCommand.isEnabled).to(beFalse())
                expect(remoteCenter.nextTrackCommand.isEnabled).to(beFalse())
                expect(remoteCenter.previousTrackCommand.isEnabled).to(beFalse())
            }
            it("handles unsupported capability") {
                let module = RNTrackPlayer()
                let constants = module.constantsToExport()
                let remoteCenter = MPRemoteCommandCenter.shared()
                
                module.update(options: [
                    "capabilities": [
                        constants["UNSUPPORTED"]
                    ]
                ], resolve: { _ in }, reject: { _ in fail() })
                
                expect(remoteCenter.playCommand.isEnabled).to(beFalse())
                expect(remoteCenter.pauseCommand.isEnabled).to(beFalse())
                expect(remoteCenter.stopCommand.isEnabled).to(beFalse())
                expect(remoteCenter.nextTrackCommand.isEnabled).to(beFalse())
                expect(remoteCenter.previousTrackCommand.isEnabled).to(beFalse())
            }
            it("maps partial capabilities correctly: 1") {
                let module = RNTrackPlayer()
                let constants = module.constantsToExport()
                let remoteCenter = MPRemoteCommandCenter.shared()
                
                module.update(options: [
                    "capabilities": [
                        constants["CAPABILITY_PLAY"],
                        constants["CAPABILITY_PAUSE"],
                        constants["CAPABILITY_SKIP_TO_NEXT"],
                        constants["CAPABILITY_SKIP_TO_PREVIOUS"],
                        constants["CAPABILITY_SEEK_TO"],
                    ]
                ], resolve: { _ in }, reject: { _ in fail() })
                
                expect(remoteCenter.playCommand.isEnabled).to(beTrue())
                expect(remoteCenter.pauseCommand.isEnabled).to(beTrue())
                expect(remoteCenter.stopCommand.isEnabled).to(beFalse())
                expect(remoteCenter.nextTrackCommand.isEnabled).to(beTrue())
                expect(remoteCenter.previousTrackCommand.isEnabled).to(beTrue())
            }
            it("maps capabilities correctly: 2") {
                let module = RNTrackPlayer()
                let constants = module.constantsToExport()
                let remoteCenter = MPRemoteCommandCenter.shared()
                
                module.update(options: [
                    "capabilities": [
                        constants["CAPABILITY_SKIP_TO_NEXT"],
                        constants["CAPABILITY_SKIP_TO_PREVIOUS"],
                        constants["CAPABILITY_SEEK_TO"],
                    ]
                ], resolve: { _ in }, reject: { _ in fail() })
                
                expect(remoteCenter.playCommand.isEnabled).to(beFalse())
                expect(remoteCenter.pauseCommand.isEnabled).to(beFalse())
                expect(remoteCenter.stopCommand.isEnabled).to(beFalse())
                expect(remoteCenter.nextTrackCommand.isEnabled).to(beTrue())
                expect(remoteCenter.previousTrackCommand.isEnabled).to(beTrue())
            }
        }
        
        describe(".add") {
            it("adds correctly formatted tracks") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in fail() }
                
                module.getQueue(resolve: { queue in
                    let castedQueue = queue as? [[String: Any]]
                    expect(castedQueue).to(haveCount(2))
                }) { _ in fail() }
            }
            
            it("rejects when given a track with wrong format") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [incompleteTrack], before: nil, resolve: { _ in }) { code, _, _ in
                    expect(code).to(equal("invalid_track_object"))
                }
                
                module.getQueue(resolve: { queue in
                    let castedQueue = queue as? [[String: Any]]
                    expect(castedQueue).to(haveCount(0))
                }) { _ in fail() }
            }
          
            it("updates current track index correctly if items are inserted before current track") {
                let module = RNTrackPlayer()
                
                var extraItem = correctTrack
                extraItem["id"] = "test-correct-3"
                module.add(trackDicts: [correctTrack, extraItem], before: nil, resolve: { _ in }) { _ in fail() }
                module.play(resolve: { _ in }, reject: { _ in fail() })
              
                module.add(trackDicts: [anotherCorrectTrack], before: "test-correct", resolve: { _ in }) { _ in fail() }
                module.skipToNext(resolve: { _ in }) { _ in }
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct-3"))
                }) { _ in }
            }
            
            it("inserts before given ID") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in fail() }
                
                var middleItem = correctTrack
                middleItem["id"] = "test-correct-3"
                module.add(trackDicts: [middleItem], before: "test-correct-2", resolve: { _ in }) { _ in fail() }
                
                module.getQueue(resolve: { queue in
                    let castedQueue = queue as! [[String: Any]]
                    expect(castedQueue).to(haveCount(3))
                    expect(castedQueue[1]["id"] as? String).to(equal("test-correct-3"))
                }) { _ in fail() }
            }
            
            it("rejects when given id to insert before is not found") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack], before: nil, resolve: { _ in }) { _ in fail() }
                
                module.add(trackDicts: [anotherCorrectTrack], before: "test-correct-3", resolve: { _ in fail() }) { code, _, _ in
                    expect(code).to(equal("track_not_in_queue"))
                }
                
                module.getQueue(resolve: { queue in
                    let castedQueue = queue as? [[String: Any]]
                    expect(castedQueue).to(haveCount(1))
                }) { _ in }
            }
        }
        
        describe(".remove") {
            it("removes all valid track id's and ignores invalid ones") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.remove(tracks: ["test-correct", "test-correct-3", "test-correct-2"], resolve: { _ in }) { _ in fail() }
                
                module.getQueue(resolve: { queue in
                    let castedQueue = queue as? [[String: Any]]
                    expect(castedQueue).to(haveCount(0))
                }) { _ in }
            }
            
            it("correctly adjusts current index when previous items are removed") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                module.skipToNext(resolve: { _ in }) { _ in }
                
                module.remove(tracks: ["test-correct"], resolve: { _ in }) { _ in fail() }
                
                module.getQueue(resolve: { queue in
                    let castedQueue = queue as? [[String: Any]]
                    expect(castedQueue).to(haveCount(1))
                }) { _ in }
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct-2"))
                }) { _ in }
            }
            
            it("stops playback if current track is last in queue and is removed") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                module.skipToNext(resolve: { _ in }) { _ in }
                
                module.remove(tracks: ["test-correct-2"], resolve: { _ in }) { _ in fail() }
                
                module.getQueue(resolve: { queue in
                    let castedQueue = queue as? [[String: Any]]
                    expect(castedQueue).to(haveCount(1))
                }) { _ in }
                
                module.getState(resolve: { state in
                    expect(state as? String).to(equal(MediaWrapper.PlaybackState.stopped.rawValue))
                }) { _ in }
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId).to(beNil())
                }) { _ in }
            }
            
            it("continues to next track if current track is removed and it wasn't the last") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                
                module.remove(tracks: ["test-correct"], resolve: { _ in }) { _ in fail() }
                
                module.getQueue(resolve: { queue in
                    let castedQueue = queue as? [[String: Any]]
                    expect(castedQueue).to(haveCount(1))
                }) { _ in }
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct-2"))
                }) { _ in }
            }
          
          it("correctly handles current track being removed as well as others") {
              let module = RNTrackPlayer()
            
              var middleItem = correctTrack
              middleItem["id"] = "test-correct-3"
              var lastItem = correctTrack
              lastItem["id"] = "test-correct-4"
              module.add(trackDicts: [correctTrack, middleItem, anotherCorrectTrack, lastItem], before: nil, resolve: { _ in }) { _ in }
              module.play(resolve: { _ in }, reject: { _ in fail() })
              module.skipToNext(resolve: { _ in }) { _ in }
            
              module.remove(tracks: ["test-correct-3", "test-correct"], resolve: { _ in }) { _ in fail() }
            
              module.getQueue(resolve: { queue in
                let castedQueue = queue as? [[String: Any]]
                expect(castedQueue).to(haveCount(2))
              }) { _ in }
            
              module.getCurrentTrack(resolve: { trackId in
                expect(trackId as? String).to(equal("test-correct-2"))
              }) { _ in }
          }
          
          it("correcly handles current track becoming last track, then being removed") {
              let module = RNTrackPlayer()
            
              var middleItem = correctTrack
              middleItem["id"] = "test-correct-3"
              module.add(trackDicts: [correctTrack, middleItem, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
              module.play(resolve: { _ in }, reject: { _ in fail() })
              module.skipToNext(resolve: { _ in }) { _ in }
            
              module.remove(tracks: ["test-correct-2"], resolve: { _ in }) { _ in fail() }
              module.remove(tracks: ["test-correct-3"], resolve: { _ in }) { _ in fail() }
            
              module.getQueue(resolve: { queue in
                let castedQueue = queue as? [[String: Any]]
                expect(castedQueue).to(haveCount(1))
              }) { _ in }
            
              module.getState(resolve: { state in
                expect(state as? String).to(equal(MediaWrapper.PlaybackState.stopped.rawValue))
              }) { _ in }
            
              module.getCurrentTrack(resolve: { trackId in
                expect(trackId).to(beNil())
              }) { _ in }
          }
        }
        
        describe(".removeUpcomingTracks") {
            it("removes all items from queue but does not stop current playback") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                
                module.removeUpcomingTracks(resolve: { _ in }, reject: { _ in fail() })
                module.getQueue(resolve: { queue in
                    let castedQueue = queue as? [[String: Any]]
                    expect(castedQueue).to(haveCount(1))
                }) { _ in }
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct"))
                }) { _ in }
                
                module.add(trackDicts: [anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.getQueue(resolve: { queue in
                    let castedQueue = queue as? [[String: Any]]
                    expect(castedQueue).to(haveCount(2))
                }) { _ in }
                
                module.skipToNext(resolve: { _ in }) { _ in }
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct-2"))
                }) { _ in }
            }
        }
        
        describe(".skip") {
            it("skips to track if given a valid id") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                
                module.skip(to: "test-correct-2", resolve: { _ in }) { _ in fail() }
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct-2"))
                }) { _ in }
            }
            
            it("rejects if given track id is not in queue") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                
                module.skip(to: "test-correct-3", resolve: { _ in }) { code, _, _ in
                    expect(code).to(equal("track_not_in_queue"))
                }
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct"))
                }) { _ in }
            }
        }
        
        describe(".skipToNext") {
            it("skips to next track if possible") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                
                module.skipToNext(resolve: { _ in }) { _ in fail() }
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct-2"))
                }) { _ in }
            }
            
            it("rejects if no more tracks left to play and stops playback") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                
                module.skipToNext(resolve: { _ in }) { code, _, _ in
                    expect(code).to(equal("queue_exhausted"))
                }
                
                module.getState(resolve: { state in
                    expect(state as? String).to(equal(MediaWrapper.PlaybackState.stopped.rawValue))
                }) { _ in }
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId).to(beNil())
                }) { _ in }
            }
        }
        
        describe(".skipToPrevious") {
            it("plays previous track if possible") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                module.skipToNext(resolve: { _ in }) { _ in }
                
                module.skipToPrevious(resolve: { _ in }) { _ in fail() }
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct"))
                }) { _ in }
            }
            
            it("rejects if no tracks before current one and stops playback") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                
                module.skipToPrevious(resolve: { _ in }) { code, _, _ in
                    expect(code).to(equal("no_previous_track"))
                }
                
                module.getState(resolve: { state in
                    expect(state as? String).to(equal(MediaWrapper.PlaybackState.stopped.rawValue))
                }) { _ in }
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId).to(beNil())
                }) { _ in }
            }
        }
        
        describe(".reset") {
            it("clears queue and stops playback") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack], before: nil, resolve: { _ in }) { _ in }
                module.reset(resolve: { _ in }, reject: { _ in fail() })
                
                module.getRate(resolve: { rate in
                    expect(rate as? Float).to(equal(0))
                }) { _ in }
                
                module.getState(resolve: { state in
                    expect(state as? String).to(equal(MediaWrapper.PlaybackState.stopped.rawValue))
                }) { _ in }
                
                module.getQueue(resolve: { queue in
                    let castedQueue = queue as? [[String: Any]]
                    expect(castedQueue).to(haveCount(0))
                }) { _ in }
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId).to(beNil())
                }) { _ in }
            }
        }
        
        describe(".play") {
            it("cannot start playback without tracks") {
                let module = RNTrackPlayer()
                module.play(resolve: { _ in }, reject: { _ in fail() })
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId).to(beNil())
                }) { _ in }
            }
            
            it("correctly starts playback from start") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack], before: nil, resolve: { _ in }) { _ in }
                
                module.play(resolve: { _ in }, reject: { _ in fail() })
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct"))
                }) { _ in }
            }
            
            it("correctly starts playback from start of queue after a having stopped") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [anotherCorrectTrack, correctTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                module.skipToNext(resolve: { _ in }) { _ in }
                module.stop(resolve: { _ in }, reject: { _ in fail() })
                
                module.play(resolve: { _ in }, reject: { _ in fail() })
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct-2"))
                }) { _ in }
            }
            
            it("correctly resumes playback when playing after a pause") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                module.pause(resolve: { _ in }, reject: { _ in fail() })
                
                module.play(resolve: { _ in }, reject: { _ in fail() })
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct"))
                }) { _ in }
            }
        }
        
        describe(".pause") {
            it("correct pauses playback to be ready for resuming") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                
                module.pause(resolve: { _ in }, reject: { _ in fail() })
                module.getState(resolve: { state in
                    expect(state as? String).to(equal(MediaWrapper.PlaybackState.paused.rawValue))
                }) { _ in }
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct"))
                }) { _ in }
            }
        }
        
        describe(".stop") {
            it("correct stops playback and resets current index") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                module.skipToNext(resolve: { _ in }) { _ in }
                
                module.stop(resolve: { _ in }, reject: { _ in fail() })
                module.getState(resolve: { state in
                    expect(state as? String).to(equal(MediaWrapper.PlaybackState.stopped.rawValue))
                }) { _ in }
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId).to(beNil())
                }) { _ in }
                
                module.play(resolve: { _ in }, reject: { _ in fail() })
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct"))
                }) { _ in }
            }
        }
        
        describe(".seek & .getPosition") {
            it("seeks to provided time") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                
                module.seek(to: 5, resolve: { _ in }, reject: { _ in fail() })
                module.getPosition(resolve: { position in
                    expect(position as? Double).to(equal(5))
                }) { _ in }
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct"))
                }) { _ in }
            }
            
            it("does not restart playback if you seek while paused") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                module.pause(resolve: { _ in }, reject: { _ in fail() })
                
                module.seek(to: 5, resolve: { _ in }, reject: { _ in fail() })
                module.getPosition(resolve: { position in
                    expect(position as? Double).to(equal(5))
                }) { _ in }
                
                module.getState(resolve: { state in
                    expect(state as? String).to(equal(MediaWrapper.PlaybackState.paused.rawValue))
                }) { _ in }
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct"))
                }) { _ in }
            }
        }
        
        describe(".setVolume & .getVolume") {
            it("sets volume to given level") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                
                module.setVolume(level: 0.2, resolve: { _ in }, reject: { _ in fail() })
                module.getVolume(resolve: { level in
                    expect(level as? Float).to(equal(0.2))
                }) { _ in }
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct"))
                }) { _ in }
            }
        }
        
        describe(".setRate & .getRate") {
            it("only sets rate if state is playing") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                module.pause(resolve: { _ in }, reject: { _ in fail() })
                
                module.getState(resolve: { state in
                    expect(state as? String).to(equal(MediaWrapper.PlaybackState.paused.rawValue))
                }) { _ in }
                
                module.setRate(rate: 1, resolve: { _ in }, reject: { _ in fail() })
                module.getRate(resolve: { rate in
                    expect(rate as? Float).toNot(equal(1))
                }) { _ in }
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct"))
                }) { _ in }
            }
        }
        
        describe(".getTrack") {
            it("returns the original object added if it exists") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                
                module.getTrack(id: "test-correct", resolve: { track in
                    let mappedTrack = track as! [String: String]
                    expect(mappedTrack["id"]).to(equal("test-correct"))
                    expect(mappedTrack["title"]).to(equal("test-title"))
                    expect(mappedTrack["artist"]).to(equal("test-artist"))
                    expect(mappedTrack["url"]).to(equal("http://test.com"))
                }) { _ in fail() }
            }
            
            it("rejects if given id is not in queue") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.getTrack(id: "test-correct-3", resolve: { _ in fail() }) { code, _, _ in
                    expect(code).to(equal("track_not_in_queue"))
                }
            }
        }
        
        describe(".getQueue") {
            it("returns an array of the original objects") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                
                module.getQueue(resolve: { queue in
                    let castedQueue = queue as! [[String: Any]]
                    let firstTrack = castedQueue[0] as! [String: String]
                    let secondTrack = castedQueue[1] as! [String: String]
                    
                    expect(firstTrack["id"]).to(equal("test-correct"))
                    expect(firstTrack["title"]).to(equal("test-title"))
                    expect(firstTrack["artist"]).to(equal("test-artist"))
                    expect(firstTrack["url"]).to(equal("http://test.com"))
                    
                    expect(secondTrack["id"]).to(equal("test-correct-2"))
                    expect(secondTrack["title"]).to(equal("test-title"))
                    expect(secondTrack["artist"]).to(equal("test-artist"))
                    expect(secondTrack["url"]).to(equal("http://test.com"))
                }) { _ in fail() }
            }
            
            it("returns an empty array if empty queue") {
                let module = RNTrackPlayer()
                module.getQueue(resolve: { queue in
                    expect(queue as? [Any]).to(beEmpty())
                }) { _ in fail() }
            }
        }
        
        describe(".getCurrentTrack") {
            it("returns id of currently playing track") {
                let module = RNTrackPlayer()
                module.add(trackDicts: [correctTrack, anotherCorrectTrack], before: nil, resolve: { _ in }) { _ in }
                module.play(resolve: { _ in }, reject: { _ in fail() })
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId as? String).to(equal("test-correct"))
                }) { _ in fail() }
            }
            
            it("returns nil if no track is playing") {
                let module = RNTrackPlayer()
                
                module.getCurrentTrack(resolve: { trackId in
                    expect(trackId).to(beNil())
                }) { _ in fail() }
            }
        }
    }
}
