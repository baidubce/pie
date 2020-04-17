//
//  BDAudioPlayer.h
//  BaiduTTS
//
//  Created by Wu,Yunpeng01 on 2020/4/16.
//  Copyright © 2020 ACG. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BDAudioPlayer : NSObject

+ (instancetype)player;

- (BOOL)isPlaying;

- (void)playWithURL:(NSURL *)url;

@end

NS_ASSUME_NONNULL_END
