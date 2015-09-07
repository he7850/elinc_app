package com.elinc.im.elinc.adapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.elinc.im.elinc.R;
import com.elinc.im.elinc.adapter.base.BaseListAdapter;
import com.elinc.im.elinc.adapter.base.ViewHolder;
import com.elinc.im.elinc.bean.Question;
import com.elinc.im.elinc.bean.Tool;
import com.elinc.im.elinc.bean.User;
import com.elinc.im.elinc.ui.SetMyInfoActivity;
import com.elinc.im.elinc.util.ImageLoadOptions;
import com.elinc.im.elinc.view.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import cn.bmob.v3.BmobUser;

public class QuestionListAdapter extends BaseListAdapter<Question> {
    Context context;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    public QuestionListAdapter(Context context, List<Question> list) {
        super(context, list);
        this.context=context;
        // TODO Auto-generated constructor stub
    }

    @Override
    public View bindView(int arg0, View convertView, ViewGroup arg2) {
        // TODO Auto-generated method stub
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_question_in_list_elinc, null);
        }
        final Question contract = getList().get(arg0);
        TextView question_title = ViewHolder.get(convertView, R.id.question_title);
        TextView question_content = ViewHolder.get(convertView, R.id.question_content);
        final TextView question_author = ViewHolder.get(convertView,R.id.question_author);
        TextView question_date = ViewHolder.get(convertView,R.id.question_date);
        CircleImageView user_avatar= ViewHolder.get(convertView,R.id.avatar_for_question);
        final TextView question_number_of_answer = ViewHolder.get(convertView,R.id.question_number_of_answer);
        //Button btn_add = ViewHolder.get(convertView, R.id.question_content);

        //String avatar = contract.getAvatar();

        /*if (avatar != null && !avatar.equals("")) {
            ImageLoader.getInstance().displayImage(avatar, iv_avatar, ImageLoadOptions.getOptions());
        } else {
            iv_avatar.setImageResource(R.drawable.default_head);
        }*/
        if(contract.getTitle()!=null){question_title.setText(contract.getTitle());}
        if(contract.getQuestionContent()!=null){question_content.setText(contract.getQuestionContent());}
        if(contract.getAuthor()!=null){question_author.setText(contract.getAuthor().getUsername());}
        question_date.setText(Tool.showdate(contract.getCreatedAt()));
        String avatar=contract.getAuthor().getAvatar();
        if(avatar!=null && !avatar.equals("")){//加载头像-为了不每次都加载头像
            ImageLoader.getInstance().displayImage(avatar, user_avatar, ImageLoadOptions.getOptions(),animateFirstListener);
        }else {
            user_avatar.setImageResource(R.drawable.head);
        }
        question_number_of_answer.setText("" + contract.getNumberOfAnswer());


        question_author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                User u= BmobUser.getCurrentUser(context, User.class);
                bundle.putString("username", question_author.getText().toString());
                if(question_author.getText().toString().equals(u.getUsername().toString())){
                    bundle.putString("from", "me");
                }else{
                    bundle.putString("from", "add");
                }
                intent.putExtras(bundle);
                intent.setClass(context, SetMyInfoActivity.class);
                context.startActivity(intent);
            }
        });

        user_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                User u = BmobUser.getCurrentUser(context, User.class);
                bundle.putString("username", question_author.getText().toString());
                if (question_author.getText().toString().equals(u.getUsername().toString())) {
                    bundle.putString("from", "me");
                } else {
                    bundle.putString("from", "add");
                }
                intent.putExtras(bundle);
                intent.setClass(context, SetMyInfoActivity.class);
                context.startActivity(intent);
            }
        });




        return convertView;
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

}
