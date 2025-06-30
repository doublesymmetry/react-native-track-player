#import "TrackPlayer.h"
#import <React/RCTEventEmitter.h>

#if __has_include("react_native_track_player-Swift.h")
#import "react_native_track_player-Swift.h"
#else
#import "react_native_track_player/react_native_track_player-Swift.h"
#endif

@interface TrackPlayer () <NativeTrackPlayerImplDelegate>
@end

@implementation TrackPlayer {
  NativeTrackPlayerImpl *nativeTrackPlayer;
}

RCT_EXPORT_MODULE()

- (instancetype) init {
  self = [super init];
  if (self) {
    nativeTrackPlayer = [NativeTrackPlayerImpl new];
    // Critical: Register ourselves as the Objective-C bridge's event emitter
    nativeTrackPlayer.delegate = self;
  }
  return self;
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeTrackPlayerSpecJSI>(params);
}

+ (BOOL)requiresMainQueueSetup {
  return NO;
}


- (void)add:(nonnull NSArray *)tracks insertBeforeIndex:(nonnull NSNumber *)insertBeforeIndex resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer addWithTrackDicts:tracks before:insertBeforeIndex resolve:resolve reject:reject];
}

- (nonnull facebook::react::ModuleConstants<JS::NativeTrackPlayer::Constants::Builder>)constantsToExport {
  return [NativeTrackPlayerImpl constantsToExport];
}

- (void)getActiveTrack:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer getActiveTrackWithResolve:resolve reject:reject];
}

- (void)getActiveTrackIndex:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer getActiveTrackIndexWithResolve:resolve reject:reject];
}

- (nonnull facebook::react::ModuleConstants<JS::NativeTrackPlayer::Constants::Builder>)getConstants {
  return [self constantsToExport];
}

- (void)getPlayWhenReady:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer getPlayWhenReadyWithResolve:resolve reject:reject];
}

- (void)getPlaybackState:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer getPlaybackStateWithResolve:resolve reject:reject];
}

- (void)getProgress:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer getProgressWithResolve:resolve reject:reject];
}

- (void)getQueue:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer getQueueWithResolve:resolve reject:reject];
}

- (void)getRate:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer getRateWithResolve:resolve reject:reject];
}

- (void)getRepeatMode:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer getRepeatModeWithResolve:resolve reject:reject];
}

- (void)getTrack:(double)index resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer getTrackWithIndex:index resolve:resolve reject:reject];
}

- (void)getVolume:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer getVolumeWithResolve:resolve reject:reject];
}

- (void)load:(nonnull NSDictionary *)track resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer loadWithTrackDict:track resolve:resolve reject:reject];
}

- (void)move:(double)fromIndex toIndex:(double)toIndex resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer moveFromIndex:fromIndex toIndex:toIndex resolve:resolve reject:reject];
}

- (void)pause:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer pauseWithResolve:resolve reject:reject];
}

- (void)play:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer playWithResolve:resolve reject:reject];
}

- (void)remove:(nonnull NSArray *)indexes resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer removeWithTracks:indexes resolve:resolve reject:reject];
}

- (void)removeUpcomingTracks:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer removeUpcomingTracksWithResolve:resolve reject:reject];
}

- (void)reset:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer resetWithResolve:resolve reject:reject];
}

- (void)retry:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer retryWithResolve:resolve reject:reject];
}

- (void)seekBy:(double)offset resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer seekByOffset:offset resolve:resolve reject:reject];
}

- (void)seekTo:(double)position resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer seekToTime:position resolve:resolve reject:reject];
}

- (void)setPlayWhenReady:(BOOL)playWhenReady resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer setPlayWhenReadyWithPlayWhenReady:playWhenReady resolve:resolve reject:reject];
}

- (void)setQueue:(nonnull NSArray *)tracks resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer setQueueWithTrackDicts:tracks resolve:resolve reject:reject];
}

- (void)setRate:(double)rate resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer setRateWithRate:rate resolve:resolve reject:reject];
}

- (void)setRepeatMode:(double)mode resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer setRepeatModeWithRepeatMode:@(mode) resolve:resolve reject:reject];
}

- (void)setVolume:(double)level resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer setVolumeWithLevel:level resolve:resolve reject:reject];
}

- (void)setupPlayer:(nonnull NSDictionary *)options resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer setupPlayer:options resolver:resolve rejecter:reject];
}

- (void)skip:(double)index initialPosition:(nonnull NSNumber *)initialPosition resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer skipTo:index initialTime:initialPosition.doubleValue resolve:resolve reject:reject];
}

- (void)skipToNext:(nonnull NSNumber *)initialPosition resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer skipToNextWithInitialTime:initialPosition.doubleValue resolve:resolve reject:reject];
}

- (void)skipToPrevious:(nonnull NSNumber *)initialPosition resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer skipToPreviousWithInitialTime:initialPosition.doubleValue resolve:resolve reject:reject];
}

- (void)stop:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer stopWithResolve:resolve reject:reject];
}

- (void)updateMetadataForTrack:(double)trackIndex metadata:(nonnull NSDictionary *)metadata resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer updateMetadataFor:trackIndex metadata:metadata resolve:resolve reject:reject];
}

- (void)updateNowPlayingMetadata:(nonnull NSDictionary *)metadata resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer updateNowPlayingMetadataWithMetadata:metadata resolve:resolve reject:reject];
}

- (void)updateOptions:(nonnull NSDictionary *)options resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  [nativeTrackPlayer updateOptions:options resolver:resolve rejecter:reject];
}

// event listeners
- (void)sendEvent:(NSString *)name body:(id)body {
  [super sendEventWithName:name body:body];
}

- (NSArray<NSString *> *)supportedEvents {
  return [NativeTrackPlayerImpl supportedEvents];
}

/*****************************************
 * Android Only Methods (Stubs)
 *****************************************/
- (void)validateOnStartCommandIntent:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  resolve(@(YES));
}
- (void)abandonWakeLock:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  resolve(nil);
}
- (void)acquireWakeLock:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
  resolve(nil);
}

@end
