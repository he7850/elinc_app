package com.bmob.im.demo.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bmob.im.demo.R;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.bean.User;

import java.security.MessageDigest;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.RequestSMSCodeListener;
import cn.bmob.v3.listener.ResetPasswordByCodeListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.VerifySMSCodeListener;

public class FindPassword extends BaseActivity {
    EditText et_find_password,et_find_password_again;
    EditText et_phone_for_pwd,et_verify_code_for_pwd ;
    Button btn_verify_code_for_find_pwd,btn_find_pwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_password);
        et_find_password= (EditText) findViewById(R.id.et_find_password);
        et_find_password_again= (EditText) findViewById(R.id.et_find_password_again);
        et_verify_code_for_pwd= (EditText) findViewById(R.id.et_verify_code_for_pwd);
        et_phone_for_pwd= (EditText) findViewById(R.id.et_phone_for_pwd);
        btn_verify_code_for_find_pwd= (Button) findViewById(R.id.btn_verify_code_for_find_pwd);
        btn_find_pwd= (Button) findViewById(R.id.btn_find_pwd);

        btn_verify_code_for_find_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_find_password.getText().toString().equals(et_find_password_again.getText().toString())) {
                    sendVerifyCodeToPhone(et_phone_for_pwd.getText().toString());
                }
                else{
                    ShowToast("亲，两次密码不一致哦");
                }
            }
        });
        btn_find_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_find_password.getText().toString().equals("") ||et_find_password.getText().toString()==null){
                    ShowToast("请填写密码哦");
                } else if (et_find_password.getText().toString().equals(et_find_password_again.getText().toString())) {
                     BmobUser.resetPasswordBySMSCode(FindPassword.this, et_verify_code_for_pwd.getText().toString(), MD5(et_find_password.getText().toString()), new ResetPasswordByCodeListener() {
                         @Override
                         public void done(BmobException ex) {
                             // TODO Auto-generated method stub
                             if (ex == null) {
                                 ShowToast("密码重置成功");
                             } else {
                                 ShowToast( "重置失败：code ="+ex.getErrorCode()+",msg = "+ex.getLocalizedMessage());
                            }
                        }
                    });
                }else{
                    ShowToast("两次密码不一样哦");

                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_find_password, menu);
        return true;
    }

    public void sendVerifyCodeToPhone(final String phoneNumber) {

        BmobQuery<User> query2 = new BmobQuery<User>();
        query2.addWhereEqualTo("mobilePhoneNumber", phoneNumber);
        query2.count(this, User.class, new CountListener() {
            @Override
            public void onSuccess(int count) {
                // TODO Auto-generated method stub
                if (count!= 0) {
                    ShowToast(count+"");
                    BmobSMS.requestSMSCode(FindPassword.this, phoneNumber, "模板1", new RequestSMSCodeListener() {
                        @Override
                        public void done(Integer integer, BmobException e) {
                            // TODO Auto-generated method stub
                            if (e == null) {//验证码发送成功
                                Log.i("smile", "短信id：" + integer);//用于查询本次短信发送详情
                                ShowToast("验证码发送成功");
                            } else {
                                ShowToast("断网了亲");
                            }
                        }
                    });
                } else {
                    ShowToast(count+"");
                    ShowToast("你在逗我么，你没注册找回啥密码");
                }
            }

            @Override
            public void onFailure(int code, String msg) {
                // TODO Auto-generated method stub
                ShowToast("世界上最遥远的距离就是没网");
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
