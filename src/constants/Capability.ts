import { Constants } from '../NativeTrackPlayer';

export enum Capability {
  Play = Constants?.CAPABILITY_PLAY ?? 1,
  PlayFromId = Constants?.CAPABILITY_PLAY_FROM_ID ?? 2,
  PlayFromSearch = Constants?.CAPABILITY_PLAY_FROM_SEARCH ?? 3,
  Pause = Constants?.CAPABILITY_PAUSE ?? 4,
  Stop = Constants?.CAPABILITY_STOP ?? 5,
  SeekTo = Constants?.CAPABILITY_SEEK_TO ?? 6,
  Skip = Constants?.CAPABILITY_SKIP ?? 7,
  SkipToNext = Constants?.CAPABILITY_SKIP_TO_NEXT ?? 8,
  SkipToPrevious = Constants?.CAPABILITY_SKIP_TO_PREVIOUS ?? 9,
  JumpForward = Constants?.CAPABILITY_JUMP_FORWARD ?? 10,
  JumpBackward = Constants?.CAPABILITY_JUMP_BACKWARD ?? 11,
  SetRating = Constants?.CAPABILITY_SET_RATING ?? 12,
}
