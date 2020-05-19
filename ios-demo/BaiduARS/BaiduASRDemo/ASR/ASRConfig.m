//
//  ASRConfig.m
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng(AI2B) on 2019/5/9.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import "ASRConfig.h"
#import <BaiduASR/BDASRInstance.h>

// 云
//#define HOST @"asr.baiduai.cloud" //180.76.107.131
//#define PORT @"8051"
//#define PID  @"1906"

//ai中台
//#define HOST @"10.161.115.27"
//#define PORT @"8089"
//#define PID  @"1906"

#define HOST @"180.76.161.229"
#define PORT @"8010"
#define PID  @"888"
#define SAMPLERATE  @"16000"
#define USERNAME @"admin"
#define PASSWORD @"1234567809"

#define defIDdess [NSArray arrayWithObjects:@"客服模型1903", \
                                @"客服模型-旅游领域1904", \
                                @"客服模型-股票领域1905", \
                                @"客服模型-金融领域1906", \
                                @"客服模型-能源领域1907", \
                                @"演讲模型1912", \
                                @"输入法模型888", \
                                @"远场模型1888", \
                                @"远场模型-机器人领域1889", \
                                @"远场模型-法院用1", \
                                nil];

#define defIDS [NSArray arrayWithObjects:@"1903", \
                           @"1904", \
                           @"1905", \
                           @"1906", \
                           @"1907", \
                           @"1912", \
                           @"888", \
                           @"1888", \
                           @"1889", \
                           @"1", \
                           nil];

static ASRConfig *config = nil;
@implementation ASRConfig

+ (instancetype)config {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        config = [[ASRConfig alloc] init];
    });
    return config;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        NSString *userName = [defaults objectForKey:@"userName"] ?: USERNAME;
        NSString *passWord = [defaults objectForKey:@"passWord"] ?: PASSWORD;
        NSString *hostAddress = [defaults objectForKey:@"hostAddress"] ?: HOST;
        NSString *serverPort = [defaults objectForKey:@"serverPort"] ?: PORT;
        NSString *productId = [defaults objectForKey:@"productId"] ?: PID;
        NSString *sampleRate = [defaults objectForKey:@"sampleRate"] ?: SAMPLERATE;
        NSString *appName = [defaults objectForKey:@"appName"] ?: @"iosdemo";
        
        NSArray *productIDArray = [defaults objectForKey:@"productIDArray"] ?: defIDS;
        NSArray *productIDDataSource = [defaults objectForKey:@"productIDDataSource"] ?: defIDdess;
        
        self.userName = userName;
        self.passWord = passWord;
        self.hostAddress = hostAddress;
        self.serverPort = serverPort;
        self.productId = productId;
        self.sampleRate = sampleRate;
        self.appName = appName;
        self.productIDArray = productIDArray;
        self.productIDDataSource = productIDDataSource;
    }
    return self;
}

- (NSString *)productDescription {
    NSString *proDes = nil;
    
    NSInteger index = [self.productIDArray indexOfObject:self.productId ?: @"0"];
    if (index != NSNotFound && index <= self.productIDDataSource.count && index >= 0) {
        proDes = [self.productIDDataSource objectAtIndex:index];
    }
    
    return proDes;
}

- (void)save {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    NSString *userName = self.userName ?: @"";
    NSString *passWord = self.passWord ?: @"";
    NSString *hostAddress = self.hostAddress ?: HOST;
    NSString *serverPort = self.serverPort ?: PORT;
    NSString *productId = self.productId ?: PID;
    NSString *sampleRate = self.sampleRate ?: SAMPLERATE;
    NSString *appName = self.appName ?: @"iosdemo";
    
    NSArray *productIDArray = self.productIDArray ?: @[];
    NSArray *productIDDataSource = self.productIDDataSource ?: @[];
    
    [defaults setObject:userName forKey:@"userName"];
    [defaults setObject:passWord forKey:@"passWord"];
    [defaults setObject:hostAddress forKey:@"hostAddress"];
    [defaults setObject:serverPort forKey:@"serverPort"];
    [defaults setObject:productId forKey:@"productId"];
    [defaults setObject:sampleRate forKey:@"sampleRate"];
    [defaults setObject:appName forKey:@"appName"];
    
    [defaults setObject:productIDArray forKey:@"productIDArray"];
    [defaults setObject:productIDDataSource forKey:@"productIDDataSource"];
    
    [defaults synchronize];
    
    BDASRInstance *ins = [BDASRInstance shareInstance];
    [ins setHostAddress:hostAddress];
    [ins setServerPort:serverPort];
    [ins setProductId:productId];
    [ins setSampleRate:sampleRate];
    [ins setAppName:appName];
    [ins refreshConfig];
}

- (void)reset {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    [defaults removeObjectForKey:@"hostAddress"];
    [defaults removeObjectForKey:@"serverPort"];
    [defaults removeObjectForKey:@"productId"];
    [defaults removeObjectForKey:@"sampleRate"];
    [defaults removeObjectForKey:@"appName"];
    [defaults removeObjectForKey:@"productIDArray"];
    [defaults removeObjectForKey:@"productIDDataSource"];
    [defaults synchronize];
}

- (void)login {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    NSString *userName = self.userName ?: @"";
    NSString *passWord = self.passWord ?: @"";
    
    [defaults setObject:userName forKey:@"userName"];
    [defaults setObject:passWord forKey:@"passWord"];
    
    [defaults synchronize];
    
    BDASRInstance *ins = [BDASRInstance shareInstance];
    [ins setUserName:userName passWord:passWord];
}

- (void)logout {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    [defaults removeObjectForKey:@"userName"];
    [defaults removeObjectForKey:@"passWord"];
    
    [defaults synchronize];
    
    BDASRInstance *ins = [BDASRInstance shareInstance];
    [ins setUserName:@"" passWord:@""];
}

@end
