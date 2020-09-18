/**
 * @format
 */

import {AppRegistry} from 'react-native';
import App from './App';
import Call from './Call';
import {name as appName} from './app.json';

AppRegistry.registerComponent(appName, () => App);
AppRegistry.registerHeadlessTask('Call', () => Call)