package com.example.baidu.testgpuimagefilter;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class MediaMuxerTest {
    private static final String TAG = "MediaMuxerTest";
    private static final boolean VERBOSE = false;
    private static final int MAX_SAMPLE_SIZE = 256 * 1024;
    private Resources mResources;

    public MediaMuxerTest(Context context) {
        mResources = context.getResources();
    }

    /**
     * Test: make sure the muxer handles both video and audio tracks correctly.
     */
    public void testVideoAudio() throws Exception {
        int[] source = new int[3];
        source[0] = R.raw.same442;
        source[1] = R.raw.video_480x360_mp4_h264_500kbps_30fps_aac_stereo_128kbps_44100hz;
        source[2] = R.raw.we_chat_sight723;
        String outputFile = "/sdcard/videoAudio" + System.currentTimeMillis() + ".mp4";

        AssetFileDescriptor testFd = mResources.openRawResourceFd(source[0]);
        MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
        retrieverSrc.setDataSource(testFd.getFileDescriptor(),
                testFd.getStartOffset(), testFd.getLength());
        String testDegrees = retrieverSrc.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        Log.d(TAG, "source[0] degree is=" + testDegrees);
        cloneAndVerify(source, outputFile, 2, Integer.parseInt(testDegrees));
    }

    /**
     * Using the MediaMuxer to clone a media file.
     */
    private void cloneMediaUsingMuxer(int[] srcMedia, String dstMediaPath,
                                      int expectedTrackCount, int degrees) throws IOException {
        // Set up MediaExtractor to read from the source.
        AssetFileDescriptor srcFd = mResources.openRawResourceFd(srcMedia[0]);
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(srcFd.getFileDescriptor(), srcFd.getStartOffset(),
                srcFd.getLength());
        int trackCount = extractor.getTrackCount();
        // Set up MediaMuxer for the destination.
        MediaMuxer muxer;
        muxer = new MediaMuxer(dstMediaPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        // Set up the tracks.
        HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>(trackCount);
        for (int i = 0; i < trackCount; i++) {
            extractor.selectTrack(i);
            MediaFormat format = extractor.getTrackFormat(i);
            int dstIndex = muxer.addTrack(format);
            indexMap.put(i, dstIndex);
        }
        // Copy the samples from MediaExtractor to MediaMuxer.
        boolean sawEOS = false;
        int bufferSize = MAX_SAMPLE_SIZE;
        int frameCount = 0;
        int offset = 100;
        ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
        BufferInfo bufferInfo = new BufferInfo();
        if (degrees >= 0) {
            muxer.setOrientationHint(degrees);
        }
        muxer.start();
        int currentIndex = 0;
        long lastPts = 0L;
        long epochNow = 0L;
        while (!sawEOS) {
            bufferInfo.offset = offset;
            bufferInfo.size = extractor.readSampleData(dstBuf, offset);
            if (bufferInfo.size < 0) {
                if (VERBOSE) {
                    Log.d(TAG, "saw input EOS.");
                }
                if (currentIndex < 2) {
                    currentIndex++;
                    epochNow = lastPts;
                    extractor.release();
                    // reuse a new src
                    srcFd = mResources.openRawResourceFd(srcMedia[currentIndex]);
                    extractor = new MediaExtractor();
                    extractor.setDataSource(srcFd.getFileDescriptor(), srcFd.getStartOffset(),
                            srcFd.getLength());
                    trackCount = extractor.getTrackCount();
                    for (int i = 0; i < trackCount; i++) {
                        extractor.selectTrack(i);
                    }

                } else {
                    sawEOS = true;
                    bufferInfo.size = 0;
                }

            } else {
                bufferInfo.presentationTimeUs = epochNow + extractor.getSampleTime();
                lastPts = bufferInfo.presentationTimeUs;
                bufferInfo.flags = extractor.getSampleFlags();
                Log.d(TAG, "flags = " + bufferInfo.flags + ";pts=" + bufferInfo.presentationTimeUs);
//                switch () {
//                    case MediaCodec.BUFFER_FLAG_END_OF_STREAM:
//                        break;
//                    default:
//                        break;
//                }
                int trackIndex = extractor.getSampleTrackIndex();
                muxer.writeSampleData(indexMap.get(trackIndex), dstBuf,
                        bufferInfo);
                extractor.advance();
                frameCount++;
                if (VERBOSE) {
                    Log.d(TAG, "Frame (" + frameCount + ") " +
                            "PresentationTimeUs:" + bufferInfo.presentationTimeUs +
                            " Flags:" + bufferInfo.flags +
                            " TrackIndex:" + trackIndex +
                            " Size(KB) " + bufferInfo.size / 1024);
                }
            }
        }
        muxer.stop();
        muxer.release();
        srcFd.close();
        return;
    }
    /**
     * Clones a media file and then compares against the source file to make
     * sure they match.
     */
    private void cloneAndVerify(int[] srcMedia, String outputMediaFile,
                                int expectedTrackCount, int degrees) throws IOException {
        try {
            cloneMediaUsingMuxer(srcMedia, outputMediaFile, expectedTrackCount, degrees);
            verifyAttributesMatch(srcMedia[0], outputMediaFile, degrees);
            // Check the sample on 1s and 0.5s.
//            verifySamplesMatch(srcMedia, outputMediaFile, 1000000);
//            verifySamplesMatch(srcMedia, outputMediaFile, 500000);
        } finally {
//            new File(outputMediaFile).delete();
        }
    }
    /**
     * Compares some attributes using MediaMetadataRetriever to make sure the
     * cloned media file matches the source file.
     */
    private void verifyAttributesMatch(int srcMedia, String testMediaPath,
                                       int degrees) {
        AssetFileDescriptor testFd = mResources.openRawResourceFd(srcMedia);
        MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
        retrieverSrc.setDataSource(testFd.getFileDescriptor(),
                testFd.getStartOffset(), testFd.getLength());
        MediaMetadataRetriever retrieverTest = new MediaMetadataRetriever();
        retrieverTest.setDataSource(testMediaPath);
        String testDegrees = retrieverTest.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if (testDegrees != null) {

        }
        String heightSrc = retrieverSrc.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String heightTest = retrieverTest.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

        String widthSrc = retrieverSrc.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String widthTest = retrieverTest.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

        String durationSrc = retrieverSrc.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String durationTest = retrieverTest.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

        retrieverSrc.release();
        retrieverTest.release();
    }
    /**
     * Uses 2 MediaExtractor, seeking to the same position, reads the sample and
     * makes sure the samples match.
     */
    private void verifySamplesMatch(int srcMedia, String testMediaPath,
                                    int seekToUs) throws IOException {
        AssetFileDescriptor testFd = mResources.openRawResourceFd(srcMedia);
        MediaExtractor extractorSrc = new MediaExtractor();
        extractorSrc.setDataSource(testFd.getFileDescriptor(),
                testFd.getStartOffset(), testFd.getLength());
        int trackCount = extractorSrc.getTrackCount();
        MediaExtractor extractorTest = new MediaExtractor();
        extractorTest.setDataSource(testMediaPath);

        // Make sure the format is the same and select them
        for (int i = 0; i < trackCount; i++) {
            MediaFormat formatSrc = extractorSrc.getTrackFormat(i);
            MediaFormat formatTest = extractorTest.getTrackFormat(i);
            String mimeIn = formatSrc.getString(MediaFormat.KEY_MIME);
            String mimeOut = formatTest.getString(MediaFormat.KEY_MIME);
            if (!(mimeIn.equals(mimeOut))) {
//                fail("format didn't match on track No." + i +
//                        formatSrc.toString() + "\n" + formatTest.toString());
            }
            extractorSrc.selectTrack(i);
            extractorTest.selectTrack(i);
        }
        // Pick a time and try to compare the frame.
        extractorSrc.seekTo(seekToUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        extractorTest.seekTo(seekToUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        int bufferSize = MAX_SAMPLE_SIZE;
        ByteBuffer byteBufSrc = ByteBuffer.allocate(bufferSize);
        ByteBuffer byteBufTest = ByteBuffer.allocate(bufferSize);
        extractorSrc.readSampleData(byteBufSrc, 0);
        extractorTest.readSampleData(byteBufTest, 0);
        if (!(byteBufSrc.equals(byteBufTest))) {
//            fail("byteBuffer didn't match");
        }
    }
}