package com.rrtoyewx.mediaexactorandmediamuxer;

import android.media.MediaMuxer;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * @author Harrison 唐广森
 * @description: 音频，和视频混合
 * @date :2019/4/22 16:22
 */
public class VideoAudioCombine {
    private String combinePath;

    private String videoPath;

    private String audioPath;

    private MediaMuxer mediaMuxer;

    private VideoThread videoThread;

    private AudioThread audioThread;
    private boolean isAudioEnd;
    private boolean isVideoEnd;


    public String getCombinePath() {
        return combinePath;
    }

    public VideoAudioCombine setCombinePath(String combinePath) {
        this.combinePath = combinePath;
        return this;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public VideoAudioCombine setVideoPath(String videoPath) {
        this.videoPath = videoPath;
        return this;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public VideoAudioCombine setAudioPath(String audioPath) {
        this.audioPath = audioPath;
        return this;
    }


    public void prepare() throws IOException {
        mediaMuxer = new MediaMuxer(combinePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        File videoFile = new File(videoPath);
        if (!videoFile.canRead()) {
            return;
        }

        File audioFile = new File(audioPath);
        if (!audioFile.canRead()) {
            return;
        }
        VideAudioCombineListener videAudioCombineListener = new VideAudioCombineListener() {
            @Override
            public void combineVideoFinished() {
                Log.e("Harrison", "combineVideoEnd");
                isVideoEnd = true;
                combineEnd();
            }

            @Override
            public void combineAudioFinished() {
                Log.e("Harrison", "combineAudioEnd");
                isAudioEnd = true;
                combineEnd();

            }
        };
        videoThread = new VideoThread();
        videoThread.setMediaMuxer(mediaMuxer).setVideoPath(videoPath).setCombineListener(videAudioCombineListener);
        videoThread.prepare();


        audioThread = new AudioThread();
        audioThread.setMediaMuxer(mediaMuxer).setAudioPath(audioPath)
                .setCombineListener(videAudioCombineListener);
        audioThread.prepare();
        mediaMuxer.start();
    }

    private void combineEnd() {
        if (isAudioEnd && isVideoEnd) {
            mediaMuxer.stop();
            mediaMuxer.release();
        }
    }


    public void startCombine() {
        if (videoThread != null && audioThread != null) {
            videoThread.start();
            audioThread.start();
        }
    }



    public interface VideAudioCombineListener {

        void combineVideoFinished();

        void combineAudioFinished();
    }



}
