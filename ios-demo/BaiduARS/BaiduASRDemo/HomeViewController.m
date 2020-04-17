//
//  HomeViewController.m
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng01 on 2020/4/15.
//  Copyright © 2020 Cloud. All rights reserved.
//

#import "HomeViewController.h"

@interface HomeViewController ()

@end

@implementation HomeViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [self loadSubViews];    
}

- (void)loadSubViews {
    NSInteger countPL = 2;
    CGFloat squareGap = 20;
    CGFloat squareWidth = ([UIScreen mainScreen].bounds.size.width - squareGap * (countPL + 1)) * (1 / (CGFloat)countPL);
    CGFloat squareHeight = squareWidth;
    
    CGFloat x = squareGap;
    CGFloat y = squareGap + 88;

    CGRect rect = CGRectMake(x, y, squareWidth, squareHeight);
    UIButton *buttonASR = [self actionbuttonWithFrame:rect title:@"ASR"];
    [self.view addSubview:buttonASR];
    
    rect = CGRectMake(x * 2 + squareWidth, y, squareWidth, squareHeight);
    UIButton *buttonTTS = [self actionbuttonWithFrame:rect title:@"TTS"];
    [self.view addSubview:buttonTTS];
    
}

- (void)pushVC:(UIButton *)button {
    if ([button.titleLabel.text isEqualToString:@"ASR"]) {
        [self performSegueWithIdentifier:@"pushASR" sender:nil];
    }else if ([button.titleLabel.text isEqualToString:@"TTS"]) {
        [self performSegueWithIdentifier:@"pushTTS" sender:nil];
    }
}

- (UIButton *)actionbuttonWithFrame:(CGRect)frame title:(NSString *)title{
    UIButton *actionBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    actionBtn.frame = frame;
    actionBtn.titleLabel.textColor = [UIColor whiteColor];
    actionBtn.backgroundColor = [UIColor systemBlueColor];
    [actionBtn setTitle:title forState:UIControlStateNormal];
    [actionBtn addTarget:self action:@selector(pushVC:) forControlEvents:UIControlEventTouchUpInside];
    return actionBtn;
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
