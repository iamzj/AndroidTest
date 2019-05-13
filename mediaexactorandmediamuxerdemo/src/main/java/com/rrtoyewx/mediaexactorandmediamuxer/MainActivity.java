package com.rrtoyewx.mediaexactorandmediamuxer;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.rrtoyewx.mediaexactorandmediamuxer.combine.VideoAudioCombine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download";

    Button exactorBtn;
    Button muxerBtn;
    Button muxerAudioBtn;
    Button combineVideoBtn;

    MediaExtractor mediaExtractor;
    MediaMuxer mediaMuxer;
    public static Executor EXECUTOR = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        exactorBtn = (Button) findViewById(R.id.exactor);
        muxerBtn = (Button) findViewById(R.id.muxer);
        muxerAudioBtn = (Button) findViewById(R.id.muxer_audio);
        combineVideoBtn = (Button) findViewById(R.id.combine_video);

        exactorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exactorMedia();
            }
        });
        muxerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muxerMedia();
            }
        });
        muxerAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muxerAudio();
            }
        });

        combineVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoAudioCombine.getInstance()
                        .setAudioPath(SDCARD_PATH + "/test8.mp3")
                        .setVideoPath(SDCARD_PATH + "/output_video.mp4")
                        .setCombinePath(SDCARD_PATH + "/outputComibine5.mp4");
                VideoAudioCombine.getInstance().prepare().startCombine();
                VideoAudioCombine.getInstance().setVideoAudioCombineStateListener(new VideoAudioCombine.VideoAudioCombineStateListener() {
                    @Override
                    public void success(String combimePath) {
                        Log.e("Harrison", "****VideoAudioCombine combimePath " + combimePath);
                    }

                    @Override
                    public void fail() {
                        Log.e("Harrison", "****VideoAudioCombine fail ");
                    }

                    @Override
                    public void start() {
                        Log.e("Harrison", "****VideoAudioCombine fail ");
                    }
                });

