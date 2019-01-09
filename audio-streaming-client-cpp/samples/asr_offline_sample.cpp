#include "audio_streaming_client.h"
#include <unistd.h>
#include <iostream>
#include <sstream>
#include <thread>

void default_callback(const com::baidu::acu::pie::AudioFragmentResponse& resp, void* data) {
        //std::cout << "do recoging ... ..." << std::endl;
	std::stringstream ss;
	if (data) {
		char* tmp = (char*) data;
		std::cout << "data = " << tmp << std::endl;
	}
	ss << "Receive completed" << resp.completed()
	   << ", start=" << resp.start_time()
	   << ", end=" << resp.end_time()
	   << ", content=" << resp.result();
	std::cout << ss.str() << std::endl;
}

int main(int argc, char* argv[]) {
	// Parse gflags
	google::ParseCommandLineFlags(&argc, &argv, true);
        std::string audio_file = "../../data/10s.wav";
        if (argc > 1) {
            audio_file = argv[1];
        }
        com::baidu::acu::pie::AsrClient client;
	client.set_enable_flush_data(true);
        client.set_product_id("1903");
        client.init("172.18.53.16:8050");
	char tmp[10] = "t\0";
	
	// File test
	//client.send_audio(audio_file, default_callback, nullptr);

	// Stream test
	FILE* fp = fopen(audio_file.c_str(), "rb");
	if (!fp) {
		std::cerr << "Failed to open file=" << audio_file << std::endl;
		return -1;
	}
	std::cout << "Stream : Open file=" << audio_file << std::endl;
       
	client.create_stream();
	std::thread writer([&client, fp](){
	    int size = 2560;
            char buffer[size];
	    size_t count=0;
	    while (!std::feof(fp)) {
                count = fread(buffer, 1, size, fp);
	        std::cout << "[debug] write stream " << std::endl;
                client.write_stream(buffer, count, false);
	        if (count < 0) {    
                    std::cout << "[debug] count < 0 !!!!!!!!" << std::endl;
	    	break;
	        }
		usleep(150*1000);
            }
	    std::cout << "[debug] write stream " << std::endl;
            client.write_stream(nullptr, 0, true);
	    std::cout << "[debug] count of last buffer=" << count << std::endl;
	});
	while (client.read_stream(default_callback, nullptr)) {
	    std::cout << "[debug] read stream" << std::endl;
	    usleep(150*1000);
	}
	writer.join();
        client.finish_stream();
        return 0;
}

