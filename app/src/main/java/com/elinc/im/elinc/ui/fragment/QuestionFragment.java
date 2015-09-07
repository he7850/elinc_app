package com.elinc.im.elinc.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

import com.elinc.im.elinc.R;
import com.elinc.im.elinc.adapter.QuestionListAdapter;
import com.elinc.im.elinc.bean.Question;
import com.elinc.im.elinc.ui.FragmentBase;
import com.elinc.im.elinc.ui.QuestionItemActivityElinc;
import com.elinc.im.elinc.util.CollectionUtils;
import com.elinc.im.elinc.view.xlist.XListView;
import com.elinc.im.elinc.view.xlist.XListView.IXListViewListener;

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
    private final int pageCapacity=5;
    int curPage = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question, container, false);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initXListView();
        initAdapter();
    }

    private void initXListView() {
        mListView = (XListView) findViewById(R.id.list_question_e);
        mListView.setPullLoadEnable(true);
        mListView.setPullRefreshEnable(true);
        mListView.setXListViewListener(this);
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
    public void onResume() {
        super.onResume();
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
                if (CollectionUtils.isNotNull(list)) {
                    if (list.size() < pageCapacity) {
                        mListView.setPullLoadEnable(false);
                    } else {
                        mListView.setPullLoadEnable(true);
                    }
                    adapter.addAll(list);
                }
                refreshLoad();
            }

            @Override
            public void onError(int i, String s) {
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

    public void initData () {
        BmobQuery<Question> allQuery = new BmobQuery<Question>();
        allQuery.include("author");
        allQuery.order("-createdAt");
        allQuery.setLimit(pageCapacity);
        allQuery.findObjects(getActivity(), new FindListener<Question>() {
            @Override
            public void onSuccess(List<Question> list) {
                if (CollectionUtils.isNotNull(list)) {
                    question.clear();
                    question.addAll(list);
                } else {
                    BmobLog.i("查询成功:无返回值");
                    if (question != null) {
                        question.clear();
                    }
                }
                //这样能保证每次查询都是从头开始
                curPage = 0;
            }
            @Override
            public void onError(int code, String msg) {
                BmobLog.i("查询错误:" + msg);
                if (question != null) {
                    question.clear();
                }
                //这样能保证每次查询都是从头开始
                curPage = 0;
            }
        });
    }


    public void initAdapter(){
        if (CollectionUtils.isNotNull(question)) {
            if (question.size() < pageCapacity) {
                mListView.setPullLoadEnable(false);
            } else {
                mListView.setPullLoadEnable(true);
            }
        } else {
            mListView.setPullLoadEnable(false);
            refreshPull();
            //ShowToast("没有您要找的问题，去提问吧");
        }
        refreshPull();
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
                ShowLog("搜索更多问题出错:"+s);
                mListView.setPullLoadEnable(false);
                refreshLoad();
                mListView.stopRefresh();
            }
        });
    }
}