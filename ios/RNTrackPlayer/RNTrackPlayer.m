//
//  RNTrackPlayer.m
//  RNTrackPlayer
//
//  Created by David Chavez on 7/1/17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

#import <AVFoundation/AVFoundation.h>

#import "Track.h"
#import "Player.h"
#import "RNTrackPlayer.h"
#import "STKAudioPlayer.h"

@interface RNTrackPlayer()
@property(strong, nonatomic) Player *player;
@end

@implementation RNTrackPlayer

-(id)init {
    self = [super init];
    if (self)  {
        _player = [[Player alloc] init];
    }
    
    return self;
}

RCT_EXPORT_MODULE(TrackPlayerModule)

RCT_EXPORT_METHOD(onReady:(RCTResponseSenderBlock)callback) {
    callback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(setupPlayer:(NSDictionary *)data
                  promise:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    // Setup audio session
    NSError *error;
    
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:&error];
    [[AVAudioSession sharedInstance] setActive:YES error:&error];
    
    if (error) {
        reject(@"failed_setup_audio_session", @"Failed to setup audio session", error);
    }
    
    // TODO: Setup player with custom properties
}

RCT_EXPORT_METHOD(destroy) {
    NSLog(@"Destroying player");
    _player = nil;
}

RCT_EXPORT_METHOD(setOptions:(NSDictionary *)options) {
    // TODO: - Implement
}

// TODO: Change signature to addTracks beforeTrackId
RCT_EXPORT_METHOD(add:(NSString *)beforeTrackId
                  tracks:(NSArray *)trackDicts
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    if (![_player queueContainsTrack:beforeTrackId]) {
        reject(@"track_not_in_queue", @"Given track ID was not found in queue", nil);
        return;
    }
    
    NSMutableArray *tracks = [[NSMutableArray alloc] init];
    for (id trackDict in trackDicts) {
        Track *track = [[Track alloc] initWithDictionary:trackDict];
        [tracks addObject:track];
    }
    
    NSLog(@"Adding tracks: %@", tracks);
    [_player addTracks:tracks before:beforeTrackId];
    
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(remove:(NSArray *)trackIds
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    NSLog(@"Removing tracks: %@", trackIds);
    [_player removeTrackIds:trackIds];
    
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(skip:(NSString *)trackId
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    if (![_player queueContainsTrack:trackId]) {
        reject(@"track_not_in_queue", @"Given track ID was not found in queue", nil);
        return;
    }
    
    NSLog(@"Skipping to track %@", trackId);
    [_player skipToTrack:trackId];
    
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(skipToNext:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    NSLog(@"Skipping to next track");
    if ([_player playNext]) {
        resolve([NSNull null]);
    } else {
        reject(@"queue_exhausted", @"There is no tracks left to play", nil);
    }
}

RCT_EXPORT_METHOD(skipToPrevious:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    NSLog(@"Skipping to previous track");
    if ([_player playPrevious]) {
        resolve([NSNull null]);
    } else {
        reject(@"no_previous_track", @"There is no previous track", nil);
    }
}

RCT_EXPORT_METHOD(load:(NSDictionary *)trackDict
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    // TODO: - Resolve what this method will do with Android API revamp
    
    Track *track = [[Track alloc] initWithDictionary:trackDict];
    
    NSLog(@"Loading track: %@", trackDict);
    [_player loadTrack:track];
    
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(reset) {
    NSLog(@"Resetting player.");
    [_player reset];
}

RCT_EXPORT_METHOD(play) {
    NSLog(@"Starting/Resuming playback");
    [_player play];
}

RCT_EXPORT_METHOD(pause) {
    NSLog(@"Pausing playback");
    [_player pause];
}

RCT_EXPORT_METHOD(stop) {
    NSLog(@"Stopping playback");
    [_player stop];
}

RCT_EXPORT_METHOD(seekTo:(NSInteger)time) {
    NSLog(@"Seeking to %ld seconds", time);
    [_player seekToTime:time];
}

RCT_EXPORT_METHOD(setVolume:(NSInteger)volume) {
    NSLog(@"Setting volume to %ld", volume);
    [_player setVolume:volume];
}

//RCT_EXPORT_METHOD(startScan:(BOOL)active callback:(RCTResponseSenderBlock)callback) {
//    // TODO: - Implement
//}
//
//RCT_EXPORT_METHOD(stopScan) {
//    // TODO: - Implement
//}
//
//RCT_EXPORT_METHOD(connect:(NSInteger)deviceId callback:(RCTResponseSenderBlock)callback) {
//    // TODO: - Implement
//}

RCT_EXPORT_METHOD(getCurrentTrack:(NSInteger)identifier callback:(RCTResponseSenderBlock)callback) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    Track *track = [_player currentTrack];
    callback(@[[NSNull null], [track identifier]]);
}

RCT_EXPORT_METHOD(getDuration:(RCTResponseSenderBlock)callback) {
    callback(@[[NSNull null], @([_player duration])]);
}

RCT_EXPORT_METHOD(getBufferedPosition:(RCTResponseSenderBlock)callback) {
    // TODO: - Implement
}

RCT_EXPORT_METHOD(getPosition:(RCTResponseSenderBlock)callback) {
    callback(@[[NSNull null], @([_player position])]);
}

RCT_EXPORT_METHOD(getState:(NSInteger)identifier callback:(RCTResponseSenderBlock)callback) {
    callback(@[[NSNull null], @([_player state])]);
}

@end

