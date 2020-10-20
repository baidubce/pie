//
//  BDASRInstance.m
//  BaiduASR
//
//  Created by Wu,Yunpeng(AI2B) on 2019/5/7.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import "BDASRInstance.h"
#import "BDASRConfig.h"

#import "BDASRNormalRPC.h"
#import "BDASRStreamRPC.h"
#import "AudioStreaming.pbobjc.h"


static BDASRInstance *asrInstance = nil;

@interface BDASRInstance()<BDASRNormalRPCDelegate, BDASRStreamRPCDelegate>

@property (nonatomic, assign) BDASRSTATUS currentStatus;

@property (nonatomic, strong) BDASRConfig *asrConfig;

@property (nonatomic, strong) BDASRNormalRPC *normalRPCManager;
@property (nonatomic, strong) BDASRStreamRPC *streamRPCManager;

@property (nonatomic, assign) BOOL canRepChange;
@property (nonatomic, assign) BOOL canRepResult;
@property (nonatomic, assign) BOOL canRepStreamResult;

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
        
        self.normalRPCManager = [[BDASRNormalRPC alloc] init];
        self.normalRPCManager.delegate = self;
        self.streamRPCManager = [[BDASRStreamRPC alloc] init];
        self.streamRPCManager.delegate = self;
    }
    return self;
}

#pragma private motherd
- (void)configHost {
    [self.normalRPCManager configHost];
    [self.streamRPCManager configHost];
}

- (BDASRSTATUS)status {
    return self.currentStatus;
}

- (BOOL)canRepChange {
    return [self.delegate respondsToSelector:@selector(bdasrStatusDidChanged:)];
}

- (BOOL)canRepResult {
    return [self.delegate respondsToSelector:@selector(bdasrAnalizeDone:result:error:)];
}

- (BOOL)canRepStreamResult {
    return [self.delegate respondsToSelector:@selector(bdasrRealTimeAnalizeResult:error:)];
}

#pragma interfaces
- (void)setDelegate:(id<BDASRDelegate>)delegate {
    _delegate = delegate;
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
    config.serverPort = serverPort;
    [self configHost];
}

- (void)setProductId:(NSString *)productId {
    BDASRConfig *config = [BDASRConfig config];
    config.productId = productId;
}

- (void)setSampleRate:(NSString *)sampleRate {
    BDASRConfig *config = [BDASRConfig config];
    config.sampleRate = [sampleRate intValue];
    
    [self.normalRPCManager configRecorder];
    [self.streamRPCManager configRecorder];
}

- (void)setAppName:(NSString *)appName {
    BDASRConfig *config = [BDASRConfig config];
    config.appName = appName;
}

- (void)startRecord {
    self.currentStatus = RECORDING;
    if (self.canRepChange) [self.delegate bdasrStatusDidChanged:RECORDING];
    
    [self.normalRPCManager startRecord];
}

- (void)stopRecord {
    self.currentStatus = ANALIZING;
    if (self.canRepChange) [self.delegate bdasrStatusDidChanged:ANALIZING];

    [self.normalRPCManager stopRecord];
}

- (void)cancel {
    self.currentStatus = NORMAL;
    [self.normalRPCManager cancel];
    if (self.canRepChange) [self.delegate bdasrStatusDidChanged:NORMAL];
    if (self.canRepResult) [self.delegate bdasrAnalizeDone:NO result:@"REQUEST CANCELED" error:nil];
}

- (void)startRealTimeRecord {
    self.currentStatus = ANALIZING;
    if (self.canRepChange) [self.delegate bdasrStatusDidChanged:ANALIZING];
    [self.streamRPCManager startStream];
}

- (void)stopRealTimeRecord {
    self.currentStatus = NORMAL;
    if (self.canRepChange) [self.delegate bdasrStatusDidChanged:NORMAL];
    [self.streamRPCManager stopStream];
}

#pragma BDASRNormalRPCDelegate
- (void)normalAnalizeDone:(BOOL)done result:(id)response error:(NSError *)error {
    if (error != nil) {
        self.currentStatus = NORMAL;
        if (self.canRepChange) {
            [self.delegate bdasrStatusDidChanged:NORMAL];
        }
        if (self.canRepResult) {
            [self.delegate bdasrAnalizeDone:YES result:nil error:error];
        }
    }else {
        if (self.canRepResult) {
            [self.delegate bdasrAnalizeDone:done result:response error:nil];
        }
        
        if (done) {
            self.currentStatus = NORMAL;
            if (self.canRepChange) {
                [self.delegate bdasrStatusDidChanged:NORMAL];
            }
        }
    }
}

#pragma BDASRStreamRPCDelegate
- (void)realTimeAnalizeResult:(id)result error:(NSError *)error {
    if (error != nil) {
        if (self.canRepStreamResult) {
            [self.delegate bdasrRealTimeAnalizeResult:result error:error];
        }
    }else {
        if (self.canRepStreamResult) {
            [self.delegate bdasrRealTimeAnalizeResult:result error:error];
        }
    }
}

@end
