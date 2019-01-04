import React, { Component } from 'react';
import TrackPlayer from 'react-native-track-player';
import { StackNavigator } from 'react-navigation';

import PlayerStore from './react/stores/Player';
import TrackStore from './react/stores/Track';

import LandingScreen from './react/screens/LandingScreen';
import PlaylistScreen from './react/screens/PlaylistScreen';

const RootStack = StackNavigator({
  Landing: {
    screen: LandingScreen,
  },
  Playlist: {
    screen: PlaylistScreen,
  },
}, { initialRouteName: 'Landing' })

export default class App extends Component {

  componentDidMount() {
    this._onTrackChanged = TrackPlayer.addEventListener('playback-track-changed', async (data) => {
      if (data.nextTrack) {
        const track = await TrackPlayer.getTrack(data.nextTrack);
        TrackStore.title = track.title;
        TrackStore.artist = track.artist;
        TrackStore.artwork = track.artwork;
      }
    })

    this._onStateChanged = TrackPlayer.addEventListener('playback-state', (data) => {
      PlayerStore.playbackState = data.state;
    })
    
    
    this._updateIcyHeader = TrackPlayer.addEventListener('icy-header-update', (data) => {
      console.log("Update IceCast Header!", data)
    })
    this._updateIcyMetaData = TrackPlayer.addEventListener('icy-metadata-update', this.icyMetaDataShow(data))
  }

  componentWillUnmount() {
    this._onTrackChanged.remove()
    this._onStateChanged.remove()

    this._updateIcyHeader.remove()
    this._updateIcyMetaData.remove()
  }

  findGetParameter(url) {
    var result = {};
    url.substr(1).split("&").forEach(function (item) {
      const tmp = item.split("=");
      result[decodeURIComponent(tmp[0])] = decodeURIComponent(tmp[1]);
    });
    return result;
  }

  icyMetaDataShow = async ({streamTitle, streamUrl}) =>{   
    console.log("Update IceCast MetaData!", streamTitle); 
    const curTrack = await TrackPlayer.getCurrentTrack();

    if(curTrack != null) {
      const Track = await TrackPlayer.getTrack(curTrack);
      
      let music = 'Unknow';
      let singer = '...';
      streamTitle = streamTitle.trim();
      if(streamTitle != '' && streamTitle.toUpperCase().indexOf('ADVERT:') != 0) {
          const corte = streamTitle.indexOf("-");
          if(corte > 0) {
              music = streamTitle.substr(0, corte).trim();
              singer = streamTitle.substr(corte+1, streamTitle.length-(corte+1)).trim();
          } else {
              music = streamTitle.substr(0, streamTitle.length).trim();
              singer = '';
          }
      } else {
        // Advert metadata
      }

      Track.title = music;
      Track.artist = singer;
      TrackPlayer.updateMetadata(Track);
      
      TrackStore.title = Track.title;
      TrackStore.artist = Track.artist;

      const params = this.findGetParameter(streamUrl);
      console.log("MetadataUpdate, Url params:", params, "Stream Title:", streamTitle);
    }
  }

  render() {
    return (
      <RootStack />
    );
  }
}
