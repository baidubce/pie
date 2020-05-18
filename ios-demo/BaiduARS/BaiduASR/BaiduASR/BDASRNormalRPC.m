//
//  BDASRNormalRPC.m
//  BaiduASR
//
//  Created by Wu,Yunpeng01 on 2020/5/17.
//  Copyright © 2020 Cloud. All rights reserved.
//

#import "BDASRNormalRPC.h"
#import <GRPCClient/GRPCCall+Tests.h>
#import <ProtoRPC/ProtoService.h>
#import <RxLibrary/GRXWriter+Immediate.h>
#import <RxLibrary/GRXWriter+Transformations.h>
#import <RxLibrary/GRXBufferedPipe.h>
#import <ProtoRPC/ProtoRPC.h>


#import "AudioStreaming.pbrpc.h"
#import "AudioStreaming.pbobjc.h"
#import "BDASRConfig.h"
#import "BDAudioManager.h"

#import "BDRTAManager.h"

@interface BDASRNormalRPC()

@property (nonatomic, strong) AsrService *client;
@property (nonatomic, strong) GRPCProtoCall *call;
@property (nonatomic, strong) BDAudioManager *recorder;
@property (nonatomic, strong) BDASRConfig *asrConfig;

@end

@implementation BDASRNormalRPC

- (instancetype)init {
    self = [super init];
    if (self != nil) {
        self.asrConfig = [BDASRConfig config];
        self.recorder = [BDAudioManager recorder];
        
        [self configHost];
    }
    return self;
}

- (void)configHost {
    NSString *hostAddress = [self.asrConfig hostAddress_Port];
    [GRPCCall useInsecureConnectionsForHost:hostAddress];
    self.client = [[AsrService alloc] initWithHost:hostAddress];
}

- (void)startRecord {
    [self.recorder resetRecorder];
    [self.recorder startRecorder];
}

- (void)stopRecord {
    [self.recorder stopRecorder];
    [self startAnalize];
}

- (void)cancel {
    [self.call cancel];
}

#pragma private motherd
- (void)startAnalize {
    NSString *authorization = [self.asrConfig getAuthorization];
    NSString *userName = self.asrConfig.userName;
    NSString *passWord = self.asrConfig.passWord;
    
    if (!authorization.length) {
        if (!(userName.length > 0 && passWord.length > 0)) {
            NSString *errorDes = @"invalid username or password, please check them!";

            if ([self.delegate respondsToSelector:@selector(normalAnalizeDone:result:error:)]) {
                NSError *error = [NSError errorWithDomain:errorDes code:-100 userInfo:nil];
                [self.delegate normalAnalizeDone:NO result:nil error:error];
            }
            return;
        }
    }

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
    
    NSString *timeStamp = [self.asrConfig getUTCTimeString];
    request.expireTime = timeStamp;
    request.token = [self.asrConfig getTokenWithTimeStamp:timeStamp];
    
    NSLog(@"audio_meta : %@", request);
    
    NSString *filePath = [self.recorder currentRecorderFilePath];
    NSData *data = [[NSData alloc] initWithContentsOfFile:filePath];
    
    NSArray *audioStream = [self getAudioStreamWithData:data];
    
    GRXWriter *writer = [GRXWriter writerWithContainer:audioStream];
    
    __weak typeof(self) weakSelf = self;
    GRPCProtoCall *call = [self.client RPCTosendWithRequestsWriter:writer eventHandler:^(BOOL done,
                                                                                         AudioFragmentResponse * _Nullable response,
                                                                                         NSError * _Nullable error) {
        
        if ([weakSelf.delegate respondsToSelector:@selector(normalAnalizeDone:result:error:)]) {
            
            NSString *jsonResult = [self resultToJsonString:response];
            
            [weakSelf.delegate normalAnalizeDone:done result:jsonResult error:error];
        }
    }];
    
    NSData *metaData = [request data];
    NSString *metaString = [metaData base64EncodedStringWithOptions:0];
    
    
    call.requestHeaders[@"audio_meta"] = metaString;
    
    if (authorization.length) {
        call.requestHeaders[@"authorization"] = authorization;
    }
    
    if (call.state == GRXWriterStateNotStarted) {
        [call start];
    }
    
    self.call = call;
}

- (NSArray *)getAudioStreamWithData:(NSData *)data {
    int length = (int)data.length;
    int current_l = 0;
    int step = 640;
    
    NSMutableArray *audioStream = [NSMutableArray arrayWithCapacity:10];
    if (length >= step) {
        while (current_l < length - step) {
            AudioFragmentRequest *dataRequest = [[AudioFragmentRequest alloc] init];
            dataRequest.audioData = [data subdataWithRange:NSMakeRange(current_l, step)];
            [audioStream addObject:dataRequest];
            
            current_l += step;
        };
        AudioFragmentRequest *dataRequest = [[AudioFragmentRequest alloc] init];
        dataRequest.audioData = [data subdataWithRange:NSMakeRange(current_l, length - current_l)];
        [audioStream addObject:dataRequest];
    }else {
        AudioFragmentRequest *dataRequest = [[AudioFragmentRequest alloc] init];
        dataRequest.audioData = [data subdataWithRange:NSMakeRange(current_l, length)];
        [audioStream addObject:dataRequest];
    }
    return audioStream;
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
