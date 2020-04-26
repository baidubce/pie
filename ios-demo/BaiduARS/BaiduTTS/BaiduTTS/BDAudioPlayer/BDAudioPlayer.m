//
//  BDAudioPlayer.m
//  BaiduTTS
//
//  Created by Wu,Yunpeng01 on 2020/4/16.
//  Copyright © 2020 ACG. All rights reserved.
//

#import "BDAudioPlayer.h"
#import <AVFoundation/AVFoundation.h>

static BDAudioPlayer *player = nil;

@interface BDAudioPlayer()

@property (nonatomic, strong) AVAudioPlayer *avPlayer;

@end

@implementation BDAudioPlayer

+ (instancetype)player {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        player = [[BDAudioPlayer alloc] init];
    });

    if ([AVAudioSession sharedInstance].category != AVAudioSessionCategoryPlayback) {
        [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error: nil];
    }
    
    return player;
}

- (BOOL)isPlaying {
    return self.avPlayer && self.avPlayer.isPlaying;
}

- (void)playWithURL:(NSURL *)url {    
    NSError *error = nil;
    self.avPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:url error:&error];
    
    if (error) {
        NSLog(@"BDAudioPlayer initiate failed with invaild url !");
    }
    
    [self.avPlayer prepareToPlay];
    [self.avPlayer play];
}

    
@end
