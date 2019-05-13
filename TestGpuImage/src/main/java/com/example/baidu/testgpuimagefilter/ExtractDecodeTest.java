package com.example.baidu.testgpuimagefilter;

/**
 * Created by baidu on 2017/2/9.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;



/**
 * Test for the integration of MediaMuxer and MediaCodec's encoder.
 *
 * <p>It uses MediaExtractor to get frames from a test stream, decodes them to a surface, uses a
 * shader to edit them, encodes them from the resulting surface, and then uses MediaMuxer to write
 * them into a file.
 *
 * <p>It does not currently check whether the result file is correct, but makes sure that nothing
 * fails along the way.
 *
 * <p>It also tests the way the codec config buffers need to be passed from the MediaCodec to the
 * MediaMuxer.
 */
@TargetApi(18)
public class ExtractDecodeTest {

    Context mAppContext;
    public ExtractDecodeTest(Context context) {
        mAppContext = context.getApplicationContext();
    }
    private static final String TAG = ExtractDecodeTest.class.getSimpleName();
    private static final boolean VERBOSE = true; // lots of logging
    /** How long to wait for the next buffer to become available. */
    private static final int TIMEOUT_USEC = 10000;
    /** Where to output the test files. */
    private static final File OUTPUT_FILENAME_DIR = Environment.getExternalStorageDirectory();
    // parameters for the video encoder
    private static final String OUTPUT_VIDEO_MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
    private static final int OUTPUT_VIDEO_BIT_RATE = 500000; // 2Mbps
    private static final int OUTPUT_VIDEO_FRAME_RATE = 15; // 15fps
    private static final int OUTPUT_VIDEO_IFRAME_INTERVAL = 10; // 10 seconds between I-frames
    private static final int OUTPUT_VIDEO_COLOR_FORMAT =
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
    // parameters for the audio encoder
    private static final String OUTPUT_AUDIO_MIME_TYPE = "audio/mp4a-latm"; // Advanced Audio Coding
    private static final int OUTPUT_AUDIO_CHANNEL_COUNT = 2; // Must match the input stream.
    private static final int OUTPUT_AUDIO_BIT_RATE = 128 * 1024;
    private static final int OUTPUT_AUDIO_AAC_PROFILE =
            MediaCodecInfo.CodecProfileLevel.AACObjectHE;
    private static final int OUTPUT_AUDIO_SAMPLE_RATE_HZ = 44100; // Must match the input stream.
    /**
     * Used for editing the frames.
     *
     * <p>Swaps green and blue channels by storing an RBGA color in an RGBA buffer.
     */
    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord).rbga;\n" +
                    "}\n";
    /** Whether to copy the video from the test video. */
    private boolean mCopyVideo;
    /** Whether to copy the audio from the test video. */
    private boolean mCopyAudio;
    /** Width of the output frames. */
    private int mWidth = -1;
    /** Height of the output frames. */
    private int mHeight = -1;
    /** The raw resource used as the input file. */
    private int mSourceResId;
    /** The destination file for the encoded output. */
//    private String mOutputFile;

    private ArrayList<GPUImageFilter> mFilterType;
