//
//  BDTTSInstance.h
//  BaiduTTS
//
//  Created by Wu,Yunpeng01 on 2020/4/9.
//  Copyright © 2020 ACG. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol BDTTSInstanceDelegate <NSObject>

@optional

- (void)onBDTTSPlayerStart;

- (void)onBDTTSPlayerFinish;

- (void)onBDTTSPlayerStop;

@end

NS_ASSUME_NONNULL_BEGIN

@interface BDTTSInstance : NSObject

@property (nullable, weak) id<BDTTSInstanceDelegate> delegate;

+ (instancetype)sharedInstance;

/// 说话人
/// @param per 请填写5117
- (void)setPer:(NSString *)per;

/// 语速
/// @param spd 取值0-15，默认为5中语速
- (void)setSpd:(NSString *)spd;

/// 音调
/// @param pit 取值0-15，默认为5中语调
- (void)setPit:(NSString *)pit;

/// 音量
/// @param vol 取值0-15，默认为5中音量
- (void)setVol:(NSString *)vol;

/// TTS 服务接口
/// @param text 需要转语音的文字
/// @param isPlay 是否完成转换后播放语音
/// @param complete TTS请求成功回调，参数filePath为缓存区文件url
/// @param failure TTS请求失败回调
- (void)ttsWithText:(NSString *)text
             isPlay:(BOOL)isPlay
           complete:(void (^ _Nullable )(NSURL *filePath))complete
            failure:(void (^ _Nullable )(NSError *error))failure;

/// 播放停止
- (void)stopPlay;

/// 取消请求
- (void)cancel;

@end

NS_ASSUME_NONNULL_END
