package com.quarto

interface QuartoListener {

    fun onDown(qid: Int): Boolean

    fun onMove(qid: Int): Boolean

    fun onMoved(w: Int, h: Int, x: Float, y: Float)

    fun onDrop(qid: Int): Boolean

}
