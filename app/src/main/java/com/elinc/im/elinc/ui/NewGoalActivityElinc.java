package com.elinc.im.elinc.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.elinc.im.elinc.R;
import com.elinc.im.elinc.bean.Goal;
import com.elinc.im.elinc.bean.Tool;
import com.elinc.im.elinc.bean.User;

import java.util.Arrays;
import java.util.List;

import cn.bmob.im.BmobUserManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class NewGoalActivityElinc extends ActivityBase implements View.OnClickListener {
    private Integer numberOfGoal;
    private String type;
    private TextView[] type1;
    private TextView tag_tv;
    private List<String> tags;
    private int day;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_goal_activity_elinc);
        initTopBarForLeft("设置新目标");
        pre_process();
        type1 = new TextView[15];
        tag_tv=(TextView) findViewById(R.id.tag_tv);
        type1[1]= (TextView) findViewById(R.id.type1);
        type1[2]= (TextView) findViewById(R.id.type2);
        type1[3]= (TextView) findViewById(R.id.type3);
        type1[4]= (TextView) findViewById(R.id.type4);
        type1[5]= (TextView) findViewById(R.id.type5);
        type1[6]= (TextView) findViewById(R.id.type6);
        type1[7]= (TextView) findViewById(R.id.type7);
        type1[8]= (TextView) findViewById(R.id.type8);
        type1[9]= (TextView) findViewById(R.id.type9);
        int i;
        for(i=1;i<=9;i++){
            type1[i].setOnClickListener(this);
        }
        type ="出国考试";

        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(numberOfGoal<3) {
                    BmobUserManager bmobUserManager = BmobUserManager.getInstance(NewGoalActivityElinc.this);
                    User me = bmobUserManager.getCurrentUser(User.class);
                    Goal goal = new Goal();
                    EditText goal_content = (EditText) findViewById(R.id.goal_content_new_goal);
                    EditText claim = (EditText) findViewById(R.id.claim_new_goal);
                    EditText day = (EditText) findViewById(R.id.day);
                    goal.setGoalContent(goal_content.getText().toString());
                    if (goal_content.getText().toString().equals("")){
                        ShowToast("请输入目标");
                        return;
                    }
                    if (claim.getText().toString().equals("")){
                        ShowToast("请输入宣言");
                        return;
                    }
                    if (day.getText().toString().equals("")){
                        ShowToast("请输入天数");
                        return;
                    }
                    goal.setClaim(claim.getText().toString());
                    goal.setOut(false);
                    goal.setType(type);
                    goal.setAuthor(me);
                    goal.setDay(Integer.parseInt(day.getText().toString()));
                    /*这段查询 比较长~~~~~~~~~~~  比较复杂，请耐心听我给你讲
                    * 首先是把填写好的东西写入数据库，新建了一个目标，
                    * 然后查询数据库里面所有该用户的有效的目标
                    * 把这最多3个目标的分类（标签）合成一个数组，放到User表里面的tags 字段里面
                    * tags 是一个  list<String>
                    *     每一次这个目标增加减少的时候，都要进行此操作*/
                    goal.save(NewGoalActivityElinc.this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            BmobUserManager bmobUserManager = BmobUserManager.getInstance(NewGoalActivityElinc.this);
                            final User me = bmobUserManager.getCurrentUser(User.class);
                            BmobQuery<Goal> query= new BmobQuery<>();
                            query.addWhereEqualTo("author", me);
                            query.addWhereNotEqualTo("out", true);
                            query.findObjects(NewGoalActivityElinc.this, new FindListener<Goal>() {
                                @Override
                                public void onSuccess(List<Goal> list) {
                                    int i;
                                    String [] a=new String[3];
                                    for(i=0;i<list.size() && i<3 ;i++){
                                        a[i]=list.get(i).getType();
                                    }
                                    User u=new User();
                                    u.setObjectId(me.getObjectId());
                                    u.setTags(Arrays.asList(a));
                                    u.update(NewGoalActivityElinc.this, new UpdateListener() {
                                        @Override
                                        public void onSuccess() {
                                            Tool.alert(NewGoalActivityElinc.this, "目标设置成功，郁小林会好好的监督你的！加油！");
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



                        }

                        @Override
                        public void onFailure(int i, String s) {

                        }
                    });
                    Log.i("1", goal_content.getText().toString());
                    Log.i("1", claim.getText().toString());
                }else{
                    Tool.alert(NewGoalActivityElinc.this,"亲，最多只能设立3个目标哦！(*^__^*)！");
                }

            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_new_aim_activity_elinc, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
    public void pre_process(){
        User u = BmobUser.getCurrentUser(NewGoalActivityElinc.this, User.class);
        BmobQuery<Goal> query = new BmobQuery<>();
        //用此方式可以构造一个BmobPointer对象。只需要设置objectId就行
        User user = new User();
        user.setObjectId(u.getObjectId());
        query.addWhereEqualTo("author", new BmobPointer(user));
        query.addWhereNotEqualTo("out",true);
        query.findObjects(this, new FindListener<Goal>() {
            @Override
            public void onSuccess(List<Goal> object) {
                numberOfGoal = object.size();
            }

            @Override
            public void onError(int code, String msg) {
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.type1:
                tag_tv.setText("标签：出国考试");
                break;
            case R.id.type2://搜索
                tag_tv.setText("标签：考研");
                break;
            case R.id.type3://搜索
                tag_tv.setText("标签：乐器");
                break;
            case R.id.type4://搜索
                tag_tv.setText("标签：四六级");
                break;
            case R.id.type5://搜索
                tag_tv.setText("标签：课程考试");
                break;
            case R.id.type6://搜索
                tag_tv.setText("标签：运动");
                break;
            case R.id.type7://搜索
                tag_tv.setText("标签：电脑技能");
                break;
            case R.id.type8://搜索
                tag_tv.setText("标签：社会考试");
                break;
            case R.id.type9://搜索
                tag_tv.setText("标签：小语种");
                break;
            default:
                break;
        }
    }
}
