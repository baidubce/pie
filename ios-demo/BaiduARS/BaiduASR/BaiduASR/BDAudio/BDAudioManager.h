//
//  BDAudioManager.h
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng(AI2B) on 2019/4/22.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BDAudioManager : NSObject

+ (instancetype)recorder;

- (BOOL)checkPermission;

- (BOOL)configRecorder;

- (void)resetRecorder;

- (void)startRecorder;

- (void)pauseRecorder;

- (void)stopRecorder;

- (NSString *)currentRecorderFilePath;

@end

NS_ASSUME_NONNULL_END
