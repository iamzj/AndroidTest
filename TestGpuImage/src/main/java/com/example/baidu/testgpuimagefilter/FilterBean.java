package com.example.baidu.testgpuimagefilter;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by baidu on 2017/3/31.
 */

public class FilterBean  implements Parcelable {

    public static final String TAG = "FilterBean";

    public FilterBean() {

    }
    protected FilterBean(Parcel in) {
        id = in.readInt();
        openId = in.readString();
        filterName = in.readString();
        table_key = in.readString();
        red0 = in.readFloat();
        red1 = in.readFloat();
        red2 = in.readFloat();
        blue0 = in.readFloat();
        blue1 = in.readFloat();
        blue2 = in.readFloat();
        green0 = in.readFloat();
        green1 = in.readFloat();
        green2 = in.readFloat();
        exposure = in.readFloat();
        highlight = in.readFloat();
        contrast = in.readFloat();
        whiteBalance = in.readFloat();
        gamma = in.readFloat();
        shadow = in.readFloat();
        sharpen = in.readFloat();
        fade = in.readFloat();
        noise = in.readFloat();
        saturation = in.readFloat();
    }

    public static final String TEST_AONE_1_JSON_STR = "" +
            "{\n" +
            "      \"id\": 11, \n" +
            "      \"filterName\": \"Aone-1\", \n" +
            "      \"exposure\": -19, \n" +
            "      \"gamma\": 120, \n" +
            "      \"whiteBalance\": 59, \n" +
            "      \"contrast\": 122, \n" +
            "      \"saturation\": 79, \n" +
            "      \"sharpen\": 39, \n" +
            "      \"shadow\": 0, \n" +
            "      \"highlight\": 14, \n" +
            "      \"noise\": 0, \n" +
            "      \"fade\": 17, \n" +
            "      \"red0\": 10, \n" +
            "      \"red1\": 120, \n" +
            "      \"red2\": 0, \n" +
            "      \"green0\": -23, \n" +
            "      \"green1\": 108, \n" +
            "      \"green2\": 0, \n" +
            "      \"blue0\": 13, \n" +
            "      \"blue1\": 111, \n" +
            "      \"blue2\": 0, \n" +
            "      \"openId\": \"\", \n" +
            "      \"table_key\": \"\"\n" +
            "    }";
    public static final String TEST_YIDE_2 = "" +
            "{\n" +
            "      \"id\": 20, \n" +
            "      \"filterName\": \"yide-2\", \n" +
            "      \"exposure\": 2, \n" +
            "      \"gamma\": 100, \n" +
            "      \"whiteBalance\": 55, \n" +
            "      \"contrast\": 102, \n" +
            "      \"saturation\": 104, \n" +
            "      \"sharpen\": 0, \n" +
            "      \"shadow\": 37, \n" +
            "      \"highlight\": 35, \n" +
            "      \"noise\": 0, \n" +
            "      \"fade\": 1, \n" +
            "      \"red0\": 0, \n" +
            "      \"red1\": 111, \n" +
            "      \"red2\": 0, \n" +
            "      \"green0\": 0, \n" +
            "      \"green1\": 108, \n" +
            "      \"green2\": 0, \n" +
            "      \"blue0\": 0, \n" +
            "      \"blue1\": 81, \n" +
            "      \"blue2\": 0, \n" +
            "      \"createTime\": 1486469271000, \n" +
            "      \"updateTime\": 1486541327000, \n" +
            "      \"openId\": \"\", \n" +
            "      \"table_key\": \"\"\n" +
            "    }";

    public static FilterBean formatFromJsonStr(String jsonStr) {
        FilterBean oneBean = new FilterBean();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            oneBean.setBlue0((float)jsonObject.getDouble("blue0"))
                    .setBlue1((float)jsonObject.getDouble("blue1"))
                    .setBlue2((float)jsonObject.getDouble("blue2"))
                    .setContrast((float)jsonObject.getDouble("contrast"))
                    .setExposure((float)jsonObject.getDouble("exposure"))
                    .setFade((float)jsonObject.getDouble("fade"))
                    .setGamma((float)jsonObject.getDouble("gamma"))
                    .setGreen0((float)jsonObject.getDouble("green0"))
                    .setGreen1((float)jsonObject.getDouble("green1"))
                    .setGreen2((float)jsonObject.getDouble("green2"))
                    .setHighlight((float)jsonObject.getDouble("highlight"))
                    .setId(jsonObject.getInt("id"))
                    .setNoise((float)jsonObject.getDouble("noise"))
                    .setWhiteBalance((float)jsonObject.getDouble("whiteBalance"))
                    .setRed0((float)jsonObject.getDouble("red0"))
                    .setRed1((float)jsonObject.getDouble("red1"))
                    .setRed2((float)jsonObject.getDouble("red2"))
                    .setSaturation((float)jsonObject.getDouble("saturation"))
                    .setShadow((float)jsonObject.getDouble("shadow"))
                    .setSharpen((float)jsonObject.getDouble("sharpen"))
                    .setOpenId(jsonObject.getString("openId"))
                    .setTable_key(jsonObject.getString("table_key"))
                    .setFilterName(jsonObject.getString("filterName"))
                    ;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, oneBean.toString());

