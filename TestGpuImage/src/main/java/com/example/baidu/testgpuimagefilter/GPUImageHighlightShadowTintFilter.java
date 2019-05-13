package com.example.baidu.testgpuimagefilter;

import android.opengl.GLES20;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

import static android.R.attr.alpha;

/**
 * Created by chad
 * Time 17/2/14
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class GPUImageHighlightShadowTintFilter extends GPUImageFilter {

    public static final String HIGHLIGHT_SHADOW_TINT_FRGAMENT_SHADER = "" +
            "varying vec2 textureCoordinate;\n" +
            "  \n" +
            "uniform sampler2D inputImageTexture;\n" +
            "uniform float shadowTintIntensity;\n" +
            "uniform float highlightTintIntensity;\n" +
            "uniform vec3 shadowTintColor;\n" +
            "uniform vec3 highlightTintColor;\n" +
            "  \n" +
            "const vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "       vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "       float luminance = dot(textureColor.rgb, luminanceWeighting);\n" +
            "  \n" +
            "       vec4 shadowResult = mix(textureColor, max(textureColor, vec4( mix(shadowTintColor.rgb, textureColor.rgb, luminance), textureColor.a)), shadowTintIntensity);\n" +
            "       vec4 highlightResult = mix(textureColor, min(shadowResult, vec4( mix(shadowResult.rgb, highlightTintColor.rgb, luminance), textureColor.a)), highlightTintIntensity);\n" +
            "  \n" +
            "       gl_FragColor = vec4( mix(shadowResult.rgb, highlightResult.rgb, luminance), textureColor.a);\n" +
            " }";

    private int mShadowTintIntensityLocation;
    private float mShadowTintIntensity;
    private int mHighlightTintIntensityLocation;
    private float mHighlightTintIntensity;
    private int mShadowTintColorLocation;
    private float[] mShadowTintColor;
    private int mHighlightTintColorLocation;
    private float[] mHighlightTintColor;

    public GPUImageHighlightShadowTintFilter() {
        this(0.0f, 0.0f, new float[]{1,1,1}, new float[]{1,1,1});
    }

    public GPUImageHighlightShadowTintFilter(final float shadowTintIntensity, final float highlightTintIntentsity, final float[] shadowTintColor, final float[] highlightTintColor) {
        super(NO_FILTER_VERTEX_SHADER, HIGHLIGHT_SHADOW_TINT_FRGAMENT_SHADER);
        mShadowTintIntensity = shadowTintIntensity;
        mHighlightTintIntensity = highlightTintIntentsity;
        mShadowTintColor = shadowTintColor;
        mHighlightTintColor = highlightTintColor;
    }

    @Override
    public void onInit() {
        super.onInit();
        mShadowTintIntensityLocation = GLES20.glGetUniformLocation(getProgram(), "shadowTintIntensity");
        mHighlightTintIntensityLocation = GLES20.glGetUniformLocation(getProgram(), "highlightTintIntensity");
        mShadowTintColorLocation = GLES20.glGetUniformLocation(getProgram(), "shadowTintColor");
        mHighlightTintColorLocation = GLES20.glGetUniformLocation(getProgram(), "highlightTintColor");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setmShadowTintIntensity(mShadowTintIntensity);
        setmHighlightTintIntensity(mHighlightTintIntensity);
        setmHighlightTintColor(mHighlightTintColor);
        setmShadowTintColor(mShadowTintColor);
        setmShadowTintColorRed(1.0f,1.0f,1.0f);
        setmHighlightTintColorRed(1.0f,1.0f,1.0f);
    }

    public void setmShadowTintIntensity(final float shadowTintIntensity) {
        mShadowTintIntensity = shadowTintIntensity;
        setFloat(mShadowTintIntensityLocation, mShadowTintIntensity);
    }

    public void setmHighlightTintIntensity(final float highlightTintIntentsity) {
        mHighlightTintIntensity = highlightTintIntentsity;
        setFloat(mHighlightTintIntensityLocation, mHighlightTintIntensity);
    }

    // FIXME delete following methods
    public void setmShadowTintColor(final float[] shadowTintColor) {
        mShadowTintColor = shadowTintColor;
        setFloatVec3(mShadowTintColorLocation, mShadowTintColor);
    }

    // FIXME delete following methods
    public void setmHighlightTintColor(final float[] highlightTintColor) {
        mHighlightTintColor = highlightTintColor;
        setFloatVec3(mHighlightTintColorLocation, mHighlightTintColor);
    }

    public void setmShadowTintColorRed(float red, float green, float blue) {
        float[] floats = {red,green,blue};
        setFloatVec3(mShadowTintColorLocation, floats);
    }

    public void setmHighlightTintColorRed(float red, float green, float blue) {
        float[] floats = {red,green,blue};
        setFloatVec3(mHighlightTintColorLocation, floats);
    }


}
