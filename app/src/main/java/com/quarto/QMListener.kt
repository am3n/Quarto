package com.quarto

interface QMListener {

    fun onTurned(turn: Turn)

    fun onPlayStateChanged(playState: PlayState, roomId: Int = -1)

    fun onMoved(w: Int, h: Int, x: Float, y: Float)

}