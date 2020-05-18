//
//  BDRTAManager.h
//  BaiduASR
//
//  Created by Wu,Yunpeng01 on 2020/5/15.
//  Copyright © 2020 Cloud. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol BDRTADelegate <NSObject>

- (void)returnData:(NSMutableData *)data;

@end

@interface BDRTAManager : NSObject

@property (nonatomic,strong) id<BDRTADelegate>delegate;

- (void)startRecord;
- (void)stopRecord;

@end

NS_ASSUME_NONNULL_END
