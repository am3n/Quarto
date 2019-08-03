package com.quarto

import android.util.Log
import kotlin.random.Random

open class Controller(
        var rooms: Array<Array<Room?>>,
        var quartos: Array<Quarto?>,
        private val roomFraction: Float,
        private val listener: ControllerListener?
) {

    var userPicked = -1


    fun doWork() {
        if (userPicked==-1)
            pickRandomQuartoFromNotInTable()
        else
            movePickedQuarto()
    }


    private fun pickRandomQuartoFromNotInTable() {
        val quartosOutOfTable = quartos.filter { quarto -> quarto?.inTable==false }
        var rand = 0
        if (quartosOutOfTable.isNotEmpty())
            rand = Random.nextInt(quartosOutOfTable.size)
        userPicked = quartosOutOfTable[rand]?.id ?: -1
        quartos[userPicked]?.pick(true)
        quartos.forEach { quarto -> if (quarto?.id!=userPicked && quarto?.inTable==false) quarto.hide(true) }
    }


    private fun movePickedQuarto() {

        if (userPicked==-1)
            return

        val arr = findBestRoomForUserPickedQuarto(userPicked, 2, true)
        val bestRoomIndex = arr[0]
        val bestRoomScore = arr[1]
        val i = bestRoomIndex / 4
        val j = bestRoomIndex % 4

        quartos[userPicked]?.animateTo(rooms[i][j]?.location, roomFraction, roomFraction)
        quartos[userPicked]?.pick(false)
        quartos.forEach { quarto -> quarto?.hide(false) }
        quartos[userPicked]?.inTable = true
        rooms[i][j]?.qid = userPicked

        listener?.onMovedToRoom(userPicked)
        userPicked = -1

    }


    private fun findBestRoomForUserPickedQuarto(picked: Int, k: Int, favor: Boolean): IntArray {

        val minMaxScoresIndex: MutableList<Int> = ArrayList()
        var score: Int
        var max = Int.MIN_VALUE
        var min = Int.MAX_VALUE

        for (index in 0 until rooms.size*rooms.size) {
            val i = index / 4
            val j = index % 4
            if (rooms[i][j]?.empty == true) {
                rooms[i][j]?.qid = picked
                quartos[picked]?.inTable = true

                score = calcScoreOfMove(i, j) * k
                if (!favor) score *= -1

                if (k-1 > 0) {
                    val newPick = pickFirstQuartoFromNotInTable()
                    val k = k - 1
                    val arr = findBestRoomForUserPickedQuarto(newPick, k, !favor)
                    score += arr[1]
                }

                if (favor) {
                    if (score > max) {
                        max = score
                        minMaxScoresIndex.clear()
                        minMaxScoresIndex.add(index)
                    } else if (score == max) {
                        minMaxScoresIndex.add(index)
                    }
                } else {
                    if (score < min) {
                        min = score
                        minMaxScoresIndex.clear()
                        minMaxScoresIndex.add(index)
                    } else if (score == min) {
                        minMaxScoresIndex.add(index)
                    }
                }

                rooms[i][j]?.qid = -1
                quartos[picked]?.inTable = false
            }
        }

        return if (minMaxScoresIndex.size>0) {
            var str = ""
            minMaxScoresIndex.forEach { str += "$it, " }
            if (favor) {
                Log.d("Me-Controller", "k: $k  -  max: $max  -  indexes: $str")
                intArrayOf(minMaxScoresIndex[Random.nextInt(minMaxScoresIndex.size)], max)
            } else {
                Log.d("Me-Controller", "k: $k  -  min: $min  -  indexes: $str")
                intArrayOf(minMaxScoresIndex[Random.nextInt(minMaxScoresIndex.size)], min)
            }
        } else {
            if (favor)
                intArrayOf(-1, max)
            else
                intArrayOf(-1, min)
        }

    }


    private fun pickFirstQuartoFromNotInTable(): Int {
        val quartosOutOfTable = quartos.filter { quarto -> quarto?.inTable==false }
        if (quartosOutOfTable.isNotEmpty())
            return quartosOutOfTable[0]?.id ?: -1
        return -1
    }


    private fun calcScoreOfMove(iQ: Int, jQ: Int): Int {

        var sizeInRow: QSize? = null; var shapeInRow: QShape? = null; var insideInRow: QInside? = null; var colorInRow: QColor? = null
        var onSizeInRow = true; var onShapeInRow = true; var onInsideInRow = true; var onColorInRow = true

        var sizeInColumn: QSize? = null; var shapeInColumn: QShape? = null; var insideInColumn: QInside? = null; var colorInColumn: QColor? = null
        var onSizeInColumn = true; var onShapeInColumn = true; var onInsideInColumn = true; var onColorInColumn = true

        var sizeInMD: QSize? = null; var shapeInMD: QShape? = null; var insideInMD: QInside? = null; var colorInMD: QColor? = null
        var onSizeInMD = true; var onShapeInMD = true; var onInsideInMD = true; var onColorInMD = true

        var sizeInSD: QSize? = null; var shapeInSD: QShape? = null; var insideInSD: QInside? = null; var colorInSD: QColor? = null
        var onSizeInSD = true; var onShapeInSD = true; var onInsideInSD = true; var onColorInSD = true

        for (i in 0 until 4) {

            // row
            rooms[iQ][i]?.let { room ->
                if (!room.empty) {
                    if (sizeInRow == null || shapeInRow == null || insideInRow == null || colorInRow == null) {
                        sizeInRow = quartos[room.qid]?.qSize
                        shapeInRow = quartos[room.qid]?.qShape
                        insideInRow = quartos[room.qid]?.qInside
                        colorInRow = quartos[room.qid]?.qColor
                    }
                    if (quartos[room.qid]?.qSize != sizeInRow)
                        onSizeInRow = false
                    if (quartos[room.qid]?.qShape != shapeInRow)
                        onShapeInRow = false
                    if (quartos[room.qid]?.qInside != insideInRow)
                        onInsideInRow = false
                    if (quartos[room.qid]?.qColor != colorInRow)
                        onColorInRow = false
                } else {
                    onSizeInRow = false
                    onShapeInRow = false
                    onInsideInRow = false
                    onColorInRow = false
                }
            }


            // column
            rooms[i][jQ]?.let { room ->
                if (!room.empty) {
                    if (sizeInColumn == null || shapeInColumn == null || insideInColumn == null || colorInColumn == null) {
                        sizeInColumn = quartos[room.qid]?.qSize
                        shapeInColumn = quartos[room.qid]?.qShape
                        insideInColumn = quartos[room.qid]?.qInside
                        colorInColumn = quartos[room.qid]?.qColor
                    }
                    if (quartos[room.qid]?.qSize != sizeInColumn)
                        onSizeInColumn = false
                    if (quartos[room.qid]?.qShape != shapeInColumn)
                        onShapeInColumn = false
                    if (quartos[room.qid]?.qInside != insideInColumn)
                        onInsideInColumn = false
                    if (quartos[room.qid]?.qColor != colorInColumn)
                        onColorInColumn = false
                } else {
                    onSizeInColumn = false
                    onShapeInColumn = false
                    onInsideInColumn = false
                    onColorInColumn = false
                }
            }


            // main diameter
            if (iQ == jQ) {
                rooms[i][i]?.let { room ->
                    if (!room.empty) {
                        if (sizeInMD == null || shapeInMD == null || insideInMD == null || colorInMD == null) {
                            sizeInMD = quartos[room.qid]?.qSize
                            shapeInMD = quartos[room.qid]?.qShape
                            insideInMD = quartos[room.qid]?.qInside
                            colorInMD = quartos[room.qid]?.qColor
                        }
                        if (quartos[room.qid]?.qSize != sizeInMD)
                            onSizeInMD = false
                        if (quartos[room.qid]?.qShape != shapeInMD)
                            onShapeInMD = false
                        if (quartos[room.qid]?.qInside != insideInMD)
                            onInsideInMD = false
                        if (quartos[room.qid]?.qColor != colorInMD)
                            onColorInMD = false
                    } else {
                        onSizeInMD = false
                        onShapeInMD = false
                        onInsideInMD = false
                        onColorInMD = false
                    }
                }
            } else {
                onSizeInMD = false
                onShapeInMD = false
                onInsideInMD = false
                onColorInMD = false
            }


            // second diameter
            if (iQ == 3-jQ) {
                rooms[i][3-i]?.let { room ->
                    if (!room.empty) {
                        if (sizeInSD == null || shapeInSD == null || insideInSD == null || colorInSD == null) {
                            sizeInSD = quartos[room.qid]?.qSize
                            shapeInSD = quartos[room.qid]?.qShape
                            insideInSD = quartos[room.qid]?.qInside
                            colorInSD = quartos[room.qid]?.qColor
                        }
                        if (quartos[room.qid]?.qSize != sizeInSD)
                            onSizeInSD = false
                        if (quartos[room.qid]?.qShape != shapeInSD)
                            onShapeInSD = false
                        if (quartos[room.qid]?.qInside != insideInSD)
                            onInsideInSD = false
                        if (quartos[room.qid]?.qColor != colorInSD)
                            onColorInSD = false
                    } else {
                        onSizeInSD = false
                        onShapeInSD = false
                        onInsideInSD = false
                        onColorInSD = false
                    }
                }
            } else {
                onSizeInSD = false
                onShapeInSD = false
                onInsideInSD = false
                onColorInSD = false
            }

        }

        val quartoInRow = onSizeInRow || onShapeInRow || onInsideInRow || onColorInRow
        val quartoInColumn = onSizeInColumn || onShapeInColumn || onInsideInColumn || onColorInColumn
        val quartoInMD = onSizeInMD || onShapeInMD || onInsideInMD || onColorInMD
        val quartoInSD = onSizeInSD || onShapeInSD || onInsideInSD || onColorInSD

        var score = 0
        if (quartoInRow || quartoInColumn || quartoInMD || quartoInSD)
            score++

        //score = 1

        return score
    }



    private fun findBestQuartoToPickForUser() {



    }


}
