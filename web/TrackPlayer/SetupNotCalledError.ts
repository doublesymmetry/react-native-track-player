export class SetupNotCalledError extends Error {
  constructor() {
    super('You must call `setupPlayer` prior to interacting with the player.');
  }
}
