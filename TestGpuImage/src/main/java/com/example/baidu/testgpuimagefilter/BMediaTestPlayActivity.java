package com.example.baidu.testgpuimagefilter;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.baidu.cloud.media.player.BDCloudMediaPlayer;
import com.baidu.cloud.media.player.IMediaPlayer;
import com.example.baidu.testgpuimagefilter.gles.EglCore;
import com.example.baidu.testgpuimagefilter.gles.GlUtil;
import com.example.baidu.testgpuimagefilter.gles.WindowSurface;


import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.Rotation;
import jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil;
import videoplayer.widget.IRenderView;
import videoplayer.widget.TextureRenderView;

import static android.R.attr.width;
import static com.example.baidu.testgpuimagefilter.GPUImageFilterTools.FilterType.CONTRAST;
import static com.example.baidu.testgpuimagefilter.GPUImageFilterTools.FilterType.GRAYSCALE;
import static com.example.baidu.testgpuimagefilter.GPUImageFilterTools.FilterType.NOFILTER;
import static jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil.TEXTURE_NO_ROTATION;

public class BMediaTestPlayActivity extends Activity implements TextureView.SurfaceTextureListener {

    public static final String TAG = "TestPlayActivity";

    private TextureRenderView textureView;

    private RenderThread mRenderThread;

    public static String[] arrText = new String[]{
            "No Filter", "CONTRAST", "GRAYSCALE",
            "SHARPEN", "SEPIA", "GAMMA",
            "THREE_X_THREE_CONVOLUTION", "FILTER_GROUP", "EMBOSS",
            "No Filter", "CONTRAST", "GRAYSCALE",
            "SHARPEN", "SEPIA", "GAMMA",
            "THREE_X_THREE_CONVOLUTION", "FILTER_GROUP", "EMBOSS"
    };
    public static GPUImageFilterTools.FilterType[] arrImages = new GPUImageFilterTools.FilterType[]{
            GRAYSCALE, CONTRAST, GRAYSCALE,
            NOFILTER, CONTRAST, GRAYSCALE,
            NOFILTER, CONTRAST, GRAYSCALE,
            NOFILTER, CONTRAST, GRAYSCALE,
            NOFILTER, CONTRAST, GRAYSCALE,
            NOFILTER, CONTRAST, GRAYSCALE
    };

    private HashMap<TextureView, Integer> holderMap = new HashMap<>();
    private int orientation;
    private SurfaceTexture mSurfaceTexture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bdmedia);

        textureView = (TextureRenderView) findViewById(R.id.texture);
        textureView.setAspectRatio(IRenderView.AR_MATCH_PARENT);
//        textureView.setVideoRotation(orientation);
        textureView.setSurfaceTextureListener(this);
        holderMap.put(textureView, 0);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume BEGIN");
        super.onResume();

