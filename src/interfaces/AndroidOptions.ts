import type { AppKilledPlaybackBehavior } from '../constants';

export interface NotificationClickBehavior {
  /**
   * Whether to add URI data to the notification click intent
   * @default true
   */
  enabled?: boolean;
  
  /**
   * Custom URI to use instead of the default "trackplayer://notification.click"
   * Only used when enabled is true
   */
  customUri?: string;
  
  /**
   * Custom action to use for the notification click intent
   * @default "android.intent.action.VIEW"
   */
  action?: string;
}

export interface AndroidOptions {
  /**
   * Whether the audio playback notification is also removed when the playback
   * stops. **If `stoppingAppPausesPlayback` is set to false, this will be
   * ignored.**
   */
  appKilledPlaybackBehavior?: AppKilledPlaybackBehavior;

  /**
   * Whether the remote-duck event will be triggered on every interruption
   */
  alwaysPauseOnInterruption?: boolean;

  /**
   * Time in seconds to wait once the player should transition to not
   * considering the service as in the foreground. If playback resumes within
   * this grace period, the service remains in the foreground state.
   * Defaults to 5 seconds.
   */
  stopForegroundGracePeriod?: number;

  /**
   * Configuration for notification click behavior
   */
  notificationClickBehavior?: NotificationClickBehavior;
}
