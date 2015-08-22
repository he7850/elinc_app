package com.bmob.im.demo.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.task.BRequest;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.QuestionListAdapter;
import com.bmob.im.demo.bean.Question;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.ui.FragmentBase;
import com.bmob.im.demo.ui.QuestionItemActivityElinc;
import com.bmob.im.demo.util.CollectionUtils;
import com.bmob.im.demo.view.xlist.XListView;
import com.bmob.im.demo.view.xlist.XListView.IXListViewListener;

/** 添加好友
 * @ClassName: SearchQuestion
 * @Description: TODO
 * @author smile
 * @date 2014-6-5 下午5:26:41
 */
public class QuestionFragment extends FragmentBase implements OnClickListener,IXListViewListener,OnItemClickListener{
    List<Question> question = new ArrayList<Question>();
    XListView mListView;
    QuestionListAdapter adapter;
    private View view;
    String searchName ="";
    private final int pageCapacity=5;
    int curPage = 0;
    ProgressDialog progress ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_question, container, false);
        /*listView = (ListView)view.findViewById(R.id.question_list);*/
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView(){
        //initTopBarForLeft("查找问题");

        initXListView();
        initAdapter();
        //refreshList();

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
        adapter = new QuestionListAdapter(getActivity(), question);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(this);

    }





    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        String questionId = question.get(position-1).getObjectId();
        //ShowToast("point"+position);
        bundle.putString("questionId", questionId);
        intent.putExtras(bundle);
        intent.setClass(getActivity(), QuestionItemActivityElinc.class);
        startAnimActivity(intent);
    }


    @Override
    public void onClick(View arg0) {

    }

    @Override
    public void onRefresh() {
        refreshList();
    }

    @Override
    public void onLoadMore() {
        BmobQuery<Question> mainQuery = new BmobQuery<Question>();

        mainQuery.setSkip((curPage + 1) * pageCapacity);
        curPage++;
        mainQuery.setLimit(pageCapacity);
        mainQuery.include("author");
        mainQuery.order("-createdAt");
        mainQuery.findObjects(getActivity(), new FindListener<Question>() {
            @Override
            public void onSuccess(List<Question> list) {
                // TODO Auto-generated method stub
                if (CollectionUtils.isNotNull(list)) {
                    if (list.size() < pageCapacity) {
                        mListView.setPullLoadEnable(false);
                        //ShowToast("问题加载完成!");
                    } else {
                        mListView.setPullLoadEnable(true);
                    }
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
        allQuery.order("-createdAt");
        allQuery.setLimit(pageCapacity);
        allQuery.findObjects(getActivity(), new FindListener<Question>() {
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
        curPage =0;
        BmobQuery<Question> mainQuery = new BmobQuery<Question>();
        mainQuery.include("author");
        mainQuery.order("-createdAt");
        mainQuery.setLimit(pageCapacity);
        mainQuery.findObjects(getActivity(), new FindListener<Question>() {
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