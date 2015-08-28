
/*
public class FindQuestion extends AppCompatActivity {


}*/


package com.elinc.im.elinc.ui;

        import java.util.ArrayList;
        import java.util.List;

        import android.app.ProgressDialog;
        import android.content.Intent;
        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.Button;
        import android.widget.EditText;

        import cn.bmob.im.util.BmobLog;
        import cn.bmob.v3.BmobQuery;
        import cn.bmob.v3.listener.FindListener;

        import com.elinc.im.elinc.R;
        import com.elinc.im.elinc.adapter.QuestionListAdapter;
        import com.elinc.im.elinc.bean.Question;
        import com.elinc.im.elinc.util.CollectionUtils;
        import com.elinc.im.elinc.view.xlist.XListView;

/** 添加好友
 * @ClassName: SearchQuestion
 * @Description: TODO
 * @author smile
 * @date 2014-6-5 下午5:26:41
 */
public class FindQuestion extends ActivityBase implements View.OnClickListener,XListView.IXListViewListener,AdapterView.OnItemClickListener {
    EditText et_search_question;
    Button btn_search_question;
    List<Question> question = new ArrayList<Question>();
    XListView mListView;
    QuestionListAdapter adapter;
    private View view;
    String searchName ="";
    private final int pageCapacity=5;
    int curPage = 0;
    ProgressDialog progress ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_question);
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_find_question, menu);
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


    private void initView(){
        initTopBarForLeft("查找问题");
        et_search_question = (EditText)findViewById(R.id.et_search_question);
        btn_search_question = (Button)findViewById(R.id.btn_search_question);
        btn_search_question.setOnClickListener(this);
        initXListView();
        initAdapter();

    }

    private void initXListView() {
        mListView = (XListView) findViewById(R.id.list_question_e);
        // 首先不允许加载更多
        mListView.setPullLoadEnable(true);
        // 不允许下拉
        mListView.setPullRefreshEnable(true);
        // 设置监听器
        mListView.setXListViewListener(this);
        //
        mListView.pullRefreshing();
        mListView.setDividerHeight(2);
        adapter = new QuestionListAdapter(FindQuestion.this, question);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(this);

    }


    private void initSearchList(final boolean isUpdate){
        if(!isUpdate){
            progress = new ProgressDialog(FindQuestion.this);
            progress.setMessage("正在搜索...");
            progress.setCanceledOnTouchOutside(true);
            progress.show();
        }
        BmobQuery<Question> eq1 = new BmobQuery<Question>();
        eq1.addWhereContains("title", et_search_question.getText().toString());
        BmobQuery<Question> eq2 = new BmobQuery<Question>();
        eq2.addWhereContains("question_content", et_search_question.getText().toString());
        BmobQuery<Question> eq3=new BmobQuery<Question>();
        eq3.addWhereContains("tags", et_search_question.getText().toString());
        List<BmobQuery<Question>> queries = new ArrayList<BmobQuery<Question>>();
        queries.add(eq1);
        queries.add(eq2);
        queries.add(eq3);
        BmobQuery<Question> mainQuery = new BmobQuery<Question>();
        mainQuery.setLimit(pageCapacity);
        mainQuery.order("-createdAt");
        mainQuery.include("author");
        mainQuery.or(queries);
        mainQuery.findObjects(FindQuestion.this, new FindListener<Question>() {
            @Override
            public void onSuccess(List<Question> list) {
                // TODO Auto-generated method stub
                if (list.size() < pageCapacity) {
                    mListView.setPullLoadEnable(false);
                    ShowToast("问题搜索完成!");
                } else {
                    mListView.setPullLoadEnable(true);
                }
                if (CollectionUtils.isNotNull(list)) {
                    if (isUpdate) {
                        question.clear();
                    }
                    adapter.addAll(list);
                } else {
                    BmobLog.i("查询成功:无返回值");
                    if (question != null) {
                        question.clear();
                    }
                    ShowToast("没有您需要的问题，去提问吧");
                }
                if (!isUpdate) {
                    progress.dismiss();
                } else {
                    refreshPull();
                }
                //这样能保证每次查询都是从头开始
                curPage = 0;
            }
            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
                BmobLog.i("查询错误:" + msg);
                if (question != null) {
                    question.clear();
                }
                ShowToast("奇怪，怎么没网了呢？");
                mListView.setPullLoadEnable(false);
                refreshPull();
                //这样能保证每次查询都是从头开始
                curPage = 0;
            }
        });

    }

    /** 查询更多
     * @Title: queryMoreNearList
     * @Description: TODO
     * @param @param page
     * @return void
     * @throws
     */
    private void onLoadMore(int page){
        BmobQuery<Question> eq1 = new BmobQuery<Question>();
        eq1.addWhereContains("title", et_search_question.getText().toString());
        BmobQuery<Question> eq2 = new BmobQuery<Question>();
        eq2.addWhereContains("question_content", et_search_question.getText().toString());
        BmobQuery<Question> eq3=new BmobQuery<Question>();
        eq3.addWhereContains("tags", et_search_question.getText().toString());
        List<BmobQuery<Question>> queries = new ArrayList<BmobQuery<Question>>();
        queries.add(eq1);
        queries.add(eq2);
        queries.add(eq3);
        BmobQuery<Question> mainQuery = new BmobQuery<Question>();
        mainQuery.include("author");
        mainQuery.setSkip((curPage + 1) * pageCapacity);
        mainQuery.setLimit(pageCapacity);
        mainQuery.order("-createdAt");
        mainQuery.or(queries);
        mainQuery.findObjects(FindQuestion.this, new FindListener<Question>() {
            @Override
            public void onSuccess(List<Question> list) {
                // TODO Auto-generated method stub
                if (list.size() < pageCapacity) {
                    mListView.setPullLoadEnable(false);
                    ShowToast("问题搜索完成!");
                } else {
                    mListView.setPullLoadEnable(true);
                }
                if (CollectionUtils.isNotNull(list)) {
                    adapter.addAll(list);

                }
                refreshLoad();
            }

            @Override
            public void onError(int i, String s) {
                // TODO Auto-generated method stub
                ShowLog("打开WIFI会死啊！根本就没网，臣妾做不到！");
                mListView.setPullLoadEnable(false);
                refreshLoad();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        String questionId = question.get(position-1).getObjectId();
        //ShowToast("point"+position);
        bundle.putString("questionId", questionId);
        intent.putExtras(bundle);
        intent.setClass(FindQuestion.this, QuestionItemActivityElinc.class);
        startAnimActivity(intent);
    }


    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        switch (arg0.getId()) {
            case R.id.btn_search_question://搜索
                question.clear();
                searchName = et_search_question.getText().toString();
                if(searchName!=null && !searchName.equals("")){
                    initSearchList(false);
                }else{
                    ShowToast("请输入搜索内容");
                }
                /*ShowToast(searchName);*/
                break;

            default:
                break;
        }
    }

    @Override
    public void onRefresh() {
        refreshList();
    }

    @Override
    public void onLoadMore() {
        BmobQuery<Question> eq1 = new BmobQuery<Question>();
        eq1.addWhereContains("title", et_search_question.getText().toString());
        BmobQuery<Question> eq2 = new BmobQuery<Question>();
        eq2.addWhereContains("question_content", et_search_question.getText().toString());
        BmobQuery<Question> eq3=new BmobQuery<Question>();
        eq3.addWhereContains("tags", et_search_question.getText().toString());
        List<BmobQuery<Question>> queries = new ArrayList<BmobQuery<Question>>();
        queries.add(eq1);
        queries.add(eq2);
        queries.add(eq3);
        BmobQuery<Question> mainQuery = new BmobQuery<Question>();
        mainQuery.include("author");
        mainQuery.setSkip((curPage + 1) * pageCapacity);
        curPage++;
        mainQuery.setLimit(pageCapacity);
        mainQuery.order("-createdAt");
        mainQuery.or(queries);
        mainQuery.findObjects(FindQuestion.this, new FindListener<Question>() {
            @Override
            public void onSuccess(List<Question> list) {
                // TODO Auto-generated method stub
                if (list.size() < pageCapacity) {
                    mListView.setPullLoadEnable(false);
                    //ShowToast("问题加载完成!");
                } else {
                    mListView.setPullLoadEnable(true);
                }
                if (CollectionUtils.isNotNull(list)) {
                    adapter.addAll(list);
                }
                refreshLoad();
            }

            @Override
            public void onError(int i, String s) {
                // TODO Auto-generated method stub
                ShowLog("搜索更多问题出错:" + s);
                mListView.setPullLoadEnable(false);
                refreshLoad();
            }
        });
    }

    private void refreshLoad(){
        if (mListView.getPullLoading()) {
            mListView.stopLoadMore();
        }
    }

    private void refreshPull(){
        if (mListView.getPullRefreshing()) {
            mListView.stopRefresh();
        }
    }

    public void initAdapter(){
        BmobQuery<Question> allQuery = new BmobQuery<Question>();
        allQuery.include("author");
        allQuery.setLimit(pageCapacity);
        allQuery.order("-createdAt");
        allQuery.findObjects(FindQuestion.this, new FindListener<Question>() {
            @Override
            public void onSuccess(List<Question> list) {
                // TODO Auto-generated method stub
                if (CollectionUtils.isNotNull(list)) {
                    question.clear();
                    adapter.addAll(list);
                    if (list.size() < pageCapacity) {
                        mListView.setPullLoadEnable(false);
                        //ShowToast("问题加载完成!");
                    } else {
                        mListView.setPullLoadEnable(true);
                    }
                } else {
                    BmobLog.i("查询成功:无返回值");
                    if (question != null) {
                        question.clear();
                    }
                    ShowToast("没有您要找的问题，去提问吧");
                    mListView.setPullLoadEnable(false);
                }
                if (!true) {
                    progress.dismiss();
                } else {
                    refreshPull();
                }
                //这样能保证每次查询都是从头开始
                curPage = 0;
            }
            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
                BmobLog.i("查询错误:" + msg);
                if (question != null) {
                    question.clear();
                }
                ShowToast("问题不存在");
                mListView.setPullLoadEnable(false);
                refreshPull();
                //这样能保证每次查询都是从头开始
                curPage = 0;
            }
        });
    }
    private void refreshList(){
        curPage = 0;
        BmobQuery<Question> eq1 = new BmobQuery<Question>();
        eq1.addWhereContains("title", et_search_question.getText().toString());
        BmobQuery<Question> eq2 = new BmobQuery<Question>();
        eq2.addWhereContains("question_content", et_search_question.getText().toString());
        BmobQuery<Question> eq3=new BmobQuery<Question>();
        eq3.addWhereContains("tags", et_search_question.getText().toString());
        List<BmobQuery<Question>> queries = new ArrayList<BmobQuery<Question>>();
        queries.add(eq1);
        queries.add(eq2);
        queries.add(eq3);
        BmobQuery<Question> mainQuery = new BmobQuery<Question>();
        mainQuery.setLimit(pageCapacity);
        mainQuery.order("-createdAt");
        mainQuery.include("author");
        mainQuery.or(queries);
        mainQuery.findObjects(FindQuestion.this, new FindListener<Question>() {
            @Override
            public void onSuccess(List<Question> list) {
                // TODO Auto-generated method stub
                if (list.size() < pageCapacity) {
                    mListView.setPullLoadEnable(false);
                    //ShowToast("问题加载完成!");
                } else {
                    mListView.setPullLoadEnable(true);
                }
                if (CollectionUtils.isNotNull(list)) {
                    question.clear();
                    adapter.addAll(list);

                    mListView.stopRefresh();
                }
                refreshLoad();
            }

            @Override
            public void onError(int i, String s) {
                // TODO Auto-generated method stub
                ShowLog("搜索更多问题出错:"+s);
                mListView.setPullLoadEnable(false);
                refreshLoad();
                mListView.stopRefresh();
            }
        });
    }
}
