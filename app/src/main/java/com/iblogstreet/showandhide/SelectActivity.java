package com.iblogstreet.showandhide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.iblogstreet.showandhide.view.SelectShowPicView;

public class SelectActivity
        extends Activity implements SelectShowPicView.OnSelectShowPicViewListener
{
    private static final String TAG = "MainActivity";
    private LinearLayout mLlResult;
    SelectShowPicView mSspv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_select);
        initView();
        initData();
        initEvent();
    }
    private void initData() {
    }

    private void initEvent() {
        mSspv.setOnSelectShowPicViewListener(this);
    }
    private void initView() {
        mLlResult = (LinearLayout) findViewById(R.id.ll_result);
        mSspv= (SelectShowPicView) findViewById(R.id.sspv);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSspv.handleResult(requestCode,resultCode,data);
        if (requestCode == 1) {
            Log.e(TAG, "handleResult");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showResult(Boolean resultStatus) {
        mLlResult.setVisibility(resultStatus
                                ? View.VISIBLE
                                : View.GONE);
    }

    @Override
    public void startActivityResult(Intent intent, int requestCode) {
        startActivityForResult(intent,requestCode);
    }
}
