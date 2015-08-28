package com.elinc.im.elinc.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elinc.im.elinc.R;
import com.elinc.im.elinc.adapter.MyTreeAdapter;
import com.elinc.im.elinc.bean.Card;
import com.elinc.im.elinc.bean.Goal;
import com.elinc.im.elinc.bean.User;
import com.elinc.im.elinc.util.ImageLoadOptions;
import com.elinc.im.elinc.view.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.FindListener;

public class MyTreeActivity extends ActivityBase {
    private RecyclerView recyclerView;
    private List<GoalRecord> goalRecordList;
    private int goalNum;
    private CircleImageView user_avatar;
    private TextView user_name,user_status;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    private int readyItemNum;
    private MyTreeAdapter myTreeAdapter;
    private View trunk;
    private String userId,str_avatar,str_username,str_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tree);
        userId = getIntent().getExtras().getString("objectId");
        str_avatar = getIntent().getExtras().getString("avatar");
        str_status = getIntent().getExtras().getString("signature");
        str_username = getIntent().getExtras().getString("username");
        //user = BmobUserManager.getInstance(MyTreeActivity.this).getCurrentUser(User.class);
        initView();
        initList();
    }

    private void initList() {
        BmobQuery<Goal> bmobQuery = new BmobQuery<>();
        User currentUser = new User();
        currentUser.setObjectId(userId);
        bmobQuery.addWhereEqualTo("author",new BmobPointer(currentUser));
        bmobQuery.findObjects(MyTreeActivity.this, new FindListener<Goal>() {
            @Override
            public void onSuccess(List<Goal> list) {
                for (int i = 0; i < list.size(); i++) {
                    goalRecordList.add(new GoalRecord(list.get(i)));
                }
                goalNum = list.size();
                readyItemNum = 0;
                for (int i = 0; i < goalNum; i++) {
                    BmobQuery<Card> bmobQuery1 = new BmobQuery<Card>();
                    bmobQuery1.addWhereEqualTo("goal",new BmobPointer(goalRecordList.get(i).getGoal()));
                    final int finalI = i;
                    bmobQuery1.findObjects(MyTreeActivity.this, new FindListener<Card>() {
                        @Override
                        public void onSuccess(List<Card> list) {
                            goalRecordList.get(finalI).setCardList(list);
                            readyItemNum++;
                            if (readyItemNum == goalNum){
                                ShowToast("查询成功");
                                myTreeAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onError(int i, String s) {
                            ShowToast("查询失败");
                        }
                    });
                }
            }

            @Override
            public void onError(int i, String s) {
                ShowToast("查询失败");
            }
        });
    }

    private void initView() {
        initTopBarForLeft("我的成长树");
        user_avatar = (CircleImageView) findViewById(R.id.avatar);
        user_name = (TextView) findViewById(R.id.user_name);
        user_status = (TextView) findViewById(R.id.user_status);
        recyclerView = (RecyclerView) findViewById(R.id.tree);

        if (str_avatar != null && !str_avatar.equals("")) {
            ImageLoader.getInstance().displayImage(str_avatar, user_avatar,
                    ImageLoadOptions.getOptions(),animateFirstListener);
        }else{
            user_avatar.setImageResource(R.drawable.head);
        }
        user_name.setText(str_username);
        if (str_status==null || str_status.equals("")){
            user_status.setText("fighting!!!");
        }else {
            user_status.setText(str_status);
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MyTreeActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        goalRecordList = new ArrayList<>();
        myTreeAdapter = new MyTreeAdapter(MyTreeActivity.this,goalRecordList);
        recyclerView.setAdapter(myTreeAdapter);

        //recyclerView.setHasFixedSize(true);

        trunk = findViewById(R.id.trunk);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LinearLayout common_actionbar = (LinearLayout) findViewById(R.id.common_actionbar);
        LinearLayout header = (LinearLayout) findViewById(R.id.header);
        int headerHeight = header.getHeight();
        int actionbarHeight = common_actionbar.getHeight();
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        int screenHeight = wm.getDefaultDisplay().getHeight();
        ((TextView)trunk).setHeight(screenHeight-actionbarHeight-headerHeight-20);
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

    public class GoalRecord{
        private Goal goal;
        private List<Card> cardList;
        private int cardNum;

        public GoalRecord(Goal goal){
            this.goal = goal;
            cardList = new ArrayList<>();
            cardNum = 0;
        }
        public Goal getGoal() {
            return goal;
        }

        public void setGoal(Goal goal) {
            this.goal = goal;
        }

        public List<Card> getCardList() {
            return cardList;
        }

        public void setCardList(List<Card> cardList) {
            this.cardList = cardList;
        }

    }
}
