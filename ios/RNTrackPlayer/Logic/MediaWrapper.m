//
//  MediaWrapper.m
//  RNTrackPlayer
//
//  Created by David Chavez on 7/2/17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

@import MediaPlayer;

#import "MediaWrapper.h"
#import "STKAudioPlayer.h"

@interface MediaWrapper() <STKAudioPlayerDelegate>
@property(strong, nonatomic) NSMutableArray *queue;
@property(strong, nonatomic) STKAudioPlayer *player;
@property(assign, nonatomic) NSInteger currentIndex;
@property(strong, nonatomic) NSURLSessionDataTask *imageDownloader;
@end

@implementation MediaWrapper

- (id)init {
    self = [super init];
    if (self) {
        _currentIndex = 0;
        _queue = [[NSMutableArray alloc] init];
        _player = [[STKAudioPlayer alloc] init];
        
        [[UIApplication sharedApplication] beginReceivingRemoteControlEvents];
    }
    
    return self;
}

- (void)dealloc {
    [_player dispose];
}


// MARK: - Public Interface

- (BOOL)queueContainsTrack:(NSString *)trackId {
    for (id track in _queue) {
        if ([[(Track *)track identifier] isEqualToString:trackId]) {
            return YES;
        }
    }
    
    return NO;
}

- (void)addTracks:(NSArray *)tracks before:(NSString *)trackId {
    __block BOOL indexFound = NO;
    __block NSInteger indexToInsertAt = 0;
    
    [_queue enumerateObjectsUsingBlock:^(id track, NSUInteger index, BOOL *stop) {
        if ([[(Track *)track identifier] isEqualToString:trackId]) {
            indexFound = YES;
            indexToInsertAt = indexToInsertAt;
        }
    }];
    
    if (indexFound) {
        NSRange range = NSMakeRange(indexToInsertAt, tracks.count);
        NSIndexSet *indexes = [NSIndexSet indexSetWithIndexesInRange:range];
        [_queue insertObjects:tracks atIndexes:indexes];
    } else {
        [_queue addObjectsFromArray:tracks];
    }
}

- (void)removeTrackIds:(NSArray *)trackIds {
    __block BOOL removedCurrentTrack = NO;
    
    NSUInteger numberOfTracksToKeep = _queue.count - trackIds.count;
    __block NSMutableArray *tracksToKeep = [NSMutableArray arrayWithCapacity:numberOfTracksToKeep];
    
    [_queue enumerateObjectsUsingBlock:^(id track, NSUInteger queueIndex, BOOL *stop) {
        for (id identifier in trackIds) {
            if ([[(Track *)track identifier] isEqualToString:(NSString *)identifier]) {
                if (queueIndex == _currentIndex) { removedCurrentTrack = YES; }
                else if (queueIndex < _currentIndex) { _currentIndex--; }
            } else {
                [tracksToKeep addObject:track];
            }
        }
    }];
    
    if (removedCurrentTrack) {
        if (_currentIndex > _queue.count - 1) {
            _currentIndex = _queue.count;
            [self stop];
        } else {
            [self play];
        }
    }
    
    [_queue setArray:tracksToKeep];
}

- (void)skipToTrack:(NSString *)trackId {
    [_queue enumerateObjectsUsingBlock:^(id track, NSUInteger index, BOOL *stop) {
        if ([[(Track *)track identifier] isEqualToString:(NSString *)trackId]) {
            _currentIndex = index;
            [self play];
        }
    }];
}

- (BOOL)playNext {
    if (_currentIndex < _queue.count - 1) {
        _currentIndex++;
        [self play];
        return YES;
    } else {
        [self stop];
        return NO;
    }
}

- (BOOL)playPrevious {
    if (_currentIndex > 0) {
        _currentIndex--;
        [self play];
        return YES;
    }
    
    return NO;
}

- (void)reset {
    [_queue removeAllObjects];
    [_player stop];
}

