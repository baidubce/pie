#include "audio_streaming_client.h"
#include <fstream>
#include <thread>
#include <sstream>
#include <grpcpp/channel.h>
#include <grpcpp/create_channel.h>
#include <grpcpp/client_context.h>
#include <grpcpp/impl/codegen/sync_stream.h>
#include "base64.h"

DEFINE_string(default_app_name, "my_sdk", "SKD's name");
DEFINE_bool(default_enable_long_speech, true, "enable long speech");
DEFINE_bool(default_enable_chunk, true, "enable chunk");
DEFINE_uint32(default_log_level, 5, "log level");
DEFINE_double(default_send_per_second, 0.16, "send per second");
DEFINE_double(default_sleep_ratio, 1, "sleep ratio");
DEFINE_int32(default_timeout, 100, "timeout");


namespace com {
namespace baidu {
namespace acu {
namespace pie {

AsrClient::AsrClient()
	: _set_enable_flush_data(false)
	, _set_product_id(false)
	, _inited(false) 
	, _created_stream(false) {
    _init_request.set_app_name(FLAGS_default_app_name);
    _init_request.set_enable_long_speech(FLAGS_default_enable_long_speech);
    _init_request.set_enable_chunk(FLAGS_default_enable_chunk);
    _init_request.set_log_level(FLAGS_default_log_level);
    _init_request.set_send_per_seconds(FLAGS_default_send_per_second);
    _init_request.set_sleep_ratio(FLAGS_default_sleep_ratio);
}

void AsrClient::set_app_name(const std::string& app_name) {
	_init_request.set_app_name(app_name);
}

void AsrClient::set_enable_flush_data(bool enable_flush_data) {
	_init_request.set_enable_flush_data(enable_flush_data);
	_set_enable_flush_data = true;
}

void AsrClient::set_enable_long_speech(bool enable_long_speech){
	_init_request.set_enable_long_speech(enable_long_speech);
}

void AsrClient::set_enable_chunk(bool enable_chunk) {
	_init_request.set_enable_chunk(enable_chunk);
}

void AsrClient::set_log_level(int log_level) {
	_init_request.set_log_level(log_level);
}

void AsrClient::set_send_per_seconds(double send_per_seconds) {
	_init_request.set_send_per_seconds(send_per_seconds);
}

void AsrClient::set_sleep_ratio(double sleep_ratio) {
	_init_request.set_sleep_ratio(sleep_ratio);
}

void AsrClient::set_product_id(const std::string& product_id) {
	_init_request.set_product_id(product_id);
	_set_product_id = true;
}

int AsrClient::init(const std::string& address) {
	if (!_set_product_id || !_set_enable_flush_data) {
		std::cerr << "Missing required field `product_id` or `enable_fush_data`" << std::endl;
		return -1;
	}
    _context.set_deadline(std::chrono::system_clock::now() + std::chrono::seconds(FLAGS_default_timeout));
    _context.AddMetadata("audio_meta", base64_encode(_init_request.SerializeAsString()));

    _channel = grpc::CreateChannel(address, grpc::InsecureChannelCredentials());
    _inited = true;
    return 0;
}

int AsrClient::send_audio(const std::string &audio_file, AsrClientCallBack callback_fun, void* data) {
    if (!_inited) {
	std::cerr << "Client hasn't been inited yet before send audio" << std::endl;
	return -1;
    }
    std::unique_ptr<AsrService::Stub> stub = AsrService::NewStub(_channel);
    if (!stub) {
        std::cerr << "Fail to init stub for AsrService before send audio" << std::endl;
        return -1;
    }
    std::shared_ptr<grpc::ClientReaderWriter<AudioFragmentRequest, AudioFragmentResponse> > stream
            = stub->send(&_context);
    if (!stream) {
        std::cerr << "Fail to init stream for AsrService before send audio" << std::endl;
        return -1;
    }
    FILE* fp = fopen(audio_file.c_str(), "rb");
    if (!fp) {
	std::cerr << "Failed to open file=" << audio_file << std::endl;
	return -1;
    }
    std::cout << "Open file=" << audio_file << std::endl;

    std::thread writer([stream, fp]() {
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
	std::cout << "[debug] run callback_fun ... ..." << std::endl;
	callback_fun(resp, data);
    }
    std::cout << "Read done" << std::endl;
    writer.join();
	grpc::Status status = stream->Finish();
    if (!status.ok()) {
        std::cerr << "Fail to finish streaming with asr streaming server" << std::endl;
	return -1;
    }
    std::cout << "Finished streaming with asr streaming server" << std::endl;
    return 0;
}

int AsrClient::write_stream(const void* buffer, size_t size, bool last_stream) {
    if (!_inited) {
        std::cerr << "Client hasn't been inited yet before write stream" << std::endl;
        return -1;
    }
    if (!_created_stream) {
        std::cerr << "Stream hasn't been created yet before write stream" << std::endl;
        return -1;
    }

    if (size < 0) {
        std::cerr << "size < 0 in  write stream" << std::endl;
        return -1;
    }
    com::baidu::acu::pie::AudioFragmentRequest request;
    request.set_audio_data(buffer, size);
    std::cout << "[debug] will run _stream->Write(request) ... ..." << std::endl;
    _stream->Write(request);
    if (last_stream) {
        _stream->WritesDone();
        std::cout << "Write done" << std::endl;
    }
    return 0;
}

int AsrClient::read_stream(AsrClientCallBack callback_fun, void* data) {
    if (!_inited) {
        std::cerr << "Client hasn't been inited yet before read stream" << std::endl;
        return -1;
    }
    if (!_created_stream) {
        std::cerr << "Stream hasn't been created yet before read stream" << std::endl;
        return -1;
    }
    AudioFragmentResponse response;
    std::cout << "[debug] will run _stream->Read(&response) ... ..." << std::endl;
    if (_stream->Read(&response)) {
        std::cout << "[debug] run callback_fun ... ..." << std::endl;
	callback_fun(response, data);
        return 1;
    }
    return 0;
}

int AsrClient::create_stream() {
    if (!_inited) {
	std::cerr << "Client hasn't been inited yet before create stream" << std::endl;
	return -1;
    }
    if (_created_stream) {
        std::cerr << "Stream has already been created when creating stream" << std::endl;
        return -1;
    }
    _stub = AsrService::NewStub(_channel);
    if (!_stub) {
        std::cerr << "Fail to init stub for AsrService before create stream" << std::endl;
        return -1;
    }
    _stream  = _stub->send(&_context);
    if (!_stream) {
        std::cerr << "Fail to init stub for AsrService before create stream" << std::endl;
        return -1;
    }
    _created_stream = true;
    return 0;
}

int AsrClient::finish_stream() {
    if (!_inited) {
        std::cerr << "Client hasn't been inited yet before finish stream" << std::endl;
        return -1;
    }
    if (!_created_stream) {
        std::cerr << "Stream hasn't been created yet before finish stream" << std::endl;
        return -1;
    }
    grpc::Status status = _stream->Finish();
    if (!status.ok()) {
        std::cerr << "Fail to finish streaming with asr streaming server" << std::endl;
		return -1;
    }
    std::cout << "Finished streaming with asr streaming server" << std::endl;
    _created_stream = false;
    return 0;
}

} // namespace pie
} // namespace acu
} // namespace baidu
} // namespace com
