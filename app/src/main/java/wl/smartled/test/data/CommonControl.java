package wl.smartled.test.data;

import java.util.ArrayList;
import java.util.List;

import wl.smartled.test.bean.DeviceBean;

/**
 * Created by Administrator on 2017/11/8 0008.
 */

public class CommonControl {
    private static CommonControl commonControl;
    private List<DeviceBean> scanList;
    private List<DeviceBean> connectList;

    private CommonControl(){
        if (scanList == null){
            scanList = new ArrayList<>();
        }
        if (connectList == null){
            connectList = new ArrayList<>();
        }
    }
    public static CommonControl getInstance(){
        if (commonControl == null){
            synchronized (CommonControl.class){
                if (commonControl == null){
                    commonControl = new CommonControl();
                }
            }
        }
        return commonControl;
    }

    public List<DeviceBean> getScanList() {
        return scanList;
    }

    public List<DeviceBean> getConnectList() {
        return connectList;
    }
}
