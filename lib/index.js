import TrackPlayer from './trackPlayer';
// React Hooks (Requires React v16.8+ and React Native v0.59+)
const hooks = require('./hooks');

// Player Event Types
const TrackPlayerEvents = require('./eventTypes');

// Components
const ProgressComponent = require('./ProgressComponent');

module.exports = { 
    ...TrackPlayer, 
    ...hooks, 
    ProgressComponent, 
    TrackPlayerEvents 
}
