package wl.smartled.test.constants;

import android.Manifest;

public interface Permissions {
    String[] CALENDAR = {
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR};
    String[] CAMERA = {
            Manifest.permission.CAMERA};
    String[] CONTACTS = {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.GET_ACCOUNTS};
    String[] LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    String[] PHONE = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.ADD_VOICEMAIL,
            Manifest.permission.USE_SIP,
            Manifest.permission.PROCESS_OUTGOING_CALLS};
    String[] SENSORS = {
            Manifest.permission.BODY_SENSORS};
    String[] SMS = {
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_WAP_PUSH,
            Manifest.permission.RECEIVE_MMS};
    String[] MUSIC_MODE = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS};

    String[] BLUETOOTH_MODE = {
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    String[] RECORDING_MODE = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS};

    String CALENDAR_DESC = "日历";
    String CAMERA_DESC = "相机";
    String CONTACTS_DESC = "通讯录";
    String LOCATION_DESC = "位置信息";
    String MICROPHONE_DESC = "麦克风";
    String PHONE_DESC = "电话";
    String SENSORS_DESC = "身体传感器";
    String SMS_DESC = "短信";
    String STORAGE_DESC = "存储空间";
    String BLUETOOTH_DESC = "蓝牙";
}
