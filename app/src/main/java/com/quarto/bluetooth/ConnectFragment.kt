package com.quarto.bluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import com.quarto.R
import kotlinx.android.synthetic.main.fragment_connect.*

@SuppressLint("SetTextI18n")
class ConnectFragment(val onConnect: (BluetoothSPP?) -> Unit) : Fragment() {

    var bt: BluetoothSPP? = null
    private var scaleAnimation: ScaleAnimation? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_connect, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bt = BluetoothSPP(context)

        init()


        if (bt?.isBluetoothEnabled==true) {
            if (bt?.isServiceAvailable==false) {
                bt?.setupService()
                bt?.startService(BluetoothState.DEVICE_ANDROID)
            }
        } else {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT)
        }

        ensureDiscoverable()

        bt?.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceConnected(name: String, address: String) {
                txt_wait?.text = "$name Connected!"
                prb_connecting?.animate()?.alpha(0f)?.setDuration(200)?.start()
                onConnect(bt)
            }
            override fun onDeviceDisconnected() {
                txt_wait?.text = "Disconnected"
                prb_connecting?.animate()?.alpha(0f)?.setDuration(200)?.start()
                txt_disconn?.visibility = View.GONE
            }
            override fun onDeviceConnectionFailed() {
                txt_wait?.text = "Unable to connect"
                prb_connecting?.animate()?.alpha(0f)?.setDuration(200)?.start()
            }
        })



        imgb_turnon?.setOnClickListener {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT)
        }


        imgb_conn?.setOnClickListener {
            if (bt?.serviceState != BluetoothState.STATE_CONNECTED) {
                if (bt?.isServiceAvailable==true) {
                    val serverIntent = Intent(activity, DeviceListActivity::class.java)
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE)
                } else {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT)
                }
            }
        }
        imgb_conn?.setOnLongClickListener {
            if (bt?.serviceState == BluetoothState.STATE_CONNECTED) {
                bt?.disconnect()
                txt_disconn?.visibility = View.GONE
            }
            true
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {

                txt_wait?.animate()?.alpha(0f)?.setDuration(500)?.start()
                prb_connecting?.animate()?.alpha(1f)?.setDuration(500)?.start()
                Handler().postDelayed({
                    txt_wait?.text = "Connecting..."
                    txt_wait?.animate()?.alpha(1f)?.setDuration(500)?.start()
                }, 500)
                Handler().postDelayed({
                    bt?.connect(data)
                }, 500)

            }

        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {

                bt?.setupService()
                bt?.startService(BluetoothState.DEVICE_ANDROID)

                txt_turnon?.animate()?.alpha(0f)?.setDuration(500)?.start()
                imgb_turnon?.animate()?.alpha(0f)?.setDuration(500)?.start()
                txt_wait?.animate()?.alpha(1f)?.setDuration(500)?.start()
                imgb_conn?.animate()?.alpha(1f)?.setDuration(500)?.start()

            } else {
                Toast.makeText(activity, "Bluetooth was not enabled.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //**********************************************************************************************

    private fun init() {

        txt_this?.text = bt?.bluetoothAdapter?.name

        if (bt?.isBluetoothEnabled==true) {
            txt_turnon?.animate()?.alpha(0f)?.setDuration(500)?.start()
            imgb_turnon?.animate()?.alpha(0f)?.setDuration(500)?.start()
            txt_wait?.animate()?.alpha(1f)?.setDuration(500)?.start()
            imgb_conn?.animate()?.alpha(1f)?.setDuration(500)?.start()
        }

        if (bt?.serviceState == BluetoothState.STATE_CONNECTED) {
            txt_wait?.text = "Your're already connected to\n${bt?.connectedDeviceName}!"
            animationOnConnected(imgb_conn)
            txt_disconn?.visibility = View.VISIBLE
        }

    }

    private fun ensureDiscoverable() {

        if (bt?.bluetoothAdapter?.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            startActivity(discoverableIntent)
        }
    }

    private fun animationOnConnected(view: View?) {
        try {
            activity?.runOnUiThread {
                scaleAnimation = ScaleAnimation(
                        1f, 1.1f,
                        1f, 1.1f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f)
                scaleAnimation?.duration = 2000
                scaleAnimation?.repeatCount = 1
                scaleAnimation?.repeatMode = Animation.REVERSE
                scaleAnimation?.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        if (bt?.serviceState == BluetoothState.STATE_CONNECTED)
                            view?.startAnimation(scaleAnimation)
                    }
                    override fun onAnimationRepeat(animation: Animation) {}
                })
                view?.startAnimation(scaleAnimation)
            }
        } catch (ignored: Exception) {}
    }

    companion object {
        const val REQUEST_CONNECT_DEVICE_SECURE = 384
    }

}
