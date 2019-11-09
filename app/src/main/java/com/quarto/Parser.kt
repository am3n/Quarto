package com.quarto

import app.akexorcist.bluetotohspp.library.BluetoothSPP
import com.google.gson.Gson
import com.quarto.server.socket.Connect
import java.io.Serializable
import kotlin.random.Random


class Parser(
        private val listener: Listener,
        private val bt: BluetoothSPP? = null,
        private val conn: Connect? = null
) {

    private val gson = Gson()
    private val me = HashMap<String, Serializable>()

    fun parse(message: String) {

        val any = gson.fromJson(message, Base::class.java)
        when (any.type) {
            Type.UNKNOWN -> {}
            Type.QUERY -> {}
            Type.SHAKE -> {
                val frnd = gson.fromJson(message, Shake::class.java)
                val me = me["shake"] as Shake
                when {
                    me.value > frnd.value -> listener.onMyTurn(PlayState.PICK, -1)
                    me.value < frnd.value -> listener.onFrndTurn(PlayState.PICK)
                    else -> shake()
                }
            }
            Type.TURN_CHANGED -> {
                val frnd = gson.fromJson(message, TurnChanged::class.java)
                listener.onMyTurn(PlayState.MOVE, frnd.picked)
            }
            Type.PLAY_STATE_CHANGED -> {
                val frnd = gson.fromJson(message, PlayStateChanged::class.java)
                listener.onFrndPlayState(PlayState.PICK, frnd.roomId)
            }
            Type.LOCATION_CHANGED -> {
                val frnd = gson.fromJson(message, LocationChanged::class.java)
                listener.onFrndLocation(PlayState.MOVE, frnd)
            }
        }

    }

    fun shake() {
        me["shake"] = Shake(Random.nextInt(0, 1000))
        send(gson.toJson(me["shake"]))
    }

    fun changeTurn(picked: Int?) {
        val turnChanged = TurnChanged(picked?:-1)
        send(gson.toJson(turnChanged))
    }

    fun changePlayState(roomId: Int?) {
        val turnChanged = PlayStateChanged(roomId?:-1)
        send(gson.toJson(turnChanged))
    }

    fun changeLocation(w: Int, h: Int, x: Float, y: Float) {
        val locationChanged = LocationChanged(w, h, x, y)
        send(gson.toJson(locationChanged))
    }


    private fun send(message: String) {
        bt?.send(message, true)
        conn?.writeMessages(message)
    }


    data class Shake(
            val value: Int
    ): Base(Type.SHAKE)

    data class TurnChanged(
            val picked: Int
    ): Base(Type.TURN_CHANGED)

    data class PlayStateChanged(
            val roomId: Int
    ): Base(Type.PLAY_STATE_CHANGED)

    data class LocationChanged(
            val w: Int,
            val h: Int,
            val x: Float,
            val y: Float
    ): Base(Type.LOCATION_CHANGED)

    //---------------------------------------------------------------

    interface Listener {
        fun onMyTurn(playState: PlayState, picked: Int)
        fun onFrndTurn(playState: PlayState)
        fun onFrndPlayState(playState: PlayState, roomId: Int)
        fun onFrndLocation(playState: PlayState, locationChanged: LocationChanged)
    }

}