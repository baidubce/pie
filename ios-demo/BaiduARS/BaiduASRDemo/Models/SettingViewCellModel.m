//
//  SettingViewCellModel.m
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng(AI2B) on 2019/5/22.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import "SettingViewCellModel.h"

@implementation SettingViewCellModel

- (instancetype)init {
    self = [super init];
    if (self != nil) {
        self.isShowTextField = YES;
        self.isTextFieldEditEnable = YES;
    }
    return self;
}

- (float)cellHeight {
    return self.isShowTextField ? 94.0 : 55.0;
}

- (UIColor *)titleColor {
    if (_titleColor == nil) {
        _titleColor = [UIColor darkTextColor];
    }
    return _titleColor;
}

@end
