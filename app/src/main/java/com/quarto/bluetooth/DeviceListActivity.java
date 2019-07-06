package com.quarto.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatImageButton;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quarto.R;

import java.util.ArrayList;
import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    String TAG = "DeviceListActivity";
    String EXTRA_DEVICE_ADDRESS = "device_address";
    BluetoothAdapter BtAdapter;
    Set<BluetoothDevice> pairedDevices;

    ArrayList<Device> paired_devices, new_devices;
    DeviceListviewAdapter paired_devices_adapter, new_devices_adapter;

    TextView txt_nodevice;
    AppCompatImageButton btn_scan;
    ListView pairedListView, newDevicesListView ;
    ProgressBar prb_scanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_devicelist);
        setResult(AppCompatActivity.RESULT_CANCELED);
        createLayout();


        BtAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = BtAdapter.getBondedDevices();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(Receiver, filter);

        paired_devices = new ArrayList<>();
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName()!=null) {
                    paired_devices.add(new Device(device.getName(), device.getAddress()));
                }else {
                    paired_devices.add(new Device(device.getAddress(), device.getAddress()));
                }
            }
        } else {
            String noDevices = "No devices found";
            paired_devices.add(new Device(noDevices, noDevices));
        }
        paired_devices_adapter = new DeviceListviewAdapter(this, paired_devices);
        pairedListView.setAdapter(paired_devices_adapter);


        new_devices = new ArrayList<>();
        new_devices_adapter = new DeviceListviewAdapter(getBaseContext(), new_devices);

        btn_scan.setOnClickListener(v -> {
            doDiscovery();
            btn_scan.setEnabled(false);
        });

        pairedListView.setOnItemClickListener((adapterView, view, i, l) -> {

            String address = paired_devices.get(i).getAddress();
            if (address.length()==17) {
                BtAdapter.cancelDiscovery();

                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                setResult(AppCompatActivity.RESULT_OK, intent);
                finish();
            }
        });

        newDevicesListView.setOnItemClickListener((adapterView, view, i, l) -> {

            String address = new_devices.get(i).getAddress();
            if (address.length()==17) {
                BtAdapter.cancelDiscovery();

                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                setResult(AppCompatActivity.RESULT_OK, intent);
                finish();
            }
        });

    }

    //**********************************************************************************************

    public void createLayout() {

        txt_nodevice = (TextView) findViewById(R.id.txt_actvdevicelist_nodevice);
        btn_scan = (AppCompatImageButton) findViewById(R.id.button_scan);
        pairedListView = (ListView) findViewById(R.id.paired_devices);
        newDevicesListView = (ListView) findViewById(R.id.new_devices);
        prb_scanning = (ProgressBar) findViewById(R.id.prb_actvdevicelist_scanning);
        prb_scanning.setIndeterminate(true);
    }

    private void doDiscovery() {

        prb_scanning.animate().alpha(1f).setDuration(1000).start();
        prb_scanning.setVisibility(View.VISIBLE);

        if (BtAdapter.isDiscovering()) {
            BtAdapter.cancelDiscovery();
        }
        BtAdapter.startDiscovery();
    }

    private final BroadcastReceiver Receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            newDevicesListView.setAdapter(new_devices_adapter);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    if (device.getName()!=null) {
                        new_devices.add(new Device(device.getName(), device.getAddress()));
                    }else {
                        new_devices.add(new Device(device.getAddress(), device.getAddress()));
                    }

                    findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
                    newDevicesListView.setVisibility(View.VISIBLE);
                    newDevicesListView.animate().alpha(1f).setDuration(500).start();
                    new_devices_adapter.notifyDataSetChanged();
                    txt_nodevice.setVisibility(View.GONE);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (new_devices_adapter.getCount() == 0) {
                    txt_nodevice.setVisibility(View.VISIBLE);
                }
                prb_scanning.animate().alpha(0f).setDuration(1000).start();
                Handler handler = new Handler();
                handler.postDelayed(() -> prb_scanning.setVisibility(View.GONE),1000);
                btn_scan.setEnabled(true);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (BtAdapter != null) {
            BtAdapter.cancelDiscovery();
        }
        unregisterReceiver(Receiver);
    }


}