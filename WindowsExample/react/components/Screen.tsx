import { Component } from 'react';

export default class Screen extends Component<{navigation: any}> {
  navigateTo = (path: any) => {
    this.props.navigation.navigate(path);
  };
}
