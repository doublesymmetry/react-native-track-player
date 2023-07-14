import { NativeModules } from 'react-native';
const { TrackPlayerModule: TrackPlayer } = NativeModules;

export enum MediaItemPlayable {
  MediaPlayable = '0',
  MediaBrowsable = '1',
}
