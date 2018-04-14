package wl.smartled.test.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import wl.smartled.test.callback.BluetoothCallback;
import wl.smartled.test.constants.Actions;
import wl.smartled.test.constants.Extras;
import wl.smartled.test.service.BluetoothLEService;


public class BluetoothUtil implements BluetoothCallback {
    private static BluetoothUtil bluetoothUtil;
    private BluetoothLEService bluetoothLEService;

    private BluetoothCallback bluetoothCallback;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            bluetoothLEService = ((BluetoothLEService.BluetoothLEServiceBinder) iBinder).getService();
            bluetoothLEService.setBluetoothLEServiceCallback(bluetoothUtil);

            if (bluetoothCallback != null) {
                if (bluetoothLEService.isBluetoothEnabled() && bluetoothLEService.isServiceStarted()) {
                    bluetoothCallback.onBluetoothEnabled(true);
                } else {
                    bluetoothCallback.onBluetoothEnabled(false);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLEService = null;
        }
    };

    private BluetoothUtil() {

    }

    public static BluetoothUtil getInstance() {
        if (bluetoothUtil == null) {
            synchronized (BluetoothUtil.class) {
                if (bluetoothUtil == null) {
                    bluetoothUtil = new BluetoothUtil();
                }
            }
        }
        return bluetoothUtil;
    }

    public void bindService(Context c) {
        Intent i = new Intent();
        i.setClass(c, BluetoothLEService.class);
        c.bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService(Context c) {
        if (bluetoothLEService != null) {
            c.unbindService(serviceConnection);
            bluetoothLEService = null;
        }
    }

    public void forceEnableBluetooth() {
        if (bluetoothLEService != null && bluetoothLEService.isServiceStarted()) {
            bluetoothLEService.forceEnableBluetooth();
        }
    }

    public void forceDisableBluetooth() {
        if (bluetoothLEService != null && bluetoothLEService.isServiceStarted()) {
            bluetoothLEService.forceDisableBluetooth();
        }
    }

    public void readConnectionState(Context c, String address) {
        Intent i = new Intent();
        i.putExtra(Extras.BLUETOOTHLE_ADDRESS, address);
        sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_READ_CONNECTION_STATE, i);
    }

