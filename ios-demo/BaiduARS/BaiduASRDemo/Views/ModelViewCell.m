//
//  ModelViewCell.m
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng01 on 2019/9/2.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import "ModelViewCell.h"

@implementation ModelViewCell

- (void)awakeFromNib {
    [super awakeFromNib];
    
    self.titleField.delegate = self;
    self.valueField.delegate = self;
}

+ (instancetype)cellWithNib {
    NSArray *arr = [[NSBundle mainBundle] loadNibNamed:@"ModelViewCell" owner:nil options:nil];
    if (arr.count > 0) {
        return [arr objectAtIndex:0];
    }
    return [[ModelViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"ModelViewCell"];
}

- (void)textFieldDidBeginEditing:(UITextField *)textField {
    if ([self.cellDelegate respondsToSelector:@selector(modelViewCellBeginEdit:indexPath:)]) {
        [self.cellDelegate modelViewCellBeginEdit:self indexPath:self.indexPath];
    }
}

- (void)textFieldDidEndEditing:(UITextField *)textField {
    
}

@end
