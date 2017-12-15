package wl.smartled.test.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import wl.smartled.test.R;
import wl.smartled.test.bean.DeviceBean;
import wl.smartled.test.callback.BluetoothCallback;
import wl.smartled.test.constants.Actions;
import wl.smartled.test.constants.Extras;
import wl.smartled.test.constants.Messages;
import wl.smartled.test.constants.Permissions;
import wl.smartled.test.utils.BKServiceSharePreferencesUtil;
import wl.smartled.test.utils.ListUtil;
import wl.smartled.test.utils.LogUtil;
import wl.smartled.test.utils.PermissionsBroadcastUtil;


public class BluetoothLEService extends Service {
    private static final String TAG = "BluetoothLEService";

    private static final int SEND_DATA_SERVICE_INDEX = 1;
    private static final int SEND_DATA_CHARACTERISTIC_INDEX = 0;

    private static final String NAME_FILTER = "ELK-";
    private boolean isStarted = false;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isBluetoothEnabled = false;
    private BluetoothCallback bluetoothLEServiceCallback;

    private static byte sendDatas1[] = new byte[9];
    private static byte sendDatas2[] = new byte[9];
    private static boolean isSendDatas1;
    private static boolean isSendDatas2;
    private static Set<String> sendDataAddressSet = new HashSet<>();

    private static final int MAX_GATT_NUMBER = 4;
    private final Map<String, BluetoothGatt> bluetoothGattMap = new ConcurrentHashMap<>();
    private final List<String> lruGattAddress = new CopyOnWriteArrayList<>();

