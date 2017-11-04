import React, { Component } from 'react';
import { UIManager, View, requireNativeComponent, findNodeHandle, processColor } from 'react-native';
import PropTypes from 'prop-types';

class CastButton extends Component {

    constructor() {
        super();
    }

    showDialog() {
        const handle = findNodeHandle(this.nativeComponent);
        UIManager.dispatchViewManagerCommand(handle, 1, null);
    }

    render() {
        const {color, ...otherProps} = this.props;

        return <NativeCastButton
            {...otherProps}
            color={processColor(color)}
            ref={(component) => { this.nativeComponent = component; }}
        />;
    }

}

CastButton.propTypes = {
    color: PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.number
    ]),
    ...View.propTypes
};

const NativeCastButton = requireNativeComponent('TrackPlayerCastButton', CastButton);

module.exports = CastButton;