//        mSurfaceTexture = textureView.getSurfaceTexture();

        mRenderThread = new RenderThread(this);
        mRenderThread.setName("TexFromCam Render");
        mRenderThread.start();
        mRenderThread.waitUntilReady();

        Log.i(TAG, "onResume END");
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause BEGIN");
        super.onPause();

        RenderHandler rh = mRenderThread.getHandler();
        rh.sendMediaPlayerRelease();
        rh.sendShutdown();
        try {
            mRenderThread.join();
        } catch (InterruptedException ie) {
            // not expected
            throw new RuntimeException("join was interrupted", ie);
        }
        mRenderThread = null;
        Log.i(TAG, "onPause END");
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d(TAG, "onSurfaceTextureAvailable surfaceTexture=" + surfaceTexture.hashCode() + ";width=" + i + ";height=" + i1);
        mSurfaceTexture = surfaceTexture;

        if (mRenderThread != null) {
            // Normal case -- render thread is running, tell it about the new surface.
            RenderHandler rh = mRenderThread.getHandler();
            rh.sendSurfaceAvailable(surfaceTexture, i, i1);
        } else {
            Log.i(TAG, "render thread not running");
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d(TAG, "onSurfaceTextureSizeChanged surfaceTexture=" + surfaceTexture.hashCode() + ";width=" + i + ";height=" + i1);
        mSurfaceTexture = surfaceTexture;
        if (mRenderThread != null) {
            // Normal case -- render thread is running, tell it about the new surface.
            RenderHandler rh = mRenderThread.getHandler();
            rh.sendSurfaceChanged(surfaceTexture, i, i1);
        } else {
            Log.i(TAG, "render thread not running");
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "onSurfaceTextureDestroyed surfaceTexture=" + surfaceTexture.hashCode());
        if (mRenderThread != null) {
            RenderHandler rh = mRenderThread.getHandler();
            rh.sendSurfaceDestroyed(surfaceTexture);
        }
        holderMap.remove(surfaceTexture);

        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//        Log.d(TAG, "onSurfaceTextureUpdated surfaceTexture=" + surfaceTexture.hashCode());
    }

    private static class RenderHandler extends Handler {
        private static final int MSG_SURFACE_AVAILABLE = 0;
        private static final int MSG_SURFACE_CHANGED = 1;
        private static final int MSG_SURFACE_DESTROYED = 2;
        private static final int MSG_SHUTDOWN = 3;
        private static final int MSG_FRAME_AVAILABLE = 4;
        private static final int MSG_RELEASE_PLAYER = 8;
        private static final int MSG_REDRAW = 9;

        // This shouldn't need to be a weak ref, since we'll go away when the Looper quits,
        // but no real harm in it.
        private WeakReference<RenderThread> mWeakRenderThread;

        /**
         * Call from render thread.
         */
        public RenderHandler(RenderThread rt) {
//            super();
            mWeakRenderThread = new WeakReference<RenderThread>(rt);
        }

        /**
         * Sends the "surface available" message.  If the surface was newly created (i.e.
         * this is called from surfaceCreated()), set newSurface to true.  If this is
         * being called during Activity startup for a previously-existing surface, set
         * newSurface to false.
         * <p>
         * The flag tells the caller whether or not it can expect a surfaceChanged() to
         * arrive very soon.
         * <p>
         * Call from UI thread.
         */
        public void sendSurfaceAvailable(SurfaceTexture holder, int width, int height) {
            sendMessage(obtainMessage(MSG_SURFACE_AVAILABLE,
                    width, height, holder));
        }

        /**
         * Sends the "surface changed" message, forwarding what we got from the SurfaceHolder.
         * <p>
         * Call from UI thread.
         */
        public void sendSurfaceChanged(SurfaceTexture holder, int width,
                                       int height) {
            // ignore format
            sendMessage(obtainMessage(MSG_SURFACE_CHANGED, width, height, holder));
        }

        /**
         * Sends the "shutdown" message, which tells the render thread to halt.
         * <p>
         * Call from UI thread.
         */
        public void sendSurfaceDestroyed(SurfaceTexture holder) {
            sendMessage(obtainMessage(MSG_SURFACE_DESTROYED, 0, 0, holder));
        }

        public void sendMediaPlayerRelease() {
            sendEmptyMessage(MSG_RELEASE_PLAYER);
        }

        /**
         * Sends the "shutdown" message, which tells the render thread to halt.
         * <p>
         * Call from UI thread.
         */
        public void sendShutdown() {
            sendMessage(obtainMessage(MSG_SHUTDOWN));
        }

        /**
         * Sends the "frame available" message.
         * <p>
         * Call from UI thread.
         */
        public void sendFrameAvailable() {
            sendMessage(obtainMessage(MSG_FRAME_AVAILABLE));
        }

        @Override  // runs on RenderThread
        public void handleMessage(Message msg) {
            int what = msg.what;
            //Log.i(TAG, "RenderHandler [" + this + "]: what=" + what);

            RenderThread renderThread = mWeakRenderThread.get();
            if (renderThread == null) {
                Log.w(TAG, "RenderHandler.handleMessage: weak ref is null");
                return;
            }

            switch (what) {
                case MSG_SURFACE_AVAILABLE:
                    renderThread.surfaceAvailable((SurfaceTexture) msg.obj, msg.arg1, msg.arg2);
                    break;
                case MSG_SURFACE_CHANGED:
                    renderThread.surfaceChanged((SurfaceTexture) msg.obj, msg.arg1, msg.arg2);
                    break;
                case MSG_SURFACE_DESTROYED:
                    renderThread.surfaceDestroyed((SurfaceTexture) msg.obj);
                    break;
                case MSG_SHUTDOWN:
                    renderThread.shutdown();
                    break;
                case MSG_FRAME_AVAILABLE:
                    renderThread.frameAvailable();
                    break;

                case MSG_REDRAW:
                    renderThread.draw();
                    break;
                case MSG_RELEASE_PLAYER:
                    renderThread.releaseMediaPlayer();
                    break;
                default:
                    throw new RuntimeException("unknown message " + what);
            }
        }
    }

    /**
     * Thread that handles all rendering and camera operations.
     */
    private class RenderThread extends Thread implements
            SurfaceTexture.OnFrameAvailableListener {
        // Object must be created on render thread to get correct Looper, but is used from
        // UI thread, so we need to declare it volatile to ensure the UI thread sees a fully
        // constructed object.
        private volatile RenderHandler mHandler;

        // Used to wait for the thread to start.
        private Object mStartLock = new Object();
        private boolean mReady = false;

//        private MediaPlayer mediaPlayer;

        private EglCore mEglCore;

        // Receives the output from the camera preview.
        private SurfaceTexture mCameraTexture;

        private Activity activity;

        /**
         * Constructor.  Pass in the MainHandler, which allows us to send stuff back to the
         * Activity.
         */
        public RenderThread(Activity activity) {
            this.activity = activity;
        }

        /**
         * Thread entry point.
         */
        @Override
        public void run() {
            Looper.prepare();

            // We need to create the Handler before reporting ready.
            mHandler = new RenderHandler(this);
            synchronized (mStartLock) {
                mReady = true;
                mStartLock.notify();    // signal waitUntilReady()
            }

            // Prepare EGL and open the camera before we start handling messages.
            mEglCore = new EglCore(null, 0);
//            mediaPlayer = new MediaPlayer();
//            try {
//                mediaPlayer.setDataSource(createFiveVideoSDPath("5808.mp4"));
//                mediaPlayer.prepare();
//                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                    @Override
//                    public void onPrepared(MediaPlayer mp) {
//                        mp.start();
//                    }
//                });
//                mediaPlayer.setLooping(true);
//                mediaPlayer.setVolume(0, 0);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            initPlayer();

            Looper.loop();

            Log.i(TAG, "looper quit");
//            mediaPlayer.release();
//            releaseGl(null);


            if (gpuImageFilters.size() > 0) {
                for (GPUImageFilterGroup gpuImageFilterGroup : gpuImageFilters.values()) {
                    gpuImageFilterGroup.destroy();
                }
            }

            mEglCore.release();

            synchronized (mStartLock) {
                mReady = false;
            }
        }

        private BDCloudMediaPlayer bdCloudMediaPlayer;

        private String[] videoPath;

        private void initPlayer() {

            videoPath = new String[]{createFiveVideoSDPath("5808.mp4")};

            BDCloudMediaPlayer.setAK("3ad8d97bb16243cf924b00d23e2b9659");
            bdCloudMediaPlayer = new BDCloudMediaPlayer(activity.getApplicationContext());
            try {//"we_chat_sight723.mp4"
                bdCloudMediaPlayer.setDataSource(createFiveVideoSDPath("5808.mp4"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            bdCloudMediaPlayer.prepareAsync();
            bdCloudMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final IMediaPlayer iMediaPlayer) {
                    if (textureView != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("bdcloud", "prepare videosize:" + iMediaPlayer.getVideoWidth() + "/" + iMediaPlayer.getVideoHeight());
                                textureView.setVideoSize(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight());
                            }
                        });
                    }
                    bdCloudMediaPlayer.start();
                }
            });

            bdCloudMediaPlayer.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(final IMediaPlayer iMediaPlayer, final int i, final int i1, final int i2, final int i3) {
                    if (textureView != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("bdcloud", "sizechange videosize:" + iMediaPlayer.getVideoWidth() + "/" + iMediaPlayer.getVideoHeight());
                                Log.i("bdcloud", "sizechange width:" + i + ";height=" + i1 + ";num=" + i2 + ";den=" + i3);
                                textureView.setVideoSize(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight());
                            }
                        });
                    }
                }
            });

            bdCloudMediaPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {

                    if (i == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
                        orientation = i1;
                        if (textureView != null)
                            textureView.setVideoRotation(i1);
//                        for(GPUImageFilterGroup value : gpuImageFilters.values()) {
//                            if (i1 == 90 || i1 == 270) {
//                                value.onOutputSizeChanged(iMediaPlayer.getVideoHeight(), iMediaPlayer.getVideoWidth());
//                            } else {
//                                value.onOutputSizeChanged(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight());
//                            }
//                        }
                    }

                    return true;
                }
            });

            bdCloudMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer iMediaPlayer) {
//                    int j = i % 3;
//                    bdCloudMediaPlayer.stop();
//                    bdCloudMediaPlayer.reset();
//                    try {
//                        bdCloudMediaPlayer.setDataSource(videoPath[j]);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Surface surface = new Surface(mSurfaceTexture);
//                    bdCloudMediaPlayer.setSurface(surface);
//                    bdCloudMediaPlayer.prepareAsync();
//                    bdCloudMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
//                        @Override
//                        public void onPrepared(IMediaPlayer iMediaPlayer) {
//                            bdCloudMediaPlayer.start();
//                        }
//                    });
//                    i++;
                }
            });
        }

        private int i = 1;

        /**
         * Waits until the render thread is ready to receive messages.
         * <p>
         * Call from the UI thread.
         */
        public void waitUntilReady() {
            synchronized (mStartLock) {
                while (!mReady) {
                    try {
                        mStartLock.wait();
                    } catch (InterruptedException ie) { /* not expected */ }
                }
            }
        }

        /**
         * Shuts everything down.
         */
        private void shutdown() {
            Log.i(TAG, "shutdown");
            Looper.myLooper().quit();
        }

        /**
         * Returns the render thread's Handler.  This may be called from any thread.
         */
        public RenderHandler getHandler() {
            return mHandler;
        }

        int mTextureId = -1;

        final float CUBE[] = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f,
        };
        private FloatBuffer mGLCubeBuffer;
        private FloatBuffer mGLTextureBuffer;

        HashMap<SurfaceTexture, WindowSurface> windowSurfacesMap = new HashMap<SurfaceTexture, WindowSurface>();
        HashMap<Integer, GPUImageFilterGroup> gpuImageFilters = new HashMap<Integer, GPUImageFilterGroup>();

        WindowSurface mWindowSurface1;
        GPUImageFilterGroup gpuImageFilter;

        int surfaceWidth = 0;
        int surfaceHeight = 0;
        /**
         * Handles the surface-created callback from SurfaceView.  Prepares GLES and the Surface.
         */
        private void surfaceAvailable(SurfaceTexture holder, int width, int height) {

            Log.i(TAG, "RenderThread surfaceCreated holder=" + holder.hashCode());
            Surface surface = new Surface(holder);
            mWindowSurface1 = new WindowSurface(mEglCore, surface, false);
            synchronized (windowSurfacesMap) {
                windowSurfacesMap.put(holder, mWindowSurface1);
                mWindowSurface1.makeCurrent();
            }
            GLES20.glViewport(0, 0, width, height);

            if (windowSurfacesMap.size() <= 1) {
                // only create once

                mTextureId = getPreviewTexture();
                Log.i(TAG, "mTextureId=" + mTextureId);
                mCameraTexture = new SurfaceTexture(mTextureId);

                mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                mGLCubeBuffer.put(CUBE).position(0);

                mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                mGLTextureBuffer.put(TEXTURE_NO_ROTATION).position(0);


                Log.i(TAG, "surfaceChanged should only once here");
                // create all filter
                if (gpuImageFilters.size() > 0) {
                    gpuImageFilters.clear();
                }
//                for (int i = 0; i < BMediaTestPlayActivity.arrText.length; ++i) {
                    gpuImageFilter = new GPUImageFilterGroup();
                    gpuImageFilter.addFilter(new GPUImageExtTexFilter());
                    GPUImageFilter filter = GPUImageFilterTools.createFilterForType(activity, GRAYSCALE);
                    gpuImageFilter.addFilter(filter);
                    gpuImageFilter.init();

                    Log.i("bdcloud", "filter videosize:" + width + "/" + height + "orientation:" + orientation);
                surfaceWidth = width;
                surfaceHeight = height;
                    gpuImageFilter.onOutputSizeChanged(width, height);
                    gpuImageFilters.put(i, gpuImageFilter);
//                }

                mCameraTexture.setOnFrameAvailableListener(this);
                finishSurfaceSetup();
            }

        }

        //            surfaceWidth = width;
