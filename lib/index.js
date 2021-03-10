import TrackPlayer from './trackPlayer';

// Player Event Types
const TrackPlayerEvents = require('./eventTypes');

// Components
const ProgressComponent = require('./ProgressComponent');

// React Hooks (Requires React v16.8+ and React Native v0.59+)
const hooks = require('./hooks');

module.exports = {
  ...TrackPlayer,
  ...hooks,
  TrackPlayerEvents,
  ProgressComponent,
};
