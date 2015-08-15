package com.bmob.im.demo.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.EmoViewPagerAdapter;
import com.bmob.im.demo.adapter.EmoteAdapter;
import com.bmob.im.demo.adapter.base.BaseListAdapter;
import com.bmob.im.demo.adapter.base.ViewHolder;
import com.bmob.im.demo.bean.Card;
import com.bmob.im.demo.bean.CardReply;
import com.bmob.im.demo.bean.FaceText;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.ui.CardItemActivityElinc;
import com.bmob.im.demo.ui.FragmentBase;
import com.bmob.im.demo.util.CommonUtils;
import com.bmob.im.demo.util.FaceTextUtils;
import com.bmob.im.demo.view.EmoticonsEditText;
import com.bmob.im.demo.view.EmoticonsTextView;
import com.bmob.im.demo.view.xlist.XListView;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class CardFragment extends FragmentBase implements XListView.IXListViewListener{
    private View view;
    private User me;
    private CardListAdapter myAdapter;
    private XListView listView;

    private int curPage = 0;
    private final int pageCapacity=10;

    private List<CardAndReplies>cardAndRepliesList;
    public int listLength;
    public boolean ready;
    public int readyItemNum;

    public CardFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_card, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        me = BmobUser.getCurrentUser(getActivity(), User.class);
        initList();
    }

    //初始化基本元素
    private void initList(){
        cardAndRepliesList = new ArrayList<>();
        listView = (XListView)findViewById(R.id.card_list);
        // 允许加载更多
        listView.setPullLoadEnable(true);
        // 允许下拉
        listView.setPullRefreshEnable(true);
        // 设置监听器
        listView.setXListViewListener(this);
        //禁止快速下拉
        listView.setFastScrollEnabled(false);
        //设置下拉刷新
        listView.pullRefreshing();
        //设置divider高度
        listView.setDividerHeight(2);
        myAdapter = new CardListAdapter(getActivity(),cardAndRepliesList);
        listView.setAdapter(myAdapter);
        refreshList();
    }

    //刷新列表，更新前 curPage 页数据
    private void refreshList(){
        BmobQuery<Card>query = new BmobQuery<>();
        query.order("-updatedAt");
        curPage = 0;
        query.setLimit(pageCapacity * (curPage + 1));
        query.include("goal");  // 希望在查询帖子信息的同时也把发布人的信息查询出来
        ready = false;
        query.findObjects(getActivity(), new FindListener<Card>() {
            @Override
            public void onSuccess(List<Card> list) {
                CardFragment.this.listLength = list.size();
                CardFragment.this.readyItemNum = 0;
                if (list.size() < pageCapacity * (curPage + 1)) {
                    listView.setPullLoadEnable(false);
                } else {
                    listView.setPullLoadEnable(true);
                }
                cardAndRepliesList.clear();
                for (int i = 0; i < listLength; i++) {

                    cardAndRepliesList.add(new CardAndReplies(list.get(i)));

                    BmobQuery<CardReply> bmobQuery = new BmobQuery<>();
                    bmobQuery.addWhereEqualTo("card", new BmobPointer(list.get(i)));
                    bmobQuery.include("replyAuthor,replyTo");
                    bmobQuery.order("-updatedAt");
                    final int finalI = i;
                    bmobQuery.findObjects(getActivity(), new FindListener<CardReply>() {
                        @Override
                        public void onSuccess(List<CardReply> list) {
                            int listSize = list.size();
                            for (int j = 0; j < (listSize > 3 ? 3 : listSize); j++) {
                                cardAndRepliesList.get(finalI).cardReplies[j] = list.get(j);
                                cardAndRepliesList.get(finalI).replyNum = listSize;
                            }
                            CardFragment.this.readyItemNum++;
                        }

                        @Override
                        public void onError(int i, String s) {
                            ShowToast("查询评论失败");
                            ready = true;
                        }
                    });
                }
            }

            @Override
            public void onError(int i, String s) {
                listView.stopRefresh();
                Toast.makeText(getActivity(), "打卡记录获取失败", Toast.LENGTH_SHORT).show();
                ready = true;
            }
        });
    }
    //刷新列表
    @Override
    public void onRefresh() {
        refreshList();
        while(!ready){
            if ( listLength == readyItemNum){
                ready = true;
                myAdapter.notifyDataSetChanged();
                listView.stopRefresh();
            }
        }
    }
    //上拉加载更多
    @Override
    public void onLoadMore() {
        BmobQuery<Card> query = new BmobQuery<>();
        query.setSkip((curPage + 1) * pageCapacity);
        query.setLimit(pageCapacity);
        query.order("-updatedAt");
        query.include("goal");  // 希望在查询帖子信息的同时也把发布人的信息查询出来
        ready = false;
        query.findObjects(getActivity(), new FindListener<Card>() {
            @Override
            public void onSuccess(List<Card> list) {
                if (list.size() != 0) { //拉取到新的数据
                    listLength = list.size();
                    readyItemNum = 0;
                    for (int i = 0; i < list.size(); i++) {
                        int existItemNum = cardAndRepliesList.size();
                        cardAndRepliesList.add(new CardAndReplies(list.get(i)));

                        BmobQuery<CardReply> bmobQuery = new BmobQuery<>();
                        bmobQuery.addWhereEqualTo("card", new BmobPointer(list.get(i)));
                        bmobQuery.include("replyAuthor,replyTo");
                        bmobQuery.order("-updatedAt");
                        final int finalI = i + existItemNum;
                        bmobQuery.findObjects(getActivity(), new FindListener<CardReply>() {
                            @Override
                            public void onSuccess(List<CardReply> list) {
                                int listSize = list.size();
                                for (int j = 0; j < (listSize > 3 ? 3 : listSize); j++) {
                                    cardAndRepliesList.get(finalI).cardReplies[j] = list.get(j);
                                    cardAndRepliesList.get(finalI).replyNum = listSize;
                                }
                                CardFragment.this.readyItemNum++;
                            }

                            @Override
                            public void onError(int i, String s) {
                                ready = true;
                                ShowToast("查询评论失败");
                            }
                        });
                    }
                    curPage++;
                } else {                    //数据全部拉取完
                    ready = true;
                    ShowToast("数据加载完成");
                    listView.setPullLoadEnable(false);
                    listView.stopLoadMore();
                }
            }

            @Override
            public void onError(int i, String s) {
                ShowToast("数据加载失败");
                ready = true;
            }
        });

        while(!ready){
            if ( listLength == readyItemNum){
                ready = true;
                myAdapter.notifyDataSetChanged();
                listView.stopLoadMore();
            }
        }
    }


    public class CardAndReplies{
        public Card card;
        public CardReply[] cardReplies;
        public int replyNum;
        public CardAndReplies(Card card){
            this.card = card;
            cardReplies = new CardReply[3];
        }
    }

    public class CardListAdapter extends BaseListAdapter<CardAndReplies> {
        private final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        public CardListAdapter(Context context, List<CardAndReplies> list) {
            super(context, list);
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public View bindView(final int arg0, View convertView, ViewGroup arg2) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_card_in_list_elinc, null);
            }
            final CardAndReplies contract = getList().get(arg0);
            TextView goal_content = ViewHolder.get(convertView, R.id.goal_content);
            TextView claim = ViewHolder.get(convertView, R.id.claim);
            TextView created_at = ViewHolder.get(convertView, R.id.created_at);
            TextView reply_num = ViewHolder.get(convertView,R.id.reply_num);
            final LinearLayout comment_layout = ViewHolder.get(convertView, R.id.comment_layout);

            goal_content.setText(contract.card.getGoal().getGoalContent());
            claim.setText(contract.card.getCardClaim());
            created_at.setText(contract.card.getCreatedAt());
            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setLayoutParams(layoutParams);
            for (int i = 0; i < (contract.replyNum>3?3:contract.replyNum); i++) {
                final User replyAuthor = contract.cardReplies[i].getReplyAuthor();
                final User replyTo = contract.cardReplies[i].getReplyTo();
                final String content = contract.cardReplies[i].getContent();
                String line = replyAuthor.getNick() + ":" +
                        (replyTo.getObjectId().equals(me.getObjectId()) ? "" : ("@" + replyTo.getNick() + "  ")) +
                        content;
                EmoticonsTextView commentLine = new EmoticonsTextView(getActivity());
                commentLine.setText(line);
                commentLine.setLayoutParams(layoutParams);
                linearLayout.addView(commentLine);
            }
            comment_layout.removeAllViews();
            comment_layout.addView(linearLayout);
            if (contract.replyNum>3){
                reply_num.setTextColor(getResources().getColor(R.color.material_deep_teal_500));
                reply_num.setText("评论还有" + (contract.replyNum - 3) + "条，点击查看详情..");
                reply_num.setVisibility(View.VISIBLE);
                reply_num.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), CardItemActivityElinc.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("cardId",contract.card.getObjectId());
                        intent.putExtras(bundle);
                        startAnimActivity(intent);
                        refreshList();
                    }
                });
            }

            //设置点赞按钮
            final Button like = ViewHolder.get(convertView, R.id.add_like);
            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BmobQuery<User> query = new BmobQuery<>();
                    query.addWhereRelatedTo("likedBy", new BmobPointer(contract));
                    query.findObjects(mContext, new FindListener<User>() {
                        @Override
                        public void onSuccess(List<User> object) {//记录点赞
                            //查询是否已经点赞过
                            Log.i("life", "查询个数：" + object.size());
                            boolean like = false;
                            for (int i = 0; i < object.size(); i++) {
                                if (object.get(i).getObjectId().equals(me.getObjectId())) {
                                    like = true;
                                    break;
                                }
                            }
                            if (like) {
                                ShowToast("已经赞过了");
                            } else {
                                Card card = new Card();
                                card.setObjectId(contract.card.getObjectId());
                                card.increment("likedByNum");
                                BmobRelation likedBy = new BmobRelation();
                                likedBy.add(me);
                                card.setLikedBy(likedBy);
                                card.setObjectId(contract.card.getObjectId());
                                card.update(mContext, new UpdateListener() {
                                    @Override
                                    public void onSuccess() {
                                        ShowToast("点赞成功");
                                    }

                                    @Override
                                    public void onFailure(int i, String s) {
                                        Log.i("card_fragment", "点赞失败");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(int code, String msg) {
                            Log.i("life", "查询失败：" + code + "-" + msg);
                        }
                    });
                }
            });
            //设置评论按钮
            final Button comment = ViewHolder.get(convertView, R.id.add_comment);
            comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), CardItemActivityElinc.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("cardId",contract.card.getObjectId());
                    intent.putExtras(bundle);
                    startAnimActivity(intent);
                    refreshList();
                }
            });
            return convertView;
        }
    }
}