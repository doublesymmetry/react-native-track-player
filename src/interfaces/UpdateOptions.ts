import type { AndroidOptions } from './AndroidOptions';
import type { ResourceObject } from './ResourceObject';
import type { CapabilityImpl } from '../constants';

export interface UpdateOptions {
  android?: AndroidOptions;
  // ratingType?: RatingType;
  // forwardJumpInterval?: number;
  // backwardJumpInterval?: number;
  progressUpdateEventInterval?: number; // in seconds

  // ios
  // likeOptions?: FeedbackOptions;
  // dislikeOptions?: FeedbackOptions;
  // bookmarkOptions?: FeedbackOptions;

  capabilities?: CapabilityImpl[];
  // notificationCapabilities?: Capability[];
  // compactCapabilities?: Capability[];

  // android
  /**
   * @deprecated Use `android` options and `appKilledPlaybackMode` instead.
   * @example
   * ```
   * await TrackPlayer.updateOptions({
   *   android: {
   *     appKilledPlaybackMode: AppKilledPlaybackMode.PausePlayback
   *  },
   * });
   *  ```
   */
  stoppingAppPausesPlayback?: boolean;
  /**
   * @deprecated use `TrackPlayer.updateOptions({ android: { alwaysPauseOnInterruption: boolean }})` instead
   */
  alwaysPauseOnInterruption?: boolean;

  // icon?: ResourceObject;
  // playIcon?: ResourceObject;
  // pauseIcon?: ResourceObject;
  // stopIcon?: ResourceObject;
  // previousIcon?: ResourceObject;
  // nextIcon?: ResourceObject;
  // rewindIcon?: ResourceObject;
  // forwardIcon?: ResourceObject;
  // color?: number;
}
