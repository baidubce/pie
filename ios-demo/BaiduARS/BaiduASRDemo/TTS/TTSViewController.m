//
//  TTSViewController.m
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng01 on 2020/4/15.
//  Copyright © 2020 Cloud. All rights reserved.
//

#import "TTSViewController.h"
#import <BaiduTTS/BDTTSInstance.h>
#import "TTSConfig.h"

@interface TTSViewController ()<BDTTSInstanceDelegate>

@property (nonatomic, weak) IBOutlet UITextView *textView;
@property (nonatomic, weak) IBOutlet UIButton *ttsBtn;
@property (nonatomic, weak) IBOutlet UIActivityIndicatorView *loadingView;
@property (nonatomic, assign) BOOL isLoading;

@end

@implementation TTSViewController

- (void)dealloc {
//    [[BDTTSInstance sharedInstance] cancel];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [self requestStop];
    [self loadTTSConfig];
}

- (void)loadTTSConfig {
    TTSConfig *config = [TTSConfig config];
    
    [[BDTTSInstance sharedInstance] setPer:config.per];
    [[BDTTSInstance sharedInstance] setPit:config.pit];
    [[BDTTSInstance sharedInstance] setSpd:config.spd];
    [[BDTTSInstance sharedInstance] setVol:config.vol];
}

- (IBAction)startTTS:(id)sender {
    NSString *text = self.textView.text;
    if (text && text.length) {
        [self requestStart];
        [[BDTTSInstance sharedInstance] setDelegate:self];
        [[BDTTSInstance sharedInstance] ttsWithText:text isPlay:YES complete:^(NSURL * _Nonnull filePath) {
            [self requestStop];
        } failure:^(NSError * _Nonnull error) {
            [self requestStop];
        }];
    }
}

- (void)requestStart {
    self.ttsBtn.hidden = YES;
    self.loadingView.hidden = NO;
    [self.loadingView startAnimating];
}

- (void)requestStop {
    self.ttsBtn.hidden = NO;
    self.loadingView.hidden = YES;
    [self.loadingView stopAnimating];
}

- (void)stopPlay {
    [[BDTTSInstance sharedInstance] stopPlay];
}

#pragma mark BDTTSInstanceDelegate
- (void)onBDTTSPlayerStop {
    NSLog(@"onBDTTSPlayerStop");
}

- (void)onBDTTSPlayerStart {
    NSLog(@"onBDTTSPlayerStart");
}

- (void)onBDTTSPlayerFinish {
    NSLog(@"onBDTTSPlayerFinish");
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
