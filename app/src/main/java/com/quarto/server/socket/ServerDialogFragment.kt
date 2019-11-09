package com.quarto.server.socket

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.quarto.Config
import com.quarto.R
import kotlinx.android.synthetic.main.dialogfragment_server.*

class ServerDialogFragment : DialogFragment() {

    private var handler: Handler? = null
    var isAdding = false

    var showSettings = true

    private val runnable = Runnable {
        /*btn_dialogfragment_server_retry?.visibility = View.VISIBLE
        prb_dialogfragment_server_retry?.visibility = View.INVISIBLE*/
        if (context != null)
            Toast.makeText(context, "عملیات ناموفق", Toast.LENGTH_SHORT).show()
    }

    private var dialogListener: DialogListener? = null

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val ft = manager.beginTransaction()
            ft.add(this, tag)
            ft.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialogfragment_server, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_requesttaxi)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            dialog?.window?.setClipToOutline(true)

        /*btn_dialogfragment_server_retry?.visibility = View.VISIBLE
        prb_dialogfragment_server_retry?.visibility = View.INVISIBLE*/

        handler = Handler()

        /*btn_dialogfragment_server_retry?.setOnClickListener {
            prb_dialogfragment_server_retry?.visibility = View.VISIBLE
            btn_dialogfragment_server_retry?.visibility = View.INVISIBLE
            handler?.removeCallbacks(runnable)
            handler?.postDelayed(runnable, (4 * 1000).toLong())
            if (dialogListener != null)
                dialogListener?.onRetry()
        }*/
        /*btn_dialogfragment_server_retry?.setOnTouchListener(object : View.OnTouchListener {
            var time: Long = 0
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> time = System.currentTimeMillis()
                    MotionEvent.ACTION_UP -> if (BuildConfig.DEBUG && System.currentTimeMillis() - time > 1000)
                        showIPportSetting()
                    else if (System.currentTimeMillis() - time > 15 * 1000)
                        showIPportSetting()
                }
                return false
            }
        })*/


        if (showSettings)
            showIPportSetting()

    }

    override fun onStart() {
        super.onStart()
        try {
            Handler().postDelayed({ isAdding = false }, 1500)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        isAdding = false
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isAdding = false
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        isAdding = false
    }

    //**********************************************************************************************

    fun setDialogListener(dialogListener: DialogListener) {
        this.dialogListener = dialogListener
    }

    /*fun connected() {
        if (activity != null)
            activity?.runOnUiThread {
                handler?.removeCallbacks(runnable)
                prb_dialogfragment_server_retry?.animate()?.alpha(0f)?.setDuration(200)
                    ?.setListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {}
                        override fun onAnimationEnd(animation: Animator) {
                            try {
                                prb_dialogfragment_server_retry?.visibility = View.INVISIBLE
                                prb_dialogfragment_server_retry?.alpha = 1f
                                dismissAllowingStateLoss()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        override fun onAnimationCancel(animation: Animator) {}
                        override fun onAnimationRepeat(animation: Animator) {}
                    })?.start()
            }
    }*/

    private fun showIPportSetting() {

        rlv_dialogfragment_server?.visibility = View.VISIBLE

        edt_dialogfragment_server_ip?.setText(Config.getIP(context))
        edt_dialogfragment_server_port?.setText(Config.getPort(context).toString())

        btn_dialogfragment_server_ok?.setOnClickListener {
            Config.setIP(context, edt_dialogfragment_server_ip?.text?.toString())
            Config.setPort(context, Integer.parseInt(edt_dialogfragment_server_port?.text.toString()))
            activity?.finish()
        }
    }

    interface DialogListener {
        fun onRetry()
    }

}
