import { observable } from 'mobx';

class Player {
  @observable playbackState;
}  

export default new Player();
