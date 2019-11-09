package com.quarto.bluetooth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.quarto.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceListviewAdapter extends ArrayAdapter<Device> {

    private List<Device> devices;
    private Context context;
    private View view;

    DeviceListviewAdapter(Context context, ArrayList<Device> devices) {
        super(context, R.layout.itemlist_device, devices);
        this.context = context;
        this.devices = devices;
    }

    @NonNull
    @SuppressLint("ViewHolder")
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = vi.inflate(R.layout.itemlist_device, null);

        Device device = devices.get(position);

        TextView txt_name = view.findViewById(R.id.txt_device_name);
        txt_name.setText(device.getName());

        TextView txt_address = view.findViewById(R.id.txt_device_address);
        txt_address.setText(device.getAddress());

        return view;
    }

}