//                combineVideo();


            }
        });
        mediaExtractor = new MediaExtractor();
    }

    private void exactorMedia() {
        FileOutputStream videoOutputStream = null;
        FileOutputStream audioOutputStream = null;
        try {
            File videoFile = new File(SDCARD_PATH, "output_video.mp4");
            if (!videoFile.exists()) {
                videoFile.createNewFile();
            }

            File audioFile = new File(SDCARD_PATH, "output_audio.mp3");
            videoOutputStream = new FileOutputStream(videoFile);
            audioOutputStream = new FileOutputStream(audioFile);
            mediaExtractor.setDataSource(SDCARD_PATH + "/input.mp4");
            int trackCount = mediaExtractor.getTrackCount();
            int audioTrackIndex = -1;
            int videoTrackIndex = -1;
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                Log.e("Harrison", trackFormat.toString());
                String mineType = trackFormat.getString(MediaFormat.KEY_MIME);

                if (mineType.startsWith("video/")) {
                    videoTrackIndex = i;

                }

                if (mineType.startsWith("audio/")) {
                    audioTrackIndex = i;
                }
            }

            ByteBuffer byteBuffer = ByteBuffer.allocate(512 * 1024);
            mediaExtractor.selectTrack(videoTrackIndex);
            while (true) {
                int readSampleCount = mediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleCount < 0) {
                    break;
                }

                byte[] buffer = new byte[readSampleCount];
                byteBuffer.get(buffer);
                videoOutputStream.write(buffer);
                byteBuffer.clear();
                mediaExtractor.advance();
            }

            mediaExtractor.selectTrack(audioTrackIndex);
            while (true) {
                int readSampleCount = mediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleCount < 0) {
                    break;
                }

                byte[] buffer = new byte[readSampleCount];
                byteBuffer.get(buffer);
                audioOutputStream.write(buffer);
                byteBuffer.clear();
                mediaExtractor.advance();
            }

            Log.e("Harrison", "finish");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mediaExtractor.release();
            try {
                videoOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void muxerMedia() {
        mediaExtractor = new MediaExtractor();
        int videoIndex = -1;
        try {
            mediaExtractor.setDataSource(SDCARD_PATH + "/input.mp4");
            int trackCount = mediaExtractor.getTrackCount();
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                String mimeType = trackFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("video/")) {
                    videoIndex = i;
                }
            }

            mediaExtractor.selectTrack(videoIndex);
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(videoIndex);
            mediaMuxer = new MediaMuxer(SDCARD_PATH + "/output_video.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int trackIndex = mediaMuxer.addTrack(trackFormat);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 500);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            mediaMuxer.start();
            long videoSampleTime;
            {
                mediaExtractor.readSampleData(byteBuffer, 0);
                // skip first I frame
                if (mediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                    Log.e("Harrison", "MediaExtractor.SAMPLE_FLAG_SYNC");
                    mediaExtractor.advance();
                }
                mediaExtractor.readSampleData(byteBuffer, 0);
                long firstVideoPTS = mediaExtractor.getSampleTime();
                mediaExtractor.advance();
                mediaExtractor.readSampleData(byteBuffer, 0);
                long SecondVideoPTS = mediaExtractor.getSampleTime();
                videoSampleTime = Math.abs(SecondVideoPTS - firstVideoPTS);
                Log.e("Harrison", "videoSampleTime is " + videoSampleTime);
            }

            mediaExtractor.unselectTrack(videoIndex);
            mediaExtractor.selectTrack(videoIndex);
            //选择起点
            mediaExtractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            while (true) {
                int readSampleSize = mediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleSize < 0) {
                    mediaExtractor.unselectTrack(trackIndex);
                    break;
                }
                mediaExtractor.advance();
                bufferInfo.size = readSampleSize;
                bufferInfo.offset = 0;
                bufferInfo.flags = mediaExtractor.getSampleFlags();
                bufferInfo.presentationTimeUs += videoSampleTime;

                mediaMuxer.writeSampleData(trackIndex, byteBuffer, bufferInfo);
            }
            mediaMuxer.stop();
            mediaExtractor.release();
            mediaMuxer.release();

            Log.e("TAG", "finish");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void muxerAudio() {
        mediaExtractor = new MediaExtractor();
        int audioIndex = -1;
        try {
            mediaExtractor.setDataSource(SDCARD_PATH + "/input.mp4");
            int trackCount = mediaExtractor.getTrackCount();
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                if (trackFormat.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                    audioIndex = i;
                }
            }
            mediaExtractor.selectTrack(audioIndex);
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(audioIndex);
            mediaMuxer = new MediaMuxer(SDCARD_PATH + "/output_audio.mp3", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int writeAudioIndex = mediaMuxer.addTrack(trackFormat);
            mediaMuxer.start();
            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            long stampTime = 0;
            //获取帧之间的间隔时间
            {
                mediaExtractor.readSampleData(byteBuffer, 0);
                if (mediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                    mediaExtractor.advance();
                }
                mediaExtractor.readSampleData(byteBuffer, 0);
                long secondTime = mediaExtractor.getSampleTime();
                mediaExtractor.advance();
                mediaExtractor.readSampleData(byteBuffer, 0);
                long thirdTime = mediaExtractor.getSampleTime();
                stampTime = Math.abs(thirdTime - secondTime);
                Log.e("Harrison", stampTime + "");
            }
            //释放选择轨道
            mediaExtractor.unselectTrack(audioIndex);
            mediaExtractor.selectTrack(audioIndex);
            int index = 0;
            Log.e("Harrison", "开始分离：" + index);
            while (true) {
                int readSampleSize = mediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleSize < 0) {
                    Log.e("Harrison", "finish::" + index);
                    break;
                }
                index++;
                mediaExtractor.advance();

                bufferInfo.size = readSampleSize;
                bufferInfo.flags = mediaExtractor.getSampleFlags();
                bufferInfo.offset = 0;
                bufferInfo.presentationTimeUs += stampTime;

                mediaMuxer.writeSampleData(writeAudioIndex, byteBuffer, bufferInfo);
                Log.e("Harrison", "finish:" + index);

            }
            mediaMuxer.stop();
            mediaMuxer.release();
            mediaExtractor.release();
            Log.e("Harrison", "finish");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void combineVideo() {
        try {
            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(SDCARD_PATH + "/output_video.mp4");
            int videoTrackIndex = -1;
            int audioTrackIndex = -1;
            int videoTrackCount = videoExtractor.getTrackCount();
            for (int i = 0; i < videoTrackCount; i++) {
                MediaFormat format = videoExtractor.getTrackFormat(i);
                String mimeType = format.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("video/")) {
                    videoTrackIndex = i;
                    break;
                }

            }
            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(SDCARD_PATH + "/output_audio.mp3");
            int audioTrackCount = audioExtractor.getTrackCount();
            for (int i = 0; i < audioTrackCount; i++) {
                MediaFormat format = audioExtractor.getTrackFormat(i);
                String mimeType = format.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("audio/")) {
                    audioTrackIndex = i;
                    break;
                }
            }
            videoExtractor.selectTrack(videoTrackIndex);
            audioExtractor.selectTrack(audioTrackIndex);

            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

            MediaMuxer mediaMuxer = new MediaMuxer(SDCARD_PATH + "/outputComibine.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int writeVideoTrackIndex = mediaMuxer.addTrack(videoExtractor.getTrackFormat(videoTrackIndex));
            int writeAudioTrackIndex = mediaMuxer.addTrack(audioExtractor.getTrackFormat(audioTrackIndex));
            mediaMuxer.start();
            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            long sampleTime = 0;
            {
                videoExtractor.readSampleData(byteBuffer, 0);
                if (videoExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                    videoExtractor.advance();
                }
                videoExtractor.readSampleData(byteBuffer, 0);
                long secondTime = videoExtractor.getSampleTime();
                videoExtractor.advance();
                long thirdTime = videoExtractor.getSampleTime();
                sampleTime = Math.abs(thirdTime - secondTime);
            }
            videoExtractor.unselectTrack(videoTrackIndex);
            videoExtractor.selectTrack(videoTrackIndex);
            Log.e("Harrison", "开始合并视频");
            while (true) {
                int readVideoSampleSize = videoExtractor.readSampleData(byteBuffer, 0);
                if (readVideoSampleSize < 0) {
                    break;
                }
                videoBufferInfo.size = readVideoSampleSize;
                videoBufferInfo.presentationTimeUs += sampleTime;
                videoBufferInfo.offset = 0;
                videoBufferInfo.flags = videoExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeVideoTrackIndex, byteBuffer, videoBufferInfo);
                videoExtractor.advance();
            }
            while (true) {
                int readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0);
                if (readAudioSampleSize < 0) {
                    break;
                }
                audioBufferInfo.size = readAudioSampleSize;
                audioBufferInfo.presentationTimeUs += sampleTime;
                audioBufferInfo.offset = 0;
                audioBufferInfo.flags = videoExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo);
                audioExtractor.advance();
            }

            mediaMuxer.stop();
            mediaMuxer.release();
            videoExtractor.release();
            audioExtractor.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}