        return oneBean;
    }

    public static final Creator<FilterBean> CREATOR = new Creator<FilterBean>() {
        @Override
        public FilterBean createFromParcel(Parcel in) {
            return new FilterBean(in);
        }

        @Override
        public FilterBean[] newArray(int size) {
            return new FilterBean[size];
        }
    };

    @Override
    public String toString() {
        return "FilterBean{" +
                "exposure=" + exposure +
                ", whiteBalance=" + whiteBalance +
                ", filterName='" + filterName + '\'' +
                ", red1=" + red1 +
                ", red2=" + red2 +
                ", highlight=" + highlight +
                ", contrast=" + contrast +
                ", id=" + id +
                ", gamma=" + gamma +
                ", blue1=" + blue1 +
                ", table_key='" + table_key + '\'' +
                ", blue0=" + blue0 +
                ", shadow=" + shadow +
                ", green2=" + green2 +
                ", green1=" + green1 +
                ", green0=" + green0 +
                ", sharpen=" + sharpen +
                ", blue2=" + blue2 +
                ", fade=" + fade +
                ", noise=" + noise +
                ", red0=" + red0 +
                ", saturation=" + saturation +
                ", openId='" + openId + '\'' +
                '}';
    }

    private int id;

    private String openId;

    private String filterName;

    private String table_key;

    private float red0;

    private float red1;

    private float red2;

    private float blue0;

    private float blue1;

    private float blue2;

    private float green0;

    private float green1;

    private float green2;

    private float exposure;//曝光

    private float highlight;//高亮

    private float contrast;//对比度

    private float whiteBalance;//白平衡

    private float gamma;//伽马

    private float shadow;//阴影

    private float sharpen;//锐化

    private float fade;//褪色

    private float noise;//噪声

    private float saturation;//饱和度

    public FilterBean setExposure(float exposure) {
        this.exposure = exposure;
        return this;
    }

    public float getExposure() {
        return this.exposure;
    }

    public FilterBean setWhiteBalance(float whiteBalance) {
        this.whiteBalance = whiteBalance;
        return this;
    }

    public float getWhiteBalance() {
        return this.whiteBalance;
    }

    public FilterBean setFilterName(String filterName) {
        this.filterName = filterName;
        return this;
    }

    public String getFilterName() {
        return this.filterName;
    }

    public FilterBean setRed1(float red1) {
        this.red1 = red1;
        return this;
    }

    public float getRed1() {
        return this.red1;
    }

    public FilterBean setRed2(float red2) {
        this.red2 = red2;
        return this;
    }

    public float getRed2() {
        return this.red2;
    }

    public FilterBean setHighlight(float highlight) {
        this.highlight = highlight;
        return this;
    }

    public float getHighlight() {
        return this.highlight;
    }

    public FilterBean setContrast(float contrast) {
        this.contrast = contrast;
        return this;
    }

    public float getContrast() {
        return this.contrast;
    }

    public FilterBean setId(int id) {
        this.id = id;
        return this;
    }

    public int getId() {
        return this.id;
    }

    public FilterBean setGamma(float gamma) {
        this.gamma = gamma;
        return this;
    }

    public float getGamma() {
        return this.gamma;
    }

    public FilterBean setBlue1(float blue1) {
        this.blue1 = blue1;
        return this;
    }

    public float getBlue1() {
        return this.blue1;
    }

    public FilterBean setTable_key(String table_key) {
        this.table_key = table_key;
        return this;
    }

    public String getTable_key() {
        return this.table_key;
    }

    public FilterBean setBlue0(float blue0) {
        this.blue0 = blue0;
        return this;
    }

    public float getBlue0() {
        return this.blue0;
    }

    public FilterBean setShadow(float shadow) {
        this.shadow = shadow;
        return this;
    }

    public float getShadow() {
        return this.shadow;
    }

    public FilterBean setGreen2(float green2) {
        this.green2 = green2;
        return this;
    }

    public float getGreen2() {
        return this.green2;
    }

    public FilterBean setGreen1(float green1) {
        this.green1 = green1;
        return this;
    }

    public float getGreen1() {
        return this.green1;
    }

    public FilterBean setGreen0(float green0) {
        this.green0 = green0;
        return this;
    }

    public float getGreen0() {
        return this.green0;
    }

    public FilterBean setSharpen(float sharpen) {
        this.sharpen = sharpen;
        return this;
    }

    public float getSharpen() {
        return this.sharpen;
    }

    public FilterBean setBlue2(float blue2) {
        this.blue2 = blue2;
        return this;
    }

    public float getBlue2() {
        return this.blue2;
    }

    public FilterBean setFade(float fade) {
        this.fade = fade;
        return this;
    }

    public float getFade() {
        return this.fade;
    }

    public FilterBean setNoise(float noise) {
        this.noise = noise;
        return this;
    }

    public float getNoise() {
        return this.noise;
    }

    public FilterBean setRed0(float red0) {
        this.red0 = red0;
        return this;
    }

    public float getRed0() {
        return this.red0;
    }

    public FilterBean setSaturation(float saturation) {
        this.saturation = saturation;
        return this;
    }

    public float getSaturation() {
        return this.saturation;
    }

    public FilterBean setOpenId(String openId) {
        this.openId = openId;
        return this;
    }

    public String getOpenId() {
        return this.openId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(openId);
        dest.writeString(filterName);
        dest.writeString(table_key);
        dest.writeFloat(red0);
        dest.writeFloat(red1);
        dest.writeFloat(red2);
        dest.writeFloat(blue0);
        dest.writeFloat(blue1);
        dest.writeFloat(blue2);
        dest.writeFloat(green0);
        dest.writeFloat(green1);
        dest.writeFloat(green2);
        dest.writeFloat(exposure);
        dest.writeFloat(highlight);
        dest.writeFloat(contrast);
        dest.writeFloat(whiteBalance);
        dest.writeFloat(gamma);
        dest.writeFloat(shadow);
        dest.writeFloat(sharpen);
        dest.writeFloat(fade);
        dest.writeFloat(noise);
        dest.writeFloat(saturation);
    }

}
