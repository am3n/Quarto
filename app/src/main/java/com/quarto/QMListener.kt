package com.quarto

interface QMListener {

    fun onTurned(turn: Turn)

    fun onPlayStateChanged(playState: PlayState)

}