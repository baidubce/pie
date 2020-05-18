//
//  BDASRStreamRPC.m
//  BaiduASR
//
//  Created by Wu,Yunpeng01 on 2020/5/13.
//  Copyright © 2020 Cloud. All rights reserved.
//

#import "BDASRStreamRPC.h"
#import <GRPCClient/GRPCCall+Tests.h>
#import <ProtoRPC/ProtoService.h>
#import <RxLibrary/GRXWriter+Immediate.h>
#import <RxLibrary/GRXWriter+Transformations.h>
#import <RxLibrary/GRXBufferedPipe.h>
#import <ProtoRPC/ProtoRPC.h>


#import "AudioStreaming.pbrpc.h"
#import "AudioStreaming.pbobjc.h"
#import "BDASRConfig.h"
#import "BDRTAManager.h"

static dispatch_queue_t rpc_stream_queue;

@interface BDASRStreamRPC() <GRPCProtoResponseHandler, BDRTADelegate>

@property (nonatomic, strong) AsrService *client;
@property (nonatomic, strong) GRPCStreamingProtoCall *call;
@property (nonatomic, strong) BDASRConfig *asrConfig;
@property (nonatomic, strong) BDRTAManager *rtaManager;

@end

@implementation BDASRStreamRPC

- (instancetype)init {
    self = [super init];
    if (self != nil) {
        self.asrConfig = [BDASRConfig config];
        self.rtaManager = [[BDRTAManager alloc] init];
        self.rtaManager.delegate = self;
        rpc_stream_queue = dispatch_queue_create("com.baidu.asr.stream", DISPATCH_QUEUE_CONCURRENT);
        [self configHost];
    }
    return self;
}

- (void)configHost {
    NSString *hostAddress = [self.asrConfig hostAddress_Port];
    self.client = [[AsrService alloc] initWithHost:hostAddress];
}

- (dispatch_queue_t)dispatchQueue {
    return rpc_stream_queue;
}

- (void)didReceiveInitialMetadata:(NSDictionary *)initialMetadata {
//    NSLog(@"didReceiveInitialMetadata---:%@", initialMetadata);
}

- (void)didReceiveProtoMessage:(GPBMessage *)message {
//    NSLog(@"didReceiveProtoMessage---:%@", message);
    
    if ([message isKindOfClass:[AudioFragmentResponse class]]) {
        AudioFragmentResponse *response = (AudioFragmentResponse *)message;
        NSString *responseString = [self resultToJsonString:response];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            if ([self.delegate respondsToSelector:@selector(realTimeAnalizeResult:error:)]) {
                [self.delegate realTimeAnalizeResult:responseString error:nil];
            }
        });
    }
}

- (void)didCloseWithTrailingMetadata:(nullable NSDictionary *)trailingMetadata error:(nullable NSError *)error {
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(realTimeAnalizeResult:error:)]) {
            [self.delegate realTimeAnalizeResult:trailingMetadata error:error];
        }
    });
}

#pragma BDRTADELEGATE

- (void)returnData:(NSMutableData *)data {
//    NSLog(@"录音中 ----- ：%@", data);
    AudioFragmentRequest *dataRequest = [[AudioFragmentRequest alloc] init];
    dataRequest.audioData = [data subdataWithRange:NSMakeRange(0, 640)];
    
    [self.call writeMessage:dataRequest];
}

- (void)startStream {
    [self.rtaManager startRecord];
    [self startStreaming];
}

- (void)stopStream {
    [self.rtaManager stopRecord];
    [self.call finish];
}

- (void)startStreaming {
    NSString *authorization = [self.asrConfig getAuthorization];
    NSString *userName = self.asrConfig.userName;
    NSString *passWord = self.asrConfig.passWord;
    
    if (!authorization.length) {
        if (!(userName.length > 0 && passWord.length > 0)) {
            if ([self.delegate respondsToSelector:@selector(realTimeAnalizeResult:error:)]) {
                NSString *errorDes = @"invalid username or password, please check them!";
                
                NSError *error = [NSError errorWithDomain:errorDes code:-100 userInfo:nil];
                [self.delegate realTimeAnalizeResult:nil error:error];
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
    
    NSData *metaData = [request data];
    NSString *metaString = [metaData base64EncodedStringWithOptions:0];
    
    NSMutableDictionary *metaDic = [NSMutableDictionary dictionary];
    [metaDic setValue:metaString forKey:@"audio_meta"];
    if (authorization.length) {
        [metaDic setValue:authorization forKey:@"authorization"];
    }
    
    GRPCMutableCallOptions *options = [[GRPCMutableCallOptions alloc] init];
    [options setInitialMetadata:metaDic];
    [options setServerAuthority:nil];
    [options setPEMRootCertificates:nil];
    [options setTransportType:GRPCTransportTypeInsecure];

    
    GRPCStreamingProtoCall *call = [self.client sendWithResponseHandler:self callOptions:options];
    self.call = call;
    
    [call start];
}

- (NSArray *)getAudioStreamWithData:(NSData *)data {
    int length = (int)data.length * 0.8;
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
