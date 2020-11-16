//
//  BDAudioManager.m
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng(AI2B) on 2019/4/22.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import "BDAudioManager.h"
#import <AVFoundation/AVFoundation.h>
#import "BDASRConfig.h"
static BDAudioManager *_recorder = nil;

@interface BDAudioManager() <AVAudioRecorderDelegate>

@property (nonatomic, strong) AVAudioRecorder *recorder;

@end

@implementation BDAudioManager

+ (instancetype)recorder {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        if (_recorder == nil) {
            _recorder = [[BDAudioManager alloc] init];
        }
    });
    
//    if ([AVAudioSession sharedInstance].category != AVAudioSessionCategoryPlayAndRecord) {
//        [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayAndRecord error: nil];
//    }
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayAndRecord withOptions:AVAudioSessionCategoryOptionDefaultToSpeaker error:nil];//PlayAndRecord
    [[AVAudioSession sharedInstance] setActive:YES error:nil];

    return _recorder;
}

- (instancetype)init {
    self = [super init];
    if (self != nil) {
        [self configRecorder];
    }
    return self;
}

#pragma mark - 权限判断
- (BOOL)checkPermission {
    AVAudioSessionRecordPermission permission = [[AVAudioSession sharedInstance] recordPermission];
    return permission == AVAudioSessionRecordPermissionGranted;
}

#pragma mark - 配置录音
- (BOOL)configRecorder {
    NSURL *fileUrl = [NSURL fileURLWithPath:[self filePathWithName:[self newRecorderName]]];
    NSDictionary *setting = [self recordSetting];
    NSError *error = nil;
    self.recorder = [[AVAudioRecorder alloc] initWithURL:fileUrl settings:setting error:&error];
    if (error) {
        NSLog(@"录音器初始化失败！！！");
        return NO;
    }; // 录音文件创建失败处理
    self.recorder.delegate = self;
    self.recorder.meteringEnabled = YES;
     [self.recorder prepareToRecord];
    // ...其他设置
    return YES;
}

// 录音参数设置
- (NSDictionary *)recordSetting {
    NSMutableDictionary *recSetting = [[NSMutableDictionary alloc] init];
    // General Audio Format Settings
    recSetting[AVFormatIDKey] = @(kAudioFormatLinearPCM);       // 编码格式
    recSetting[AVSampleRateKey] = @([BDASRConfig config].sampleRate);                     // 采样率
    recSetting[AVNumberOfChannelsKey] = @(1);                   // 通道数
    // Linear PCM Format Settings
    recSetting[AVLinearPCMBitDepthKey] = @(16);
    recSetting[AVLinearPCMIsBigEndianKey] = @(NO);
    recSetting[AVLinearPCMIsFloatKey] = @(NO);
    // Encoder Settings
    recSetting[AVEncoderAudioQualityKey] = @(AVAudioQualityMedium); // 录音质量
//    recSetting[AVEncoderBitRateKey] = @(128000);
    return [recSetting copy];
}

// 录音文件的名称使用时间戳+caf后缀
- (NSString *)newRecorderName {
//    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
//    formatter.dateFormat = @"yyyyMMddhhmmss";
//    return [[formatter stringFromDate:[NSDate date]] stringByAppendingPathExtension:@"wav"];
    return @"audio.wav";
}
// Document目录
- (NSString *)filePathWithName:(NSString *)fileName {
    NSString *urlStr = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
    return [urlStr stringByAppendingPathComponent:fileName];
}

- (void)handleRecorderData {
    [self.recorder updateMeters];
    float peak0 = ([self.recorder peakPowerForChannel:0] + 160.0) * (1.0 / 160.0);
    float peak1 = ([self.recorder peakPowerForChannel:1] + 160.0) * (1.0 / 160.0);
    float ave0 = ([self.recorder averagePowerForChannel:0] + 160.0) * (1.0 / 160.0);
    float ave1 = ([self.recorder averagePowerForChannel:1] + 160.0) * (1.0 / 160.0);
    
    NSLog(@"peak0:%f, peak1:%f, ave0:%f, ave1:%f", peak0, peak1, ave0, ave1);
}
- (void)resetRecorder {
    if (!self.recorder.isRecording) {
        [self.recorder deleteRecording];
        [self configRecorder];
    }
}

- (void)startRecorder {
//    BOOL isOK = [self configRecorder];
//    if (isOK)
        [self.recorder record];
}

- (void)pauseRecorder {
    if (self.recorder.isRecording) [self.recorder pause];
}

- (void)stopRecorder {
    if (self.recorder.isRecording) {
        [self.recorder stop];
    }
}

- (NSString *)currentRecorderFilePath {
    return self.recorder.url.path;
}

#pragma mark - AVAudioRecorderDelegate

- (void)audioRecorderDidFinishRecording:(AVAudioRecorder *)recorder successfully:(BOOL)flag {
    if (flag) {
        NSLog(@"录音正常结束");
        // 录音正常结束
    } else {
        // 未正常结束
        if ([_recorder deleteRecording]) {
            NSLog(@"录音文件删除成功");
            // 录音文件删除成功
        } else {
            NSLog(@"录音文件删除失败");
            // 录音文件删除失败
        }
    }
}

@end
