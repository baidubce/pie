#if !defined(GPB_GRPC_PROTOCOL_ONLY) || !GPB_GRPC_PROTOCOL_ONLY
#import "AudioStreaming.pbrpc.h"
#import "AudioStreaming.pbobjc.h"
#import <ProtoRPC/ProtoRPC.h>
#import <RxLibrary/GRXWriter+Immediate.h>


@implementation AsrService

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wobjc-designated-initializers"

// Designated initializer
- (instancetype)initWithHost:(NSString *)host callOptions:(GRPCCallOptions *_Nullable)callOptions {
  return [super initWithHost:host
                 packageName:@"com.baidu.acu.pie"
                 serviceName:@"AsrService"
                 callOptions:callOptions];
}

- (instancetype)initWithHost:(NSString *)host {
  return [super initWithHost:host
                 packageName:@"com.baidu.acu.pie"
                 serviceName:@"AsrService"];
}

#pragma clang diagnostic pop

// Override superclass initializer to disallow different package and service names.
- (instancetype)initWithHost:(NSString *)host
                 packageName:(NSString *)packageName
                 serviceName:(NSString *)serviceName {
  return [self initWithHost:host];
}

- (instancetype)initWithHost:(NSString *)host
                 packageName:(NSString *)packageName
                 serviceName:(NSString *)serviceName
                 callOptions:(GRPCCallOptions *)callOptions {
  return [self initWithHost:host callOptions:callOptions];
}

#pragma mark - Class Methods

+ (instancetype)serviceWithHost:(NSString *)host {
  return [[self alloc] initWithHost:host];
}

+ (instancetype)serviceWithHost:(NSString *)host callOptions:(GRPCCallOptions *_Nullable)callOptions {
  return [[self alloc] initWithHost:host callOptions:callOptions];
}

#pragma mark - Method Implementations

#pragma mark send(stream AudioFragmentRequest) returns (stream AudioFragmentResponse)

// Deprecated methods.
- (void)sendWithRequestsWriter:(GRXWriter *)requestWriter eventHandler:(void(^)(BOOL done, AudioFragmentResponse *_Nullable response, NSError *_Nullable error))eventHandler{
  [[self RPCTosendWithRequestsWriter:requestWriter eventHandler:eventHandler] start];
}
// Returns a not-yet-started RPC object.
- (GRPCProtoCall *)RPCTosendWithRequestsWriter:(GRXWriter *)requestWriter eventHandler:(void(^)(BOOL done, AudioFragmentResponse *_Nullable response, NSError *_Nullable error))eventHandler{
  return [self RPCToMethod:@"send"
            requestsWriter:requestWriter
             responseClass:[AudioFragmentResponse class]
        responsesWriteable:[GRXWriteable writeableWithEventHandler:eventHandler]];
}
- (GRPCStreamingProtoCall *)sendWithResponseHandler:(id<GRPCProtoResponseHandler>)handler callOptions:(GRPCCallOptions *_Nullable)callOptions {
  return [self RPCToMethod:@"send"
           responseHandler:handler
               callOptions:callOptions
             responseClass:[AudioFragmentResponse class]];
}

@end
#endif
