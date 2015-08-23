package com.bmob.im.demo.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bmob.im.demo.R;
import com.bmob.im.demo.bean.Bug;
import com.bmob.im.demo.bean.User;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.SaveListener;

public class BugReportActivityElinc extends ActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug_report_activity_elinc);
        initTopBarForLeft("用户反馈");
        Button btn_send_bug= (Button) findViewById(R.id.btn_send_bug);

        final EditText et_bug_title= (EditText) findViewById(R.id.et_bug_title);
        final EditText et_bug_content= (EditText) findViewById(R.id.et_bug_content);
        btn_send_bug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bug bug=new Bug();
                //bug.setContent(et_bug_content.getText().toString());
                bug.setTitle(et_bug_title.getText().toString());
                bug.setSender(BmobUser.getCurrentUser(BugReportActivityElinc.this, User.class));
                bug.save(BugReportActivityElinc.this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        // TODO Auto-generated method stub
                        ShowToast("提交成功，如果出现bug，我们攻城狮会尽快修复，同时还会请你喝奶茶哦");
                        finish();
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        // TODO Auto-generated method stub
                        ShowToast("网络开销差了呢");
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bug_report_activity_elinc, menu);
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
}
