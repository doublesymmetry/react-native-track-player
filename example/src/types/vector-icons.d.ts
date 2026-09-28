declare module 'react-native-vector-icons/Ionicons' {
  import type { ComponentType } from 'react';
  import type { ColorValue, TextProps } from 'react-native';

  const Ionicons: ComponentType<
    TextProps & {
      name: string;
      size?: number;
      color?: ColorValue;
    }
  >;
  export default Ionicons;
}
