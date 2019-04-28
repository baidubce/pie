#include "audio_streaming_client.h"
#include <unistd.h>
#include <iostream>
#include <sstream>
#include <thread>
#include <string>
#include "picosha2.h"

typedef com::baidu::acu::pie::AsrClient AsrClient;
typedef com::baidu::acu::pie::AsrStream AsrStream;
typedef com::baidu::acu::pie::AudioFragmentResponse AudioFragmentResponse;
typedef com::baidu::acu::pie::AudioFragmentResult AudioFragmentResult;

void default_callback(AudioFragmentResponse& resp, 
                      void* data) {
    //std::cout << "[debug] do recoging ... ..." << std::endl;
    std::stringstream ss;
    if (data) {
        char* tmp = (char*) data;
        std::cout << "data = " << tmp << std::endl;
    }
    if (resp.type() == com::baidu::acu::pie::FRAGMENT_DATA) {
        AudioFragmentResult *audio_fragment = resp.mutable_audio_fragment();
        ss << "Receive " << (audio_fragment->completed() ? "completed" : "uncompleted")
           << ", serial_num=" << audio_fragment->serial_num()
           << ", start=" << audio_fragment->start_time()
           << ", end=" << audio_fragment->end_time()
           << ", error_code=" << resp.error_code()
           << ", error_message=" << resp.error_message()
           << ", content=" << audio_fragment->result();
        std::cout << ss.str() << std::endl;
    } else {
        std::cout << "error resp type is=" << resp.type() << std::endl;
    }
}

void write_to_stream(AsrClient client, AsrStream* stream, 
                     FILE* fp, int i) {
    int size = client.get_send_package_size();
    char buffer[size];
    size_t count = 0;
    while (!std::feof(fp)) {
        count = fread(buffer, 1, size, fp);
        //std::cout << "[debug] write stream " << std::endl;
        if (stream->write(buffer, count, false) != 0) {
            std::cerr << "[error] stream write buffer error" << std::endl;
            break;
        }
        if (count < 0) {
            std::cerr << "[warning] count < 0 !!!!!!!!" << std::endl;
            break;
        }
        // you can add usleep to simulate audio pause
        //usleep(150*1000);
    }
    //std::cout << "[debug] write stream " << std::endl;
    stream->write(nullptr, 0, true);
    std::cout << "[debug] write last buffer to stream" << std::endl;
    std::cout << "Complete to write audio " << i << std::endl;
}

void read_from_stream_once(AsrStream* stream) {
    if (stream->read(default_callback, nullptr) == 0) {
        std::cout << "[debug] read from stream once success" << std::endl;
    } else {
        std::cout << "[debug] read from stream once failed" << std::endl;
    }
}

int main(int argc, char* argv[]) {
    size_t case_count = 2;

    // specify audio path
    std::string audio_file[2];
    audio_file[0] = "../../data/10s.wav";
    audio_file[1] = "../../data/8k-0.pcm";

    // create AsrClient
    AsrClient client;
    client.set_enable_flush_data(true);
    if (argc == 6) {
        client.set_product_id(argv[1]);
        client.init(argv[2]);
        client.set_user_name(argv[3]);
        std::string passwd = argv[4];
        std::string str = argv[3] + passwd + argv[5];
        std::string token = picosha2::hash256_hex_string(str);
        client.set_token(token);
        client.set_expire_time(argv[5]); //expire_time UTC format, 2019-04-25T12:41:16Z
    } else {
        client.set_product_id("1906");
        client.init("127.0.0.1:8200");
    }
    
    // start to write and read one by one
    for (size_t i = 0; i < case_count; i++) {
        std::cout << "case " << i << " start..." << std::endl;
        // do not call stream->write after sending the last buffer
        AsrStream* stream = client.get_stream();
        std::cout << "client get stream success" << std::endl;

        FILE* fp = fopen(audio_file[i].c_str(), "rb");
        if (!fp) {
            std::cerr << "Failed to open file=" << audio_file[i] << std::endl;
            return -1;
        }
        std::cout << "Stream : Open file=" << audio_file[i] << std::endl;

        // write to stream continuously
        std::thread writer(write_to_stream, client, stream, fp, i);

        // read from stream continuously
        char tmp[100] = "\0";
        sprintf(tmp,"audio %d\0",i);
        std::cout << "Start to read " << tmp << std::endl;
        int read_num = 1;
        while (1) {
            if (stream->read(default_callback, tmp) != 0) {
                break;
            }
            std::cout << "[debug] read stream return " 
                      << read_num++ << "times" << std::endl;
        }
        std::cout << "Complete to read " << tmp << std::endl;
        
        if (writer.joinable()) {
            writer.join();
        }
        std::cout << "case " << i << " complete" << std::endl;
       
        if (client.destroy_stream(stream) != 0) {
            std::cerr << "[error] client destroy stream failed" << std::endl;
        } else {
            std::cout << "client destroy stream success" << std::endl;
        }
    }   
    return 0;
}

