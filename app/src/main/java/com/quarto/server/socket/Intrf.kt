package com.quarto.server.socket

interface Intrf {

    fun onConnecting()

    fun onConnected()

    fun onDisconnected()

    fun onMessageReceived(message: String)

    fun onDownloaded()

    fun onDownloadFailed()

}
