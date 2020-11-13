//
//  BDAudioPlayer.h
//  BaiduTTS
//
//  Created by Wu,Yunpeng01 on 2020/4/16.
//  Copyright © 2020 ACG. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol BDAudioPlayerDelegate <NSObject>

@optional

- (void)onBDPlayerStart;

- (void)onBDPlayerFinish;

- (void)onBDPlayerStop;

@end

NS_ASSUME_NONNULL_BEGIN

@interface BDAudioPlayer : NSObject

@property (nonatomic, weak) id<BDAudioPlayerDelegate> delegate;

+ (instancetype)player;

- (BOOL)isPlaying;

- (void)playWithURL:(NSURL *)url;

- (void)stop;

@end

NS_ASSUME_NONNULL_END
