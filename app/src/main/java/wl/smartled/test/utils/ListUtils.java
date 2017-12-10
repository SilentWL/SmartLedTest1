package wl.smartled.test.utils;

import android.bluetooth.BluetoothGatt;

import java.util.List;

import wl.smartled.test.bean.DeviceBean;

/**
 * Created by Administrator on 2017/11/8 0008.
 */

public class ListUtils {

    public static int containsDeviceBean(List<DeviceBean> l, String address) {
        int index = -1;

        for (int i = 0; i < l.size(); ++i) {
            String dstAddress = l.get(i).getAddress();
            if (dstAddress != null && dstAddress.equals(address)) {
                index = i;
                break;
            }
        }

        return index;
    }

    public static boolean removeDeviceBean(List<DeviceBean> l, String address) {
        boolean remove = false;

        for (int i = 0; i < l.size(); ++i) {
            String dstAddress = l.get(i).getAddress();
            if (dstAddress != null && dstAddress.equals(address)) {
                l.remove(i);
                remove = true;
                break;
            }
        }

        return remove;
    }

    public static int containsGatt(List<BluetoothGatt> l, String address) {
        int index = -1;

        for (int i = 0; i < l.size(); ++i) {
            String dstAddress = l.get(i).getDevice().getAddress();
            if (dstAddress != null && dstAddress.equals(address)) {
                index = i;
                break;
            }
        }

        return index;
    }

    public static int containsString(List<String> l, String src) {
        int index = -1;

        for (int i = 0; i < l.size(); ++i) {
            String dst = l.get(i);
            if (dst != null && dst.equals(src)) {
                index = i;
                break;
            }
        }

        return index;
    }
}
