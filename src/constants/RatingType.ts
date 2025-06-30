import { Constants } from '../NativeTrackPlayer';

export enum RatingType {
  Heart = Constants?.RATING_HEART ?? 1,
  ThumbsUpDown = Constants?.RATING_THUMBS_UP_DOWN ?? 2,
  ThreeStars = Constants?.RATING_3_STARS ?? 3,
  FourStars = Constants?.RATING_4_STARS ?? 4,
  FiveStars = Constants?.RATING_5_STARS ?? 5,
  Percentage = Constants?.RATING_PERCENTAGE ?? 6,
}
