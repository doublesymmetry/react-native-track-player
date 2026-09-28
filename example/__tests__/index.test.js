const mockRegisterComponent = jest.fn();
const mockRegisterPlaybackSession = jest.fn();
const mockApp = jest.fn(() => null);
let mockAppEvaluations = 0;

jest.mock('react-native', () => ({
  AppRegistry: { registerComponent: mockRegisterComponent },
  NativeModules: {},
  Platform: { OS: 'android' },
}));

jest.mock('@rntp/player', () => ({
  __esModule: true,
  default: { registerPlaybackSession: mockRegisterPlaybackSession },
  DEFAULT_CAST_RECEIVER_APP_ID: 'cast-receiver',
  Event: {},
}));

jest.mock('../src/assets/CallTheSpirit.mp3', () => 1);

jest.mock('../src/App', () => {
  mockAppEvaluations += 1;
  return { __esModule: true, default: mockApp };
});

test('registers headless services without evaluating the UI', () => {
  jest.isolateModules(() => require('../index'));

  expect(mockRegisterPlaybackSession).toHaveBeenCalledWith(expect.any(Function));
  expect(mockRegisterComponent).toHaveBeenCalledWith(
    'TrackPlayerExample',
    expect.any(Function),
  );
  expect(mockAppEvaluations).toBe(0);

  const appProvider = mockRegisterComponent.mock.calls[0][1];
  expect(appProvider()).toBe(mockApp);
  expect(mockAppEvaluations).toBe(1);
});
