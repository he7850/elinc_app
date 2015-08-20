package com.bmob.im.demo.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Bundle;
import android.support.v7.widget.CardView;
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
import com.bmob.im.demo.bean.Goal;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.ui.CardItemActivityElinc;
import com.bmob.im.demo.ui.FragmentBase;
import com.bmob.im.demo.ui.NewGoalActivityElinc;
import com.bmob.im.demo.view.EmoticonsTextView;
import com.bmob.im.demo.view.xlist.XListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class CardFragment extends FragmentBase{
    private User me;
    private Goal[] goal;
    private int goalNum;
    private CardView goal1,goal2,goal3;
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
        View view = inflater.inflate(R.layout.fragment_card, container, false);
        return view;
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
        claim1 = (TextView) findViewById(R.id.claim1);
        claim2 = (TextView) findViewById(R.id.claim2);
        claim3 = (TextView) findViewById(R.id.claim3);
        date1 = (TextView) findViewById(R.id.date1);
        date2 = (TextView) findViewById(R.id.date1);
        date3 = (TextView) findViewById(R.id.date1);
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
        BmobQuery<Goal> query = new BmobQuery<>();
        //用此方式可以构造一个BmobPointer对象。只需要设置objectId就行
        query.addWhereEqualTo("author", new BmobPointer(me));
        query.addWhereNotEqualTo("out", true);
        query.findObjects(getActivity(), new FindListener<Goal>() {
            @Override
            public void onSuccess(final List<Goal> list) {
                goalNum = list.size();
                Log.i("goalNum", goalNum + "");
                if (goalNum == 0 ){
                    goal1.setVisibility(View.GONE);
                    goal2.setVisibility(View.GONE);
                    goal3.setVisibility(View.GONE);
                    btn_add_goal.setBackgroundColor(Color.parseColor("#FF7171"));
                }
                if (goalNum > 0){
                    goal1.setVisibility(View.VISIBLE);
                    goal2.setVisibility(View.GONE);
                    goal3.setVisibility(View.GONE);
                    btn_add_goal.setBackgroundColor(Color.parseColor("#FF7171"));
                    title1.setText(list.get(0).getGoalContent());
                    claim1.setText(list.get(0).getClaim());
                    date1.setText(list.get(0).getDay().toString());
                    comment_num1.setText("3");
                    fight_num1.setText("4");
                    card1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                }
                if (goalNum > 1){
                    goal1.setVisibility(View.VISIBLE);
                    goal2.setVisibility(View.VISIBLE);
                    goal3.setVisibility(View.GONE);
                    btn_add_goal.setBackgroundColor(Color.parseColor("#FFEA00"));
                    title2.setText(list.get(1).getGoalContent());
                    claim2.setText(list.get(1).getClaim());
                    date2.setText(list.get(1).getDay());
                    comment_num2.setText("3");
                    fight_num2.setText("4");
                }
                if (goalNum > 2){
                    goal1.setVisibility(View.VISIBLE);
                    goal2.setVisibility(View.VISIBLE);
                    goal3.setVisibility(View.VISIBLE);
                    btn_add_goal.setBackgroundColor(Color.parseColor("#66CC99"));
                    title3.setText(list.get(2).getGoalContent());
                    claim3.setText(list.get(2).getClaim());
                    date3.setText(list.get(2).getDay());
                    comment_num3.setText("3");
                    fight_num3.setText("4");
                }

            }

            @Override
            public void onError(int code, String msg) {
                Toast.makeText(getActivity(), "无法获取目标！", Toast.LENGTH_SHORT).show();
            }
        });
    }
}