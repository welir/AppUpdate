package com.kcode.lib;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.kcode.lib.bean.VersionModel;
import com.kcode.lib.common.Constant;
import com.kcode.lib.dialog.UpdateActivity;
import com.kcode.lib.log.L;
import com.kcode.lib.net.CheckUpdateTask;
import com.kcode.lib.utils.PublicFunctionUtils;

/**
 * Created by caik on 2017/3/8.
 */

public class UpdateWrapper {

    private static final String TAG = "UpdateWrapper";

    private Context mContext;
    private String mUrl;
    private String mToastMsg;
    private CheckUpdateTask.Callback mCallback;
    private int mNotificationIcon;
    private long mTime;
    private Class<? extends FragmentActivity> mCls;

    private UpdateWrapper() {
    }

    public void start() {

        if (TextUtils.isEmpty(mUrl)) {
            throw new RuntimeException("url not be null");
        }

        if (checkUpdateTime(mTime)) {
            L.d(TAG,"距离上次更新时间太近");
            return;
        }
        new CheckUpdateTask(mUrl, innerCallBack).start();
    }

    private CheckUpdateTask.Callback innerCallBack = new CheckUpdateTask.Callback() {
        @Override
        public void callBack(VersionModel model) {

            if (model == null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext,
                                TextUtils.isEmpty(mToastMsg) ? "当前已是最新版本" : mToastMsg,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            //记录本次更新时间
            PublicFunctionUtils.setLastCheckTime(mContext, System.currentTimeMillis());
            if (mCallback != null) {
                mCallback.callBack(model);
            }

            start2Activity(mContext, model);
        }
    };

    private boolean checkUpdateTime(long time) {
        long lastCheckUpdateTime = PublicFunctionUtils.getLastCheckTime(mContext);
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheckUpdateTime > time ){
            return false;
        }
        return true;
    }

    private void start2Activity(Context context, VersionModel model) {
        try {
            Intent intent = new Intent(context, mCls == null ? UpdateActivity.class : mCls);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constant.MODEL, model);
            intent.putExtra(Constant.NOTIFICATION_ICON, mNotificationIcon);
            context.startActivity(intent);
        } catch (Exception e) {

        }

    }

    public static class Builder {
        private UpdateWrapper wrapper = new UpdateWrapper();

        public Builder(Context context) {
            wrapper.mContext = context;
        }

        public Builder setUrl(String url) {
            wrapper.mUrl = url;
            return this;
        }

        public Builder setTime(long time) {
            wrapper.mTime = time;
            return this;
        }

        public Builder setNotificationIcon(int notificationIcon) {
            wrapper.mNotificationIcon = notificationIcon;
            return this;
        }

        public Builder setCustomsActivity(Class<? extends FragmentActivity> cls) {
            wrapper.mCls = cls;
            return this;
        }

        public Builder setCallback(CheckUpdateTask.Callback callback) {
            wrapper.mCallback = callback;
            return this;
        }

        public Builder setToastMsg(String toastMsg) {
            wrapper.mToastMsg = toastMsg;
            return this;
        }

        public UpdateWrapper build() {
            return wrapper;
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());
}
