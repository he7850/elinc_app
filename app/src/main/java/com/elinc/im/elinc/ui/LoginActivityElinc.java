package com.elinc.im.elinc.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.elinc.im.elinc.R;
import com.elinc.im.elinc.bean.User;
import com.elinc.im.elinc.config.BmobConstants;
import com.elinc.im.elinc.util.CommonUtils;
import com.elinc.im.elinc.view.dialog.DialogTips;

import java.security.MessageDigest;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;

/**
 * @ClassName: LoginActivityElinc
 * @Description: TODO
 * @author smile
 * @date 2014-6-3 下午4:41:42
 */
public class LoginActivityElinc extends BaseActivity implements OnClickListener {

	EditText et_username, et_password;
	Button btn_login;
	Button btn_register;
	TextView btn_find_pwd_in_login;

	private MyBroadcastReceiver receiver = new MyBroadcastReceiver();
	private TextView elinc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_elinc);
		init();
		//注册退出广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(BmobConstants.ACTION_REGISTER_SUCCESS_FINISH);
		registerReceiver(receiver, filter);
//		showNotice();
	}

	public void showNotice() {
		DialogTips dialog = new DialogTips(this,"提示",getResources().getString(R.string.show_notice), "确定",true,true);
		// 设置成功事件
		dialog.SetOnSuccessListener(new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int userId) {

			}
		});
		// 显示确认对话框
		dialog.show();
		dialog = null;
	}

	private void init() {
		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_register = (Button) findViewById(R.id.btn_register);
		btn_find_pwd_in_login= (TextView) findViewById(R.id.btn_find_pwd_in_login);
		btn_login.setOnClickListener(this);
		btn_register.setOnClickListener(this);
		btn_find_pwd_in_login.setOnClickListener(this);
		elinc = (TextView) findViewById(R.id.elinc);

		AssetManager mgr=getAssets();
		Typeface tf=Typeface.createFromAsset(mgr, "raw/Blokletters-Balpen.ttf");
		elinc.setTypeface(tf);

	}

	public class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && BmobConstants.ACTION_REGISTER_SUCCESS_FINISH.equals(intent.getAction())) {
				finish();
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v == btn_register) {
			Intent intent = new Intent(LoginActivityElinc.this, RegisterActivityElinc.class);
			startActivity(intent);
		} else if(v==btn_find_pwd_in_login){
			Intent intent = new Intent(LoginActivityElinc.this, FindPassword.class);
			startActivity(intent);
		}
		else{
			boolean isNetConnected = CommonUtils.isNetworkAvailable(this);
			if(!isNetConnected){
				ShowToast(R.string.network_tips);
				return;
			}
			login();
		}
	}

	private void login(){

		String name = et_username.getText().toString();
		String password = MD5(et_password.getText().toString());

		if (TextUtils.isEmpty(name)) {
			ShowToast(R.string.toast_error_username_null);
			return;
		}

		if (TextUtils.isEmpty(password)) {
			ShowToast(R.string.toast_error_password_null);
			return;
		}

		final ProgressDialog progress = new ProgressDialog(
				LoginActivityElinc.this);
		progress.setMessage("正在登陆...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		/*User user = new User();
		user.setUsername(name);
		user.setPassword(password);*/
		BmobUser.loginByAccount(this, name, password, new LogInListener<User>() {
			@Override
			public void done(User user, BmobException e) {
				if(user!=null){
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							progress.setMessage("正在获取好友列表...");
						}
					});
					//更新用户的地理位置以及好友的资料
					updateUserInfos();
					progress.dismiss();
					Intent intent = new Intent(LoginActivityElinc.this,MainActivity.class);
					startActivity(intent);
					finish();
				}else{
					progress.dismiss();
					//BmobLog.i(e);
					ShowToast("登陆失败");
				}

			}
		});
		/*这一块是原来用用户名登陆的额，已经改成了用手机*/
		/*userManager.login(user,new SaveListener() {

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						progress.setMessage("正在获取好友列表...");
					}
				});
				//更新用户的地理位置以及好友的资料
				updateUserInfos();
				progress.dismiss();
				Intent intent = new Intent(LoginActivityElinc.this,MainActivity.class);
				startActivity(intent);
				finish();
			}

			@Override
			public void onFailure(int errorcode, String arg0) {
				// TODO Auto-generated method stub
				progress.dismiss();
				BmobLog.i(arg0);
				ShowToast(arg0);
			}
		});*/

	}

	@Override
	protected void onDestroy() {
		 //TODO Auto-generated method stub
		super.onDestroy();
		try {
			unregisterReceiver(receiver);
		} catch (Exception e) {
			e.printStackTrace();
		}

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


	private static long firstTime;
	/**
	 * 连续按两次返回键就退出
	 */
	@Override
	public void onBackPressed() {
		if (firstTime + 2000 > System.currentTimeMillis()) {
			finish();
		} else {
			ShowToast("再按一次退出程序");
		}
		firstTime = System.currentTimeMillis();
	}
}
