#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(LocalAssetHelper, NSObject)

RCT_EXTERN_METHOD(copyToDocuments:(NSString *)resourceName
                  withExtension:(NSString *)ext
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

@end
