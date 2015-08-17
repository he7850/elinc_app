package com.bmob.im.demo.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.task.BRequest;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.NearPeopleAdapter;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.util.CollectionUtils;
import com.bmob.im.demo.view.xlist.XListView;
import com.bmob.im.demo.view.xlist.XListView.IXListViewListener;

/**
 * 附近的人列表
 * 现已被修改为推荐学伴
 * 
 * @ClassName: NewFriendActivity
 * @Description: TODO
 * @author smile
 * @date 2014-6-6 下午4:28:09
 */
public class NearPeopleActivity extends ActivityBase implements IXListViewListener,OnItemClickListener {

	XListView mListView;
	NearPeopleAdapter adapter;
	String from = "";
	User currentUser;

	List<User> nears = new ArrayList<User>();

	private double QUERY_KILOMETERS = 100;//默认查询10公里范围内的人
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_near_people);
		initView();
	}

	private void initView() {
		initTopBarForLeft("学伴推荐");
		currentUser = BmobUser.getCurrentUser(this,User.class);
		initXListView();
	}

	private void initXListView() {
		mListView = (XListView) findViewById(R.id.list_near);
		mListView.setOnItemClickListener(this);
		// 首先不允许加载更多
		mListView.setPullLoadEnable(false);
		// 允许下拉
		mListView.setPullRefreshEnable(true);
		// 设置监听器
		mListView.setXListViewListener(this);
		//
		mListView.pullRefreshing();
		
		adapter = new NearPeopleAdapter(this, nears);
		mListView.setAdapter(adapter);
		initNearByList(false);
	}

	
	int curPage = 0;
	ProgressDialog progress ;
	private void initNearByList(final boolean isUpdate){
		if(!isUpdate){
			progress = new ProgressDialog(NearPeopleActivity.this);
			progress.setMessage("正在为您推荐...");
			progress.setCanceledOnTouchOutside(true);
			progress.show();
		}
		
//		if(!mApplication.getLatitude().equals("")&&!mApplication.getLongtitude().equals("")){
//			double latitude = Double.parseDouble(mApplication.getLatitude());
//			double longtitude = Double.parseDouble(mApplication.getLongtitude());
//			//封装的查询方法，当进入此页面时 isUpdate为false，当下拉刷新的时候设置为true就行。
//			//此方法默认每页查询10条数据,若想查询多于10条，可在查询之前设置BRequest.QUERY_LIMIT_COUNT，如：BRequest.QUERY_LIMIT_COUNT=20
//			// 此方法是新增的查询指定10公里内的性别为女性的用户列表，默认包含好友列表
//			//如果你不想查询性别为女的用户，可以将equalProperty设为null或者equalObj设为null即可
//			userManager.queryKiloMetersListByPage(isUpdate,0,"location", longtitude, latitude, true,QUERY_KILOMETERS,"sex",null,new FindListener<User>() {
//			//此方法默认查询所有带地理位置信息的且性别为女的用户列表，如果你不想包含好友列表的话，将查询条件中的isShowFriends设置为false就行
////			userManager.queryNearByListByPage(isUpdate,0,"location", longtitude, latitude, true,"sex",false,new FindListener<User>() {
//
//
//				@Override
//				public void onSuccess(List<User> arg0) {
//					// TODO Auto-generated method stub
//					if (CollectionUtils.isNotNull(arg0)) {
//						if(isUpdate){
//							nears.clear();
//						}
//						adapter.addAll(arg0);
//						if(arg0.size()<BRequest.QUERY_LIMIT_COUNT){
//							mListView.setPullLoadEnable(false);
//							ShowToast("搜索完成^_^!");
//						}else{
//							mListView.setPullLoadEnable(true);
//						}
//					}else{
//						ShowToast("附近没有人T_T");
//					}
//
//					if(!isUpdate){
//						progress.dismiss();
//					}else{
//						refreshPull();
//					}
//				}
//
//				@Override
//				public void onError(int arg0, String arg1) {
//					// TODO Auto-generated method stub
//					ShowToast("附近没有人T_T");
//					mListView.setPullLoadEnable(false);
//					if(!isUpdate){
//						progress.dismiss();
//					}else{
//						refreshPull();
//					}
//				}
//
//			});
//		}else{
//			ShowToast("附近没有人T_T");
//			progress.dismiss();
//			refreshPull();
//		}

		/**之下为按标签匹配的算法
		 * by VkaZas 2015.8.16
		 */
		List<String> tags = currentUser.getTags();
		List<BmobQuery<User>> queries = new ArrayList<BmobQuery<User>>();
		BmobQuery<User> query = new BmobQuery<User>();
		if (CollectionUtils.isNotNull(tags)) {
			BmobLog.i("获取tag数量", String.valueOf(tags.size()));
			for (int i=0;i<tags.size();i++) {
				BmobLog.i("获取tag", tags.get(i));
				if (tags.get(i)!=null) {
					query.addWhereContains("tags",tags.get(i));
					queries.add(query);
				}
			}
		}
		BmobQuery<User> orQuery = new BmobQuery<User>();
		orQuery.or(queries);
		orQuery.findObjects(this, new FindListener<User>() {

				@Override
				public void onSuccess(List<User> arg0) {
					// TODO Auto-generated method stub
					if (CollectionUtils.isNotNull(arg0)) {
						if(isUpdate){
							nears.clear();
						}
						for (int i=0;i<arg0.size();i++) {
							BmobLog.i("匹配列表中的用户",arg0.get(i).getUsername());
							if (arg0.get(i).getUsername().equals(currentUser.getUsername())) {
								arg0.remove(i);
								break;
							}
						}
						for (int i=arg0.size();i>=0;i--) {
							if (i>4) {
								arg0.remove(i);
							}
						}
						adapter.addAll(arg0);
						if(arg0.size()<BRequest.QUERY_LIMIT_COUNT){
							mListView.setPullLoadEnable(false);
							ShowToast("匹配完成^_^!");
						}else{
							mListView.setPullLoadEnable(true);
						}
					}else{
						ShowToast("没有匹配到人T_T");
					}

					if(!isUpdate){
						progress.dismiss();
					}else{
						refreshPull();
					}
				}

				@Override
				public void onError(int arg0, String arg1) {
					// TODO Auto-generated method stub
					ShowToast("没有匹配到人T_T");
					mListView.setPullLoadEnable(false);
					if(!isUpdate){
						progress.dismiss();
					}else{
						refreshPull();
					}
				}
		});
	}
	
	/** 查询更多
	  * @Title: queryMoreNearList
	  * @Description: TODO
	  * @param @param page 
	  * @return void
	  * @throws
	  */
	private void queryMoreNearList(int page){
		double latitude = Double.parseDouble(mApplication.getLatitude());
		double longtitude = Double.parseDouble(mApplication.getLongtitude());
		//查询10公里范围内的性别为女的用户列表
		userManager.queryKiloMetersListByPage(true,page,"location", longtitude, latitude, true,QUERY_KILOMETERS,"sex",false,new FindListener<User>() {
		//查询全部地理位置信息且性别为女性的用户列表
//		userManager.queryNearByListByPage(true,page, "location", longtitude, latitude, true,"sex",false,new FindListener<User>() {

			@Override
			public void onSuccess(List<User> arg0) {
				// TODO Auto-generated method stub
				if (CollectionUtils.isNotNull(arg0)) {
					adapter.addAll(arg0);
				}
				refreshLoad();
			}
			
			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				ShowLog("查询更多附近的人出错:"+arg1);
				mListView.setPullLoadEnable(false);
				refreshLoad();
			}

		});
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
		User user = (User) adapter.getItem(position-1);
		Intent intent =new Intent(this,SetMyInfoActivity.class);
		intent.putExtra("from", "add");
		intent.putExtra("username", user.getUsername());
		startAnimActivity(intent);		
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		initNearByList(true);
	}

	private void refreshLoad(){
		if (mListView.getPullLoading()) {
			mListView.stopLoadMore();
		}
	}
	
	private void refreshPull(){
		if (mListView.getPullRefreshing()) {
			mListView.stopRefresh();
		}
	}
	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		double latitude = Double.parseDouble(mApplication.getLatitude());
		double longtitude = Double.parseDouble(mApplication.getLongtitude());
		//这是查询10公里范围内的性别为女用户总数
		userManager.queryKiloMetersTotalCount(User.class, "location", longtitude, latitude, true,QUERY_KILOMETERS,"sex",false,new CountListener() {
	    //这是查询附近的人且性别为女性的用户总数
//		userManager.queryNearTotalCount(User.class, "location", longtitude, latitude, true,"sex",false,new CountListener() {
			
			@Override
			public void onSuccess(int arg0) {
				// TODO Auto-generated method stub
				if(arg0 >nears.size()){
					curPage++;
					queryMoreNearList(curPage);
				}else{
					ShowToast("数据加载完成");
					mListView.setPullLoadEnable(false);
					refreshLoad();
				}
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub
				ShowLog("查询附近的人总数失败"+arg1);
				refreshLoad();
			}
		});
		
	}

}
