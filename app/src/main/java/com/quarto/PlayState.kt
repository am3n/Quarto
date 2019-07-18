package com.quarto

enum class PlayState {

    PICK {
        override fun getString(): String {
            return "انتخاب مهره"
        }
    },
    MOVE {
        override fun getString(): String {
            return "حرکت مهره"
        }
    },
    QUARTO {
        override fun getString(): String {
            return "کوآرتووو!"
        }
    };

    abstract fun getString(): String

}