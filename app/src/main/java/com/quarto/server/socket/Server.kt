package com.quarto.server.socket

import android.os.AsyncTask
import android.os.Handler

class Server {

    private var connect: Connect? = null
    private var autoConnectHandler: Handler? = null
    private var autoConnectRunnable: Runnable? = null
    private var host: String = "0.0.0.0"
    private var port: Int = 0
    private var delaySocketConnection: Int = 0
    private var timeout: Int = 0
    private var autoConnect: Boolean = false

    private var state = State.DISCONNECTED
    private var appOnStop = false

    val isConnected: Boolean get() = connect != null && connect!!.isConnected

    //----------------------------------------------------------------------------------------------

    private val intrf = object : Intrf {
        override fun onConnecting() {
            //MLog.log("Server - Intrf - onConnecting()");
            state = State.CONNECTING
            if (autoConnectHandler != null)
                autoConnectHandler!!.removeCallbacks(autoConnectRunnable)
            socketListener!!.onConnecting()
        }

        override fun onConnected() {
            //MLog.log("Server - Intrf - onConnected()");
            state = State.CONNECTED
            if (autoConnectHandler != null)
                autoConnectHandler!!.removeCallbacks(autoConnectRunnable)
            socketListener!!.onConnected()
        }

        override fun onDisconnected() {
            //MLog.log("Server - Intrf - onDisconnected()");
            state = State.DISCONNECTED
            socketListener!!.onDisconnected()
            if (autoConnectHandler != null) {
                autoConnectHandler!!.removeCallbacks(autoConnectRunnable)
                if (autoConnect)
                    autoConnectHandler!!.postDelayed(
                        autoConnectRunnable,
                        delaySocketConnection.toLong()
                    )
            }
        }

        override fun onMessageReceived(message: String) {
            //MLog.log("Server - Intrf - onMessageReceived() : "+message);
            //Log.d("YaMahdi-Server", "onMessageReceived(): "+message);
            socketListener!!.onMessageReceived(message)
        }

        override fun onDownloaded() {}
        override fun onDownloadFailed() {}
    }

    private var socketListener: SocketListener? = null

    fun createSocket(host: String, port: Int, timeOut: Int): Server {
        this.host = host
        this.port = port
        this.timeout = timeOut
        appOnStop = false
        return this
    }

    fun setListener(socketListener: SocketListener): Server {
        this.socketListener = socketListener
        return this
    }

    fun setAutoConnect(attemptToConnect: Int): Server {
        if (attemptToConnect > 0) {
            delaySocketConnection = attemptToConnect
            autoConnect = true
            autoConnectHandler = Handler()
            autoConnectRunnable = Runnable { connect() }
        } else {
            autoConnect = false
            if (autoConnectHandler != null)
                autoConnectHandler!!.removeCallbacks(autoConnectRunnable)
        }
        return this
    }

    fun connect() {
        //MLog.log("Server - connect()");
        if (state !== State.CONNECTED && state !== State.CONNECTING && !appOnStop) {
            //Log.d("YaMahdi-Server", "connect()");
            reconnect()
        }
    }

    private fun reconnect() {
        //MLog.log("Server - reconnect()");
        if (!appOnStop) {
            connect = Connect(host, port, timeout, intrf)
            connect!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            //AsyncTaskCompat.executeParallel(connect, null);
        }
    }

    fun sendMessage(message: String) {
        try {
            if (state === State.CONNECTED && !appOnStop) {
                if (connect != null)
                    connect!!.writeMessages(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun disconnect() {
        if (connect != null)
            connect!!.disconnect()
    }

    fun configHasChanged(
        host: String,
        port: Int,
        timeout: Int,
        delay_socket_connection: Int,
        reconnect: Boolean
    ) {
        this.delaySocketConnection = delay_socket_connection
        this.timeout = timeout
        if (this.host == null || this.port == 0 || this.host != host || this.port != port) {
            this.host = host
            this.port = port
            if (reconnect)
                disconnect()
        }
        if (reconnect)
            reconnect()
    }

    fun onDestroy() {
        try {
            disconnect()
            autoConnect = false
            if (autoConnectHandler != null)
                autoConnectHandler!!.removeCallbacks(autoConnectRunnable)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun enableAll() {
        connect()
    }

    fun disableAll() {
        disconnect()
        if (autoConnectHandler != null)
            autoConnectHandler!!.removeCallbacks(autoConnectRunnable)
    }

    fun disconnectAndReconnect() {
        Thread {
            disconnect()
            reconnect()
        }.start()
    }

    fun getListener(): SocketListener? {
        return socketListener
    }

}
