package com.quarto.server.socket

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.Charset
import java.util.*

class Connect(
    private val host: String,
    private val port: Int,
    private val timeout: Int,
    private var intrf: Intrf
) : AsyncTask<Void, Void, Void>() {

    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null
    private val messageList = ArrayList<String>()

    val isConnected: Boolean get() = socket != null && socket!!.isConnected

    override fun doInBackground(vararg voids: Void): Void? {
        try {

            disconnect()
            messageList.clear()
            intrf.onConnecting()
            //Log.d("YaMahdi-Server", "doInBackground() > onConnecting");
            val socketAddress = InetSocketAddress(host, port)
            socket = Socket()
            socket!!.keepAlive = true
            socket!!.connect(socketAddress, timeout)
            if (socket!!.isConnected) {
                //Log.d("YaMahdi-Server", "doInBackground() > onConnected");
                writer = PrintWriter(OutputStreamWriter(socket!!.getOutputStream()), true)
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream(), Charset.forName("UTF-8")))
                readMessages()
                intrf.onConnected()
            } else {
                //Log.d("YaMahdi-Server", "doInBackground() > onDisconnected");
                intrf.onDisconnected()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //Log.d("YaMahdi-Server", "doInBackground() > exception > onDisconnected");
            intrf.onDisconnected()
        }

        return null
    }

    private fun readMessages() {
        Thread {
            var message: String
            try {
                while (socket!!.isConnected) {
                    message = reader!!.readLine()
                    intrf.onMessageReceived(message)
                }
            } catch (e: Exception) {
                //Log.d("YaMahdi-Server", "readMessages() >  exception");
                e.printStackTrace()
                //Log.e("YaMahdi", "read() > error: "+e.getMessage());
            }

            //Log.d("YaMahdi-Server", "readMessages() >  onDisconnected");
            intrf.onDisconnected()
        }.start()
    }

    fun writeMessages(newMessage: String) {

        messageList.add(newMessage)

        if (isConnected) {
            Thread {
                while (messageList.size > 0) {
                    try {
                        val message = messageList[0]
                        messageList.removeAt(0)
                        writer!!.println(message)
                        Log.d("Me-Server", "writeMessages() : $message")
                        //MLog.log("Server - Connect - writeMessages() : "+enc);
                        //Log.d("SocketAsyncTask0000", enc);
                        writer!!.flush()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("Me-Server", "writeMessages() > error: " + e.message)
                    }

                }
            }.start()
        }
    }

    fun disconnect() {
        try {
            if (socket != null)
                socket!!.close()
            if (writer != null)
                writer!!.close()
            if (reader != null)
                reader!!.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun listen(intrf: Intrf) {
        this.intrf = intrf
    }

}