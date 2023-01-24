export enum AndroidAudioContentType {
  /**
   * Content type value to use when the content type is music.
   *
   * See https://developer.android.com/reference/android/media/AudioAttributes#CONTENT_TYPE_MUSIC
   */
  Music = 'music',
  /**
   * Content type value to use when the content type is speech.
   *
   * See https://developer.android.com/reference/android/media/AudioAttributes#CONTENT_TYPE_SPEECH
   */
  Speech = 'speech',
  /**
   * Content type value to use when the content type is a sound used to
   * accompany a user action, such as a beep or sound effect expressing a key
   * click, or event, such as the type of a sound for a bonus being received in
   * a game. These sounds are mostly synthesized or short Foley sounds.
   *
   * See https://developer.android.com/reference/android/media/AudioAttributes#CONTENT_TYPE_SONIFICATION
   */
  Sonification = 'sonification',
  /**
   * Content type value to use when the content type is a soundtrack, typically
   * accompanying a movie or TV program.
   */
  Movie = 'movie',
  /**
   * Content type value to use when the content type is unknown, or other than
   * the ones defined.
   *
   * See https://developer.android.com/reference/android/media/AudioAttributes#CONTENT_TYPE_UNKNOWN
   */
  Unknown = 'unknown',
}
