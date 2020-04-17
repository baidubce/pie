//
//  BDTTSSessionManager.h
//  BaiduTTS
//
//  Created by Wu,Yunpeng01 on 2020/4/7.
//  Copyright © 2020 ACG. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BDTTSSessionManager : NSObject

+ (instancetype)sharedManager;

- (NSURLSessionTask *)Get:(NSString *)urlSring
               parameters:(NSDictionary * _Nullable)parameters
                  success:(void (^  _Nullable )(NSURLSessionTask * _Nonnull, id _Nonnull))success
                  failure:(void (^  _Nullable )(NSURLSessionTask * _Nonnull, NSError * _Nonnull))failure;

- (NSURLSessionTask *)Post:(NSString *)urlSring
                parameters:(NSDictionary * _Nullable)parameters
                   success:(void  (^ _Nullable )(NSURLSessionTask * _Nonnull, id _Nonnull))success
                   failure:(void  (^ _Nullable )(NSURLSessionTask * _Nonnull, NSError * _Nonnull))failure;

- (NSURLSessionTask *)Down:(NSString *)urlSring
                parameters:(NSDictionary * _Nullable)parameters
                   success:(void  (^ _Nullable )(id _Nonnull, NSURL * _Nonnull))success
                   failure:(void  (^ _Nullable )(NSError * _Nonnull))failure;

- (void)cancel;

@end

NS_ASSUME_NONNULL_END
