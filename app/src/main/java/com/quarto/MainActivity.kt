package com.quarto

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import com.quarto.bluetooth.ConnectFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var quartoDrawables = intArrayOf(
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
            R.drawable.ic_square_white_small_hollow)
    var bt: BluetoothSPP? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        img_room_0_0.setImageResource(quartoDrawables[0])
        img_room_1_0.setImageResource(quartoDrawables[1])
        img_room_1_1.setImageResource(quartoDrawables[2])
        img_room_2_0.setImageResource(quartoDrawables[3])
        img_room_2_1.setImageResource(quartoDrawables[4])
        img_room_2_2.setImageResource(quartoDrawables[5])
        img_room_3_0.setImageResource(quartoDrawables[6])
        img_room_3_1.setImageResource(quartoDrawables[7])
        img_room_3_2.setImageResource(quartoDrawables[8])
        img_room_3_3.setImageResource(quartoDrawables[9])
        img_room_4_0.setImageResource(quartoDrawables[10])
        img_room_4_1.setImageResource(quartoDrawables[11])
        img_room_4_2.setImageResource(quartoDrawables[12])
        img_room_5_0.setImageResource(quartoDrawables[13])
        img_room_5_1.setImageResource(quartoDrawables[14])
        img_room_6_0.setImageResource(quartoDrawables[15])


        bt = BluetoothSPP(baseContext)
        if (bt?.isBluetoothAvailable==false) {
            Toast.makeText(applicationContext, "Bluetooth is not available", Toast.LENGTH_SHORT).show()
            finish()
        }


        btn_conn.setOnClickListener {
            val connectFragment = ConnectFragment {}
            connectFragment.bt = bt
            val fragmentManager0 = supportFragmentManager
            val fragmentTransaction0 = fragmentManager0.beginTransaction()
            fragmentTransaction0.add(R.id.activity_main, connectFragment)
            fragmentTransaction0.addToBackStack("connectFragment")
            fragmentTransaction0.commit()
        }

        btn_send.setOnClickListener { bt?.send(edt_msg.text.toString(), true) }

        bt?.setOnDataReceivedListener { _, message -> txt_msg.text = message }

    }

    public override fun onDestroy() {
        super.onDestroy()
        bt?.stopService()
    }

}
