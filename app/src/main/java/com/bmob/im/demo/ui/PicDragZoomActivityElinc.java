/**
 * 缩放和拖动图片
 * by VkaZas 2015.8.8 20:27
 */
package com.bmob.im.demo.ui;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.bmob.im.demo.R;
import com.bmob.im.demo.util.ImageLoadOptions;
import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PicDragZoomActivityElinc extends ActivityBase {


    private ImageView iv_detail_pic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_drag_zoom_activity_elinc);

        initTopBarForLeft("详细信息");
        String pic_address = getIntent().getStringExtra("pic_address");
        iv_detail_pic = (ImageView) findViewById(R.id.iv_detail_pic);
        if (pic_address!=null && !pic_address.equals("")) {
            Glide.with(this).load(pic_address).fitCenter().into(iv_detail_pic);
        }else {
            iv_detail_pic.setImageResource(R.drawable.test_image);
        }
        iv_detail_pic.setOnTouchListener(new TouchListener());

    }

    private final class TouchListener implements View.OnTouchListener {
        private int mode = 0; //记录拖拉还是放缩模式，0为初始状态
        private static final int MODE_DRAG = 1; //拖拉模式
        private static final int MODE_ZOOM = 2; //放缩模式
        private PointF startPoint = new PointF(); //开始坐标位置
        private Matrix matrix = new Matrix(); //拖拉图片移动的坐标
        private Matrix currentMatrix = new Matrix(); //将要拖拉时图片的坐标
        private float startDis; //两指的开始距离
        private PointF midPoint; //两指的中间点

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK ) {
                case MotionEvent.ACTION_DOWN:
                    mode = MODE_DRAG;
                    currentMatrix.set(iv_detail_pic.getImageMatrix());
                    startPoint.set(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == MODE_DRAG) {
                        float dx = event.getX() - startPoint.x;
                        float dy = event.getY() - startPoint.y;
                        matrix.set(currentMatrix);
                        matrix.postTranslate(dx,dy);
                    }else if (mode == MODE_ZOOM) {
                        float endDis = distance(event);
                        if (endDis > 10f) {
                            float scale = endDis / startDis;
                            matrix.set(currentMatrix);
                            matrix.postScale(scale,scale,midPoint.x,midPoint.y);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mode = MODE_ZOOM;
                    startDis = distance(event);
                    if (startDis > 10f) {
                        midPoint = mid(event);
                        currentMatrix.set(iv_detail_pic.getImageMatrix());
                    }
                    break;
            }
            iv_detail_pic.setImageMatrix(matrix);
            return true;
        }

        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            return FloatMath.sqrt(dx*dx+dy*dy);
        }

        private PointF mid(MotionEvent event) {
            float midX = (event.getX(1) + event.getX(0)) / 2;
            float midY = (event.getY(1) + event.getY(0)) / 2;
            return new PointF(midX,midY);
        }
    }
}
