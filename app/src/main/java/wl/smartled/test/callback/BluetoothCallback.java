package wl.smartled.test.callback;

/**
 * Created by Administrator on 2017/11/7 0007.
 */

public interface BluetoothCallback {
    void onBluetoothEnabled(boolean enabled);

    void onScanResult(String name, String address);

    void onConnectionStateChange(String name, String address, int state);

    void onDataRead(String address);
}
