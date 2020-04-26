//
//  TTSSetingViewController.m
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng01 on 2020/4/16.
//  Copyright © 2020 Cloud. All rights reserved.
//

#import "TTSSetingViewController.h"
#import "TTSConfig.h"
#import "SettingViewCell.h"
#import "SettingViewCellModel.h"

@interface TTSSetingViewController () <UITableViewDelegate, UITableViewDataSource, SettingViewCellDelegate>

@property (nonatomic, weak) IBOutlet UITableView *settingTableView;
@property (nonatomic, strong) SettingViewCell *editingCell;

@property (nonatomic, strong) NSMutableArray *settings;

@end

@implementation TTSSetingViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [self initData];
    
    UIView *footer = [[UIView alloc] init];
    self.settingTableView.tableFooterView = footer;
}

- (void)initData {
    TTSConfig *config = [TTSConfig config];
    
    NSMutableArray *settingsArray = [NSMutableArray array];
    
    SettingViewCellModel *settingModel = nil;
    settingModel = [SettingViewCellModel new];
    settingModel.title = @"说话人ID:";
    settingModel.descrptionString = config.per;
    settingModel.valueString = config.per;
    settingModel.isTextFieldEditEnable = YES;
    [settingsArray addObject:settingModel];
    
    settingModel = [SettingViewCellModel new];
    settingModel.title = @"spd(语速,默认为5中语速):";
    settingModel.descrptionString = config.spd;
    settingModel.valueString = config.spd;
    settingModel.isShowSlider = YES;
    settingModel.isTextFieldEditEnable = NO;
    settingModel.sliderMin = 0;
    settingModel.sliderMax = 15;
    [settingsArray addObject:settingModel];
    
    settingModel = [SettingViewCellModel new];
    settingModel.title = @"pit(音调,默认为5中语调):";
    settingModel.descrptionString = config.pit;
    settingModel.valueString = config.pit;
    settingModel.isShowSlider = YES;
    settingModel.isTextFieldEditEnable = NO;
    settingModel.sliderMin = 0;
    settingModel.sliderMax = 15;
    [settingsArray addObject:settingModel];
    
    settingModel = [SettingViewCellModel new];
    settingModel.title = @"vol(音量,默认为5中音量):";
    settingModel.descrptionString = config.vol;
    settingModel.valueString = config.vol;
    settingModel.isShowSlider = YES;
    settingModel.isTextFieldEditEnable = NO;
    settingModel.sliderMin = 0;
    settingModel.sliderMax = 15;
    [settingsArray addObject:settingModel];
    
    self.settings = settingsArray;
}

- (IBAction)conform:(id)sender {
    if (self.editingCell != nil && self.editingCell.textField.isFirstResponder) {
        [self.editingCell.textField resignFirstResponder];
    }
    
    TTSConfig *config = [TTSConfig config];
    
    SettingViewCell *cell = nil;

    cell = [self.settingTableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:0 inSection:0]];
    NSString *per = cell.textField.text;

    cell = [self.settingTableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:1 inSection:0]];
    NSString *spd = cell.textField.text;
    
    cell = [self.settingTableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:2 inSection:0]];
    NSString *pit = cell.textField.text;
    
    cell = [self.settingTableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:3 inSection:0]];
    NSString *vol = cell.textField.text;
    
    config.per = per;
    config.spd = spd;
    config.pit = pit;
    config.vol = vol;

    [config save];
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.navigationController popViewControllerAnimated:YES];
    });
}

#pragma mark- SettingViewCellDelegate
- (void)settingViewCellBeginEdit:(SettingViewCell *)cell indexPath:(NSIndexPath *)indexPath {
    self.editingCell = cell;
}

- (void)settingViewCell:(SettingViewCell *)cell indexPath:(NSIndexPath *)indexPath slierValueChanged:(CGFloat)value {
    self.editingCell = cell;
    
    NSString *newSliderValue = [NSString stringWithFormat:@"%ld", (NSInteger)value];
    SettingViewCellModel *model = [self.settings objectAtIndex:indexPath.row];
    if (![model.valueString isEqualToString:newSliderValue]) {
        model.valueString = newSliderValue;
        cell.textField.text = newSliderValue;
    }
}

#pragma mark- UITableViewDelegate UITableViewDataSource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.settings.count;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    SettingViewCellModel *model = [self.settings objectAtIndex:indexPath.row];
    return model.cellHeight;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    SettingViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"SettingViewCell"];
    if (cell == nil) {
        cell = [SettingViewCell cellWithNib];
        cell.cellDelegate = self;
        cell.indexPath = indexPath;
    }

    SettingViewCellModel *model = [self.settings objectAtIndex:indexPath.row];
    cell.cellModel = model;

    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    NSLog(@"index - %ld", indexPath.row);
}

@end
