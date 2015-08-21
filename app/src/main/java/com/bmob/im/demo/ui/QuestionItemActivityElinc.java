package com.bmob.im.demo.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.AddFriendAdapter;
import com.bmob.im.demo.adapter.AnswerListAdapter;
import com.bmob.im.demo.adapter.QuestionListAdapter;
import com.bmob.im.demo.bean.Answer;
import com.bmob.im.demo.bean.Question;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.config.BmobConstants;
import com.bmob.im.demo.util.CollectionUtils;
import com.bmob.im.demo.util.ImageLoadOptions;
import com.bmob.im.demo.util.PhotoUtil;
import com.bmob.im.demo.view.xlist.XListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.task.BRequest;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.BmobPushManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.QuerySMSStateListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import static com.bmob.im.demo.R.drawable.remove_favorite_e;

/**
 * Created by HUBIN on 2015/7/25.
 */
public class QuestionItemActivityElinc extends ActivityBase  implements View.OnClickListener,XListView.IXListViewListener,AdapterView.OnItemClickListener {
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    Bundle bundle;
    List<Map<String,String>> mapList;
    SimpleAdapter questionAdapter;
    List<Answer> answer = new ArrayList<Answer>();
    XListView mListView;
    AnswerListAdapter adapter;
    int curPage = 0;
    ProgressDialog progress;
    String url;
    ImageView detail_question;
    Answer new_answer;
    Button submit_answer_avatar;
    ImageView answer_iv;
    LinearLayout layout_all_of_question_detail;
    TextView author_in_question_list;
    ImageView avatar_for_author_in_question_list;
    TextView question_date_in_question_list;
    String author_name;
    Question question;
    ImageButton follow;
    User author;

    private void initXListView() {
        mListView = (XListView) findViewById(R.id.list_answer_e);
        // 首先不允许加载更多
        mListView.setPullLoadEnable(true);
        // 不允许下拉
        mListView.setPullRefreshEnable(true);
        // 设置监听器
        mListView.setXListViewListener(this);
        //
        mListView.pullRefreshing();

        adapter = new AnswerListAdapter(this, answer);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail_elinc);

        manager = BmobChatManager.getInstance(this);

        author_in_question_list= (TextView) findViewById(R.id.author_in_question_list);

