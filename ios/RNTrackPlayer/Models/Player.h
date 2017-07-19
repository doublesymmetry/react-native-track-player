//
//  Player.h
//  RNTrackPlayer
//
//  Created by David Chavez on 7/2/17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

@import Foundation;

#import "Track.h"

@interface Player: NSObject
- (BOOL)queueContainsTrack:(NSString *)trackId;
- (void)addTracks:(NSArray *)tracks before:(NSString *)trackId;
- (void)removeTrackIds:(NSArray *)trackIds;
- (void)skipToTrack:(NSString *)trackId;
- (BOOL)playNext;
- (BOOL)playPrevious;
- (void)reset;
- (void)play;
- (void)pause;
- (void)stop;
- (void)seekToTime:(NSInteger)time;
- (void)setVolume:(NSInteger)volume;
- (Track *)currentTrack;
- (NSInteger)duration;
- (NSInteger)position;
- (NSString *)state;
@end
