import { Constants } from '../NativeTrackPlayer';

export enum PitchAlgorithm {
  /**
   * A high-quality time pitch algorithm that doesn’t perform pitch correction.
   * */
  Linear = Constants?.PITCH_ALGORITHM_LINEAR ?? 1,
  /**
   * A highest-quality time pitch algorithm that’s suitable for music.
   **/
  Music = Constants?.PITCH_ALGORITHM_MUSIC ?? 2,
  /**
   * A modest quality time pitch algorithm that’s suitable for voice.
   **/
  Voice = Constants?.PITCH_ALGORITHM_VOICE ?? 3,
}
