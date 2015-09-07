package com.elinc.im.elinc.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.elinc.im.elinc.R;
import com.elinc.im.elinc.bean.User;

import cn.bmob.v3.listener.UpdateListener;

/**
 * 设置昵称和性别
 * 
 * @ClassName: SetNickAndSexActivity
 * @Description: TODO
 * @author smile
 * @date 2014-6-7 下午4:03:40
 */
public class UpdateInfoActivityElinc extends ActivityBase {
	private EditText et_uni,et_sign;
	private String chosenCampus;
	private RadioGroup sex;
	private RadioButton sex_woman,sex_man;
	private Boolean chosenSex;
	private Button confirm;
	private Spinner campus;

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
		et_uni.setText(user.getUniversity(),bufferType);
		if(user.getSex()){
			sex_man.setChecked(true);
			sex_woman.setChecked(false);
		}else{
			sex_woman.setChecked(true);
			sex_man.setChecked(false);
		}
		campus= (Spinner) findViewById(R.id.campus);
		chosenCampus ="紫金港";
		// 建立数据源
		String[] mItems = getResources().getStringArray(R.array.campus);
		// 建立Adapter并且绑定数据源
		ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, mItems);
		//绑定 Adapter到控件
		adapter.setDropDownViewResource(R.layout.elinc_spinner);
		campus.setAdapter(adapter);
		campus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				chosenCampus = parent.getItemAtPosition(position).toString();
				Log.i("click", chosenCampus);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
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
				u.setCampus(chosenCampus);
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
}
