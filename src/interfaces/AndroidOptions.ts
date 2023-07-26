import type { AppKilledPlaybackBehavior } from '../constants';
import {ResourceObject} from "./ResourceObject";

export interface AndroidOptions {
  /**
   * Whether the audio playback notification is also removed when the playback
   * stops. **If `stoppingAppPausesPlayback` is set to false, this will be
   * ignored.**
   */
  appKilledPlaybackBehavior?: AppKilledPlaybackBehavior;

  /** Whether the remote-duck event will be triggered on every interruption. */
  alwaysPauseOnInterruption?: boolean;

  /** Configuration settings for the notification. */
  notificationConfig?: {
    /** The small icon to use in the notification. */
    smallIcon?: ResourceObject;
    /** The color to use for the accent in the notification. */
    accentColor?: string;
  }
}
