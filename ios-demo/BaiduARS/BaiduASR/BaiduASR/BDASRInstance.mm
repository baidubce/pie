//
//  BDASRInstance.m
//  BaiduASR
//
//  Created by Wu,Yunpeng(AI2B) on 2019/5/7.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import "BDASRInstance.h"

#import <GRPCClient/GRPCCall+Tests.h>
#import <ProtoRPC/ProtoService.h>
#import <RxLibrary/GRXWriter+Immediate.h>
#import <RxLibrary/GRXWriter+Transformations.h>
#import <RxLibrary/GRXBufferedPipe.h>

#import "AudioStreaming.pbrpc.h"
#import "AudioStreaming.pbobjc.h"
#import "BDASRConfig.h"
#import "BDAudioManager.h"

static BDASRInstance *asrInstance = nil;

@interface BDASRInstance()

@property (nonatomic, assign) BDASRSTATUS currentStatus;
@property (nonatomic, strong) AsrService *client;
@property (nonatomic, strong) GRPCProtoCall *call;
@property (nonatomic, strong) BDAudioManager *recorder;
@property (nonatomic, strong) BDASRConfig *asrConfig;
@property (nonatomic, strong) GRPCProtoCall *grpcCall;

@property (nonatomic, assign) BOOL canRepChange;
@property (nonatomic, assign) BOOL canRepResult;

@end

@implementation BDASRInstance

+ (instancetype)shareInstance {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        if (asrInstance == nil) {
            asrInstance = [[BDASRInstance alloc] init];
        }
    });
    return asrInstance;
}

- (void)refreshConfig {
    [self configHost];
}

- (instancetype)init {
    self = [super init];
    if (self != nil) {
        self.currentStatus = NORMAL;
        self.asrConfig = [BDASRConfig config];
        self.recorder = [BDAudioManager recorder];
        [self configHost];
    }
    return self;
}

#pragma private motherd
- (void)configHost {
    NSString *hostAddress = [self.asrConfig hostAddress_Port];
    [GRPCCall useInsecureConnectionsForHost:hostAddress];
    self.client = [[AsrService alloc] initWithHost:hostAddress];
}

- (BDASRSTATUS)status {
    return self.currentStatus;
}

- (void)setDelegate:(id<BDASRDelegate>)delegate {
    _delegate = delegate;

    self.canRepChange = [delegate respondsToSelector:@selector(bdasrStatusDidChanged:)];
    self.canRepResult = [delegate respondsToSelector:@selector(bdasrAnalizeDone:result:error:)];
}

- (void)setUserName:(NSString *)userName passWord:(NSString *)passWord {
    BDASRConfig *config = [BDASRConfig config];
    config.userName = userName;
    config.passWord = passWord;
}

- (void)setHostAddress:(NSString *)hostAddress {
    BDASRConfig *config = [BDASRConfig config];
    config.hostAddress = hostAddress;
    [self configHost];
}

- (void)setServerPort:(NSString *)serverPort {
    BDASRConfig *config = [BDASRConfig config];
    config.serverPort = [serverPort intValue];
    [self configHost];
}

- (void)setProductId:(NSString *)productId {
    BDASRConfig *config = [BDASRConfig config];
    config.productId = productId;
}

- (void)setSampleRate:(NSString *)sampleRate {
    BDASRConfig *config = [BDASRConfig config];
    config.sampleRate = [sampleRate intValue];
}

- (void)setAppName:(NSString *)appName {
    BDASRConfig *config = [BDASRConfig config];
    config.appName = appName;
}

- (void)startRecord {
    self.currentStatus = RECORDING;
    if (self.canRepChange) [self.delegate bdasrStatusDidChanged:RECORDING];
    [self.recorder resetRecorder];
    [self.recorder startRecorder];
}

- (void)stopRecord {
    self.currentStatus = ANALIZING;
    if (self.canRepChange) [self.delegate bdasrStatusDidChanged:ANALIZING];
    [self.recorder stopRecorder];
    [self startAnalize];
}

- (void)cancel {
    self.currentStatus = NORMAL;
    [self.grpcCall cancel];
    if (self.canRepChange) [self.delegate bdasrStatusDidChanged:NORMAL];
    if (self.canRepResult) [self.delegate bdasrAnalizeDone:NO result:@"REQUEST CANCELED" error:nil];
}

