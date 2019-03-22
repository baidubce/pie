using Grpc.Core;
using System;
using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;
using static Com.Baidu.Acu.Pie.AsrService;
using Google.Protobuf;

namespace Com.Baidu.Acu.Pie
{
    public class AsrStream
    {
        private readonly AsyncDuplexStreamingCall<AudioFragmentRequest, AudioFragmentResponse> stream;
        internal AsrStream(AsyncDuplexStreamingCall<AudioFragmentRequest, AudioFragmentResponse> stream)
        {
            this.stream = stream;
        }

        public Task Write(byte[] audioData)
        {
            return Write(audioData, 0, audioData.Length);
        }

        public Task Write(byte[] audioData, int offset, int count)
        {
            AudioFragmentRequest request = new AudioFragmentRequest();
            request.AudioData = Google.Protobuf.ByteString.CopyFrom(audioData, offset, count);
            return stream.RequestStream.WriteAsync(request);
        }

        public Task WriteComplete()
        {
            return stream.RequestStream.CompleteAsync();
        }

        public Task<bool> MoveNext()
        {
            return stream.ResponseStream.MoveNext();
        }

        public AudioFragmentResponse Current()
        {
            return stream.ResponseStream.Current;
        }
    }
    public class AsrClient
    {
        private Channel channel;
        public bool Flush { get; set; }
        public string ProductId { get; }
        public string AppName { get; set; }
        public double SendPerSeconds { get; set; }
        public double SleepRatio { get; set; }
        public uint LogLevel { get; set; }
        public int RecommendPacketSize
        {
            get
            {
                return (int)(SendPerSeconds * bitrate * 2);
            }
        }

        private readonly int bitrate;
        private AsrServiceClient client;
        public AsrClient(string serverAddress, string productId)
        {
            switch (productId)
            {
                case "1903":
                case "1904":
                case "1905":
                case "1906":
                case "1907":
                    bitrate = 8000;
                    break;
                case "888":
                case "1888":
                    bitrate = 16000;
                    break;
                default:
                    throw new Exception("Unrecoginzed product id");
            }
            this.ProductId = productId;
            this.SleepRatio = 1;
            this.SendPerSeconds = 0.02;
            this.LogLevel = 4;
            this.AppName = "csharp_sdk";
            this.Flush = true;
            channel = new Channel(serverAddress, ChannelCredentials.Insecure);
            client = new AsrServiceClient(channel);
        }

        ~AsrClient()
        {
            // do not wait
            channel.ShutdownAsync();
        }

        public AsrStream NewStream()
        {

            InitRequest initRequest = new InitRequest();
            initRequest.ProductId = this.ProductId;
            initRequest.AppName = this.AppName;
            initRequest.EnableChunk = true;
            initRequest.EnableFlushData = this.Flush;
            initRequest.EnableLongSpeech = true;
            initRequest.LogLevel = this.LogLevel;
            initRequest.SleepRatio = this.SleepRatio;
            initRequest.SendPerSeconds = this.SendPerSeconds;
            initRequest.SamplePointBytes = 2;
            Metadata meta = new Metadata();
            meta.Add("audio_meta", initRequest.ToByteString().ToBase64());
            var stream = client.send(meta);
            return new AsrStream(stream);
        }
    }
}
