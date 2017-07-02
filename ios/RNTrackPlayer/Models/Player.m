//
//  Player.m
//  RNTrackPlayer
//
//  Created by David Chavez on 7/2/17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

#import "Player.h"
#import "STKAudioPlayer.h"

@interface Player() <STKAudioPlayerDelegate>
@property(strong, nonatomic) STKAudioPlayer *player;
@property(strong, nonatomic) NSMutableArray *queue;
@end

@implementation Player

- (id)init {
    self = [super init];
    if (self) {
        _player = [[STKAudioPlayer alloc] init];
        _queue = [[NSMutableArray alloc] init];
    }
    
    return self;
}

- (void)dealloc {
    [_player dispose];
}


// MARK: - Public Interface

- (void)load:(Track *)track {
    // TODO: - Implement preloading (this would be the perfect time)
    [self reset];
    [_queue insertObject:track atIndex:0];
}

- (void)reset {
    [_queue removeAllObjects];
    [_player stop];
}

- (void)play {
    if ([_queue count] < 1) { return; }
    
    // Resume playback if it was paused
    if ([_player state] == STKAudioPlayerStatePaused) {
        [_player resume];
        return;
    }
    
    Track *nextTrack = [_queue firstObject];
    
    // TODO: - Update system music metadata
    
    [_player playURL:[[nextTrack url] value]];
}

- (void)pause {
    [_player pause];
}

- (void)stop {
    [_player stop];
}

- (void)seekToTime:(NSInteger)time {
    [_player seekToTime:time];
}

- (void)setVolume:(NSInteger)volume {
    [_player setVolume:volume];
}

- (Track *)currentTrack {
    return [_queue firstObject];
}

- (NSInteger)duration {
    return [_player duration];
}

- (NSInteger)position {
    return [_player progress];
}

- (NSInteger)state {
    // Mapping to values used by Android project
    // TODO: - Unify into a shared enum
    switch ([_player state]) {
        case STKAudioPlayerStateReady:
            return 3;
        case STKAudioPlayerStateBuffering:
            return 6;
        case STKAudioPlayerStatePaused:
            return 2;
        case STKAudioPlayerStateStopped:
            return 1;
        default:
            return 0;
    }
}


// MARK: - STKAudioPlayerDelegate

- (void)audioPlayer:(STKAudioPlayer *)audioPlayer didFinishBufferingSourceWithQueueItemId:(NSObject *)queueItemId {}

- (void)audioPlayer:(STKAudioPlayer *)audioPlayer didFinishPlayingQueueItemId:(NSObject *)queueItemId withReason:(STKAudioPlayerStopReason)stopReason andProgress:(double)progress andDuration:(double)duration {
    // Play the next song in the queue when song ends
    if (stopReason == STKAudioPlayerStopReasonEof) {
        [_queue removeObjectAtIndex:0];
        [self play];
    }
}

- (void)audioPlayer:(STKAudioPlayer *)audioPlayer didStartPlayingQueueItemId:(NSObject *)queueItemId {}

- (void)audioPlayer:(STKAudioPlayer *)audioPlayer stateChanged:(STKAudioPlayerState)state previousState:(STKAudioPlayerState)previousState {}

- (void)audioPlayer:(STKAudioPlayer *)audioPlayer unexpectedError:(STKAudioPlayerErrorCode)errorCode {}

@end
