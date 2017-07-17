import { View, Component, UIManager, requireNativeComponent, findNodeHandle } from 'react-native';

const NATIVE_REF = 'NativeCastButton';

export default class CastButton extends Component {

    constructor() {
        super();
    }

    showDialog() {
        const handle = findNodeHandle(this.refs[NATIVE_REF]);
        UIManager.dispatchViewManagerCommand(handle, 1, null);
    }

    render() {
        return <NativeCastButton {...this.props} ref={NATIVE_REF} />;
    }

}

const NativeCastButton = ReactNative.requireNativeComponent('TrackPlayerCastButton', CastButton);
