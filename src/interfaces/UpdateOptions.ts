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

  capabilities?: Capability[];

  // android
  notificationCapabilities?: Capability[];

  color?: number;
}
