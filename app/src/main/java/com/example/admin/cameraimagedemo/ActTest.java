package com.example.admin.cameraimagedemo;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoActivity;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.LubanOptions;
import com.jph.takephoto.model.TImage;
import com.jph.takephoto.model.TResult;
import com.jph.takephoto.model.TakePhotoOptions;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 图片选择takephoto框架
 * Created by admin on 2018/4/10.
 */
public class ActTest extends TakePhotoActivity {

    @BindView(R.id.btn_photo)
    Button btnPhoto;
    @BindView(R.id.btn_camera)
    Button btnCamera;
    @BindView(R.id.layout_imgs)
    LinearLayout layoutImgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_test);
        ButterKnife.bind(this);
    }

    @Override
    public void takeCancel() {
        super.takeCancel();
    }

    @Override
    public void takeFail(TResult result, String msg) {
        super.takeFail(result, msg);
    }

    //选择好的图片们
    @Override
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);
        showImg(result.getImages());
    }

    //显示图片
    private void showImg(ArrayList<TImage> images) {
        for (int i = 0, j = images.size(); i < j - 1; i += 2) {
            View view = LayoutInflater.from(this).inflate(R.layout.image_show, null);
            ImageView imageView1 = (ImageView) view.findViewById(R.id.imgShow1);
            ImageView imageView2 = (ImageView) view.findViewById(R.id.imgShow2);
            Glide.with(this).load(new File(images.get(i).getCompressPath())).into(imageView1);
            Glide.with(this).load(new File(images.get(i + 1).getCompressPath())).into(imageView2);
            layoutImgs.addView(view);
        }
        if (images.size() % 2 == 1) {
            View view = LayoutInflater.from(this).inflate(R.layout.image_show, null);
            ImageView imageView1 = (ImageView) view.findViewById(R.id.imgShow1);
            Glide.with(this).load(new File(images.get(images.size() - 1).getCompressPath())).into(imageView1);
            layoutImgs.addView(view);
        }
    }

    @OnClick({R.id.btn_photo, R.id.btn_camera})
    public void onViewClicked(View view){
        Boolean isUserOwnGallery = true;//是否使用TakePhoto自带相册
        Boolean isCrop = false;//是否裁剪
        Boolean isCompress = true;//是否压缩

        File file = new File(Environment.getExternalStorageDirectory(), "/temp/" + System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        Uri imageUri = Uri.fromFile(file);

        TakePhoto takePhoto = getTakePhoto();
        TakePhotoOptions.Builder builder = new TakePhotoOptions.Builder();
        if(isUserOwnGallery){
            builder.setWithOwnGallery(true);
        }else {
            builder.setCorrectImage(true);
        }
        if(isCompress){
            configCompress(takePhoto,isCompress);//压缩
        }
        takePhoto.setTakePhotoOptions(builder.create());
        switch (view.getId()) {
            case R.id.btn_photo://相册
                int limit = Integer.parseInt("5");//最多选几张图片
                if (limit > 1) {
                    if(isCrop){
                        takePhoto.onPickMultipleWithCrop(limit, getCropOptions(isCrop));
                    }else {
                        takePhoto.onPickMultiple(limit);
                    }
                    return;
                }
                //从相册、文件选择图片
                Boolean isFromDocument = true;
                if (isFromDocument){
                    if (isCrop) {
                        takePhoto.onPickFromDocumentsWithCrop(imageUri, getCropOptions(isCrop));
                    } else {
                        takePhoto.onPickFromDocuments();
                    }
                    return;
                } else {
                    if (isCrop) {
                        takePhoto.onPickFromGalleryWithCrop(imageUri, getCropOptions(isCrop));
                    } else {
                        takePhoto.onPickFromGallery();
                    }
                }
                break;
            case R.id.btn_camera://相机
                if(isCrop){
                    takePhoto.onPickFromCaptureWithCrop(imageUri, getCropOptions(isCrop));//裁剪
                }else {
                    takePhoto.onPickFromCapture(imageUri);//不裁剪
                }
                break;
        }
    }

    //裁剪
    private CropOptions getCropOptions(boolean isCrop) {
        if (!isCrop) {
            return null;
        }
        int height = Integer.parseInt("800");
        int width = Integer.parseInt("800");
        CropOptions.Builder builder = new CropOptions.Builder();
        //尺寸/比例：
        builder.setAspectX(width).setAspectY(height);//宽/高
//        builder.setOutputX(width).setOutputY(height);//宽x高
        builder.setWithOwnCrop(true);////裁切工具:TakePhoto自带、第三方
        return builder.create();
    }

    //压缩
    private void configCompress(TakePhoto takePhoto,boolean isCompress) {
        if (!isCompress) {
            takePhoto.onEnableCompress(null, false);
            return;
        }
        int maxSize = Integer.parseInt("102400");//单位 B：大小不超过多少B
        int width = Integer.parseInt("800");
        int height = Integer.parseInt("800");//单位 px
        boolean showProgressBar = true;//是否显示进度条
        boolean enableRawFile = true;//拍照压缩后是否保存原图
        CompressConfig config;
        boolean isCompressToolOwn = true;
        if (isCompressToolOwn) {
            config = new CompressConfig.Builder().setMaxSize(maxSize)
                    .setMaxPixel(width >= height ? width : height)
                    .enableReserveRaw(enableRawFile)
                    .create();
        } else {
            LubanOptions option = new LubanOptions.Builder().setMaxHeight(height).setMaxWidth(width).setMaxSize(maxSize).create();
            config = CompressConfig.ofLuban(option);
            config.enableReserveRaw(enableRawFile);
        }
        takePhoto.onEnableCompress(config, showProgressBar);
    }

}
