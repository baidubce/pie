//
//  SettingViewCellModel.h
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng(AI2B) on 2019/5/22.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface SettingViewCellModel : NSObject

@property (nonatomic, strong) NSString *title;
@property (nonatomic, strong) NSString *descrptionString;
@property (nonatomic, strong) NSString *valueString;
@property (nonatomic, strong) UIColor *titleColor;

@property (nonatomic, assign) BOOL isShowTextField;
@property (nonatomic, assign) BOOL isTextFieldEditEnable;
@property (nonatomic, assign) BOOL isShowSlider;
@property (nonatomic, assign) float cellHeight;

@property (nonatomic, assign) float sliderMin;
@property (nonatomic, assign) float sliderMax;

@end

NS_ASSUME_NONNULL_END
