package com.bmob.im.demo.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.design.widget.*;
import android.support.v7.internal.view.menu.MenuPopupHelper;
import android.support.v7.internal.view.menu.MenuView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.inteface.EventListener;
import cn.bmob.v3.BmobUser;

import com.bmob.im.demo.CustomApplcation;
import com.bmob.im.demo.MyMessageReceiver;
import com.bmob.im.demo.R;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.ui.fragment.ContactFragment;
import com.bmob.im.demo.ui.fragment.CardFragment;
import com.bmob.im.demo.ui.fragment.QuestionFragment;
import com.bmob.im.demo.util.ImageLoadOptions;
import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 登陆
 * @ClassName: MainActivity
 * @Description: TODO
 * @author smile
 * @date 2014-5-29 下午2:45:35
 */
public class MainActivity extends ActivityBase implements EventListener{
	private CardFragment cardFragment;
	private ContactFragment contactFragment;
	private QuestionFragment questionFragment;
	private DrawerLayout mDrawerLayout;
	private User me;
	//private Boolean updated; // 今日是否已经登陆过了
	private Integer numberOfGoal;
	int currentTabIndex;
	
	//ImageView iv_recent_tips,iv_contact_tips;//消息提示
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//开启定时检测服务（单位为秒）-在这里检测后台是否还有未读的消息，有的话就取出来
		//如果你觉得检测服务比较耗流量和电量，你也可以去掉这句话-同时还有onDestory方法里面的stopPollService方法
//		BmobChat.getInstance(this).startPollService(30);
		//开启广播接收器
		initNewMessageBroadCast();
		initTagMessageBroadCast();
		initView();
		Log.i("test","main created");
	}

	private void initView(){
		/**
		 * 设置ToolBar
		 */
		View itemView = findViewById(R.id.action_add);
		me = BmobUserManager.getInstance(MainActivity.this).getCurrentUser(User.class);

		Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
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
						PopupMenu popupMenu = new PopupMenu(context,findViewById(R.id.action_add));
						popupMenu.inflate(R.menu.menu_popup);
						popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem item) {
								switch (item.getItemId()) {
									case R.id.action_add_question:
										startAnimActivity(NewQuestionActivityElinc.class);
										break;
									case R.id.action_add_contact:
										startAnimActivity(AddFriendActivity.class);
										break;
									case R.id.action_add_aim:
										startAnimActivity(NewGoalActivityElinc.class);
										break;
								}
								return false;
							}
						});
						try {
							Field mpopup=popupMenu.getClass().getDeclaredField("mPopup");
							mpopup.setAccessible(true);
							MenuPopupHelper mPopup = (MenuPopupHelper) mpopup.get(popupMenu);
							mPopup.setForceShowIcon(true);
						}catch (Exception e) {
							e.printStackTrace();
						}
						popupMenu.show();
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
				intent.setClass(MainActivity.this,SetMyInfoActivity.class);
				startAnimActivity(intent);
			}
		});

		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
					case R.id.nav_settings:
						startAnimActivity(SettingActivity.class);
						break;
					case R.id.nav_recommend:
						startAnimActivity(NearPeopleActivity.class);
						break;
					case R.id.nav_my_goal:

						Intent intent = new Intent();
						Bundle bundle = new Bundle();
						User u= BmobUser.getCurrentUser(context, User.class);
						bundle.putString("username", u.getUsername());
						if(u.getUsername().toString().equals(u.getUsername().toString())){
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

		/**
		 * 设置tabs，将带有adapter的viewpager与TabLayout一起设置
		 */
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		if (viewPager != null) {
			setupViewPager(viewPager);
		}
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

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);

//		FloatingActionButton fab_toChat = (FloatingActionButton) findViewById(R.id.fab_toChat);
//		fab_toChat.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				getSupportFragmentManager().beginTransaction()
//						.add(new RecentFragment(),"recent")
//						.addToBackStack("recent");
//				Snackbar.make(v, "学伴会话", Snackbar.LENGTH_LONG)
//						.setAction("Action", null).show();
//			}
//		});
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
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
		//recentFragment = new RecentFragment();
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
		//小圆点提示
//		if(BmobDB.create(this).hasUnReadMsg()){
//			iv_recent_tips.setVisibility(View.VISIBLE);
//		}else{
//			iv_recent_tips.setVisibility(View.GONE);
//		}
//		if(BmobDB.create(this).hasNewInvite()){
//			iv_contact_tips.setVisibility(View.VISIBLE);
//		}else{
//			iv_contact_tips.setVisibility(View.GONE);
//		}
		MyMessageReceiver.ehList.add(this);// 监听推送的消息
		//清空
		MyMessageReceiver.mNewNum=0;
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MyMessageReceiver.ehList.remove(this);// 取消监听推送的消息
	}
	
	@Override
	public void onMessage(BmobMsg message) {
		// TODO Auto-generated method stub
		refreshNewMsg(message);
	}
	
	
	/** 刷新界面
	  * @Title: refreshNewMsg
	  * @Description:
	  * @param message
	  * @return void
	  */
	private void refreshNewMsg(BmobMsg message){
		// 声音提示
		boolean isAllow = CustomApplcation.getInstance().getSpUtil().isAllowVoice();
		if(isAllow){
			CustomApplcation.getInstance().getMediaPlayer().start();
		}
//		iv_recent_tips.setVisibility(View.VISIBLE);
//		也要存储起来
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
			//刷新界面
			refreshNewMsg(null);
			// 记得把广播给终结掉
			abortBroadcast();
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
			// 记得把广播给终结掉
			abortBroadcast();
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
	  * @Title: notifyAddUser
	  * @Description: TODO
	  * @param message
	  * @return void
	  */
	private void refreshInvite(BmobInvitation message){
		boolean isAllow = CustomApplcation.getInstance().getSpUtil().isAllowVoice();
		if(isAllow){
			CustomApplcation.getInstance().getMediaPlayer().start();
		}
//		iv_contact_tips.setVisibility(View.VISIBLE);
		if(currentTabIndex==1){
			if(contactFragment != null){
				contactFragment.refresh();
			}
		}else{
			//同时提醒通知
			String tickerText = message.getFromname()+"请求添加好友";
			boolean isAllowVibrate = CustomApplcation.getInstance().getSpUtil().isAllowVibrate();
			BmobNotifyManager.getInstance(this).showNotify(isAllow,isAllowVibrate,R.drawable.ic_launcher, tickerText, message.getFromname(), tickerText.toString(),NewFriendActivity.class);
		}
	}

	@Override
	public void onOffline() {
		// TODO Auto-generated method stub
		showOfflineDialog(this);
	}
	
	@Override
	public void onReaded(String conversionId, String msgTime) {
		// TODO Auto-generated method stub
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
		}
		try {
			unregisterReceiver(userReceiver);
		} catch (Exception e) {
		}
//		取消定时检测服务
//		BmobChat.getInstance(this).stopPollService();
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
