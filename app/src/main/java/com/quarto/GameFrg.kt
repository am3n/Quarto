package com.quarto

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Point
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import com.quarto.server.socket.Connect
import com.quarto.server.socket.Intrf
import kotlinx.android.synthetic.main.frg_game.*

@SuppressLint("ClickableViewAccessibility")
class GameFrg(
        private val gameType: GameType,
        private val bt: BluetoothSPP? = null,
        private val conn: Connect? = null
) : Fragment(), QMListener, Parser.Listener {

    private val pickrooms: Array<Room?> = arrayOfNulls(16)
    private val rooms = Array<Array<Room?>>(4) { arrayOfNulls(4) }
    private var quartos: Array<Quarto?> = arrayOfNulls(16)
    private val imgDrws: IntArray = intArrayOf(
            R.drawable.ic_square_black_big_filled,
            R.drawable.ic_square_black_big_hollow,
            R.drawable.ic_square_black_small_filled,
            R.drawable.ic_square_black_small_hollow,
            R.drawable.ic_circle_black_big_filled,
            R.drawable.ic_circle_black_big_hollow,
            R.drawable.ic_circle_black_small_filled,
            R.drawable.ic_circle_black_small_hollow,
            R.drawable.ic_circle_white_small_hollow,
            R.drawable.ic_circle_white_small_filled,
            R.drawable.ic_circle_white_big_hollow,
            R.drawable.ic_circle_white_big_filled,
            R.drawable.ic_square_white_small_hollow,
            R.drawable.ic_square_white_small_filled,
            R.drawable.ic_square_white_big_hollow,
            R.drawable.ic_square_white_big_filled)
    private val sizes = Array(16) {
        when (it) {
            0, 1, 4, 5, 10, 11, 14, 15 -> QSize.LARGE
            else -> QSize.SMALL
        }
    }
    private val shapes = Array(16) {
        when (it) {
            0, 1, 2, 3, 12, 13, 14, 15 -> QShape.SQUARE
            else -> QShape.CIRCLE
        }
    }
    private val insides = Array(16) {
        when (it) {
            1, 3, 5, 7, 8, 10, 12, 14 -> QInside.HOLLOW
            else -> QInside.FILLED
        }
    }
    private val colors = Array(16) {
        if (it<8)
            QColor.DARK
        else
            QColor.LIGHT
    }
    private var roomsize: Int = 0
    private var pickroomsize: Int = 0
    private var screen: Point? = null

    var qm: QuartoManager? = null
    private var  parser: Parser? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frg_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        screen = AppUtils.getScreenSize(context)

        if (gameType==GameType.FRND_BT) {
            linr_wait?.visibility = VISIBLE
            bt?.setOnDataReceivedListener { _, message ->
                parser?.parse(message)
            }
            parser = Parser(this@GameFrg, bt = bt)
            parser?.shake()

        } else if (gameType==GameType.ONLINE) {
            linr_wait?.visibility = VISIBLE
            conn?.listen(object : Intrf {
                override fun onConnecting() {

                }
                override fun onConnected() {

                }
                override fun onDisconnected() {

                }
                override fun onMessageReceived(message: String) {
                    parser?.parse(message)
                }
                override fun onDownloaded() {}
                override fun onDownloadFailed() {}
            })
            parser = Parser(this@GameFrg, conn = conn)
            parser?.shake()
        }

    }

    override fun onTurned(turn: Turn) {
        txt_player?.text = turn.getString(gameType)
        if (gameType==GameType.COMP && turn==Turn.PLAYER2) {
            Handler().postDelayed({
                qm?.doWork()
            }, 500)
        } else if ((gameType==GameType.FRND_BT || gameType==GameType.ONLINE) && turn==Turn.PLAYER2) {
            onFrndTurn(PlayState.MOVE)
            parser?.changeTurn(qm?.picked)
        }
    }
    override fun onPlayStateChanged(playState: PlayState, roomId: Int) {
        txt_playstate?.text = playState.getString()
        if (gameType==GameType.COMP && qm?.turn==Turn.PLAYER2)
            Handler().postDelayed({
                qm?.doWork()
            }, 500)
        else if ((gameType==GameType.FRND_BT || gameType==GameType.ONLINE) && playState==PlayState.PICK && qm?.turn==Turn.PLAYER1) {
            parser?.changePlayState(roomId)
        }
    }
    override fun onMoved(w: Int, h: Int, x: Float, y: Float) {
        if ((gameType==GameType.FRND_BT || gameType==GameType.ONLINE) && qm?.playState==PlayState.MOVE && qm?.turn==Turn.PLAYER1) {
            parser?.changeLocation(w, h, x, y)
        }
    }


    override fun onMyTurn(playState: PlayState, picked: Int) {
        activity?.runOnUiThread {
            if (linr_wait?.visibility==VISIBLE)
                linr_wait?.visibility = GONE
            txt_player?.text = Turn.PLAYER1.getString(gameType)
            txt_playstate?.text = playState.getString()
            qm?.onMyTurn(playState, picked)
        }
    }
    override fun onFrndTurn(playState: PlayState) {
        activity?.runOnUiThread {
            if (linr_wait?.visibility==VISIBLE)
                linr_wait?.visibility = GONE
            txt_player?.text = Turn.PLAYER2.getString(gameType)
            txt_playstate?.text = playState.getString()
            qm?.onFrndTurn(playState)
        }
    }
    override fun onFrndPlayState(playState: PlayState, roomId: Int) {
        activity?.runOnUiThread {
            if (linr_wait?.visibility==VISIBLE)
                linr_wait?.visibility = GONE
            txt_player?.text = Turn.PLAYER2.getString(gameType)
            txt_playstate?.text = playState.getString()
            qm?.onFrndPlayState(playState, roomId)
        }
    }
    override fun onFrndLocation(playState: PlayState, locationChanged: Parser.LocationChanged) {
        activity?.runOnUiThread {
            val pX: Float = locationChanged.x / locationChanged.w
            val pY: Float = locationChanged.y / locationChanged.h
            val nX: Float = pX * (screen?.x?:0)
            val nY: Float = pY * (screen?.y?:0)
            qm?.onFrndLocation(playState, nX, nY)
        }
    }

    //**********************************************************************************************

    private fun init() {

        txt_player.text = Turn.PLAYER1.getString(gameType)
        txt_playstate.text = PlayState.PICK.getString()
        prb_progress.visibility = GONE
        prb_progress.indeterminateDrawable.setColorFilter(Color.parseColor("#666666"), PorterDuff.Mode.MULTIPLY)

        roomsize = resources.getDimensionPixelSize(R.dimen.room_size)
        val roompadding = resources.getDimensionPixelSize(R.dimen.room_padding) * 2
        pickroomsize = resources.getDimensionPixelSize(R.dimen.pickroom_size)
        val pickroompadding = resources.getDimensionPixelSize(R.dimen.pickroom_padding) * 2

        val screenSize = AppUtils.getScreenSize(context)

        while (screenSize.x < (roomsize+roompadding)*6) {
            roomsize--
        }
        while (screenSize.x < (pickroomsize+pickroompadding)*9) {
            pickroomsize--
        }

        var index = 0
        pickrooms[index++] = Room(-1, -1, pickroom_0 as RelativeLayout?, pickroomsize, false)
        pickrooms[index++] = Room(-1, -1, pickroom_1 as RelativeLayout?, pickroomsize, false)
        pickrooms[index++] = Room(-1, -1, pickroom_2 as RelativeLayout?, pickroomsize, false)
        pickrooms[index++] = Room(-1, -1, pickroom_3 as RelativeLayout?, pickroomsize, false)
        pickrooms[index++] = Room(-1, -1, pickroom_4 as RelativeLayout?, pickroomsize, false)
        pickrooms[index++] = Room(-1, -1, pickroom_5 as RelativeLayout?, pickroomsize, false)
        pickrooms[index++] = Room(-1, -1, pickroom_6 as RelativeLayout?, pickroomsize, false)
        pickrooms[index++] = Room(-1, -1, pickroom_7 as RelativeLayout?, pickroomsize, false)
        pickrooms[index++] = Room(-1, -1, pickroom_8 as RelativeLayout?, pickroomsize, false)
        pickrooms[index++] = Room(-1, -1, pickroom_9 as RelativeLayout?, pickroomsize, false)
        pickrooms[index++] = Room(-1, -1, pickroom_10 as RelativeLayout?, pickroomsize, false)
        pickrooms[index++] = Room(-1, -1, pickroom_11 as RelativeLayout?, pickroomsize, false)
        pickrooms[index++] = Room(-1, -1, pickroom_12 as RelativeLayout?, pickroomsize, false)
        pickrooms[index++] = Room(-1, -1, pickroom_13 as RelativeLayout?, pickroomsize, false)
        pickrooms[index++] = Room(-1, -1, pickroom_14 as RelativeLayout?, pickroomsize, false)
        pickrooms[index  ] = Room(-1, -1, pickroom_15 as RelativeLayout?, pickroomsize, false)

        rooms[0][0] = Room(0, -1, room_0_0 as RelativeLayout?, roomsize, true)
        rooms[0][1] = Room(1, -1, room_0_1 as RelativeLayout?, roomsize, true)
        rooms[0][2] = Room(2, -1, room_0_2 as RelativeLayout?, roomsize, true)
        rooms[0][3] = Room(3, -1, room_0_3 as RelativeLayout?, roomsize, true)
        rooms[1][0] = Room(4, -1, room_1_0 as RelativeLayout?, roomsize, true)
        rooms[1][1] = Room(5, -1, room_1_1 as RelativeLayout?, roomsize, true)
        rooms[1][2] = Room(6, -1, room_1_2 as RelativeLayout?, roomsize, true)
        rooms[1][3] = Room(7, -1, room_1_3 as RelativeLayout?, roomsize, true)
        rooms[2][0] = Room(8, -1, room_2_0 as RelativeLayout?, roomsize, true)
        rooms[2][1] = Room(9, -1, room_2_1 as RelativeLayout?, roomsize, true)
        rooms[2][2] = Room(10, -1, room_2_2 as RelativeLayout?, roomsize, true)
        rooms[2][3] = Room(11, -1, room_2_3 as RelativeLayout?, roomsize, true)
        rooms[3][0] = Room(12, -1, room_3_0 as RelativeLayout?, roomsize, true)
        rooms[3][1] = Room(13, -1, room_3_1 as RelativeLayout?, roomsize, true)
        rooms[3][2] = Room(14, -1, room_3_2 as RelativeLayout?, roomsize, true)
        rooms[3][3] = Room(15, -1, room_3_3 as RelativeLayout?, roomsize, true)



        for (i in quartos.indices) {
            val layoutParams = RelativeLayout.LayoutParams(pickroomsize, pickroomsize)
            quartos[i] = Quarto(context, layoutParams, i, sizes[i], shapes[i], insides[i], colors[i], imgDrws[i])
            frg_game?.addView(quartos[i]?.view)
        }
        /*var size = 9
        val constSize = size
        val temp: Array<Quarto?> = arrayOfNulls(constSize)
        temp[constSize-size] = quartos[0]; size--
        temp[constSize-size] = quartos[1]; size--
        temp[constSize-size] = quartos[2]; size--
        temp[constSize-size] = quartos[3]; size--
        temp[constSize-size] = quartos[4]; size--
        //temp[constSize-size] = quartos[5]; size--
        temp[constSize-size] = quartos[12]; temp[constSize-size]?.id = constSize-size; size--
        temp[constSize-size] = quartos[13]; temp[constSize-size]?.id = constSize-size; size--
        temp[constSize-size] = quartos[14]; temp[constSize-size]?.id = constSize-size; size--
        temp[constSize-size] = quartos[15]; temp[constSize-size]?.id = constSize-size; size--
        quartos = temp*/


        val observer = frg_game?.viewTreeObserver
        observer?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                for (i in 0..3) { for (j in 0..3) {
                    rooms[i][j]?.location?.calc()
                } }

                for (i in quartos.indices) {
                    pickrooms[i]?.location?.calc()
                    quartos[i]?.moveTo(pickrooms[i]?.location)
                }


                qm = QuartoManager(context, prb_progress, circle_table, roomsize, pickroomsize, rooms, pickrooms, quartos, this@GameFrg)

                frg_game?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
            }
        })

    }

}