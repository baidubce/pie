//
//  BDASRConfig.m
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng(AI2B) on 2019/4/15.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import "BDASRConfig.h"
#import <CommonCrypto/CommonDigest.h>

NSString *kHostAddress = @"180.76.107.131";
int defServerPort = 8050;//8212;
NSString *defAppName = @"ios demo";
BOOL defEnableFlushData = YES;
int defBitDepth = 2;
double defSendPerSeconds = 0.02;
double defSleepRatio = 1;
int defTimeoutMinutes = 10;
int defLogLevel = 0;

static BDASRConfig *commonConfig = nil;

@implementation BDASRConfig

+ (instancetype)config {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        commonConfig = [[BDASRConfig alloc] init];
    });
    return commonConfig;
}

- (instancetype)init {
    self = [super init];
    if (self != nil) {
        self.hostAddress = kHostAddress;
        self.serverPort = defServerPort;
        self.product = CUSTOMERSERVICEFINANCE;
        self.productId = [NSString stringWithFormat:@"%lu",(unsigned long)CUSTOMERSERVICEFINANCE];
        self.appName = defAppName;
        self.enableFlushData = defEnableFlushData;
        self.bitDepth = defBitDepth;
        self.sendPerSeconds = defSendPerSeconds;
        self.sleepRatio = defSleepRatio;
        self.timeoutMinutes = defTimeoutMinutes;
        self.logLevel = defLogLevel;
        self.sampleRate = 8000;
        
        self.productIDDataSource = [NSArray arrayWithObjects:@"客服模型1903",
                               @"客服模型-旅游领域1904",
                               @"客服模型-股票领域1905",
                               @"客服模型-股票领域1906",
                               @"客服模型-股票领域1907",
                               @"输入法模型888",
                               @"远场模型1888",
                               @"远场模型-机器人领域1889",nil];
        
        self.productIDArray = [NSArray arrayWithObjects:@"1903",
                                    @"1904",
                                    @"1905",
                                    @"1906",
                                    @"1907",
                                    @"888",
                                    @"1888",
                                    @"1889",nil];
    }
    return self;
}

- (NSString *)hostAddress_Port {
    return [NSString stringWithFormat:@"%@:%d", self.hostAddress, self.serverPort];
}

//- (void)setProduct:(ASRProduct)product {
//    _product = product;
//    if (product == INPUT_METHOD || product == FAR_FIELD || product == FARFIELDROBOT) {
//        self.sampleRate = 16000;
//    }else {
//        self.sampleRate = 8000;
//    }
//}

- (NSString *)getUTCTimeString
{
    NSDate *currentDate = [NSDate dateWithTimeInterval:24*60*60 sinceDate:[NSDate date]];//获得当前一天后的UTC时间，设置为终止时间
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    //输入格式
    NSTimeZone *timeZone = [NSTimeZone timeZoneWithName:@"UTC"];
    [dateFormatter setTimeZone:timeZone];
    //输出格式
    [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss'Z'"];
    NSString *dateString = [dateFormatter stringFromDate:currentDate];
    return dateString;
}

- (NSString*) sha256:(NSString *)string
{
    const char *cstr = [string cStringUsingEncoding:NSUTF8StringEncoding];
    NSData *data = [NSData dataWithBytes:cstr length:string.length];
    
    uint8_t digest[CC_SHA256_DIGEST_LENGTH];
    
    CC_SHA256(data.bytes, (unsigned int)data.length, digest);
    
    NSMutableString* output = [NSMutableString stringWithCapacity:CC_SHA256_DIGEST_LENGTH * 2];
    
    for(int i = 0; i < CC_SHA256_DIGEST_LENGTH; i++)
        [output appendFormat:@"%02x", digest[i]];
    return output;
}

- (NSString *)getToken:(NSString *)string {
    return [self sha256:string];
}

@end
