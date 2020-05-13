//
//  BDTTSRequest.m
//  BaiduTTS
//
//  Created by Wu,Yunpeng01 on 2020/4/7.
//  Copyright © 2020 ACG. All rights reserved.
//

#import "BDTTSRequest.h"
#import "BDTTSSessionManager.h"
#import "BDTTSMacro.h"
static BDTTSRequest *ttsRequset;

@implementation BDTTSRequest

//+ (instancetype)sharedInstance {
//    static dispatch_once_t onceToken;
//    dispatch_once(&onceToken, ^{
//        ttsRequset = [[BDTTSRequest alloc] init];
//    });
//    return ttsRequset;
//}

+ (BOOL)checkoutURL:(NSString *)url {
    if (url && url.length > 0) {
        return YES;
    }
    NSLog(@"invaild url, please check it !!!");
    return NO;
}

+ (NSString *)requestURL {
    NSMutableString *urlString = [NSMutableString stringWithString:BDTTSProtocol];
    
    if (BDTTSHost.length) {
        [urlString appendFormat:@"://%@", BDTTSHost];
    }
    
    if (BDTTSPort.length) {
        [urlString appendFormat:@":%@", BDTTSPort];
    }
    
    if (BDTTSPath.length) {
        [urlString appendString:BDTTSPath];
    }
    
    return urlString;
}

+ (void)requestGet:(NSString *)urlSring
        parameters:(NSDictionary * _Nullable)parameters
           success:(void (^ _Nullable )(NSDictionary *result))success
           failure:(void (^ _Nullable )(NSError *error))failure {
    if (![self checkoutURL:urlSring]) return;
    
    [[BDTTSSessionManager sharedManager] Get:urlSring parameters:parameters success:^(NSURLSessionTask * task, id response) {
        if (response && [response isKindOfClass:[NSDictionary class]]) {
//            NSString *code = [response valueForKey:@"code"];
            if (success) {
                success(response);
            }
        }
    } failure:^(NSURLSessionTask * task, NSError * error) {
        if (failure) {
            failure(error);
        }
    }];
}

+ (void)requestPost:(NSString *)urlSring
         parameters:(NSDictionary * _Nullable)parameters
            success:(void (^ _Nullable )(NSDictionary *result))success
            failure:(void (^ _Nullable )(NSError *error))failure {
    if (![self checkoutURL:urlSring]) return;
    
    [[BDTTSSessionManager sharedManager] Post:urlSring parameters:parameters success:^(NSURLSessionTask * task, id response) {
        if (response && [response isKindOfClass:[NSDictionary class]]) {
            //            NSString *code = [response valueForKey:@"code"];
            if (success) {
                success(response);
            }
        }
    } failure:^(NSURLSessionTask * task, NSError * error) {
        if (failure) {
            failure(error);
        }
    }];
}

+ (void)requestDownload:(NSString *)urlSring
             parameters:(NSDictionary *)parameters
                success:(void (^)(NSURL * _Nonnull))success
                failure:(void (^)(NSError * _Nonnull))failure {
    if (![self checkoutURL:urlSring]) return;
    
    [[BDTTSSessionManager sharedManager] Down:urlSring parameters:parameters success:^(id response, NSURL *filePath) {
        if (success) {
            success(filePath);
        }
    } failure:^(NSError *error) {
        if (failure) {
            failure(error);
        }
    }];
}

+ (void)cancel {
    [[BDTTSSessionManager sharedManager] cancel];
}

@end
