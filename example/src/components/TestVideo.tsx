import { useVideoPlayer, VideoView } from 'react-native-video';

export default function TestVideo() {
  const player = useVideoPlayer(
    {
      uri: 'https://file-examples.com/storage/fed494e9bd6a999f99b252c/2017/04/file_example_MP4_480_1_5MG.mp4',
    },
    (player) => {
      player.rate = 1.0;
      player.volume = 1.0;
    },
  );

  return (
    <VideoView
      player={player}
      style={{
        aspectRatio: 9 / 16,
        backgroundColor: 'black',
        height: 100,
        width: 200,
      }}
    />
  );
}
