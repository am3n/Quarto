package com.quarto.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.quarto.R;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class ConnectFragment extends Fragment {

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 384;
    public BluetoothSPP bt;
    View v;
    TextView txt_this, txt_turnon, txt_wait, txt_disconn;
    AppCompatImageButton imgb_turnon, imgb_conn;
    ProgressBar prb_connecting;
    ScaleAnimation scaleAnimation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_connect, container, false);
        createLayout();

        txt_this.setText(bt.getBluetoothAdapter().getName());

        if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
            txt_wait.setText("Your're already connected to\n"+bt.getConnectedDeviceName()+"!");
            animationOnConnected(imgb_conn);
            txt_disconn.setVisibility(View.VISIBLE);
        }

        if(bt.isBluetoothEnabled()) {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
            }
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }

        ensureDiscoverable();

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {

            public void onDeviceConnected(String name, String address) {

                txt_wait.setText(name+" Connected!");

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getActivity().getSupportFragmentManager().popBackStack("connectFragment"
                                    , FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        }catch (Exception e) {}
                    }
                },500);

                prb_connecting.animate().alpha(0f).setDuration(200).start();
            }
            public void onDeviceDisconnected() {
                txt_wait.setText("Disconnected");
                prb_connecting.animate().alpha(0f).setDuration(200).start();
                txt_disconn.setVisibility(View.GONE);
            }
            public void onDeviceConnectionFailed() {
                txt_wait.setText("Unable to connect");
                prb_connecting.animate().alpha(0f).setDuration(200).start();
            }
        });



        imgb_turnon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
            }
        });


        imgb_conn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bt.getServiceState() != BluetoothState.STATE_CONNECTED) {
                    if (bt.isServiceAvailable()) {
                        Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                    } else {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
                    }
                }
            }
        });
        imgb_conn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                    txt_disconn.setVisibility(View.GONE);
                }
                return true;
            }
        });



        return v;
    }

    //**********************************************************************************************

    private void createLayout() {

        txt_this = (TextView) v.findViewById(R.id.txt_frgconn_this);
        txt_turnon = (TextView) v.findViewById(R.id.txt_frgconn_turnon);
        txt_wait = (TextView) v.findViewById(R.id.txt_frgconn_wait);
        txt_disconn = (TextView) v.findViewById(R.id.txt_frgconn_disconn);
        imgb_turnon = (AppCompatImageButton) v.findViewById(R.id.imgb_frgconn_turnon);
        imgb_conn = (AppCompatImageButton) v.findViewById(R.id.imgb_frgconn_conn);
        prb_connecting = (ProgressBar) v.findViewById(R.id.prb_connecting);

        if(bt.isBluetoothEnabled()) {
            txt_turnon.animate().alpha(0f).setDuration(500).start();
            imgb_turnon.animate().alpha(0f).setDuration(500).start();
            txt_wait.animate().alpha(1f).setDuration(500).start();
            imgb_conn.animate().alpha(1f).setDuration(500).start();
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {

                bt.connect(data);

                txt_wait.animate().alpha(0f).setDuration(800).start();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        txt_wait.setText("Connecting...");
                        txt_wait.animate().alpha(1f).setDuration(500).start();
                    }
                },600);
                prb_connecting.animate().alpha(1f).setDuration(500).start();

            }
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {

                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);

                txt_turnon.animate().alpha(0f).setDuration(500).start();
                imgb_turnon.animate().alpha(0f).setDuration(500).start();
                txt_wait.animate().alpha(1f).setDuration(500).start();
                imgb_conn.animate().alpha(1f).setDuration(500).start();

            } else {
                Toast.makeText(getActivity(), "Bluetooth was not enabled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void ensureDiscoverable() {

        if (bt.getBluetoothAdapter().getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void animationOnConnected(final View view) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scaleAnimation = new ScaleAnimation(1f, 1.1f, 1f, 1.1f,
                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    scaleAnimation.setDuration(2000);
                    scaleAnimation.setRepeatCount(1);
                    scaleAnimation.setRepeatMode(Animation.REVERSE);
                    scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override public void onAnimationStart(Animation animation) {}
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                                view.startAnimation(scaleAnimation);
                            }
                        }
                        @Override public void onAnimationRepeat(Animation animation) {}
                    });
                    view.startAnimation(scaleAnimation);
                }
            });
        }catch (Exception e) {}
    }

}
