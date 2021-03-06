package wl.smartled.test.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import wl.smartled.test.R;
import wl.smartled.test.adapter.DeviceListAdapter;
import wl.smartled.test.bean.DeviceBean;
import wl.smartled.test.callback.BluetoothCallback;
import wl.smartled.test.constants.Actions;
import wl.smartled.test.constants.Extras;
import wl.smartled.test.constants.Messages;
import wl.smartled.test.data.CommonControl;
import wl.smartled.test.permission.PermissionReq;
import wl.smartled.test.permission.PermissionResult;
import wl.smartled.test.utils.BKServiceSharePreferencesUtil;
import wl.smartled.test.utils.BluetoothUtil;
import wl.smartled.test.utils.CircleProgressDialogUtil;
import wl.smartled.test.utils.ListUtil;
import wl.smartled.test.utils.LogUtil;
import wl.smartled.test.utils.PermissionsBroadcastUtil;
import wl.smartled.test.view.AlphaImageButton;


public class MainActivity extends AppCompatActivity implements BluetoothCallback, View.OnTouchListener, View.OnClickListener {
    private static final String TAG = "MainActivity";

    private static final int START_STOP_SCAN_INTERVAL = 3000;
    private static final int STOP_START_SCAN_INTERVAL = 3000;

    private static final int CHECK_TIME_INTERVAL = 500;

    private static final int RECONNECTING_WAIT_INTERVAL = 8000;

    private static final int CONNECTED_TIMEOUT_INTERVAL = 1500;
    private static final int READ_CONNECTION_STATE_TIMEOUT_INTERVAL = 2000;
    private static final int READ_CONNECTION_STATE_TIMEOUT_FORCE_CLOSE_INTERVAL = 4000;

    private static final int SEND_TEST_COMPLETE_DELAY = 1000;

    private static final int TRY_RESTART_BKSERVICE_INTERVAL = 2000;
    private static final int TRY_RESTART_BKSERVICE_TIMES_MAX = 15;
    private int tryRestartBKServiceTimes = 0;

    private AlphaImageButton sendCommand1Button1;
    private AlphaImageButton sendCommand1Button2;
    private AlphaImageButton sendCommand1Button3;
    private AlphaImageButton sendCommand1Button4;

    private AlphaImageButton restartApp;
    private AlphaImageButton restartBluetooth;

    private List<DeviceBean> scanList;
    private List<DeviceBean> connectList;
    private List<DeviceBean> scanListInListView = new ArrayList<>();
    private List<DeviceBean> connectListInListView = new ArrayList<>();

    private DeviceListAdapter scanListAdapter;
    private DeviceListAdapter connectListAdapter;

