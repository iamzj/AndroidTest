package com.example.baidu.testgpuimagefilter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.example.baidu.testgpuimagefilter.gles.EglCore;
import com.example.baidu.testgpuimagefilter.gles.GlUtil;
import com.example.baidu.testgpuimagefilter.gles.WindowSurface;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageLookupFilter;

import static android.R.attr.format;
import static android.R.attr.height;
import static android.R.attr.width;
import static com.example.baidu.testgpuimagefilter.GPUImageFilterTools.FilterType.CONTRAST;
import static com.example.baidu.testgpuimagefilter.GPUImageFilterTools.FilterType.GRAYSCALE;
import static com.example.baidu.testgpuimagefilter.GPUImageFilterTools.FilterType.NOFILTER;
import static jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil.TEXTURE_NO_ROTATION;

/**
 * Created by baidu on 2017/3/1.
 */

public class MultiFilterTexturePreviewActivity extends Activity implements AdapterView.OnItemClickListener,
        TextureView.SurfaceTextureListener {
    public static final String TAG = "MultiFilter";
    private GridView mGridView;
    private ArrayList<GridItem> mGridData;
    private GridViewAdapter mGridViewAdapter;
//    MediaPlayer mediaPlayer;

    public static String[] arrText = new String[]{
            "No Filter", "CONTRAST", "GRAYSCALE",
            "SHARPEN", "SEPIA", "GAMMA",
            "THREE_X_THREE_CONVOLUTION", "FILTER_GROUP", "EMBOSS",
            "No Filter", "CONTRAST", "GRAYSCALE",
            "SHARPEN", "SEPIA", "GAMMA",
            "THREE_X_THREE_CONVOLUTION", "FILTER_GROUP", "EMBOSS"
    };
    public static GPUImageFilterTools.FilterType[] arrImages=new GPUImageFilterTools.FilterType[]{
            NOFILTER, CONTRAST, GRAYSCALE,
            NOFILTER, CONTRAST, GRAYSCALE,
            NOFILTER, CONTRAST, GRAYSCALE,
            NOFILTER, CONTRAST, GRAYSCALE,
            NOFILTER, CONTRAST, GRAYSCALE,
            NOFILTER, CONTRAST, GRAYSCALE
//            SHARPEN, SEPIA, GAMMA,
//            THREE_X_THREE_CONVOLUTION, FILTER_GROUP, EMBOSS,
//            NOFILTER, CONTRAST, GRAYSCALE,
//            SHARPEN, SEPIA, GAMMA,
//            THREE_X_THREE_CONVOLUTION, FILTER_GROUP, EMBOSS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_multi_preview);

        mGridView = (GridView) findViewById(R.id.gridView);

        mGridData = new ArrayList<GridItem>();
        for (int i = 0; i < arrText.length; i++) {
            GridItem item = new GridItem();
            item.setTitle(arrText[i]);
            item.setFilterType(arrImages[i]);
            mGridData.add(item);
        }
        mGridViewAdapter = new GridViewAdapter(this, R.layout.grid_item_texture, mGridData);
        mGridView.setAdapter(mGridViewAdapter);
        mGridView.setOnItemClickListener(this);

        mHandler = new MainHandler(this);

    }

//    private Renderer mRender = new Renderer();

//    @Override   // SurfaceHolder.Callback
//    public void surfaceCreated(SurfaceHolder holder) {
//        Log.i(TAG, "surfaceCreated holder=" + holder.hashCode() + " (Surface=" +
//                holder.getSurface().hashCode() + ")");
//
//
//    }
//
//    @Override   // SurfaceHolder.Callback
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        Log.i(TAG, "surfaceChanged fmt=" + format + " size=" + width + "x" + height +
//                " holder=" + holder.hashCode());
//
//
//    }
//
//    @Override   // SurfaceHolder.Callback
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        // In theory we should tell the RenderThread that the surface has been destroyed.
//
//        Log.i(TAG, "surfaceDestroyed holder=" + holder.hashCode());
////        sSurfaceHolder = null;
//    }

    // Thread that handles rendering and controls the camera.  Started in onResume(),
    // stopped in onPause().
    private RenderThread mRenderThread;

    // Receives messages from renderer thread.
    private MainHandler mHandler;

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume BEGIN");
        super.onResume();

        mRenderThread = new RenderThread(mHandler, this);
        mRenderThread.setName("TexFromCam Render");
        mRenderThread.start();
        mRenderThread.waitUntilReady();

        RenderHandler rh = mRenderThread.getHandler();

        // FIXME i change here, remember to reset!
//        if (sSurfaceHolder != null) {
//            Log.i(TAG, "Sending previous surface");
//            rh.sendSurfaceAvailable(sSurfaceHolder, false);
//        } else {
//            Log.i(TAG, "No previous surface");
//        }

        Log.i(TAG, "onResume END");
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause BEGIN");
        super.onPause();

        RenderHandler rh = mRenderThread.getHandler();
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
        Log.d(TAG, "onSurfaceTextureAvailable surfaceTexture=" + surfaceTexture.hashCode());
        if (mRenderThread != null) {
            // Normal case -- render thread is running, tell it about the new surface.
            RenderHandler rh = mRenderThread.getHandler();
            rh.sendSurfaceAvailable(surfaceTexture, i, i1);
        } else {
            // Sometimes see this on 4.4.x N5: power off, power on, unlock, with device in
            // landscape and a lock screen that requires portrait.  The surface-created
            // message is showing up after onPause().
            //
            // Chances are good that the surface will be destroyed before the activity is
            // unpaused, but we track it anyway.  If the activity is un-paused and we start
            // the RenderThread, the SurfaceHolder will be passed in right after the thread
            // is created.
            Log.i(TAG, "render thread not running");
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d(TAG, "onSurfaceTextureSizeChanged surfaceTexture=" + surfaceTexture.hashCode());
        // onSurfaceTextureAvailable has width&height already
//        if (mRenderThread != null) {
//            RenderHandler rh = mRenderThread.getHandler();
//            rh.sendSurfaceChanged(surfaceTexture, width, height);
//        } else {
//            Log.i(TAG, "Ignoring surfaceChanged");
//            return;
//        }
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

    /**
     * Custom message handler for main UI thread.
     * <p>
     * Receives messages from the renderer thread with UI-related updates, like the camera
     * parameters (which we show in a text message on screen).
     */
    private static class MainHandler extends Handler {
        private static final int MSG_SEND_CAMERA_PARAMS0 = 0;
        private static final int MSG_SEND_CAMERA_PARAMS1 = 1;
        private static final int MSG_SEND_RECT_SIZE = 2;
        private static final int MSG_SEND_ZOOM_AREA = 3;
        private static final int MSG_SEND_ROTATE_DEG = 4;

        private WeakReference<MultiFilterTexturePreviewActivity> mWeakActivity;

        public MainHandler(MultiFilterTexturePreviewActivity activity) {
            mWeakActivity = new WeakReference<MultiFilterTexturePreviewActivity>(activity);
        }

        /**
         * Sends the updated camera parameters to the main thread.
         * <p>
         * Call from render thread.
         */
        public void sendCameraParams(int width, int height, float fps) {
            // The right way to do this is to bundle them up into an object.  The lazy
            // way is to send two messages.
            sendMessage(obtainMessage(MSG_SEND_CAMERA_PARAMS0, width, height));
            sendMessage(obtainMessage(MSG_SEND_CAMERA_PARAMS1, (int) (fps * 1000), 0));
        }

        /**
         * Sends the updated rect size to the main thread.
         * <p>
         * Call from render thread.
         */
        public void sendRectSize(int width, int height) {
            sendMessage(obtainMessage(MSG_SEND_RECT_SIZE, width, height));
        }

        /**
         * Sends the updated zoom area to the main thread.
         * <p>
         * Call from render thread.
         */
        public void sendZoomArea(int width, int height) {
            sendMessage(obtainMessage(MSG_SEND_ZOOM_AREA, width, height));
        }

        /**
         * Sends the updated zoom area to the main thread.
         * <p>
         * Call from render thread.
         */
        public void sendRotateDeg(int rot) {
            sendMessage(obtainMessage(MSG_SEND_ROTATE_DEG, rot, 0));
        }

        @Override
        public void handleMessage(Message msg) {
            MultiFilterTexturePreviewActivity activity = mWeakActivity.get();
            if (activity == null) {
                Log.i(TAG, "Got message for dead activity");
                return;
            }

            switch (msg.what) {
                default:
                    throw new RuntimeException("Unknown message " + msg.what);
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

        private MainHandler mMainHandler;

        private MediaPlayer mediaPlayer;

        private EglCore mEglCore;

        private int mWindowSurfaceWidth;
        private int mWindowSurfaceHeight;

        // Receives the output from the camera preview.
        private SurfaceTexture mCameraTexture;

        Activity activity;
        /**
         * Constructor.  Pass in the MainHandler, which allows us to send stuff back to the
         * Activity.
         */
        public RenderThread(MainHandler handler, Activity activity) {
            mMainHandler = handler;
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
            mediaPlayer = MediaPlayer.create(activity, R.raw.video_480x360_mp4_h264_500kbps_30fps_aac_stereo_128kbps_44100hz);
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0, 0);
            mediaPlayer.start();

            Looper.loop();

            Log.i(TAG, "looper quit");
            mediaPlayer.release();
//            releaseGl(null);

            if (gpuImageFilters.size() > 0) {
                for (GPUImageFilterGroup gpuImageFilterGroup: gpuImageFilters.values()) {
                    gpuImageFilterGroup.destroy();
                }
            }

            mEglCore.release();

            synchronized (mStartLock) {
                mReady = false;
            }
        }

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

//        GPUImageFilterGroup gpuImageFilter1;
//        GPUImageFilterGroup gpuImageFilter2;

//        HashSet<SurfaceHolder> holders = new HashSet<SurfaceHolder>();
        HashMap<SurfaceTexture, WindowSurface> windowSurfacesMap = new HashMap<SurfaceTexture, WindowSurface>();
        HashMap<Integer, GPUImageFilterGroup> gpuImageFilters = new HashMap<Integer, GPUImageFilterGroup>();

//        SurfaceHolder surfaceHolder1;
//        SurfaceHolder surfaceHolder2;
        /**
         * Handles the surface-created callback from SurfaceView.  Prepares GLES and the Surface.
         */
        private void surfaceAvailable(SurfaceTexture holder, int width, int height) {

            Log.i(TAG, "RenderThread surfaceCreated holder=" + holder.hashCode());
//            if (holders.contains(holder)) {
//                // added before
//                Log.e(TAG, "surfaceAvailable holder contains should never comein");
//            }
//            holders.add(holder);

//            if (!windowSurfacesMap.containsKey(holder)) {
                Surface surface = new Surface(holder);
                WindowSurface mWindowSurface1 = new WindowSurface(mEglCore, surface, false);
                synchronized (windowSurfacesMap) {
                    windowSurfacesMap.put(holder, mWindowSurface1);
                    mWindowSurface1.makeCurrent();
                }
//            }

            mWindowSurfaceWidth = width;
            mWindowSurfaceHeight = height;

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


//            if (!newSurface) {
//                // This Surface was established on a previous run, so no surfaceChanged()
//                // message is forthcoming.  Finish the surface setup now.
//                //
//                // We could also just call this unconditionally, and perhaps do an unnecessary
//                // bit of reallocating if a surface-changed message arrives.
//                mWindowSurfaceWidth = mWindowSurface.getWidth();
//                mWindowSurfaceHeight = mWindowSurface.getHeight();
//                finishSurfaceSetup(holder);
//            }
                Log.i(TAG, "surfaceChanged should only once here");
                // create all filter
                if (gpuImageFilters.size() > 0) {
                    gpuImageFilters.clear();
                }
                for (int i = 0; i < MultiFilterTexturePreviewActivity.arrText.length; ++i) {
                    GPUImageFilterGroup gpuImageFilter = new GPUImageFilterGroup();
                    gpuImageFilter.addFilter(new GPUImageExtTexFilter());
//                    GPUImageFilter filter = GPUImageFilterTools.createFilterForType(activity, MultiFilterTexturePreviewActivity.arrImages[i]);
//                    gpuImageFilter.addFilter(filter);

                    GPUImageLookupFilter lookupFilter = new GPUImageLookupFilter();
                    lookupFilter.setBitmap(BitmapFactory.decodeResource(getResources(), R.raw.overlaymap));
                    gpuImageFilter.addFilter(lookupFilter);

                    gpuImageFilter.init();
                    gpuImageFilter.onOutputSizeChanged(width, height);
                    gpuImageFilters.put(i, gpuImageFilter);
                }

                mCameraTexture.setOnFrameAvailableListener(this);
                finishSurfaceSetup();
            }

        }

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

//            synchronized (windowSurfacesMap) {
                WindowSurface windowSurface = windowSurfacesMap.get(surfaceHolder);
                if (windowSurface != null) {

                    windowSurfacesMap.remove(surfaceHolder);
                    windowSurface.release();
//                holders.remove(surfaceHolder);
//                    holderMap.remove(surfaceHolder);
//                windowSurface.
                }
//            }

            GlUtil.checkGlError("releaseGl done");

//            mEglCore.makeNothingCurrent();
        }

        /**
         * Handles the surfaceChanged message.
         * <p>
         * We always receive surfaceChanged() after surfaceCreated(), but surfaceAvailable()
         * could also be called with a Surface created on a previous run.  So this may not
         * be called.
         */
        private void surfaceChanged(SurfaceTexture surfaceHolder, int width, int height) {
            Log.i(TAG, "RenderThread surfaceChanged " + width + "x" + height + ";surfaceHolder=" + surfaceHolder.hashCode());



        }

        /**
         * Handles the surfaceDestroyed message.
         */
        private void surfaceDestroyed(SurfaceTexture surfaceHolder) {
            // In practice this never appears to be called -- the activity is always paused
            // before the surface is destroyed.  In theory it could be called though.
//            Log.i(TAG, "RenderThread surfaceDestroyed holder=" + surfaceHolder.hashCode());
            releaseGl(surfaceHolder);
//            Log.i(TAG, "RenderThread surfaceDestroyed done;holder=" + surfaceHolder.hashCode());
        }

        /**
         * Sets up anything that depends on the window size.
         * <p>
         * Open the camera (to set mCameraAspectRatio) before calling here.
         */
        private void finishSurfaceSetup() {
            mediaPlayer.setSurface(new Surface(mCameraTexture));
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


//            synchronized (windowSurfacesMap) {
                for (Map.Entry<SurfaceTexture, WindowSurface> entry : windowSurfacesMap.entrySet()) {

                    SurfaceTexture holder = entry.getKey();

                    int position = -1;
                    for (Map.Entry<TextureView, Integer> entryTV : holderMap.entrySet()) {
                        if (holder.equals(entryTV.getKey().getSurfaceTexture())) {
                            position = entryTV.getValue();
                            break;
                        }
                    }
                    if (position >= 0) {
                        GPUImageFilterGroup filter = gpuImageFilters.get(position);
                        if (filter != null) {
                            GlUtil.checkGlError("draw start >" + holder.hashCode());
                            WindowSurface windowSurface = entry.getValue();
                            windowSurface.makeCurrent();
                            filter.onDraw(mTextureId, mGLCubeBuffer, mGLTextureBuffer);
                            windowSurface.swapBuffers();
                            GlUtil.checkGlError("draw done >" + holder.hashCode());
                        }

                    }


                }
//            }



//            Log.i(TAG, "frameAvailable draw texture end");
        }

    }


    /**
     * Handler for RenderThread.  Used for messages sent from the UI thread to the render thread.
     * <p>
     * The object is created on the render thread, and the various "send" methods are called
     * from the UI thread.
     */
    private static class RenderHandler extends Handler {
        private static final int MSG_SURFACE_AVAILABLE = 0;
        private static final int MSG_SURFACE_CHANGED = 1;
        private static final int MSG_SURFACE_DESTROYED = 2;
        private static final int MSG_SHUTDOWN = 3;
        private static final int MSG_FRAME_AVAILABLE = 4;
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
                default:
                    throw new RuntimeException("unknown message " + what);
            }
        }
    }


    HashMap<TextureView, Integer> holderMap = new HashMap<TextureView, Integer>();

    public class GridViewAdapter extends ArrayAdapter<GridItem> {

        private Context mContext;
        private int layoutResourceId;
        private ArrayList<GridItem> mGridData = new ArrayList<GridItem>();

        public GridViewAdapter(Context context, int resource, ArrayList<GridItem> objects) {
            super(context, resource, objects);
            this.mContext = context;
            this.layoutResourceId = resource;
            this.mGridData = objects;
        }

        public void setGridData(ArrayList<GridItem> mGridData) {
            this.mGridData = mGridData;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.textView = (TextView) convertView.findViewById(R.id.tv_title_in_grid);
                viewHolder.textureView = (TextureView) convertView.findViewById(R.id.texture_in_grid);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            GridItem item = mGridData.get(position);

            viewHolder.textView.setText(item.getTitle());
            viewHolder.textureView.setSurfaceTextureListener(MultiFilterTexturePreviewActivity.this);
            holderMap.put(viewHolder.textureView, position);
            Log.i(TAG, "getView holder=" + viewHolder.textureView.hashCode() + ";position=" + position);
            return convertView;
        }

        private class ViewHolder {
            TextView textView;
            TextureView textureView;
        }
    }

    public class GridItem {
        private GPUImageFilterTools.FilterType filterType;
        private String title;

        public GridItem() {
            super();
        }
        public GPUImageFilterTools.FilterType getFilterType() {
            return filterType;
        }
        public void setFilterType(GPUImageFilterTools.FilterType image) {
            this.filterType = image;
        }
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
    }


    /**
     * GridView的点击回调函数
     *
     * @param adapter  -- GridView对应的dapterView
     * @param view     -- AdapterView中被点击的视图(它是由adapter提供的一个视图)。
     * @param position -- 视图在adapter中的位置。
     * @param rowid    -- 被点击元素的行id。
     */
    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long rowid) {
        String index = arrImages[position].name();
        Intent intent = new Intent();
        intent.putExtra("filter", index);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        // destroy files
        super.onDestroy();
    }
}
