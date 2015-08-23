package com.bmob.im.demo.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.base.BaseListAdapter;
import com.bmob.im.demo.adapter.base.ViewHolder;
import com.bmob.im.demo.bean.Answer;
import com.bmob.im.demo.bean.Goal;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.ui.ImageBrowserActivity;
import com.bmob.im.demo.ui.SetMyInfoActivity;
import com.bmob.im.demo.util.ImageLoadOptions;
import com.bmob.im.demo.view.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by zheruicao on 15/8/23.
 */
public class MyTreeAdapter extends BaseListAdapter<Goal>{
    Context context;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
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
    public MyTreeAdapter(Context context, List<Goal> list) {
        super(context, list);
        this.context=context;
        // TODO Auto-generated constructor stub
    }
    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_my_tree_elinc, null);
            TextView date_of_tree = ViewHolder.get(convertView,R.id.date_of_tree);
            TextView goal_of_tree= ViewHolder.get(convertView,R.id.goal_of_tree);
            TextView water_of_tree=ViewHolder.get(convertView,R.id.water_of_tree);
            Goal contract=getList().get(position);

            date_of_tree.setText(contract.getCreatedAt());
            goal_of_tree.setText(contract.getGoalContent());
        }
        return convertView;
    }
}


/*
public class AnswerListAdapter extends BaseListAdapter<Answer> {


    @Override
    public View bindView(int arg0, View convertView, ViewGroup arg2) {
        // TODO Auto-generated method stub
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_answer_in_list_elinc, null);
        }
        final Answer contract = getList().get(arg0);
        final Integer[] like_number = new Integer[1];
        final TextView answer_number= ViewHolder.get(convertView, R.id.answer_number);

        refreshNumber(contract,answer_number);

        TextView answer_content = ViewHolder.get(convertView, R.id.answer_content);
        TextView answer_date  =ViewHolder.get(convertView,R.id.answer_date);
        final TextView answer_responder = ViewHolder.get(convertView, R.id.answer_responder);
        CircleImageView avatar_for_responder= ViewHolder.get(convertView,R.id.avatar_for_responder);
        ImageView avatar_for_answer_iv = ViewHolder.get(convertView, R.id.avatar_for_answer_iv);
        answer_content.setText(contract.getAnswerContent());
        answer_responder.setText(contract.getResponder().getUsername());
        answer_date.setText(contract.getCreatedAt());
        String avatar=contract.getResponder().getAvatar();
        ImageView like_answer;
        like_answer = ViewHolder.get(convertView,R.id.like_answer);


        //=======================================================================
        if(avatar!=null && !avatar.equals("")){//加载头像-为了不每次都加载头像
            ImageLoader.getInstance().displayImage(avatar, avatar_for_responder, ImageLoadOptions.getOptions(),animateFirstListener);
        }else {
            avatar_for_responder.setImageResource(R.drawable.head);
        }

        final String answeravatar=contract.getAnswerAvatar();
        if(answeravatar!=null && !answeravatar.equals("")){//加载头像-为了不每次都加载头像
            ImageLoader.getInstance().displayImage(answeravatar, avatar_for_answer_iv, ImageLoadOptions.getOptions(), animateFirstListener);
            avatar_for_answer_iv.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}
*/