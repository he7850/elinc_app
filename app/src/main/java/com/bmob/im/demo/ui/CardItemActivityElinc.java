package com.bmob.im.demo.ui;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.EmoViewPagerAdapter;
import com.bmob.im.demo.adapter.EmoteAdapter;
import com.bmob.im.demo.adapter.base.BaseListAdapter;
import com.bmob.im.demo.adapter.base.ViewHolder;
import com.bmob.im.demo.bean.Card;
import com.bmob.im.demo.bean.CardReply;
import com.bmob.im.demo.bean.FaceText;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.util.FaceTextUtils;
import com.bmob.im.demo.view.EmoticonsEditText;
import com.bmob.im.demo.view.EmoticonsTextView;
import com.bmob.im.demo.view.xlist.XListView;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.SaveListener;

public class CardItemActivityElinc extends ActivityBase{
    Bundle bundle;
    TextView card_goal_e,card_claim_e;

    private Button btn_chat_emo,btn_chat_send;
    private LinearLayout layout_input_bar;
    private LinearLayout layout_more;
    private LinearLayout layout_emo;
    private EmoticonsEditText edit_user_comment;
    private ViewPager pager_emo;
    private List<FaceText> emos;

    private XListView listView;
    private ReplyListAdapter replyListAdapter;
    private List<CardReply> replyList;

    private User replyTo,me;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_item_activity_elinc);

        bundle=getIntent().getExtras();
        me = BmobUser.getCurrentUser(CardItemActivityElinc.this, User.class);

        initTitle();
        initBottomView();
        layout_input_bar.setVisibility(View.VISIBLE);
        initListView();
    }

    private void initTitle() {
        initTopBarForLeft("评论详情");
        card_goal_e = (TextView) findViewById(R.id.card_goal_e);
        card_claim_e = (TextView) findViewById(R.id.card_claim_e);
        BmobQuery<Card> bmobQuery = new BmobQuery<>();
        bmobQuery.include("goal");
        bmobQuery.getObject(this, bundle.getString("cardId"), new GetListener<Card>() {
            @Override
            public void onSuccess(Card object) {
                card_claim_e.setText(object.getCardClaim());
                card_goal_e.setText(object.getGoal().getGoalContent());
                replyTo = object.getGoal().getAuthor();
            }

            @Override
            public void onFailure(int code, String msg) {
                ShowToast("载入失败");
            }
        });
    }

    private void initListView() {
        listView = (XListView) findViewById(R.id.list_reply_of_card_e);
        listView.pullRefreshing();
        listView.setPullRefreshEnable(false);
        listView.setPullLoadEnable(false);
        listView.setFastScrollEnabled(false);
        replyList = new ArrayList<>();
        replyListAdapter = new ReplyListAdapter(CardItemActivityElinc.this,replyList);
        listView.setAdapter(replyListAdapter);
        refreshList();
        refreshList();
        refreshList();
        refreshList();
    }

    private void refreshList(){
        BmobQuery<CardReply> bmobQuery = new BmobQuery<>();
        Card card = new Card();
        card.setObjectId(bundle.getString("cardId"));
        bmobQuery.addWhereEqualTo("card", new BmobPointer(card));
        bmobQuery.include("replyAuthor,replyTo");
        bmobQuery.order("-updatedAt");
        bmobQuery.findObjects(CardItemActivityElinc.this, new FindListener<CardReply>() {
            @Override
            public void onSuccess(List<CardReply> list) {
                replyList.clear();
                replyList.addAll(list);
                replyListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(int i, String s) {
                ShowToast("查询评论失败");
            }
        });
    }

    public class ReplyListAdapter extends BaseListAdapter<CardReply> {
        public ReplyListAdapter(Context context, List<CardReply> list) {
            super(context, list);
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public View bindView(final int arg0, View convertView, ViewGroup arg2) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_reply_in_list_elinc, null);
            }
            CardReply cardReply = getList().get(arg0);
            EmoticonsTextView reply_line = ViewHolder.get(convertView, R.id.reply_line);

            final User replyAuthor = cardReply.getReplyAuthor();
            final User replyTo = cardReply.getReplyTo();
            final String content = cardReply.getContent();
            String line = replyAuthor.getNick() + ":" +
                    ( replyTo==null ?  "" : ("@"+replyTo.getNick()+"  ") ) +
                    content;
            reply_line.setText(line);
            return convertView;
        }
    }

    //初始化底部隐藏input bar
    private void initBottomView() {
        // 最左边
        btn_chat_emo = (Button)findViewById(R.id.btn_chat_emo);
        // 最右边
        btn_chat_send = (Button)findViewById(R.id.btn_chat_send);
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
        btn_chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_user_comment.getText().toString().equals("")){
                    ShowToast("评论不能为空");
                }else{
                    Card card = new Card();
                    card.setObjectId(bundle.getString("cardId"));
                    CardReply cardReply=new CardReply();
                    cardReply.setReplyAuthor(me);
                    cardReply.setReplyTo(replyTo);
                    cardReply.setContent(edit_user_comment.getText().toString());
                    cardReply.setCard(card);
                    cardReply.save(CardItemActivityElinc.this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            ShowToast("评论成功");
                            refreshList();
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            ShowToast("评论失败");
                        }
                    });
                    edit_user_comment.setText("");
                }
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
        View view = View.inflate(CardItemActivityElinc.this, R.layout.include_emo_gridview, null);
        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        List<FaceText> list = new ArrayList<>();
        if (i == 0) {
            list.addAll(emos.subList(0, 21));
        } else if (i == 1) {
            list.addAll(emos.subList(21, emos.size()));
        }
        final EmoteAdapter gridAdapter = new EmoteAdapter(CardItemActivityElinc.this, list);
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
        InputMethodManager manager = ((InputMethodManager) CardItemActivityElinc.this.getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (CardItemActivityElinc.this.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (CardItemActivityElinc.this.getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(CardItemActivityElinc.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
