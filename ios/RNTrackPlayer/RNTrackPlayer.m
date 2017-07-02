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
@property(strong, nonatomic) Player *mainPlayer;
@property(strong, nonatomic) NSMutableDictionary *players;
@property(assign, nonatomic) NSInteger lastId;
@end

@implementation RNTrackPlayer

RCT_EXPORT_MODULE(TrackPlayerModule)

RCT_EXPORT_METHOD(onReady:(RCTResponseSenderBlock)callback) {
    // TODO: - Add these initializations to init?
    _lastId = 0;
    _players = [[NSMutableDictionary alloc] init];
    
    // Setup audio session
    NSError *error;
    
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:&error];
    [[AVAudioSession sharedInstance] setActive:YES error:&error];
    
    // TODO: - Handle possible setup error (promise rejection?)
    if (error) {
        NSLog(@"Audio session error");
    }
    
    callback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(setOptions:(NSDictionary *)options) {
    // TODO: - Implement
}

RCT_EXPORT_METHOD(createPlayer:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    Player *player = [[Player alloc] init];
    
    [_players setObject:player forKey:@(_lastId)];
    
    NSInteger identifier = _lastId++;
    resolve(@(identifier));
}

RCT_EXPORT_METHOD(setMain:(NSInteger)identifier) {
    if (identifier != -1) {
        NSLog(@"Setting %ld as the main player...", identifier);
        Player *player = [_players objectForKey:@(identifier)];
        _mainPlayer = player;
    } else {
        NSLog(@"Removing the main player...");
        _mainPlayer = nil;
    }
}

RCT_EXPORT_METHOD(destroy:(NSInteger)identifier) {
    if (identifier == -1) {
        NSLog(@"Destroying all players...");
        _mainPlayer = nil;
        
        [_players removeAllObjects];
    } else {
        [_players removeObjectForKey:@(identifier)];
    }
}

RCT_EXPORT_METHOD(add:(NSInteger)identifier before:(NSString *)insertBeforeId data:(NSArray *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    // TODO: - Implement
}

RCT_EXPORT_METHOD(remove:(NSInteger)identifier tracks:(NSArray *)tracks resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    // TODO: - Implement
}

RCT_EXPORT_METHOD(skip:(NSInteger)identifier track:(NSString *)track resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    // TODO: - Implement
}

RCT_EXPORT_METHOD(skipToNext:(NSInteger)identifier resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    // TODO: - Implement
}

RCT_EXPORT_METHOD(skipToPrevious:(NSInteger)identifier resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    // TODO: - Implement
}

RCT_EXPORT_METHOD(load:(NSUInteger)identifier track:(NSDictionary *)trackDict resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    Track *track = [[Track alloc] initWithDictionary:trackDict];
    
    if (identifier != -1) {
        NSLog(@"Loading a track in %ld...", identifier);
        Player *player = [_players objectForKey:@(identifier)];
        [player load:track];
    } else {
        for (id player in [_players allValues]) {
            [(Player *)player load:track];
        }
    }
    
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(reset:(NSInteger)identifier) {
    if (identifier != -1) {
        NSLog(@"Resetting %ld...", identifier);
        Player *player = [_players objectForKey:@(identifier)];
        [player reset];
    } else {
        for (id player in [_players allValues]) {
            [(Player *)player reset];
        }
    }
}

RCT_EXPORT_METHOD(play:(NSInteger)identifier) {
    if (identifier != -1) {
        NSLog(@"Sending play command to %ld...", identifier);
        Player *player = [_players objectForKey:@(identifier)];
        [player play];
    } else {
        for (id player in [_players allValues]) {
            [(Player *)player play];
        }
    }
}

RCT_EXPORT_METHOD(pause:(NSInteger)identifier) {
    if (identifier != -1) {
        NSLog(@"Sending pause command to %ld...", identifier);
        Player *player = [_players objectForKey:@(identifier)];
        [player pause];
    } else {
        for (id player in [_players allValues]) {
            [(Player *)player pause];
        }
    }
}

RCT_EXPORT_METHOD(stop:(NSInteger)identifier) {
    if (identifier != -1) {
        NSLog(@"Sending stop command to %ld...", identifier);
        Player *player = [_players objectForKey:@(identifier)];
        [player stop];
    } else {
        for (id player in [_players allValues]) {
            [(Player *)player stop];
        }
    }
}

RCT_EXPORT_METHOD(seekTo:(NSInteger)identifier time:(NSInteger)time) {
    if (identifier != -1) {
        NSLog(@"Seeking to %ld seconds in %ld...", time, identifier);
        Player *player = [_players objectForKey:@(identifier)];
        [player seekToTime:time];
    } else {
        for (id player in [_players allValues]) {
            [(Player *)player seekToTime:time];
        }
    }
}

RCT_EXPORT_METHOD(setVolume:(NSInteger)identifier volume:(NSInteger)volume) {
    Player *player = [_players objectForKey:@(identifier)];
    [player setVolume:volume];
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
    // TODO: - Implement
}

RCT_EXPORT_METHOD(getCurrentTrack:(NSInteger)identifier callback:(RCTResponseSenderBlock)callback) {
    Player *player = [_players objectForKey:@(identifier)];
    Track *track = [player currentTrack];
    callback(@[[NSNull null], [track identifier]]);
}

RCT_EXPORT_METHOD(getDuration:(NSInteger)identifier callback:(RCTResponseSenderBlock)callback) {
    Player *player = [_players objectForKey:@(identifier)];
    callback(@[[NSNull null], @([player duration])]);
}

RCT_EXPORT_METHOD(getBufferedPosition:(NSInteger)identifier callback:(RCTResponseSenderBlock)callback) {
    // TODO: - Implement
}

RCT_EXPORT_METHOD(getPosition:(NSInteger)identifier callback:(RCTResponseSenderBlock)callback) {
    Player *player = [_players objectForKey:@(identifier)];
    callback(@[[NSNull null], @([player duration])]);
}

RCT_EXPORT_METHOD(getState:(NSInteger)identifier callback:(RCTResponseSenderBlock)callback) {
    Player *player = [_players objectForKey:@(identifier)];
    callback(@[[NSNull null], @([player duration])]);
}

@end
