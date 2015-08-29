package com.elinc.im.elinc.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.bmob.im.BmobChatManager;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.PushListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import com.elinc.im.elinc.CustomApplcation;
import com.elinc.im.elinc.R;
import com.elinc.im.elinc.bean.Goal;
import com.elinc.im.elinc.bean.User;
import com.elinc.im.elinc.config.BmobConstants;
import com.elinc.im.elinc.util.CollectionUtils;
import com.elinc.im.elinc.util.ImageLoadOptions;
import com.elinc.im.elinc.util.PhotoUtil;
import com.elinc.im.elinc.view.dialog.DialogTips;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 个人资料页面
 * 
 * @ClassName: SetMyInfoActivity
 * @Description:
 * @author smile
 * @date 2014-6-10 下午2:55:19
 */
public class SetMyInfoActivity extends ActivityBase implements OnClickListener {

	private TextView  tv_set_nick, tv_set_gender,tv_set_campus,tv_set_sign,tv_set_goal;
	private ImageView iv_set_avator, iv_arraw;
	private Button iv_edit;
	private LinearLayout layout_all;

	private Button btn_chat, btn_black, btn_add_friend;
	private RelativeLayout layout_head, layout_nick, layout_goal,layout_black_tips;
	private String objectId = "";
	private String avatar = "";
	private String from = "";
	private String signature ="";
	private String username = "";
	private User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 因为魅族手机下面有三个虚拟的导航按钮，需要将其隐藏掉，不然会遮掉拍照和相册两个按钮，且在setContentView之前调用才能生效
		/*int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= 14) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}*/
		setContentView(R.layout.activity_set_info);
		from = getIntent().getStringExtra("from");//me add other
		username = getIntent().getStringExtra("username");
		initView();
	}

	private void initView() {
		layout_all = (LinearLayout) findViewById(R.id.layout_all);
		iv_set_avator = (ImageView) findViewById(R.id.iv_set_avator);
		iv_arraw = (ImageView) findViewById(R.id.iv_arraw);
		iv_edit = (Button) findViewById(R.id.iv_edit);
		//tv_set_name = (TextView) findViewById(R.id.tv_set_name);
		tv_set_nick = (TextView) findViewById(R.id.tv_set_nick);
		layout_head = (RelativeLayout) findViewById(R.id.layout_head);
		layout_nick = (RelativeLayout) findViewById(R.id.layout_nick);
		layout_goal = (RelativeLayout) findViewById(R.id.layout_goal);
		layout_goal.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("username", username);
				bundle.putString("objectId", objectId);
				bundle.putString("avatar", avatar);
				bundle.putString("signature",signature);
				intent.putExtras(bundle);
				intent.setClass(SetMyInfoActivity.this, MyTreeActivity.class);
				startActivity(intent);
			}
		});
		layout_black_tips = (RelativeLayout) findViewById(R.id.layout_black_tips);
		tv_set_gender = (TextView) findViewById(R.id.tv_set_gender);
		tv_set_goal = (TextView) findViewById(R.id.tv_set_goal);
		tv_set_campus = (TextView) findViewById(R.id.tv_set_campus);
		tv_set_sign = (TextView) findViewById(R.id.tv_set_sign);
		btn_chat = (Button) findViewById(R.id.btn_chat);
		btn_black = (Button) findViewById(R.id.btn_black);
		btn_add_friend = (Button) findViewById(R.id.btn_add_friend);
		btn_add_friend.setEnabled(false);
		btn_chat.setEnabled(false);
		btn_black.setEnabled(false);

		if (from.equals("me")) {    //自己
			initTopBarForLeft("个人资料");
			layout_head.setOnClickListener(this);
			layout_nick.setOnClickListener(this);
			iv_edit.setVisibility(View.VISIBLE);
            iv_edit.setOnClickListener(this);
			iv_arraw.setVisibility(View.VISIBLE);
			btn_black.setVisibility(View.GONE);
			btn_chat.setVisibility(View.GONE);
			btn_add_friend.setVisibility(View.GONE);
		} else {    //
			initTopBarForLeft("详细资料");
			iv_edit.setVisibility(View.GONE);
			iv_arraw.setVisibility(View.INVISIBLE);
			//不管对方是不是你的好友，均可以发送消息
			btn_chat.setVisibility(View.VISIBLE);
			btn_chat.setOnClickListener(this);
			if (from.equals("add")) {// 从附近的人列表添加好友--因为获取附近的人的方法里面有是否显示好友的情况，因此在这里需要判断下这个用户是否是自己的好友
				if (mApplication.getContactList().containsKey(username)) {// 是好友
//					btn_chat.setVisibility(View.VISIBLE);
//					btn_chat.setOnClickListener(this);
					btn_black.setVisibility(View.VISIBLE);
					btn_black.setOnClickListener(this);
				} else {
//					btn_chat.setVisibility(View.GONE);
					btn_black.setVisibility(View.GONE);
					btn_add_friend.setVisibility(View.VISIBLE);
					btn_add_friend.setOnClickListener(this);
				}
			} else {// 查看他人
//				btn_chat.setVisibility(View.VISIBLE);
//				btn_chat.setOnClickListener(this);
				btn_black.setVisibility(View.VISIBLE);
				btn_black.setOnClickListener(this);
			}
			initOtherData(username);
		}
	}

	private void initMeData() {
		User user = userManager.getCurrentUser(User.class);
		initOtherData(user.getUsername());
	}

	private void initOtherData(String name) {
		userManager.queryUser(name, new FindListener<User>() {
			@Override
			public void onError(int arg0, String arg1) {
				ShowLog("onError onError:" + arg1);
			}

			@Override
			public void onSuccess(List<User> arg0) {
				if (arg0 != null && arg0.size() > 0) {
					user = arg0.get(0);
					objectId = arg0.get(0).getObjectId();
					signature =arg0.get(0).getSignature();
					avatar = arg0.get(0).getAvatar();
					btn_chat.setEnabled(true);
					btn_black.setEnabled(true);
					btn_add_friend.setEnabled(true);
					updateUser(user);
				} else {
					ShowLog("onSuccess 查无此人");
				}
			}
		});
	}

	private void updateUser(User user) {
		// 更改
		refreshAvatar(user.getAvatar());
		tv_set_sign.setText(user.getSignature());
		tv_set_campus.setText(user.getCampus());
		tv_set_nick.setText(user.getUsername());

		BmobQuery<Goal> query = new BmobQuery<>();
		//用此方式可以构造一个BmobPointer对象。只需要设置objectId就行
		User u= new User();
		u.setObjectId(user.getObjectId());
		query.addWhereEqualTo("author", new BmobPointer(user));
		query.addWhereNotEqualTo("out", true);
		query.findObjects(this, new FindListener<Goal>() {
			@Override
			public void onSuccess(List<Goal> object) {
				String g=""; //暂存 goal 的title
				for (int i = 0; i < object.size() && i < 3; i++) {
					g = g +"  "+object.get(i).getGoalContent();
				}
				tv_set_goal.setText(g);

			}
			@Override
			public void onError(int code, String msg) {
				Toast.makeText(SetMyInfoActivity.this, "无法获取我的目标！", Toast.LENGTH_SHORT).show();
			}
		});

		tv_set_gender.setText(user.getSex() ? "男" : "女");
		// 检测是否为黑名单用户
		if (from.equals("other")) {
			if (BmobDB.create(this).isBlackUser(user.getUsername())) {
				btn_black.setVisibility(View.GONE);
				layout_black_tips.setVisibility(View.VISIBLE);
			} else {
				btn_black.setVisibility(View.VISIBLE);
				layout_black_tips.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 更新头像 refreshAvatar
	 * @return void
	 */
	private void refreshAvatar(String avatar) {
		if (avatar != null && !avatar.equals("")) {
			ImageLoader.getInstance().displayImage(avatar, iv_set_avator,
					ImageLoadOptions.getOptions());
		} else {
			iv_set_avator.setImageResource(R.drawable.default_head);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (from.equals("me")) {
			initMeData();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
            case R.id.btn_chat: // 发起聊天
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("user", user);
                startAnimActivity(intent);
                finish();
                break;
            case R.id.layout_head:
                showAvatarPop();
                break;
            case R.id.iv_edit:
                startAnimActivity(UpdateInfoActivityElinc.class);
                break;
            case R.id.btn_black:// 黑名单
                showBlackDialog(user.getUsername());
                break;
            case R.id.btn_add_friend://添加好友
                addFriend();
                break;
		}
	}
	
	/** 修改资料
	  * updateInfo
	  * @Title: updateInfo
	  * @return void
	  */
	private void updateInfo(int which) {
		final User u = new User();
		if(which==0){
			u.setSex(true);
		}else{
			u.setSex(false);
		}
		updateUserData(u,new UpdateListener() {
			@Override
			public void onSuccess() {
				ShowToast("修改成功");
				tv_set_gender.setText(u.getSex() ? "男" : "女");
			}
			@Override
			public void onFailure(int arg0, String arg1) {
				ShowToast("onFailure:" + arg1);
			}
		});
	}

	/**
	 * 添加好友请求
	 * @Title: addFriend
	 * @Description:
	 * @return void
	 */
	private void addFriend() {
		final ProgressDialog progress = new ProgressDialog(this);
		progress.setMessage("正在添加...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		// 发送tag请求
		BmobChatManager.getInstance(this).sendTagMessage(BmobConfig.TAG_ADD_CONTACT,
				user.getObjectId(), new PushListener() {

					@Override
					public void onSuccess() {
						progress.dismiss();
						ShowToast("发送请求成功，等待对方验证！");
					}

					@Override
					public void onFailure(int arg0, final String arg1) {
						progress.dismiss();
						ShowToast("发送请求失败，请重试！");
						ShowLog("发送请求失败:" + arg1);
					}
				});
	}

	/**
	 * 显示黑名单提示框
	 * @Title: showBlackDialog
	 * @Description:
	 * @return void
	 */
	private void showBlackDialog(final String username) {
		DialogTips dialog = new DialogTips(this, "加入黑名单", "加入黑名单，你将不再收到对方的消息，确定要继续吗？", "确定", true, true);
		dialog.SetOnSuccessListener(new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int userId) {
                // 添加到黑名单列表
                userManager.addBlack(username, new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        ShowToast("黑名单添加成功!");
                        btn_black.setVisibility(View.GONE);
                        layout_black_tips.setVisibility(View.VISIBLE);
                        // 重新设置下内存中保存的好友列表
                        CustomApplcation.getInstance().setContactList(CollectionUtils.list2map(BmobDB.create(SetMyInfoActivity.this).getContactList()));
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        ShowToast("黑名单添加失败:" + arg1);
                    }
                });
            }
        });
		// 显示确认对话框
		dialog.show();
	}

	private RelativeLayout layout_choose;
    private RelativeLayout layout_photo;
    private PopupWindow popupWindow;
	public String filePath = "";

	private void showAvatarPop() {
		View view = LayoutInflater.from(this).inflate(R.layout.pop_showavator, null);
		layout_choose = (RelativeLayout) view.findViewById(R.id.layout_choose);
		layout_photo = (RelativeLayout) view.findViewById(R.id.layout_photo);
		layout_photo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ShowLog("点击拍照");
				layout_choose.setBackgroundColor(getResources().getColor(R.color.base_color_text_white));
				layout_photo.setBackgroundDrawable(getResources().getDrawable(R.drawable.pop_bg_press));
				File dir = new File(BmobConstants.MyAvatarDir);
				if (!dir.exists()) {
					if (!dir.mkdirs()){
                        Log.i("error","mkdir失败");
                    };
				}
				// 原图
				File file = new File(dir, new SimpleDateFormat("yyMMddHHmmss", Locale.CHINA).format(new Date()));
				filePath = file.getAbsolutePath();// 获取相片的保存路径
				Uri imageUri = Uri.fromFile(file);
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				startActivityForResult(intent, BmobConstants.REQUESTCODE_UPLOADAVATAR_CAMERA);
			}
		});
		layout_choose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ShowLog("点击相册");
				layout_photo.setBackgroundColor(getResources().getColor(R.color.base_color_text_white));
				layout_choose.setBackgroundDrawable(getResources().getDrawable(R.drawable.pop_bg_press));
				Intent intent = new Intent(Intent.ACTION_PICK, null);
				intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				startActivityForResult(intent, BmobConstants.REQUESTCODE_UPLOADAVATAR_LOCATION);
			}
		});

		popupWindow = new PopupWindow(view, mScreenWidth, 600);
		popupWindow.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    popupWindow.dismiss();
                    return true;
                }
                return false;
            }
        });

		popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
		popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popupWindow.setTouchable(true);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		// 动画效果 从底部弹起
		popupWindow.setAnimationStyle(R.style.Animations_GrowFromBottom);
		popupWindow.showAtLocation(layout_all, Gravity.BOTTOM, 0, 0);
	}

	/**
	 * @Title: startImageAction
	 * @return void
	 */
	private void startImageAction(Uri uri, int outputX, int outputY,
			int requestCode, boolean isCrop) {
		Intent intent = null;
		if (isCrop) {
			intent = new Intent("com.android.camera.action.CROP");
		} else {
			intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		}
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);
		intent.putExtra("scale", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		intent.putExtra("return-data", true);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		startActivityForResult(intent, requestCode);
	}

	boolean isFromCamera = false;// 区分拍照旋转
	int degree = 0;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case BmobConstants.REQUESTCODE_UPLOADAVATAR_CAMERA:// 拍照修改头像
			if (resultCode == RESULT_OK) {
				if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					ShowToast("SD不可用");
					return;
				}
				isFromCamera = true;
				File file = new File(filePath);
				degree = PhotoUtil.readPictureDegree(file.getAbsolutePath());
				Log.i("life", "拍照后的角度：" + degree);
				startImageAction(Uri.fromFile(file), 200, 200, BmobConstants.REQUESTCODE_UPLOADAVATAR_CROP, true);
			}
			break;
		case BmobConstants.REQUESTCODE_UPLOADAVATAR_LOCATION:// 本地修改头像
			if (popupWindow != null) {
				popupWindow.dismiss();
			}
			Uri uri;
			if (data == null) {
				return;
			}
			if (resultCode == RESULT_OK) {
				if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					ShowToast("SD不可用");
					return;
				}
				isFromCamera = false;
				uri = data.getData();
				startImageAction(uri, 200, 200, BmobConstants.REQUESTCODE_UPLOADAVATAR_CROP, true);
			} else {
				ShowToast("照片获取失败");
			}

			break;
		case BmobConstants.REQUESTCODE_UPLOADAVATAR_CROP:// 裁剪头像返回
			if (popupWindow != null) {
				popupWindow.dismiss();
			}
			if (data == null) {
				return;
			} else {
				saveCropAvator(data);
			}
			// 初始化文件路径
			filePath = "";
			// 上传头像
			uploadAvatar();
			break;
		default:
			break;

		}
	}

	private void uploadAvatar() {
		BmobLog.i("头像地址：" + path);
		final BmobFile bmobFile = new BmobFile(new File(path));
		bmobFile.upload(this, new UploadFileListener() {
			@Override
			public void onSuccess() {
				String url = bmobFile.getFileUrl(SetMyInfoActivity.this);
				// 更新BmobUser对象
				updateUserAvatar(url);
			}

			@Override
			public void onProgress(Integer arg0) {
			}

			@Override
			public void onFailure(int arg0, String msg) {
				ShowToast("头像上传失败：" + msg);
			}
		});
	}

	private void updateUserAvatar(final String url) {
		User  u =new User();
		u.setAvatar(url);
		updateUserData(u,new UpdateListener() {
			@Override
			public void onSuccess() {
				ShowToast("头像更新成功！");
				// 更新头像
				refreshAvatar(url);
			}

			@Override
			public void onFailure(int code, String msg) {
				ShowToast("头像更新失败：" + msg);
			}
		});
	}

	String path;

	/**
	 * 保存裁剪的头像
	 */
	private void saveCropAvator(Intent data) {
		Bundle extras = data.getExtras();
		if (extras != null) {
			Bitmap bitmap = extras.getParcelable("data");
			if (bitmap != null) {
				bitmap = PhotoUtil.toRoundCorner(bitmap, 10);
				if (isFromCamera && degree != 0) {
					bitmap = PhotoUtil.rotaingImageView(degree, bitmap);
				}
				iv_set_avator.setImageBitmap(bitmap);
				// 保存图片
				String filename = new SimpleDateFormat("yyMMddHHmmss",Locale.CHINA).format(new Date())+".png";
				path = BmobConstants.MyAvatarDir + filename;
				PhotoUtil.saveBitmap(BmobConstants.MyAvatarDir, filename, bitmap, true);
				// 上传头像
				if (bitmap != null && bitmap.isRecycled()) {
					bitmap.recycle();
				}
			}
		}
	}
	
	private void updateUserData(User user,UpdateListener listener){
		User current = userManager.getCurrentUser(User.class);
		user.setObjectId(current.getObjectId());
		user.update(this, listener);
	}

}
