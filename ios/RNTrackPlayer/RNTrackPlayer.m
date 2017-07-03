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
    // Setup audio session
    NSError *error;
    
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:&error];
    [[AVAudioSession sharedInstance] setActive:YES error:&error];
    
    // TODO: - Handle possible setup error (promise rejection?)
    if (error) {
        NSLog(@"Audio session error!");
    }
    
    callback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(setOptions:(NSDictionary *)options) {
    // TODO: - Implement
}

RCT_EXPORT_METHOD(createPlayer:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    resolve(@(0));
}

RCT_EXPORT_METHOD(setMain:(NSInteger)identifier) {
    // TODO: - Remove when Android project gets rid of multiple players
}

RCT_EXPORT_METHOD(destroy:(NSInteger)identifier) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    NSLog(@"Destroying player");
    _player = nil;
}

RCT_EXPORT_METHOD(add:(NSInteger)identifier before:(NSString *)trackId tracks:(NSArray *)trackDicts resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    if (![_player queueContainsTrack:trackId]) {
        reject(@"track_not_in_queue", @"Given track ID was not found in queue", nil);
        return;
    }
    
    NSMutableArray *tracks = [[NSMutableArray alloc] init];
    for (id trackDict in trackDicts) {
        Track *track = [[Track alloc] initWithDictionary:trackDict];
        [tracks addObject:track];
    }
    
    NSLog(@"Adding tracks: %@", tracks);
    [_player addTracks:tracks before:trackId];
    
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(remove:(NSInteger)identifier trackIds:(NSArray *)trackIds resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    NSLog(@"Removing tracks: %@", trackIds);
    [_player removeTrackIds:trackIds];
    
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(skip:(NSInteger)identifier trackId:(NSString *)trackId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    if (![_player queueContainsTrack:trackId]) {
        reject(@"track_not_in_queue", @"Given track ID was not found in queue", nil);
        return;
    }
    
    NSLog(@"Skipping to track %@", trackId);
    [_player skipToTrack:trackId];
    
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(skipToNext:(NSInteger)identifier resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    NSLog(@"Skipping to next track");
    if ([_player playNext]) {
        resolve([NSNull null]);
    } else {
        reject(@"queue_exhausted", @"There is no tracks left to play", nil);
    }
}

RCT_EXPORT_METHOD(skipToPrevious:(NSInteger)identifier resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    NSLog(@"Skipping to previous track");
    if ([_player playPrevious]) {
        resolve([NSNull null]);
    } else {
        reject(@"no_previous_track", @"There is no previous track", nil);
    }
}

RCT_EXPORT_METHOD(load:(NSUInteger)identifier track:(NSDictionary *)trackDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    Track *track = [[Track alloc] initWithDictionary:trackDict];
    
    NSLog(@"Loading track: %@", trackDict);
    [_player loadTrack:track];
    
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(reset:(NSInteger)identifier) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    NSLog(@"Resetting player.");
    [_player reset];
}

RCT_EXPORT_METHOD(play:(NSInteger)identifier) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    NSLog(@"Starting/Resuming playback");
    [_player play];
}

RCT_EXPORT_METHOD(pause:(NSInteger)identifier) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    NSLog(@"Pausing playback");
    [_player pause];
}

RCT_EXPORT_METHOD(stop:(NSInteger)identifier) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    NSLog(@"Stopping playback");
    [_player stop];
}

RCT_EXPORT_METHOD(seekTo:(NSInteger)identifier time:(NSInteger)time) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    NSLog(@"Seeking to %ld seconds", time);
    [_player seekToTime:time];
}

RCT_EXPORT_METHOD(setVolume:(NSInteger)identifier volume:(NSInteger)volume) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    NSLog(@"Setting volume to %ld", volume);
    [_player setVolume:volume];
}

RCT_EXPORT_METHOD(startScan:(BOOL)active callback:(RCTResponseSenderBlock)callback) {
    // TODO: - Implement
}

RCT_EXPORT_METHOD(stopScan) {
    // TODO: - Implement
}

RCT_EXPORT_METHOD(connect:(NSInteger)deviceId callback:(RCTResponseSenderBlock)callback) {
    // TODO: - Implement
}

RCT_EXPORT_METHOD(copyQueue:(NSInteger)fromId toId:(NSInteger)toId beforeId:(NSString *)beforeId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    // TODO: - Remove when Android project gets rid of multiple players
}

RCT_EXPORT_METHOD(getCurrentTrack:(NSInteger)identifier callback:(RCTResponseSenderBlock)callback) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    Track *track = [_player currentTrack];
    callback(@[[NSNull null], [track identifier]]);
}

RCT_EXPORT_METHOD(getDuration:(NSInteger)identifier callback:(RCTResponseSenderBlock)callback) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    callback(@[[NSNull null], @([_player duration])]);
}

RCT_EXPORT_METHOD(getBufferedPosition:(NSInteger)identifier callback:(RCTResponseSenderBlock)callback) {
    // TODO: - Implement
}

RCT_EXPORT_METHOD(getPosition:(NSInteger)identifier callback:(RCTResponseSenderBlock)callback) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    callback(@[[NSNull null], @([_player duration])]);
}

RCT_EXPORT_METHOD(getState:(NSInteger)identifier callback:(RCTResponseSenderBlock)callback) {
    // TODO: - Remove identifier parameter when Android project gets rid of multiple players
    
    callback(@[[NSNull null], @([_player duration])]);
}

@end