- (void)play {
    // Resume playback if it was paused
    if (_player.state == STKAudioPlayerStatePaused) {
        [_player resume];
        return;
    }
    
    Track *track = [_queue objectAtIndex:_currentIndex];
    [_imageDownloader cancel];
    
    // TODO: Handle artwork (part of supporting local files)
    NSDictionary *metadata = @{
                               MPMediaItemPropertyMediaType: @(MPMediaTypeMusic),
                               MPMediaItemPropertyTitle: track.title ? track.title : @"",
                               MPMediaItemPropertyAlbumTitle: track.album ? track.album : @"",
                               MPMediaItemPropertyArtist: track.artist ? track.artist : @"",
                               MPMediaItemPropertyGenre: track.genre ? track.genre : @"",
                               MPMediaItemPropertyPlaybackDuration: track.duration ? track.duration : @(0),
                               MPMediaItemPropertyReleaseDate: track.date ? track.date : @"",
                               MPNowPlayingInfoPropertyElapsedPlaybackTime: @(_player.progress),
                               };
    
    [[MPNowPlayingInfoCenter defaultCenter] setNowPlayingInfo:metadata];
    
    _imageDownloader = [[NSURLSession sharedSession]
                        dataTaskWithURL:track.artwork.value
                        completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
                            if (!error) {
                                NSMutableDictionary *dict = [MPNowPlayingInfoCenter defaultCenter].nowPlayingInfo.mutableCopy;
                                UIImage *artwork = [UIImage imageWithData:data];
                                dict[MPMediaItemPropertyArtwork] = [[MPMediaItemArtwork alloc] initWithImage:artwork];
                                [[MPNowPlayingInfoCenter defaultCenter] setNowPlayingInfo:dict];
                            }
                        }];
    [_imageDownloader resume];
    
    // fetch possible redirected url
    if (!track.url.isLocal) {
        NSMutableURLRequest *newRequest = [[NSMutableURLRequest alloc] initWithURL:track.url.value];
        NSURLResponse *response = nil;
        NSError *error = nil;
        
        [newRequest setValue:@"HEAD" forKey:@"HTTPMethod"];
        [NSURLConnection
         sendSynchronousRequest:newRequest
         returningResponse:&response
         error:&error];
        
        if (response && !error) {
            [track.url setValue:[response URL]];
        }
    }
    
    [_player playURL:track.url.value];
}

- (void)pause {
    [_player pause];
}

- (void)stop {
    [_player stop];
    [[MPNowPlayingInfoCenter defaultCenter] setNowPlayingInfo:nil];
}

- (void)seekToTime:(NSInteger)time {
    double delayInSeconds = 0.1;
    int64_t delay = (int64_t)(delayInSeconds * NSEC_PER_SEC);
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, delay);
    dispatch_after(popTime, dispatch_get_main_queue(), ^() {
        [_player seekToTime:time];
    });
}

- (void)setVolume:(NSInteger)volume {
    [_player setVolume:volume];
}

- (Track *)currentTrack {
    return _queue[_currentIndex];
}

- (NSInteger)duration {
    return _player.duration;
}

- (NSInteger)position {
    return _player.progress;
}

- (NSString *)state {
    // Mapping to values used by Android project
    // TODO: - Unify into a shared enum
    switch (_player.state) {
        case STKAudioPlayerStatePlaying:
            return @"STATE_PLAYING";
        case STKAudioPlayerStatePaused:
            return @"STATE_PAUSED";
        case STKAudioPlayerStateStopped:
            return @"STATE_STOPPED";
        case STKAudioPlayerStateBuffering:
            return @"STATE_BUFFERING";
        default:
            return @"STATE_NONE";
    }
}


// MARK: - STKAudioPlayerDelegate

- (void)audioPlayer:(STKAudioPlayer *)audioPlayer
didFinishBufferingSourceWithQueueItemId:(NSObject *)queueItemId {}

- (void)audioPlayer:(STKAudioPlayer *)audioPlayer
didFinishPlayingQueueItemId:(NSObject *)queueItemId
         withReason:(STKAudioPlayerStopReason)stopReason
        andProgress:(double)progress
        andDuration:(double)duration {
    // Play the next song in the queue when song ends
    if (stopReason == STKAudioPlayerStopReasonEof) {
        [self playNext];
        [_imageDownloader cancel];
        [[MPNowPlayingInfoCenter defaultCenter] setNowPlayingInfo:@{}];
    }
}

- (void)audioPlayer:(STKAudioPlayer *)audioPlayer
didStartPlayingQueueItemId:(NSObject *)queueItemId {}

- (void)audioPlayer:(STKAudioPlayer *)audioPlayer
       stateChanged:(STKAudioPlayerState)state
      previousState:(STKAudioPlayerState)previousState {}

- (void)audioPlayer:(STKAudioPlayer *)audioPlayer
    unexpectedError:(STKAudioPlayerErrorCode)errorCode {}

@end