//            surfaceHeight = height;
//            releaseGl(surfaceHolder);
//            mEglCore.makeNothingCurrent();
//
//            Surface surface = new Surface(surfaceHolder);
//            mWindowSurface1 = new WindowSurface(mEglCore, surface, false);
//            mWindowSurface1.makeCurrent();

        /**
         * Handles the surfaceChanged message.
         * <p>
         * We always receive surfaceChanged() after surfaceCreated(), but surfaceAvailable()
         * could also be called with a Surface created on a previous run.  So this may not
         * be called.
         */
        private void surfaceChanged(SurfaceTexture surfaceHolder, int width, int height) {
            Log.i(TAG, "RenderThread surfaceChanged " + width + "x" + height + ";surfaceHolder=" + surfaceHolder.hashCode());

            GLES20.glViewport(0, 0, width, height);
            gpuImageFilter.onOutputSizeChanged(width, height);
        }

        /**
         * Handles the surfaceDestroyed message.
         */
        private void surfaceDestroyed(SurfaceTexture surfaceHolder) {
            // In practice this never appears to be called -- the activity is always paused
            // before the surface is destroyed.  In theory it could be called though.
//            Log.i(TAG, "RenderThread surfaceDestroyed holder=" + surfaceHolder.hashCode());
            releaseGl(surfaceHolder);
        }

