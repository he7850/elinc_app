package com.elinc.im.elinc.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.elinc.im.elinc.CustomApplcation;
import com.elinc.im.elinc.MyMessageReceiver;
import com.elinc.im.elinc.R;
import com.elinc.im.elinc.bean.User;
import com.elinc.im.elinc.ui.fragment.CardFragment;
import com.elinc.im.elinc.ui.fragment.ContactFragment;
import com.elinc.im.elinc.ui.fragment.QuestionFragment;
import com.elinc.im.elinc.util.ImageLoadOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.EventListener;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.BmobUpdateListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateResponse;
import cn.bmob.v3.update.UpdateStatus;

/**
 * 登陆
 * ClassName: MainActivity
 * Description: ...
 * author smile
 * date 2014-5-29 下午2:45:35
 */
public class MainActivity extends ActivityBase implements EventListener{
	private CardFragment cardFragment;
	private ContactFragment contactFragment;
	private QuestionFragment questionFragment;
	private DrawerLayout mDrawerLayout;
	private User me;
	int currentTabIndex;
    private PopupWindow popupWindow;
    private LayoutInflater layoutInflater;
    private LinearLayout view;
	private Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//开启定时检测服务（单位为秒）-在这里检测后台是否还有未读的消息，有的话就取出来
		//如果你觉得检测服务比较耗流量和电量，你也可以去掉这句话-同时还有onDestory方法里面的stopPollService方法
		//BmobChat.getInstance(this).startPollService(30);
		//开启广播接收器
		initNewMessageBroadCast();
		initTagMessageBroadCast();
		//initView();
		//发起自动更新
		BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {
			@Override
			public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
				// TODO Auto-generated method stub
				if (updateStatus == UpdateStatus.Yes) {//版本有更新

				}else if(updateStatus == UpdateStatus.No){
					Toast.makeText(MainActivity.this, "版本无更新", Toast.LENGTH_SHORT).show();
				}else if(updateStatus==UpdateStatus.EmptyField){//此提示只是提醒开发者关注那些必填项，测试成功后，无需对用户提示
					Toast.makeText(MainActivity.this, "请检查你AppVersion表的必填项，1、target_size（文件大小）是否填写；2、path或者android_url两者必填其中一项。", Toast.LENGTH_SHORT).show();
				}else if(updateStatus==UpdateStatus.IGNORED){
					Toast.makeText(MainActivity.this, "该版本已被忽略更新", Toast.LENGTH_SHORT).show();
				}else if(updateStatus==UpdateStatus.ErrorSizeFormat){
					Toast.makeText(MainActivity.this, "请检查target_size填写的格式，请使用file.length()方法获取apk大小。", Toast.LENGTH_SHORT).show();
				}else if(updateStatus==UpdateStatus.TimeOut){
					Toast.makeText(MainActivity.this, "查询出错或查询超时", Toast.LENGTH_SHORT).show();
				}
			}
		});
		BmobUpdateAgent.update(this);
	}

	private void initView(){
		me = BmobUserManager.getInstance(MainActivity.this).getCurrentUser(User.class);

        view = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.popup_menu, null);
		toolbar= (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle("郁林");
		toolbar.setBackgroundColor(getResources().getColor(R.color.elinc_main_green));
		setSupportActionBar(toolbar);
		final android.support.v7.app.ActionBar main_ab = getSupportActionBar();
		if (main_ab != null) {
			main_ab.setHomeAsUpIndicator(R.drawable.ic_menu);
			main_ab.setDisplayHomeAsUpEnabled(true);
		}
		final Context context = this;

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_chat:
                        startAnimActivity(RecentActivity.class);
                        break;
                    case R.id.action_find:
                        startAnimActivity(FindQuestion.class);
                        break;
                    case R.id.action_add:
                        if (popupWindow == null) {
                            popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        }
                        popupWindow.setOutsideTouchable(true);
                        popupWindow.setBackgroundDrawable(new BitmapDrawable());
                        popupWindow.setFocusable(true);
                        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                        int xoffset = windowManager.getDefaultDisplay().getWidth() - popupWindow.getWidth();
                        popupWindow.showAsDropDown(findViewById(R.id.toolbar), xoffset, 0);
                        LinearLayout new_friends = (LinearLayout) view.findViewById(R.id.new_friends);
                        new_friends.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startAnimActivity(NearPeopleActivity.class);
                                if (popupWindow != null) {
                                    popupWindow.dismiss();
                                }
                            }
                        });
                        LinearLayout add_friend = (LinearLayout) view.findViewById(R.id.add_friend);
                        add_friend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startAnimActivity(AddFriendActivity.class);
                                if (popupWindow != null) {
                                    popupWindow.dismiss();
                                }
                            }
                        });
                        LinearLayout new_question = (LinearLayout) view.findViewById(R.id.new_question);
                        new_question.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startAnimActivity(NewQuestionActivityElinc.class);
                                if (popupWindow != null) {
                                    popupWindow.dismiss();
                                }
                            }
                        });
                        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if (!hasFocus) {
                                    if (popupWindow != null) {
                                        popupWindow.dismiss();
                                    }
                                }
                            }
                        });

                        break;
                }
                return false;
            }
        });


		/**
		 * 设置DrawyerLayout和NavigationView
		 */
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		if (navigationView != null) {
			setupDrawerContent(navigationView);
		}

		TextView tvNavUsername = (TextView) findViewById(R.id.tvNavUsername);
		String username = me.getNick();
		tvNavUsername.setText(username+"");

		ImageView civNavUserAvantar = (ImageView) findViewById(R.id.civUserAvantar);
		String avatar = me.getAvatar();
		if (avatar != null && !avatar.equals("")) {
			ImageLoader.getInstance().displayImage(avatar, civNavUserAvantar,
					ImageLoadOptions.getOptions());
		}else{
			Glide.with(this).load(R.drawable.head).fitCenter().into(civNavUserAvantar);
		}
		civNavUserAvantar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("from","me");
				intent.putExtra("username",me.getUsername());
				intent.setClass(MainActivity.this,SetMyInfoActivity.class);
				startAnimActivity(intent);
			}
		});

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.nav_settings:
                            startAnimActivity(SettingActivity.class);
                            break;
                        case R.id.nav_my_tree:
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            User u= BmobUser.getCurrentUser(context, User.class);
                            bundle.putString("username", u.getUsername());
                            bundle.putString("signature",u.getSignature());
                            bundle.putString("objectId",u.getObjectId());
                            bundle.putString("avatar",u.getAvatar());
                            if(u.getUsername().equals(u.getUsername())){
                                bundle.putString("from", "me");
                            }else{
                                bundle.putString("from", "add");
                            }
                            intent.putExtras(bundle);
                            intent.setClass(MainActivity.this, MyTreeActivity.class);
                            startAnimActivity(intent);
                            break;
                        case R.id.nav_messages:
                            startAnimActivity(MyFavoriteActivityElinc.class);
                            break;
                        case R.id.nav_bug_report:
                            startAnimActivity(BugReportActivityElinc.class);
                            break;
                        case R.id.nav_about_us:
                            startAnimActivity(AboutUsActivityElinc.class);
                            break;
                    }

                    return false;
                }
            });
        }

        /**
		 * 设置tabs，将带有adapter的viewpager与TabLayout一起设置
		 */
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		if (viewPager != null) {
			setupViewPager(viewPager);
            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (position == 0) {
                        currentTabIndex = 0;
                    } else if (position == 1) {
                        currentTabIndex = 1;
                    } else if (position == 2) {
                        currentTabIndex = 2;
                    } else if (position == 3) {
                        currentTabIndex = 3;
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
		}


		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem item = menu.findItem(R.id.action_chat);
		if (BmobDB.create(this).hasUnReadMsg()) {
			item.setIcon(R.drawable.new_msg_elinc);
		} else {
			item.setIcon(R.drawable.no_msg_elinc);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				mDrawerLayout.openDrawer(GravityCompat.START);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setupDrawerContent(NavigationView navigationView) {
		navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
	}

	private void setupViewPager(ViewPager viewPager) {
		cardFragment = new CardFragment();
		contactFragment = new ContactFragment();
		questionFragment = new QuestionFragment();
		Fragment[] fragments = new Fragment[]{ questionFragment,contactFragment,cardFragment};
		Adapter adapter = new Adapter(getSupportFragmentManager());
		adapter.addFragment(fragments[0], "校园");
		adapter.addFragment(fragments[1], "学伴");
		adapter.addFragment(fragments[2], "目标");
		viewPager.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		initView();

		MyMessageReceiver.ehList.add(this);// 监听推送的消息
		MyMessageReceiver.mNewNum=0;//清空
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MyMessageReceiver.ehList.remove(this);// 取消监听推送的消息
	}
	
	@Override
	public void onMessage(BmobMsg message) {
		refreshNewMsg(message);
	}

	@Override
	public void onReaded(String s, String s1) {

	}


	/** 刷新界面
	  * Title: refreshNewMsg
	  * Description:
	  * param message
	  * return void
	  */
	private void refreshNewMsg(BmobMsg message){
		// 声音提示
		boolean isAllow = CustomApplcation.getInstance().getSpUtil().isAllowVoice();
		if(isAllow){
			CustomApplcation.getInstance().getMediaPlayer().start();
		}
		invalidateOptionsMenu();
		if(message!=null){
			BmobChatManager.getInstance(MainActivity.this).saveReceiveMessage(true,message);
		}
	}
	
	NewBroadcastReceiver  newReceiver;
	
	private void initNewMessageBroadCast(){
		// 注册接收消息广播
		newReceiver = new NewBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(BmobConfig.BROADCAST_NEW_MESSAGE);
		//优先级要低于ChatActivity
		intentFilter.setPriority(3);
		registerReceiver(newReceiver, intentFilter);
	}

    /**
	 * 新消息广播接收者
	 * 
	 */
	private class NewBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshNewMsg(null);//刷新界面
			abortBroadcast();// 记得把广播给终结掉
		}
	}
	
	TagBroadcastReceiver  userReceiver;
	
	private void initTagMessageBroadCast(){
		// 注册接收消息广播
		userReceiver = new TagBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(BmobConfig.BROADCAST_ADD_USER_MESSAGE);
		//优先级要低于ChatActivity
		intentFilter.setPriority(3);
		registerReceiver(userReceiver, intentFilter);
	}
	
	/**
	 * 标签消息广播接收者
	 */
	private class TagBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			BmobInvitation message = (BmobInvitation) intent.getSerializableExtra("invite");
			refreshInvite(message);
			abortBroadcast();// 记得把广播给终结掉
		}
	}
	
	@Override
	public void onNetChange(boolean isNetConnected) {
		// TODO Auto-generated method stub
		if(isNetConnected){
			ShowToast(R.string.network_tips);
		}
	}

	@Override
	public void onAddUser(BmobInvitation message) {
		// TODO Auto-generated method stub
		refreshInvite(message);
	}
	
	/** 刷新好友请求
	  * Title: notifyAddUser
	  * Description: TODO
	  * param message
	  * return void
	  */
	private void refreshInvite(BmobInvitation message){
		boolean isAllow = CustomApplcation.getInstance().getSpUtil().isAllowVoice();
		if(isAllow){
			CustomApplcation.getInstance().getMediaPlayer().start();
		}
		if(currentTabIndex==1){
			if(contactFragment != null){
				contactFragment.refresh();
			}
		}else{
			//同时提醒通知
			String tickerText = message.getFromname()+"请求添加好友";
			boolean isAllowVibrate = CustomApplcation.getInstance().getSpUtil().isAllowVibrate();
			BmobNotifyManager.getInstance(this).showNotify(isAllow,isAllowVibrate,R.drawable.ic_launcher, tickerText, message.getFromname(), tickerText,NewFriendActivity.class);
		}
	}

	@Override
	public void onOffline() {
		showOfflineDialog(this);
	}
	
	
	private static long firstTime;
	/**
	 * 连续按两次返回键就退出
	 */
	@Override
	public void onBackPressed() {
		if (firstTime + 2000 > System.currentTimeMillis()) {
			super.onBackPressed();
		} else {
			ShowToast("再按一次退出程序");
		}
		firstTime = System.currentTimeMillis();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			unregisterReceiver(newReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			unregisterReceiver(userReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class Adapter extends FragmentPagerAdapter {
		private final List<Fragment> mFragments = new ArrayList<>();
		private final List<String> mFragmentTitles = new ArrayList<>();

		public Adapter(FragmentManager fm) {
			super(fm);
		}

		public void addFragment(Fragment fragment, String title) {
			mFragments.add(fragment);
			mFragmentTitles.add(title);
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mFragmentTitles.get(position);
		}

	}

}
