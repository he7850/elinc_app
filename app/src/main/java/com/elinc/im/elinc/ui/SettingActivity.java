package com.elinc.im.elinc.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.elinc.im.elinc.CustomApplcation;
import com.elinc.im.elinc.R;
import com.elinc.im.elinc.util.SharePreferenceUtil;
import com.elinc.im.elinc.bean.ActivityCollection;

public class SettingActivity extends ActivityBase implements View.OnClickListener {
    Context a;
    Button btn_logout;
    RelativeLayout rl_switch_notification, rl_switch_voice, rl_switch_vibrate,layout_blacklist;
    ImageView iv_open_notification, iv_close_notification, iv_open_voice, iv_close_voice, iv_open_vibrate, iv_close_vibrate;
    View view1,view2;
    SharePreferenceUtil mSharedUtil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_set);
        mSharedUtil = mApplication.getSpUtil();
        initView();
    }

    private void initView() {
        initTopBarForLeft("设置");
        //黑名单列表
        layout_blacklist = (RelativeLayout) findViewById(R.id.layout_blacklist);
        //layout_info = (RelativeLayout) findViewById(R.id.layout_info);
        rl_switch_notification = (RelativeLayout) findViewById(R.id.rl_switch_notification);
        rl_switch_voice = (RelativeLayout) findViewById(R.id.rl_switch_voice);
        rl_switch_vibrate = (RelativeLayout) findViewById(R.id.rl_switch_vibrate);
        rl_switch_notification.setOnClickListener(this);
        rl_switch_voice.setOnClickListener(this);
        rl_switch_vibrate.setOnClickListener(this);

        iv_open_notification = (ImageView) findViewById(R.id.iv_open_notification);
        iv_close_notification = (ImageView) findViewById(R.id.iv_close_notification);
        iv_open_voice = (ImageView) findViewById(R.id.iv_open_voice);
        iv_close_voice = (ImageView) findViewById(R.id.iv_close_voice);
        iv_open_vibrate = (ImageView) findViewById(R.id.iv_open_vibrate);
        iv_close_vibrate = (ImageView) findViewById(R.id.iv_close_vibrate);
        view1 = findViewById(R.id.view1);
        view2 = findViewById(R.id.view2);

        btn_logout = (Button) findViewById(R.id.btn_logout);

        // 初始化
        boolean isAllowNotify = mSharedUtil.isAllowPushNotify();

        if (isAllowNotify) {
            iv_open_notification.setVisibility(View.VISIBLE);
            iv_close_notification.setVisibility(View.INVISIBLE);
        } else {
            iv_open_notification.setVisibility(View.INVISIBLE);
            iv_close_notification.setVisibility(View.VISIBLE);
        }
        boolean isAllowVoice = mSharedUtil.isAllowVoice();
        if (isAllowVoice) {
            iv_open_voice.setVisibility(View.VISIBLE);
            iv_close_voice.setVisibility(View.INVISIBLE);
        } else {
            iv_open_voice.setVisibility(View.INVISIBLE);
            iv_close_voice.setVisibility(View.VISIBLE);
        }
        boolean isAllowVibrate = mSharedUtil.isAllowVibrate();
        if (isAllowVibrate) {
            iv_open_vibrate.setVisibility(View.VISIBLE);
            iv_close_vibrate.setVisibility(View.INVISIBLE);
        } else {
            iv_open_vibrate.setVisibility(View.INVISIBLE);
            iv_close_vibrate.setVisibility(View.VISIBLE);
        }
        btn_logout.setOnClickListener(this);
        layout_blacklist.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_blacklist:// 启动到黑名单页面
                startAnimActivity(new Intent(this,BlackListActivity.class));
                break;
            case R.id.btn_logout:
                CustomApplcation.getInstance().logout();
                Intent intent=new Intent();
                intent.setClass(SettingActivity.this, LoginActivityElinc.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                ActivityCollection.finishAll();

                break;
            case R.id.rl_switch_notification:
                if (iv_open_notification.getVisibility() == View.VISIBLE) {
                    iv_open_notification.setVisibility(View.INVISIBLE);
                    iv_close_notification.setVisibility(View.VISIBLE);
                    mSharedUtil.setPushNotifyEnable(false);
                    rl_switch_vibrate.setVisibility(View.GONE);
                    rl_switch_voice.setVisibility(View.GONE);
                    view1.setVisibility(View.GONE);
                    view2.setVisibility(View.GONE);
                } else {
                    iv_open_notification.setVisibility(View.VISIBLE);
                    iv_close_notification.setVisibility(View.INVISIBLE);
                    mSharedUtil.setPushNotifyEnable(true);
                    rl_switch_vibrate.setVisibility(View.VISIBLE);
                    rl_switch_voice.setVisibility(View.VISIBLE);
                    view1.setVisibility(View.VISIBLE);
                    view2.setVisibility(View.VISIBLE);
                }

                break;
            case R.id.rl_switch_voice:
                if (iv_open_voice.getVisibility() == View.VISIBLE) {
                    iv_open_voice.setVisibility(View.INVISIBLE);
                    iv_close_voice.setVisibility(View.VISIBLE);
                    mSharedUtil.setAllowVoiceEnable(false);
                } else {
                    iv_open_voice.setVisibility(View.VISIBLE);
                    iv_close_voice.setVisibility(View.INVISIBLE);
                    mSharedUtil.setAllowVoiceEnable(true);
                }

                break;
            case R.id.rl_switch_vibrate:
                if (iv_open_vibrate.getVisibility() == View.VISIBLE) {
                    iv_open_vibrate.setVisibility(View.INVISIBLE);
                    iv_close_vibrate.setVisibility(View.VISIBLE);
                    mSharedUtil.setAllowVibrateEnable(false);
                } else {
                    iv_open_vibrate.setVisibility(View.VISIBLE);
                    iv_close_vibrate.setVisibility(View.INVISIBLE);
                    mSharedUtil.setAllowVibrateEnable(true);
                }
                break;
        }
    }
}
