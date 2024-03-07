/************************************************
 * NOTE: this is the entrypoint for web
 ************************************************/
import { AppRegistry } from 'react-native';
import App from './App';
import TrackPlayer from 'react-native-track-player';
import { PlaybackService } from './services';
import 'mux.js';

/******************************************
 * BEGIN: react-native-vector-icons import
 ******************************************/
// Generate required css
// @ts-expect-error: ts doesn't like this for some reason
import iconFont from 'react-native-vector-icons/Fonts/FontAwesome6_Solid.ttf';
const iconFontStyles = `@font-face {
  src: url(${iconFont});
  font-family: FontAwesome6_Solid;
}`;

// Create stylesheet
const style: HTMLStyleElement = document.createElement('style');
style.type = 'text/css';
if ('styleSheet' in style) {
  type CSSTextStyleElement = { styleSheet: { cssText: string } };
  (style as unknown as CSSTextStyleElement).styleSheet.cssText = iconFontStyles;
} else {
  style.appendChild(document.createTextNode(iconFontStyles));
}

// Inject stylesheet
document.head.appendChild(style);
/******************************************
 * END: react-native-vector-icons import
 ******************************************/

const appName = 'RNTP Example';

AppRegistry.registerComponent(appName, () => App);
AppRegistry.runApplication(appName, {
  // Mount the react-native app in the 'root' div of index.html
  rootTag: document.getElementById('root'),
});

TrackPlayer.registerPlaybackService(() => PlaybackService);
