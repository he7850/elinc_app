package com.elinc.im.elinc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.elinc.im.elinc.R;
import com.elinc.im.elinc.bean.User;
import com.elinc.im.elinc.config.BmobConstants;

import java.security.MessageDigest;

import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.listener.SaveListener;

public class FirstUpdateInfoElinc extends BaseActivity {
    private EditText et_nick,et_uni,et_password,et_password_again;
    private RadioGroup sex;
    private RadioGroup campus;
    private RadioButton sex_woman,sex_man,first_zjg,first_zj,first_yq,first_xx,first_hjc,first_zs;
    private Boolean chosenSex;
    private Button confirm;
    private String mobilePhone;
    private String inviter;
    private String chosenCampus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_update_info_elinc);
        mobilePhone = getIntent().getExtras().getString("mobile_phone");//电话号码
        inviter = getIntent().getExtras().getString("inviter");//电话号码
        Log.i("1",mobilePhone+"");
        et_password = (EditText)findViewById(R.id.et_password);
        et_password_again = (EditText)findViewById(R.id.et_password_again);
        et_nick= (EditText) findViewById(R.id.et_nick);
        et_uni= (EditText) findViewById(R.id.et_uni);
        sex= (RadioGroup) findViewById(R.id.sex);
        sex_woman= (RadioButton) findViewById(R.id.sex_woman);
        sex_man= (RadioButton) findViewById(R.id.sex_man);
        sex_man.setChecked(true);
        chosenSex = true;
        sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == sex_woman.getId()) {
                    chosenSex = false;
                } else if (checkedId == sex_man.getId()) {
                    chosenSex = true;
                }
            }
        });

        //新增校区的选择，不能乱填
//        campus= (RadioGroup) findViewById(R.id.first_select_campus);
//        first_zjg= (RadioButton) findViewById(R.id.first_zjg);
//        first_zj= (RadioButton) findViewById(R.id.first_zj);
//        first_xx= (RadioButton) findViewById(R.id.first_xx);
//        first_yq= (RadioButton) findViewById(R.id.first_yq);
//        first_zs= (RadioButton) findViewById(R.id.first_zs);
//        first_hjc= (RadioButton) findViewById(R.id.first_hjc);
        first_zjg.setChecked(true);
        chosenCampus ="紫金港";
        campus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == first_zjg.getId()) {
                    chosenCampus="紫金港";
                } else if (checkedId == first_zj.getId()) {
                    chosenCampus="之江";
                }
                else if (checkedId == first_xx.getId()) {
                    chosenCampus="西溪";
                }
                else if (checkedId == first_yq.getId()) {
                    chosenCampus="玉泉";
                }
                else if (checkedId == first_zs.getId()) {
                    chosenCampus="舟山";
                }
                else{
                    chosenCampus="华家池";
                }
            }
        });


        confirm= (Button) findViewById(R.id.confirm_first_info);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = MD5(et_password.getText().toString());
                if (password.equals("")) {
                    ShowToast("密码为空真的好吗");
                    return;
                }
                if (!password.equals(MD5(et_password_again.getText().toString()))) {
                    ShowToast("第二个密码填错了亲");
                    return;
                }
                if (et_nick.getText().toString().equals("")) {
                    ShowToast("昵称不能不甜！");
                    return;
                }
                if (et_uni.getText().toString().equals("")) {
                    ShowToast("学校不能不恬！");
                    return;
                }

                User u = new User();
                u.setPassword(password);
                u.setMobilePhoneNumber(mobilePhone);
                u.setMobilePhoneNumberVerified(true);
                u.setNick(et_nick.getText().toString());
                u.setUsername(et_nick.getText().toString());
                u.setSex(chosenSex);
                u.setSignature("Fighting!!");
                u.setCampus(chosenCampus);
                u.setUniversity(et_uni.getText().toString());
                u.setInviter(inviter);
                u.setDeviceType("android");
                u.setInstallId(BmobInstallation.getInstallationId(FirstUpdateInfoElinc.this));
                u.signUp(FirstUpdateInfoElinc.this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        final User user = userManager.getCurrentUser(User.class);
                        ShowToast("注册成功");
                        // 将设备与username进行绑定
                        userManager.bindInstallationForRegister(user.getUsername());
                        //更新地理位置信息
                        updateUserLocation();
                        //发广播通知登陆页面退出
                        sendBroadcast(new Intent(BmobConstants.ACTION_REGISTER_SUCCESS_FINISH));
                        Intent intent = new Intent(FirstUpdateInfoElinc.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        ShowToast("用户名已被注册或者无网络访问o(╯□╰)o");
                    }
                });
            }
        });
    }

    private static String MD5(String str) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        char[] charArray = str.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);

        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }
}