    public void scanLEDevice(Context c, boolean clearScanList) {
        Intent i = new Intent();
        i.putExtra(Extras.BLUETOOTHLE_SCAN_CLEAR, clearScanList);
        sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_SCAN, i);
    }

    public void stopScanLEDevice(Context c) {
        sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_SCAN_STOP, null);
    }

    public void releaseResource(Context c, String address) {
        if (address != null && !address.isEmpty()) {
            Intent i = new Intent();
            i.putExtra(Extras.BLUETOOTHLE_ADDRESS, address);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_RELEASE_RESOURCE, i);
        }
    }

    public void connectDevice(Context c, String address) {
        if (address != null && !address.isEmpty()) {
            Intent i = new Intent();
            i.putExtra(Extras.BLUETOOTHLE_ADDRESS, address);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_CONNECT, i);
        }
    }

    public void disconnectDevice(Context c, String address) {
        if (address != null && !address.isEmpty()) {
            Intent i = new Intent();
            i.putExtra(Extras.BLUETOOTHLE_ADDRESS, address);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_DISCONNECT, i);
        }
    }

    public void sendColor(Context c, String address, int color) {
        if (address != null && !address.isEmpty()) {
            Intent i = new Intent();
            i.putExtra(Extras.BLUETOOTHLE_ADDRESS, address);
            i.putExtra(Extras.BLUETOOTHLE_COLOR, color);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_CHANGE_COLOR, i);
        }
    }

    public void sendColor(Context c, List<String> addressList, int color) {
        if (addressList != null && addressList.size() > 0) {
            Intent i = new Intent();
            i.putStringArrayListExtra(Extras.BLUETOOTHLE_ADDRESS_LIST, (ArrayList<String>) addressList);
            i.putExtra(Extras.BLUETOOTHLE_COLOR, color);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_CHANGE_COLOR, i);
        }
    }

    public void sendColorTemperature(Context c, List<String> addressList, int warm, int cold) {
        if (addressList != null && addressList.size() > 0) {
            Intent i = new Intent();
            i.putStringArrayListExtra(Extras.BLUETOOTHLE_ADDRESS_LIST, (ArrayList<String>) addressList);
            i.putExtra(Extras.BLUETOOTHLE_COLOR_WARM, warm);
            i.putExtra(Extras.BLUETOOTHLE_COLOR_COLD, cold);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_CHANGE_COLOR_TEMPERATURE, i);
        }
    }

    public void sendSingleColor(Context c, List<String> addressList, int color) {
        if (addressList != null && addressList.size() > 0) {
            Intent i = new Intent();
            i.putStringArrayListExtra(Extras.BLUETOOTHLE_ADDRESS_LIST, (ArrayList<String>) addressList);
            i.putExtra(Extras.BLUETOOTHLE_SINGLE_COLOR, color);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_CHANGE_SINGLE_COLOR, i);
        }
    }

    public void sendBrightnessChange(Context c, String address, int brightness) {
        if (address != null && !address.isEmpty()) {
            Intent i = new Intent();
            i.putExtra(Extras.BLUETOOTHLE_ADDRESS, address);
            i.putExtra(Extras.BLUETOOTHLE_BRIGHTNESS, brightness);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_CHANGE_BRIGHTNESS, i);
        }
    }

    public void sendBrightnessChange(Context c, List<String> addressList, int brightness) {
        if (addressList != null && addressList.size() > 0) {
            Intent i = new Intent();
            i.putStringArrayListExtra(Extras.BLUETOOTHLE_ADDRESS_LIST, (ArrayList<String>) addressList);
            i.putExtra(Extras.BLUETOOTHLE_BRIGHTNESS, brightness);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_CHANGE_BRIGHTNESS, i);
        }
    }

    public void sendMusicAmplitude(Context c, String address, int color, int brightness) {
        if (address != null && !address.isEmpty()) {
            Intent i = new Intent();
            i.putExtra(Extras.BLUETOOTHLE_ADDRESS, address);
            i.putExtra(Extras.BLUETOOTHLE_COLOR, color);
            i.putExtra(Extras.BLUETOOTHLE_BRIGHTNESS, brightness);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_MUSIC_AMPLITUDE, i);
        }
    }

    public void sendMusicAmplitude(Context c, List<String> addressList, int color, int brightness) {
        if (addressList != null && addressList.size() > 0) {
            Intent i = new Intent();
            i.putStringArrayListExtra(Extras.BLUETOOTHLE_ADDRESS_LIST, (ArrayList<String>) addressList);
            i.putExtra(Extras.BLUETOOTHLE_COLOR, color);
            i.putExtra(Extras.BLUETOOTHLE_BRIGHTNESS, brightness);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_MUSIC_AMPLITUDE, i);
        }
    }

    public void sendMode(Context c, String address, int mode) {
        if (address != null && !address.isEmpty()) {
            Intent i = new Intent();
            i.putExtra(Extras.BLUETOOTHLE_ADDRESS, address);
            i.putExtra(Extras.BLUETOOTHLE_MODE, mode);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_CHANGE_MODE, i);
        }
    }

    public void sendMode(Context c, List<String> addressList, int mode) {
        if (addressList != null && addressList.size() > 0) {
            Intent i = new Intent();
            i.putStringArrayListExtra(Extras.BLUETOOTHLE_ADDRESS_LIST, (ArrayList<String>) addressList);
            i.putExtra(Extras.BLUETOOTHLE_MODE, mode);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_CHANGE_MODE, i);
        }
    }

    public void sendModeChangeSpeed(Context c, String address, int speed) {
        if (address != null && !address.isEmpty()) {
            Intent i = new Intent();
            i.putExtra(Extras.BLUETOOTHLE_ADDRESS, address);
            i.putExtra(Extras.BLUETOOTHLE_MODE_CHANGE_SPEED, speed);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_CHANGE_MODE_SPEED, i);
        }
    }

    public void sendModeChangeSpeed(Context c, List<String> addressList, int speed) {
        if (addressList != null && addressList.size() > 0) {
            Intent i = new Intent();
            i.putStringArrayListExtra(Extras.BLUETOOTHLE_ADDRESS_LIST, (ArrayList<String>) addressList);
            i.putExtra(Extras.BLUETOOTHLE_MODE_CHANGE_SPEED, speed);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_CHANGE_MODE_SPEED, i);
        }
    }

    public void sendLightOn(Context c, String address, boolean on) {
        if (address != null && !address.isEmpty()) {
            Intent i = new Intent();
            i.putExtra(Extras.BLUETOOTHLE_ADDRESS, address);
            i.putExtra(Extras.BLUETOOTHLE_LIGHT_ON, on);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_LIGHT_ON, i);
        }
    }

    public void sendLightOn(Context c, List<String> addressList, boolean on) {
        if (addressList != null && addressList.size() > 0) {
            Intent i = new Intent();
            i.putStringArrayListExtra(Extras.BLUETOOTHLE_ADDRESS_LIST, (ArrayList<String>) addressList);
            i.putExtra(Extras.BLUETOOTHLE_LIGHT_ON, on);
            sendCommand(c, Actions.BLUETOOTH_LE_SERVICE_LIGHT_ON, i);
        }
    }

    private void sendCommand(Context c, String command, Intent i) {
        if (bluetoothLEService != null && bluetoothLEService.isServiceStarted() && bluetoothLEService.isBluetoothEnabled()) {
            if (i == null) {
                i = new Intent();
            }
            i.setClass(c, BluetoothLEService.class);
            i.setAction(command);
            c.startService(i);
        }
    }

    public void setBluetoothCallback(BluetoothCallback callback) {
        bluetoothCallback = callback;
    }

    @Override
    public void onBluetoothEnabled(boolean enabled) {
        if (bluetoothCallback != null) {
            if (bluetoothLEService != null && bluetoothLEService.isServiceStarted() && bluetoothLEService.isBluetoothEnabled()) {
                bluetoothCallback.onBluetoothEnabled(true);
            } else {
                bluetoothCallback.onBluetoothEnabled(false);
            }
        }
    }

    @Override
    public void onConnectionStateChange(String name, String address, int state) {
        if (bluetoothCallback != null) {
            bluetoothCallback.onConnectionStateChange(name, address, state);
        }
    }

    @Override
    public void onScanResult(String name, String address) {
        if (bluetoothCallback != null) {
            bluetoothCallback.onScanResult(name, address);
        }
    }

    @Override
    public void onDataRead(String address) {
        if (bluetoothCallback != null) {
            bluetoothCallback.onDataRead(address);
        }
    }
}
