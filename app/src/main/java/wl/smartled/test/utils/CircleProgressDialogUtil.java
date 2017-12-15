package wl.smartled.test.utils;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import wl.smartled.test.R;

/**
 * Created by Administrator on 2017/12/10 0010.
 */

public class CircleProgressDialogUtil {
    private static Dialog progressDialog;

    public static void show(Context context, String text) {
        if (progressDialog == null) {
            progressDialog = new Dialog(context, R.style.progress_dialog);
            progressDialog.setContentView(R.layout.circle_dialog);
            progressDialog.setCancelable(true);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText(text);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    public static void hideDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
