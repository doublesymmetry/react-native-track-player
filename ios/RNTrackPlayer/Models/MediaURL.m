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
            
            NSMutableURLRequest *newRequest = [[NSMutableURLRequest alloc] initWithURL:_value];
            NSURLResponse *response = nil;
            NSError *error = nil;
            
            [newRequest setValue:@"HEAD" forKey:@"HTTPMethod"];
            [NSURLConnection
             sendSynchronousRequest:newRequest
             returningResponse:&response
             error:&error];
            
            if (response && !error) {
                _value = [response URL];
            }
        }
    }
    
    return self;
}

@end
