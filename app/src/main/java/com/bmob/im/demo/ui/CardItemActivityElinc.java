package com.bmob.im.demo.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bmob.im.demo.R;
import com.bmob.im.demo.bean.Card;
import com.bmob.im.demo.bean.CardReply;
import com.bmob.im.demo.bean.User;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.SaveListener;

public class CardItemActivityElinc extends ActivityBase{
    Bundle bundle;
    Card card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_item_activity_elinc);
        bundle=getIntent().getExtras();
        Button btn_reply= (Button) findViewById(R.id.submit_reply_of_card_e);
        btn_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(card!=null) {
                    CardReply cardReply=new CardReply();
                    cardReply.setReplyAuthor(BmobUser.getCurrentUser(CardItemActivityElinc.this, User.class));
                    EditText edit_reply_e= (EditText) findViewById(R.id.edit_reply_e);
                    cardReply.setContent(edit_reply_e.getText().toString());
                    cardReply.setCard(card);
                    cardReply.save(CardItemActivityElinc.this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            ShowToast("成功");
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            ShowToast("失败");
                        }
                    });
                }else{
                    ShowToast("三三四四");
                }
            }
        });
        card=new Card();
        BmobQuery<Card> bmobQuery = new BmobQuery<Card>();
        bmobQuery.include("goal");
        bmobQuery.getObject(this, bundle.getString("cardId"), new GetListener<Card>() {
            @Override
            public void onSuccess(Card object) {
                card = object;
                update_title();
                //updatelist();
            }

            @Override
            public void onFailure(int code, String msg) {
                ShowToast("��������");
            }
        });
    }

    public void update_title(){
        TextView card_claim_e= (TextView) findViewById(R.id.card_claim_e);
        TextView card_goal_e = (TextView) findViewById(R.id.card_goal_e);
        card_claim_e.setText(card.getCardClaim());
        card_goal_e.setText(card.getGoal().getGoalContent());
    }
}
