import type { ResourceObject } from './ResourceObject';
import { MediaItemPlayable } from '../constants';


export interface MediaItem {
  mediaId: string;
  title: string;
  subtitle?: string;
  mediaUri?: string | ResourceObject;
  iconUri?: string | ResourceObject;
  playableFlag: number;
}