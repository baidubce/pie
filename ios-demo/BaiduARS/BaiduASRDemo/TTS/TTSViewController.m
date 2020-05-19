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

@interface TTSViewController ()

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
//    [[BDTTSInstance sharedInstance] setAccessKey:@"c4e9d74d-2d19-4e82-6bbe-c3652473633d"
//                                      secrityKey:@"3264d372-466f-4a46-758b-048950a02315"];
}

- (IBAction)startTTS:(id)sender {
    NSString *text = self.textView.text;
    if (text && text.length) {
        [self requestStart];
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

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
