//
//  SettingViewCell.m
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng(AI2B) on 2019/5/22.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import "SettingViewCell.h"

@interface SettingViewCell()


@end

@implementation SettingViewCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    [self setSelectionStyle:UITableViewCellSelectionStyleNone];
    self.textField.delegate = self;
}

+ (instancetype)cellWithNib {
    NSArray *arr = [[NSBundle mainBundle] loadNibNamed:@"SettingViewCell" owner:nil options:nil];
    if (arr.count > 0) {
        return [arr objectAtIndex:0];
    }
    return [[SettingViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"SettingViewCell"];
}

- (void)setCellModel:(SettingViewCellModel *)cellModel {
    if (_cellModel != cellModel) {
        _cellModel = cellModel;
        
        self.title.text = cellModel.title;
        self.textField.text = cellModel.descrptionString;
        self.title.textColor = cellModel.titleColor;
        self.textField.hidden = !cellModel.isShowTextField;
        self.textField.userInteractionEnabled = cellModel.isTextFieldEditEnable;
        self.slider.hidden = !cellModel.isShowSlider;
        
        if (cellModel.isShowSlider) {
            CGFloat width = self.frame.size.width - 60;
            
            self.sliderWidth.constant = width;
            
            self.slider.minimumValue = cellModel.sliderMin;
            self.slider.maximumValue = cellModel.sliderMax;
            self.slider.value = [cellModel.descrptionString floatValue];
        }else {
            self.sliderWidth.constant = 0;
        }        
    }
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (void)textFieldDidBeginEditing:(UITextField *)textField {
    if ([self.cellDelegate respondsToSelector:@selector(settingViewCellBeginEdit:indexPath:)]) {
        [self.cellDelegate settingViewCellBeginEdit:self indexPath:self.indexPath];
    }
}

- (void)textFieldDidEndEditing:(UITextField *)textField {
    
}

- (IBAction)sliderValueChange:(UISlider *)slider {
    if ([self.cellDelegate respondsToSelector:@selector(settingViewCell:indexPath:slierValueChanged:)]) {
        [self.cellDelegate settingViewCell:self indexPath:self.indexPath slierValueChanged:slider.value];
    }
}


@end
