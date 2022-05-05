package com.miguel_lm.socialmedia.utils;

import android.app.ActivityManager;
import android.content.Context;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.UserProvider;
import com.miguel_lm.socialmedia.ui.activities.ChatActivity;

import java.util.List;

public class ViewedMessageHelper {

    public static void updateOnline(boolean status, final Context context){

        UserProvider userProvider = new UserProvider();
        AuthProvider authProvider = new AuthProvider();

        if(authProvider.getUid() != null){
            if(isBackgroundRunning(context)){
                userProvider.updateOnline(authProvider.getUid(), status);

            } else {
                userProvider.updateOnline(authProvider.getUid(), status);
            }
        }
    }

    public static boolean isBackgroundRunning(Context context) {

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        //If your app is the process in foreground, then it's not in running in background
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
