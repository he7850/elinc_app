package com.bmob.im.demo.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import cn.bmob.v3.listener.UpdateListener;

import com.bmob.im.demo.R;
import com.bmob.im.demo.bean.User;

/**
 * 设置昵称和性别
 * 
 * @ClassName: SetNickAndSexActivity
 * @Description: TODO
 * @author smile
 * @date 2014-6-7 下午4:03:40
 */
public class UpdateInfoActivityElinc extends ActivityBase {
	private EditText et_uni,et_campus,et_sign;
	private RadioGroup sex;
	private RadioButton sex_woman,sex_man;
	private Boolean chosenSex;
	private Button confirm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_my_info_elinc);
		//初始化View
		initView();
		//初始化Listener
		initTopBarForLeft("修改资料");
		initListener();
	}
	private void initView(){
		et_campus= (EditText) findViewById(R.id.et_campus);
		et_sign= (EditText) findViewById(R.id.edit_sign_text);
		et_uni= (EditText) findViewById(R.id.et_university);
		sex= (RadioGroup) findViewById(R.id.sex);
		sex_woman= (RadioButton) findViewById(R.id.sex_woman);
		sex_man= (RadioButton) findViewById(R.id.sex_man);
		confirm= (Button) findViewById(R.id.submit_info);
		TextView.BufferType bufferType;
		bufferType = TextView.BufferType.EDITABLE;
		final User user = userManager.getCurrentUser(User.class);
		Log.i("1", user.getCampus());
		et_campus.setText(user.getCampus(),bufferType);
		et_uni.setText(user.getUniversity(),bufferType);
		if(user.getSex()){
			sex_man.setChecked(true);
			sex_woman.setChecked(false);
		}else{
			sex_woman.setChecked(true);
			sex_man.setChecked(false);
		}
	}
	private void initListener(){
		sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == sex_woman.getId()) {
					chosenSex = false;
				} else if (checkedId == sex_man.getId()) {
					chosenSex = true;
				}
			}
		});
		confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final User user = userManager.getCurrentUser(User.class);
				User u = new User();
				u.setSex(chosenSex);
				u.setUniversity(et_uni.getText().toString());
				u.setCampus(et_campus.getText().toString());
				u.setSignature(et_sign.getText().toString());
				u.setObjectId(user.getObjectId());
				u.update(UpdateInfoActivityElinc.this, new UpdateListener() {
					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						final User c = userManager.getCurrentUser(User.class);
						ShowToast("修改成功");
						finish();
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						// TODO Auto-generated method stub
						ShowToast("onFailure:" + arg1);
					}
				});
			}
		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_first_update_info, menu);
		return true;
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
}
