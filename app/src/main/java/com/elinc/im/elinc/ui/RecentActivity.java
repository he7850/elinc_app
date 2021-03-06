package com.elinc.im.elinc.ui;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.elinc.im.elinc.R;
import com.elinc.im.elinc.adapter.MessageRecentAdapter;
import com.elinc.im.elinc.view.ClearEditText;
import com.elinc.im.elinc.view.dialog.DialogTips;

import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobRecent;
import cn.bmob.im.db.BmobDB;

/**
 * 还没有加工具栏
 * 没有加入刷新机制
 */
public class RecentActivity extends ActivityBase implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ClearEditText mClearEditText;
    private ListView listview;
    private MessageRecentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_recent);
    }

    private void initView(){
        initTopBarForLeft("消息");
        listview = (ListView)findViewById(R.id.list);
        listview.setOnItemClickListener(this);
        listview.setOnItemLongClickListener(this);
        adapter = new MessageRecentAdapter(this, R.layout.item_conversation, BmobDB.create(this).queryRecents());
        listview.setAdapter(adapter);
        mClearEditText = (ClearEditText)findViewById(R.id.et_msg_search);
        mClearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    /** 删除会话
     * deleteRecent
     * @param recent
     * @return void
     */
    private void deleteRecent(BmobRecent recent){
        adapter.remove(recent);
        BmobDB.create(this).deleteRecent(recent.getTargetid());
        BmobDB.create(this).deleteMessages(recent.getTargetid());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        BmobRecent recent = adapter.getItem(position);
        showDeleteDialog(recent);
        return true;
    }

    public void showDeleteDialog(final BmobRecent recent) {
        DialogTips dialog = new DialogTips(this,recent.getUserName(),"删除会话", "确定",true,true);
        // 设置成功事件
        dialog.SetOnSuccessListener(new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int userId) {
                deleteRecent(recent);
            }
        });
        // 显示确认对话框
        dialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        BmobRecent recent = adapter.getItem(position);
        //重置未读消息
        BmobDB.create(this).resetUnread(recent.getTargetid());
        //组装聊天对象
        BmobChatUser user = new BmobChatUser();
        user.setAvatar(recent.getAvatar());
        user.setNick(recent.getNick());
        user.setUsername(recent.getUserName());
        user.setObjectId(recent.getTargetid());
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("user", user);
        startAnimActivity(intent);
    }
}
