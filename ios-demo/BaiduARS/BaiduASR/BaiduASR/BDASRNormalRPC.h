//
//  BDASRNormalRPC.h
//  BaiduASR
//
//  Created by Wu,Yunpeng01 on 2020/5/17.
//  Copyright © 2020 Cloud. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol BDASRNormalRPCDelegate <NSObject>

@optional

/**
 * ASR分析结果回调
 */
- (void)normalAnalizeDone:(BOOL)done result:(id _Nullable )response error:(NSError* _Nullable)error;

@end

@interface BDASRNormalRPC : NSObject

@property(nonatomic, weak) id<BDASRNormalRPCDelegate> delegate;

- (void)configHost;

- (void)configRecorder;

- (void)startRecord;

- (void)stopRecord;

- (void)cancel;

@end

NS_ASSUME_NONNULL_END
