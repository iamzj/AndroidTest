package com.example.baidu.testgpuimagefilter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageExposureFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageGammaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHighlightShadowFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageLookupFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageRGBFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSaturationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSharpenFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageWhiteBalanceFilter;


public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private VideoSurfaceView videoSurfaceView = null;
    private MediaPlayer mediaPlayer = null;

    private GPUImageFilter mFilter;
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;
    private GPUImageFilterTools.FilterType currentFilterType = GPUImageFilterTools.FilterType.NOFILTER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(this);
        findViewById(R.id.button_choose_filter).setOnClickListener(this);
        findViewById(R.id.btnGenerate).setOnClickListener(this);
        findViewById(R.id.btnMulti).setOnClickListener(this);
        /**
         * 初始化显示view
         */
        initGLView();


        // FIXME add for test
//        findViewById(R.id.btnGenerate).performClick();
    }

    private volatile boolean isGenerating = false;
    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.button_choose_filter:
                GPUImageFilterTools.showDialog(this, new GPUImageFilterTools.OnGpuImageFilterChosenListener() {

                    @Override
                    public void onGpuImageFilterChosenListener(final GPUImageFilter filter, final GPUImageFilterTools.FilterType filterType) {
                        currentFilterType = filterType;
                        switchFilterTo(filter);
                    }
                });
                break;
            case R.id.btnMulti:
                Intent intent = new Intent(MainActivity.this, MultiFilterPreviewActivity.class);
                this.startActivityForResult(intent, 10000);
                break;
            case R.id.btnGenerate:
                if (isGenerating) {
                    Toast.makeText(MainActivity.this, "已经有一个正在生成的任务，请等待。。", Toast.LENGTH_LONG).show();
                    return;
                }
                isGenerating = true;
                final ResultListener resultListener = new ResultListener() {
                    @Override
                    public void onResult(final boolean success, final String extra) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isGenerating = false;
                                Toast.makeText(MainActivity.this, success? "Generate Sucess!" : "GenerateFailed! reason="
                                        + extra, Toast.LENGTH_LONG).show();
                                Log.i("tag", "==========================!!!!!!done!!!!!!!!===================");
                            }
                        });

                    }
                };
                // do effect-reencode
                ExtractDecodeEditEncodeMuxTest test = new ExtractDecodeEditEncodeMuxTest(MainActivity.this);
                try {

                    test.setFilterType(generateGPUImageFilter(FilterBean.formatFromJsonStr(FilterBean.TEST_YIDE_2)));
                    test.testExtractDecodeEditEncodeMuxAudioVideo(resultListener);

                } catch (Throwable tr) {

                }
                break;

        }
    }



    private ArrayList<GPUImageFilter> generateGPUImageFilter(FilterBean item) {
        ArrayList<GPUImageFilter> filterArrayList = new ArrayList<>();

        GPUImageRGBFilter gpuImageRGBFilter = new GPUImageRGBFilter(item.getRed1() / 100, item.getGreen1() / 100, item.getBlue1() / 100);
        filterArrayList.add(gpuImageRGBFilter);

        GPUImageHighlightShadowTintFilter highlightShadowTintFilter0 = new GPUImageHighlightShadowTintFilter();
        highlightShadowTintFilter0.setmShadowTintColorRed(item.getRed2() / 100, 0, 0);
        highlightShadowTintFilter0.setmHighlightTintColorRed(item.getRed0() / 100, 0, 0);
        filterArrayList.add(highlightShadowTintFilter0);

        GPUImageHighlightShadowTintFilter highlightShadowTintFilter1 = new GPUImageHighlightShadowTintFilter();
        highlightShadowTintFilter1.setmShadowTintColorRed(0, item.getGreen2() / 100, 0);
        highlightShadowTintFilter1.setmHighlightTintColorRed(0, item.getGreen0() / 100, 0);
        filterArrayList.add(highlightShadowTintFilter1);

        GPUImageHighlightShadowTintFilter highlightShadowTintFilter2 = new GPUImageHighlightShadowTintFilter();
        highlightShadowTintFilter2.setmShadowTintColorRed(0, 0, item.getBlue2() / 100);
        highlightShadowTintFilter2.setmHighlightTintColorRed(0, 0, item.getBlue0() / 100);
        filterArrayList.add(highlightShadowTintFilter2);

        GPUImageExposureFilter gpuImageExposureFilter = new GPUImageExposureFilter(item.getExposure() / 100);
        filterArrayList.add(gpuImageExposureFilter);

        GPUImageGammaFilter gpuImageGammaFilter = new GPUImageGammaFilter(item.getGamma() / 100);
        filterArrayList.add(gpuImageGammaFilter);

        GPUImageWhiteBalanceFilter gpuImageWhiteBalanceFilter = new GPUImageWhiteBalanceFilter(item.getWhiteBalance() * 100, 0.0f);
        filterArrayList.add(gpuImageWhiteBalanceFilter);

        GPUImageContrastFilter gpuImageContrastFilter = new GPUImageContrastFilter(item.getContrast() / 100);
        filterArrayList.add(gpuImageContrastFilter);

        GPUImageSaturationFilter gpuImageSaturationFilter = new GPUImageSaturationFilter(item.getSaturation() / 100);
        filterArrayList.add(gpuImageSaturationFilter);

        GPUImageSharpenFilter gpuImageSharpenFilter = new GPUImageSharpenFilter(item.getSharpen() / 100);
        filterArrayList.add(gpuImageSharpenFilter);

        GPUImageHighlightShadowFilter gpuImageHighlightShadowFilter = new GPUImageHighlightShadowFilter(item.getShadow() / 100, item.getHighlight() / 100);
        filterArrayList.add(gpuImageHighlightShadowFilter);

        GPUImageBrightnessFilter gpuImageBrightnessFilter = new GPUImageBrightnessFilter(item.getFade() / 100);
        filterArrayList.add(gpuImageBrightnessFilter);
        return filterArrayList;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10000) {
            if (resultCode == RESULT_OK) {
                // set the selected filter
                String filterSelect = data.getStringExtra("filter");
                GPUImageFilterTools.FilterType filterType = GPUImageFilterTools.FilterType.valueOf(filterSelect);
                switchFilterTo(GPUImageFilterTools.createFilterForType(this, filterType));
            }
        }
    }

    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
