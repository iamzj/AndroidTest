package com.owohvide.testconstanlayoutset;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * desc: 播放视频的view 单个视频循环播放
 */

public class VideoPreviewView extends SurfaceView  {



    public VideoPreviewView(Context context) {
        super(context, null);
        // init(context);
    }

    public VideoPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //   init(context);
    }

//    private void init(Context context) {
//        //初始化Drawer和VideoPlayer
//        mMediaPlayer = new MediaPlayerWrapper();
//        mMediaPlayer.setOnCompletionListener(this);
//    }
//
//    /**
//     * 设置视频的播放地址
//     */
//    public void setVideoPath(List<String> paths) {
//        mMediaPlayer.setDataSource(paths);
//    }
//
//
//
//
//    public void onDestroy() {
//        if (mMediaPlayer.isPlaying()) {
//            mMediaPlayer.stop();
//        }
//        mMediaPlayer.release();
//    }


//    /**
//     * isPlaying now
//     */
//    public boolean isPlaying() {
//        return mMediaPlayer.isPlaying();
//    }
//
//    /**
//     * pause play
//     */
//    public void pause() {
//        mMediaPlayer.pause();
//    }
//
//    /**
//     * start play video
//     */
//    public void start() {
//        mMediaPlayer.start();
//    }
//
//    /**
//     * 跳转到指定的时间点，只能跳到关键帧
//     */
//    public void seekTo(int time) {
//        mMediaPlayer.seekTo(time);
//    }
//
//    /**
//     * 获取当前视频的长度
//     */
//    public int getVideoDuration() {
//        return mMediaPlayer.getCurVideoDuration();
//    }
//
//    /**
//     * 获取当前播放的视频的列表
//     */
//    public List<VideoInfo> getVideoInfo() {
//        return mMediaPlayer.getVideoInfo();
//    }


}
