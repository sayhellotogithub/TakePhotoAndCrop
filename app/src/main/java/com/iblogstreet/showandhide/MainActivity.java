package com.iblogstreet.showandhide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.iblogstreet.showandhide.view.photo.BitmapUtil;
import com.iblogstreet.showandhide.view.photo.CropHandler;
import com.iblogstreet.showandhide.view.photo.CropHelper;
import com.iblogstreet.showandhide.view.photo.CropParams;
import com.polites.android.GestureImageView;

public class MainActivity
        extends Activity
        implements View.OnClickListener, CropHandler
{
    private static final String TAG = "MainActivity";
    private Button       mBtnShowOrHide;
    private Button       mBtnTakPhoto;
    private LinearLayout mLlResult;
    GestureImageView mIvface;
    private ImageView  mIvEmoji;
    private CropParams mCropParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initEvent();
    }


    private void initData() {
        mCropParams = new CropParams(this);
    }

    private void initEvent() {
        mBtnShowOrHide.setOnClickListener(this);
        mBtnTakPhoto.setOnClickListener(this);
    }

    boolean resultStaus = false;

    private void initView() {
        mBtnTakPhoto = (Button) findViewById(R.id.btn_take_photo);
        mBtnShowOrHide = (Button) findViewById(R.id.btn_show_or_hide);
        mLlResult = (LinearLayout) findViewById(R.id.ll_result);
        mIvface = (GestureImageView) findViewById(R.id.iv_face);
        mIvEmoji = (ImageView) findViewById(R.id.iv_emoji);
    }
    void hide(){
        mBtnTakPhoto.setVisibility(View.GONE);
        mIvEmoji.setVisibility(View.GONE);
        mIvface.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        Log.e("onClick", "btn_take_photo");
        switch (v.getId()) {
            case R.id.btn_show_or_hide:
                resultStaus = !resultStaus;
                mLlResult.setVisibility(resultStaus
                                        ? View.VISIBLE
                                        : View.GONE);
                break;
            case R.id.btn_take_photo:
                Log.e("onClick", "btn_take_photo");
                takePhoto();
                break;
            default:
                break;
        }
    }

    private void takePhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择图片");
        String[] avatar = getResources().getStringArray(R.array.choose_picture);
        builder.setItems(avatar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //相册
                    startImagePick();
                } else {
                    //相机
                    startGalleryPick();
                }
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("取消", null);
        builder.show();
    }

    private void startGalleryPick() {
        mCropParams.enable = true;
        mCropParams.compress = false;
        Intent intent = CropHelper.buildGalleryIntent(mCropParams);
        startActivityForResult(intent, CropHelper.REQUEST_CROP);
    }

    /**
     * 选择图片裁剪
     */
    private void startImagePick() {
        mCropParams.enable = true;
        mCropParams.compress = false;
        Intent intent = CropHelper.buildCameraIntent(mCropParams);
        startActivityForResult(intent, CropHelper.REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CropHelper.handleResult(this, requestCode, resultCode, data);
        if (requestCode == 1) {
            Log.e(TAG, "");
        }
    }

    @Override
    protected void onDestroy() {
        CropHelper.clearCacheDir();
        super.onDestroy();
    }

    @Override
    public CropParams getCropParams() {
        return mCropParams;
    }

    @Override
    public void onPhotoCropped(Uri uri) {
        Log.d(TAG, "Crop Uri in path: " + uri.getPath());
        if (!mCropParams.compress) {
            mIvface.setImageBitmap(BitmapUtil.decodeUriAsBitmap(this, uri));
            hide();
        }
    }

    @Override
    public void onCompressed(Uri uri) {
        // Compressed uri
        mIvface.setImageBitmap(BitmapUtil.decodeUriAsBitmap(this, uri));
        hide();
    }

    @Override
    public void handleIntent(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCancel() {
        Toast.makeText(this, "Crop canceled!", Toast.LENGTH_LONG)
             .show();
    }
    @Override
    public void onFailed(String message) {
        Toast.makeText(this, "Crop failed: " + message, Toast.LENGTH_LONG)
             .show();
    }
}
