import React, {useState, useEffect} from 'react';
import TrackPlayer from 'react-native-track-player';
import {
  SafeAreaView,
  StatusBar,
  StyleSheet,
  View,
  ActivityIndicator,
} from 'react-native';

import {Button, PlayerControls, Progress, TrackInfo} from './components';
import {SetupService, QueueInitalTracksService} from './services';
import {useCurrentTrack} from './hooks';

const App: React.FC = () => {
  const track = useCurrentTrack();
  const [isPlayerReady, setIsPlayerReady] = useState<boolean>(false);

  useEffect(() => {
    async function run() {
      const isSetup = await SetupService();
      setIsPlayerReady(isSetup);

      const queue = await TrackPlayer.getQueue();
      if (isSetup && queue.length <= 0) {
        await QueueInitalTracksService();
      }
    }

    run();
  }, []);

  if (!isPlayerReady) {
    return (
      <SafeAreaView style={styles.screenContainer}>
        <ActivityIndicator/>
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
        <TrackInfo track={track}/>
        <Progress />
      </View>
      <View style={styles.actionRowContainer}>
        <PlayerControls/>
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
