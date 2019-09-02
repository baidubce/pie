//
//  EditModelViewController.m
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng01 on 2019/9/2.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import "EditModelViewController.h"
#import "ModelViewCell.h"
#import "ModelCellModel.h"
#import "ASRConfig.h"

@interface EditModelViewController ()<UITableViewDelegate, UITableViewDataSource, ModelViewCellDelegate>

@property (nonatomic, weak) IBOutlet UITableView *tableView;
@property (nonatomic, weak) IBOutlet NSLayoutConstraint *tableViewBottom;

@property (nonatomic, strong) NSMutableArray *models;
@property (nonatomic, strong) ModelViewCell *editingCell;

@end

@implementation EditModelViewController

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self initData];
    
    UIView *footer = [[UIView alloc] init];
    self.tableView.tableFooterView = footer;
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
}

- (void)initData {
    self.models = [[NSMutableArray alloc] init];

    ASRConfig *config = [ASRConfig config];
    
    NSArray *idArray = config.productIDArray;
    NSArray *valueArray = config.productIDDataSource;
    
    for (NSInteger i = 0; i < idArray.count; i++) {
        ModelCellModel *model = [[ModelCellModel alloc] init];
        model.titleString = [idArray objectAtIndex:i];
        model.valueString = valueArray.count > i ? [valueArray objectAtIndex:i] : @"";
        [self.models addObject:model];
    }
}

- (IBAction)back:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)conform:(id)sender {
    [self.editingCell.titleField resignFirstResponder];
    [self.editingCell.valueField resignFirstResponder];
    
    NSMutableArray *ids = [NSMutableArray array];
    NSMutableArray *deses = [NSMutableArray array];
    for (NSInteger i = 0; i < self.models.count; i++) {
        ModelViewCell *cell = nil;
        
        cell = [self.tableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:i inSection:0]];
        NSString *idString = cell.titleField.text;
        NSString *dessString = cell.valueField.text;
        
        if (idString.length > 0 && dessString > 0) {
            [ids addObject:idString];
            [deses addObject:dessString];
        }else {
            UIAlertController *alert = [UIAlertController alertControllerWithTitle:nil message:@"请填写完整的模型ID与描述" preferredStyle:UIAlertControllerStyleAlert];
            [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleCancel handler:nil]];
            [self presentViewController:alert animated:YES completion:nil];
            return;
        }
    }
    
    ASRConfig *config = [ASRConfig config];
    config.productIDArray = ids;
    config.productIDDataSource = deses;
    [config save];
}

#pragma mark- notification
- (void)keyboardWillShow:(NSNotification *)notification {
    NSDictionary *userInfo = [notification userInfo];
    NSValue *value = [userInfo objectForKey:UIKeyboardFrameEndUserInfoKey];
    
    float height = [value CGRectValue].size.height;
    
    self.tableViewBottom.constant = height;
}

- (void)keyboardWillHide:(NSNotification *)notification {
    self.tableViewBottom.constant = 0.0;
}

#pragma mark- SettingViewCellDelegate
- (void)modelViewCellBeginEdit:(ModelViewCell *)cell indexPath:(NSIndexPath *)indexPath {
    self.editingCell = cell;
}

#pragma mark- UITableViewDelegate UITableViewDataSource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.models.count;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    ModelCellModel *model = [self.models objectAtIndex:indexPath.row];
    return model.cellHeight;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ModelViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ModelViewCell"];
    if (cell == nil) {
        cell = [ModelViewCell cellWithNib];
    }
    ModelCellModel *model = [self.models objectAtIndex:indexPath.row];

    cell.titleField.text = model.titleString;
    cell.valueField.text = model.valueString;
    cell.indexPath = indexPath;
    cell.cellDelegate = self;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    NSLog(@"index - %ld", indexPath.row);
}

@end
