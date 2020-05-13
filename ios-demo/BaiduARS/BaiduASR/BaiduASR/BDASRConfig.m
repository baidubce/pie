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
NSString *defServerPort = @"8050";//8212;
NSString *defAppName = @"iosdemo";
NSString *defProducID = @"1903";
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
        self.productId = defProducID;
        self.appName = defAppName;
        self.enableFlushData = defEnableFlushData;
        self.bitDepth = defBitDepth;
        self.sendPerSeconds = defSendPerSeconds;
        self.sleepRatio = defSleepRatio;
        self.timeoutMinutes = defTimeoutMinutes;
        self.logLevel = defLogLevel;
        self.sampleRate = 8000;
    }
    return self;
}

- (NSString *)hostAddress_Port {
    NSMutableString *host = [NSMutableString stringWithString:self.hostAddress];

    if (self.serverPort) {
        [host appendFormat:@":%@", self.serverPort];
    }

    return host;
}

- (NSString *)getTokenWithTimeStamp:(NSString *)timeStamp {
    if (self.userName.length && self.passWord.length) {
        return [self sha256:[NSString stringWithFormat:@"%@%@%@", self.userName, self.passWord, timeStamp]];
    }
    
    return nil;
}

- (NSString *)getSignKeyWithtimeStamp:(NSString *)timeStamp {
    
    if (self.accessKey.length && self.secrityKey.length) {
        return [self sha256:[NSString stringWithFormat:@"%@%@%@", self.accessKey, self.secrityKey, timeStamp]];
    }
    
    return nil;
}

- (NSString *)getAuthorization {
    NSString *timeStamp = [self getUTCTimeString];
    NSString *signKey = [self getSignKeyWithtimeStamp:timeStamp];
    
    if (signKey.length) {
        return [NSString stringWithFormat:@"bml-auth-v1/%@/%@/%@", self.accessKey, timeStamp, signKey];
    }
    
    return nil;
}

- (NSString*) sha256:(NSString *)string {
    const char *cstr = [string cStringUsingEncoding:NSUTF8StringEncoding];
    NSData *data = [NSData dataWithBytes:cstr length:string.length];
    
    uint8_t digest[CC_SHA256_DIGEST_LENGTH];
    
    CC_SHA256(data.bytes, (unsigned int)data.length, digest);
    
    NSMutableString* output = [NSMutableString stringWithCapacity:CC_SHA256_DIGEST_LENGTH * 2];
    
    for(int i = 0; i < CC_SHA256_DIGEST_LENGTH; i++)
        [output appendFormat:@"%02x", digest[i]];
    return output;
}

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

@end
