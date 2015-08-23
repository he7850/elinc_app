package com.bmob.im.demo.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.base.BaseListAdapter;
import com.bmob.im.demo.adapter.base.ViewHolder;
import com.bmob.im.demo.bean.Answer;
import com.bmob.im.demo.bean.Card;
import com.bmob.im.demo.bean.Goal;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.ui.ImageBrowserActivity;
import com.bmob.im.demo.ui.MyTreeActivity;
import com.bmob.im.demo.ui.SetMyInfoActivity;
import com.bmob.im.demo.util.ImageLoadOptions;
import com.bmob.im.demo.view.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by zheruicao on 15/8/23.
 */
public class MyTreeAdapter extends RecyclerView.Adapter{
    private Context context;
    private List<MyTreeActivity.GoalRecord> goalRecordList;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    public MyTreeAdapter(List<MyTreeActivity.GoalRecord> goalRecordList){
        this.goalRecordList = goalRecordList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_my_tree_elinc, null);
        context = viewGroup.getContext();
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int i) {
        final MyViewHolder myViewHolder = (MyViewHolder)viewHolder;
        String createdAt = goalRecordList.get(i).getGoal().getCreatedAt();
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            Date date = sdf.parse(createdAt);
            calendar.setTime(date);
            String dateStr = "";
            dateStr+=calendar.get(Calendar.YEAR)+"."+calendar.get(Calendar.MONTH)+"."+calendar.get(Calendar.DATE);
            myViewHolder.date_of_tree.setText(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        myViewHolder.goal_of_tree.setText(goalRecordList.get(i).getGoal().getGoalContent());
        myViewHolder.water_of_tree.setText("0");
        if (goalRecordList.get(i).getGoal().getOut()) {
            myViewHolder.leaf.setImageResource(R.drawable.fruit);
        }

        myViewHolder.card_list.removeAllViews();
        List<Card> cardList = goalRecordList.get(i).getCardList();
        for (int j = 0; j < cardList.size(); j++) {
            LinearLayout cardLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.card_in_tree_elinc,null);
            CardViewHolder cardViewHolder = new CardViewHolder(cardLayout);
            try {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                Date date = sdf.parse(cardList.get(j).getCreatedAt());
                calendar.setTime(date);
                String dateStr = calendar.get(Calendar.YEAR)+"."+calendar.get(Calendar.MONTH)+"."+calendar.get(Calendar.DATE);
                cardViewHolder.card_date.setText(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cardViewHolder.card_claim.setText(cardList.get(j).getCardClaim());
            //cardViewHolder.card_emotion.setImageResource(R.drawable.face);
            myViewHolder.card_list.addView(cardLayout);
        }

        if (!myViewHolder.isDisplay){
            myViewHolder.card_list.setVisibility(View.GONE);
        }else {
            myViewHolder.card_list.setVisibility(View.VISIBLE);
        }
        myViewHolder.triangle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("1","click");
                if (!myViewHolder.isDisplay)
                    myViewHolder.card_list.setVisibility(View.VISIBLE);
                AlphaAnimation alphaAnimation;
                if (myViewHolder.isDisplay) {
                    alphaAnimation = new AlphaAnimation(1, 0);
                    alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            myViewHolder.card_list.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    myViewHolder.isDisplay = false;
                }else{
                    alphaAnimation = new AlphaAnimation(0, 1);
                    myViewHolder.isDisplay = true;
                }
                alphaAnimation.setDuration(500);
                myViewHolder.card_list.startAnimation(alphaAnimation);
            }
        });

    }

    @Override
    public int getItemCount() {
        return goalRecordList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        public boolean isDisplay;
        public TextView date_of_tree;
        public TextView goal_of_tree;
        public TextView water_of_tree;
        public ImageView leaf,triangle,water;
        public LinearLayout card_list;
        public MyViewHolder(View itemView) {
            super(itemView);
            date_of_tree = (TextView) itemView.findViewById(R.id.date_of_tree);
            goal_of_tree = (TextView) itemView.findViewById(R.id.goal_of_tree);
            water_of_tree = (TextView) itemView.findViewById(R.id.water_of_tree);
            card_list = (LinearLayout) itemView.findViewById(R.id.card_list);
            leaf = (ImageView) itemView.findViewById(R.id.leaf_of_tree);
            triangle = (ImageView) itemView.findViewById(R.id.triangle);
            water = (ImageView) itemView.findViewById(R.id.water);
            isDisplay = false;
        }
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

    public class CardViewHolder{
        TextView card_date,card_claim;
        ImageView card_emotion;
        public CardViewHolder(View itemView) {
            card_date = (TextView) itemView.findViewById(R.id.card_date);
            card_claim = (TextView) itemView.findViewById(R.id.card_claim);
            card_emotion = (ImageView) itemView.findViewById(R.id.card_emotion);
        }
    }
}
