import { observable } from 'mobx';

class Track {
  @observable title = "Demo";
  @observable artist = "David Chavez";
  @observable artwork;
}  

export default new Track();
