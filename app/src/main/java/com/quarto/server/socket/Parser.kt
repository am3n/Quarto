package com.quarto.server.socket

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.quarto.server.Action
import com.quarto.server.Query

class Parser(private val server: Server?) {

    private val gson: Gson? = GsonBuilder().create()

    fun parse(message: String): Action? {
        try {
            return gson?.fromJson(message, Action::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getAction(query: Query) {
        gson?.toJson(query)?.trim()?.let {
            server?.sendMessage(it)
        }
    }

}
