package com.bmob.im.demo.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.base.BaseListAdapter;
import com.bmob.im.demo.adapter.base.ViewHolder;
import com.bmob.im.demo.bean.Card;
import com.bmob.im.demo.bean.CardReply;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.ui.CardItemActivityElinc;
import com.bmob.im.demo.ui.FragmentBase;
import com.bmob.im.demo.view.EmoticonsTextView;
import com.bmob.im.demo.view.xlist.XListView;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class CardFragment extends FragmentBase implements XListView.IXListViewListener{
    private View view;
    private User me;
    private CardListAdapter myAdapter;
    private XListView listView;

    private int curPage = 0;
    private final int pageCapacity=10;

    private List<CardAndReplies>cardAndRepliesList;
    public int cardListLength;
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
        cardListLength = pageCapacity * (curPage + 1);
        readyItemNum = 0;
        query.setLimit(pageCapacity * (curPage + 1));
        query.include("goal");  // 希望在查询帖子信息的同时也把发布人的信息查询出来
        query.findObjects(getActivity(), new FindListener<Card>() {
            @Override
            public void onSuccess(List<Card> list) {
                cardListLength = list.size();
                readyItemNum = 0;
                if (list.size() < pageCapacity * (curPage + 1)) {
                    listView.setPullLoadEnable(false);
                } else {
                    listView.setPullLoadEnable(true);
                }
                cardAndRepliesList.clear();
                for (int i = 0; i < cardListLength; i++) {

                    cardAndRepliesList.add(new CardAndReplies(list.get(i)));

                    BmobQuery<CardReply> bmobQuery = new BmobQuery<>();
                    bmobQuery.addWhereEqualTo("card", new BmobPointer(list.get(i)));
                    bmobQuery.include("replyAuthor,replyTo");
                    bmobQuery.order("-updatedAt");
                    final int finalI = i;
                    bmobQuery.findObjects(getActivity(), new FindListener<CardReply>() {
                        @Override
                        public void onSuccess(List<CardReply> list) {
                            Log.i("update card list","card"+finalI);
                            int listSize = list.size();
                            CardReply[] cardReplies = new CardReply[3];
                            for (int i = 0; i < (listSize > 3 ? 3 : listSize); i++) {
                                cardReplies[i] = list.get(i);
                            }
                            cardAndRepliesList.get(finalI).setCardReplies(cardReplies);
                            cardAndRepliesList.get(finalI).setReplyNum(listSize);
                            Log.i("card"+finalI+"'s reply number",listSize+"");
                            readyItemNum++;
                            if ( cardListLength == readyItemNum){
                                Log.i("data","have been updated");
                                myAdapter.notifyDataSetChanged();
                                listView.stopRefresh();
                            }else{
                                Log.i("data","wait for update");
                            }
                        }

                        @Override
                        public void onError(int i, String s) {
                            ShowToast("查询评论失败");
                        }
                    });
                }
            }

            @Override
            public void onError(int i, String s) {
                listView.stopRefresh();
                Toast.makeText(getActivity(), "打卡记录获取失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //刷新列表
    @Override
    public void onRefresh() {
        refreshList();
    }
    //上拉加载更多
    @Override
    public void onLoadMore() {
        cardListLength = (curPage + 1) * pageCapacity;
        readyItemNum = 0;
        BmobQuery<Card> query = new BmobQuery<>();
        query.setSkip((curPage + 1) * pageCapacity);
        query.setLimit(pageCapacity);
        query.order("-updatedAt");
        query.include("goal");  // 希望在查询帖子信息的同时也把发布人的信息查询出来
        query.findObjects(getActivity(), new FindListener<Card>() {
            @Override
            public void onSuccess(List<Card> list) {
                if (list.size() != 0) { //拉取到新的数据
                    cardListLength = list.size();
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
                                }
                                cardAndRepliesList.get(finalI).replyNum = listSize;
                                Log.i("card"+finalI+"'s replylist number",listSize+"");
                                readyItemNum++;
                                if ( cardListLength == readyItemNum){
                                    myAdapter.notifyDataSetChanged();
                                    listView.stopLoadMore();
                                }
                            }

                            @Override
                            public void onError(int i, String s) {
                                ShowToast("查询评论失败");
                            }
                        });
                    }
                    curPage++;
                } else {                    //数据全部拉取完
                    ShowToast("数据加载完成");
                    listView.setPullLoadEnable(false);
                    listView.stopLoadMore();
                }
            }

            @Override
            public void onError(int i, String s) {
                ShowToast("数据加载失败");
            }
        });
    }


    public class CardAndReplies{
        private Card card;
        private CardReply[] cardReplies;
        private int replyNum;
        public CardAndReplies(Card card){
            this.card = card;
            cardReplies = new CardReply[3];
        }
        public int getReplyNum() {
            return replyNum;
        }

        public void setReplyNum(int replyNum) {
            this.replyNum = replyNum;
        }

        public Card getCard() {
            return card;
        }

        public void setCard(Card card) {
            this.card = card;
        }

        public CardReply[] getCardReplies() {
            return cardReplies;
        }

        public void setCardReplies(CardReply[] cardReplies) {
            this.cardReplies = cardReplies;
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
            final CardAndReplies data = getList().get(arg0);
            TextView goal_content = ViewHolder.get(convertView, R.id.goal_content);
            TextView claim = ViewHolder.get(convertView, R.id.claim);
            TextView created_at = ViewHolder.get(convertView, R.id.created_at);
            TextView reply_num = ViewHolder.get(convertView,R.id.reply_num);
            LinearLayout comment_layout = ViewHolder.get(convertView, R.id.comment_layout);

            goal_content.setText(data.getCard().getGoal().getGoalContent());
            claim.setText(data.getCard().getGoal().getClaim());
            created_at.setText(data.getCard().getCreatedAt());
            comment_layout.removeAllViews();
            for (int i = 0; i < ( data.getReplyNum() > 3 ? 3 : data.getReplyNum() ); i++) {
                User replyAuthor = data.getCardReplies()[i].getReplyAuthor();
                User replyTo = data.getCardReplies()[i].getReplyTo();
                String content = data.getCardReplies()[i].getContent();
                String line = replyAuthor.getNick() + ":" +
                        (replyTo.getObjectId().equals(me.getObjectId()) ? "" : ("@" + replyTo.getNick() + "  ")) +
                        content;
                EmoticonsTextView commentLine = new EmoticonsTextView(getActivity());
                commentLine.setText(line);
                commentLine.setLayoutParams(layoutParams);
                comment_layout.addView(commentLine);
            }
            //评论多于3条提示
            if ( data.getReplyNum() > 3 ){
                Log.i("data "+arg0,""+data.getReplyNum());
                reply_num.setTextColor(getResources().getColor(R.color.material_deep_teal_500));
                reply_num.setText("评论还有" + (data.getReplyNum() - 3) + "条，点击查看详情..");
                reply_num.setVisibility(View.VISIBLE);
                reply_num.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), CardItemActivityElinc.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("cardId",data.getCard().getObjectId());
                        intent.putExtras(bundle);
                        startAnimActivity(intent);
                        refreshList();
                    }
                });
            }else{
                reply_num.setVisibility(View.GONE);
            }

            //设置点赞按钮
            final Button like = ViewHolder.get(convertView, R.id.add_like);
            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BmobQuery<User> query = new BmobQuery<>();
                    query.addWhereRelatedTo("likedBy", new BmobPointer(data));
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
                                card.setObjectId(data.getCard().getObjectId());
                                card.increment("likedByNum");
                                BmobRelation likedBy = new BmobRelation();
                                likedBy.add(me);
                                card.setLikedBy(likedBy);
                                card.setObjectId(data.getCard().getObjectId());
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
                    bundle.putString("cardId", data.getCard().getObjectId());
                    intent.putExtras(bundle);
                    startAnimActivity(intent);
                    refreshList();
                }
            });
            return convertView;
        }
    }
}