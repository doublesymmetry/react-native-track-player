import React, { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  SafeAreaView,
  StatusBar,
  StyleSheet,
  View,
  Platform,
} from 'react-native';
import TrackPlayer, { useActiveTrack } from 'react-native-track-player';

import { Button, PlayerControls, Progress, TrackInfo } from './components';
import { QueueInitialTracksService, SetupService } from './services';

const App: React.FC = () => {
  const track = useActiveTrack();
  const [isPlayerReady, setIsPlayerReady] = useState<boolean>(false);

  useEffect(() => {
    let unmounted = false;
    (async () => {
      const isSetup = await SetupService();
      if (unmounted) return;
      setIsPlayerReady(isSetup);
      const queue = await TrackPlayer.getQueue();
      if (unmounted) return;
      if (isSetup && queue.length <= 0) {
        await QueueInitialTracksService();
      }
    })();
    return () => {
      unmounted = true;
    };
  }, []);

  if (!isPlayerReady) {
    return (
      <SafeAreaView style={styles.screenContainer}>
        <ActivityIndicator />
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.screenContainer}>
      <StatusBar barStyle={'light-content'} />
      <View style={styles.contentContainer}>
        <View style={styles.topBarContainer}>
          <Button
            title="Queue"
            onPress={() => console.log('TODO: implement queue interface')}
            type="primary"
          />
        </View>
        <TrackInfo track={track} />
        <Progress live={track?.isLiveStream} />
      </View>
      <View style={styles.actionRowContainer}>
        <PlayerControls />
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  screenContainer: {
    flex: 1,
    backgroundColor: '#212121',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: Platform.OS === 'web' ? '100vh' : '100%',
  },
  contentContainer: {
    flex: 3,
    alignItems: 'center',
  },
  topBarContainer: {
    width: '100%',
    flexDirection: 'row',
    paddingHorizontal: 20,
    justifyContent: 'flex-end',
  },
  actionRowContainer: {
    flex: 1,
    flexDirection: 'row',
  },
});

export default App;
