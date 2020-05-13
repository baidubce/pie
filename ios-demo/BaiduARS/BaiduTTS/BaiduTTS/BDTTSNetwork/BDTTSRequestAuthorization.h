//
//  BDTTSRequestAuthorization.h
//  BaiduTTS
//
//  Created by Wu,Yunpeng01 on 2020/5/9.
//  Copyright © 2020 ACG. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BDTTSRequestAuthorization : NSObject

+ (NSString *)getAuthorizationWithUri:(NSString *)uri
                                   ak:(NSString *)ak
                                   sk:(NSString *)sk
                            timeStamp:(NSString *)timeStamp
                              request:(NSURLRequest *)request;

@end

NS_ASSUME_NONNULL_END
