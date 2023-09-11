import type { ResourceObject } from './ResourceObject';

export interface CustomButtons {
  customActionsList: string[];
  [key: string]: string[] | ResourceObject;
}
