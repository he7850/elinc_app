package com.elinc.im.elinc.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.elinc.im.elinc.R;
import com.elinc.im.elinc.bean.User;
import com.elinc.im.elinc.util.CharacterParser;
import com.elinc.im.elinc.util.CollectionUtils;
import com.elinc.im.elinc.util.ImageLoadOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/** 好友列表
  * @ClassName: UserFriendAdapter
  * @Description: TODO
  * @author smile
  * @date 2014-6-12 下午3:03:40
  */
@SuppressLint("DefaultLocale")
public class UserFriendAdapter extends BaseAdapter implements SectionIndexer {
	private Context ct;
	private List<User> data;

	public UserFriendAdapter(Context ct, List<User> datas) {
		this.ct = ct;
		this.data = datas;
	}

	/** 当ListView数据发生变化时,调用此方法来更新ListView
	  * @Title: updateListView
	  * @Description:
	  * @param list
	  * @return void
	  */
	public void updateListView(List<User> list) {
		this.data = list;
		notifyDataSetChanged();
	}

	public void remove(User user){
		this.data.remove(user);
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(ct).inflate(R.layout.item_user_friend, null);
			viewHolder = new ViewHolder();
			viewHolder.alpha = (TextView) convertView.findViewById(R.id.alpha);
			viewHolder.name = (TextView) convertView.findViewById(R.id.tv_friend_name);
			viewHolder.campus = (TextView) convertView.findViewById(R.id.campus);
			viewHolder.tag1 = (TextView) convertView.findViewById(R.id.tag1);
			viewHolder.tag2 = (TextView) convertView.findViewById(R.id.tag2);
			viewHolder.tag3 = (TextView) convertView.findViewById(R.id.tag3);
			viewHolder.avatar = (ImageView) convertView.findViewById(R.id.img_friend_avatar);
			viewHolder.gender = (ImageView) convertView.findViewById(R.id.gender);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		User friend = data.get(position);
		final String name = friend.getUsername();
		final String avatar = friend.getAvatar();

		if (!TextUtils.isEmpty(avatar)) {
			ImageLoader.getInstance().displayImage(avatar, viewHolder.avatar, ImageLoadOptions.getOptions());
		} else {
			viewHolder.avatar.setImageDrawable(ct.getResources().getDrawable(R.drawable.head));
		}
		if (friend.getSex()){
			viewHolder.gender.setImageResource(R.drawable.male);
		}else {
			viewHolder.gender.setImageResource(R.drawable.female);
		}
		viewHolder.name.setText(name);
		viewHolder.campus.setText(friend.getCampus());
		viewHolder.tag2.setVisibility(View.GONE);
		viewHolder.tag3.setVisibility(View.GONE);
		viewHolder.tag1.setText(friend.getSignature().toString());
//		if (CollectionUtils.isNotNull(friend.getTags())) {
//			if (friend.getTags().size() == 0){
//				viewHolder.tag1.setText("TA还没有设置目标");
//			}
//			if (friend.getTags().size() > 0) {
//				if (friend.getTags().get(0)!=null&&!friend.getTags().get(0).equals("")) {
//					viewHolder.tag1.setText(friend.getTags().get(0));
//				}
//			}
//			if (friend.getTags().size() > 1) {
//				if (friend.getTags().get(1)!=null&&!friend.getTags().get(1).equals("")) {
//					viewHolder.tag2.setText(friend.getTags().get(1));
//				}else{
//					viewHolder.tag2.setVisibility(View.GONE);
//				}
//			} else {
//				viewHolder.tag2.setVisibility(View.GONE);
//			}
//			if (friend.getTags().size() > 2) {
//				if (friend.getTags().get(2)!=null&&!friend.getTags().get(2).equals("")) {
//					viewHolder.tag3.setText(friend.getTags().get(2));
//				}else{
//					viewHolder.tag3.setVisibility(View.GONE);
//				}
//			} else {
//				viewHolder.tag3.setVisibility(View.GONE);
//			}
//		}else {
//			viewHolder.tag1.setText("TA还没有设置目标");
//			viewHolder.tag2.setVisibility(View.GONE);
//			viewHolder.tag3.setVisibility(View.GONE);
//		}
		viewHolder.campus.setText(friend.getCampus());


		// 根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);
		// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if (position == getPositionForSection(section)) {
			viewHolder.alpha.setVisibility(View.VISIBLE);
			viewHolder.alpha.setText(CharacterParser.getInstance().getSelling(friend.getNick()).toUpperCase().substring(0, 1));
		} else {
			viewHolder.alpha.setVisibility(View.GONE);
		}

		return convertView;
	}

	static class ViewHolder {
		TextView alpha;// 首字母提示
		TextView campus;
		TextView tag1;
		TextView tag2;
		TextView tag3;
		ImageView avatar;
		ImageView gender;
		TextView name;
	}

	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return CharacterParser.getInstance().getSelling(data.get(position).getNick()).toUpperCase().charAt(0);
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	@SuppressLint("DefaultLocale")
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = CharacterParser.getInstance().getSelling(data.get(i).getNick());
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section){
				return i;
			}
		}

		return -1;
	}

	@Override
	public Object[] getSections() {
		return null;
	}

}