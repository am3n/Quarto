package com.quarto

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.quarto.bluetooth.ConnectFragment
import com.quarto.online.RandFrndDialog
import com.quarto.server.socket.ServerDialogFragment
import kotlinx.android.synthetic.main.activity_main2.*


// https://www.tutorialspoint.com/android/android_drag_and_drop.htm
// https://proandroiddev.com/home-automation-with-android-things-kotlin-2e0334101f08   server socket handler seems good

class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        AppUtils.hideNavAndStus(window)

        txtTitle?.setOnLongClickListener {
            ServerDialogFragment().show(supportFragmentManager, "ServerDialogFragment")
            return@setOnLongClickListener true
        }

        btnPlayComp?.setOnClickListener {
            supportFragmentManager.beginTransaction()
                    .add(R.id.activity_main, GameFrg(GameType.COMP, null), "GameFrg")
                    .addToBackStack("GameFrg")
                    .commit()
        }

        btnPlayLocal?.setOnClickListener {
            supportFragmentManager.beginTransaction()
                    .add(R.id.activity_main, GameFrg(GameType.FRND_LOCAL, null), "GameFrg")
                    .addToBackStack("GameFrg")
                    .commit()
        }

        btnPlayBt?.setOnClickListener {
            supportFragmentManager?.beginTransaction()
                    ?.add(R.id.activity_main, ConnectFragment { bt ->
                        Handler().postDelayed({
                            try {
                                supportFragmentManager?.popBackStack("ConnectFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                                supportFragmentManager.beginTransaction()
                                        .add(R.id.activity_main, GameFrg(GameType.FRND_BT, bt = bt), "GameFrg")
                                        .addToBackStack("GameFrg")
                                        .commit()
                            } catch (ignored: Exception) {}
                        }, 500)
                    }, "ConnectFragment")
                    ?.addToBackStack("ConnectFragment")
                    ?.commit()
        }

        btnPlayOnline?.setOnClickListener {
            RandFrndDialog {conn ->
                Handler().postDelayed({
                    try {
                        supportFragmentManager.beginTransaction()
                                .add(R.id.activity_main, GameFrg(GameType.ONLINE, conn = conn), "GameFrg")
                                .addToBackStack("GameFrg")
                                .commit()
                    } catch (ignored: Exception) {}
                }, 500)
            }.show(supportFragmentManager, "RandFrndDialog")
        }

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        AppUtils.hideNavAndStus(window)
    }

}
