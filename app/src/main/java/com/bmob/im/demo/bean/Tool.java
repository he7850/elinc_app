package com.bmob.im.demo.bean;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by czr on 2015/7/22.
 */
public  class Tool{
    public List<Question> list;

    public static void alert(Context a, String sentence){
        Toast.makeText(a, sentence, Toast.LENGTH_SHORT).show();
    }

    public static void alert(Context a, BmobException sentence){
        Toast.makeText(a, sentence.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }
    public static void getAllQuestion(final Context context){
        BmobQuery<Question> query = new BmobQuery<Question>();
        query.findObjects(context, new FindListener<Question>() {

            @Override
            public void onSuccess(List<Question> object) {
                // TODO Auto-generated method stub
                Tool.alert(context, object.get(0).getQuestionContent());
            }

            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
            }
        });
    }

    //调整listView的高度，使它可以滑动
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // params.height += 5;  // if without this statement,the listview will be a little short
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }
    
    
}
