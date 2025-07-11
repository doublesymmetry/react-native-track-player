import { MaterialCommunityIcons } from '@expo/vector-icons';
import { SafeAreaView, TouchableOpacity } from 'react-native';
import TrackPlayer, { Track, useIsPlaying } from 'react-native-track-player';
import playlistData from '@/assets/data/playlist.json';
export default function HomeScreen() {
  async function addTrack() {
    try {
      await TrackPlayer.reset();
      await TrackPlayer.add(playlistData as Track[]);
      await TrackPlayer.play();
    } catch (error) {
      console.log('Error adding track:', error);
    }
  }
  const { playing } = useIsPlaying();
  return (
    <SafeAreaView>
      <TouchableOpacity
        onPress={() => {
          if (playing) {
            TrackPlayer.pause();
          } else {
            addTrack();
          }
        }}
        style={{
          marginHorizontal: 'auto',
          width: 80,
          height: 80,
          justifyContent: 'center',
          alignItems: 'center',
          backgroundColor: '#f0f0f0',
          borderRadius: 40,
          shadowColor: '#000',
          shadowOffset: { width: 0, height: 2 },
          shadowOpacity: 0.25,
          shadowRadius: 3.84,
          elevation: 5,
          marginTop: 60,
        }}
      >
        <MaterialCommunityIcons name={playing ? 'pause' : 'play'} size={32} />
      </TouchableOpacity>
    </SafeAreaView>
  );
}
