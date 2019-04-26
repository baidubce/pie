//
//  ViewController.m
//  BaiduARS
//
//  Created by Wu,Yunpeng(AI2B) on 2019/4/12.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import "ViewController.h"

#import <GRPCClient/GRPCCall+Tests.h>
#import <ProtoRPC/ProtoService.h>
#import <RxLibrary/GRXWriter+Immediate.h>
#import <RxLibrary/GRXWriter+Transformations.h>
#import <RxLibrary/GRXBufferedPipe.h>

#import "AudioStreaming.pbrpc.h"
#import "AudioStreaming.pbobjc.h"
#import "BDASRConfig.h"

#import <Novocaine/Novocaine.h>

#import "BDAudioManager.h"
#import "SettingViewController.h"

//static NSString * const kHostAddress = @"180.76.107.131:8212"; //180.76.107.131:8212
//static NSString * const kPackageName = @"com.baidu.acu.pie";

UIColor *bgColor = [UIColor colorWithRed:0.0 green:122/255.0 blue:1.0 alpha:1.0];

@interface ViewController ()

@property (nonatomic, weak) IBOutlet UITextView *resultTextView;
@property (nonatomic, weak) IBOutlet UIButton *recordBtn;
@property (nonatomic, weak) IBOutlet UIView *recordView;
@property (nonatomic, assign) BOOL isRecording;

@property (nonatomic, strong) AsrService *client;
@property (nonatomic, strong) GRPCProtoCall *call;
@property (nonatomic, strong) BDAudioManager *recorder;
@property (nonatomic, assign) BOOL isProssing;

@property (nonatomic, strong) BDASRConfig *asrConfig;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.

    self.asrConfig = [BDASRConfig config];
    self.recorder = [BDAudioManager recorder];
    self.isRecording = NO;
    self.isProssing = NO;

    if ([self.recorder checkPermission]) {
        // 检查麦克风权限
    };
    
    [GRPCCall useInsecureConnectionsForHost:[self.asrConfig hostAddress_Port]];
    self.client = [[AsrService alloc] initWithHost:[self.asrConfig hostAddress_Port]];
}
- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    [GRPCCall useInsecureConnectionsForHost:[self.asrConfig hostAddress_Port]];
    self.client = [[AsrService alloc] initWithHost:[self.asrConfig hostAddress_Port]];
}

- (void)setUpConfig {
    NSString *userName = @"user1";
    NSString *passWord = @"password1";
    NSString *expireTime = [self.asrConfig getUTCTimeString];
    NSString *token = [self.asrConfig getToken:[NSString stringWithFormat:@"%@%@%@", userName, passWord, expireTime]];
    
    InitRequest *request = [[InitRequest alloc] init];
    request.enableLongSpeech = YES;
    request.enableChunk = YES;
    request.enableFlushData = YES;
    request.samplePointBytes = self.asrConfig.bitDepth;
    request.sendPerSeconds = self.asrConfig.sendPerSeconds;
    request.enableFlushData = self.asrConfig.enableFlushData;
    request.productId = [NSString stringWithFormat:@"%ld", self.asrConfig.product];
    request.sleepRatio = self.asrConfig.sleepRatio;
    request.appName = self.asrConfig.appName;
    request.logLevel = self.asrConfig.logLevel;
    request.userName = userName;
    request.expireTime = [self.asrConfig getUTCTimeString];
    request.token = token;
    
    NSLog(@"audio_meta : %@", request);
    
    NSString *filePath = [self.recorder currentRecorderFilePath];
//    NSString *filePath = [[NSBundle mainBundle] pathForResource:@"xeq16k" ofType:@"wav"];
    NSData *data = [[NSData alloc] initWithContentsOfFile:filePath];
    
    NSArray *audioScream = [self getAudioScreamWithData:data];

    GRXWriter *writer = [GRXWriter writerWithContainer:audioScream];

    __block NSMutableString *result = [NSMutableString stringWithString:self.resultTextView.text ?: @""];
    __weak typeof(self) weakSelf = self;
    GRPCProtoCall *call = [self.client RPCTosendWithRequestsWriter:writer eventHandler:^(BOOL done,
                                                                                         AudioFragmentResponse * _Nullable response,
                                                                                         NSError * _Nullable error) {
        if (error != nil) {
            [result appendString:@" error: \n"];
            [result appendFormat:@"%@", error];
            
            weakSelf.resultTextView.text = result;
        }else if (response != nil) {
            NSLog(@"识别结果:%@", response);
            
            NSMutableString *showText = result;
            if (response.errorCode != 0 && response.errorMessage != nil) {
                [result appendFormat:@"(%@)", response.errorMessage];
                showText = result;
            }else {
                AudioFragmentResult *fragmentResult = response.audioFragment;
                if (fragmentResult.completed == YES) {
                    [result appendFormat:@"%@", fragmentResult.result];
                    showText = result;
                }else {
                    showText = [NSMutableString stringWithFormat:@"%@%@", result, fragmentResult.result];
                }
            }

            weakSelf.resultTextView.text = showText;
        }else {
//            NSLog(@"？？？？？？？？");
//            [result appendFormat:@"%@", @"(?????)"];
//            weakSelf.resultTextView.text = result;
        }
        
        if (done) {
            weakSelf.recordBtn.userInteractionEnabled = YES;
            [self.recordBtn setTitle:@"录音" forState:UIControlStateNormal];
        }
    }];

    NSData *metaData = [request data];
    NSString *metaString = [metaData base64EncodedStringWithOptions:0];

    call.requestHeaders[@"audio_meta"] = metaString;

    if (call.state == GRXWriterStateNotStarted) {
        [call start];
    }
}

- (NSArray *)getAudioScreamWithData:(NSData *)data {
    int length = (int)data.length;
    int current_l = 0;
    int step = 640;
    
    NSMutableArray *audioScream = [NSMutableArray arrayWithCapacity:10];
    if (length >= step) {
        while (current_l < length - step) {
            AudioFragmentRequest *dataRequest = [[AudioFragmentRequest alloc] init];
            dataRequest.audioData = [data subdataWithRange:NSMakeRange(current_l, step)];
            [audioScream addObject:dataRequest];
            
            current_l += step;
        };
        AudioFragmentRequest *dataRequest = [[AudioFragmentRequest alloc] init];
        dataRequest.audioData = [data subdataWithRange:NSMakeRange(current_l, length - current_l)];
        [audioScream addObject:dataRequest];
    }else {
        AudioFragmentRequest *dataRequest = [[AudioFragmentRequest alloc] init];
        dataRequest.audioData = [data subdataWithRange:NSMakeRange(current_l, length)];
        [audioScream addObject:dataRequest];
    }
    return audioScream;
}

- (IBAction)record:(id)sender {
    if (!self.isRecording) {
        self.isRecording = YES;
        [self startRecording];
    }else {
        self.isRecording = NO;
        [self stopRecording];
        self.recordBtn.userInteractionEnabled = NO;
        [self.recordBtn setTitle:@"识别中" forState:UIControlStateNormal];
        // 语音解析
        [self setUpConfig];
    }
}

- (void)startRecording {
    [self.recorder resetRecorder];
    [self.recorder startRecorder];

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
    [self.recorder stopRecorder];
    [self.recordBtn setTitle:@"录音" forState:UIControlStateNormal];
    [self.recordView.layer removeAllAnimations];
}

- (IBAction)showSetting:(id)sender {
    SettingViewController *setting = [[SettingViewController alloc] init];
    [self presentViewController:setting animated:YES completion:nil];
}

@end
