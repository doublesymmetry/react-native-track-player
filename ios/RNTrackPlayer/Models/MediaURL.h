//
//  MediaURL.h
//  RNTrackPlayer
//
//  Created by David Chavez on 7/1/17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

@import Foundation;

@interface MediaURL: NSObject
@property(strong, nonatomic) NSURL *value;
@property(assign, nonatomic) bool isLocal;

- (id)initWithObject:(id)object;
@end
