package com.iblogstreet.showandhide.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
        extends LinearLayout
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
    /**
     * 是否展开
     */
    private boolean isOpened = true;
    private Context mContext;

    private View mStretchView;
    private int mStretchViewLayoutId = -1;
    private int mStretchHeight;

    private View mContentView;
    private int mContentViewLayoutId = -1;
    /**
     *  是否显示拍照按钮
     */
    private boolean showTakePhotoIcon=true;

    private String mExpandText;

    private String mShrinkText;

    private String mBtnTakePhotoText;

    private int mAnimationDuration = 300;

    public interface OnSelectShowPicViewListener {

        void startActivityResult(Intent intent, int requestCode);

        /**
         * 伸缩动画结束的监听事件
         * @param isOpened
         */
        void onStretchFinished(boolean isOpened);

    }

    public void handleResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "handleResult");
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
        mContext = context;
        setOrientation(LinearLayout.VERTICAL);
        //获取自定义属性
        TypedArray a = context.getTheme()
                              .obtainStyledAttributes(attrs,
                                                      R.styleable.SelectShowPicViewAttrs,
                                                      defStyle,
                                                      0);
        int count = a.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.SelectShowPicViewAttrs_SSPVIsOpen:
                    isOpened = a.getBoolean(attr, true);
                    break;
                case R.styleable.SelectShowPicViewAttrs_SSPVTakePhotoText:
                    Log.e(TAG, a.getString(attr)+"SSPVTakePhotoText");
                    mBtnTakePhotoText = a.getString(attr);
                    break;
                case R.styleable.SelectShowPicViewAttrs_SSPVBackGround:
                    int resId = a.getColor(attr, -1);
                    if (resId != -1) { setBackgroundColor(resId); }
                    break;
                case R.styleable.SelectShowPicViewAttrs_SSPVStretchView:
                    mStretchViewLayoutId = a.getResourceId(attr, -1);
                    if (mStretchViewLayoutId > 0) {
                        View view = View.inflate(context, mStretchViewLayoutId, null);
                        setStretchView(view);
                    }
                    break;
                case R.styleable.SelectShowPicViewAttrs_SSPVContentView:
                    mContentViewLayoutId = a.getResourceId(attr, -1);
                    if (mContentViewLayoutId > 0) {
                        View view = View.inflate(context, mContentViewLayoutId, null);
                        setContentView(view);
                    } else {
                        throw new RuntimeException("SSPVContentView必须设置");
                    }
                    break;

                case R.styleable.SelectShowPicViewAttrs_SSPVShowTakePhotoIcon:
                    showTakePhotoIcon = a.getBoolean(attr, true);
                    break;

                case R.styleable.SelectShowPicViewAttrs_SSPVShowOrHideExpandText:
                    mExpandText = a.getString(attr);
                    break;
                case R.styleable.SelectShowPicViewAttrs_SSPVShowOrHideShrinkText:
                    mShrinkText = a.getString(attr);
                    break;
                default:
                    break;
            }
        }
        if (mContentView != null) {
            initView();
            initEvent();
            initData();
        }
        a.recycle();
    }

    private void initView() {
        //初始化布局
        //        LayoutInflater.from(getContext())
        //                      .inflate(R.layout.view_select_show_pic, this);
        mBtnTakePhoto = (Button) findViewById(R.id.btn_take_photo);
        if(!TextUtils.isEmpty(mBtnTakePhotoText))
            mBtnTakePhoto.setText(mBtnTakePhotoText);
        mBtnTakePhoto.setVisibility(showTakePhotoIcon
                                    ? View.VISIBLE
                                    : View.GONE);

        mBtnShowOrHide = (Button) findViewById(R.id.btn_show_or_hide);
        if (isOpened) {
            mBtnShowOrHide.setText(mShrinkText);
        } else {
            mBtnShowOrHide.setText(mExpandText);
        }
        mIvface = (GestureImageView) findViewById(R.id.iv_face);
        mIvEmoji = (ImageView) findViewById(R.id.iv_emoji);
        // mContentView.setVisibility(View.GONE);
    }

    /**
     * 设置伸展View
     * @param view
     */
    public void setStretchView(View view) {
        if (view != null) {
            if (this.mStretchView != null) {
                removeView(this.mStretchView);
                // 在重新设置时，将该值置为0，否则新view将不能显示正确的高度
                this.mStretchHeight = 0;
            }
            this.mStretchView = view;
            addView(mStretchView);
        }
    }

    /**

     * 设置主View

     * @param view

     */
    public void setContentView(View view) {
        if (view != null) {
            if (this.mContentView != null) {
                removeView(this.mContentView);
            }
            this.mContentView = view;
            addView(mContentView, 0);
        }
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mStretchHeight == 0 && mStretchView != null) {
            mStretchView.measure(widthMeasureSpec, heightMeasureSpec);
            mStretchHeight = mStretchView.getMeasuredHeight();
            //mStretchView.getLayoutParams().height=getHeight()-mStretchHeight;
            if(!isOpened)
              mStretchView.getLayoutParams().height = 0;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_show_or_hide:
                Log.e(TAG, "isOpened:" + isOpened);
                mBtnShowOrHide.setText(isOpened
                                       ? mExpandText
                                       : mShrinkText);
                if (isOpened) {
                    closeStretchView();

                } else {
                    openStretchView();
                }
                break;
            case R.id.btn_take_photo:
                takePhoto();
                break;
            default:
                break;
        }
    }

    private void openStretchView() {
        if (mStretchView != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    StretchAnimation animation = new StretchAnimation(0, mStretchHeight);
                    animation.setDuration(mAnimationDuration);
                    animation.setAnimationListener(animationListener);
                    mStretchView.startAnimation(animation);
                    invalidate();
                }
            });
        }
    }

    private void closeStretchView() {
        if (mStretchView != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    StretchAnimation animation = new StretchAnimation(mStretchHeight, 0);
                    animation.setDuration(mAnimationDuration);
                    animation.setAnimationListener(animationListener);
                    mStretchView.startAnimation(animation);
                    invalidate();
                }
            });
        }
    }

    private Animation.AnimationListener animationListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {
            Log.e(TAG, "onAnimationStart");
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Log.e(TAG, "onAnimationEnd");
            isOpened = !isOpened;
            if (mOnSelectShowPicViewListener != null) {
                mOnSelectShowPicViewListener.onStretchFinished(isOpened);
            }
        }
    };

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
            mOnSelectShowPicViewListener.startActivityResult(intent, requestCode);
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

    /**

     * 伸缩动画

     */
    private class StretchAnimation
            extends Animation
    {
        private int startHeight;
        private int deltaHeight;

        public StretchAnimation(int startH, int endH) {
            this.startHeight = startH;
            this.deltaHeight = endH - startH;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (mStretchView != null) {
                LayoutParams params = (LayoutParams) mStretchView.getLayoutParams();
                params.height = (int) (startHeight + deltaHeight * interpolatedTime);
                mStretchView.setLayoutParams(params);
            }
        }
    }
}
