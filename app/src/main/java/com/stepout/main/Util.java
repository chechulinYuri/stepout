package com.stepout.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

/**
 * Created by Admin on 13.08.2014.
 */
public class Util {
    private static boolean isProgressDialogShowing;
    private static ProgressDialog pd;

    public static void showLoadingDialog(Context context) {
        if (!isProgressDialogShowing) {
            isProgressDialogShowing = true;
            Log.d("asd", "pd show");
            pd = new ProgressDialog(context);
            pd.setTitle(context.getResources().getString(R.string.loading_process));
            pd.setCancelable(false);
            pd.show();
        }
    }

    public static void dismissLoadingDialog() {
        if (isProgressDialogShowing) {
            isProgressDialogShowing = false;
            Log.d("asd", "pd dismiss");
            pd.dismiss();
            pd = null;
        }
    }
}
