package com.bmob.im.demo.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
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
    private List<Card>cardList;
    private User me;
    private CardListAdapter myAdapter;
    private XListView listView;
    private Button btn_chat_emo,btn_chat_send;
    private LinearLayout layout_more,layout_emo,layout_input_bar;
    private EmoticonsEditText edit_user_comment;
    private ViewPager pager_emo;
    private List<FaceText> emos;

    private int curPage = 0;
    private final int pageCapacity=10;
    private Card currentCard;
    private int currentPosition;

    public CardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_card, container, false);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initList();
        initBottomView();
        me = BmobUser.getCurrentUser(getActivity(), User.class);
    }
    //初始化基本元素
    private void initList(){
        cardList = new ArrayList<>();
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
        myAdapter = new CardListAdapter(getActivity(),cardList);
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
        query.findObjects(getActivity(), new FindListener<Card>() {
            @Override
            public void onSuccess(List<Card> list) {
                if (list.size()<pageCapacity*(curPage+1)){
                    listView.setPullLoadEnable(false);
                }else{
                    listView.setPullLoadEnable(true);
                }
                cardList.clear();
                cardList.addAll(list);
                myAdapter.notifyDataSetChanged();
                listView.stopRefresh();
                ShowToast("打卡记录获取成功");
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
        BmobQuery<Card> query = new BmobQuery<>();
        query.setSkip((curPage + 1) * pageCapacity);
        query.setLimit(pageCapacity);
        query.order("-updatedAt");
        query.include("goal");  // 希望在查询帖子信息的同时也把发布人的信息查询出来
        query.findObjects(getActivity(), new FindListener<Card>() {
            @Override
            public void onSuccess(List<Card> list) {
                if (list.size() != 0) { //拉取到新的数据
                    cardList.addAll(list);
                    curPage++;
                    myAdapter.notifyDataSetChanged();
                    ShowToast("数据加载成功");
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

    //初始化底部隐藏input bar
    private void initBottomView() {
        // 最左边
        btn_chat_emo = (Button)view.findViewById(R.id.btn_chat_emo);
        // 最右边
        btn_chat_send = (Button)view.findViewById(R.id.btn_chat_send);
        // 最下面
        layout_input_bar = (LinearLayout)findViewById(R.id.input_bar);
        layout_more = (LinearLayout)findViewById(R.id.layout_more);
        layout_emo = (LinearLayout)findViewById(R.id.layout_emo);
        //点击表情
        btn_chat_emo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layout_more.getVisibility() == View.GONE) {
                    layout_more.setVisibility(View.VISIBLE);
                    layout_emo.setVisibility(View.VISIBLE);
                    hideSoftInputView();
                } else {
                    layout_more.setVisibility(View.GONE);
                }
            }
        });
        initEmoView();      //初始化表情选择器
        // 输入框
        edit_user_comment = (EmoticonsEditText)findViewById(R.id.edit_user_comment);
        edit_user_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layout_more.getVisibility() == View.VISIBLE) {
                    layout_emo.setVisibility(View.GONE);
                    layout_more.setVisibility(View.GONE);
                }
            }
        });
        //设置当input bar失去焦点（点击list view时）隐藏输入法与评论栏
        edit_user_comment.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        layout_input_bar.setVisibility(View.GONE);
                        return true;
                }
                return false;
            }
        });
    }
    //初始化表情包
    private void initEmoView() {
        pager_emo = (ViewPager)findViewById(R.id.pager_emo);
        emos = FaceTextUtils.faceTexts;
        List<View> views = new ArrayList<>();
        for (int i = 0; i < 2; ++i) {
            views.add(getGridView(i));
        }
        pager_emo.setAdapter(new EmoViewPagerAdapter(views));
    }
    //表情包 grid view
    private View getGridView(final int i) {
        View view = View.inflate(getActivity(), R.layout.include_emo_gridview, null);
        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        List<FaceText> list = new ArrayList<>();
        if (i == 0) {
            list.addAll(emos.subList(0, 21));
        } else if (i == 1) {
            list.addAll(emos.subList(21, emos.size()));
        }
        final EmoteAdapter gridAdapter = new EmoteAdapter(getActivity(), list);
        gridview.setAdapter(gridAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                FaceText name = (FaceText) gridAdapter.getItem(position);
                //key为表情的文字编码
                String key = name.text;
                try {
                    if (edit_user_comment != null && !TextUtils.isEmpty(key)) {
                        //start为插入前位置
                        int start = edit_user_comment.getSelectionStart();
                        //将key插入原edit_user_comment中
                        CharSequence content = edit_user_comment.getText().insert(start, key);
                        edit_user_comment.setText(content);
                        // 定位光标位置
                        CharSequence info = edit_user_comment.getText();
                        if (info != null) {
                            Spannable spanText = (Spannable) info;
                            Selection.setSelection(spanText, start + key.length());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }

    // 隐藏软键盘
    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     *  card_fragment专用适配器
     * Created by HUBIN on 2015/8/3.
     */
    public class CardListAdapter extends BaseListAdapter<Card> {
        private final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        public CardListAdapter(Context context, List<Card> list) {
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
            final Card contract = getList().get(arg0);
            TextView goal_content = ViewHolder.get(convertView, R.id.goal_content);
            TextView claim = ViewHolder.get(convertView, R.id.claim);
            TextView created_at = ViewHolder.get(convertView,R.id.created_at);
            final LinearLayout comment_layout = ViewHolder.get(convertView,R.id.comment_layout);

            goal_content.setText(contract.getGoal().getGoalContent());
            claim.setText(contract.getGoal().getClaim());
            created_at.setText(contract.getCreatedAt());
            setCommentLayout(contract, comment_layout);

            //设置点赞按钮
            final Button like = ViewHolder.get(convertView,R.id.add_like);
            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BmobQuery<User>query = new BmobQuery<>();
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
                                card.setObjectId(contract.getObjectId());
                                card.increment("likedByNum");
                                BmobRelation likedBy = new BmobRelation();
                                likedBy.add(me);
                                card.setLikedBy(likedBy);
                                card.setObjectId(contract.getObjectId());
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
            final Button comment = ViewHolder.get(convertView,R.id.add_comment);
            comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSendBtnListener(cardList.get(arg0),cardList.get(arg0).getGoal().getAuthor(),true);
                }
            });
            return convertView;
        }

        private void setCommentLayout(final Card contract, final LinearLayout comment_layout) {
            //查找comment并填充至comment layout
            BmobQuery<CardReply>bmobQuery = new BmobQuery<>();
            bmobQuery.addWhereEqualTo("card", new BmobPointer(contract));
            bmobQuery.include("replyAuthor,replyTo");
            bmobQuery.order("-updatedAt");
            bmobQuery.findObjects(getActivity(), new FindListener<CardReply>() {
                @Override
                public void onSuccess(List<CardReply> list) {
                    LinearLayout linearLayout = new LinearLayout(getActivity());
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.setLayoutParams(layoutParams);
                    for (int i = 0; i < list.size(); i++) {
                        final User replyAuthor = list.get(i).getReplyAuthor();
                        final User replyTo = list.get(i).getReplyTo();
                        final String content = list.get(i).getContent();
                        String line = replyAuthor.getNick() + ":" +
                                ( replyTo.getObjectId().equals(me.getObjectId()) ?  "" : ("@"+replyTo.getNick()+"  ") ) +
                                content;
                        EmoticonsTextView commentLine = new EmoticonsTextView(getActivity());
                        commentLine.setText(line);
                        commentLine.setLayoutParams(layoutParams);
                        commentLine.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setSendBtnListener(contract,replyAuthor,false);
                            }
                        });
                        linearLayout.addView(commentLine);
                    }
                    comment_layout.removeAllViews();
                    comment_layout.addView(linearLayout);
                }
                @Override
                public void onError(int i, String s) {
                    ShowToast("查询评论失败");
                }
            });
        }
    }
    //设置发送
    private void setSendBtnListener(final Card card, final User replyTo,boolean isToOriginAuthor){
        //显示input栏
        layout_input_bar.setVisibility(View.VISIBLE);
        edit_user_comment.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
        String hint = (isToOriginAuthor?"说点什么..":"@"+replyTo.getNick());
        //设置监听
        edit_user_comment.setHint(hint);
        btn_chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String msg = edit_user_comment.getText().toString();
                if (msg.equals("")) {
                    ShowToast("请输入回复!");
                    return;
                }
                boolean isNetConnected = CommonUtils.isNetworkAvailable(getActivity());
                if (!isNetConnected) {
                    ShowToast(R.string.network_tips + "");
                    return;
                }
                CardReply cardReply = new CardReply();
                cardReply.setCard(card);
                cardReply.setContent(msg);
                cardReply.setReplyAuthor(me);
                cardReply.setReplyTo(replyTo);
                edit_user_comment.setText("");
                edit_user_comment.setVisibility(View.GONE);
                cardReply.save(getActivity(), new SaveListener() {
                    @Override
                    public void onSuccess() {
                        ShowToast("发送成功");
                        refreshList();
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        ShowToast("发送失败");
                    }
                });
            }
        });
    }

    private List<CardAndReplies>cardAndRepliesList;
    private int listLength=0;
    private boolean ready=false;
    public class CardAndReplies{
        public Card card;
        public List<CardReply> cardReplies;
        public CardAndReplies(){
            card = new Card();
            cardReplies = new ArrayList<>();
        }
        public CardAndReplies(Card arg0,List<CardReply>arg1){
            card = arg0;
            cardReplies = new ArrayList<>(arg1);
        }
    }
    private void getCardItem(final Card contract) {
        //查找comment并填充至comment layout
        final int position = listLength;
        BmobQuery<CardReply>bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("card", new BmobPointer(contract));
        bmobQuery.include("replyAuthor,replyTo");
        bmobQuery.order("-createdAt");
        bmobQuery.findObjects(getActivity(), new FindListener<CardReply>() {
            @Override
            public void onSuccess(List<CardReply> list) {
                cardAndRepliesList.add(position,new CardAndReplies(contract,list));
                listLength++;
                if (listLength==cardList.size()) {
                    myAdapter.notifyDataSetChanged();
                    listLength = 0;
                }
            }

            @Override
            public void onError(int i, String s) {
                ShowToast("查询评论失败");
                getCardItem(position,contract);
            }
        });
    }
    private void getCardItem(final int position,final Card contract) {
        //查找comment并填充至comment layout
        BmobQuery<CardReply>bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("card", new BmobPointer(contract));
        bmobQuery.include("replyAuthor,replyTo");
        bmobQuery.order("-updatedAt");
        bmobQuery.findObjects(getActivity(), new FindListener<CardReply>() {
            @Override
            public void onSuccess(List<CardReply> list) {
                cardAndRepliesList.add(position, new CardAndReplies(contract, list));
                listLength++;
                if (listLength==cardList.size()) {
                    myAdapter.notifyDataSetChanged();
                    listLength = 0;
                }
            }

            @Override
            public void onError(int i, String s) {
                ShowToast("查询评论失败");
                getCardItem(position,contract);
            }
        });
    }
    private void getCardAndRepliesList(){
        listLength = 0;
        cardAndRepliesList.clear();
        for (int i=0;i<cardList.size();i++){
            getCardItem(cardList.get(i));
        }
    }
    public class CardListAdapter2 extends BaseListAdapter<CardAndReplies> {
        private final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        public CardListAdapter2(Context context, List<CardAndReplies> list) {
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
            final LinearLayout comment_layout = ViewHolder.get(convertView, R.id.comment_layout);

            goal_content.setText(contract.card.getGoal().getGoalContent());
            claim.setText(contract.card.getGoal().getClaim());
            created_at.setText(contract.card.getCreatedAt());
            //setCommentLayout(contract, comment_layout);
            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setLayoutParams(layoutParams);
            for (int i = 0; i < contract.cardReplies.size(); i++) {
                final User replyAuthor = contract.cardReplies.get(i).getReplyAuthor();
                final User replyTo = contract.cardReplies.get(i).getReplyTo();
                final String content = contract.cardReplies.get(i).getContent();
                String line = replyAuthor.getNick() + ":" +
                        (replyTo.getObjectId().equals(me.getObjectId()) ? "" : ("@" + replyTo.getNick() + "  ")) +
                        content;
                EmoticonsTextView commentLine = new EmoticonsTextView(getActivity());
                commentLine.setText(line);
                commentLine.setLayoutParams(layoutParams);
                commentLine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setSendBtnListener(contract.card, replyAuthor, false);
                    }
                });
                linearLayout.addView(commentLine);
            }
            comment_layout.removeAllViews();
            comment_layout.addView(linearLayout);

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
                    setSendBtnListener(cardList.get(arg0), cardList.get(arg0).getGoal().getAuthor(), true);
                }
            });
            return convertView;
        }
    }
}