import type { AndroidOptions } from './AndroidOptions';
import type { FeedbackOptions } from './FeedbackOptions';
import type { ResourceObject } from './ResourceObject';
import type {
  RatingType,
  Capability,
  IOSCategory,
  IOSCategoryMode,
  IOSCategoryOptions,
  AndroidAudioContentType,
} from '../constants';
import { AndroidAudioUsageType } from '../constants/AndroidAudioUsageType';

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

  /**
   * [AVAudioSession.Category](https://developer.apple.com/documentation/avfoundation/avaudiosession/1616615-category)
   */
  iosCategory?: IOSCategory;
  /**
   * (iOS only) The audio session mode, together with the audio session category,
   * indicates to the system how you intend to use audio in your app. You can use
   * a mode to configure the audio system for specific use cases such as video
   * recording, voice or video chat, or audio analysis.
   *
   * See https://developer.apple.com/documentation/avfoundation/avaudiosession/1616508-mode
   */
  iosCategoryMode?: IOSCategoryMode;
  /**
   * [AVAudioSession.CategoryOptions](https://developer.apple.com/documentation/avfoundation/avaudiosession/1616503-categoryoptions) for iOS.
   */
  iosCategoryOptions?: IOSCategoryOptions[];

  /**
   * (Android only) Specifies why the source is playing and controls routing, focus, and volume decisions.
   * With `androidAudioUsageType` set to VoiceCommunication the audio will come from the earpiece speaker
   *
   * @default AndroidAudioUsageType.Media
   */
  androidAudioUsageType?: AndroidAudioUsageType;

  /**
   * (Android only) The audio content type indicates to the android system how
   * you intend to use audio in your app.
   *
   * With `autoHandleInterruptions: true` and
   * `androidAudioContentType: AndroidAudioContentType.Speech`, the audio will be
   * paused during short interruptions, such as when a message arrives.
   * Otherwise the playback volume is reduced while the notification is playing.
   *
   * @default AndroidAudioContentType.Music
   */
  androidAudioContentType?: AndroidAudioContentType;

  /**
   * Indicates whether the player should automatically handle audio interruptions.
   * Defaults to `false`.
   */
  autoHandleInterruptions?: boolean;
}
