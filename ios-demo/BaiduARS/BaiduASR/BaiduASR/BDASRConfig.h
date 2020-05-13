//
//  BDASRConfig.h
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng(AI2B) on 2019/4/15.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
    CUSTOMER_SERVICE = 1903,    //客服模型    音频采样率8000
    CUSTOMERSERVICETOUR = 1904,    //客服模型-旅游领域    音频采样率8000
    CUSTOMERSERVICESTOCK = 1905,    //客服模型-股票领域    音频采样率8000
    CUSTOMERSERVICEFINANCE = 1906,    //客服模型-股票领域    音频采样率8000
    CUSTOMERSERVICEENERGY = 1907,    //客服模型-股票领域    音频采样率8000
    SPEECK = 1912,    //客服模型-股票领域    音频采样率8000
    INPUT_METHOD = 888,    //输入法模型    音频采样率16000
    FAR_FIELD = 1888,    //远场模型    音频采样率16000
    FARFIELDROBOT = 1889    //远场模型-机器人领域    音频采样率16000
} ASRProduct;

@interface BDASRConfig : NSObject

+ (instancetype)config;

/**
 * asr流式服务Host地址，私有化版本请咨询供应商
 */
@property (nonatomic, copy) NSString *hostAddress;

/**
 * asr流式服务的端口，私有化版本请咨询供应商
 */
@property (nonatomic, copy) NSString *serverPort;

/**
 * asr识别服务的产品类型id，私有化版本请咨询供应商
 */

@property (nonatomic, copy) NSString *productId;

/**
 * asr客户端的名称，为便于后端查错，请设置一个易于辨识的appName
 */
@property (nonatomic, copy) NSString *appName;

/**
 * 服务端的日志输出级别
 */
//@property (nonatomic, assign) AsrServerLogLevel logLevel = AsrServerLogLevel.INFO;

/**
 * 是否返回中间翻译结果
 */
@property (nonatomic, assign) BOOL enableFlushData;

/**
 * do not change this
 */
@property (nonatomic, assign) int bitDepth;

/**
 * 指定每次发送的音频数据包大小，通常不需要修改
 */
@property (nonatomic, assign) double sendPerSeconds;

/**
 * 指定asr服务的识别间隔，通常不需要修改，不能小于1
 */
@property (nonatomic, assign) double sleepRatio;

/**
 * 识别单个文件的最大等待时间，默认10分，最长不能超过120分
 */
@property (nonatomic, assign) int timeoutMinutes;

/**
 * 日志输出等级
 */
@property (nonatomic, assign) int logLevel;

/**
 * 音频采样率
 */
@property (nonatomic, assign) int sampleRate;

/**
 * userName & password
 */
@property (nonatomic, copy) NSString *userName;
@property (nonatomic, copy) NSString *passWord;

/**
 * ak & sk
 */
@property (nonatomic, copy) NSString *accessKey;
@property (nonatomic, copy) NSString *secrityKey;


- (NSString *)hostAddress_Port;

- (NSString *)getUTCTimeString;

- (NSString *)getTokenWithTimeStamp:(NSString *)timeStamp;

//- (NSString *)getSignKey;

- (NSString *)getAuthorization;

@end

NS_ASSUME_NONNULL_END
