package com.bmob.im.demo.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bmob.im.demo.R;
import com.bmob.im.demo.bean.Card;
import com.bmob.im.demo.bean.Goal;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.ui.FragmentBase;
import com.bmob.im.demo.ui.NewGoalActivityElinc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class CardFragment extends FragmentBase{
    private User me;
    private Goal[] goal;
    private int goalNum;
    private CardView goal1,goal2,goal3;
    private TextView tag1,tag2,tag3;
    private TextView title1,title2,title3;
    private TextView claim1,claim2,claim3;
    private TextView date1,date2,date3;
    private TextView fight_num1,fight_num2,fight_num3;
    private TextView comment_num1,comment_num2,comment_num3;
    private Button btn_add_goal;
    TextView card1,card2,card3;

    public CardFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        me = BmobUser.getCurrentUser(getActivity(), User.class);
        initView();
        initList();
    }

    private void initView() {
        goal1 = (CardView) findViewById(R.id.goal1);
        goal2 = (CardView) findViewById(R.id.goal2);
        goal3 = (CardView) findViewById(R.id.goal3);
        title1 = (TextView) findViewById(R.id.title1);
        title2 = (TextView) findViewById(R.id.title2);
        title3 = (TextView) findViewById(R.id.title3);
        tag1 = (TextView) findViewById(R.id.tag1);
        tag2 = (TextView) findViewById(R.id.tag2);
        tag3 = (TextView) findViewById(R.id.tag3);
        claim1 = (TextView) findViewById(R.id.claim1);
        claim2 = (TextView) findViewById(R.id.claim2);
        claim3 = (TextView) findViewById(R.id.claim3);
        date1 = (TextView) findViewById(R.id.date1);
        date2 = (TextView) findViewById(R.id.date2);
        date3 = (TextView) findViewById(R.id.date3);
        fight_num1 = (TextView) findViewById(R.id.fight_num1);
        fight_num2 = (TextView) findViewById(R.id.fight_num2);
        fight_num3 = (TextView) findViewById(R.id.fight_num3);
        comment_num1 = (TextView) findViewById(R.id.comment_num1);
        comment_num2 = (TextView) findViewById(R.id.comment_num2);
        comment_num3 = (TextView) findViewById(R.id.comment_num3);
        btn_add_goal = (Button) findViewById(R.id.btn_new_goal);
        card1 = (TextView) findViewById(R.id.btn_card1);
        card2 = (TextView) findViewById(R.id.btn_card2);
        card3 = (TextView) findViewById(R.id.btn_card3);
        btn_add_goal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnimActivity(new Intent(getActivity(), NewGoalActivityElinc.class));
                initList();
            }
        });
    }

    private void initList(){
        goal = new Goal[3];
        btn_add_goal.setVisibility(View.GONE);
        goal1.setVisibility(View.GONE);
        goal2.setVisibility(View.GONE);
        goal3.setVisibility(View.GONE);
        BmobQuery<Goal> query = new BmobQuery<>();
        //用此方式可以构造一个BmobPointer对象。只需要设置objectId就行
        query.addWhereEqualTo("author", new BmobPointer(me));
        query.addWhereNotEqualTo("out", true);
        query.findObjects(getActivity(), new FindListener<Goal>() {
            @Override
            public void onSuccess(final List<Goal> list) {
                int length = 0;
                for (int i = 0; i < list.size() && length < 3; i++) {
                    if (!list.get(i).getOut()){
                        goal[length++] = list.get(i);
                    }
                }
                goalNum = length;
                Log.i("goalNum", goalNum + "");
                btn_add_goal.setVisibility(View.VISIBLE);
                if (goalNum == 0 ){
                    btn_add_goal.setBackgroundColor(Color.parseColor("#FF7171"));
                }
                if (goalNum > 0){
                    goal1.setVisibility(View.VISIBLE);
                    btn_add_goal.setBackgroundColor(Color.parseColor("#FF7171"));
                    title1.setText(goal[0].getGoalContent());
                    tag1.setText(goal[0].getType());
                    claim1.setText(goal[0].getClaim());
                    try {
                        Calendar calendar = Calendar.getInstance();
                        Calendar calendarNow = Calendar.getInstance();
                        calendarNow.setTime(new Date());
                        String createdAt = goal[0].getCreatedAt();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                        Date date = sdf.parse(createdAt);
                        calendar.setTime(date);
                        long t1 = calendar.getTimeInMillis();
                        long t2 = calendarNow.getTimeInMillis();
                        long passedDays = (t2 - t1) / (24 * 60 * 60 * 1000);
                        if (goal[0].getDay()>passedDays){
                            date1.setText("只剩 "+(goal[0].getDay()-passedDays)+"天了");
                        }else{
                            date1.setText("过期了呢");
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    comment_num1.setText("0");
                    fight_num1.setText("0");
                    card1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog(0);
                        }
                    });
                }
                if (goalNum > 1){
                    goal2.setVisibility(View.VISIBLE);
                    btn_add_goal.setBackgroundColor(Color.parseColor("#FFEA00"));
                    title2.setText(goal[1].getGoalContent());
                    tag2.setText(goal[1].getType());
                    claim2.setText(goal[1].getClaim());
                    try {
                        Calendar calendar = Calendar.getInstance();
                        Calendar calendarNow = Calendar.getInstance();
                        calendarNow.setTime(new Date());
                        String createdAt = goal[1].getCreatedAt();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                        Date date = sdf.parse(createdAt);
                        calendar.setTime(date);
                        long t1 = calendar.getTimeInMillis();
                        long t2 = calendarNow.getTimeInMillis();
                        long passedDays = (t2 - t1) / (24 * 60 * 60 * 1000);
                        if (goal[1].getDay()>passedDays){
                            date2.setText("只剩 "+(goal[1].getDay()-passedDays)+"天了");
                        }else{
                            date2.setText("过期了呢");
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    comment_num2.setText("0");
                    fight_num2.setText("0");
                    card2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog(1);
                        }
                    });
                }
                if (goalNum > 2){
                    goal3.setVisibility(View.VISIBLE);
                    btn_add_goal.setBackgroundColor(Color.parseColor("#66CC99"));
                    title3.setText(goal[2].getGoalContent());
                    tag3.setText(goal[2].getType());
                    claim3.setText(goal[2].getClaim());
                    try {
                        Calendar calendar = Calendar.getInstance();
                        Calendar calendarNow = Calendar.getInstance();
                        calendarNow.setTime(new Date());
                        String createdAt = goal[2].getCreatedAt();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                        Date date = sdf.parse(createdAt);
                        calendar.setTime(date);
                        long t1 = calendar.getTimeInMillis();
                        long t2 = calendarNow.getTimeInMillis();
                        long passedDays = (t2 - t1) / (24 * 60 * 60 * 1000);
                        if (goal[2].getDay()>passedDays){
                            date3.setText("只剩 "+(goal[2].getDay()-passedDays)+"天了");
                        }else{
                            date3.setText("过期了呢");
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    comment_num3.setText("0");
                    fight_num3.setText("0");
                    card3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog(2);
                        }
                    });
                    btn_add_goal.setVisibility(View.GONE);
                }

            }

            @Override
            public void onError(int code, String msg) {
                Toast.makeText(getActivity(), "无法获取目标！", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void dialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_hit_card, null);
        //设置我们自己定义的布局文件作为弹出框的Content
        builder.setView(view);
        builder.setTitle("今日打卡");
        AlertDialog.Builder request = builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final EditText et = (EditText) view.findViewById(R.id.dialog_message);
                if (et.getText().toString().equals("")) {
                    ShowToast("还没有说一句话呢");
                } else {
                    Card card = new Card();
                    card.setGoal(goal[position]);
                    card.setLikedBy(new BmobRelation());
                    card.setLikedByNum(0);
                    card.setReply(new BmobRelation());
                    card.setCardClaim(et.getText().toString());
                    card.save(getContext(), new SaveListener() {
                        @Override
                        public void onSuccess() {
                            ShowToast("打卡成功");
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            ShowToast("打卡失败");
                        }
                    });
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


}
