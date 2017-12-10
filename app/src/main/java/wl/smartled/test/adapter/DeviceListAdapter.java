package wl.smartled.test.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import wl.smartled.test.R;
import wl.smartled.test.bean.DeviceBean;

/**
 * Created by Administrator on 2017/11/8 0008.
 */

public class DeviceListAdapter extends BaseAdapter {
    private List<DeviceBean> deviceList;
    private LayoutInflater layoutInflater;
    private Context context;

    public DeviceListAdapter(Context context, List<DeviceBean> deviceList) {
        this.layoutInflater = LayoutInflater.from(context);
        this.deviceList = deviceList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.device_info_row, parent, false);
            viewHolder.address = (TextView) convertView.findViewById(R.id.id_tv_device_address);
            viewHolder.name = (TextView) convertView.findViewById(R.id.id_tv_device_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        DeviceBean deviceBean = deviceList.get(position);
        String name = deviceBean.getDeviceName();
        viewHolder.name.setText((name == null || name.isEmpty()) ? context.getString(R.string.string_no_name) : name);
        viewHolder.address.setText(deviceBean.getAddress());

        return convertView;
    }

    private static class ViewHolder {
        private TextView name;
        private TextView address;
    }
}
