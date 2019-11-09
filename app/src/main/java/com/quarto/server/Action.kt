package com.quarto.server

data class Action(var queryType: QueryType, var index: Int, var score: Int = 0)