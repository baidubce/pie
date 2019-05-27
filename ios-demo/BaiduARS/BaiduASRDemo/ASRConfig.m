//
//  ASRConfig.m
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng(AI2B) on 2019/5/9.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import "ASRConfig.h"
#import <BaiduASR/BDASRInstance.h>

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
        NSString *userName = [defaults objectForKey:@"userName"] ?: @"";
        NSString *passWord = [defaults objectForKey:@"passWord"] ?: @"";
        NSString *hostAddress = [defaults objectForKey:@"hostAddress"] ?: @"180.76.107.131";
        NSString *serverPort = [defaults objectForKey:@"serverPort"] ?: @"8050";
        NSString *productId = [defaults objectForKey:@"productId"] ?: @"1906";
        NSString *sampleRate = [defaults objectForKey:@"sampleRate"] ?: @"8000";
        
        self.userName = userName;
        self.passWord = passWord;
        self.hostAddress = hostAddress;
        self.serverPort = serverPort;
        self.productId = productId;
        self.sampleRate = sampleRate;
        
        self.productIDDataSource = [NSArray arrayWithObjects:@"客服模型1903",
                                    @"客服模型-旅游领域1904",
                                    @"客服模型-股票领域1905",
                                    @"客服模型-金融领域1906",
                                    @"客服模型-能源领域1907",
                                    @"输入法模型888",
                                    @"远场模型1888",
                                    @"远场模型-机器人领域1889",
                                    @"远场模型-法院用1",
                                    nil];
        
        self.productIDArray = [NSArray arrayWithObjects:@"1903",
                               @"1904",
                               @"1905",
                               @"1906",
                               @"1907",
                               @"888",
                               @"1888",
                               @"1889",
                               @"1",
                               nil];
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
    NSString *hostAddress = self.hostAddress ?: @"180.76.107.131";
    NSString *serverPort = self.serverPort ?: @"8050";
    NSString *productId = self.productId ?: @"1906";
    NSString *sampleRate = self.sampleRate ?: @"8000";
    
    [defaults setObject:userName forKey:@"userName"];
    [defaults setObject:passWord forKey:@"passWord"];
    [defaults setObject:hostAddress forKey:@"hostAddress"];
    [defaults setObject:serverPort forKey:@"serverPort"];
    [defaults setObject:productId forKey:@"productId"];
    [defaults setObject:sampleRate forKey:@"sampleRate"];
    
    [defaults synchronize];
    
    BDASRInstance *ins = [BDASRInstance shareInstance];
    [ins setHostAddress:hostAddress];
    [ins setServerPort:serverPort];
    [ins setProductId:productId];
    [ins setSampleRate:sampleRate];
    [ins refreshConfig];
}

- (void)reset {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    [defaults removeObjectForKey:@"hostAddress"];
    [defaults removeObjectForKey:@"serverPort"];
    [defaults removeObjectForKey:@"productId"];
    [defaults removeObjectForKey:@"sampleRate"];
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
