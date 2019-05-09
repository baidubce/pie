//
//  SettingViewController.m
//  BaiduARS
//
//  Created by Wu,Yunpeng(AI2B) on 2019/4/25.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import "SettingViewController.h"
#import "ASRConfig.h"

@interface SettingViewController ()<UIPickerViewDelegate, UIPickerViewDataSource>

@property (nonatomic, weak) IBOutlet UITextField *addressTF;
@property (nonatomic, weak) IBOutlet UITextField *portTF;
@property (nonatomic, weak) IBOutlet UIButton *modelBtn;
@property (nonatomic, weak) IBOutlet UITextField *sampleRateTF;

@property (nonatomic, weak) IBOutlet UIPickerView *modelPicker;

@end

@implementation SettingViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.

    ASRConfig *config = [ASRConfig config];
    self.addressTF.text = config.hostAddress;
    self.portTF.text = config.serverPort;
    
    NSString *productId = config.productId;
    NSInteger index = [config.productIDArray indexOfObject:productId];
    NSString *proDes = [self productDesWithIndex:index];
    [self.modelBtn setTitle:proDes forState:UIControlStateNormal];
    
    NSString *sampleRate = config.sampleRate;
    self.sampleRateTF.text = sampleRate;
    
    UITapGestureRecognizer *cancel = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(cancelEdit)];
    [self.view addGestureRecognizer:cancel];
}

- (void)cancelEdit {
    [self.addressTF resignFirstResponder];
    [self.portTF resignFirstResponder];
    self.modelPicker.hidden = YES;
}

- (NSString *)productDesWithIndex:(NSInteger)index {
    NSString *proDes = @"";
    ASRConfig *config = [ASRConfig config];

    if (index >= 0 && index <= config.productIDDataSource.count) {
        proDes = [config.productIDDataSource objectAtIndex:index];
    }
    return proDes;
}

- (IBAction)pickModel:(id)sender {
    ASRConfig *config = [ASRConfig config];
    NSString *product = config.productId;
    NSInteger index = [config.productIDArray indexOfObject:product];
    
    if (index >= 0 && index <= config.productIDDataSource.count) {
        [self.modelPicker selectRow:index inComponent:0 animated:NO];
    }
    
    self.modelPicker.hidden = NO;
}

- (IBAction)back:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)conform:(id)sender {
    [self cancelEdit];
    
    ASRConfig *config = [ASRConfig config];

    NSString *address = self.addressTF.text;
    NSString *port = self.portTF.text;
    NSString *productID = [config.productIDArray objectAtIndex:[self.modelPicker selectedRowInComponent:0]];
    NSString *sampleRate = self.sampleRateTF.text;
    
    config.hostAddress = address;
    config.serverPort = port;
    config.productId = productID;
    config.sampleRate = sampleRate;
    
    [config save];
    
    [self performSelector:@selector(back:) withObject:nil afterDelay:.5f];
}

- (IBAction)reset:(id)sender {
    [[ASRConfig config] reset];
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:nil message:@"数据已恢复为原始设置，重启App生效！" preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleCancel handler:nil]];
    [self presentViewController:alert animated:YES completion:nil];
}


- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    ASRConfig *config = [ASRConfig config];
    return config.productIDArray.count;
}

- (CGFloat)pickerView:(UIPickerView *)pickerView rowHeightForComponent:(NSInteger)component {
    return 30.f;
}

- (CGFloat)pickerView:(UIPickerView *)pickerView widthForComponent:(NSInteger)component {
    return self.view.frame.size.width;
}

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
    ASRConfig *config = [ASRConfig config];
    NSString *title = [config.productIDDataSource objectAtIndex:row];
    return title;
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component {
    NSString *proDes = [self productDesWithIndex:row];
    [self.modelBtn setTitle:proDes forState:UIControlStateNormal];
}


/*
 #pragma mark - Navigation
 
 // In a storyboard-based application, you will often want to do a little preparation before navigation
 - (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
 // Get the new view controller using [segue destinationViewController].
 // Pass the selected object to the new view controller.
 }
 */
@end
