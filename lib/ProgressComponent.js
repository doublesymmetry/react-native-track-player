import React, { Component } from 'react';
import TrackPlayer from './trackPlayer.js';

class ProgressComponent extends Component {

    constructor(props) {
        super(props);
        this.state = {
            position: 0,
            bufferedPosition: 0,
            duration: 0
        };
    }

    componentDidMount() {
        this._progressUpdates = true;
        this._updateProgress();
        this._timer = setInterval(this._updateProgress.bind(this), 1000);
    }

    componentWillUnmount() {
        this._progressUpdates = false;
        clearInterval(this._timer);
    }

    /**
     * Updates the progress state
     * @private
     */
    async _updateProgress() {
        // TODO check for performance here
        // We can create a new native function to reduces these 3 native calls to only one, if needed
        try {
            const data = {
                position: await TrackPlayer.getPosition(),
                bufferedPosition: await TrackPlayer.getBufferedPosition(),
                duration: await TrackPlayer.getDuration()
            };

            if(this._progressUpdates) {
                this.setState(data);
            }
        } catch(e) {
            // The player is probably not initialized yet, we'll just ignore it
        }
    }

    /**
     * Gets the played progress expressed between 0 and 1
     * @return {number}
     */
    getProgress() {
        if(!this.state.duration || !this.state.position) return 0;

        return this.state.position / this.state.duration;
    }

    /**
     * Gets the buffered progress expressed between 0 and 1
     * @return {number}
     */
    getBufferedProgress() {
        if(!this.state.duration || !this.state.bufferedPosition) return 0;

        return this.state.bufferedPosition / this.state.duration;
    }

}

module.exports = ProgressComponent;
