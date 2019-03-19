#include "audio_streaming_client.h"
#include "audio_streaming_client_wrapper.h"

using namespace com::baidu::acu::pie;

extern "C" {

AsrClientWrapper* asr_client_create() {
    return new AsrClient();
}
int asr_client_init(AsrClientWrapper* asr_client, char* address) {
    return ((AsrClient*) asr_client)->init(std::string(address));
}
void asr_client_destroy(AsrClientWrapper* asr_client) {
    delete (AsrClient*) asr_client;
}

void asr_client_set_product_id(AsrClientWrapper* asr_client, char* product_id) {
    return ((AsrClient*) asr_client)->set_product_id(std::string(product_id));
}

void asr_client_set_flush_data(AsrClientWrapper* asr_client, bool enable_flush) {
    return ((AsrClient*) asr_client)->set_enable_flush_data(enable_flush);
}


AsrStreamWrapper* asr_client_new_stream(AsrClientWrapper* asr_client) {
    return ((AsrClient*) asr_client)->get_stream();
}

int asr_client_destroy_stream(AsrClientWrapper* asr_client, AsrStreamWrapper* stream_wrapper) {
    return ((AsrClient*) asr_client)->destroy_stream((AsrStream*) stream_wrapper);
}

typedef struct {
    void * callback;
    void * data;
} CallbackAdaptor;

void callback_wrapper(const AudioFragmentResponse &resp, void* data) {
    CallbackAdaptor* adaptor = (CallbackAdaptor*)data;
    AsrStreamCallBackWrapper callBackWrapper = (AsrStreamCallBackWrapper)(adaptor->callback);
    AudioFragmentResponseWrapper responseWrapper;
    responseWrapper.error_code = resp.error_code();
    responseWrapper.error_message = resp.error_message().data();
    responseWrapper.serial_num = resp.serial_num().data();
    responseWrapper.start_time = resp.start_time().data();
    responseWrapper.end_time = resp.end_time().data();
    responseWrapper.result = resp.result().data();
    responseWrapper.complete = resp.completed();
    callBackWrapper(&responseWrapper, adaptor->data);
}

int asr_stream_read(AsrStreamWrapper* stream_wrapper, AsrStreamCallBackWrapper callback, void* data) {
    CallbackAdaptor adaptor;
    adaptor.callback = (void *)callback;
    adaptor.data = data;
    return ((AsrStream*) stream_wrapper)->read(callback_wrapper, &adaptor);
}

int asr_stream_write(AsrStreamWrapper* stream_wrapper, const void* buffer, int size, bool is_last) {
    return ((AsrStream*) stream_wrapper)->write(buffer, size, is_last);
}
}