- (void)startAnalize {
    NSString *userName = self.asrConfig.userName;
    NSString *passWord = self.asrConfig.passWord;
    
    if (!(userName.length > 0 && passWord.length > 0)) {
        NSLog(@"invalid username or password, please check them!");
        return;
    }
    
    NSString *expireTime = [self.asrConfig getUTCTimeString];
    NSString *token = [self.asrConfig getToken:[NSString stringWithFormat:@"%@%@%@", userName, passWord, expireTime]];

    InitRequest *request = [[InitRequest alloc] init];
    request.enableLongSpeech = YES;
    request.enableChunk = YES;
    request.enableFlushData = YES;
    request.samplePointBytes = self.asrConfig.bitDepth;
    request.sendPerSeconds = self.asrConfig.sendPerSeconds;
    request.enableFlushData = self.asrConfig.enableFlushData;
    request.productId = self.asrConfig.productId;
    request.sleepRatio = self.asrConfig.sleepRatio;
    request.appName = self.asrConfig.appName;
    request.logLevel = self.asrConfig.logLevel;
    request.userName = userName;
    request.expireTime = expireTime;
    request.token = token;
    
    NSLog(@"audio_meta : %@", request);
    
    NSString *filePath = [self.recorder currentRecorderFilePath];
    NSData *data = [[NSData alloc] initWithContentsOfFile:filePath];
    
    NSArray *audioScream = [self getAudioScreamWithData:data];
    
    GRXWriter *writer = [GRXWriter writerWithContainer:audioScream];
    
    __weak typeof(self) weakSelf = self;
    GRPCProtoCall *call = [self.client RPCTosendWithRequestsWriter:writer eventHandler:^(BOOL done,
                                                                                         AudioFragmentResponse * _Nullable response,
                                                                                         NSError * _Nullable error) {
        
        if (error != nil) {
            self.currentStatus = NORMAL;
            if (weakSelf.canRepChange) {
                [weakSelf.delegate bdasrStatusDidChanged:NORMAL];
            }
            if (weakSelf.canRepResult) {
                [weakSelf.delegate bdasrAnalizeDone:YES result:nil error:error];
            }
        }else {
//            NSLog(@"识别结果:%@", response);
            NSString *result = nil;
            if (response != nil) {
                result = [self resultToJsonString:response];
            }
            
            if (weakSelf.canRepResult) {
                [weakSelf.delegate bdasrAnalizeDone:done result:result error:nil];
            }
            
            if (done) {
                self.currentStatus = NORMAL;
                if (weakSelf.canRepChange) {
                    [weakSelf.delegate bdasrStatusDidChanged:NORMAL];
                }
            }
        }
    }];
    
    NSData *metaData = [request data];
    NSString *metaString = [metaData base64EncodedStringWithOptions:0];
    
    call.requestHeaders[@"audio_meta"] = metaString;
    
    if (call.state == GRXWriterStateNotStarted) {
        [call start];
    }
    
    self.grpcCall = call;
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

- (NSString *)resultToJsonString:(AudioFragmentResponse *)response {
    NSString *jsonString = nil;
    NSError *error;
    NSMutableDictionary *responseDic = [NSMutableDictionary dictionary];
    
    if (response != nil) {
        NSString *errorCode = [NSString stringWithFormat:@"%d", response.errorCode];
        NSString *errorMessage = response.errorMessage;
        
        [responseDic setObject:errorCode forKey:@"errorCode"];
        [responseDic setObject:errorMessage forKey:@"errorMessage"];
        
        NSMutableDictionary *resultDic = [NSMutableDictionary dictionary];
        AudioFragmentResult *audioFragment = response.audioFragment;
        if (audioFragment != nil) {
            NSString *startTime = audioFragment.startTime;
            NSString *endTime = audioFragment.endTime;
            NSString *result = audioFragment.result;
            NSString *serialNum = audioFragment.serialNum;
            NSString *completed = audioFragment.completed ? @"1" : @"0";
            
            [resultDic setObject:startTime forKey:@"startTime"];
            [resultDic setObject:endTime forKey:@"endTime"];
            [resultDic setObject:result forKey:@"result"];
            [resultDic setObject:serialNum forKey:@"serialNum"];
            [resultDic setObject:completed forKey:@"completed"];
        }
        
        [responseDic setObject:resultDic forKey:@"result"];
    }
    
    NSData *data = [NSJSONSerialization dataWithJSONObject:responseDic options:NSJSONWritingPrettyPrinted error:&error];
    
    if (data == nil) {
        NSLog(@"resultToJsonString get error:%@",error);
    }else {
        jsonString = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    }
    
    return jsonString;
}

@end
