export enum IOSCategory {
  /**
   * The category for playing recorded music or other sounds that are central
   * to the successful use of your app.
   * See https://developer.apple.com/documentation/avfaudio/avaudiosession/category/1616509-playback
   **/
  Playback = 'playback',
  /**
   * The category for recording (input) and playback (output) of audio, such as
   * for a Voice over Internet Protocol (VoIP) app.
   * See https://developer.apple.com/documentation/avfaudio/avaudiosession/category/1616568-playandrecord
   **/
  PlayAndRecord = 'playAndRecord',
  /**
   * The category for routing distinct streams of audio data to different
   * output devices at the same time.
   * See https://developer.apple.com/documentation/avfaudio/avaudiosession/category/1616484-multiroute
   **/
  MultiRoute = 'multiRoute',
  /**
   * The category for an app in which sound playback is nonprimary — that is,
   * your app also works with the sound turned off.
   * See https://developer.apple.com/documentation/avfaudio/avaudiosession/category/1616560-ambient
   **/
  Ambient = 'ambient',
  /**
   * The default audio session category.
   * See https://developer.apple.com/documentation/avfaudio/avaudiosession/category/1616488-soloambient
   **/
  SoloAmbient = 'soloAmbient',
  /**
   * The category for recording audio while also silencing playback audio.
   * See https://developer.apple.com/documentation/avfaudio/avaudiosession/category/1616451-record
   **/
  Record = 'record',
}
