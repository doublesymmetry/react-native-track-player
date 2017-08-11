//
//  MediaURL.m
//  RNTrackPlayer
//
//  Created by David Chavez on 7/1/17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

#import "MediaURL.h"

@implementation MediaURL

- (id)initWithObject:(id)object {
    self = [super init];
    if (self) {
        _isLocal = [object isKindOfClass:[NSDictionary class]];
        
        if (_isLocal) {
            _value = [[NSURL alloc] initWithString:object[@"uri"]];
        } else {
            _value = [[NSURL alloc] initWithString:object];;
        }
    }
    
    return self;
}

@end
