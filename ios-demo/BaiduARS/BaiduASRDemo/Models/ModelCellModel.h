//
//  ModelCellModel.h
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng01 on 2019/9/2.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ModelCellModel : NSObject

@property (nonatomic, strong) NSString *titleString;
@property (nonatomic, strong) NSString *valueString;

@property (nonatomic, assign) float cellHeight;

@end

NS_ASSUME_NONNULL_END
