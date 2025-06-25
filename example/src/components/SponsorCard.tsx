import React from 'react';
import { StyleSheet, Text, View } from 'react-native';

export const SponsorCard: React.FC = () => {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>RNTP Pro</Text>
      <Text style={styles.body}>
        If your project or business has found value in using RNTP, please
        consider sponsoring it. Sponsors will receive additional benefits and
        help us continue the ongoing development and maintenance of this project
        under the Apache-2.0 license.
      </Text>
      <Text style={styles.link}>github.com/sponsors/doublesymmetry</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#2a2a2a',
    borderRadius: 8,
    padding: 16,
    width: '94%',
    marginBottom: 16,
  },
  title: {
    fontSize: 22,
    fontWeight: '600',
    color: '#fff',
  },
  body: {
    marginVertical: 8,
    fontSize: 16,
    fontWeight: '400',
    color: '#ddd',
  },
  link: {
    marginVertical: 8,
    fontSize: 16,
    fontWeight: '400',
    color: '#FFD479',
  },
});
