import { observable } from 'mobx';

export const playbackStates = Object.freeze({
  playing: 'playing',
  halted: 'halted',
})
class Track {
  @observable title = "Demo";
  @observable artist = "David Chavez";
  @observable artwork;
  @observable playbackState = playbackStates.halted;
}  

export default new Track();
