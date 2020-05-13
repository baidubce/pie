//
//  BDTTSRequestAuthorization.m
//  BaiduTTS
//
//  Created by Wu,Yunpeng01 on 2020/5/9.
//  Copyright © 2020 ACG. All rights reserved.
//

#import "BDTTSRequestAuthorization.h"
#import <CommonCrypto/CommonDigest.h>
#import <CommonCrypto/CommonCrypto.h>

#define BCEAuthVersion                      @"bce-auth-v1"
#define ExpirationPeriodInSeconds           @"1800"
#define SignHeadersFliter                   @[@"Host", @"Content-Length", @"Content-Type", @"Content-MD5", @"x-bce-"]

@implementation BDTTSRequestAuthorization

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

- (NSString *)hmac:(NSString *)plaintext withKey:(NSString *)key {
    const char *cKey  = [key cStringUsingEncoding:NSASCIIStringEncoding];
    const char *cData = [plaintext cStringUsingEncoding:NSASCIIStringEncoding];
    unsigned char cHMAC[CC_SHA256_DIGEST_LENGTH];

    CCHmac(kCCHmacAlgSHA256, cKey, strlen(cKey), cData, strlen(cData), cHMAC);
    NSData *HMACData = [NSData dataWithBytes:cHMAC length:sizeof(cHMAC)];

    const unsigned char *buffer = (const unsigned char *)[HMACData bytes];
    NSMutableString *HMAC = [NSMutableString stringWithCapacity:HMACData.length * 2];

    for (int i = 0; i < HMACData.length; ++i) {
        [HMAC appendFormat:@"%02x", buffer[i]];
    }

    return HMAC;
}

- (NSString *)urlEncodeUsingEncoding:(NSStringEncoding)encoding str:(NSString *)str
{
    return (NSString *)CFBridgingRelease(CFURLCreateStringByAddingPercentEscapes(
                                                                                 NULL,
                                                                                 (__bridge CFStringRef)str,
                                                                                 NULL,
                                                                                 (CFStringRef)@"!*'\"();:@&=+$,/?%#[]% ",
                                                                                 CFStringConvertNSStringEncodingToEncoding(encoding)));
}


+ (NSString *)getAuthorizationWithUri:(NSString *)uri ak:(NSString *)ak sk:(NSString *)sk timeStamp:(NSString *)timeStamp request:(NSURLRequest *)request {
    return [[[BDTTSRequestAuthorization alloc] init] getAuthorizationWithUri:uri
                                                                          ak:ak
                                                                          sk:sk
                                                                   timeStamp:timeStamp
                                                                     request:request];
}

- (NSString *)getAuthorizationWithUri:(NSString *)uri
                                   ak:(NSString *)ak
                                   sk:(NSString *)sk
                            timeStamp:(NSString *)timeStamp
                              request:(NSURLRequest *)request {
    // 生成认证字符串前缀
    NSString *authStringPrefix = [self authStringPrefixWithAK:ak timeStamp:timeStamp expirationPeriodInSeconds:ExpirationPeriodInSeconds];

    // 生成signKey
    NSString *signingKey = [self signKeyWithAuthStringPrefix:authStringPrefix sk:sk];
    
    // 生成canonicalRequest
    NSString *httpMethod = [request HTTPMethod];
    NSString *canonicalQueryString = [self canonicalQueryStringWithURLRequest:request];
    NSString *canonicalHeaders = [self canonicalHeadersWithURLRequest:request];
    
    NSString *canonicalRequest = [self canonicalRequestWithHttpMethod:httpMethod canonicalURL:uri canonicalQueryString:canonicalQueryString canonicalHeaders:canonicalHeaders];
    
    // 生成signature
    NSString *signature = [self hmac:canonicalRequest withKey:signingKey];
    
    // 生成authorization
    NSString *singedHeaders = [self signedHeadersWithURL:request];

    NSString *authorization = [self authorizationWithAuthStringPrefix:authStringPrefix signedHeaders:singedHeaders signature:signature];

    return authorization;
}

- (NSString *)authStringPrefixWithAK:(NSString *)ak
                           timeStamp:(NSString *)timeStamp
           expirationPeriodInSeconds:(NSString *)sec {
    return [NSString stringWithFormat:@"%@/%@/%@/%@", BCEAuthVersion, ak, timeStamp, sec];
}

- (NSString *)signKeyWithAuthStringPrefix:(NSString *)authStringPrefix sk:(NSString *)sk {
    return [self hmac:authStringPrefix withKey:sk];
}

- (NSString *)canonicalRequestWithHttpMethod:(NSString *)httpMethod canonicalURL:(NSString *)canonicalURL canonicalQueryString:(NSString *)canonicalQueryString canonicalHeaders:(NSString *)canonicalHeaders {
    return [NSString stringWithFormat:@"%@\n%@\n%@\n%@", httpMethod, canonicalURL, canonicalQueryString, canonicalHeaders];
}

