package wl.smartled.test.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import wl.smartled.test.constants.Actions;
import wl.smartled.test.constants.Extras;
import wl.smartled.test.constants.Messages;

public class PermissionsBroadcastUtils {
    public static void sendRequestPermissionResultBroadcast(Context context, boolean success, String permissions[]) {
        Intent i = new Intent(Actions.ACTION_REQUEST_PERMISSION_RESULT);
        i.putExtra(Extras.PERMISSION_NAME, permissions);
        i.putExtra(Extras.PERMISSION_RESULT, success ? 1 : 0);
        context.sendBroadcast(i);
    }

    public static void sendRequestPermissionBroadcast(Context context, String permissions[]) {
        Intent i = new Intent(Actions.ACTION_REQUEST_PERMISSION);
        i.putExtra(Extras.PERMISSION_NAME, permissions);
        context.sendBroadcast(i);
    }

    public static void sendPermissionsMessageResult(Intent intent, Handler h) {
        Message m = Message.obtain();

        m.what = Messages.PERMISSION_RESULT_MESSAGE;
        m.arg1 = intent.getIntExtra(Extras.PERMISSION_RESULT, 0);

        if (m.arg1 == 1) {
            h.sendMessage(m);
        }
    }

    public static void sendPermissionsMessage(Intent intent, Handler h) {
        Message message = Message.obtain();

        Bundle b = new Bundle();
        b.putStringArray(Extras.PERMISSION_NAME, intent.getStringArrayExtra(Extras.PERMISSION_NAME));
        message.what = Messages.PERMISSION_REQUEST_MESSAGE;
        message.setData(b);
        h.sendMessage(message);
    }
}
