import type { AppKilledPlaybackBehavior } from '../constants';

export interface AndroidOptions {
  /**
   * Whether the audio playback notification is also removed when the playback
   * stops. **If `stoppingAppPausesPlayback` is set to false, this will be
   * ignored.**
   */
  appKilledPlaybackBehavior?: AppKilledPlaybackBehavior;
  alwaysPauseOnInterruption?: boolean;
}
