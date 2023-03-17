

export type EqualizerSettings = {
  activePreset?: string;
  bandCount: number;
  bandLevels: number[];
  centerBandFrequencies: number[];
  enabled: boolean;
  lowerBandLevelLimit: number;
  presets: string[];
  upperBandLevelLimit: number;
}
