package com.quarto

import android.util.Log

open class Controller(
        private var rooms: Array<Array<Room?>>,
        private var quartos: Array<Quarto?>,
        private val listener: ControllerListener?
) {

    private val log = true
    private val moveK = 3
    private val pickK = 3
    var moveCount = 0
    var pickCount = 0

    internal fun moveQuarto(picked: Int) {

        moveCount = 0
        val arr = findBestRoomForPickedQuarto(picked, moveK, true)
        val bestRoomIndex = arr[0]
        val bestRoomScore = arr[1]
        Log.d("Me-Controller", "move count: $moveCount")
        listener?.move(bestRoomIndex)

    }
    private fun findBestRoomForPickedQuarto(picked: Int, k: Int, favor: Boolean): IntArray {

        val minMaxScoresIndex: MutableList<Int> = ArrayList()
        var score: Int
        var max = Int.MIN_VALUE
        var min = Int.MAX_VALUE

        for (index in 0 until rooms.size*rooms.size) {
            moveCount++
            val i = index/4; val j = index%4
            if (rooms[i][j]?.empty == true) {
                rooms[i][j]?.qid = picked
                quartos[picked]?.inTable = true

                score = calcScoreOfMove(i, j) * k
                if (!favor) score *= -1

                if (k-1 > 0) {
                    var secondScore: Int
                    var secondMax = Int.MIN_VALUE
                    var secondMin = Int.MAX_VALUE
                    quartos.filter { it?.inTable==false }.forEach { q ->
                        moveCount++
                        val newPick = q?.id ?: -1
                        val k = k - 1
                        val arr = findBestRoomForPickedQuarto(newPick, k, !favor)
                        secondScore = arr[1]
                        if (favor && secondScore > secondMax) {
                            secondMax = secondScore
                        } else if (!favor && secondScore < secondMin) {
                            secondMin = secondScore
                        }
                    }
                    score += if (favor) secondMax else secondMin
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
                if (log) Log.d("Me-Controller", "k: ${moveK - k}  -  max: $max  -  indexes: $str")
                intArrayOf(minMaxScoresIndex.random(), max)
            } else {
                if (log) Log.d("Me-Controller", "k: ${moveK - k}  -  min: $min  -  indexes: $str")
                intArrayOf(minMaxScoresIndex.random(), min)
            }
        } else {
            if (favor)
                intArrayOf(-1, max)
            else
                intArrayOf(-1, min)
        }

    }

    internal fun pickQuarto() {

        pickCount = 0
        val arr = findBestQuartoToPickForUser(pickK, false)
        val bestQuartoId = arr[0]
        val bestQuartoScore = arr[1]
        if (log) println("Me-Controller - pick count: $pickCount")
        listener?.pick(bestQuartoId)

    }
    private fun findBestQuartoToPickForUser(k: Int, favor: Boolean): IntArray {

        val minMaxScores: MutableList<Int> = ArrayList()
        var max = Int.MIN_VALUE
        var min = Int.MAX_VALUE

        quartos.filter { it?.inTable == false }.forEach { it?.let { q ->

            var score = 0

            //for (index in 0 until rooms.size*rooms.size) { }
            emptyEffectiveRooms(rooms).forEach { r ->
                pickCount++

                val i = r.id / 4 ; val j = r.id % 4
                rooms[i][j]?.qid = q.id
                quartos[q.id]?.inTable = true

                var s = calcScoreOfMove(i, j) * k
                if (!favor) s *= -1

                score += s

                if (k-1>0 && s==0) {
                    val arr = findBestQuartoToPickForUser(k-1, !favor)
                    score += arr[1]

                } else if (k-1>0 && s!=0) {
                    var ee = emptyEffectiveRooms(rooms).size
                    var kk = k
                    var ss = 0
                    while (kk>0) {
                        ss += ee
                        ee--
                        kk--
                    }
                    if (!favor) ss *= -1
                    score += ss
                }

                rooms[i][j]?.qid = -1
                quartos[q.id]?.inTable = false

            }

            if (favor) {
                if (score < min) {
                    min = score
                    minMaxScores.clear()
                    minMaxScores.add(q.id)
                } else if (score == min) {
                    minMaxScores.add(q.id)
                }
            } else {
                if (score > max) {
                    max = score
                    minMaxScores.clear()
                    minMaxScores.add(q.id)
                } else if (score == max) {
                    minMaxScores.add(q.id)
                }
            }

            return@let

        } }

        return if (minMaxScores.size>0) {
            var str = ""
            minMaxScores.forEach { str += "$it, " }
            if (favor) {
                if (log) println("Me-Controller - k: ${pickK - k}  -  min: $min  -  quartos: $str")
                intArrayOf(minMaxScores.random(), min)
            } else {
                if (log) println("Me-Controller - k: ${pickK - k}  -  max: $max  -  quartos: $str")
                intArrayOf(minMaxScores.random(), max)
            }
        } else {
            if (favor) {
                if (log) println("Me-Controller - k: $k  -  min: $min  -  indexes: -1")
                intArrayOf(-1, min)
            } else {
                if (log) println("Me-Controller - k: $k  -  max: $max  -  indexes: -1")
                intArrayOf(-1, max)
            }
        }
    }
    private fun emptyEffectiveRooms(rooms: Array<Array<Room?>>): ArrayList<Room> {

        val arr = ArrayList<Room>()

        var row = 0
        var col = 0

        col = 0
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][1]?.empty!=true || rooms[row][2]?.empty!=true || rooms[row][3]?.empty!=true ||
                             rooms[1][col]?.empty!=true || rooms[2][col]?.empty!=true || rooms[3][col]?.empty!=true ||
                             rooms[1][1]?.empty!=true || rooms[2][2]?.empty!=true || rooms[3][3]?.empty!=true)) {
                arr.add(r)
            }
        }

        col = 1
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][0]?.empty!=true || rooms[row][2]?.empty!=true || rooms[row][3]?.empty!=true ||
                            rooms[1][col]?.empty!=true || rooms[2][col]?.empty!=true || rooms[3][col]?.empty!=true /*||
                            rooms[1][1]?.empty!=true || rooms[2][2]?.empty!=true || rooms[3][3]?.empty!=true*/)) {
                arr.add(r)
            }
        }

        col = 2
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][0]?.empty!=true || rooms[row][1]?.empty!=true || rooms[row][3]?.empty!=true ||
                            rooms[1][col]?.empty!=true || rooms[2][col]?.empty!=true || rooms[3][col]?.empty!=true /*||
                            rooms[1][1]?.empty!=true || rooms[2][2]?.empty!=true || rooms[3][3]?.empty!=true*/)) {
                arr.add(r)
            }
        }

        col = 3
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][0]?.empty!=true || rooms[row][1]?.empty!=true || rooms[row][2]?.empty!=true ||
                            rooms[1][col]?.empty!=true || rooms[2][col]?.empty!=true || rooms[3][col]?.empty!=true ||
                            rooms[3][0]?.empty!=true || rooms[2][1]?.empty!=true || rooms[1][2]?.empty!=true)) {
                arr.add(r)
            }
        }


        // -------------------------------
        row = 1


        col = 0
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][1]?.empty!=true || rooms[row][2]?.empty!=true || rooms[row][3]?.empty!=true ||
                            rooms[0][col]?.empty!=true || rooms[2][col]?.empty!=true || rooms[3][col]?.empty!=true /*||
                            rooms[1][1]?.empty!=true || rooms[2][2]?.empty!=true || rooms[3][3]?.empty!=true*/)) {
                arr.add(r)
            }
        }

        col = 1
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][0]?.empty!=true || rooms[row][2]?.empty!=true || rooms[row][3]?.empty!=true ||
                            rooms[0][col]?.empty!=true || rooms[2][col]?.empty!=true || rooms[3][col]?.empty!=true ||
                            rooms[0][0]?.empty!=true || rooms[2][2]?.empty!=true || rooms[3][3]?.empty!=true)) {
                arr.add(r)
            }
        }

        col = 2
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][0]?.empty!=true || rooms[row][1]?.empty!=true || rooms[row][3]?.empty!=true ||
                            rooms[0][col]?.empty!=true || rooms[2][col]?.empty!=true || rooms[3][col]?.empty!=true ||
                            rooms[3][0]?.empty!=true || rooms[2][1]?.empty!=true || rooms[0][3]?.empty!=true)) {
                arr.add(r)
            }
        }

        col = 3
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][0]?.empty!=true || rooms[row][1]?.empty!=true || rooms[row][2]?.empty!=true ||
                            rooms[0][col]?.empty!=true || rooms[2][col]?.empty!=true || rooms[3][col]?.empty!=true /*||
                            rooms[3][0]?.empty!=true || rooms[2][1]?.empty!=true || rooms[1][2]?.empty!=true*/)) {
                arr.add(r)
            }
        }


        // --------------------------------
        row = 2

        col = 0
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][1]?.empty!=true || rooms[row][2]?.empty!=true || rooms[row][3]?.empty!=true ||
                            rooms[0][col]?.empty!=true || rooms[1][col]?.empty!=true || rooms[3][col]?.empty!=true /*||
                            rooms[1][1]?.empty!=true || rooms[2][2]?.empty!=true || rooms[3][3]?.empty!=true*/)) {
                arr.add(r)
            }
        }

        col = 1
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][0]?.empty!=true || rooms[row][2]?.empty!=true || rooms[row][3]?.empty!=true ||
                            rooms[0][col]?.empty!=true || rooms[1][col]?.empty!=true || rooms[3][col]?.empty!=true ||
                            rooms[3][0]?.empty!=true || rooms[1][2]?.empty!=true || rooms[0][3]?.empty!=true)) {
                arr.add(r)
            }
        }

        col = 2
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][0]?.empty!=true || rooms[row][1]?.empty!=true || rooms[row][3]?.empty!=true ||
                            rooms[0][col]?.empty!=true || rooms[1][col]?.empty!=true || rooms[3][col]?.empty!=true ||
                            rooms[0][0]?.empty!=true || rooms[1][1]?.empty!=true || rooms[3][3]?.empty!=true)) {
                arr.add(r)
            }
        }

        col = 3
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][0]?.empty!=true || rooms[row][1]?.empty!=true || rooms[row][2]?.empty!=true ||
                            rooms[0][col]?.empty!=true || rooms[1][col]?.empty!=true || rooms[3][col]?.empty!=true /*||
                            rooms[3][0]?.empty!=true || rooms[2][1]?.empty!=true || rooms[1][2]?.empty!=true*/)) {
                arr.add(r)
            }
        }


        // -------------------------------
        row = 3

        col = 0
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][1]?.empty!=true || rooms[row][2]?.empty!=true || rooms[row][3]?.empty!=true ||
                            rooms[0][col]?.empty!=true || rooms[1][col]?.empty!=true || rooms[2][col]?.empty!=true ||
                            rooms[2][1]?.empty!=true || rooms[1][2]?.empty!=true || rooms[0][3]?.empty!=true)) {
                arr.add(r)
            }
        }

        col = 1
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][0]?.empty!=true || rooms[row][2]?.empty!=true || rooms[row][3]?.empty!=true ||
                            rooms[0][col]?.empty!=true || rooms[1][col]?.empty!=true || rooms[2][col]?.empty!=true /*||
                            rooms[1][1]?.empty!=true || rooms[2][2]?.empty!=true || rooms[3][3]?.empty!=true*/)) {
                arr.add(r)
            }
        }

        col = 2
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][0]?.empty!=true || rooms[row][1]?.empty!=true || rooms[row][3]?.empty!=true ||
                            rooms[0][col]?.empty!=true || rooms[1][col]?.empty!=true || rooms[2][col]?.empty!=true /*||
                            rooms[1][1]?.empty!=true || rooms[2][2]?.empty!=true || rooms[3][3]?.empty!=true*/)) {
                arr.add(r)
            }
        }

        col = 3
        rooms[row][col]?.let { r ->
            if (r.empty && (rooms[row][0]?.empty!=true || rooms[row][1]?.empty!=true || rooms[row][2]?.empty!=true ||
                            rooms[0][col]?.empty!=true || rooms[1][col]?.empty!=true || rooms[2][col]?.empty!=true ||
                            rooms[0][0]?.empty!=true || rooms[1][1]?.empty!=true || rooms[2][2]?.empty!=true)) {
                arr.add(r)
            }
        }


        return arr
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
                        sizeInRow = quartos[room.qid]?.size
                        shapeInRow = quartos[room.qid]?.shape
                        insideInRow = quartos[room.qid]?.inside
                        colorInRow = quartos[room.qid]?.color
                    }
                    if (quartos[room.qid]?.size != sizeInRow)
                        onSizeInRow = false
                    if (quartos[room.qid]?.shape != shapeInRow)
                        onShapeInRow = false
                    if (quartos[room.qid]?.inside != insideInRow)
                        onInsideInRow = false
                    if (quartos[room.qid]?.color != colorInRow)
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
                        sizeInColumn = quartos[room.qid]?.size
                        shapeInColumn = quartos[room.qid]?.shape
                        insideInColumn = quartos[room.qid]?.inside
                        colorInColumn = quartos[room.qid]?.color
                    }
                    if (quartos[room.qid]?.size != sizeInColumn)
                        onSizeInColumn = false
                    if (quartos[room.qid]?.shape != shapeInColumn)
                        onShapeInColumn = false
                    if (quartos[room.qid]?.inside != insideInColumn)
                        onInsideInColumn = false
                    if (quartos[room.qid]?.color != colorInColumn)
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
                            sizeInMD = quartos[room.qid]?.size
                            shapeInMD = quartos[room.qid]?.shape
                            insideInMD = quartos[room.qid]?.inside
                            colorInMD = quartos[room.qid]?.color
                        }
                        if (quartos[room.qid]?.size != sizeInMD)
                            onSizeInMD = false
                        if (quartos[room.qid]?.shape != shapeInMD)
                            onShapeInMD = false
                        if (quartos[room.qid]?.inside != insideInMD)
                            onInsideInMD = false
                        if (quartos[room.qid]?.color != colorInMD)
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
                            sizeInSD = quartos[room.qid]?.size
                            shapeInSD = quartos[room.qid]?.shape
                            insideInSD = quartos[room.qid]?.inside
                            colorInSD = quartos[room.qid]?.color
                        }
                        if (quartos[room.qid]?.size != sizeInSD)
                            onSizeInSD = false
                        if (quartos[room.qid]?.shape != shapeInSD)
                            onShapeInSD = false
                        if (quartos[room.qid]?.inside != insideInSD)
                            onInsideInSD = false
                        if (quartos[room.qid]?.color != colorInSD)
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

}
