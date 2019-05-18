import React from 'react';
import {
  Image,
  StyleSheet,
  Text,
  View
} from 'react-native';

export function ProgressBar() {
  const [progress, setProgress] = useState(0);
  useTrackPlayerProgress(1000, ({
    position,
    duration
  }) => {
    setProgress(position / duration);
  });
  return (
    <View style={styles.progress}>
      <View style={{ flex: progress, backgroundColor: 'red' }} />
      <View
        style={{ flex: 1 - progress, backgroundColor: 'grey' }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  progress: {
    height: 1,
    width: '90%',
    marginTop: 10,
    flexDirection: 'row'
  }
});
