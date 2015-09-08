package com.elinc.im.elinc.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.bmob.BTPFileResponse;
import com.bmob.BmobProFile;
import com.bmob.btp.callback.LocalThumbnailListener;
import com.bmob.btp.callback.UploadListener;
import com.elinc.im.elinc.R;
import com.elinc.im.elinc.bean.Question;
import com.elinc.im.elinc.bean.Tool;
import com.elinc.im.elinc.bean.User;
import com.elinc.im.elinc.config.BmobConstants;
import com.elinc.im.elinc.util.ImageLoadOptions;
import com.elinc.im.elinc.util.PhotoUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class NewQuestionActivityElinc extends ActivityBase {
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
    EditText questionContent;
    EditText questionTitle;
    List<String> tags;
    EditText et_input_tags;
    BmobUserManager userManager = BmobUserManager.getInstance(NewQuestionActivityElinc.this);
    LinearLayout layout_all_of_new_question;
    ImageView iv_set_question_avatar; //显示上传的图片
    Question new_question;  //这个是需要save 的新question
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_question_elinc);
        init();
        initTagButton();
        initListener();
    }
    private void init(){
        initTopBarForLeft("新问题");
        userManager.init(this);
        questionContent = (EditText)findViewById(R.id.question_content);
        questionTitle= (EditText) findViewById(R.id.question_title);
        et_input_tags= (EditText) findViewById(R.id.et_input_tags);
        iv_set_question_avatar= (ImageView) findViewById(R.id.iv_set_question_avatar);
        layout_all_of_new_question = (LinearLayout) findViewById(R.id.layout_all_of_new_question);
        new_question=new Question();
    }
    private void initTagButton(){
        Button btn_add_food= (Button) findViewById(R.id.btn_add_food);
        btn_add_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String a = et_input_tags.getText().toString();
                if (!a.equals("") && a!=null) {
                    a = a + ",美食";
                } else {
                    a = "美食";
                }
                et_input_tags.setText(a);
            }
        });
        Button btn_add_fun= (Button) findViewById(R.id.btn_add_fun);
        btn_add_fun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String a=et_input_tags.getText().toString();
                if(!a.equals("") && a!=null){a=a+",娱乐";}
                else{a="娱乐";}
                et_input_tags.setText(a);
            }
        });
        Button btn_add_study= (Button) findViewById(R.id.btn_add_study);
        btn_add_study.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String a=et_input_tags.getText().toString();
                //Tool.alert(NewQuestionActivityElinc.this,a);
                System.out.println(a);
                if(!a.equals("") && a!=null){a=a+",学习";}
                else{a="学习";}
                et_input_tags.setText(a);
            }
        });       
        Button btn_add_life= (Button) findViewById(R.id.btn_add_life);
        btn_add_life.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String a=et_input_tags.getText().toString();
                if(!a.equals("") && a!=null){a=a+",旅行";}
                else{a="旅行";}
                et_input_tags.setText(a);
            }
        });
    }
    private void initListener(){
        Button button = (Button)findViewById(R.id.button_new_question);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String questionContentText = questionContent.getText().toString();
                String questionTitleText = questionTitle.getText().toString();

                tags= Arrays.asList((et_input_tags.getText().toString().split(",|，| |　",3)));
                if(questionContentText.length()<5){
                    ShowToast("内容字数不足");
                }
                if(questionTitleText.length()<5){
                    ShowToast("标题字数不足");
                }
                if(questionTitleText.length()>=5 && questionContentText.length()>=5)
                AddQuestion(questionTitleText, questionContentText);
            }
        });
        Button question_add_photo;
        question_add_photo = (Button) findViewById(R.id.question_add_photo);
        question_add_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAvatarPop();
            }
        });
    }
    public void AddQuestion(String title,String questionContent){
        new_question.setTags(tags);
        new_question.setQuestionContent(questionContent);
        new_question.setTitle(title);
        new_question.setNumberOfAnswer(0);
        new_question.setAuthor(BmobUser.getCurrentUser(this, User.class));
        new_question.save(NewQuestionActivityElinc.this, new SaveListener() {
            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                String id = new_question.getObjectId();
                Tool.alert(NewQuestionActivityElinc.this, "提问成功，请静候答案");
                User user = BmobUser.getCurrentUser(NewQuestionActivityElinc.this, User.class);
                BmobRelation relation = new BmobRelation();
                Question q = new Question();
                q.setObjectId(id);
                relation.add(q);
                user.setFollow(relation);
                user.update(NewQuestionActivityElinc.this, new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        // TODO Auto-generated method stub
                        //Log.i("life", "多对多关联添加成功");
                        //Tool.alert(NewQuestionActivityElinc.this, "收藏成功");
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        // TODO Auto-generated method stub
                        // Log.i("life", "多对多关联添加失败");
                        //Tool.alert(NewQuestionActivityElinc.this, "提交失败，请检查网络");
                    }
                });
                finish();
            }

            @Override
            public void onFailure(int code, String arg0) {
                // TODO Auto-generated method stub
                Tool.alert(NewQuestionActivityElinc.this, "提问失败，请查看网络状态");
            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_new_question, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

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
                selectImageFromCamera();
/*                File dir = new File(BmobConstants.MyAvatarDir);
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
                        BmobConstants.REQUESTCODE_UPLOADAVATAR_CAMERA);*/
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
                selectImageFromLocal();
               /* Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image*//*");
                startActivityForResult(intent,
                        BmobConstants.REQUESTCODE_UPLOADAVATAR_LOCATION);*/
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
        avatorPop.showAtLocation(layout_all_of_new_question, Gravity.BOTTOM, 0, 0);
    }
    Bitmap newBitmap;
    boolean isFromCamera = false;// 区分拍照旋转
    int degree = 0;


    String path;
    private void uploadAvatar() {
        BmobLog.i("头像地址：" + path);
        final BmobFile bmobFile = new BmobFile(new File(path));
        bmobFile.upload(this, new UploadFileListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                String url = bmobFile.getFileUrl(NewQuestionActivityElinc.this);
                // ShowToast(url);
                if (url != null) {
                    new_question.setQuestionAvatar(url);
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

    private void updateQuestionAvatar(String url) {
        new_question.setQuestionAvatar(url);
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
                iv_set_question_avatar.setImageBitmap(bitmap);
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


    private String localCameraPath = "";// 拍照后得到的图片地址

    /**
     * 启动相机拍照 startCamera
     *
     * @Title: startCamera
     * @throws
     */
    public void selectImageFromCamera() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File dir = new File(BmobConstants.BMOB_PICTURE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, String.valueOf(System.currentTimeMillis())
                + ".jpg");
        localCameraPath = file.getPath();
        Uri imageUri = Uri.fromFile(file);
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(openCameraIntent,
                BmobConstants.REQUESTCODE_TAKE_CAMERA);
    }

    /**
     * 选择图片
     * @Title: selectImage
     * @Description: TODO
     * @param
     * @return void
     * @throws
     */
    public void selectImageFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, BmobConstants.REQUESTCODE_TAKE_LOCAL);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        avatorPop.dismiss();
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case BmobConstants.REQUESTCODE_TAKE_CAMERA:// 当取到值的时候才上传path路径下的图片到服务器
                    ShowLog("本地图片的地址：" + localCameraPath);
                    upload(localCameraPath);
                    break;
                case BmobConstants.REQUESTCODE_TAKE_LOCAL:
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(
                                    selectedImage, null, null, null, null);
                            cursor.moveToFirst();
                            int columnIndex = cursor.getColumnIndex("_data");
                            String localSelectPath = cursor.getString(columnIndex);
                            cursor.close();
                            if (localSelectPath == null
                                    || localSelectPath.equals("null")) {
                                ShowToast("找不到您想要的图片");
                                return;
                            }
                            upload(localSelectPath);
                        }
                    }
                    break;

            }
        }
    }

    public void upload(String path) {
        BmobProFile.getInstance(NewQuestionActivityElinc.this).getLocalThumbnail(path,1, new LocalThumbnailListener() {

            @Override
            public void onError(int statuscode, String errormsg) {
                // TODO Auto-generated method stub
                Log.i("bmob","本地缩略图创建失败 :"+statuscode+","+errormsg);
            }

            @Override
            public void onSuccess(String thumbnailPath) {
                // TODO Auto-generated method stub
                Log.i("bmob","本地缩略图创建成功  :"+thumbnailPath);
                BTPFileResponse response = BmobProFile.getInstance(NewQuestionActivityElinc.this).upload(thumbnailPath, new UploadListener() {
                    @Override
                    public void onSuccess(String fileName, String url, BmobFile file) {
                        Log.i("bmob", "文件上传成功：" + fileName + ",可访问的文件地址：" + file.getUrl());
                        new_question.setQuestionAvatar(file.getUrl());
                        iv_set_question_avatar.setVisibility(View.VISIBLE);
                        ImageLoader.getInstance().displayImage(file.getUrl(), iv_set_question_avatar, ImageLoadOptions.getOptions(), animateFirstListener);
                    }

                    @Override
                    public void onProgress(int progress) {
                        // TODO Auto-generated method stub
                        ShowToast("图片上传中，请稍后");
                        Log.i("bmob", "onProgress :" + progress);
                    }

                    @Override
                    public void onError(int statuscode, String errormsg) {
                        // TODO Auto-generated method stub
                        Log.i("bmob", "文件上传失败：" + errormsg);
                        ShowToast("图片上传失败，请检查网络");
                    }
                });
            }
        });

    }
}




/*
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
    }*/
