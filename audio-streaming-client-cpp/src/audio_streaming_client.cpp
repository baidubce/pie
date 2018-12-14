#include "audio_streaming_client.h"
#include <fstream>
#include <thread>
#include <sstream>
#include <grpcpp/channel.h>
#include <grpcpp/create_channel.h>
#include <grpcpp/client_context.h>
#include <grpcpp/impl/codegen/sync_stream.h>
#include "base64.h"

DEFINE_string(app_name, "my_sdk", "SKD's name");
DEFINE_bool(enable_flush_data, true, "enable flush data");
DEFINE_bool(enable_long_speech, true, "enable long speech");
DEFINE_bool(enable_chunk, true, "enable chunk");
DEFINE_uint32(log_level, 5, "log level");
DEFINE_double(send_per_second, 0.16, "send per second");
DEFINE_double(sleep_ratio, 1, "sleep ratio");
DEFINE_string(product_id, "1903", "product id");
DEFINE_int32(timeout, 100, "timeout");

namespace com {
namespace baidu {
namespace acu {
namespace pie {

using grpc::Server;
using grpc::ServerBuilder;
using grpc::ServerContext;
using grpc::ServerReader;
using grpc::ServerReaderWriter;
using grpc::ServerWriter;
using grpc::Status;
using std::chrono::system_clock;

namespace {

void default_callback(const AudioFragmentResponse& resp, void*) {
	std::stringstream ss;
	ss << "Receive completed" << resp.completed()
	   << ", start=" << resp.start_time()
	   << ", end=" << resp.end_time()
	   << ", content=" << resp.result();
	std::cout << ss.str() << std::endl;
}

} // anynmous namespace

int AsrClient::init(const std::string& address) {
    InitRequest init_request;
    init_request.set_app_name(FLAGS_app_name);
    init_request.set_enable_flush_data(FLAGS_enable_flush_data);
    init_request.set_enable_long_speech(FLAGS_enable_long_speech);
    init_request.set_enable_chunk(FLAGS_enable_chunk);
    init_request.set_log_level(FLAGS_log_level);
    init_request.set_send_per_seconds(FLAGS_send_per_second);
    init_request.set_sleep_ratio(FLAGS_sleep_ratio);
    init_request.set_product_id(FLAGS_product_id);

    _stub = AsrService::NewStub(grpc::CreateChannel(address, grpc::InsecureChannelCredentials()));
    if (!_stub) {
        std::cerr << "Fail to init stub for AsrService, server_address=" << address << std::endl;
        return -1;
    }

    _context.set_deadline(std::chrono::system_clock::now() + std::chrono::seconds(FLAGS_timeout));
    _context.AddMetadata("audio_meta", base64_encode(init_request.SerializeAsString()));

    _callback_fun = default_callback;
    return 0;
}

void AsrClient::set_response_callback(AsrClientCallBack callback_fun) {
    _callback_fun = callback_fun;
}

int AsrClient::send_audio(const std::string &audio_file) {
    std::shared_ptr<grpc::ClientReaderWriter<AudioFragmentRequest, AudioFragmentResponse> > stream
            = _stub->send(&_context);
    if (!stream) {
        std::cerr << "Fail to init stub for AsrService" << std::endl;
        return -1;
    }
    std::thread writer([stream, audio_file]() {
        FILE* fp = fopen(audio_file.c_str(), "rb");
        if (!fp) {
            std::cerr << "Failed to open file=" << audio_file << std::endl;
        } else {
            std::cout << "Open file=" << audio_file << std::endl;
        }
        int size = 2560;
        char buffer[size];
        while (!std::feof(fp)) {
            auto count = fread(buffer, 1, size, fp);
            if (count > 0) {
                com::baidu::acu::pie::AudioFragmentRequest request;
                request.set_audio_data(buffer, count);
                stream->Write(request);
            } else {
                break;
            }
        }
        stream->WritesDone();
        std::cout << "Write done" << std::endl;
    });
    AudioFragmentResponse resp;
    while (stream->Read(&resp)) {
        // You can send custom data container to callback fun.
        _callback_fun(resp, nullptr);
    }
    writer.join();
    Status status = stream->Finish();
    if (!status.ok()) {
        std::cerr << "Fail to streaming with asr streaming server" << std::endl;
    } else {
        std::cout << "Finished streaming with asr streaming server" << std::endl;
    }
    return 0;
}

} // namespace pie
} // namespace acu
} // namespace baidu
} // namespace com