//            mGPUImage.setFilter(mFilter);
            GPUImageFilterGroup filterGroup = new GPUImageFilterGroup();
            filterGroup.addFilter(new GPUImageExtTexFilter());
//
//            // TODO test only
////            ArrayList<GPUImageFilter> list = generateGPUImageFilter(FilterBean.formatFromJsonStr(FilterBean.TEST_YIDE_2));
////            for (int i = 0; i< list.size(); ++i) {
////                filterGroup.addFilter(list.get(i));
////            }
//
//            GPUImageLookupFilter lookupFilter = new GPUImageLookupFilter();
//            lookupFilter.setBitmap(BitmapFactory.decodeResource(getResources(), R.raw.overlaymap));
//            filterGroup.addFilter(lookupFilter);
            filterGroup.addFilter(mFilter);
            videoSurfaceView.setFilter(filterGroup);
            mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(mFilter);
        }
    }

    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress,
                                  final boolean fromUser) {
        if (mFilterAdjuster != null) {
            mFilterAdjuster.adjust(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {
    }

    public void initGLView() {
        RelativeLayout rlGlViewContainer = (RelativeLayout)findViewById(R.id.rlGlViewContainer);
        mediaPlayer = MediaPlayer.create(this, R.raw.we_chat_sight723);
//        mediaPlayer = new MediaPlayer();
//
//        try {
//            HashMap<String ,String> headersMap = new HashMap<String, String>();
//            headersMap.put("User-Agent", "test-hou");
//            headersMap.put("SelfDefineKey", "self-hou");
//            mediaPlayer.setDataSource(this, Uri.parse("http://172.18.24.100:8000/3110.mp4"), headersMap);
//            mediaPlayer.prepare();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        videoSurfaceView = new VideoSurfaceView(this, mediaPlayer);
        videoSurfaceView.setSourceSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());

        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(-1, -1);
        rlGlViewContainer.addView(videoSurfaceView, rllp);
    }

    static public interface ResultListener {
        void onResult(boolean success, String extra);
    }

    @Override
    protected void onPause() {
        // do sth
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // do sth
        mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        super.onDestroy();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();
}
