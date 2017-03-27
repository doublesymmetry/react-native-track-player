const React = require('react');
const ReactNative = require('react-native');

// TODO: make video work
class Video extends React.Component {
    render() {
        return <NativeVideo {...this.props} />;
    }
}

Video.propTypes = {
    player: React.PropTypes.number
};

const NativeVideo = ReactNative.requireNativeComponent('TrackPlayerView', Video);

module.exports = Video;