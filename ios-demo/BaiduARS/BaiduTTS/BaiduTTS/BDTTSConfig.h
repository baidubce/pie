//
//  BDTTSConfig.h
//  BaiduTTS
//
//  Created by Wu,Yunpeng01 on 2020/4/16.
//  Copyright © 2020 ACG. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BDTTSConfig : NSObject

/// 语言选择，填写zh
@property (nonatomic, copy) NSString *lan;

/// 填写993
@property (nonatomic, copy) NSString *pdt;

/// 填写1
@property (nonatomic, copy) NSString *ctp;

/// 用户唯一标识，已读取设备MAC ADRESS
@property (nonatomic, copy) NSString *cuid;

/// 语速，取值0-15，默认为5中语速
@property (nonatomic, copy) NSString *spd;

/// 音调，取值0-15，默认为5中语调
@property (nonatomic, copy) NSString *pit;

/// 音量，取值0-15，默认为5中音量
@property (nonatomic, copy) NSString *vol;

/// 输出格式，默认为3，MP3;  4为pcm-16k；5为pcm-8k；6为wav（内容同pcm-16k）; 注意aue=4，5，6是语音识别要求的格式，但是音频内容不是语音识别要求的自然人发音，所以识别效果会受影响。
@property (nonatomic, copy) NSString *aue;

/// 说话人，请填写5117
@property (nonatomic, copy) NSString *per;

/**
 * ak & sk
 */
@property (nonatomic, copy) NSString *accessKey;
@property (nonatomic, copy) NSString *secrityKey;

@end

NS_ASSUME_NONNULL_END
