//
//  Player.h
//  RNTrackPlayer
//
//  Created by David Chavez on 7/2/17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "Track.h"

@interface Player: NSObject
- (void)load:(Track *)track;
- (void)reset;
- (void)play;
- (void)pause;
- (void)stop;
- (void)seekToTime:(NSInteger)time;
- (void)setVolume:(NSInteger)volume;
- (Track *)currentTrack;
- (NSInteger)duration;
- (NSInteger)position;
@end