//        Rotation mRotation = Rotation.NORMAL;
//        private void adjustImageScaling(int mOutputWidth, int mOutputHeight) {
//            float outputWidth = mOutputWidth;
//            float outputHeight = mOutputHeight;
//            if (mRotation == Rotation.ROTATION_270 || mRotation == Rotation.ROTATION_90) {
//                outputWidth = mOutputHeight;
//                outputHeight = mOutputWidth;
//            }
//
//            float ratio1 = outputWidth / bdCloudMediaPlayer.getVideoHeight();
//            float ratio2 = outputHeight / bdCloudMediaPlayer.getVideoWidth();
//            float ratioMax = Math.max(ratio1, ratio2);
//            int imageWidthNew = Math.round( bdCloudMediaPlayer.getVideoHeight() * ratioMax);
//            int imageHeightNew = Math.round(bdCloudMediaPlayer.getVideoWidth() * ratioMax);
//
//            float ratioWidth = imageWidthNew / outputWidth;
//            float ratioHeight = imageHeightNew / outputHeight;
//
//            float[] cube = CUBE;
//            float[] textureCords = TextureRotationUtil.getRotation(mRotation, false, false);
////            if (GPUImage.ScaleType.CENTER_CROP == GPUImage.ScaleType.CENTER_CROP) {
////                float distHorizontal = (1 - 1 / ratioWidth) / 2;
////                float distVertical = (1 - 1 / ratioHeight) / 2;
////                textureCords = new float[]{
////                        addDistance(textureCords[0], distHorizontal), addDistance(textureCords[1], distVertical),
////                        addDistance(textureCords[2], distHorizontal), addDistance(textureCords[3], distVertical),
////                        addDistance(textureCords[4], distHorizontal), addDistance(textureCords[5], distVertical),
////                        addDistance(textureCords[6], distHorizontal), addDistance(textureCords[7], distVertical),
////                };
////            } else {
//                cube = new float[]{
//                        CUBE[0] / ratioHeight, CUBE[1] / ratioWidth,
//                        CUBE[2] / ratioHeight, CUBE[3] / ratioWidth,
//                        CUBE[4] / ratioHeight, CUBE[5] / ratioWidth,
//                        CUBE[6] / ratioHeight, CUBE[7] / ratioWidth,
//                };
////            }
//
//            mGLCubeBuffer.clear();
//            mGLCubeBuffer.put(cube).position(0);
//            mGLTextureBuffer.clear();
//            mGLTextureBuffer.put(textureCords).position(0);
//        }

