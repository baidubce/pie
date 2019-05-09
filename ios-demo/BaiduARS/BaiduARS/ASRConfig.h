//
//  ASRConfig.h
//  BaiduARS
//
//  Created by Wu,Yunpeng(AI2B) on 2019/5/9.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASRConfig : NSObject

/**
 * asr流式服务Host地址，私有化版本请咨询供应商
 */
@property (nonatomic, copy) NSString *hostAddress;

/**
 * asr流式服务的端口，私有化版本请咨询供应商
 */
@property (nonatomic, copy) NSString *serverPort;

/**
 * asr识别服务的产品类型，私有化版本请咨询供应商
 */

@property (nonatomic, copy) NSString *productId;

/**
 * 音频采样率
 */
@property (nonatomic, copy) NSString *sampleRate;

/**
 * userName & password
 */
@property (nonatomic, copy) NSString *userName;
@property (nonatomic, copy) NSString *passWord;

/**
 * 产品id 数据源
 */
@property (nonatomic, strong) NSArray *productIDArray;
@property (nonatomic, strong) NSArray *productIDDataSource;

+ (instancetype)config;

- (void)save;

- (void)reset;

- (void)login;

- (void)logout;

@end

NS_ASSUME_NONNULL_END
