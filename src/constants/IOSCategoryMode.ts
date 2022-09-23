export enum IOSCategoryMode {
  /**
   * The default audio session mode.
   * See https://developer.apple.com/documentation/avfaudio/avaudiosession/mode/1616579-default
   **/
  Default = 'default',
  /**
   * A mode that the GameKit framework sets on behalf of an application that
   * uses GameKit’s voice chat service.
   * See https://developer.apple.com/documentation/avfaudio/avaudiosession/mode/1616511-gamechat
   **/
  GameChat = 'gameChat',
  /**
   * A mode that indicates that your app is performing measurement of audio
   * input or output.
   * See https://developer.apple.com/documentation/avfaudio/avaudiosession/mode/1616608-measurement
   **/
  Measurement = 'measurement',
  /** A mode that indicates that your app is playing back movie content.
   * See https://developer.apple.com/documentation/avfaudio/avaudiosession/mode/1616623-movieplayback
   **/
  MoviePlayback = 'moviePlayback',
  /** A mode used for continuous spoken audio to pause the audio when another
   * app plays a short audio prompt. See https://developer.apple.com/documentation/avfaudio/avaudiosession/mode/1616510-spokenaudio */
  SpokenAudio = 'spokenAudio',
  /**
   * A mode that indicates that your app is engaging in online video conferencing.
   * See https://developer.apple.com/documentation/avfaudio/avaudiosession/mode/1616590-videochat
   **/
  VideoChat = 'videoChat',
  /**
   * A mode that indicates that your app is recording a movie.
   * See https://developer.apple.com/documentation/avfaudio/avaudiosession/mode/1616535-videorecording
   **/
  VideoRecording = 'videoRecording',
  /**
   * A mode that indicates that your app is performing two-way voice communication,
   * such as using Voice over Internet Protocol (VoIP).
   * See https://developer.apple.com/documentation/avfaudio/avaudiosession/mode/1616455-voicechat
   **/
  VoiceChat = 'voiceChat',
  /**
   * A mode that indicates that your app plays audio using text-to-speech.
   * See https://developer.apple.com/documentation/avfaudio/avaudiosession/mode/2962803-voiceprompt
   **/
  VoicePrompt = 'voicePrompt',
}
