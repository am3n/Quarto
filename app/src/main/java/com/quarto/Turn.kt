package com.quarto

enum class Turn {

    PLAYER1 {
        override fun getString(): String {
            return "نوبت بازیکن ۱"
        }
    },
    PLAYER2 {
        override fun getString(): String {
            return "نوبت بازیکن ۲"
        }
    };

    abstract fun getString(): String

    fun switch(): Turn {
        return if (this == PLAYER1)
            PLAYER2
        else
            PLAYER1
    }

}