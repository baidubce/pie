//
//  BDTTSSessionManager.m
//  BaiduTTS
//
//  Created by Wu,Yunpeng01 on 2020/4/7.
//  Copyright © 2020 ACG. All rights reserved.
//

#import "BDTTSSessionManager.h"
#import "AFNetworking.h"
#import "BDTTSMacro.h"

static BDTTSSessionManager *ttsManager = nil;

@interface BDTTSSessionManager()

@property (nonatomic, strong) AFHTTPSessionManager *sessionManager;

@property (nonatomic, strong) NSMutableArray *tasks;

@end

@implementation BDTTSSessionManager

+ (instancetype)sharedManager {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        ttsManager = [[BDTTSSessionManager alloc] init];
        [ttsManager loadSessionManager];
    });
    return ttsManager;
}

#pragma mark session Manager
- (void)loadSessionManager {
    AFHTTPSessionManager *manager = [[AFHTTPSessionManager alloc] initWithBaseURL:[NSURL URLWithString:BDTTSHost]];

    manager.requestSerializer = [self setRequestSerializer];
    manager.responseSerializer = [self setResponseSerializer];

    self.sessionManager = manager;
    
    self.tasks = [NSMutableArray array];
}

- (AFHTTPRequestSerializer *)setRequestSerializer {
    AFHTTPRequestSerializer *serializer = [AFHTTPRequestSerializer serializer];
//    [serializer setValue:@"" forHTTPHeaderField:@""]; //设置请求头

    return serializer;
}

- (AFHTTPResponseSerializer *)setResponseSerializer {
    AFHTTPResponseSerializer *responseSerializer = [AFHTTPResponseSerializer serializer];
    responseSerializer.acceptableContentTypes = [NSSet setWithObjects:@"application/json", @"text/json", @"text/javascript",@"text/plain",@"text/html", nil];
    return responseSerializer;
}


- (NSURLSessionTask *)Get:(NSString *)urlSring parameters:(NSDictionary * _Nullable)parameters success:(void (^  _Nullable )(NSURLSessionTask * _Nonnull, id _Nonnull))success failure:(void (^  _Nullable )(NSURLSessionTask * _Nonnull, NSError * _Nonnull))failure {
    NSURLSessionTask *task = [self.sessionManager GET:urlSring parameters:parameters progress:nil success:success failure:failure];
    [self.tasks addObject:task];
    return task;
}

- (NSURLSessionTask *)Post:(NSString *)urlSring parameters:(NSDictionary *  _Nullable)parameters success:(void (^  _Nullable )(NSURLSessionTask * _Nonnull, id _Nonnull))success failure:(void (^  _Nullable )(NSURLSessionTask * _Nonnull, NSError * _Nonnull))failure {
    NSURLSessionTask *task = [self.sessionManager POST:urlSring parameters:parameters progress:nil success:success failure:failure];
    [self.tasks addObject:task];
    return task;
}

- (NSURLSessionTask *)Down:(NSString *)urlSring parameters:(NSDictionary *)parameters success:(void (^)(id _Nonnull, NSURL * _Nonnull))success failure:(void (^)(NSError * _Nonnull))failure {
    NSError *serializationError = nil;
    NSMutableURLRequest *request = [self.sessionManager.requestSerializer requestWithMethod:@"GET" URLString:urlSring parameters:parameters error:&serializationError];
    if (serializationError) {
        NSLog(@"invalid url request !!!");
        if (failure) {
            failure(serializationError);
        }
        return nil;
    }
    
    NSURLSessionDownloadTask *task = [self.sessionManager downloadTaskWithRequest:request progress:^(NSProgress * _Nonnull downloadProgress) {
        NSLog(@"now loading process: %@",downloadProgress);
    } destination:^NSURL * _Nonnull(NSURL * _Nonnull targetPath, NSURLResponse * _Nonnull response) {
        //file path
        NSString *cachePath = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject;
        NSString *fileName = [cachePath stringByAppendingPathComponent:response.suggestedFilename];
        
        NSFileManager *manager = [NSFileManager defaultManager];
        if ([manager fileExistsAtPath:fileName]) {
            NSError *error = nil;
            if ([manager removeItemAtPath:fileName error:&error]) {
                return [NSURL fileURLWithPath:fileName];
            }
        }

        return [NSURL fileURLWithPath:fileName];
    } completionHandler:^(NSURLResponse * _Nonnull response, NSURL * _Nullable filePath, NSError * _Nullable error) {
        if (error) {
            NSLog(@"download completed with error:%@", error);
            if (failure) {
                failure(serializationError);
            }
        }else {
            NSLog(@"download succeeded !");
            if (success) {
                success(response, filePath);
            }
        }
    }];
    
    [task resume];
    
    [self.tasks addObject:task];
    
    return task;
}

- (void)cancel {
    for (id task in self.tasks) {
        if ([task isKindOfClass:[NSURLSessionTask class]]) {
            [(NSURLSessionTask *)task cancel];
        }
    }
}

@end
