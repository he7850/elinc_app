package com.elinc.im.elinc.bean;

/**
 * Created by zheruicao on 15/9/8.
 */
import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity收集器
 */
public class ActivityCollection {
    private static List<Activity> activities= new ArrayList<Activity>();

    public static void addActivity(Activity activity){
        if(!activities.contains(activity)){
            activities.add(activity);
        }
    }
    public static Activity getActivity(int position){

        return activities.get(position);

    }
    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }
    //退出程序方法
    public static void finishAll(){
        for(Activity activity: activities){
            if(!activity.isFinishing()){
                activity.finish();
            }
        }
    }
}