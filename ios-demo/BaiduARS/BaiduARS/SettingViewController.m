//
//  SettingViewController.m
//  BaiduARS
//
//  Created by Wu,Yunpeng(AI2B) on 2019/4/25.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import "SettingViewController.h"

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

    BDASRConfig *config = [BDASRConfig config];
    self.addressTF.text = config.hostAddress;
    self.portTF.text = [NSString stringWithFormat:@"%d", config.serverPort];
    
    NSInteger product = config.product;
    NSString *proS = [NSString stringWithFormat:@"%ld", (long)product];
    NSInteger index = [config.productIDArray indexOfObject:proS];
    NSString *proDes = [self productDesWithIndex:index];
    [self.modelBtn setTitle:proDes forState:UIControlStateNormal];
    
    NSString *sampleRate = [NSString stringWithFormat:@"%d", config.sampleRate];
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
    BDASRConfig *config = [BDASRConfig config];

    if (index >= 0 && index <= config.productIDDataSource.count) {
        proDes = [config.productIDDataSource objectAtIndex:index];
    }
    return proDes;
}

- (IBAction)pickModel:(id)sender {
    BDASRConfig *config = [BDASRConfig config];
    NSInteger product = config.product;
    NSString *proS = [NSString stringWithFormat:@"%ld", (long)product];
    NSInteger index = [config.productIDArray indexOfObject:proS];
    
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
    
    BDASRConfig *config = [BDASRConfig config];
    NSString *address = self.addressTF.text;
    int port = [self.portTF.text intValue];
    int sampleRate = [self.sampleRateTF.text intValue];
    
    NSString *title = [config.productIDArray objectAtIndex:[self.modelPicker selectedRowInComponent:0]];
    ASRProduct product = [title integerValue];
    
    config.hostAddress = address;
    config.serverPort = port;
    config.product = product;
    config.sampleRate = sampleRate;
    
    [self performSelector:@selector(back:) withObject:nil afterDelay:.5f];
}
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    BDASRConfig *config = [BDASRConfig config];
    return config.productIDArray.count;
}

- (CGFloat)pickerView:(UIPickerView *)pickerView rowHeightForComponent:(NSInteger)component {
    return 30.f;
}

- (CGFloat)pickerView:(UIPickerView *)pickerView widthForComponent:(NSInteger)component {
    return self.view.frame.size.width;
}

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
    BDASRConfig *config = [BDASRConfig config];
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
