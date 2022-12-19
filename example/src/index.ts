/************************************************
 * NOTE: this is the entrypoint for web
 ************************************************/
import {AppRegistry} from 'react-native';
import App from './App';

const appName = 'Your app name';

AppRegistry.registerComponent(appName, () => App);
AppRegistry.runApplication(appName, {
  // Mount the react-native app in the 'root' div of index.html
  rootTag: document.getElementById('root'),
});
