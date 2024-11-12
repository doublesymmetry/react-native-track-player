import type { AppKilledPlaybackBehavior } from '../constants';

export interface AndroidOptions {
  /**
   * parses embedded artwork on local files to a dedicated png path.
   * This is only necessary for certain MediaBrowsers, namely Android Auto to
   * read embedded artwork.
   * @default false
   */

  androidParseEmbeddedArtwork?: boolean;
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
   * https://developer.android.com/media/media3/exoplayer/track-selection#audioOffload
   */
  audioOffload?: boolean;

  /**
   * enables exoplayer's skipSilence parser
   * @default false
   */
  androidSkipSilence?: boolean;

  /**
   * enables exoplayer's shuffle mode
   * @default false
   */
  shuffle?: boolean;
}
