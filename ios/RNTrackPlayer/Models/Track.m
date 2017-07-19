//
//  Track.m
//  RNTrackPlayer
//
//  Created by David Chavez on 7/1/17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

#import "Track.h"

@implementation Track

- (id)initWithDictionary:(NSDictionary *)dict {
    self = [super init];
    if (self) {
        _identifier = [dict objectForKey:@"id"];
        
        _url = [[MediaURL alloc] initWithObject:[dict objectForKey:@"url"]];
        _duration = [dict objectForKey:@"duration"] != nil ? [dict objectForKey:@"duration"] : @(0);
        
        _title = [dict objectForKey:@"title"];
        _artist = [dict objectForKey:@"artist"];
        _album = [dict objectForKey:@"album"];
        _genre = [dict objectForKey:@"genre"];
        _date = [dict objectForKey:@"date"];
        _desc = [dict objectForKey:@"description"];
        _artwork = [[MediaURL alloc] initWithObject:[dict objectForKey:@"artwork"]];
    }
    
    return self;
}

- (BOOL)needsNetwork {
    return !_url.isLocal || !_artwork.isLocal;
}

@end
