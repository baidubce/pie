//
//  TTSConfig.m
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng01 on 2020/4/16.
//  Copyright © 2020 Cloud. All rights reserved.
//

#import "TTSConfig.h"
#import <BaiduTTS/BDTTSInstance.h>

static TTSConfig *ttsConfig = nil;
@implementation TTSConfig

+ (instancetype)config {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        ttsConfig = [[TTSConfig alloc] init];
    });
    return ttsConfig;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        NSString *per = [defaults objectForKey:@"per"];
        NSString *spd = [defaults objectForKey:@"spd"];
        NSString *pit = [defaults objectForKey:@"pit"];
        NSString *vol = [defaults objectForKey:@"vol"];
        
        self.per = per ?: @"5117";
        self.spd = spd ?: @"5";
        self.pit = pit ?: @"5";
        self.vol = vol ?: @"5";
    }
    return self;
}

- (void)save {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    NSString *per = self.per ?: @"";
    NSString *spd = self.spd ?: @"";
    NSString *pit = self.pit ?: @"";
    NSString *vol = self.vol ?: @"";

    [defaults setObject:per forKey:@"per"];
    [defaults setObject:spd forKey:@"spd"];
    [defaults setObject:pit forKey:@"pit"];
    [defaults setObject:vol forKey:@"vol"];

    [defaults synchronize];
    
    BDTTSInstance *ins = [BDTTSInstance sharedInstance];
    [ins setPer:per];
    [ins setSpd:spd];
    [ins setPit:pit];
    [ins setVol:vol];
}

- (void)reset {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    [defaults removeObjectForKey:@"per"];
    [defaults removeObjectForKey:@"spd"];
    [defaults removeObjectForKey:@"pit"];
    [defaults removeObjectForKey:@"vol"];
    [defaults synchronize];
}

@end
