import { Component } from 'react';

export default class Screen extends Component {
  navigateTo = path => {
    this.props.navigation.navigate(path);
  };
}
