import { Component } from 'react';
import TrackPlayer from './index.js';

class ProgressComponent extends Component {

    componentWillMount() {
        setState({position: 0});
        this._timer = setInterval(this._updatePosition.bind(this), 1000);
    }

    componentWillUnmount() {
        clearInterval(this._timer);
    }

    async _updatePosition() {
        let pos = await TrackPlayer.getPosition();
        setState({position: pos});
    }

    getPosition() {
        return this.state.position || 0;
    }

}

module.exports = ProgressComponent;
