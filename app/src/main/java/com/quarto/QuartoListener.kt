package com.quarto

import android.view.View

interface QuartoListener {

    fun onDown(qid: Int): Boolean

    fun onMove(qid: Int): Boolean

    fun onDrop(qid: Int): Boolean

}
