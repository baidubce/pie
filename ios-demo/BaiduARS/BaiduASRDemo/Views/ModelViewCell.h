//
//  ModelViewCell.h
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng01 on 2019/9/2.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class ModelViewCell;
@protocol ModelViewCellDelegate <NSObject>

@optional
- (void)modelViewCellBeginEdit:(ModelViewCell *_Nullable)cell indexPath:(NSIndexPath *_Nullable)indexPath;

@end

@interface ModelViewCell : UITableViewCell<UITextFieldDelegate>

@property (nonatomic, weak) IBOutlet UITextField *titleField;
@property (nonatomic, weak) IBOutlet UITextField *valueField;
@property (nonatomic, strong) NSIndexPath *indexPath;
@property (nonatomic, weak) id<ModelViewCellDelegate>cellDelegate;

+ (instancetype)cellWithNib;

@end

NS_ASSUME_NONNULL_END
