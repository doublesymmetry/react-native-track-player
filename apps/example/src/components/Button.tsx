import React from 'react';
import {
  StyleSheet,
  Text,
  type TextStyle,
  TouchableWithoutFeedback,
} from 'react-native';

export interface ButtonProps {
  title: string;
  onPress: () => void;
  type?: keyof typeof styles;
  style?: TextStyle;
}

export const Button: React.FC<ButtonProps> = ({
  title,
  onPress,
  type = 'primary',
  style,
}) => {
  return (
    <TouchableWithoutFeedback onPress={onPress}>
      <Text style={[styles[type], style]}>{title}</Text>
    </TouchableWithoutFeedback>
  );
};

const styles = StyleSheet.create({
  primary: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFD479',
    padding: 20,
  },
  secondary: {
    fontSize: 14,
    color: '#FFD479',
    padding: 22,
  },
});
