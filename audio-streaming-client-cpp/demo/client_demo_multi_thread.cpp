#include "audio_streaming_client.h"
#include <unistd.h>
#include <iostream>
#include <mutex>
#include <sstream>
#include <thread>
#include <string>
#include "picosha2.h"

typedef com::baidu::acu::pie::AsrClient AsrClient;
typedef com::baidu::acu::pie::AsrStream AsrStream;
typedef com::baidu::acu::pie::AudioFragmentResponse AudioFragmentResponse;
typedef com::baidu::acu::pie::AudioFragmentResult AudioFragmentResult;

std::mutex recog_start_mutex;
std::mutex recog_complete_mutex;

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

void write_to_stream(AsrClient* client, AsrStream* stream, 
                     FILE* fp, int thread_num) {
    int size = client->get_send_package_size();
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
    std::cout << "Complete to write audio " << thread_num << std::endl;
}

void read_from_stream_once(AsrStream* stream, int thread_num) {
    if (stream->read(default_callback, nullptr) == 0) {
        std::cout << "[debug] read from stream once success" << std::endl;
    } else {
        std::cout << "[debug] read from stream once failed" << std::endl;
    }
}

void do_recog(AsrClient* client, std::string audio_file, int thread_num) {
    recog_start_mutex.lock();
    std::cout << "thread " << thread_num << " start..." << std::endl;
    // do not call stream->write after sending the last buffer
    AsrStream* stream = client->get_stream();
    std::cout << "Client get stream in thread " << thread_num << " success" << std::endl;
    FILE* fp = fopen(audio_file.c_str(), "rb");
    if (!fp) {
        std::cerr << "Failed to open file=" << audio_file << std::endl;
        return ;
    }
    std::cout << "Stream : Open file=" << audio_file << std::endl;
    recog_start_mutex.unlock();

    // write to stream continuously
    std::thread writer(write_to_stream, client, stream, fp, thread_num);

    // read from stream continuously
    char tmp[100] = "\0";
    sprintf(tmp,"audio %d\0",thread_num);
    std::cout << "Start to read " << tmp << std::endl;
    int read_times = 1;
    while (1) {
        if (stream->read(default_callback, tmp) != 0) {
            break;
        }
        std::cout << "[debug] read stream return "
                  << read_times++ << "times" << std::endl;
    }

    std::cout << "Complete to read " << tmp << std::endl;

    if (writer.joinable()) {
        writer.join();
    }

    recog_complete_mutex.lock();
    std::cout << "thread " << thread_num << " complete" << std::endl;

    if (client->destroy_stream(stream) != 0) {
        std::cerr << "[error] client destroy stream failed" << std::endl;
    } else {
        std::cout << "client destroy stream success" << std::endl;
    }
    recog_complete_mutex.unlock();
    return ;
}

int main(int argc, char* argv[]) {
    size_t thread_count = 2;
    
    // specify audio path
    std::string audio_file[2];
    audio_file[0] = "../../data/10s.wav";
    audio_file[1] = "../../data/8k-0.pcm";
    
    // create AsrClient
    AsrClient client;
    client.set_enable_flush_data(true);
    if (argc == 7) {
        client.set_product_id(argv[1]);
        client.init(argv[2], std::atoi(argv[6]));
        client.set_user_name(argv[3]);
        std::string passwd = argv[4];
        std::string str = argv[3] + passwd + argv[5];
        std::string token = picosha2::hash256_hex_string(str);
        client.set_token(token);
        client.set_expire_time(argv[5]); //expire_time UTC format, 2019-04-25T12:41:16Z
    } else {
        client.set_product_id("1906");
        client.init("127.0.0.1:8200", 0);
    }
   
    // create threads
    std::thread recog_threads[thread_count];
    for (int i = 0; i < thread_count; i++) {
        recog_threads[i] = std::thread(do_recog, &client, audio_file[i], i);
    }

    // wait threads complete
    for (int i = 0; i < thread_count; i++) {
        if (recog_threads[i].joinable()) {
            recog_threads[i].join();
        }
    }

    return 0;
}

