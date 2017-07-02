//
//  TrackURL.m
//  RNTrackPlayer
//
//  Created by David Chavez on 7/1/17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

#import "TrackURL.h"

@implementation TrackURL

- (id)initWithObject:(id)object {
    self = [super init];
    if (self) {
        // Check if local url
        _isLocal = [object isKindOfClass:[NSDictionary class]];
        
        if (_isLocal) {
            // TODO: - Figure out how to load local assets
        } else {
            _value = [[NSURL alloc] initWithString:object];
        }
    }
    
    return self;
}

@end