    private ListView scanListView;
    private ListView connectListView;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Messages.PERMISSION_REQUEST_MESSAGE) {
                final String permissions[] = msg.getData().getStringArray(Extras.PERMISSION_NAME);

                if (permissions != null && permissions.length > 0) {
                    PermissionReq.with(MainActivity.this)
                            .permissions(permissions)
                            .result(new PermissionResult() {
                                @Override
                                public void onGranted() {
                                    PermissionsBroadcastUtil.sendRequestPermissionResultBroadcast(MainActivity.this, true, permissions);
                                }

                                @Override
                                public void onDenied() {
                                    PermissionsBroadcastUtil.sendRequestPermissionResultBroadcast(MainActivity.this, false, permissions);
                                }
                            })
                            .request();
                }
            } else if (msg.what == Messages.BLUETOOTHLE_SCAN_REQUEST_MESSAGE) {
                boolean isScanListChanged = false;
                Message scanMessage = Message.obtain();
                scanMessage.what = Messages.BLUETOOTHLE_SCAN_REQUEST_MESSAGE;

                if (msg.arg1 == 1) {
                    BluetoothUtil.getInstance().stopScanLEDevice(MainActivity.this);

                    scanMessage.arg1 = 0;
                    scanMessage.arg2 = 0;
                    handler.sendMessageDelayed(scanMessage, STOP_START_SCAN_INTERVAL);
                } else {
                    synchronized (scanList) {
                        for (int i = 0; i < scanList.size(); ++i) {
                            DeviceBean deviceBean = scanList.get(i);
                            if (deviceBean.getState() == DeviceBean.DEVICE_STATE_IDLE) {
                                scanList.remove(i);
                                --i;
                                isScanListChanged = true;
                            }
                        }
                    }
                    BluetoothUtil.getInstance().scanLEDevice(MainActivity.this, msg.arg2 != 0);

                    scanMessage.arg1 = 1;
                    scanMessage.arg2 = 0;
                    handler.sendMessageDelayed(scanMessage, START_STOP_SCAN_INTERVAL);
                }

                if (isScanListChanged) {
                    handler.sendEmptyMessage(Messages.BLUETOOTHLE_SCAN_LIST_CHANGED_MESSAGE);
                }
            } else if (msg.what == Messages.BLUETOOTHLE_SCAN_LIST_CHANGED_MESSAGE) {
                removeMessages(Messages.BLUETOOTHLE_SCAN_LIST_CHANGED_MESSAGE);
                synchronized (scanList) {
                    scanListInListView.clear();
                    for (DeviceBean deviceBean : scanList) {
                        scanListInListView.add(deviceBean);
                    }
                    scanListAdapter.notifyDataSetChanged();
                }
            } else if (msg.what == Messages.BLUETOOTHLE_CONNECT_LIST_CHANGED_MESSAGE) {
                removeMessages(Messages.BLUETOOTHLE_CONNECT_LIST_CHANGED_MESSAGE);
                synchronized (connectList) {
                    connectListInListView.clear();
                    for (DeviceBean deviceBean : connectList) {
                        if (deviceBean.isConfirmConnected()) {
                            connectListInListView.add(deviceBean);
                        }
                    }
                    connectListAdapter.notifyDataSetChanged();
                }
            } else if (msg.what == Messages.BLUETOOTHLE_SEND_TEST_COMPLETE_COMMAND) {
                removeMessages(Messages.BLUETOOTHLE_SEND_TEST_COMPLETE_COMMAND);

                List<String> sendList;
                synchronized (connectList) {
                    if (connectList.size() > 0) {
                        sendList = new ArrayList<>();

                        for (DeviceBean deviceBean : connectList) {
                            sendList.add(deviceBean.getAddress());
                        }
                        if (connectList.size() > 0) {
                            BluetoothUtil.getInstance().sendMode(MainActivity.this, sendList, 7);
                        }
                    }
                }
            } else if (msg.what == Messages.START_BK_SERVICE_COMMAND) {
                checkStartBKService();
            }
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionReq.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Actions.ACTION_REQUEST_PERMISSION)) {
                PermissionsBroadcastUtil.sendPermissionsMessage(intent, handler);
            } else if (action.equals(Actions.ACTION_REQUEST_ENABLE_BLUETOOTH)) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);  // 弹对话框的形式提示用户开启蓝牙
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkStartBKService();

        initView();
        initEvent();
        initData();
    }

    private void checkStartBKService() {
        handler.removeMessages(Messages.START_BK_SERVICE_COMMAND);

        if (BKServiceSharePreferencesUtil.getStopBKServicePreferences(this, true) || tryRestartBKServiceTimes >= TRY_RESTART_BKSERVICE_TIMES_MAX) {
            doBKService(true);
            BKServiceSharePreferencesUtil.writeStopBKServicePreferences(MainActivity.this, true);
        } else {
            handler.sendEmptyMessageDelayed(Messages.START_BK_SERVICE_COMMAND, TRY_RESTART_BKSERVICE_INTERVAL);
            ++tryRestartBKServiceTimes;
        }
    }

    private void initData() {
        scanList = CommonControl.getInstance().getScanList();
        connectList = CommonControl.getInstance().getConnectList();

        scanListAdapter = new DeviceListAdapter(this, scanListInListView);
        scanListView.setAdapter(scanListAdapter);

        connectListAdapter = new DeviceListAdapter(this, connectListInListView);
        connectListView.setAdapter(connectListAdapter);
    }

    private void initView() {
        sendCommand1Button1 = (AlphaImageButton) findViewById(R.id.aib_sendCommand1);
        sendCommand1Button2 = (AlphaImageButton) findViewById(R.id.aib_sendCommand2);
        sendCommand1Button3 = (AlphaImageButton) findViewById(R.id.aib_sendCommand3);
        sendCommand1Button4 = (AlphaImageButton) findViewById(R.id.aib_sendCommand4);

        restartApp = (AlphaImageButton) findViewById(R.id.aib_restartapp);
        restartBluetooth = (AlphaImageButton) findViewById(R.id.aib_restartbluetooth);

        scanListView = (ListView) findViewById(R.id.scanList);
        connectListView = (ListView) findViewById(R.id.connectedList);
    }

    private void initEvent() {
        sendCommand1Button1.setOnTouchListener(this);
        sendCommand1Button2.setOnTouchListener(this);
        sendCommand1Button3.setOnTouchListener(this);
        sendCommand1Button4.setOnTouchListener(this);

        restartApp.setOnClickListener(this);
        restartBluetooth.setOnClickListener(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Actions.ACTION_REQUEST_ENABLE_BLUETOOTH);
        intentFilter.addAction(Actions.ACTION_REQUEST_PERMISSION);
        registerReceiver(receiver, intentFilter);

        executorService.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LogUtil.d(TAG, "scheduleAtFixedRate");

                synchronized (scanList) {
                    boolean refreshScanList = false;

                    for (int i = 0; i < scanList.size(); ++i) {
                        DeviceBean deviceBean = scanList.get(i);
                        if (deviceBean.getState() == DeviceBean.DEVICE_STATE_CONNECTING) {
                            deviceBean.setTime(deviceBean.getTime() + CHECK_TIME_INTERVAL);

                            if (deviceBean.getTime() >= RECONNECTING_WAIT_INTERVAL) {
                                LogUtil.d(TAG, "scheduleAtFixedRate --> connecting timeout = " + deviceBean.getAddress());

                                deviceBean.setTime(0);
                                deviceBean.setState(DeviceBean.DEVICE_STATE_IDLE);
                                BluetoothUtil.getInstance().disconnectDevice(MainActivity.this, deviceBean.getAddress());
                                refreshScanList = true;
                            }
                        }
                    }

                    if (refreshScanList) {
                        handler.sendEmptyMessage(Messages.BLUETOOTHLE_SCAN_LIST_CHANGED_MESSAGE);
                    }
                }

                synchronized (connectList) {
                    boolean refreshConnectList = false;
                    for (int i = 0; i < connectList.size(); ++i) {
                        DeviceBean deviceBean = connectList.get(i);
                        deviceBean.setTime(deviceBean.getTime() + CHECK_TIME_INTERVAL);

                        if (deviceBean.getState() == DeviceBean.DEVICE_STATE_DISCONNECTING) {
                            if (deviceBean.getTime() >= READ_CONNECTION_STATE_TIMEOUT_FORCE_CLOSE_INTERVAL) {
                                LogUtil.d(TAG, "scheduleAtFixedRate --> connection timeout2 = " + deviceBean.getAddress());
                                BluetoothUtil.getInstance().releaseResource(MainActivity.this, deviceBean.getAddress());
                                connectList.remove(i);
                                --i;
                                refreshConnectList = true;
                            }
                        } else if (deviceBean.getState() == DeviceBean.DEVICE_STATE_READ_PENDING) {
                            if (deviceBean.getTime() >= READ_CONNECTION_STATE_TIMEOUT_INTERVAL) {
                                LogUtil.d(TAG, "scheduleAtFixedRate --> connection timeout1 = " + deviceBean.getAddress());

                                deviceBean.setTime(0);
                                BluetoothUtil.getInstance().disconnectDevice(MainActivity.this, deviceBean.getAddress());
                                deviceBean.setState(DeviceBean.DEVICE_STATE_DISCONNECTING);
                                refreshConnectList = true;
                            }
                        } else if (deviceBean.getState() == DeviceBean.DEVICE_STATE_CONNECTED) {
                            if (deviceBean.getTime() >= CONNECTED_TIMEOUT_INTERVAL) {
                                LogUtil.d(TAG, "scheduleAtFixedRate --> reading connection = " + deviceBean.getAddress());

                                deviceBean.setTime(0);
                                deviceBean.setState(DeviceBean.DEVICE_STATE_READ_PENDING);
                                BluetoothUtil.getInstance().readConnectionState(MainActivity.this, deviceBean.getAddress());
                            }
                        }
                    }
                    if (refreshConnectList) {
                        handler.sendEmptyMessage(Messages.BLUETOOTHLE_CONNECT_LIST_CHANGED_MESSAGE);
                    }
                }
            }
        }, CHECK_TIME_INTERVAL, CHECK_TIME_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.aib_restartapp) {
            CircleProgressDialogUtil.show(this, getString(R.string.string_restarting_app));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BKServiceSharePreferencesUtil.writeStopBKServicePreferences(MainActivity.this, false);
                    //重启app代码
                    Intent intent = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(getBaseContext().getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }, 100L);
        } else if (v.getId() == R.id.aib_restartbluetooth) {
            CircleProgressDialogUtil.show(this, getString(R.string.string_restarting_bluetooth));
            handler.removeMessages(Messages.BLUETOOTHLE_SCAN_REQUEST_MESSAGE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BluetoothUtil.getInstance().forceDisableBluetooth();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    BluetoothUtil.getInstance().forceEnableBluetooth();
                }
            }).start();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CircleProgressDialogUtil.hideDialog();
                }
            }, 3000L);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        synchronized (connectList) {
            if (connectList.size() > 0) {
                List<String> sendList = new ArrayList<>();

                for (DeviceBean deviceBean : connectList) {
                    sendList.add(deviceBean.getAddress());
                }

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    switch (v.getId()) {
                        case R.id.aib_sendCommand1:
                            BluetoothUtil.getInstance().sendColor(this, sendList, 0xffff0000);
                            break;
                        case R.id.aib_sendCommand2:
                            BluetoothUtil.getInstance().sendColor(this, sendList, 0xff00ff00);
                            break;
                        case R.id.aib_sendCommand3:
                            BluetoothUtil.getInstance().sendColor(this, sendList, 0xff0000ff);
                            break;
                        case R.id.aib_sendCommand4:
                            BluetoothUtil.getInstance().sendSingleColor(this, sendList, 100);
                            break;
                    }

                    handler.removeMessages(Messages.BLUETOOTHLE_SEND_TEST_COMPLETE_COMMAND);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    switch (v.getId()) {
                        case R.id.aib_sendCommand1:
                            BluetoothUtil.getInstance().sendColor(this, sendList, 0);
                            break;
                        case R.id.aib_sendCommand2:
                            BluetoothUtil.getInstance().sendColor(this, sendList, 0);
                            break;
                        case R.id.aib_sendCommand3:
                            BluetoothUtil.getInstance().sendColor(this, sendList, 0);
                            break;
                        case R.id.aib_sendCommand4:
                            BluetoothUtil.getInstance().sendColor(this, sendList, 0);
                            break;
                    }

                    handler.sendEmptyMessageDelayed(Messages.BLUETOOTHLE_SEND_TEST_COMPLETE_COMMAND, SEND_TEST_COMPLETE_DELAY);
                }
            }
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        doBKService(false);

        unregisterReceiver(receiver);
        executorService.shutdownNow();

        removeMessages();
        CircleProgressDialogUtil.hideDialog();

        super.onDestroy();
    }

    private void removeMessages() {
        handler.removeMessages(Messages.START_BK_SERVICE_COMMAND);
        handler.removeMessages(Messages.BLUETOOTHLE_CONNECT_LIST_CHANGED_MESSAGE);
        handler.removeMessages(Messages.BLUETOOTHLE_SCAN_LIST_CHANGED_MESSAGE);
        handler.removeMessages(Messages.BLUETOOTHLE_SCAN_REQUEST_MESSAGE);
        handler.removeMessages(Messages.BLUETOOTHLE_SEND_TEST_COMPLETE_COMMAND);
        handler.removeMessages(Messages.PERMISSION_REQUEST_MESSAGE);
        handler.removeMessages(Messages.PERMISSION_RESULT_MESSAGE);
    }

    @Override
    public void onBluetoothEnabled(boolean enabled) {
        if (enabled) {
            handler.removeMessages(Messages.BLUETOOTHLE_SCAN_REQUEST_MESSAGE);

            Message scanMessage = Message.obtain();
            scanMessage.what = Messages.BLUETOOTHLE_SCAN_REQUEST_MESSAGE;
            scanMessage.arg1 = 0;
            scanMessage.arg2 = 1;

            handler.sendMessageDelayed(scanMessage, 100);
        } else {
            handler.removeMessages(Messages.BLUETOOTHLE_SCAN_REQUEST_MESSAGE);
        }
    }

    @Override
    public void onScanResult(final String name, final String address) {
        LogUtil.d(TAG, "onScanResult, address = " + address + ", name = " + name);

        synchronized (connectList) {
            int index = ListUtil.containsDeviceBean(connectList, address);

            if (index != -1) {
                LogUtil.w(TAG, "onScanResult: connected device found!!!");
                return;
            }
        }

        synchronized (scanList) {
            int index = ListUtil.containsDeviceBean(scanList, address);

            if (index == -1) {
                scanList.add(new DeviceBean(address, name, DeviceBean.DEVICE_STATE_CONNECTING));
                BluetoothUtil.getInstance().connectDevice(MainActivity.this, address);
                handler.sendEmptyMessage(Messages.BLUETOOTHLE_SCAN_LIST_CHANGED_MESSAGE);
            }
        }
    }

    @Override
    public void onConnectionStateChange(final String name, final String address, final int state) {
        LogUtil.d(TAG, "onConnectionStateChange, state = " + state + ", scanList = " + scanList.size() + ", connectList = " + connectList.size());
        if (state == DeviceBean.DEVICE_STATE_CONNECTED) {
            synchronized (scanList) {
                int index = ListUtil.containsDeviceBean(scanList, address);

                if (index != -1) {
                    scanList.remove(index);
                    handler.sendEmptyMessage(Messages.BLUETOOTHLE_SCAN_LIST_CHANGED_MESSAGE);
                }
            }
            synchronized (connectList) {
                int index = ListUtil.containsDeviceBean(connectList, address);

                if (index == -1) {
                    connectList.add(new DeviceBean(address, name, DeviceBean.DEVICE_STATE_CONNECTED));
                    handler.sendEmptyMessage(Messages.BLUETOOTHLE_CONNECT_LIST_CHANGED_MESSAGE);
                }
            }
        } else {
            synchronized (connectList) {
                int index = ListUtil.containsDeviceBean(connectList, address);

                if (index != -1) {
                    BluetoothUtil.getInstance().releaseResource(MainActivity.this, address);
                    connectList.remove(index);
                    handler.sendEmptyMessage(Messages.BLUETOOTHLE_CONNECT_LIST_CHANGED_MESSAGE);
                }
            }

            synchronized (scanList) {
                int index = ListUtil.containsDeviceBean(scanList, address);

                if (index != -1) {
                    DeviceBean deviceBean = scanList.get(index);
                    deviceBean.setState(DeviceBean.DEVICE_STATE_IDLE);
                    deviceBean.setTime(0);
                    handler.sendEmptyMessage(Messages.BLUETOOTHLE_SCAN_LIST_CHANGED_MESSAGE);
                }
            }
        }
    }

    @Override
    public void onDataRead(final String address) {
        synchronized (connectList) {
            LogUtil.d(TAG, "onDataRead, address = " + address);

            int index = ListUtil.containsDeviceBean(connectList, address);
            if (index != -1) {
                DeviceBean deviceBean = connectList.get(index);

                if (!deviceBean.isConfirmConnected()) {
                    deviceBean.setConfirmConnected(true);
                }
                deviceBean.setState(DeviceBean.DEVICE_STATE_CONNECTED);
                deviceBean.setTime(0);
                handler.sendEmptyMessage(Messages.BLUETOOTHLE_CONNECT_LIST_CHANGED_MESSAGE);
            }
        }
    }

    private void doBKService(boolean start) {
        Intent i = new Intent(Actions.ACTION_BLUETOOTHLE_SERVICE);
        i.setPackage(getPackageName());

        if (start) {
            BluetoothUtil.getInstance().setBluetoothCallback(this);
            BluetoothUtil.getInstance().bindService(this);
        } else {
            BluetoothUtil.getInstance().setBluetoothCallback(null);
            BluetoothUtil.getInstance().unbindService(this);
        }

        if (start) {
            startService(i);
        } else {
            stopService(i);
        }
    }
}
