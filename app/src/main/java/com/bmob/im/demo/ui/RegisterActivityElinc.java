package com.bmob.im.demo.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.bmob.im.demo.R;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.config.BmobConstants;
import com.bmob.im.demo.util.CommonUtils;

import java.util.List;

import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.RequestSMSCodeListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.VerifySMSCodeListener;

public class RegisterActivityElinc extends BaseActivity {
	Button btn_register,btn_verify_code;
	EditText et_username,et_code;
	Bundle bundle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		initTopBarForLeft("注册");
		bundle = new Bundle();
		et_username = (EditText) findViewById(R.id.et_username);
		et_code= (EditText) findViewById(R.id.et_verify_code);

		btn_register = (Button) findViewById(R.id.btn_register);
		btn_register.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				registerVerify(et_username.getText().toString(), et_code.getText().toString());
			}
		});
		btn_verify_code= (Button) findViewById(R.id.btn_verify_code);
		btn_verify_code.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.i("phone", et_username.getText().toString());
				BmobQuery<User> query = new BmobQuery<User>();
				query.addWhereEqualTo("mobilePhoneNumber", et_username.getText().toString());
				query.count(RegisterActivityElinc.this, User.class, new CountListener() {
					@Override
					public void onSuccess(int count) {
						if (count > 0) {
							ShowToast("该手机号已经被别人注册了哦！");
						} else {
							sendVerifyCodeToPhone(et_username.getText().toString());
						}

					}

					@Override
					public void onFailure(int code, String msg) {
						ShowToast("sorry,没有网络");
					}
				});

			}
		});
		//checkUser();
	}

	private void checkUser(){
		BmobQuery<User> query = new BmobQuery<User>();
		query.addWhereEqualTo("username", "smile");
		query.findObjects(this, new FindListener<User>() {
			@Override
			public void onError(int arg0, String arg1) {
			}
			@Override
			public void onSuccess(List<User> arg0) {
				if (arg0 != null && arg0.size() > 0) {
					User user = arg0.get(0);
					user.setPassword("1234567");
					user.update(RegisterActivityElinc.this, new UpdateListener() {
						@Override
						public void onSuccess() {
							// TODO Auto-generated method stub
							userManager.login("smile", "1234567", new SaveListener() {
								@Override
								public void onSuccess() {
									Log.i("smile", "登陆成功");
								}
								@Override
								public void onFailure(int code, String msg) {
									Log.i("smile", "登陆失败：" + code + ".msg = " + msg);
								}
							});
						}
						@Override
						public void onFailure(int code, String msg) {
						}
					});
				}
			}
		});
	}

	/*以下的方法都是自己加的*/
	/* this method aims to send verifying code to the phoneNumber */
	public void sendVerifyCodeToPhone(final String phoneNumber) {
		BmobQuery<User> query2 = new BmobQuery<User>();
		query2.addWhereEqualTo("phoneNumber",phoneNumber);
		query2.count(this, User.class, new CountListener() {
			@Override
			public void onSuccess(int count) {
				// TODO Auto-generated method stub
				if (count == 0) {
					BmobSMS.requestSMSCode(RegisterActivityElinc.this, phoneNumber, "模板1", new RequestSMSCodeListener() {
						@Override
						public void done(Integer integer, BmobException ex) {
							if (ex == null) {//验证码发送成功
								//Log.i("smile", " message id:" + integer);//用于查询本次短信发送详情
								Tool.alert(RegisterActivityElinc.this, "验证短信发送成功");
							} else {
								ShowToast("世界上最遥远的距离就是没网");
							}
						}
					});
				}
			}

			@Override
			public void onFailure(int code, String msg) {
				// TODO Auto-generated method stub
				ShowToast("世界上最遥远的距离就是没网");
			}
		});

	}

	public void registerVerify(final String phoneNumber,final String code){
		BmobSMS.verifySmsCode(RegisterActivityElinc.this, phoneNumber, code, new VerifySMSCodeListener() {
			@Override
			public void done(BmobException ex) {
				if (ex == null) {//短信验证码已验证成功
					EditText et_inviter=(EditText)findViewById(R.id.et_inviter);
					String inviter=et_inviter.getText().toString();
					register(phoneNumber,inviter);
				} else {
					//Tool.alert(RegisterActivityElinc.this, "fail:code =" + ex.getErrorCode() + ",msg=" + ex.getLocalizedMessage());
					ShowToast("验证码错误");
				}
			}
		});
	}

	private void register(String phone,String inviter){
		if(phone==null||phone.equals("")){
			return;
		}
		boolean isNetConnected = CommonUtils.isNetworkAvailable(this);
		if(!isNetConnected){
			ShowToast(R.string.network_tips);
			return;
		}
		final ProgressDialog progress = new ProgressDialog(RegisterActivityElinc.this);
		progress.setMessage("正在验证...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		//由于每个应用的注册所需的资料都不一样，故IM sdk未提供注册方法，用户可按照bmod SDK的注册方式进行注册。
		//注册的时候需要注意两点：1、User表中绑定设备id和type，2、设备表中绑定username字段
		progress.dismiss();
		bundle.putString("mobile_phone", phone);
		bundle.putString("inviter",inviter);
		Intent intent = new Intent(RegisterActivityElinc.this, FirstUpdateInfoElinc.class);
		intent.putExtras(bundle);
		startActivity(intent);
		finish();
//		bu.signUp(RegisterActivityElinc.this, new SaveListener() {
//			@Override
//			public void onSuccess() {
//				progress.dismiss();
//				ShowToast("注册成功");
//				// 启动主页
//				Intent intent = new Intent(RegisterActivityElinc.this, FirstUpdateInfoElinc.class);
//				startActivity(intent);
//				finish();
//			}
//			@Override
//			public void onFailure(int arg0, String arg1) {
//				BmobLog.i(arg1);
//				ShowToast("注册失败:" + arg1);
//				progress.dismiss();
//			}
//		});
	}
}