    private BluetoothLeScanner scaner;
    private ScanCallback scanCallback;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public class BluetoothLEServiceBinder extends Binder {
        public BluetoothLEService getService() {
            return BluetoothLEService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new BluetoothLEServiceBinder();
    }

    private boolean checkBluetoothLESupport() {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.string_ble_not_support, Toast.LENGTH_SHORT).show();
            stopSelf();
            return false;
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.string_bluetooth_not_support, Toast.LENGTH_SHORT).show();
            stopSelf();
            return false;
        }
        return true;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Actions.ACTION_REQUEST_PERMISSION_RESULT) && Arrays.equals(intent.getStringArrayExtra(Extras.PERMISSION_NAME), Permissions.BLUETOOTH_MODE)) {
                PermissionsBroadcastUtil.sendPermissionsMessageResult(intent, handler);
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                isBluetoothEnabled = bluetoothAdapter.isEnabled();
                if (bluetoothLEServiceCallback != null) {
                    bluetoothLEServiceCallback.onBluetoothEnabled(isBluetoothEnabled);
                }
            }
        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Messages.PERMISSION_RESULT_MESSAGE) {
                if (msg.arg1 == 1) {
                    enableBluetooth();
                } else {
                    isBluetoothEnabled = false;
                    stopSelf();
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (checkBluetoothLESupport()) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Actions.ACTION_REQUEST_PERMISSION_RESULT);
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

            registerReceiver(receiver, intentFilter);

            PermissionsBroadcastUtil.sendRequestPermissionBroadcast(this, Permissions.BLUETOOTH_MODE);

            executorService.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    sendCommand();

                }
            }, 0, 60, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public int onStartCommand(@NonNull final Intent intent, int flags, int startId) {
        isStarted = true;
        flags = START_NOT_STICKY;

        if (intent != null) {
            String action = intent.getAction();
            switch (action) {
                case Actions.BLUETOOTH_LE_SERVICE_SCAN:
                    startDiscovery(intent.getBooleanExtra(Extras.BLUETOOTHLE_SCAN_CLEAR, true));
                    break;
                case Actions.BLUETOOTH_LE_SERVICE_SCAN_STOP:
                    cancelDiscovery();
                    break;
                case Actions.BLUETOOTH_LE_SERVICE_CHANGE_COLOR:
                    changeColor(intent);
                    break;
                case Actions.BLUETOOTH_LE_SERVICE_MUSIC_AMPLITUDE:
                    music_amplitude(intent);
                    break;
                case Actions.BLUETOOTH_LE_SERVICE_CHANGE_BRIGHTNESS:
                    changeBrightness(intent);
                    break;
                case Actions.BLUETOOTH_LE_SERVICE_CHANGE_MODE:
                    changeMode(intent);
                    break;
                case Actions.BLUETOOTH_LE_SERVICE_CHANGE_MODE_SPEED:
                    changeModeSpeed(intent);
                    break;
                case Actions.BLUETOOTH_LE_SERVICE_CONNECT:
                    Connect(intent, true);
                    break;
                case Actions.BLUETOOTH_LE_SERVICE_DISCONNECT:
                    Connect(intent, false);
                    break;
                case Actions.BLUETOOTH_LE_SERVICE_LIGHT_ON:
                    lightOn(intent);
                    break;
                case Actions.BLUETOOTH_LE_SERVICE_CHANGE_COLOR_TEMPERATURE:
                    changeColorTemperature(intent);
                    break;
                case Actions.BLUETOOTH_LE_SERVICE_CHANGE_SINGLE_COLOR:
                    changeSingleColor(intent);
                    break;
                case Actions.BLUETOOTH_LE_SERVICE_SEND:
                    break;
                case Actions.BLUETOOTH_LE_SERVICE_RECEIVE:
                    break;
                case Actions.BLUETOOTH_LE_SERVICE_RELEASE_RESOURCE:
                    releaseResource(intent);
                    break;
                case Actions.BLUETOOTH_LE_SERVICE_READ_CONNECTION_STATE:
                    readConnectionState(intent);
                    break;
                default:
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean requestGattResourceFromLruAddressList(String address) {
        boolean requestSuccess = true;
        String removeAddress = null;
        int lruAddressIndex = ListUtil.containsString(lruGattAddress, address);

        if (lruAddressIndex != -1) {
            lruGattAddress.remove(lruAddressIndex);
        } else {
            if (bluetoothGattMap.size() >= MAX_GATT_NUMBER && bluetoothGattMap.get(address) == null) {
                if (lruGattAddress.size() > 0) {
                    removeAddress = lruGattAddress.remove(lruGattAddress.size() - 1);
                } else {
                    requestSuccess = false;
                }
            }
        }

        if (removeAddress != null) {
            BluetoothGatt removeGatt = bluetoothGattMap.remove(removeAddress);
            if (removeGatt != null) {
                removeGatt.close();
            }
        }
        return requestSuccess;
    }

    private void releaseResourceFromLruAddressList(String address) {
        String removeAddress = null;
        int lruAddressIndex = ListUtil.containsString(lruGattAddress, address);

        if (lruAddressIndex != -1) {
            lruGattAddress.remove(lruAddressIndex);
        } else {
            if (bluetoothGattMap.size() >= MAX_GATT_NUMBER) {
                if (bluetoothGattMap.get(address) == null && lruGattAddress.size() > 0) {
                    removeAddress = lruGattAddress.remove(lruGattAddress.size() - 1);
                }
            }
        }

        if (removeAddress != null) {
            BluetoothGatt removeGatt = bluetoothGattMap.remove(removeAddress);
            if (removeGatt != null) {
                removeGatt.close();
            }
        }

        lruGattAddress.add(0, address);
    }


    private void Connect(@NonNull Intent intent, boolean connect) {
        String address = intent.getStringExtra(Extras.BLUETOOTHLE_ADDRESS);
        BluetoothGatt gatt = bluetoothGattMap.get(address);

        if (connect) {
            if (!requestGattResourceFromLruAddressList(address)) {
                return;
            }

            if (gatt == null) {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);  // 通过mac地址获取蓝牙对象，或者可以直接用扫描到的对象，效果都是一样
                gatt = device.connectGatt(this, false, gattCallback);
                bluetoothGattMap.put(address, gatt);
            } else {
                gatt.connect();
            }
        } else {
            if (gatt != null) {
                gatt.disconnect();
            }
        }
    }

    private void releaseResource(@NonNull Intent intent) {
        String address = intent.getStringExtra(Extras.BLUETOOTHLE_ADDRESS);
        releaseResourceFromLruAddressList(address);
    }

    private void sendCommand() {
        synchronized (BluetoothLEService.class) {
            for (String address : sendDataAddressSet) {
                BluetoothGatt gatt = bluetoothGattMap.get(address);

                if (gatt != null) {
                    List<BluetoothGattService> services = gatt.getServices();
                    if (services != null && services.size() >= SEND_DATA_SERVICE_INDEX + 1) {
                        List<BluetoothGattCharacteristic> characteristics = services.get(SEND_DATA_SERVICE_INDEX).getCharacteristics();
                        if (characteristics != null && characteristics.size() >= SEND_DATA_CHARACTERISTIC_INDEX + 1) {
                            BluetoothGattCharacteristic txd_charact = characteristics.get(SEND_DATA_CHARACTERISTIC_INDEX);

                            if (isSendDatas1) {
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                txd_charact.setValue(sendDatas1);
                                gatt.writeCharacteristic(txd_charact);
                            }

                            if (isSendDatas2) {
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                txd_charact.setValue(sendDatas2);
                                gatt.writeCharacteristic(txd_charact);
                            }
                        }
                    }
                }
            }

            isSendDatas1 = false;
            isSendDatas2 = false;
            sendDataAddressSet.clear();
        }
    }

    public void startDiscovery(boolean clearScanList) {
        if (bluetoothAdapter == null) {
            return;
        }

        //判断版本号,如果api版本号大于5.0则使用最新的方法搜素
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        if (true) {
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            if (scaner == null) {
                scaner = bluetoothAdapter.getBluetoothLeScanner();
            }

            if (scanCallback == null) {
                scanCallback = new BleScanCallback();
            }
            scaner.startScan(scanCallback);
        }
    }

    public void cancelDiscovery() {
        if (bluetoothAdapter == null) {
            return;
        }
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        if (true) {
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        } else {
            if (scaner != null) {
                if (scanCallback != null) {
                    scaner.stopScan(scanCallback);
                    scanCallback = null;
                }
                scaner = null;
            }
        }
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            //注意在此方法中不要过多的操作
            processScanResult(device.getName(), device.getAddress());
        }
    };

    private void processScanResult(String name, String address) {
        if (name != null) {
            if (name.startsWith(NAME_FILTER)) {
                bluetoothLEServiceCallback.onScanResult(name, address);
            }
        }
        LogUtil.d(TAG, "processScanResult, --->name = " + name + ", address = " + address);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class BleScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            processScanResult(result.getDevice().getName(), result.getDevice().getAddress());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            // 批量回调，一般不推荐使用，使用上面那个会更灵活
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            // 扫描失败，并且失败原因
        }
    }

    private int GattState2DeviceState(int GattState) {
        int state = DeviceBean.DEVICE_STATE_IDLE;
        switch (GattState) {
            case BluetoothProfile.STATE_CONNECTED:
                state = DeviceBean.DEVICE_STATE_CONNECTED;
                break;
            case BluetoothProfile.STATE_CONNECTING:
                state = DeviceBean.DEVICE_STATE_CONNECTING;
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                state = DeviceBean.DEVICE_STATE_IDLE;
                break;
            case BluetoothProfile.STATE_DISCONNECTING:
                state = DeviceBean.DEVICE_STATE_DISCONNECTING;
                break;
            default:
                break;
        }
        return state;
    }

    // BLE连接回调
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, final int status, final int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            String address = gatt.getDevice().getAddress();

            if (bluetoothLEServiceCallback != null) {
                bluetoothLEServiceCallback.onConnectionStateChange(gatt.getDevice().getName(), address, GattState2DeviceState(newState));
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) { // 连接成功判断
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                gatt.discoverServices(); // 发现服务
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, final int status) {
            super.onServicesDiscovered(gatt, status);
            if (status != BluetoothGatt.GATT_SUCCESS) { // 发现服务失败
            }
            //不是失败的情况就是成功
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, final int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {  // 写Descriptor失败
            }
            //不是失败的情况就是成功
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //BLE设备主动向手机发送的数据时收到的数据回调
            characteristic.getValue(); // 通过这个方法来提取收到的数据
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {  // 写数据失败
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (bluetoothLEServiceCallback != null) {
                bluetoothLEServiceCallback.onDataRead(gatt.getDevice().getAddress());
            }
        }
    };

    @Override
    public void onDestroy() {
        isStarted = false;
        cancelDiscovery();

        for (Map.Entry<String, BluetoothGatt> entry : bluetoothGattMap.entrySet()) {
            BluetoothGatt gatt = entry.getValue();
            gatt.close();
        }

        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }

        executorService.shutdownNow();
        bluetoothAdapter = null;
        BKServiceSharePreferencesUtil.writeStopBKServicePreferences(this, true);
        super.onDestroy();
    }

    public boolean isBluetoothEnabled() {
        return isBluetoothEnabled;
    }

    public void forceEnableBluetooth() {
        if (isStarted) {
            if (bluetoothAdapter != null) {
                bluetoothAdapter.enable();
            }
        }
    }

    public void forceDisableBluetooth() {
        if (isStarted) {
            if (bluetoothAdapter != null) {
                for (Map.Entry<String, BluetoothGatt> entry : bluetoothGattMap.entrySet()) {
                    BluetoothGatt gatt = entry.getValue();
                    gatt.close();
                }
                bluetoothGattMap.clear();
                lruGattAddress.clear();
                sendDataAddressSet.clear();
                isSendDatas1 = false;
                isSendDatas2 = false;

                bluetoothAdapter.disable();
            }
        }
    }

    public void enableBluetooth() {
        if (isStarted && !isBluetoothEnabled) {
            if (bluetoothAdapter != null) {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent i = new Intent(Actions.ACTION_REQUEST_ENABLE_BLUETOOTH);
                    sendBroadcast(i);
                    //bluetoothAdapter.enable(); // 强制开启
                } else {
                    isBluetoothEnabled = true;
                    if (bluetoothLEServiceCallback != null) {
                        bluetoothLEServiceCallback.onBluetoothEnabled(isBluetoothEnabled);
                    }
                }
            }
        }
    }

    public boolean isServiceStarted() {
        return isStarted;
    }

    private synchronized void changeModeSpeed(@NonNull final Intent intent) {
        if (bluetoothAdapter != null) {
            sendDatas2[0] = (byte) 0x7e;
            sendDatas2[1] = (byte) 0x04;
            sendDatas2[2] = (byte) 0x02;
            sendDatas2[3] = (byte) intent.getIntExtra(Extras.BLUETOOTHLE_MODE_CHANGE_SPEED, 0);
            sendDatas2[4] = (byte) 0xff;
            sendDatas2[5] = (byte) 0xff;
            sendDatas2[6] = (byte) 0xff;
            sendDatas2[7] = (byte) 0x00;
            sendDatas2[8] = (byte) 0xef;
            isSendDatas2 = true;
            getAddressFromIntent(intent);

//            sendCommand();
        }
    }

    private synchronized void changeMode(@NonNull final Intent intent) {
        if (bluetoothAdapter != null) {
            sendDatas1[0] = (byte) 0x7e;
            sendDatas1[1] = (byte) 0x05;
            sendDatas1[2] = (byte) 0x03;
            sendDatas1[3] = (byte) (0x80 + intent.getIntExtra(Extras.BLUETOOTHLE_MODE, 0));
            sendDatas1[4] = (byte) 0x03;
            sendDatas1[5] = (byte) 0xff;
            sendDatas1[6] = (byte) 0xff;
            sendDatas1[7] = (byte) 0x00;
            sendDatas1[8] = (byte) 0xef;
            isSendDatas1 = true;
            getAddressFromIntent(intent);

            sendCommand();
        }
    }

    private synchronized void changeBrightness(@NonNull final Intent intent) {
        if (bluetoothAdapter != null) {
            sendDatas2[0] = (byte) 0x7e;
            sendDatas2[1] = (byte) 0x04;
            sendDatas2[2] = (byte) 0x01;
            sendDatas2[3] = (byte) intent.getIntExtra(Extras.BLUETOOTHLE_BRIGHTNESS, 0);
            sendDatas2[4] = (byte) 0xff;
            sendDatas2[5] = (byte) 0xff;
            sendDatas2[6] = (byte) 0xff;
            sendDatas2[7] = (byte) 0x00;
            sendDatas2[8] = (byte) 0xef;
            isSendDatas2 = true;
            getAddressFromIntent(intent);

//            sendCommand();
        }
    }

    private synchronized void music_amplitude(@NonNull final Intent intent) {
        if (bluetoothAdapter != null) {
            int rgb = intent.getIntExtra(Extras.BLUETOOTHLE_COLOR, 0);

            sendDatas1[0] = (byte) 0x7e;
            sendDatas1[1] = (byte) 0x04;
            sendDatas1[2] = (byte) 0x01;
            sendDatas1[3] = (byte) intent.getIntExtra(Extras.BLUETOOTHLE_BRIGHTNESS, 0);
            sendDatas1[4] = (byte) 0xff;
            sendDatas1[5] = (byte) 0xff;
            sendDatas1[6] = (byte) 0xff;
            sendDatas1[7] = (byte) 0x00;
            sendDatas1[8] = (byte) 0xef;
            isSendDatas1 = true;

            sendDatas2[0] = (byte) 0x7e;
            sendDatas2[1] = (byte) 0x07;
            sendDatas2[2] = (byte) 0x05;
            sendDatas2[3] = (byte) 0x03;
            sendDatas2[4] = (byte) ((rgb >> 16) & 0xff);
            sendDatas2[5] = (byte) ((rgb >> 8) & 0xff);
            sendDatas2[6] = (byte) (rgb & 0xff);
            sendDatas2[7] = (byte) 0x00;
            sendDatas2[8] = (byte) 0xef;
            isSendDatas2 = true;
            getAddressFromIntent(intent);

//            sendCommand();
        }
    }

    private synchronized void changeColor(@NonNull final Intent intent) {
        if (bluetoothAdapter != null) {
            int rgb = intent.getIntExtra(Extras.BLUETOOTHLE_COLOR, 0);
            sendDatas1[0] = (byte) 0x7e;
            sendDatas1[1] = (byte) 0x07;
            sendDatas1[2] = (byte) 0x05;
            sendDatas1[3] = (byte) 0x03;
            sendDatas1[4] = (byte) ((rgb >> 16) & 0xff);
            sendDatas1[5] = (byte) ((rgb >> 8) & 0xff);
            sendDatas1[6] = (byte) (rgb & 0xff);
            sendDatas1[7] = (byte) 0x00;
            sendDatas1[8] = (byte) 0xef;
            isSendDatas1 = true;
            getAddressFromIntent(intent);

//            sendCommand();
        }
    }

    private synchronized void changeColorTemperature(@NonNull final Intent intent) {
        if (bluetoothAdapter != null) {
            sendDatas1[0] = (byte) 0x7e;
            sendDatas1[1] = (byte) 0x06;
            sendDatas1[2] = (byte) 0x05;
            sendDatas1[3] = (byte) 0x02;
            sendDatas1[4] = (byte) intent.getIntExtra(Extras.BLUETOOTHLE_COLOR_WARM, 0);
            sendDatas1[5] = (byte) intent.getIntExtra(Extras.BLUETOOTHLE_COLOR_COLD, 0);
            sendDatas1[6] = (byte) 0xff;
            sendDatas1[7] = (byte) 0x08;
            sendDatas1[8] = (byte) 0xef;
            isSendDatas1 = true;
            getAddressFromIntent(intent);

//            sendCommand();
        }
    }

    private synchronized void readConnectionState(@NonNull final Intent intent) {
        synchronized (BluetoothLEService.class) {
            BluetoothGatt gatt = bluetoothGattMap.get(intent.getStringExtra(Extras.BLUETOOTHLE_ADDRESS));

            if (gatt != null) {
                List<BluetoothGattService> services = gatt.getServices();
                if (services != null && services.size() > 0) {
                    List<BluetoothGattCharacteristic> state_characts = services.get(1).getCharacteristics();
                    if (state_characts != null && state_characts.size() > 0) {
                        BluetoothGattCharacteristic state_charact = state_characts.get(0);
                        gatt.readCharacteristic(state_charact);
                    }
                }
            }
        }
    }

    private synchronized void changeSingleColor(@NonNull final Intent intent) {
        if (bluetoothAdapter != null) {
            sendDatas1[0] = (byte) 0x7e;
            sendDatas1[1] = (byte) 0x05;
            sendDatas1[2] = (byte) 0x05;
            sendDatas1[3] = (byte) 0x01;
            sendDatas1[4] = (byte) intent.getIntExtra(Extras.BLUETOOTHLE_SINGLE_COLOR, 0);
            sendDatas1[5] = (byte) 0xff;
            sendDatas1[6] = (byte) 0xff;
            sendDatas1[7] = (byte) 0x08;
            sendDatas1[8] = (byte) 0xef;
            isSendDatas1 = true;
            getAddressFromIntent(intent);

//            sendCommand();
        }
    }

    private synchronized void lightOn(@NonNull final Intent intent) {
        if (bluetoothAdapter != null) {
            sendDatas1[0] = (byte) 0x7e;
            sendDatas1[1] = (byte) 0x04;
            sendDatas1[2] = (byte) 0x04;
            sendDatas1[3] = (byte) (intent.getBooleanExtra(Extras.BLUETOOTHLE_LIGHT_ON, false) ? 1 : 0);
            sendDatas1[4] = (byte) 0xff;
            sendDatas1[5] = (byte) 0xff;
            sendDatas1[6] = (byte) 0xff;
            sendDatas1[7] = (byte) 0x00;
            sendDatas1[8] = (byte) 0xef;
            isSendDatas1 = true;
            getAddressFromIntent(intent);

            sendCommand();
        }
    }

    private void getAddressFromIntent(@NonNull Intent intent) {
        List<String> addressList = intent.getStringArrayListExtra(Extras.BLUETOOTHLE_ADDRESS_LIST);

        if (addressList != null) {
            for (String address : addressList) {
                if (address != null && !address.isEmpty()) {
                    sendDataAddressSet.add(address);
                }
            }
        }

        String address = intent.getStringExtra(Extras.BLUETOOTHLE_ADDRESS);

        if (address != null && !address.isEmpty()) {
            sendDataAddressSet.add(address);
        }
    }

    public void setBluetoothLEServiceCallback(BluetoothCallback bluetoothLEServiceCallback) {
        this.bluetoothLEServiceCallback = bluetoothLEServiceCallback;
    }
}
