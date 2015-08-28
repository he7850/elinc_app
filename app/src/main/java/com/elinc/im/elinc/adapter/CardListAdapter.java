package com.elinc.im.elinc.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.elinc.im.elinc.R;
import com.elinc.im.elinc.adapter.base.BaseListAdapter;
import com.elinc.im.elinc.adapter.base.ViewHolder;
import com.elinc.im.elinc.bean.Card;

import java.util.List;

/**
 * Created by HUBIN on 2015/8/3.
 */
public class CardListAdapter extends BaseListAdapter<Card> {

    public CardListAdapter(Context context, List<Card> list) {
        super(context, list);
    }

    @Override
    public View bindView(int arg0, View convertView, ViewGroup arg2) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_card_in_list_elinc, null);
        }
        final Card contract = getList().get(arg0);

        TextView goal_content = ViewHolder.get(convertView, R.id.goal_content);
        TextView claim = ViewHolder.get(convertView, R.id.claim);

        goal_content.setText(contract.getGoal().getGoalContent());
        claim.setText(contract.getGoal().getClaim());
//        created_at.setText(contract.getCreatedAt());
//        day.setText(contract.getGoal().getDay());

//        Button like = ViewHolder.get(convertView,R.id.add_like);
//        Button comment = ViewHolder.get(convertView,R.id.add_comment);
//        like.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Card card = new Card();
//                card.increment("likedByNum");
//                card.setObjectId(contract.getObjectId());
//                BmobRelation likedBy = contract.getLikedBy();
//                likedBy.add(new BmobPointer(BmobUser.getCurrentUser(mContext, User.class)));
//                card.setLikedBy(likedBy);
//                card.setObjectId(contract.getObjectId());
//                card.update(mContext, new UpdateListener() {
//                    @Override
//                    public void onSuccess() {
//                        ShowToast("点赞成功");
//                    }
//                    @Override
//                    public void onFailure(int i, String s) {
//                        Log.i("card_fragment","点赞失败");
//                    }
//                });
//            }
//        });
        return convertView;
    }

}
