package com.quarto

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.quarto.server.Query
import com.quarto.server.QueryType
import com.quarto.server.rest.Retrovice
import com.quarto.server.socket.Parser
import com.quarto.server.socket.Server
import com.quarto.server.socket.SocketListener
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class QuartoManager internal constructor(
        context: Context?,
        private val prb: ProgressBar?,
        circleTable: View,
        roomSize: Int,
        pickroomSize: Int,
        private val rooms: Array<Array<Room?>>,
        private val pickrooms: Array<Room?>,
        private val quartos: Array<Quarto?>,
        private val qmListener: QMListener
) : QuartoListener, ControllerListener {

    private val listeners: MutableList<QuartoTouchListener>

    private var service: Retrovice? = null
    private var server: Server? = null
    private var parser: Parser? = null
    private val controller: Controller?

    private val circleTableLocation: Location
    private val circleTableRadius: Int

    private val pickroomRadius: Float
    private val roomRadius: Float
    private val roomFraction: Float

    var playState: PlayState = PlayState.PICK
    var turn: Turn = Turn.PLAYER1
    var picked = -1

    init {

        listeners = ArrayList()

        val loc = IntArray(2)
        circleTable.getLocationOnScreen(loc)
        circleTableLocation = Location(loc[0].toFloat(), (circleTable.width * sqrt(2f)) / 2)
        circleTableRadius = max(circleTable.width, circleTable.measuredWidth) / 2

        pickroomRadius = (pickroomSize / 2).toFloat()
        roomRadius = (roomSize / 2).toFloat()
        val roomPadding = context?.resources?.getDimensionPixelSize(R.dimen.room_padding)

        val pickroomSquareRadius = sqrt(pickroomRadius.toDouble().pow(2))
        val roomSquareRadius = sqrt(roomRadius.toDouble().pow(2))
        roomFraction = (roomSquareRadius - pickroomSquareRadius).toFloat() + (roomPadding?:0)

        for (i in quartos.indices) {
            listeners.add(QuartoTouchListener(listeners.size, this))
            quartos[i]?.listenTo(listeners[listeners.size - 1])
        }

        controller = Controller(rooms, quartos, this@QuartoManager)


        server = Server()
                .createSocket(Config.getIP(context), Config.getPort(context), Config.timeout)
                .setAutoConnect(Config.delay_socket_connection)
                .setListener(object : SocketListener {
                    override fun onConnecting() {
                        Log.d("Me-QuartoManager", "onConnecting()")
                    }
                    override fun onConnected() {
                        Log.d("Me-QuartoManager", "onConnected()")
                    }
                    override fun onDisconnected() {
                        Log.d("Me-QuartoManager", "onDisconnected()")
                    }
                    override fun onMessageReceived(message: String) {
                        Log.d("Me-QuartoManager", "onMessageReceived() $message")
                        val action = parser?.parse(message)
                        action?.let {
                            when (it.queryType) {
                                QueryType.PICK -> pick(it.index)
                                QueryType.MOVE -> move(it.index)
                            }
                        }
                    }
                    override fun onDownloaded() {}
                    override fun onDownloadFailed() {}
                })
        server?.connect()
        parser = Parser(server)

    }


    override fun onDown(qid: Int): Boolean {
        if (playState==PlayState.PICK && picked==-1) {
            // not picked yet
            return false

        } else if (playState==PlayState.MOVE && picked!=-1) {
            // can down on view just on picked quarto view
            return picked==qid
        }
        return false
    }

    override fun onMove(qid: Int): Boolean {
        if (playState==PlayState.PICK)
            return false
        else if (playState==PlayState.MOVE && qid==picked)
            return true
        return false
    }

    override fun onMoved(w: Int, h: Int, x: Float, y: Float) {
        qmListener.onMoved(w, h, x, y)
    }

    override fun onDrop(qid: Int): Boolean {

        if (playState==PlayState.PICK && picked==-1) {

            if (quartos[qid]?.inTable==false) {
                playState = PlayState.MOVE
                picked = qid
                qmListener.onPlayStateChanged(playState)
                turn = turn.switch()
                qmListener.onTurned(turn)
                quartos[qid]?.pick(true)
                quartos.forEach { quarto -> if (quarto?.id!=qid && quarto?.inTable==false) quarto.hide(true) }
            }
            return false


        } else if (playState==PlayState.MOVE && picked==qid) {

            quartos[qid]?.location?.let {

                val dropedIn = quartoDropedIn(it)

                if (dropedIn >= 0) {
                    val i = dropedIn / 4
                    val j = dropedIn % 4

                    quartos[qid]?.animateTo(rooms[i][j]?.location, roomFraction, roomFraction)
                    playState = PlayState.PICK
                    picked = -1
                    qmListener.onPlayStateChanged(playState, dropedIn)
                    quartos[qid]?.pick(false)
                    quartos.forEach { quarto -> quarto?.hide(false) }
                    quartos[qid]?.inTable = true
                    for (ii in 0..3) {
                        for (jj in 0..3) {
                            if (rooms[ii][jj]?.qid == qid)
                                rooms[ii][jj]?.qid = -1
                        }
                    }
                    rooms[i][j]?.qid = qid

                    checkIsQuartoo()

                    return true

                } else {

                    var backToPickRoom = true
                    var location = pickrooms[qid]?.location

                    if (dropedIn==-2) { // droped in circle table
                        for (i in 0..3) {
                            for (j in 0..3) {
                                if (rooms[i][j]?.qid == qid) {
                                    location = rooms[i][j]?.location
                                    backToPickRoom = false
                                }
                            }
                        }

                    } else { // droped outside of circle table

                    }

                    quartos[qid]?.animateTo(location)
                    if (backToPickRoom) {
                        for (i in 0..3) {
                            for (j in 0..3) {
                                if (rooms[i][j]?.qid == qid)
                                    rooms[i][j]?.qid = -1
                            }
                        }
                    }

                }

                // quartos[qid].requestLayout() ? need it or no ?

            }

        }
        return false

    }

    private fun quartoDropedIn(quartoLocation: Location): Int {

        // check quarto is witch room
        for (index in 0..15) {
            val i = index / 4
            val j = index % 4
            val xd = rooms[i][j]?.location?.coordX?.let { cX ->
                quartoLocation.coordX?.minus(cX)?.let { minus ->
                    abs(minus)
                }
            }
            val yd = rooms[i][j]?.location?.coordY?.let { cY ->
                quartoLocation.coordY?.minus(cY)?.let { minus ->
                    abs(minus)
                }
            }
            val d = yd?.toDouble()?.pow(2.0)?.let { pow ->
                xd?.toDouble()?.pow(2.0)?.plus(pow)?.let { plus ->
                    sqrt(plus)
                }
            }

            if (d!=null && d<=roomRadius && rooms[i][j]?.empty==true)
                return index
        }



        // if not in rooms, check is quarto is in circle table
        val xd = circleTableLocation.centerX?.let { cX ->
            quartoLocation.centerX?.minus(cX)?.let { minus ->
                abs(minus)
            }
        }
        val yd = circleTableLocation.centerY?.let { cY ->
            quartoLocation.centerY?.minus(cY)?.let { minus ->
                abs(minus)
            }
        }
        val d = yd?.toDouble()?.pow(2.0)?.let { pow ->
            xd?.toDouble()?.pow(2.0)?.plus(pow)?.let { plus ->
                sqrt(plus)
            }
        }
        if (d!=null && d<=circleTableRadius)
            return -2



        // if quarto is outside of circle table
        return -1
    }

    private fun checkIsQuartoo() {
        val arr = scanForQuarto()
        Log.d("Me-QuartoManager", Arrays.toString(arr))
        if (arr[0]!=-3 && arr[1]!=-3) {
            playState = PlayState.QUARTO
            qmListener.onPlayStateChanged(playState)
            quartos.forEach { quarto ->
                quarto?.quarto(false)
            }
            for (index in 0 until 4) {
                if (arr[0]>=0 && arr[1]==-1) {
                    rooms[arr[0]][index]?.qid?.let { qid ->
                        quartos[qid]?.quarto(true)
                    }
                    rooms[arr[0]][index]?.quarto(true)
                } else if (arr[0]==-1 && arr[1]>=0) {
                    rooms[index][arr[1]]?.qid?.let { qid ->
                        quartos[qid]?.quarto(true)
                    }
                    rooms[index][arr[1]]?.quarto(true)
                } else if (arr[0]==-1 && arr[1]==-1) {
                    rooms[index][index]?.qid?.let { qid ->
                        quartos[qid]?.quarto(true)
                    }
                    rooms[index][index]?.quarto(true)
                } else if (arr[0]==-2 && arr[1]==-2) {
                    rooms[index][3-index]?.qid?.let { qid ->
                        quartos[qid]?.quarto(true)
                    }
                    rooms[index][3-index]?.quarto(true)
                }
            }
        }
    }

    private fun scanForQuarto(): IntArray {

        /*

        -3, -3: not quartooo
        -2, -2: quarto in second diameter
        -1, -1: quarto in main diameter
        -1, ?: quarto in colmn ?
        ?, -1: quarto in row ?

         */

        for (index in 0 until 4) {

            // ----------  scan in rows  --------------------------
            var size: QSize? = null; var shape: QShape? = null; var inside: QInside? = null; var color: QColor? = null
            var onSize = true; var onShape = true; var onInside = true; var onColor = true
            for (i in 0 until 4) {
                rooms[index][i]?.let { room ->
                    if (!room.empty) {
                        if (size == null) {
                            size = quartos[room.qid]?.size
                            shape = quartos[room.qid]?.shape
                            inside = quartos[room.qid]?.inside
                            color = quartos[room.qid]?.color
                        }
                        if (quartos[room.qid]?.size != size)
                            onSize = false
                        if (quartos[room.qid]?.shape != shape)
                            onShape = false
                        if (quartos[room.qid]?.inside != inside)
                            onInside = false
                        if (quartos[room.qid]?.color != color)
                            onColor = false
                    } else {
                        onSize = false
                        onShape = false
                        onInside = false
                        onColor = false
                    }
                }
            }
            if (onSize || onShape || onInside || onColor) {
                return intArrayOf(index, -1)
            }

            //------------  scan in columns  -------------------------
            size = null; shape = null; inside = null; color = null
            onSize = true; onShape = true; onInside = true; onColor = true
            for (i in 0 until 4) {
                rooms[i][index]?.let { room ->
                    if (!room.empty) {
                        if (size == null) {
                            size = quartos[room.qid]?.size
                            shape = quartos[room.qid]?.shape
                            inside = quartos[room.qid]?.inside
                            color = quartos[room.qid]?.color
                        }
                        if (quartos[room.qid]?.size != size)
                            onSize = false
                        if (quartos[room.qid]?.shape != shape)
                            onShape = false
                        if (quartos[room.qid]?.inside != inside)
                            onInside = false
                        if (quartos[room.qid]?.color != color)
                            onColor = false
                    } else {
                        onSize = false
                        onShape = false
                        onInside = false
                        onColor = false
                    }
                }
            }
            if (onSize || onShape || onInside || onColor) {
                return intArrayOf(-1, index)
            }

        }


        //-----------  scan main diameter --------------------------
        var size: QSize? = null; var shape: QShape? = null; var inside: QInside? = null; var color: QColor? = null
        var onSize = true; var onShape = true; var onInside = true; var onColor = true
        for (index in 0 until 4) {
            rooms[index][index]?.let { room ->
                if (!room.empty) {
                    if (size == null) {
                        size = quartos[room.qid]?.size
                        shape = quartos[room.qid]?.shape
                        inside = quartos[room.qid]?.inside
                        color = quartos[room.qid]?.color
                    }
                    if (quartos[room.qid]?.size != size)
                        onSize = false
                    if (quartos[room.qid]?.shape != shape)
                        onShape = false
                    if (quartos[room.qid]?.inside != inside)
                        onInside = false
                    if (quartos[room.qid]?.color != color)
                        onColor = false
                } else {
                    onSize = false
                    onShape = false
                    onInside = false
                    onColor = false
                }
            }
        }
        if (onSize || onShape || onInside || onColor) {
            return intArrayOf(-1, -1)
        }


        //-----------  scan scnd diameter --------------------------
        size = null; shape = null; inside = null; color = null
        onSize = true; onShape = true; onInside = true; onColor = true
        for (index in 0 until 4) {
            rooms[index][3-index]?.let { room ->
                if (!room.empty) {
                    if (size == null) {
                        size = quartos[room.qid]?.size
                        shape = quartos[room.qid]?.shape
                        inside = quartos[room.qid]?.inside
                        color = quartos[room.qid]?.color
                    }
                    if (quartos[room.qid]?.size != size)
                        onSize = false
                    if (quartos[room.qid]?.shape != shape)
                        onShape = false
                    if (quartos[room.qid]?.inside != inside)
                        onInside = false
                    if (quartos[room.qid]?.color != color)
                        onColor = false
                } else {
                    onSize = false
                    onShape = false
                    onInside = false
                    onColor = false
                }
            }
        }
        if (onSize || onShape || onInside || onColor) {
            return intArrayOf(-2, -2)
        }


        return intArrayOf(-3, -3)
    }

    // -----------------------------------------------------

    fun doWork() {

        prb?.visibility = View.VISIBLE

        parser?.getAction(Query(rooms, quartos, if (picked==-1) QueryType.PICK else QueryType.MOVE, picked))

        /*if (service==null)
            service = RetrofitFactory.makeRetrovice()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service?.getAction(Query(
                        rooms, quartos, if (picked==-1) QueryType.PICK else QueryType.MOVE, picked
                ))
                withContext(Dispatchers.Main) { prb?.visibility = View.GONE }
                if (response?.isSuccessful == true) {
                    response.body()?.let { action ->
                        when (action.queryType) {
                            QueryType.PICK -> onPickQuarto(action.index)
                            QueryType.MOVE -> onMoveToRoom(action.index)
                        }
                    }
                } else {
                    Log.e("Retrofit", "${response?.errorBody()?.string()}")
                }
            } catch (e: HttpException) {
                //toast("Exception ${e.message}")
            } catch (t: Throwable) {
                //toast("Ooops: Something else went wrong")
            }
        }*/

        /*val t = Thread(Runnable {
            if (picked==-1)
                controller?.pickQuarto()
            else
                controller?.moveQuarto(picked)
        })
        t.priority = Thread.MAX_PRIORITY
        t.start()*/
    }

    override fun move(id: Int) {
        Handler(Looper.getMainLooper()).post {
            prb?.visibility = View.GONE

            val i = id / 4
            val j = id % 4
            quartos[picked]?.animateTo(rooms[i][j]?.location, roomFraction, roomFraction)
            quartos[picked]?.pick(false)
            quartos.forEach { quarto -> quarto?.hide(false) }
            quartos[picked]?.inTable = true
            rooms[i][j]?.qid = picked

            picked = -1
            playState = PlayState.PICK
            checkIsQuartoo()
            qmListener.onPlayStateChanged(playState, id)
        }
    }

    override fun pick(qid: Int) {
        Handler(Looper.getMainLooper()).post {
            prb?.visibility = View.GONE

            quartos[qid]?.pick(true)
            quartos.forEach { quarto -> if (quarto?.id!=qid && quarto?.inTable==false) quarto.hide(true) }

            picked = qid
            turn = turn.switch()
            qmListener.onTurned(turn)
            playState = PlayState.MOVE
            qmListener.onPlayStateChanged(playState)
        }
    }

    // -----------------------------------------------------

    fun onMyTurn(playState: PlayState, picked: Int) {
        turn = Turn.PLAYER1
        this.playState = playState
        quartos.filter { it?.inTable==false }.forEach { it?.enable(true) }
        if (picked != -1) {
            this.picked = picked
            quartos[picked]?.pick(true)
            quartos.forEach { quarto -> if (quarto?.id!=picked && quarto?.inTable==false) quarto.hide(true) }
        }
    }
    fun onFrndTurn(playState: PlayState) {
        turn = Turn.PLAYER2
        this.playState = playState
        quartos.filter { it?.inTable==false }.forEach { it?.enable(false) }
    }
    fun onFrndPlayState(playState: PlayState, roomId: Int) {
        turn = Turn.PLAYER2
        this.playState = playState
        val i = roomId / 4; val j = roomId % 4
        quartos[picked]?.animateTo(rooms[i][j]?.location, roomFraction, roomFraction)
        quartos[picked]?.pick(false)
        quartos.forEach { quarto -> quarto?.hide(false) }
        quartos[picked]?.inTable = true
        rooms[i][j]?.qid = picked
    }
    fun onFrndLocation(playState: PlayState, nX: Float, nY: Float) {
        turn = Turn.PLAYER2
        this.playState = playState
        val loc = Location()
        loc.coordX = nX
        loc.coordY = nY
        quartos[picked]?.moveTo(loc)
    }

}