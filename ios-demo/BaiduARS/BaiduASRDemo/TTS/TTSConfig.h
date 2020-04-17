//
//  TTSConfig.h
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng01 on 2020/4/16.
//  Copyright © 2020 Cloud. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface TTSConfig : NSObject

/// 说话人ID，请填写5117
@property (nonatomic, copy) NSString *per;

/// 语速，spd 取值0-15，默认为5中语速
@property (nonatomic, copy) NSString *spd;

/// 音调， pit 取值0-15，默认为5中语调
@property (nonatomic, copy) NSString *pit;

/// 音量， vol 取值0-15，默认为5中音量
@property (nonatomic, copy) NSString *vol;

+ (instancetype)config;

- (void)save;

- (void)reset;

@end

NS_ASSUME_NONNULL_END
