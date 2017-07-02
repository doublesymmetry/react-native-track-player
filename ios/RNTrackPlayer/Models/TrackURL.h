//
//  TrackURL.h
//  RNTrackPlayer
//
//  Created by David Chavez on 7/1/17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TrackURL : NSObject
@property(strong, nonatomic) NSURL *value;
@property(assign, nonatomic) bool isLocal;

- (id)initWithObject:(id)object;
@end
