/**
 * @file utils
 * @author sunjiahao
 */

import {encodedPcmBuffer, promiseOldBrowser} from './utils';

import './App.css';

// 控制通道，如下分别为缓冲区大小，输入通道，输出通道
const BUFFER_SIZE = 512;
const INPUT_CHANNELS = 1;
const OUTPUT_CHANNELS = 1;
// 位深默认16
const SAMPLE_SIZE = 16;
// 采样率默认16k
const SAMPLE_RATE = 16 * 1000;

const UNIT = 512;

const INTERVAL = 20;
const WS_URL = 'ws://1.1.1.1:80/api/v1/asr/stream';
const DEFAULT_PARAMS = JSON.stringify({
    "enableFlushData": true,
    "productId": "1912",
    "samplePointBytes": 1,
    "sendPerSeconds": INTERVAL / 1000,
    "sleepRatio": 0,
    "appName": "webproxy",
    "userName": "youruserName",
    "password": "yourpassword"
});

function App() {
    let websocket;
    const outputMessageArray = [];

    if (navigator.mediaDevices === undefined) {
        navigator.mediaDevices = {};
    }
    if (navigator.mediaDevices.getUserMedia === undefined) {
        navigator.mediaDevices.getUserMedia = promiseOldBrowser;
    }

    function startWebSocket(way, afterOpen) {
        websocket && websocket.close();
        websocket = new WebSocket(WS_URL);
        websocket.onopen = function(evt) {
            onOpen(evt, way, afterOpen);
        };
        websocket.onclose = function(evt) {
            onClose(evt);
        };
        websocket.onmessage = function(evt) {
            onMessage(evt);
        }; 
        websocket.onerror = function(evt) {
            onError(evt);
        };
    }

    function onOpen(evt, way, afterOpen) {
        console.log('websocket连接成功');
        doSend(DEFAULT_PARAMS);
        way === 'upload' && afterOpen();
    }

    function onClose(evt) {
        console.log('websocket连接关闭');
    }

    function onMessage(evt) {
        try {
            const data = JSON.parse(evt.data);
            const {serialNum} = data;
            if (outputMessageArray.length === 0) {
                outputMessageArray.push(data);
            }
            else {
                const {serialNum: lastSerialNum, completed: lastCompleted} = outputMessageArray[outputMessageArray.length - 1];
                if (serialNum === lastSerialNum && !lastCompleted) {
                    outputMessageArray[outputMessageArray.length - 1] = data;
                }
                else {
                    outputMessageArray.push(data);
                }
            }
            // 打印识别结果调试
            // console.log(outputMessageArray);
            document.getElementById("output").innerHTML = `<h4>识别结果：${outputMessageArray.map(msg => msg.result).join('')}</h4>`;
        }
        catch (error) {
            throw new Error('解析错误!' + error);
        }
    }

    function onError(evt) {
        throw new Error('websocket连接失败'+ evt.data);
    }

    function doSend(message) {
        websocket.readyState === 1 && websocket.send(message);
    }

    function handleAudioprocess(stream) {
        const {inputBuffer} = stream;
        const encodeBuffer = encodedPcmBuffer(inputBuffer.getChannelData(0), inputBuffer.sampleRate);
        // 过滤连续绝对静音的脏数据
        if (encodeBuffer.some(value => value !== 0)) {
            doSend(encodeBuffer);
        }
    }

    function initAudio(stream) {
        const AudioContext = (window.AudioContext || window.webkitAudioContext);
        const audioContent = new AudioContext({
            // 由于AudioContext一旦创建采样率不能更改，在创建时设置采样率
            sampleRate: SAMPLE_RATE
        });
        // 创建音频源
        const streamSource = audioContent.createMediaStreamSource(stream);
        // 创建脚本处理器，用于支持音频流采样设置及格式输出
        const scriptProcessor = audioContent.createScriptProcessor(BUFFER_SIZE, INPUT_CHANNELS, OUTPUT_CHANNELS);
        // 连接到AudioNode的输出之一，这里连接到脚本处理器
        streamSource.connect(scriptProcessor);
        scriptProcessor.connect(audioContent.destination);
        // 监听麦克风音频流输出
        scriptProcessor.onaudioprocess = handleAudioprocess;
    }

    function startRecorder() {
        navigator.mediaDevices.getUserMedia({
            video: false,
            audio: {
                sampleRate: SAMPLE_RATE,
                sampleSize: SAMPLE_SIZE,
                channelCount: INPUT_CHANNELS
            }
        }).then(stream => {
            startWebSocket();
            initAudio(stream);
        }).catch(error => {
            let errorMessage;
            switch (error.name) {
                // 用户拒绝
                case 'NotAllowedError':
                case 'PermissionDeniedError':
                    errorMessage = '用户已禁止网页调用录音设备';
                    break;
                // 未接入录音设备
                case 'NotFoundError':
                case 'DevicesNotFoundError':
                    errorMessage = '录音设备未找到';
                    break;
                // 其它错误
                case 'NotSupportedError':
                    errorMessage = '不支持录音功能';
                    break;
                default:
                    errorMessage = '录音调用错误';
            }
            throw new Error(errorMessage);
        });
    }

    function handleFileChange(event) {
        const newFile = event.target.files[0];
        //读取为ArrayBuffer
        const reader = new FileReader();
        reader.readAsArrayBuffer(newFile);
        //显示进度
        const progress = document.getElementById('progress');
        progress.max = newFile.size;
        progress.value = 0;
        reader.onprogress = function (e) {
            progress.value = e.loaded;
        }
        reader.onload = function () {
            const byteArray = new Uint8Array(reader.result);
            const newLength = byteArray.byteLength / UNIT;
            const afterOpen = async () => {
                // 使用setTimeout而不使用setInterval提高性能
                const sleep = interval => new Promise(resolve => setTimeout(resolve, interval));
                for (let i = 0; i < newLength; i++) {
                    const newArray = (i === Math.floor(newLength) && Math.floor(newLength) !== newLength)
                        ? new Uint8Array(reader.result, UNIT * i, byteArray.byteLength % UNIT)
                        : new Uint8Array(reader.result, UNIT * i, UNIT);
                    await sleep(INTERVAL);
                    doSend(newArray);
                }
            };
            startWebSocket('upload', afterOpen);
        }
    }
  return (
    <div className="App">
      <h1>JS Demo</h1>
      <h3>实时语音识别：</h3>
      <button onClick={startRecorder}>进行语音识别</button>
      <h3>上传音频文件识别：</h3>
      <input type="file" id="file" name="upload" onChange={handleFileChange} />
      上传进度：
      <progress id="progress" value="0"></progress>
      <div id="output" style={{marginTop: '40px'}}></div>
    </div>
  );
}

export default App;
