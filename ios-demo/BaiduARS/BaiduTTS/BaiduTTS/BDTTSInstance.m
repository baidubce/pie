//
//  BDTTSInstance.m
//  BaiduTTS
//
//  Created by Wu,Yunpeng01 on 2020/4/9.
//  Copyright © 2020 ACG. All rights reserved.
//

#import "BDTTSInstance.h"
#import "BDTTSNetwork/BDTTSRequest.h"
#import "BDTTSMacro.h"
#import "BDAudioPlayer.h"
#import "BDTTSConfig.h"

static BDTTSInstance *ttsInstance = nil;

@interface BDTTSInstance() <BDAudioPlayerDelegate>

@property (nonatomic, strong) BDTTSConfig *config;

@property (nonatomic, copy) NSString *textCache;
@property (nonatomic, copy) NSURL *urlCache;

@end

@implementation BDTTSInstance

+ (instancetype)sharedInstance {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        ttsInstance = [[BDTTSInstance alloc] init];
        [ttsInstance loadConfig];
    });
    return ttsInstance;
}

- (void)loadConfig {
    BDTTSConfig *config = [[BDTTSConfig alloc] init];
    self.config = config;
}

- (void)setPer:(NSString *)per {
    self.config.per = per;
}

- (void)setPit:(NSString *)pit {
    self.config.pit = pit;
}

- (void)setSpd:(NSString *)spd {
    self.config.spd = spd;
}

- (void)setVol:(NSString *)vol {
    self.config.vol = vol;
}

- (void)ttsWithText:(NSString *)text
             isPlay:(BOOL)isPlay
           complete:(void (^ _Nullable )(NSURL * _Nonnull))complete
            failure:(void (^ _Nullable )(NSError * _Nonnull))failure {
    if (self.textCache && self.textCache.length && [self.textCache isEqualToString:text] && self.urlCache) {
        [self playAudioWithURL:self.urlCache];
        if (complete) complete(self.urlCache);
        return;
    }
    
    BDTTSConfig *config = self.config;
    
    NSString *tex = text;
    NSString *lan = config.lan;
    NSString *pdt = config.pdt;
    NSString *ctp = config.ctp;
    NSString *cuid = config.cuid;
    NSString *spd = config.spd;
    NSString *pit = config.pit;
    NSString *vol = config.vol;
    NSString *aue = config.aue;
    NSString *per = config.per;
    
    NSDictionary *parameters = @{@"tex" : tex, @"lan" : lan, @"pdt" : pdt, @"ctp" : ctp, @"cuid" : cuid, @"spd" : spd, @"pit" : pit, @"vol" : vol, @"aue" : aue, @"per" : per};
    
    [BDTTSRequest requestDownload:[BDTTSRequest requestURL] parameters:parameters success:^(NSURL * _Nonnull filePath) {
        if (isPlay) {
            if (filePath) {
                [self playAudioWithURL:filePath];
                self.textCache = text;
                self.urlCache = filePath;
            }
        }

        if (complete) {
            complete(filePath);
        }
    } failure:^(NSError * _Nonnull error) {
        if (failure) {
            failure(error);
        }
    }];
}

- (void)stopPlay {
    [[BDAudioPlayer player] stop];
}

- (void)cancel {
    [BDTTSRequest cancel];
}

- (void)playAudioWithURL:(NSURL *)url {
    [BDAudioPlayer player].delegate = self;
    
    if (![[BDAudioPlayer player] isPlaying]) {
        [[BDAudioPlayer player] playWithURL:url];
    }
}

#pragma mark BDAudioPlayerDelegate
- (void)onBDPlayerFinish {
    if (self.delegate && [self.delegate respondsToSelector:@selector(onBDTTSPlayerFinish)]) {
        [self.delegate onBDTTSPlayerFinish];
    }
}

- (void)onBDPlayerStart {
    if (self.delegate && [self.delegate respondsToSelector:@selector(onBDTTSPlayerStart)]) {
        [self.delegate onBDTTSPlayerStart];
    }
}

- (void)onBDPlayerStop {
    if (self.delegate && [self.delegate respondsToSelector:@selector(onBDTTSPlayerStop)]) {
        [self.delegate onBDTTSPlayerStop];
    }
}

@end
