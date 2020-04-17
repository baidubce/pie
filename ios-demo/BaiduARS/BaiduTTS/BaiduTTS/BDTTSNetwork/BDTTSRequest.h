//
//  BDTTSRequest.h
//  BaiduTTS
//
//  Created by Wu,Yunpeng01 on 2020/4/7.
//  Copyright © 2020 ACG. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BDTTSRequest : NSObject

//+ (instancetype)sharedInstance;

+ (void)requestGet:(NSString *)urlSring
        parameters:(NSDictionary * _Nullable)parameters
           success:(void (^ _Nullable )(NSDictionary *result))success
           failure:(void (^ _Nullable )(NSError *error))failure;

+ (void)requestPost:(NSString *)urlSring
         parameters:(NSDictionary * _Nullable)parameters
            success:(void (^ _Nullable )(NSDictionary *result))success
            failure:(void (^ _Nullable )(NSError *error))failure;

+ (void)requestDownload:(NSString *)urlSring
             parameters:(NSDictionary * _Nullable)parameters
                success:(void (^ _Nullable )(NSURL *filePath))success
                failure:(void (^ _Nullable )(NSError *error))failure;

+ (void)cancel;

@end

NS_ASSUME_NONNULL_END
