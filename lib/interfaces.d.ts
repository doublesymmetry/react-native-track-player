export declare enum Event {
    PlaybackState = "playback-state",
    PlaybackError = "playback-error",
    PlaybackQueueEnded = "playback-queue-ended",
    PlaybackTrackChanged = "playback-track-changed",
    RemotePlay = "remote-play",
    RemotePlayId = "remote-play-id",
    RemotePlaySearch = "remote-play-search",
    RemotePause = "remote-pause",
    RemoteStop = "remote-stop",
    RemoteSkip = "remote-skip",
    RemoteNext = "remote-next",
    RemotePrevious = "remote-previous",
    RemoteJumpForward = "remote-jump-forward",
    RemoteJumpBackward = "remote-jump-backward",
    RemoteSeek = "remote-seek",
    RemoteSetRating = "remote-set-rating",
    RemoteDuck = "remote-duck",
    RemoteLike = "remote-like",
    RemoteDislike = "remote-dislike",
    RemoteBookmark = "remote-bookmark"
}
declare type ResourceObject = number;
interface FeedbackOptions {
    /** Marks wether the option should be marked as active or "done" */
    isActive: boolean;
    /** The title to give the action (relevant for iOS) */
    title: string;
}
export declare enum TrackType {
    Default = "default",
    Dash = "dash",
    HLS = "hls",
    SmoothStreaming = "smoothstreaming"
}
export declare type RatingType = string | number;
export declare type PitchAlgorithm = string | number;
export declare type Capability = string | number;
export declare type State = string | number;
export interface TrackMetadata {
    duration?: number;
    title: string;
    artist: string;
    album?: string;
    description?: string;
    genre?: string;
    date?: string;
    rating?: number | boolean;
    artwork?: string | ResourceObject;
}
export interface Track extends TrackMetadata {
    id: string;
    url: string | ResourceObject;
    type?: TrackType;
    userAgent?: string;
    contentType?: string;
    pitchAlgorithm?: PitchAlgorithm;
    [key: string]: any;
}
export interface MetadataOptions {
    ratingType?: RatingType;
    jumpInterval?: number;
    likeOptions?: FeedbackOptions;
    dislikeOptions?: FeedbackOptions;
    bookmarkOptions?: FeedbackOptions;
    stopWithApp?: boolean;
    capabilities?: Capability[];
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
}
declare enum IOSCategory {
    Playback = "playback",
    PlayAndRecord = "playAndRecord",
    MultiRoute = "multiRoute",
    Ambient = "ambient",
    SoloAmbient = "soloAmbient",
    Record = "record"
}
declare enum IOSCategoryMode {
    Default = "default",
    GameChat = "gameChat",
    Measurement = "measurement",
    MoviePlayback = "moviePlayback",
    SpokenAudio = "spokenAudio",
    VideoChat = "videoChat",
    VideoRecording = "videoRecording",
    VoiceChat = "voiceChat",
    VoicePrompt = "voicePrompt"
}
declare enum IOSCategoryOptions {
    MixWithOthers = "mixWithOthers",
    DuckOthers = "duckOthers",
    InterruptSpokenAudioAndMixWithOthers = "interruptSpokenAudioAndMixWithOthers",
    AllowBluetooth = "allowBluetooth",
    AllowBluetoothA2DP = "allowBluetoothA2DP",
    AllowAirPlay = "allowAirPlay",
    DefaultToSpeaker = "defaultToSpeaker"
}
export interface PlayerOptions {
    minBuffer?: number;
    maxBuffer?: number;
    playBuffer?: number;
    maxCacheSize?: number;
    iosCategory?: IOSCategory;
    iosCategoryMode?: IOSCategoryMode;
    iosCategoryOptions?: IOSCategoryOptions[];
    waitForBuffer?: boolean;
}
export {};
