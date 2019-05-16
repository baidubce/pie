protoc.exe -I=. --grpc_out=. --plugin=protoc-gen-grpc=.\grpc_cpp_plugin.exe audio_streaming.proto
protoc.exe -I=. --cpp_out=. audio_streaming.proto