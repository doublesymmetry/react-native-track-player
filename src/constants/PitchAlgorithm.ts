import TrackPlayer from '../TrackPlayerModule';

export enum PitchAlgorithm {
  /**
   * A high-quality time pitch algorithm that doesn’t perform pitch correction.
   * */
  Linear = TrackPlayer.PITCH_ALGORITHM_LINEAR,
  /**
   * A highest-quality time pitch algorithm that’s suitable for music.
   **/
  Music = TrackPlayer.PITCH_ALGORITHM_MUSIC,
  /**
   * A modest quality time pitch algorithm that’s suitable for voice.
   **/
  Voice = TrackPlayer.PITCH_ALGORITHM_VOICE,
}