//        private float addDistance(float coordinate, float distance) {
//            return coordinate == 0.0f ? distance : 1 - distance;
//        }

        public int getPreviewTexture() {
            int textureId = -1;
            if (textureId == GlUtil.NO_TEXTURE) {
                textureId = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
            }
            return textureId;
        }

        /**
         * Releases most of the GL resources we currently hold (anything allocated by
         * surfaceAvailable()).
         * <p>
         * Does not release EglCore.
         */
        private void releaseGl(SurfaceTexture surfaceHolder) {
            GlUtil.checkGlError("releaseGl start");

            WindowSurface windowSurface = windowSurfacesMap.get(surfaceHolder);
            if (windowSurface != null) {

                windowSurfacesMap.remove(surfaceHolder);
                windowSurface.release();
            }

            GlUtil.checkGlError("releaseGl done");

        }



        private void releaseMediaPlayer() {
            bdCloudMediaPlayer.release();
        }

        /**
         * Sets up anything that depends on the window size.
         * <p>
         * Open the camera (to set mCameraAspectRatio) before calling here.
         */
        private void finishSurfaceSetup() {
//            mediaPlayer.setSurface(new Surface(mCameraTexture));
            bdCloudMediaPlayer.setSurface(new Surface(mCameraTexture));
        }

        @Override   // SurfaceTexture.OnFrameAvailableListener; runs on arbitrary thread
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            mHandler.sendFrameAvailable();
        }

        /**
         * Handles incoming frame of data from the camera.
         */
        private void frameAvailable() {
            mCameraTexture.updateTexImage();
            draw();
        }

        /**
         * Draws the scene and submits the buffer.
         */
        private void draw() {

            GPUImageFilterGroup filter = gpuImageFilter;
            if (filter != null) {
                GlUtil.checkGlError("draw start >");
                WindowSurface windowSurface = mWindowSurface1;
                windowSurface.makeCurrent();
                filter.onDraw(mTextureId, mGLCubeBuffer, mGLTextureBuffer);
                windowSurface.swapBuffers();
                GlUtil.checkGlError("draw done >");
            }
//            synchronized (windowSurfacesMap) {
//            for (Map.Entry<SurfaceTexture, WindowSurface> entry : windowSurfacesMap.entrySet()) {
//
//                SurfaceTexture holder = entry.getKey();
//
//                int position = -1;
//                for (Map.Entry<TextureView, Integer> entryTV : holderMap.entrySet()) {
//                    if (holder.equals(entryTV.getKey().getSurfaceTexture())) {
//                        position = entryTV.getValue();
//                        break;
//                    }
//                }
//                if (position >= 0) {
//                    GPUImageFilterGroup filter = gpuImageFilters.get(position);
//                    if (filter != null) {
//                        GlUtil.checkGlError("draw start >" + holder.hashCode());
//                        WindowSurface windowSurface = entry.getValue();
//                        windowSurface.makeCurrent();
//                        filter.onDraw(mTextureId, mGLCubeBuffer, mGLTextureBuffer);
//                        windowSurface.swapBuffers();
//                        GlUtil.checkGlError("draw done >" + holder.hashCode());
//                    }
//
//                }
//
//
//            }
        }

    }

    public static String createFiveVideoSDPath(String name) {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            File dir = new File(sdDir.toString() + "/Five");
            if (!dir.exists()) {
                dir.mkdir();
            }
            return dir + "/" + name;
        }

        return null;
    }
}
