//
//  RNTrackPlayer.m
//  RNTrackPlayer
//
//  Created by David Chavez on 7/1/17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

#import <AVFoundation/AVFoundation.h>

#import "Track.h"
#import "MediaWrapper.h"
#import "RNTrackPlayer.h"
#import "STKAudioPlayer.h"

@interface RNTrackPlayer()
@property(strong, nonatomic) MediaWrapper *mediaWrapper;
@end

@implementation RNTrackPlayer

-(id)init {
    self = [super init];
    if (self)  {
        _mediaWrapper = [[MediaWrapper alloc] init];
    }
    
    return self;
}

RCT_EXPORT_MODULE(TrackPlayerModule)

- (NSDictionary *)constantsToExport {
    return @{
             @"STATE_NONE": @(STKAudioPlayerStateDisposed),
             @"STATE_PLAYING": @(STKAudioPlayerStatePlaying),
             @"STATE_PAUSED": @(STKAudioPlayerStatePaused),
             @"STATE_STOPPED": @(STKAudioPlayerStateStopped),
             @"STATE_BUFFERING": @(STKAudioPlayerStateBuffering)
             };
}

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
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(destroy) {
    NSLog(@"Destroying player");
    _mediaWrapper = nil;
}

RCT_EXPORT_METHOD(updateOptions:(NSDictionary *)options) {
    // TODO: - Implement
}

RCT_EXPORT_METHOD(add:(NSArray *)objects
                  before:(NSString *)trackId
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    if (![_mediaWrapper queueContainsTrack:trackId] && trackId != nil) {
        reject(@"track_not_in_queue", @"Given track ID was not found in queue", nil);
        return;
    }
    
    NSMutableArray *tracks = [[NSMutableArray alloc] init];
    for (id dict in objects) {
        Track *track = [[Track alloc] initWithDictionary:dict];
        [tracks addObject:track];
    }
    
    NSLog(@"Adding tracks: %@", tracks);
    [_mediaWrapper addTracks:tracks before:trackId];
    
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(remove:(NSArray *)objects
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    NSLog(@"Removing tracks: %@", objects);
    [_mediaWrapper removeTrackIds:objects];
    
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(skip:(NSString *)trackId
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    if (![_mediaWrapper queueContainsTrack:trackId]) {
        reject(@"track_not_in_queue", @"Given track ID was not found in queue", nil);
        return;
    }
    
    NSLog(@"Skipping to track %@", trackId);
    [_mediaWrapper skipToTrack:trackId];
    
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(skipToNext:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    NSLog(@"Skipping to next track");
    if ([_mediaWrapper playNext]) {
        resolve([NSNull null]);
    } else {
        reject(@"queue_exhausted", @"There is no tracks left to play", nil);
    }
}

RCT_EXPORT_METHOD(skipToPrevious:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    NSLog(@"Skipping to previous track");
    if ([_mediaWrapper playPrevious]) {
        resolve([NSNull null]);
    } else {
        reject(@"no_previous_track", @"There is no previous track", nil);
    }
}

RCT_EXPORT_METHOD(reset) {
    NSLog(@"Resetting player.");
    [_mediaWrapper reset];
}

RCT_EXPORT_METHOD(play) {
    NSLog(@"Starting/Resuming playback");
    [_mediaWrapper play];
}

RCT_EXPORT_METHOD(pause) {
    NSLog(@"Pausing playback");
    [_mediaWrapper pause];
}

RCT_EXPORT_METHOD(stop) {
    NSLog(@"Stopping playback");
    [_mediaWrapper stop];
}

RCT_EXPORT_METHOD(seekTo:(NSInteger)time) {
    NSLog(@"Seeking to %ld seconds", time);
    [_mediaWrapper seekToTime:time];
}

RCT_EXPORT_METHOD(setVolume:(NSInteger)volume) {
    NSLog(@"Setting volume to %ld", volume);
    [_mediaWrapper setVolume:volume];
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

RCT_EXPORT_METHOD(getCurrentTrack:(RCTResponseSenderBlock)callback) {
    Track *track = [_mediaWrapper currentTrack];
    callback(@[[NSNull null], [track identifier]]);
}

RCT_EXPORT_METHOD(getDuration:(RCTResponseSenderBlock)callback) {
    callback(@[[NSNull null], @([_mediaWrapper duration])]);
}

RCT_EXPORT_METHOD(getBufferedPosition:(RCTResponseSenderBlock)callback) {
    // TODO: - Implement
}

RCT_EXPORT_METHOD(getPosition:(RCTResponseSenderBlock)callback) {
    callback(@[[NSNull null], @([_mediaWrapper position])]);
}

RCT_EXPORT_METHOD(getState:(RCTResponseSenderBlock)callback) {
    callback(@[[NSNull null], [_mediaWrapper state]]);
}

@end
