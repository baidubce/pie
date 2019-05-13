//
//  BDASRRecognitionResult.h
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng(AI2B) on 2019/4/15.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BDASRRecognitionResult : NSObject

/*
 * 0表示没有错误
 */
@property (nonatomic, assign) int errorCode;

/*
  * 如果 errorCode 不为0，那么会显示具体的错误信息
  */
@property (nonatomic, copy) NSString *errorMessage;

/*
 * 句子的开始时间
 */
@property (nonatomic, weak) NSDate *startTime;

/*
 * 句子的结束时间
 */
@property (nonatomic, weak) NSDate *endTime;

/*
 * 识别结果
 */
@property (nonatomic, copy) NSString *result;

/*
 * 请求唯一标识，用于查错
 */
@property (nonatomic, copy) NSString *serialNum;

/*
 * 该值为 true 的时候，result 是一个完整的句子 * 否则，result 只是中间结果
 */
@property (nonatomic, assign) BOOL completed;

@end

NS_ASSUME_NONNULL_END