        avatar_for_author_in_question_list= (ImageView) findViewById(R.id.avatar_for_author_in_question_list);
        submit_answer_avatar= (Button) findViewById(R.id.submit_answer_avatar);
        submit_answer_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAvatarPop();
            }
        });

        answer_iv = (ImageView) findViewById(R.id.answer_iv);
        author_in_question_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserDetail();
            }
        });

        avatar_for_author_in_question_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserDetail();
            }
        });



        detail_question= (ImageView) findViewById(R.id.detail_question);
        detail_question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(QuestionItemActivityElinc.this,ImageBrowserActivity.class);
                ArrayList<String> photos = new ArrayList<String>();
                photos.add(url);
                intent.putStringArrayListExtra("photos", photos);
                intent.putExtra("position", 0);
                QuestionItemActivityElinc.this.startActivity(intent);
            }
        });
        bundle = getIntent().getExtras();
        new_answer=new Answer();
        layout_all_of_question_detail= (LinearLayout) findViewById(R.id.layout_all_of_question_detail);
        /*点击按钮收藏*/
        follow= (ImageButton) findViewById(R.id.follow);

        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuestionItemActivityElinc.this.followQustion();
            }
        });
        /*点击按钮 提交答案*/
        final Button submit= (Button) findViewById(R.id.submit_answer);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText a = (EditText) findViewById(R.id.edit_answer);
                QuestionItemActivityElinc.this.submitAnswer(a.getText().toString());
            }
        });
        initBasicView();
        initQuestionContent();
        initList();
        refreshList();
    }
    private void initQuestionContent(){
        String id=bundle.getString("questionId");
        BmobQuery<Question> bmobQuery = new BmobQuery<Question>();
        bmobQuery.include("author");
        bmobQuery.getObject(this, id, new GetListener<Question>() {
            @Override
            public void onSuccess(Question object) {
                question = object;
                refreshButton();
                // TODO Auto-generated method stub
                TextView titleTV = (TextView) findViewById(R.id.question_item_title);
                titleTV.setText(object.getTitle());
                TextView contentTV = (TextView) findViewById(R.id.question_item_question_content);
                contentTV.setText(object.getQuestionContent());
                question_date_in_question_list = (TextView) findViewById(R.id.question_date_in_question_list);
                question_date_in_question_list.setText(object.getCreatedAt());
                TextView question_detail_tags= (TextView) findViewById(R.id.question_detail_tags);
                question_detail_tags.setText(object.getTags().toString());
                author_in_question_list.setText(object.getAuthor().getUsername());
                author = object.getAuthor();
                author_name = object.getAuthor().getUsername();
                String avatar;

                avatar = object.getAuthor().getAvatar();
                if (avatar != null && !avatar.equals("")) {//加载头像-为了不每次都加载头像
                    ImageLoader.getInstance().displayImage(avatar, avatar_for_author_in_question_list, ImageLoadOptions.getOptions(), animateFirstListener);
                } else {
                    avatar_for_author_in_question_list.setImageResource(R.drawable.head);
                }


                if (object.getQuestionAvatar() != null && object.getQuestionAvatar() != "") {
                    detail_question.setVisibility(View.VISIBLE);
                    url = object.getQuestionAvatar();
                    ImageLoader.getInstance().displayImage(url, detail_question,
                            ImageLoadOptions.getOptions());
                }
                //Tool.alert(QuestionItemActivityElinc.this,"查询成功");
            }

            @Override
            public void onFailure(int code, String msg) {
                // TODO Auto-generated method stub
                Tool.alert(QuestionItemActivityElinc.this, "查询失败：" + msg);
            }
        });
    }
    private void initBasicView() {
        initTopBarForLeft("校园问答");
    }
    private void initList(){
        initXListView();
        BmobQuery<Answer> bmobQuery = new BmobQuery<Answer>();
        bmobQuery.addWhereEqualTo("questionId", bundle.getString("questionId"));
        bmobQuery.include("responder,questionId");
        bmobQuery.order("-createdAt");
        bmobQuery.findObjects(QuestionItemActivityElinc.this, new FindListener<Answer>() {
            @Override
            public void onSuccess(List<Answer> list) {

                // TODO Auto-generated method stub
                if (CollectionUtils.isNotNull(list)) {
                    answer.clear();

                    adapter.addAll(list);
                    if (list.size() < BRequest.QUERY_LIMIT_COUNT) {
                        mListView.setPullLoadEnable(false);
                        //ShowToast("问题搜索完成!");
                    } else {
                        mListView.setPullLoadEnable(true);
                    }
                } else {
                    BmobLog.i("查询成功:无返回值");
                    if (answer != null) {
                        answer.clear();
                    }
                    ShowToast("没有答案，添加一个把");
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
            public void onError(int i, String s) {
                Tool.alert(QuestionItemActivityElinc.this, "提交失败，请检查网络");
            }
        });
    }

    @Override
    public void onLoadMore() {
        // TODO Auto-generated method stub
        BmobQuery<Answer> bmobQuery = new BmobQuery<Answer>();
        bmobQuery.addWhereEqualTo("questionId", bundle.getString("questionId"));
        bmobQuery.include("questionId,responder");
        bmobQuery.order("-createdAt");
        bmobQuery.findObjects(QuestionItemActivityElinc.this, new FindListener<Answer>() {
            @Override
            public void onSuccess(List<Answer> list) {
                // TODO Auto-generated method stub
                if (list.size() > answer.size()) {
                    curPage++;
                    queryMoreSearchList(curPage);
                } else {
                   // ShowToast("数据加载完成");
                    mListView.setPullLoadEnable(false);
                    refreshLoad();
                }
            }

            @Override
            public void onError(int i, String s) {
                ShowLog("查询附近的人总数失败" + s);
                refreshLoad();
            }
        });

    }

    private void queryMoreSearchList(int page){
        BmobQuery<Answer> bmobQuery = new BmobQuery<Answer>();
        bmobQuery.addWhereEqualTo("questionId", bundle.getString("questionId"));
        bmobQuery.include("questionId,responder");
        bmobQuery.order("-createdAt");
        bmobQuery.findObjects(QuestionItemActivityElinc.this, new FindListener<Answer>() {
            @Override
            public void onSuccess(List<Answer> arg0) {
                // TODO Auto-generated method stub
                if (CollectionUtils.isNotNull(arg0)) {
                    answer.clear();
                    adapter.addAll(arg0);
                }
                refreshLoad();
            }

            @Override
            public void onError(int arg0, String arg1) {
                // TODO Auto-generated method stub
                ShowLog("搜索更多用户出错:" + arg1);
                mListView.setPullLoadEnable(false);
                refreshLoad();
            }

        });
    }



    public void followQustion(){
        /*此方法不需要传参数，只能被QustionItemActivity的关注按钮的毁掉函数调用
        * 会自动从bundle 里面获取本question的Id 进行关注
        * 从而用户的关注里面会多一个问题
        *
        *                           czr
        * */
        BmobQuery<Question> query1 = new BmobQuery<Question>();
        BmobQuery<Question> query2 = new BmobQuery<Question>();
        Question q=new Question();
        q.setObjectId(question.getObjectId());
        User u=BmobUser.getCurrentUser(QuestionItemActivityElinc.this, User.class);
        query1.addWhereEqualTo("objectId", q.getObjectId());
        query2.addWhereRelatedTo("follow", new BmobPointer(u));
        List<BmobQuery<Question>> queries = new ArrayList<BmobQuery<Question>>();
        queries.add(query1);
        queries.add(query2);
        BmobQuery<Question> mainQuery = new BmobQuery<Question>();
        mainQuery.and(queries);
        mainQuery.count(QuestionItemActivityElinc.this, Question.class, new CountListener() {
            @Override
            public void onSuccess(int i) {
                Question question = new Question();
                question.setObjectId(bundle.getString("questionId"));
                User user = BmobUser.getCurrentUser(QuestionItemActivityElinc.this, User.class);
                User u = new User();
                u.setObjectId(user.getObjectId());

                if (i == 1) {
                    BmobRelation relation = new BmobRelation();
                    relation.remove(question);
                    u.setFollow(relation);
                    u.update(QuestionItemActivityElinc.this, new UpdateListener() {
                        @Override
                        public void onSuccess() {
                            // TODO Auto-generated method stub
                            refreshButton();
                            Log.i("life", "多对多关联添加成功");
                            Tool.alert(QuestionItemActivityElinc.this, "取消收藏");
                        }

                        @Override
                        public void onFailure(int arg0, String arg1) {
                            // TODO Auto-generated method stub
                            Log.i("life", "多对多关联添加失败");
                            Tool.alert(QuestionItemActivityElinc.this, "提交失败，请检查网络");
                        }
                    });
                } else {
                    BmobRelation relation = new BmobRelation();
                    relation.add(question);
                    u.setFollow(relation);
                    u.update(QuestionItemActivityElinc.this, new UpdateListener() {
                        @Override
                        public void onSuccess() {
                            // TODO Auto-generated method stub
                            refreshButton();
                            Log.i("life", "多对多关联添加成功");
                            Tool.alert(QuestionItemActivityElinc.this, "收藏成功");
                        }

                        @Override
                        public void onFailure(int arg0, String arg1) {
                            // TODO Auto-generated method stub
                            Log.i("life", "多对多关联添加失败");
                            Tool.alert(QuestionItemActivityElinc.this, "提交失败，请检查网络");
                        }
                    });
                }
                refreshButton();
            }

            @Override
            public void onFailure(int i, String s) {
                ShowToast("亲，没有网了呢！");
            }
        });











        //==============

    }
    public void submitAnswer(String a){
        final User user = BmobUser.getCurrentUser(this, User.class);
        Question question=new Question();
        question.setObjectId( bundle.getString("questionId"));
        question.increment("numberOfAnswer");
        question.update(QuestionItemActivityElinc.this, new UpdateListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i, String s) {

            }
        });
        new_answer.setAnswerContent(a);
        new_answer.setQuestionId(question);
        new_answer.setResponder(user);
        Tool.alert(QuestionItemActivityElinc.this, a);
        new_answer.save(QuestionItemActivityElinc.this, new SaveListener() {
            @Override
            public void onSuccess() {
                Tool.alert(QuestionItemActivityElinc.this, "提交成功");
                BmobLog.i("Json测试", user.getUsername());
                BmobLog.i("Json测试", CreatJsonMsgInString(author_name, user.getUsername(), bundle.getString("questionId")));
                manager.sendJsonMessage(CreatJsonMsgInString(author_name, user.getUsername(), bundle.getString("questionId")), author.getObjectId());

                finish();
            }

            @Override
            public void onFailure(int i, String s) {
                Tool.alert(QuestionItemActivityElinc.this, "提交失败，请检查网络");
            }
        });
    }

    public String CreatJsonMsgInString(String author_name, String answerer_name,String questionId) {
        JSONObject res = new JSONObject();
        try{
            res.put("author_name",author_name);
            res.put("answerer_name",answerer_name);
            res.put("questionId",questionId);
        }catch (JSONException e) {

        }
        return res.toString();
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
    public void onRefresh() {
        refreshList();

    }

    public void refreshList(){
        BmobQuery<Answer> bmobQuery = new BmobQuery<Answer>();
        bmobQuery.addWhereEqualTo("questionId", bundle.getString("questionId"));
        bmobQuery.include("questionId,responder");
        bmobQuery.order("-createdAt");
        bmobQuery.findObjects(QuestionItemActivityElinc.this, new FindListener<Answer>() {
            @Override
            public void onSuccess(List<Answer> arg0) {
                // TODO Auto-generated method stub
                if (CollectionUtils.isNotNull(arg0)) {
                    answer.clear();
                    adapter.addAll(arg0);
                    mListView.stopRefresh();
                }
                refreshLoad();
            }

            @Override
            public void onError(int arg0, String arg1) {
                // TODO Auto-generated method stub
                ShowLog("搜索更多用户出错:" + arg1);
                mListView.setPullLoadEnable(false);
                mListView.stopRefresh();
                refreshLoad();

            }

        });
    }
    @Override
    public void onClick(View v) {

    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }



    //===================================
    //以下是上传图片
    //需要上传的时候，只需要调用 showAvatarPop 即可

    RelativeLayout layout_choose;
    RelativeLayout layout_photo;
    PopupWindow avatorPop;

    public String filePath = "";

    private void showAvatarPop() {
        View view = LayoutInflater.from(this).inflate(R.layout.pop_showavator,
                null);
        layout_choose = (RelativeLayout) view.findViewById(R.id.layout_choose);
        layout_photo = (RelativeLayout) view.findViewById(R.id.layout_photo);
        layout_photo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                ShowLog("点击拍照");
                // TODO Auto-generated method stub
                layout_choose.setBackgroundColor(getResources().getColor(
                        R.color.base_color_text_white));
                layout_photo.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.pop_bg_press));
                File dir = new File(BmobConstants.MyAvatarDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                // 原图
                File file = new File(dir, new SimpleDateFormat("yyMMddHHmmss")
                        .format(new Date()));
                filePath = file.getAbsolutePath();// 获取相片的保存路径
                Uri imageUri = Uri.fromFile(file);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent,
                        BmobConstants.REQUESTCODE_UPLOADAVATAR_CAMERA);
            }
        });
        layout_choose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                ShowLog("点击相册");
                layout_photo.setBackgroundColor(getResources().getColor(
                        R.color.base_color_text_white));
                layout_choose.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.pop_bg_press));
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent,
                        BmobConstants.REQUESTCODE_UPLOADAVATAR_LOCATION);
            }
        });

        avatorPop = new PopupWindow(view, mScreenWidth, 600);
        avatorPop.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    avatorPop.dismiss();
                    return true;
                }
                return false;
            }
        });

        avatorPop.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        avatorPop.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        avatorPop.setTouchable(true);
        avatorPop.setFocusable(true);
        avatorPop.setOutsideTouchable(true);
        avatorPop.setBackgroundDrawable(new BitmapDrawable());
        // 动画效果 从底部弹起
        avatorPop.setAnimationStyle(R.style.Animations_GrowFromBottom);
        avatorPop.showAtLocation(layout_all_of_question_detail, Gravity.BOTTOM, 0, 0);
    }
    Bitmap newBitmap;
    boolean isFromCamera = false;// 区分拍照旋转
    int degree = 0;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BmobConstants.REQUESTCODE_UPLOADAVATAR_CAMERA:// 拍照修改头像
                if (resultCode == RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        ShowToast("SD不可用");
                        return;
                    }
                    isFromCamera = true;
                    File file = new File(filePath);
                    degree = PhotoUtil.readPictureDegree(file.getAbsolutePath());
                    Log.i("life", "拍照后的角度：" + degree);
                    startImageAction(Uri.fromFile(file), 500, 500,
                            BmobConstants.REQUESTCODE_UPLOADAVATAR_CROP, true);
                }
                break;
            case BmobConstants.REQUESTCODE_UPLOADAVATAR_LOCATION:// 本地修改头像
                if (avatorPop != null) {
                    avatorPop.dismiss();
                }
                Uri uri = null;
                if (data == null) {
                    return;
                }
                if (resultCode == RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        ShowToast("SD不可用");
                        return;
                    }
                    isFromCamera = false;
                    uri = data.getData();
                    startImageAction(uri, 500, 500,
                            BmobConstants.REQUESTCODE_UPLOADAVATAR_CROP, true);
                } else {
                    ShowToast("照片获取失败");
                }

                break;
            case BmobConstants.REQUESTCODE_UPLOADAVATAR_CROP:// 裁剪头像返回
                // TODO sent to crop
                if (avatorPop != null) {
                    avatorPop.dismiss();
                }
                if (data == null) {
                    // Toast.makeText(this, "取消选择", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    saveCropAvator(data);
                }
                // 初始化文件路径
                filePath = "";
                // 上传头像
                uploadAvatar();
                break;
            default:
                break;

        }
    }
    String path;
    private void uploadAvatar() {
        BmobLog.i("头像地址：" + path);
        final BmobFile bmobFile = new BmobFile(new File(path));
        bmobFile.upload(this, new UploadFileListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                String url = bmobFile.getFileUrl(QuestionItemActivityElinc.this);
                //ShowToast(url);
                if (url!=null){
                    new_answer.setAnswerAvatar(url);
                }
            }

            @Override
            public void onProgress(Integer arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFailure(int arg0, String msg) {
                // TODO Auto-generated method stub
                ShowToast("头像上传失败：" + msg);
            }
        });
    }

    private void updateAnswerAvatar(String url) {
        new_answer.setAnswerAvatar(url);
    }


    private void saveCropAvator(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap bitmap = extras.getParcelable("data");
            Log.i("life", "avatar - bitmap = " + bitmap);
            if (bitmap != null) {
                bitmap = PhotoUtil.toRoundCorner(bitmap, 10);
                if (isFromCamera && degree != 0) {
                    bitmap = PhotoUtil.rotaingImageView(degree, bitmap);
                }
                answer_iv.setImageBitmap(bitmap);
                answer_iv.setVisibility(View.VISIBLE);
                // 保存图片
                String filename = new SimpleDateFormat("yyMMddHHmmss")
                        .format(new Date())+".png";
                path = BmobConstants.MyAvatarDir + filename;
                PhotoUtil.saveBitmap(BmobConstants.MyAvatarDir, filename,
                        bitmap, true);
                // 上传头像
                if (bitmap != null && bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        }
    }

    private void refreshAvatar(String avatar) {
        if (avatar != null && !avatar.equals("")) {
            ImageLoader.getInstance().displayImage(avatar, answer_iv,
                    ImageLoadOptions.getOptions());
        } else {
            answer_iv.setImageResource(R.drawable.default_head);
        }
    }


    private void startImageAction(Uri uri, int outputX, int outputY,
                                  int requestCode, boolean isCrop) {
        Intent intent = null;
        if (isCrop) {
            intent = new Intent("com.android.camera.action.CROP");
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        }
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, requestCode);
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
    public void openUserDetail(){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        User u= BmobUser.getCurrentUser(QuestionItemActivityElinc.this, User.class);
        if(author_name!="" && author_name!=null){
            bundle.putString("username", author_name);
            if(author_name.equals(u.getUsername().toString())){
                bundle.putString("from", "me");
            }else{
                bundle.putString("from", "add");
            }
        }
        intent.putExtras(bundle);
        intent.setClass(QuestionItemActivityElinc.this, SetMyInfoActivity.class);
        QuestionItemActivityElinc.this.startActivity(intent);
    }
    public void refreshButton(){
        BmobQuery<Question> query1 = new BmobQuery<Question>();
        BmobQuery<Question> query2 = new BmobQuery<Question>();
        Question q=new Question();
        q.setObjectId(question.getObjectId());
        User u=BmobUser.getCurrentUser(QuestionItemActivityElinc.this, User.class);
        query1.addWhereEqualTo("objectId", q.getObjectId());
        query2.addWhereRelatedTo("follow", new BmobPointer(u));
        List<BmobQuery<Question>> queries = new ArrayList<BmobQuery<Question>>();
        queries.add(query1);
        queries.add(query2);
        BmobQuery<Question> mainQuery = new BmobQuery<Question>();
        mainQuery.and(queries);
        mainQuery.count(QuestionItemActivityElinc.this, Question.class, new CountListener() {
            @Override
            public void onSuccess(int i) {
                if (i == 1) {
                    follow.setBackgroundResource(R.drawable.remove_favorite_e);
                } else {
                    follow.setBackgroundResource(R.drawable.add_favorite_e);
                }
            }

            @Override
            public void onFailure(int i, String s) {

            }
        });
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
