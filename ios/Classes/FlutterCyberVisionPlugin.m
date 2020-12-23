#import "FlutterCyberVisionPlugin.h"
#if __has_include(<flutter_cyber_vision/flutter_cyber_vision-Swift.h>)
#import <flutter_cyber_vision/flutter_cyber_vision-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_cyber_vision-Swift.h"
#endif

@implementation FlutterCyberVisionPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterCyberVisionPlugin registerWithRegistrar:registrar];
}
@end
