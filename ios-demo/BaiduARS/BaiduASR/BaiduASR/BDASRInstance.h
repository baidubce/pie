//
//  BDASRInstance.h
//  BaiduASR
//
//  Created by Wu,Yunpeng(AI2B) on 2019/5/7.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef enum : NSUInteger {
    NORMAL = 0,
    RECORDING,
    ANALIZING
} BDASRSTATUS;

@protocol BDASRDelegate <NSObject>

@optional
/**
 * sdk当前状态，NORMAL为等待录音，RECORDING为录音中，ANALIZING为语音分析中
 */
- (void)bdasrStatusDidChanged:(BDASRSTATUS)status;
/**
 * ASR分析结果回调
 */
- (void)bdasrAnalizeDone:(BOOL)done result:(id _Nullable )result error:(NSError* _Nullable)error;

/**
 * ASR实时结果回调
 */
- (void)bdasrRealTimeAnalizeResult:(id _Nullable )result error:(NSError* _Nullable)error;

@end

NS_ASSUME_NONNULL_BEGIN

@interface BDASRInstance : NSObject

@property(nonatomic, assign, readonly) BDASRSTATUS status;

@property(nonatomic, weak) id<BDASRDelegate> delegate;

+ (instancetype)shareInstance;

/**
 * 刷新配置，在更新过hostAddress，服务器端口，产品类型，音频采样率后调用
 */
- (void)refreshConfig;

/**
 * 设置用户名密码，userName：用户名，passWord：密码
 */
- (void)setUserName:(NSString *)userName passWord:(NSString *)passWord;

/**
 * 设置用户AK, SK用于鉴权
 */
- (void)setAccessKey:(NSString *)accessKey secrityKey:(NSString *)secrityKey;

/**
 * 设置hostAddress
 */
- (void)setHostAddress:(NSString *)hostAddress;

/**
 * 设置服务器端口
 */
- (void)setServerPort:(NSString *)serverPort;

/**
 * asr识别服务的产品类型，私有化版本请咨询供应商
 */
- (void)setProductId:(NSString *)productId;

/**
 * 设置音频采样率
 */
- (void)setSampleRate:(NSString *)sampleRate;

/**
 * 设置appName
 */
- (void)setAppName:(NSString *)appName;

/**
 * 开始录音
 */
- (void)startRecord;

/**
 * 结束录音并开始ASR，分析结束后会回调delegate
 */
- (void)stopRecord;

/**
 * 取消目前所有的ASR请求
 */
- (void)cancel;

/**
 * 开始实时收音
 */
- (void)startRealTimeRecord;

/**
 * 结束实时收音
 */
- (void)stopRealTimeRecord;

@end

NS_ASSUME_NONNULL_END
