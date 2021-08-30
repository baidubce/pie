//
//  BDTTSConfig.m
//  BaiduTTS
//
//  Created by Wu,Yunpeng01 on 2020/4/16.
//  Copyright © 2020 ACG. All rights reserved.
//

#import "BDTTSConfig.h"
#import <sys/sysctl.h>
#import <net/if.h>
#import <net/if_dl.h>
#import "BDTTSMacro.h"

@implementation BDTTSConfig

- (instancetype)init {
    if (self = [super init]) {
        self.host = [self hostURL];
        self.lan = BDTTS_lan;
        self.pdt = BDTTS_pdt;
        self.ctp = BDTTS_ctp;
        self.spd = BDTTS_spd;
        self.pit = BDTTS_pit;
        self.vol = BDTTS_vol;
        self.aue = BDTTS_aue;
        self.per = BDTTS_per;
    }
    return self;
}

- (NSString *)hostURL {
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

- (NSString *)cuid {
    if (_cuid == nil) {
        _cuid = [self getDeviceMacAddress];
    }
    return _cuid;
}

/*
 * 获取设备物理地址
 */
- (NSString *)getDeviceMacAddress {
    int                 mib[6];
    size_t              len;
    char                *buf;
    unsigned char       *ptr;
    struct if_msghdr    *ifm;
    struct sockaddr_dl  *sdl;
    
    mib[0] = CTL_NET;
    mib[1] = AF_ROUTE;
    mib[2] = 0;
    mib[3] = AF_LINK;
    mib[4] = NET_RT_IFLIST;
    
    if ((mib[5] = if_nametoindex("en0")) == 0) {
        printf("Error: if_nametoindex error/n");
        return NULL;
    }
    
    if (sysctl(mib, 6, NULL, &len, NULL, 0) < 0) {
        printf("Error: sysctl, take 1/n");
        return NULL;
    }
    
    if ((buf = malloc(len)) == NULL) {
        printf("Could not allocate memory. error!/n");
        return NULL;
    }
    
    if (sysctl(mib, 6, buf, &len, NULL, 0) < 0) {
        printf("Error: sysctl, take 2");
        return NULL;
    }
    
    
    ifm = (struct if_msghdr *)buf;
    
    sdl = (struct sockaddr_dl *)(ifm + 1);
    
    ptr = (unsigned char *)LLADDR(sdl);
    
    NSString *outstring = [NSString stringWithFormat:@"%02x:%02x:%02x:%02x:%02x:%02x", *ptr, *(ptr+1), *(ptr+2), *(ptr+3), *(ptr+4), *(ptr+5)];
    
    // release pointer
    free(buf);
    
    return [outstring lowercaseString];
}

@end
