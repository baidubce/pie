// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.client;

import java.nio.file.Path;
import java.util.List;

import com.baidu.acu.pie.model.RecognitionResult;

/**
 * AsrClient
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
public interface AsrClient {
    List<RecognitionResult> recognizeAudioFile(Path audioFilePath);

    void shutdown();
}
