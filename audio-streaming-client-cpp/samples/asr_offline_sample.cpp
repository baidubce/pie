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
        client.set_product_id("1906");
        client.init("180.76.107.131:8200");
	
	// Stream test
	FILE* fp = fopen(audio_file.c_str(), "rb");
	if (!fp) {
		std::cerr << "Failed to open file=" << audio_file << std::endl;
		return -1;
	}
	std::cout << "Stream : Open file=" << audio_file << std::endl;
      
	// do not call stream->write after sending the last buffer 
	size_t case_num = 1;
        com::baidu::acu::pie::AsrStream* stream = client.get_stream();
	std::thread writer([stream, fp, case_num](){
	    int size = 2560;
            char buffer[size];
	    size_t count = 0;
	    for (size_t i = 0; i < case_num; i++) {
	        while (!std::feof(fp)) {
                    count = fread(buffer, 1, size, fp);
	            //std::cout << "[debug] write stream " << std::endl;
                    if (!stream->write(buffer, count, false)) {
		        std::cerr << "[error] stream write buffer error" << std::endl;
		        break;
		    }
	            if (count < 0) {    
                        std::cerr << "[warning] count < 0 !!!!!!!!" << std::endl;
	        	break;
	            }
	            usleep(150*1000);
                }
	        //std::cout << "[debug] write stream " << std::endl;
		stream->write(nullptr, 0, true);
	        std::cout << "[debug] count of last buffer=" << count << std::endl;
	        rewind(fp);
	    }
	});

	for (size_t i = 0; i < case_num; i++) {
	    char tmp[100] = "\0";
	    sprintf(tmp,"case %d.\0",i+1);
	    std::cout << "Start to read " << tmp << std::endl;
    	    while (stream->read(default_callback, tmp)) {
    	        //std::cout << "[debug] read stream" << std::endl;
    	        //usleep(150*1000);
    	    }
	    std::cout << "Complete to read " << tmp << std::endl;
        }
	writer.join();
        client.destroy_stream(stream);
        return 0;
}

