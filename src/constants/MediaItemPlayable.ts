import { NativeModules } from 'react-native';
const { TrackPlayerModule: TrackPlayer } = NativeModules;

export enum MediaItemPlayable {
    MEDIA_PLAYABLE = TrackPlayer.MEDIA_PLAYABLE,
    MEDIA_BROWSABLE = TrackPlayer.MEDIA_BROWSABLE,
}