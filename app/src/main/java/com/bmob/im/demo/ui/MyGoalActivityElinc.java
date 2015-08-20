package com.bmob.im.demo.ui;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.base.ViewHolder;
import com.bmob.im.demo.bean.Card;
import com.bmob.im.demo.bean.Goal;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.bean.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.bmob.im.BmobUserManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class MyGoalActivityElinc extends ActivityBase {
    private ListView listView;
    private Goal[] goal;
    private int goalNum;
    private MyAdapter myAdapter;
    private User me;
    private String from,username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_goal_activity_elinc);
        from = getIntent().getStringExtra("from");//me add other
        username = getIntent().getStringExtra("username");
        if (from.equals("me")) {
            initTopBarForLeft("我的目标");
        } else {
            initTopBarForLeft("Ta的目标");
        }
        me=BmobUserManager.getInstance(MyGoalActivityElinc.this).getCurrentUser(User.class);
        initList();
    }
    private void initList(){
        listView = (ListView)findViewById(R.id.my_goal_list);
        goal = new Goal[3];
        userManager.queryUser(username, new FindListener<User>() {

            @Override
            public void onError(int arg0, String arg1) {
                // TODO Auto-generated method stub
                ShowLog("onError onError:" + arg1);
            }

            @Override
            public void onSuccess(List<User> arg0) {
                // TODO Auto-generated method stub
                if (arg0 != null && arg0.size() > 0) {
                    User u = arg0.get(0);

                    BmobQuery<Goal> query = new BmobQuery<>();
                    //用此方式可以构造一个BmobPointer对象。只需要设置objectId就行
                    query.addWhereEqualTo("author", new BmobPointer(u));
                    query.addWhereNotEqualTo("out", true);
                    query.findObjects(MyGoalActivityElinc.this, new FindListener<Goal>() {
                        @Override
                        public void onSuccess(List<Goal> object) {
                            goalNum = object.size();
                            Log.i("goalNum", goalNum + "");
                            List<Map<String, String>> mapList = new ArrayList<>();
                            Map<String, String> map;
                            for (int i = 0; i < goalNum && i < 3; i++) {
                                goal[i] = object.get(i);
                                map = new HashMap<String, String>();
                                map.put("goal_content", goal[i].getGoalContent());
                                map.put("claim", goal[i].getClaim());
                                map.put("day", goal[i].getDay() + "");
                                map.put("created_at", goal[i].getCreatedAt());
                                mapList.add(map);
                            }
                            myAdapter = new MyAdapter(MyGoalActivityElinc.this, mapList, R.layout.item_my_goal_in_list_elinc,
                                    new String[]{"goal_content", "claim", "day", "created_at"},
                                    new int[]{R.id.goal_content, R.id.claim, R.id.day, R.id.created_at});
                            listView.setAdapter(myAdapter);
                            Tool.setListViewHeightBasedOnChildren(listView);
                        }

                        @Override
                        public void onError(int code, String msg) {
                            Toast.makeText(MyGoalActivityElinc.this, "无法获取目标！", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    ShowLog("onSuccess 查无此人");
                }
            }
        });
    }


    private class MyAdapter extends SimpleAdapter{
        public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            final Button button = (Button)view.findViewById(R.id.card);
            final Button delete_goal =(Button)view.findViewById(R.id.delete_goal);
            button.setTag(position);
            if (!from.equals("me")){
                button.setVisibility(View.GONE);
            }
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText et_mood= (EditText) findViewById(R.id.mood);
                    Card card = new Card();
                    card.setGoal(goal[position]);
                    card.setCardClaim(et_mood.getText().toString());
                    card.setLikedBy(new BmobRelation());
                    card.setLikedByNum(0);
                    card.setReply(new BmobRelation());
                    card.save(MyGoalActivityElinc.this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(MyGoalActivityElinc.this,"打卡成功",Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(int i, String s) {
                            Toast.makeText(MyGoalActivityElinc.this,"网络不好..郁小林给跪了 ╮(╯﹏╰）╭",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            delete_goal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Goal g = new Goal();
                    g.setObjectId(goal[position].getObjectId());
                    g.setOut(true);
                    g.update(MyGoalActivityElinc.this, new UpdateListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(MyGoalActivityElinc.this,"完成",Toast.LENGTH_SHORT).show();
                            BmobUserManager bmobUserManager = BmobUserManager.getInstance(MyGoalActivityElinc.this);
                            final User me = bmobUserManager.getCurrentUser(User.class);
                            Goal goal = new Goal();
                            BmobQuery<Goal> query= new BmobQuery<Goal>();
                            query.addWhereEqualTo("author",me);
                            query.addWhereNotEqualTo("out", true);
                            query.findObjects(MyGoalActivityElinc.this, new FindListener<Goal>() {
                                @Override
                                public void onSuccess(List<Goal> list) {
                                    int i;
                                    String[] a = new String[3];
                                    for (i = 0; i < list.size() && i < 3; i++) {
                                        a[i] = list.get(i).getType();
                                    }
                                    User u = new User();
                                    u.setObjectId(me.getObjectId());
                                    u.setTags(Arrays.asList(a));
                                    u.update(MyGoalActivityElinc.this, new UpdateListener() {
                                        @Override
                                        public void onSuccess() {
                                            Tool.alert(MyGoalActivityElinc.this, "目标完成！朝着新的目标，冲吧！");
                                            finish();
                                        }

                                        @Override
                                        public void onFailure(int i, String s) {

                                        }
                                    });

                                }
                                @Override
                                public void onError(int i, String s) {
                                }
                            });
                            finish();
                        }
                        @Override
                        public void onFailure(int i, String s) {
                            Toast.makeText(MyGoalActivityElinc.this,"网络不好..郁小林给跪了 ╮(╯﹏╰）╭",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            return view;
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_goal_activity_elinc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
