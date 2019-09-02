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
@end
