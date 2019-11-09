package com.quarto.online

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.quarto.Config
import com.quarto.Config.timeout
import com.quarto.R
import com.quarto.server.socket.Connect
import com.quarto.server.socket.Intrf
import kotlinx.android.synthetic.main.dlg_randfrnd.*

class RandFrndDialog(val onConnect: (Connect?) -> Unit) : DialogFragment() {

    private var connect: Connect? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(R.layout.dlg_randfrnd, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        connect = Connect(Config.getIP(context), Config.getPort(context), timeout, object : Intrf {
            override fun onConnecting() {}
            override fun onConnected() {
                txt_wait?.text = "find a player ..."
                connect?.writeMessages("find")
            }
            override fun onDisconnected() {
                dismiss()
            }
            override fun onMessageReceived(message: String) {
                if (message == "found") {
                    Looper.getMainLooper()?.let {
                        Handler(it).post {
                            txt_wait?.text = "founded, wait ..."
                        }
                        Handler(it).postDelayed({
                            try {
                                dismiss()
                                onConnect(connect)
                            } catch (e: Exception) { e.printStackTrace() }
                        }, 1000)
                    }
                } else if (message == "not_found") {
                    Looper.getMainLooper()?.let {
                        Handler(it).post {
                            txt_wait?.text = "not found"
                        }
                        Handler(it).postDelayed({
                            try {
                                dismiss()
                            } catch (e: Exception) { e.printStackTrace() }
                        }, 1000)
                    }
                }
            }
            override fun onDownloaded() {}
            override fun onDownloadFailed() {}
        })
        connect?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


    }


}