//
//  Track.h
//  RNTrackPlayer
//
//  Created by David Chavez on 7/1/17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "TrackURL.h"

@interface Track: NSObject
@property(strong, nonatomic) NSString *identifier;

@property(strong, nonatomic) TrackURL *url;
@property(strong, nonatomic) NSNumber *duration;

@property(strong, nonatomic) NSString *title;
@property(strong, nonatomic) NSString *artist;
@property(strong, nonatomic) NSString *album;
@property(strong, nonatomic) NSString *genre;
@property(strong, nonatomic) NSString *date;
@property(strong, nonatomic) NSString *desc;
@property(strong, nonatomic) TrackURL *artwork;

- (id)initWithDictionary:(NSDictionary *)dict;
- (BOOL)needsNetwork;
@end
