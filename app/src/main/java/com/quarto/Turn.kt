package com.quarto

enum class Turn {

    PLAYER1 {
        override fun getString(gameType: GameType): String {
            return when (gameType) {
                GameType.COMP -> "YOU"
                GameType.FRND_LOCAL -> "Player 1"
                GameType.FRND_BT -> "You"
                GameType.ONLINE -> "You"
            }
        }
    },
    PLAYER2 {
        override fun getString(gameType: GameType): String {
            return when (gameType) {
                GameType.COMP -> "COMPUTER"
                GameType.FRND_LOCAL -> "Player 2"
                GameType.FRND_BT -> "Your Friend"
                GameType.ONLINE -> "Friend"
            }
        }
    };

    abstract fun getString(gameType: GameType): String

    fun switch(): Turn {
        return if (this == PLAYER1) PLAYER2 else PLAYER1
    }

}