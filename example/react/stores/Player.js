import { observable } from 'mobx';

class Player {
  @observable playbackState = null;
}

export default new Player();
