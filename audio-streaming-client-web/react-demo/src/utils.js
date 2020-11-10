/**
 * @file utils
 * @author sunjiahao
 */

// 获取设备浏览器兼容
export function promiseOldBrowser(constraints, successCallback, errorCallback) {
    const getUserMedia = navigator.getUserMedia
    || navigator.webkitGetUserMedia
    || navigator.mozGetUserMedia
    || navigator.msGetUserMedia;
    if (!getUserMedia) {
        return Promise.reject(
            new Error('getUserMedia is not implemented in this browser')
        );
    }
    return new Promise((successCallback, errorCallback) => {
        getUserMedia.call(
            navigator,
            constraints,
            successCallback,
            errorCallback
        );
    });
};

export function sampleData(pcmDatas, pcmSampleRate, newSampleRate, prevChunkInfo = {}) {
    try {
        let index = prevChunkInfo ? prevChunkInfo.index : 0;
        let offset = prevChunkInfo ? prevChunkInfo.offset : 0;
        let size = 0;

        for (let i = index; i < pcmDatas.length; i++) {
            size += pcmDatas[i].length;
        }

        size = Math.max(0, size - Math.floor(offset));
        let step = pcmSampleRate / newSampleRate;
        // 新采样高于录音采样不处理，省去了插值处理，直接抽样
        if (step > 1) {
            size = Math.floor(size / step);
        }
        else {
            step = 1;
            newSampleRate = pcmSampleRate;
        }

        // 准备数据
        let res = new Int16Array(size);
        let idx = 0;
        for (let nl = pcmDatas.length; index < nl; index++) {
            let o = pcmDatas[index];
            let i = offset;
            let il = o.length;
            while (i < il) {
                let before = Math.floor(i);
                let after = Math.ceil(i);
                let atPoint = i - before;
                res[idx] = o[before] + (o[after] - o[before]) * atPoint;
                idx++;
                // 抽样
                i += step;
            }
            offset = i - il;
        }

        return {
            index,
            offset,
            sampleRate: newSampleRate,
            data: res
        };
    }
    catch (error) {
        throw new Error('转音错误了！', error);
    }
};

export function convertBuffer(arrayBuffer) {
    const data = new Float32Array(arrayBuffer);
    const out = new Int16Array(arrayBuffer.length);
    // floatTo16BitPCM
    for (let i = 0; i < data.length; i++) {
        const s = Math.max(-1, Math.min(1, data[i]));
        out[i] = (s < 0 ? s * 0x8000 : s * 0x7FFF);
    }
    return out;
};

// 合并pcm数据
export function encodedPcmBuffer(channelBuffer, sampleRate) {
    const float32array = channelBuffer;
    const float32arrayBuffer = convertBuffer(float32array);
    const Samp = sampleData([float32arrayBuffer], sampleRate, 16000, null);
    return Samp.data;
};
