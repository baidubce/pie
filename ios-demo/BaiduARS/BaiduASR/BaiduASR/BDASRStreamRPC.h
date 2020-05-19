//
//  BDASRStreamRPC.h
//  BaiduASR
//
//  Created by Wu,Yunpeng01 on 2020/5/13.
//  Copyright © 2020 Cloud. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN



@protocol BDASRStreamRPCDelegate <NSObject>

/**
 * ASR实时结果回调
 */
- (void)realTimeAnalizeResult:(id _Nullable )result error:(NSError* _Nullable)error;

@end

@interface BDASRStreamRPC : NSObject

@property(nonatomic, weak) id<BDASRStreamRPCDelegate> delegate;

- (void)configHost;

- (void)configRecorder;

- (void)startStream;

- (void)stopStream;

@end

NS_ASSUME_NONNULL_END