//    private int mFilterAjust = 0; // not use now
//    public void setFilterType(GPUImageFilterTools.FilterType filterType) {
//        mFilterType = filterType;
//    }
    public void testExtractDecodeEditEncodeMuxAudioVideo(final MainActivity.ResultListener resultListener) throws Throwable {
        setSize(480, 360);
        setSource(R.raw.video_480x360_mp4_h264_500kbps_30fps_aac_stereo_128kbps_44100hz);
        setCopyAudio();
        setCopyVideo();
//        TestWrapper.runTest(this);
        new HandlerThread("completethread") {
            @Override
            public void run() {
//                super.run();
//                setOutputFile();
                try {
                    extractDecodeEditEncodeMux();
                    resultListener.onResult(true, "no ErrorDesc now");
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Sets the test to copy the video stream.
     */
    private void setCopyVideo() {
        mCopyVideo = true;
    }
    /**
     * Sets the test to copy the video stream.
     */
    private void setCopyAudio() {
        mCopyAudio = false;
    }
    /**
     * Sets the desired frame size.
     */
    private void setSize(int width, int height) {
        if ((width % 16) != 0 || (height % 16) != 0) {
            Log.w(TAG, "WARNING: width or height not multiple of 16");
        }
        mWidth = width;
        mHeight = height;
    }
    /**
     * Sets the raw resource used as the source video.
     */
    private void setSource(int resId) {
        mSourceResId = resId;
    }

    /**
     * Tests encoding and subsequently decoding video from frames generated into a buffer.
     * <p>
     * We encode several frames of a video test pattern using MediaCodec, then decode the output
     * with MediaCodec and do some simple checks.
     */
    private void extractDecodeEditEncodeMux() throws Exception {
        // Exception that may be thrown during release.
        Exception exception = null;
        MediaCodecInfo videoCodecInfo = selectCodec(OUTPUT_VIDEO_MIME_TYPE);
        if (videoCodecInfo == null) {
            // Don't fail CTS if they don't have an AVC codec (not here, anyway).
            Log.e(TAG, "Unable to find an appropriate codec for " + OUTPUT_VIDEO_MIME_TYPE);
            return;
        }
        if (VERBOSE) Log.d(TAG, "video found codec: " + videoCodecInfo.getName());

        MediaExtractor videoExtractor = null;

        OutputSurfaceWithFilter outputSurface = null;
        MediaCodec videoDecoder = null;

        try {
            if (mCopyVideo) {
                videoExtractor = createExtractor();
                int videoInputTrack = getAndSelectVideoTrackIndex(videoExtractor);
                MediaFormat inputFormat = videoExtractor.getTrackFormat(videoInputTrack);
                // We avoid the device-specific limitations on width and height by using values
                // that are multiples of 16, which all tested devices seem to be able to handle.
                // Create a MediaCodec for the decoder, based on the extractor's format.
                outputSurface = new OutputSurfaceWithFilter(mAppContext, mFilterType, 480, 360, 0);
//                outputSurface.changeFragmentShader(FRAGMENT_SHADER);
                videoDecoder = createVideoDecoder(inputFormat, outputSurface.getSurface());
            }


            doExtractDecodeEditEncodeMux(
                    videoExtractor,
                    videoDecoder,
                    outputSurface);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (VERBOSE) Log.d(TAG, "releasing extractor, decoder, encoder, and muxer");
            // Try to release everything we acquired, even if one of the releases fails, in which
            // case we save the first exception we got and re-throw at the end (unless something
            // other exception has already been thrown). This guarantees the first exception thrown
            // is reported as the cause of the error, everything is (attempted) to be released, and
            // all other exceptions appear in the logs.
            try {
                if (videoExtractor != null) {
                    videoExtractor.release();
                }
            } catch(Exception e) {
                Log.e(TAG, "error while releasing videoExtractor", e);
                if (exception == null) {
                    exception = e;
                }
            }

            try {
                if (videoDecoder != null) {
                    videoDecoder.stop();
                    videoDecoder.release();
                }
            } catch(Exception e) {
                Log.e(TAG, "error while releasing videoDecoder", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (outputSurface != null) {
                    outputSurface.release();
                }
            } catch(Exception e) {
                Log.e(TAG, "error while releasing outputSurface", e);
                if (exception == null) {
                    exception = e;
                }
            }

        }
        if (exception != null) {
            throw exception;
        }
    }
    /**
     * Creates an extractor that reads its frames from {@link #mSourceResId}.
     */
    private MediaExtractor createExtractor() throws IOException {
        MediaExtractor extractor;
        AssetFileDescriptor srcFd = mAppContext.getResources().openRawResourceFd(mSourceResId);
        extractor = new MediaExtractor();
        extractor.setDataSource(srcFd.getFileDescriptor(), srcFd.getStartOffset(),
                srcFd.getLength());
        return extractor;
    }
    /**
     * Creates a decoder for the given format, which outputs to the given surface.
     *
     * @param inputFormat the format of the stream to decode
     * @param surface into which to decode the frames
     */
    private MediaCodec createVideoDecoder(MediaFormat inputFormat, Surface surface) throws IOException {
        MediaCodec decoder = MediaCodec.createDecoderByType(getMimeTypeFor(inputFormat));
        decoder.configure(inputFormat, surface, null, 0);
        decoder.start();
        return decoder;
    }

    private int getAndSelectVideoTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            if (VERBOSE) {
                Log.d(TAG, "format for track " + index + " is "
                        + getMimeTypeFor(extractor.getTrackFormat(index)));
            }
            if (isVideoFormat(extractor.getTrackFormat(index))) {
                extractor.selectTrack(index);
                return index;
            }
        }
        return -1;
    }

    /**
     * Does the actual work for extracting, decoding, encoding and muxing.
     */
    private void doExtractDecodeEditEncodeMux(
            MediaExtractor videoExtractor,
            MediaCodec videoDecoder,
            OutputSurfaceWithFilter outputSurface) {
        ByteBuffer[] videoDecoderInputBuffers = null;
        ByteBuffer[] videoDecoderOutputBuffers = null;

        MediaCodec.BufferInfo videoDecoderOutputBufferInfo = null;
        if (mCopyVideo) {
            videoDecoderInputBuffers = videoDecoder.getInputBuffers();
            videoDecoderOutputBuffers = videoDecoder.getOutputBuffers();
            videoDecoderOutputBufferInfo = new MediaCodec.BufferInfo();
        }

        // We will get these from the decoders when notified of a format change.
        MediaFormat decoderOutputVideoFormat = null;
        // We will determine these once we have the output format.
        int outputVideoTrack = -1;
        // Whether things are done on the video side.
        boolean videoExtractorDone = false;
        boolean videoDecoderDone = false;
        // The audio decoder output buffer to process, -1 if none.
        int videoExtractedFrameCount = 0;
        int videoDecodedFrameCount = 0;
        while ((mCopyVideo && !videoDecoderDone)) {
            if (VERBOSE) {
                Log.d(TAG, String.format(
                        "loop: "
                                + "V(%b){"
                                + "extracted:%d(done:%b) "
                                + "decoded:%d(done:%b) }",
                        mCopyVideo,
                        videoExtractedFrameCount, videoExtractorDone,
                        videoDecodedFrameCount, videoDecoderDone));
            }
            // Extract video from file and feed to decoder.
            // Do not extract video if we have determined the output format but we are not yet
            // ready to mux the frames.
            while (mCopyVideo && !videoExtractorDone) {
                int decoderInputBufferIndex = videoDecoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (decoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (VERBOSE) Log.d(TAG, "no video decoder input buffer");
                    break;
                }
                if (VERBOSE) {
                    Log.d(TAG, "video decoder: returned input buffer: " + decoderInputBufferIndex);
                }
                ByteBuffer decoderInputBuffer = videoDecoderInputBuffers[decoderInputBufferIndex];
                int size = videoExtractor.readSampleData(decoderInputBuffer, 0);
                long presentationTime = videoExtractor.getSampleTime();
                if (VERBOSE) {
                    Log.d(TAG, "video extractor: returned buffer of size " + size);
                    Log.d(TAG, "video extractor: returned buffer for time " + presentationTime);
                }
                if (size >= 0) {
                    videoDecoder.queueInputBuffer(
                            decoderInputBufferIndex,
                            0,
                            size,
                            presentationTime,
                            videoExtractor.getSampleFlags());
                }
                videoExtractorDone = !videoExtractor.advance();
                if (videoExtractorDone) {
                    if (VERBOSE) Log.d(TAG, "video extractor: EOS");
                    videoDecoder.queueInputBuffer(
                            decoderInputBufferIndex,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                }
                videoExtractedFrameCount++;
                // We extracted a frame, let's try something else next.
                break;
            }

            // Poll output frames from the video decoder and feed the encoder.
            while (mCopyVideo && !videoDecoderDone) {
                int decoderOutputBufferIndex =
                        videoDecoder.dequeueOutputBuffer(
                                videoDecoderOutputBufferInfo, TIMEOUT_USEC);
                if (decoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (VERBOSE) Log.d(TAG, "no video decoder output buffer");
                    break;
                }
                if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    if (VERBOSE) Log.d(TAG, "video decoder: output buffers changed");
                    videoDecoderOutputBuffers = videoDecoder.getOutputBuffers();
                    break;
                }
                if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    decoderOutputVideoFormat = videoDecoder.getOutputFormat();
                    if (VERBOSE) {
                        Log.d(TAG, "video decoder: output format changed: "
                                + decoderOutputVideoFormat);
                    }
                    break;
                }
                if (VERBOSE) {
                    Log.d(TAG, "video decoder: returned output buffer: "
                            + decoderOutputBufferIndex);
                    Log.d(TAG, "video decoder: returned buffer of size "
                            + videoDecoderOutputBufferInfo.size);
                }
                ByteBuffer decoderOutputBuffer =
                        videoDecoderOutputBuffers[decoderOutputBufferIndex];
                if ((videoDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG)
                        != 0) {
                    if (VERBOSE) Log.d(TAG, "video decoder: codec config buffer");
                    videoDecoder.releaseOutputBuffer(decoderOutputBufferIndex, false);
                    break;
                }
                if (VERBOSE) {
                    Log.d(TAG, "video decoder: returned buffer for time "
                            + videoDecoderOutputBufferInfo.presentationTimeUs);
                }
                boolean render = videoDecoderOutputBufferInfo.size != 0;
                videoDecoder.releaseOutputBuffer(decoderOutputBufferIndex, render);

                if (render) {
                    if (VERBOSE) Log.d(TAG, "output surface: await new image");
                    outputSurface.awaitNewImage();
                    // Edit the frame and send it to the encoder.
                    if (VERBOSE) Log.d(TAG, "output surface: draw image");
                    outputSurface.drawImage();
//                    inputSurface.setPresentationTime(
//                            videoDecoderOutputBufferInfo.presentationTimeUs * 1000);
//                    if (VERBOSE) Log.d(TAG, "input surface: swap buffers");
//                    inputSurface.swapBuffers();
                    if (VERBOSE) Log.d(TAG, "video encoder: notified of new frame");
                }
                if ((videoDecoderOutputBufferInfo.flags
                        & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (VERBOSE) Log.d(TAG, "video decoder: EOS");
                    videoDecoderDone = true;
//                    videoEncoder.signalEndOfInputStream();
                }
                videoDecodedFrameCount++;
                // We extracted a pending frame, let's try something else next.
                break;
            }


        }
        // Basic sanity checks.
//        if (mCopyVideo) {
//            assertEquals("encoded and decoded video frame counts should match",
//                    videoDecodedFrameCount, videoEncodedFrameCount);
//            assertTrue("decoded frame count should be less than extracted frame count",
//                    videoDecodedFrameCount <= videoExtractedFrameCount);
//        }
//        if (mCopyAudio) {
////            assertEquals("no frame should be pending", -1, pendingAudioDecoderOutputBufferIndex);
//        }
    }
    private static boolean isVideoFormat(MediaFormat format) {
        return getMimeTypeFor(format).startsWith("video/");
    }
//    private static boolean isAudioFormat(MediaFormat format) {
//        return getMimeTypeFor(format).startsWith("audio/");
//    }
    private static String getMimeTypeFor(MediaFormat format) {
        return format.getString(MediaFormat.KEY_MIME);
    }
    /**
     * Returns the first codec capable of encoding the specified MIME type, or null if no match was
     * found.
     */
    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }
}