import React from 'react';
import {
  Text,
  TouchableOpacity,
  StyleSheet
} from 'react-native';
import { ControlButton } from './ControlButton';

const styles = StyleSheet.create({
  container: {
    flex: 1
  },
  text: {
    fontSize: 18,
    textAlign: 'center'
  },
  inactive: {
    opacity: '50%'
  }
});

const skipToNext = () => {
  TrackPlayer.skipToNext();
};

const skipToPrevious = () => {
  TrackPlayer.skipToPrevious();
};

export default function Controls({ paused, hasNext, hasPrevious }) {
  const [paused, setPaused] = useState(null);
  const [hasNext, setHasNext] = useState(true);
  const [hasPrevious, setHasPrevious] = useState(false);

  useTrackPlayerEvents([TrackPlayer.PLAYBACK_STATE], ({ state }) => {
    setPaused(state === STATE_PAUSED);
  });

  useTrackPlayerEvents([PLAYBACK_TRACK_CHANGED], async ({ nextTrack }) => {
    const queue = await TrackPlayer.getQueue();
    const index = queue.findIndex(({ id }) => id === nextTrack);
    setHasNext(index < tracks.length - 1);
    setHasPrevious(index > 0);
  });

  const togglePlayback = () => {
    if (paused) {
      TrackPlayer.play();
    } else {
      TrackPlayer.pause();
    }
  };

  return (
    <View style={styles.controls}>
      <ControlButton
        title={'<<'}
        active={hasNext}
        onPress={skipToPrevious}
      />
      <ControlButton title={paused ? 'Play' : 'Pause'} onPress={togglePlayback} />
      <ControlButton
        title={'>>'}
        active={hasPrevious}
        onPress={skipToNext}
      />
    </View>
  );
}

ControlButton.propTypes = {
  title: PropTypes.string.isRequired,
  onPress: PropTypes.func.isRequired
};
