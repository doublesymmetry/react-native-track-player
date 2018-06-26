import PropTypes from 'prop-types';
import { observer } from 'mobx-react';
import React, { Component } from 'react';
import TrackPlayer, { ProgressComponent } from 'react-native-track-player';
import { Image, StyleSheet, Text, TouchableOpacity, View, ViewPropTypes } from 'react-native';

import TrackStore from '../stores/Track';
import PlayerStore from '../stores/Player';

class ProgressBar extends ProgressComponent {
  render() {
    return (
      <View style={styles.progress}>
        <View style={{ flex: this.getProgress(), backgroundColor: 'red' }} />
        <View style={{ flex: 1 - this.getProgress(), backgroundColor: 'grey' }} />
      </View>
    );
  }
}

function ControlButton({ title, onPress }) {
  return (
    <TouchableOpacity style={styles.controlButtonContainer} onPress={onPress}>
      <Text style={styles.controlButtonText}>{title}</Text>
    </TouchableOpacity>
  );
}

ControlButton.propTypes = {
  title: PropTypes.string.isRequired,
  onPress: PropTypes.func.isRequired,
};

@observer
export default class Player extends Component {
  static propTypes = {
    style: ViewPropTypes.style,
    onNext: PropTypes.func.isRequired,
    onPrevious: PropTypes.func.isRequired,
    onTogglePlayback: PropTypes.func.isRequired,
  };

  static defaultProps = {
    style: {}
  };

  render() {
    const { style, onNext, onPrevious, onTogglePlayback } = this.props;
    var middleButtonText = 'Play'

    if (PlayerStore.playbackState === TrackPlayer.STATE_PLAYING
      || PlayerStore.playbackState === TrackPlayer.STATE_BUFFERING) {
      middleButtonText = 'Pause'
    }

    return (
      <View style={[styles.card, style]}>
        <Image style={styles.cover} source={{ uri: TrackStore.artwork }} />
        <ProgressBar />
        <Text style={styles.title}>{TrackStore.title}</Text>
        <Text style={styles.artist}>{TrackStore.artist}</Text>
        <View style={styles.controls}>
          <ControlButton title={'<<'} onPress={onPrevious} />
          <ControlButton title={middleButtonText} onPress={onTogglePlayback} />
          <ControlButton title={'>>'} onPress={onNext}/>
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  card: {
    width: '80%',
    elevation: 1,
    borderRadius: 4,
    shadowRadius: 2,
    shadowOpacity: 0.1,
    alignItems: 'center',
    shadowColor: 'black',
    backgroundColor: 'white',
    shadowOffset: { width: 0, height: 1},
  },
  cover: {
    width: 140,
    height: 140,
    marginTop: 20,
    backgroundColor: 'grey',
  },
  progress: {
    height: 1,
    width: '90%',
    marginTop: 10,
    flexDirection: 'row',
  },
  title: {
    marginTop: 10,
  },
  artist: {
    fontWeight: 'bold',
  },
  controls: {
    marginVertical: 20,
    flexDirection: 'row',
  },
  controlButtonContainer: {
    flex: 1,
  },
  controlButtonText: {
    fontSize: 18,
    textAlign: 'center',
  },
});
