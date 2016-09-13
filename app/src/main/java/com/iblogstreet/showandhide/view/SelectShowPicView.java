package com.iblogstreet.showandhide.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.iblogstreet.showandhide.R;
import com.iblogstreet.showandhide.view.photo.BitmapUtil;
import com.iblogstreet.showandhide.view.photo.CropHandler;
import com.iblogstreet.showandhide.view.photo.CropHelper;
import com.iblogstreet.showandhide.view.photo.CropParams;
import com.polites.android.GestureImageView;

/*
 *  @项目名：  ShowAndHide 
 *  @包名：    com.iblogstreet.showandhide.view
 *  @文件名:   SelectShowPicView
 *  @创建者:   王军
 *  @创建时间:  2016/9/13 14:59
 *  @描述：    这个主要用于图层展示，选择图片，并裁剪
 */
public class SelectShowPicView
        extends RelativeLayout
        implements CropHandler, View.OnClickListener
{

    private static final String TAG = "SelectShowPicView";
    /**
     * 显示或隐藏按钮
     */
    private Button mBtnShowOrHide;
    /**
     *拍照按钮
     */
    private Button mBtnTakePhoto;
    /**
     * 显示结果图片
     */
    GestureImageView mIvface;
    /**
     * 表情
     */
    private ImageView  mIvEmoji;
    /**
     * 裁剪参数
     */
    private CropParams mCropParams;
    private boolean resultStaus = true;
    private Context mContext;


    public interface OnSelectShowPicViewListener {
        void showResult(Boolean resultStatus);

        void startActivityResult(Intent intent, int requestCode);

    }
    public void handleResult(int requestCode,int resultCode,Intent data){
        Log.e(TAG,"handleResult");
        CropHelper.handleResult(this, requestCode, resultCode, data);
    }

    private OnSelectShowPicViewListener mOnSelectShowPicViewListener;

    public void setOnSelectShowPicViewListener(OnSelectShowPicViewListener onSelectShowPicViewListener) {
        this.mOnSelectShowPicViewListener = onSelectShowPicViewListener;
    }

    public SelectShowPicView(Context context) {
        this(context, null);
    }
    public SelectShowPicView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SelectShowPicView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext=context;
        initView();
        initEvent();
        initData();
        //获取自定义属性
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                                                      R.styleable.SelectShowPicViewAttrs,
                                                      defStyle,
                                                      0);

        int count = a.getIndexCount();
        Log.e(TAG,"count:"+count);
        for (int i = 0; i < count; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.SelectShowPicViewAttrs_SSPVResultStaus:
                    Log.e(TAG,a.getBoolean(attr,true)+"");
                    resultStaus=a.getBoolean(attr,true);
                    break;
                case R.styleable.SelectShowPicViewAttrs_SSPVShowOrHideText:
                    Log.e(TAG,a.getString(attr));
                    mBtnShowOrHide.setText(a.getString(attr));
                    break;
                case R.styleable.SelectShowPicViewAttrs_SSPVTakePhotoText:
                    Log.e(TAG,a.getString(attr));
                    mBtnTakePhoto.setText(a.getString(attr));
                    break;
                case R.styleable.SelectShowPicViewAttrs_SSPVBackGround:
                   int resId= a.getColor(attr,-1);
                    if(resId!=-1)
                      setBackgroundColor(resId);
                    break;
                default:
                    break;
            }
        }
        a.recycle();
    }



    private void initView() {
        //初始化布局
        LayoutInflater.from(getContext())
                      .inflate(R.layout.view_select_show_pic, this);
        mBtnTakePhoto = (Button) findViewById(R.id.btn_take_photo);
        mBtnShowOrHide = (Button) findViewById(R.id.btn_show_or_hide);
        mIvface = (GestureImageView) findViewById(R.id.iv_face);
        mIvEmoji = (ImageView) findViewById(R.id.iv_emoji);
    }

    private void initEvent() {
        mBtnShowOrHide.setOnClickListener(this);
        mBtnTakePhoto.setOnClickListener(this);
    }

    private void initData() {
        mCropParams = new CropParams(getContext());
    }

   private void hide() {
        mBtnTakePhoto.setVisibility(View.GONE);
        mIvEmoji.setVisibility(View.GONE);
        mIvface.setVisibility(View.VISIBLE);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_show_or_hide:
                resultStaus = !resultStaus;
                if (mOnSelectShowPicViewListener != null) {
                    mOnSelectShowPicViewListener.showResult(resultStaus);
                }
                break;
            case R.id.btn_take_photo:
                takePhoto();
                break;
            default:
                break;
        }
    }

    /**
     * 选择拍照
     */
    private void takePhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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

    /**
     * 从相册里选择照片
     */
    private void startGalleryPick() {
        mCropParams.enable = true;
        mCropParams.compress = false;
        Intent intent = CropHelper.buildGalleryIntent(mCropParams);
        //startActivityForResult(intent, CropHelper.REQUEST_CROP);
        if (mOnSelectShowPicViewListener != null) {
            mOnSelectShowPicViewListener.startActivityResult(intent, CropHelper.REQUEST_CROP);
        }
    }

    /**
     * 选择图片裁剪
     */
    private void startImagePick() {
        mCropParams.enable = true;
        mCropParams.compress = false;
        Intent intent = CropHelper.buildCameraIntent(mCropParams);
        // startActivityForResult(intent, CropHelper.REQUEST_CAMERA);
        if (mOnSelectShowPicViewListener != null) {
            mOnSelectShowPicViewListener.startActivityResult(intent, CropHelper.REQUEST_CAMERA);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        CropHelper.clearCacheDir();
    }

    @Override
    public CropParams getCropParams() {
        return mCropParams;
    }

    @Override
    public void onPhotoCropped(Uri uri) {
        Log.d(TAG, "Crop Uri in path: " + uri.getPath());
        if (!mCropParams.compress) {
            mIvface.setImageBitmap(BitmapUtil.decodeUriAsBitmap(mContext, uri));
            hide();
        }
    }

    @Override
    public void onCompressed(Uri uri) {
        // Compressed uri
        mIvface.setImageBitmap(BitmapUtil.decodeUriAsBitmap(mContext, uri));
        hide();
    }

    @Override
    public void handleIntent(Intent intent, int requestCode) {
        //startActivityForResult(intent, requestCode);
        if (mOnSelectShowPicViewListener != null) {
            mOnSelectShowPicViewListener.startActivityResult(intent,requestCode);
        }
    }

    @Override
    public void onCancel() {
        Toast.makeText(mContext, "Crop canceled!", Toast.LENGTH_LONG)
             .show();
    }

    @Override
    public void onFailed(String message) {
        Toast.makeText(mContext, "Crop failed: " + message, Toast.LENGTH_LONG)
             .show();
    }
}
