using ReactNative.Bridge;
using ReactNative.Modules.Core;
using ReactNative.UIManager;
using System;
using System.Collections.Generic;

namespace TrackPlayer
{ // <- Put the bracket in a new line here to prevent linking errors

    public class TrackPlayerPackage : IReactPackage {

        public IReadOnlyList<INativeModule> CreateNativeModules(ReactContext reactContext) {
            return new List<INativeModule> {
                new TrackPlayerModule(reactContext)
            };
        }
        
        public IReadOnlyList<Type> CreateJavaScriptModulesConfig() {
            return new List<Type>(0);
        }
        
        public IReadOnlyList<IViewManager> CreateViewManagers(ReactContext reactContext) {
            // TODO: CastButton with SharpCaster
            // https://github.com/Tapanila/SharpCaster
            return new List<IViewManager>(0);
        }
    }
}