- (NSString *)signatureWithCanonicalRequest:(NSString *)canonicalRequest signKey:(NSString *)signKey  {
    return [self hmac:canonicalRequest withKey:signKey];
}

- (NSString *)authorizationWithAuthStringPrefix:(NSString *)authStringPrefix signedHeaders:(NSString *)signedHeaders signature:(NSString *)signture {
    return [NSString stringWithFormat:@"%@/%@/%@", authStringPrefix, signedHeaders, signture];
}

- (NSString *)authorizationWithAK:(NSString *)ak timeStamp:(NSString *)timeStamp expirationPeriodInSeconds:(NSString *)sec signedHeaders:(NSString *)signedHeaders signature:(NSString *)signture {
    return [NSString stringWithFormat:@"%@/%@/%@/%@/%@/%@", BCEAuthVersion, ak, timeStamp, sec, signedHeaders, signture];
}

- (NSString *)canonicalQueryStringWithURLRequest:(NSURLRequest *)urlRequest {
    NSString *queryString = urlRequest.URL.query;
    
    NSArray *queryStringArray = [queryString componentsSeparatedByString:@"&"];
    
    NSMutableArray *encodedQueryStringArray = [NSMutableArray array];
    
    for (NSString *query in queryStringArray) {
        NSArray *queryA = [query componentsSeparatedByString:@"="];
        NSString *key = queryA.count > 0 ? [queryA firstObject] : nil;
        NSString *vaule = queryA.count > 1 ? [queryA lastObject] : @"";
        if (!key) continue;
        
        NSString *encodeKey = [self urlEncodeUsingEncoding:NSUTF8StringEncoding str:key];
        NSString *encodeValue = [self urlEncodeUsingEncoding:NSUTF8StringEncoding str:vaule];
        NSString *tempQueryS = [NSString stringWithFormat:@"%@=%@", encodeKey, encodeValue];
        [encodedQueryStringArray addObject: tempQueryS];
    }
    
    [encodedQueryStringArray sortUsingSelector:@selector(compare:)];
    
    NSMutableString *canonicalQueryString = [NSMutableString string];
    
    for (NSString *queryS in encodedQueryStringArray) {
        [canonicalQueryString appendString:queryS];
        
        if (![queryS isEqualToString:encodedQueryStringArray.lastObject]) {
            [canonicalQueryString appendString:@"&"];
        }
    }
    
    return canonicalQueryString;
}

- (NSString *)canonicalHeadersWithURLRequest:(NSURLRequest *)urlRequest {
    NSDictionary *headers = urlRequest.allHTTPHeaderFields;
    
    NSArray *signHeaders = [self signHeadersWithHeaders:headers];
    
    NSMutableArray *encodedHeaderArray = [NSMutableArray array];
    
    for (NSString *key in signHeaders) {
        NSString *vaule = [headers valueForKey:key];

        NSString *encodeKey = [self urlEncodeUsingEncoding:NSUTF8StringEncoding str:[key lowercaseString]];
        NSString *encodeValue = [self urlEncodeUsingEncoding:NSUTF8StringEncoding str:vaule];
        NSString *tempQueryS = [NSString stringWithFormat:@"%@:%@", encodeKey, encodeValue];
        [encodedHeaderArray addObject: tempQueryS];
    }
    
    [encodedHeaderArray sortUsingSelector:@selector(compare:)];
    
    NSMutableString *canonicalHeadersString = [NSMutableString string];
    
    for (NSString *encodedHeader in encodedHeaderArray) {
        [canonicalHeadersString appendString:encodedHeader];
        
        if (![encodedHeader isEqualToString:encodedHeaderArray.lastObject]) {
            [canonicalHeadersString appendString:@"\n"];
        }
    }
    
    return canonicalHeadersString;
}

- (NSArray *)signHeadersWithHeaders:(NSDictionary *)headers {
    NSMutableArray *signHeaders = [NSMutableArray array];
    
    for (NSString *key in [headers allKeys]) {
        for (NSString *fliterKey in SignHeadersFliter) {
            if ([key isEqualToString:fliterKey] || [key containsString:fliterKey]) {
                [signHeaders addObject:key];
            }
        }
    }
    return signHeaders;
}

- (NSString *)signedHeadersWithURL:(NSURLRequest *)request {
    NSDictionary *headers = request.allHTTPHeaderFields;
    NSArray *signHeaders = [self signHeadersWithHeaders:headers];
    NSMutableArray *headerArray = [NSMutableArray arrayWithArray:signHeaders];
    [headerArray sortUsingSelector:@selector(compare:)];

    NSMutableString *signedHeadersString = [NSMutableString string];

    for (NSString *header in headerArray) {
        [signedHeadersString appendString:[header lowercaseString]];
        
        if (![header isEqualToString:headerArray.lastObject]) {
            [signedHeadersString appendString:@";"];
        }
    }
    return signedHeadersString;
}

@end
