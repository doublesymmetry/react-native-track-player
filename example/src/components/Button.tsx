import React from 'react';
import {
  StyleSheet,
  Text,
  TouchableWithoutFeedback,
} from 'react-native';

export interface ButtonProps {
  title: string;
  onPress: () => void;
  type?: 'primary' | 'secondary';
}

export const Button: React.FC<ButtonProps> = ({
  title,
  onPress,
  type = 'primary',
}) => {
  const style = type == 'primary' ? styles.primary : styles.secondary;
  return (
    <TouchableWithoutFeedback onPress={onPress}>
      <Text style={style}>{title}</Text>
    </TouchableWithoutFeedback>
  );
};

const styles = StyleSheet.create({
  primary: {
    fontSize: 18,
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
