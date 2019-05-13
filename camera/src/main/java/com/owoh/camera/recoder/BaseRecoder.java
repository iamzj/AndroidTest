package com.owoh.camera.recoder;

/**
 * Author : 唐家森
 * Version: 1.0
 * On     : 2017/11/6 17:06
 * Des    :抽取视频录制和音频录制的步骤
 */

public interface BaseRecoder {
    void initRecoder();

    void encodeData(byte[] data);

    void stopRecoder();

    void releseRecoder();

    void startRecoder();

     interface OnRecoderListener {
        void onStarRecoder();

        void onStopRecoder();

        void onFinishRecoder();

        void onRecoderError(int code, String msg);
    }

}
