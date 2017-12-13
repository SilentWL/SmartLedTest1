package wl.smartled.test.bean;

/**
 * Created by Administrator on 2017/5/13 0013.
 */

public class DeviceBean {
    public static int DEVICE_STATE_IDLE = 0;
    public static int DEVICE_STATE_CONNECTING = 1;
    public static int DEVICE_STATE_WAIT_CONFIRM_CONNECTED = 2;
    public static int DEVICE_STATE_CONNECTED = 3;
    public static int DEVICE_STATE_READ_PENDING = 4;
    public static int DEVICE_STATE_DISCONNECTING = 5;

    private String address;
    private String deviceName;
    private int state;
    private int time;

    public DeviceBean(String address, String deviceName, int state) {
        this.address = address;
        this.deviceName = deviceName;
        this.state = state;
        this.time = 0;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
