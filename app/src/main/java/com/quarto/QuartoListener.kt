package com.quarto

interface QuartoListener {

    fun onDown(qid: Int): Boolean

    fun onMove(qid: Int): Boolean

    fun onDrop(qid: Int): Boolean

}
