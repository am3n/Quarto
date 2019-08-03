package com.quarto

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewTreeObserver
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main2.*
import kotlin.math.max
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log


// https://www.tutorialspoint.com/android/android_drag_and_drop.htm
// https://proandroiddev.com/home-automation-with-android-things-kotlin-2e0334101f08   server socket handler seems good

class Main2Activity : AppCompatActivity(), QMListener {

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
    private val qSizes = Array(16) {
        when (it) {
            0, 1, 4, 5, 10, 11, 14, 15 -> QSize.LARGE
            else -> QSize.SMALL
        }
    }
    private val qShapes = Array(16) {
        when (it) {
            0, 1, 2, 3, 12, 13, 14, 15 -> QShape.SQUARE
            else -> QShape.CIRCLE
        }
    }
    private val qInsides = Array(16) {
        when (it) {
            1, 3, 5, 7, 8, 10, 12, 14 -> QInside.HOLLOW
            else -> QInside.FILLED
        }
    }
    private val qColors = Array(16) {
        if (it<8)
            QColor.DARK
        else
            QColor.LIGHT
    }
    private var roomsize: Int = 0
    private var pickroomsize: Int = 0

    var qm: QuartoManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        App.hideNavAndStus(window)

        init()


        txtTitle?.setOnClickListener {
            qm?.controller?.doWork()
        }

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        App.hideNavAndStus(window)
    }

    override fun onTurned(turn: Turn) {
        txt_player.text = turn.getString()
    }

    override fun onPlayStateChanged(playState: PlayState) {
        txt_playstate.text = playState.getString()
        if (playState==PlayState.QUARTO)
            txt_player.text = txt_player.text.toString().replace("نوبت ", "")
    }

    //**********************************************************************************************

    @SuppressLint("ClickableViewAccessibility")
    fun init() {

        roomsize = resources.getDimensionPixelSize(R.dimen.room_size)
        val roompadding = resources.getDimensionPixelSize(R.dimen.room_padding) * 2
        pickroomsize = resources.getDimensionPixelSize(R.dimen.pickroom_size)
        val pickroompadding = resources.getDimensionPixelSize(R.dimen.pickroom_padding) * 2

        val screenSize = App.getScreenSize(baseContext)

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
            quartos[i] = Quarto(baseContext, layoutParams, i, qSizes[i], qShapes[i], qInsides[i], qColors[i], imgDrws[i])
            activity_main?.addView(quartos[i]?.view)
        }
        var size = 10
        val constSize = size
        val temp: Array<Quarto?> = arrayOfNulls(constSize)
        temp[constSize-size] = quartos[0]; size--
        temp[constSize-size] = quartos[1]; size--
        temp[constSize-size] = quartos[2]; size--
        temp[constSize-size] = quartos[3]; size--
        temp[constSize-size] = quartos[4]; size--
        temp[constSize-size] = quartos[5]; size--
        temp[constSize-size] = quartos[12]; size--
        temp[constSize-size] = quartos[13]; size--
        temp[constSize-size] = quartos[14]; size--
        temp[constSize-size] = quartos[15]; size--
        quartos = temp


        val observer = activity_main?.viewTreeObserver
        observer?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                for (i in 0..3) { for (j in 0..3) {
                    rooms[i][j]?.calc()
                } }

                for (i in quartos.indices) {
                    pickrooms[i]?.calc()
                    quartos[i]?.moveTo(pickrooms[i]?.location)
                }


                qm = QuartoManager(baseContext, circle_table, roomsize, pickroomsize, rooms, pickrooms, quartos, this@Main2Activity)

                activity_main?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
            }
        })

    }

}
