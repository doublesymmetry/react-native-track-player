import type { AndroidOptions } from './AndroidOptions';
import type { FeedbackOptions } from './FeedbackOptions';
import type { ResourceObject } from './ResourceObject';
import type { Capability, RatingType } from '../constants';

export interface MetadataOptions {
  android?: AndroidOptions;
  ratingType?: RatingType;
  forwardJumpInterval?: number;
  backwardJumpInterval?: number;
  progressUpdateEventInterval?: number; // in seconds

  // ios
  likeOptions?: FeedbackOptions;
  dislikeOptions?: FeedbackOptions;
  bookmarkOptions?: FeedbackOptions;

  capabilities?: Capability[];

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
  alwaysPauseOnInterruption?: boolean;
  notificationCapabilities?: Capability[];
  compactCapabilities?: Capability[];

  icon?: ResourceObject;
  playIcon?: ResourceObject;
  pauseIcon?: ResourceObject;
  stopIcon?: ResourceObject;
  previousIcon?: ResourceObject;
  nextIcon?: ResourceObject;
  rewindIcon?: ResourceObject;
  forwardIcon?: ResourceObject;
  color?: number;
}
