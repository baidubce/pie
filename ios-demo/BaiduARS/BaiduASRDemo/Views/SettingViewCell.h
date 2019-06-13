//
//  SettingViewCell.h
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng(AI2B) on 2019/5/22.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import <UIKit/UIKit.h>

@class SettingViewCell;
@protocol SettingViewCellDelegate <NSObject>

@optional
- (void)settingViewCellBeginEdit:(SettingViewCell *_Nullable)cell indexPath:(NSIndexPath *_Nullable)indexPath;

@end

NS_ASSUME_NONNULL_BEGIN

@interface SettingViewCell : UITableViewCell<UITextFieldDelegate>

@property (nonatomic, weak) IBOutlet UILabel *title;
@property (nonatomic, weak) IBOutlet UITextField *textField;
@property (nonatomic, strong) NSIndexPath *indexPath;
@property (nonatomic, weak) id<SettingViewCellDelegate>cellDelegate;

+ (instancetype)cellWithNib;

@end

NS_ASSUME_NONNULL_END
