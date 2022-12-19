import { NativeModules } from 'react-native';
const { TrackPlayerModule: TrackPlayer } = NativeModules;

export enum RatingType {
  Heart = TrackPlayer.RATING_HEART,
  ThumbsUpDown = TrackPlayer.RATING_THUMBS_UP_DOWN,
  ThreeStars = TrackPlayer.RATING_3_STARS,
  FourStars = TrackPlayer.RATING_4_STARS,
  FiveStars = TrackPlayer.RATING_5_STARS,
  Percentage = TrackPlayer.RATING_PERCENTAGE,
}
