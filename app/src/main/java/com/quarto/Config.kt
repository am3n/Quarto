package com.quarto

import android.content.Context
import android.content.SharedPreferences

object Config {

    var timeout = 5 * 1000
    var delay_socket_connection = 5 * 1000

    private fun getSh(context: Context?): SharedPreferences? {
        return context?.getSharedPreferences("setting", Context.MODE_PRIVATE)
    }

    fun getIP(context: Context?): String {
        return context?.let { getSh(it)?.getString("ip", "192.168.137.1") }.toString()
    }
    fun setIP(context: Context?, ip: String?) {
        getSh(context)?.edit()?.putString("ip", ip)?.apply()
    }

    fun getPort(context: Context?): Int {
        getSh(context)?.getInt("port", 8080)?.let { int -> return int }
        return 0
    }
    fun setPort(context: Context?, port: Int) {
        getSh(context)?.edit()?.putInt("port", port)?.apply()
    }

}
