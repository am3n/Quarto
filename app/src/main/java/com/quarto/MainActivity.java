package com.quarto;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatImageView;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quarto.bluetooth.ConnectFragment;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

public class MainActivity extends AppCompatActivity {

    Button btn_send, btn_conn;
    EditText edt_msg;
    TextView txt_msg;
    AppCompatImageView img_room_0_0, img_room_1_0, img_room_1_1, img_room_2_0, img_room_2_1, img_room_2_2
            , img_room_3_0, img_room_3_1, img_room_3_2, img_room_3_3, img_room_4_0, img_room_4_1, img_room_4_2
            , img_room_5_0, img_room_5_1, img_room_6_0;
    int quarto_drawables[] = {
            R.drawable.ic_circle_black_big_filled,
            R.drawable.ic_circle_black_big_hollow,
            R.drawable.ic_circle_black_small_filled,
            R.drawable.ic_circle_black_small_hollow,
            R.drawable.ic_circle_white_big_filled,
            R.drawable.ic_circle_white_big_hollow,
            R.drawable.ic_circle_white_small_filled,
            R.drawable.ic_circle_white_small_hollow,
            R.drawable.ic_square_black_big_filled,
            R.drawable.ic_square_black_big_hollow,
            R.drawable.ic_square_black_small_filled,
            R.drawable.ic_square_black_small_hollow,
            R.drawable.ic_square_white_big_filled,
            R.drawable.ic_square_white_big_hollow,
            R.drawable.ic_square_white_small_filled,
            R.drawable.ic_square_white_small_hollow
    };
    BluetoothSPP bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        create_layout();

        img_room_0_0.setImageResource(quarto_drawables[0]);
        img_room_1_0.setImageResource(quarto_drawables[1]);
        img_room_1_1.setImageResource(quarto_drawables[2]);
        img_room_2_0.setImageResource(quarto_drawables[3]);
        img_room_2_1.setImageResource(quarto_drawables[4]);
        img_room_2_2.setImageResource(quarto_drawables[5]);
        img_room_3_0.setImageResource(quarto_drawables[6]);
        img_room_3_1.setImageResource(quarto_drawables[7]);
        img_room_3_2.setImageResource(quarto_drawables[8]);
        img_room_3_3.setImageResource(quarto_drawables[9]);
        img_room_4_0.setImageResource(quarto_drawables[10]);
        img_room_4_1.setImageResource(quarto_drawables[11]);
        img_room_4_2.setImageResource(quarto_drawables[12]);
        img_room_5_0.setImageResource(quarto_drawables[13]);
        img_room_5_1.setImageResource(quarto_drawables[14]);
        img_room_6_0.setImageResource(quarto_drawables[15]);


        bt = new BluetoothSPP(getBaseContext());
        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            finish();
        }


        btn_conn.setOnClickListener(v -> {
            ConnectFragment connectFragment = new ConnectFragment();
            connectFragment.bt = bt;
            FragmentManager fragmentManager0 = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction0 = fragmentManager0.beginTransaction();
            fragmentTransaction0.add(R.id.activity_main, connectFragment);
            fragmentTransaction0.addToBackStack("connectFragment");
            fragmentTransaction0.commit();
        });

        btn_send.setOnClickListener(view -> bt.send(edt_msg.getText().toString(), true));

        bt.setOnDataReceivedListener((data, message) -> txt_msg.setText(message));

    }

    public void create_layout() {

        img_room_0_0 = findViewById(R.id.img_room_0_0);
        img_room_1_0 = findViewById(R.id.img_room_1_0);
        img_room_1_1 = findViewById(R.id.img_room_1_1);
        img_room_2_0 = findViewById(R.id.img_room_2_0);
        img_room_2_1 = findViewById(R.id.img_room_2_1);
        img_room_2_2 = findViewById(R.id.img_room_2_2);
        img_room_3_0 = findViewById(R.id.img_room_3_0);
        img_room_3_1 = findViewById(R.id.img_room_3_1);
        img_room_3_2 = findViewById(R.id.img_room_3_2);
        img_room_3_3 = findViewById(R.id.img_room_3_3);
        img_room_4_0 = findViewById(R.id.img_room_4_0);
        img_room_4_1 = findViewById(R.id.img_room_4_1);
        img_room_4_2 = findViewById(R.id.img_room_4_2);
        img_room_5_0 = findViewById(R.id.img_room_5_0);
        img_room_5_1 = findViewById(R.id.img_room_5_1);
        img_room_6_0 = findViewById(R.id.img_room_6_0);

        btn_send = findViewById(R.id.btn_send);
        btn_conn = findViewById(R.id.btn_conn);
        edt_msg = findViewById(R.id.edt_msg);
        txt_msg = findViewById(R.id.txt_msg);

    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

}
