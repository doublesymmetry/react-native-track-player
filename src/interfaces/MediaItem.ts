import type { ResourceObject } from './ResourceObject';
import { MediaItemPlayable } from '../constants';
import { AndroidAutoContentStyle } from '../constants/AndroidAutoContentStyle';

export interface MediaItem {
  mediaId: string;
  title: string;
  subtitle?: string;
  /**mediaUri doesn't seem to work. use Event.RemotePlayId to handle playback instead. */
  mediaUri?: string | ResourceObject;
  iconUri?: string | ResourceObject;
  /** playable has 2 states: 1 as browsable, or any other as playable.  */
  playable: MediaItemPlayable.MediaBrowsable | string;
  groupTitle?: string;
  contentStyle?: AndroidAutoContentStyle;
  childrenPlayableContentStyle?: AndroidAutoContentStyle;
  childrenBrowsableContentStyle?: AndroidAutoContentStyle;
}
