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
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.ui.ImageBrowserActivity;
import com.bmob.im.demo.ui.PicDragZoomActivityElinc;
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
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by czr on 2015/8/3.
 */
public class AnswerListAdapter extends BaseListAdapter<Answer> {
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
    public AnswerListAdapter(Context context, List<Answer> list) {
        super(context, list);
        this.context=context;
        // TODO Auto-generated constructor stub
    }

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
        like_answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BmobQuery<User> query1 = new BmobQuery<User>();
                BmobQuery<User> query2 = new BmobQuery<User>();
                Answer ans = new Answer();
                ans.setObjectId(contract.getObjectId());
                User u=BmobUser.getCurrentUser(context,User.class);
                query1.addWhereEqualTo("objectId", u.getObjectId());
                query2.addWhereRelatedTo("likedBy", new BmobPointer(ans));
                List<BmobQuery<User>> queries = new ArrayList<BmobQuery<User>>();
                queries.add(query1);
                queries.add(query2);
                BmobQuery<User> mainQuery = new BmobQuery<User>();
                mainQuery.and(queries);
                mainQuery.count(context, User.class, new CountListener() {
                    @Override
                    public void onSuccess(int count) {
                        if (count > 0) {
                            /*如果已经点赞了*/
                            User user = BmobUser.getCurrentUser(context, User.class);
                            Answer ans = new Answer();
                            ans.setObjectId(contract.getObjectId());
                            BmobRelation relation = new BmobRelation();
                            relation.remove(user);
                            ans.setLikedBy(relation);
                            ans.update(context, new UpdateListener() {
                                @Override
                                public void onSuccess() {
                                    // TODO Auto-generated method stub
                                    refreshNumber(contract, answer_number);
                                    //ShowToast("点赞成功");
                                    Log.i("life", "多对多关联添加成功");
                                }

                                @Override
                                public void onFailure(int arg0, String arg1) {
                                    // TODO Auto-generated method stub
                                    ShowToast("亲，怎么没网了呢");
                                    Log.i("life", "多对多关联添加失败");
                                }
                            });
                        } else {
                            /*还没有点赞*/
                            User user = BmobUser.getCurrentUser(context, User.class);
                            Answer ans = new Answer();
                            ans.setObjectId(contract.getObjectId());
                            BmobRelation relation = new BmobRelation();
                            relation.add(user);
                            ans.setLikedBy(relation);
                            ans.update(context, new UpdateListener() {
                                @Override
                                public void onSuccess() {
                                    // TODO Auto-generated method stub
                                    refreshNumber(contract,answer_number);
                                    //ShowToast("点赞成功");
                                    Log.i("life", "多对多关联添加成功");
                                }

                                @Override
                                public void onFailure(int arg0, String arg1) {
                                    // TODO Auto-generated method stub
                                    ShowToast("亲，怎么没网了呢");
                                    Log.i("life", "多对多关联添加失败");
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        // TODO Auto-generated method stub
                        //ShowToast("count failure："+msg);
                        ShowToast("亲，断网了呢");
                    }
                });
            }
        });

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
        avatar_for_answer_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent =new Intent(context,ImageBrowserActivity.class);
                ArrayList<String> photos = new ArrayList<String>();
                photos.add(answeravatar);
                intent.putStringArrayListExtra("photos", photos);
                intent.putExtra("position", 0);
                context.startActivity(intent);
            }
        });
        answer_responder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                User u= BmobUser.getCurrentUser(context, User.class);
                bundle.putString("username", answer_responder.getText().toString());
                if(answer_responder.getText().toString().equals(u.getUsername().toString())){
                    bundle.putString("from", "me");
                }else{
                    bundle.putString("from", "add");
                }
                intent.putExtras(bundle);
                intent.setClass(context, SetMyInfoActivity.class);
                context.startActivity(intent);
            }
        });

        avatar_for_responder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                User u= BmobUser.getCurrentUser(context, User.class);
                bundle.putString("username", answer_responder.getText().toString());
                if(answer_responder.getText().toString().equals(u.getUsername().toString())){
                    bundle.putString("from", "me");
                }else{
                    bundle.putString("from", "add");
                }
                intent.putExtras(bundle);
                intent.setClass(context, SetMyInfoActivity.class);
                context.startActivity(intent);
            }
        });
        return convertView;
    }
    public void refreshNumber(Answer contract, final TextView answer_number){
        BmobQuery<User> query = new BmobQuery<User>();
        Answer ans = new Answer();
        ans.setObjectId(contract.getObjectId());
        //likes是Post表中的字段，用来存储所有喜欢该帖子的用户
        query.addWhereRelatedTo("likedBy", new BmobPointer(ans));
        query.count(context, User.class, new CountListener() {
            @Override
            public void onSuccess(int count) {
                answer_number.setText(count + "");
                // TODO Auto-generated method stub
                //ShowToast("Barbie has played" + count + "games");
            }

            @Override
            public void onFailure(int code, String msg) {
                // TODO Auto-generated method stub
                //ShowToast("count failure："+msg);
                ShowToast("亲，断网了呢");
            }
        });
    }

}
