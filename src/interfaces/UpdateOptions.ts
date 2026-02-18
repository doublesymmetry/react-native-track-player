import type { Capability, RatingType } from '../constants';
import type { AndroidOptions } from './AndroidOptions';
import type { FeedbackOptions } from './FeedbackOptions';

export interface UpdateOptions {
  android?: AndroidOptions;
  ratingType?: RatingType;
  forwardJumpInterval?: number;
  backwardJumpInterval?: number;
  progressUpdateEventInterval?: number; // in seconds

  // ios
  likeOptions?: FeedbackOptions;
  dislikeOptions?: FeedbackOptions;
  bookmarkOptions?: FeedbackOptions;

  /**
   * iOS only. When `true`, registers `nextTrackCommand` and
   * `previousTrackCommand` on `MPRemoteCommandCenter` but routes them to skip
   * forward / backward by the configured jump intervals instead of emitting
   * `Event.RemoteNext` / `Event.RemotePrevious`.
   *
   * This enables AirPods double-press (skip forward) and triple-press (skip
   * backward) without changing the lock screen / Control Center UI, which will
   * continue to show the skip-interval buttons when `Capability.JumpForward`
   * and `Capability.JumpBackward` are in the `capabilities` array.
   *
   * Default: `false`
   */
  iosNextPreviousCommandsSkip?: boolean;

  capabilities?: Capability[];

  // android
  notificationCapabilities?: Capability[];

  color?: number;
}
