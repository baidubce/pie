//
//  ViewController.m
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng(AI2B) on 2019/4/12.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import "ASRViewController.h"
#import "SettingViewController.h"
#import "ASRConfig.h"

#import <BaiduASR/BDASRInstance.h>

UIColor *bgColor = [UIColor colorWithRed:0.0 green:122/255.0 blue:1.0 alpha:1.0];

@interface ASRViewController ()<BDASRDelegate>

@property (nonatomic, weak) IBOutlet UITextView *resultTextView;
@property (nonatomic, weak) IBOutlet UIButton *recordBtn;
@property (nonatomic, weak) IBOutlet UIView *recordView;
@property (nonatomic, assign) BOOL isRecording;

@property (nonatomic, strong) BDASRInstance *asrInstance;

@property (nonatomic, strong) NSMutableString *resultString;

@end

@implementation ASRViewController
- (void)dealloc {
    [self.asrInstance cancel];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.resultTextView.layoutManager.allowsNonContiguousLayout = NO;
    
    self.resultString = [NSMutableString string];
    self.asrInstance = [BDASRInstance shareInstance];
    
    ASRConfig *config = [ASRConfig config];
    
    [self.asrInstance setUserName:config.userName passWord:config.passWord];
    [self.asrInstance setHostAddress:config.hostAddress];
    [self.asrInstance setServerPort:config.serverPort];
    [self.asrInstance setProductId:config.productId];
    [self.asrInstance setSampleRate:config.sampleRate];
    [self.asrInstance setAccessKey:@"c4e9d74d-2d19-4e82-6bbe-c3652473633d" secrityKey:@"3264d372-466f-4a46-758b-048950a02315"];
    [self.asrInstance setDelegate:self];
}

- (IBAction)record:(id)sender {
    if (![self checkLogin]) {
        UIAlertController *alert = [UIAlertController alertControllerWithTitle:nil message:@"请到设置页填写账户信息" preferredStyle:UIAlertControllerStyleAlert];
        [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleCancel handler:nil]];
        [self presentViewController:alert animated:YES completion:nil];
        return;
    }
    
    if (self.asrInstance.status == NORMAL) {
        [self startRecording];
    }else if (self.asrInstance.status == RECORDING){
        [self stopRecording];
        self.recordBtn.userInteractionEnabled = NO;
        [self.recordBtn setTitle:@"识别中" forState:UIControlStateNormal];
    }
}

- (IBAction)cleanScreen:(id)sender {
    self.resultTextView.text = @"";
    self.resultString = [NSMutableString string];
}


- (IBAction)showSetting:(id)sender {
    SettingViewController *setting = [[SettingViewController alloc] init];
    [self presentViewController:setting animated:YES completion:nil];
}

- (void)startRecording {
    [self.asrInstance startRecord];
    
    [self.recordBtn setTitle:@"停止" forState:UIControlStateNormal];
    
    CABasicAnimation *animation = [CABasicAnimation animationWithKeyPath:@"transform.scale"];
    animation.fromValue = [NSNumber numberWithFloat:1.0];
    animation.toValue = [NSNumber numberWithFloat:1.3];
    
    CABasicAnimation *animation2 = [CABasicAnimation animationWithKeyPath:@"opacity"];
    animation2.fromValue = [NSNumber numberWithFloat:1.0];
    animation2.toValue = [NSNumber numberWithFloat:0.8];
    
    CAAnimationGroup *animations = [CAAnimationGroup animation];
    animations.animations = [NSArray arrayWithObjects:animation, animation2, nil];
    animations.duration = 1.3;
    animations.repeatCount = 100000;
    animations.autoreverses = YES;
    animations.removedOnCompletion = NO;
    
    [self.recordView.layer addAnimation:animations forKey:nil];
}

- (void)stopRecording {
    [self.asrInstance stopRecord];
    [self.recordBtn setTitle:@"录音" forState:UIControlStateNormal];
    [self.recordView.layer removeAllAnimations];
}

- (BOOL)checkLogin {
    ASRConfig *config = [ASRConfig config];
    NSString *userName = config.userName;
    NSString *passWord = config.passWord;

    return (userName.length > 0 && passWord.length > 0);
}

- (NSDictionary *)dicFromJsonString:(NSString *)jsonString {
    if (jsonString == nil) {
        return nil;
    }
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSError *error;
    NSDictionary *result = [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingMutableContainers error:&error];
    if (error != nil) {
        NSLog(@"解析失败：%@", error);
        return nil;
    }
    
    return result;
}

#pragma new sdk

- (void)bdasrStatusDidChanged:(BDASRSTATUS)status {
    NSLog(@"status:%lu", (unsigned long)status);
    if (status == NORMAL) {
        [self.recordBtn setTitle:@"录音" forState:UIControlStateNormal];
        [self.recordView.layer removeAllAnimations];
        self.recordBtn.userInteractionEnabled = YES;
    }
}

- (void)bdasrAnalizeDone:(BOOL)done result:(id)result error:(NSError *)error {
    NSLog(@"result:%@----done:%d", result, done);

    NSString *completed = @"0";
    NSString *newText = @"";
    if (error) {
        NSLog(@"error: %@", error);
        newText = [NSString stringWithFormat:@"error:%@", error];
    }else {
        if ([result isKindOfClass:[NSString class]]) {
            NSDictionary *responseDic = [self dicFromJsonString:result];
            
            NSDictionary *resultDic = [responseDic objectForKey:@"result"];
            NSString *errorMessage = [responseDic objectForKey:@"errorMessage"];
            
            if (errorMessage.length > 0) {
                newText = errorMessage;
            }
            
            if (responseDic.count > 0) {
                NSString *result = [resultDic objectForKey:@"result"];
                completed = [resultDic objectForKey:@"completed"];
                
                newText = result;
                
                if ([completed  isEqual: @"1"]) {
                    NSLog(@"识别完成");
                }
            }
        }
    }
    
    if ([completed isEqualToString:@"1"]) {
        if (newText.length > 0) {
            self.resultTextView.text = [NSString stringWithFormat:@"%@%@", self.resultString, newText];
        }
        self.resultString = [NSMutableString stringWithFormat:@"%@\n", self.resultTextView.text];
    }else {
        self.resultTextView.text = [NSString stringWithFormat:@"%@%@", self.resultString, newText];
    }
    
    [self.resultTextView scrollRangeToVisible:NSMakeRange(self.resultTextView.text.length, 1)]; 
}

@end
