package com.quarto

enum class PlayState {

    PICK {
        override fun getString(): String {
            return "pick a quarto"
        }
    },
    MOVE {
        override fun getString(): String {
            return "move picked quarto"
        }
    },
    QUARTO {
        override fun getString(): String {
            return "Quartooo!!!"
        }
    };

    abstract fun getString(): String

}