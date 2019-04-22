package com.quarto;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

// https://www.tutorialspoint.com/android/android_drag_and_drop.htm

public class Main2Activity extends AppCompatActivity {

    private ViewGroup root;
    private AppCompatActivity _this = this;
    private RelativeLayout[] pickrooms = new RelativeLayout[16];
    private Location[] pickRoomsLocation = new Location[16];
    private RelativeLayout[][] rooms = new RelativeLayout[4][4];
    private Location[][] tableRoomsLocation = new Location[4][4];
    private AppCompatImageView[] quartos = new AppCompatImageView[16];
    private int drawables[] = {
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
            R.drawable.ic_square_white_big_filled
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        hideNavAndSts();
        findViewsById();

    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideNavAndSts();
    }


    //**********************************************************************************************

    @SuppressLint("ClickableViewAccessibility")
    public void findViewsById() {

        root = findViewById(R.id.activity_main);

        int index = 0;
        pickrooms[index++] = findViewById(R.id.pickroom_0);
        pickrooms[index++] = findViewById(R.id.pickroom_1);
        pickrooms[index++] = findViewById(R.id.pickroom_2);
        pickrooms[index++] = findViewById(R.id.pickroom_3);
        pickrooms[index++] = findViewById(R.id.pickroom_4);
        pickrooms[index++] = findViewById(R.id.pickroom_5);
        pickrooms[index++] = findViewById(R.id.pickroom_6);
        pickrooms[index++] = findViewById(R.id.pickroom_7);
        pickrooms[index++] = findViewById(R.id.pickroom_8);
        pickrooms[index++] = findViewById(R.id.pickroom_9);
        pickrooms[index++] = findViewById(R.id.pickroom_10);
        pickrooms[index++] = findViewById(R.id.pickroom_11);
        pickrooms[index++] = findViewById(R.id.pickroom_12);
        pickrooms[index++] = findViewById(R.id.pickroom_13);
        pickrooms[index++] = findViewById(R.id.pickroom_14);
        pickrooms[index  ] = findViewById(R.id.pickroom_15);

        rooms[0][0] = findViewById(R.id.room_0_0);
        rooms[0][1] = findViewById(R.id.room_0_1);
        rooms[0][2] = findViewById(R.id.room_0_2);
        rooms[0][3] = findViewById(R.id.room_0_3);
        rooms[1][0] = findViewById(R.id.room_1_0);
        rooms[1][1] = findViewById(R.id.room_1_1);
        rooms[1][2] = findViewById(R.id.room_1_2);
        rooms[1][3] = findViewById(R.id.room_1_3);
        rooms[2][0] = findViewById(R.id.room_2_0);
        rooms[2][1] = findViewById(R.id.room_2_1);
        rooms[2][2] = findViewById(R.id.room_2_2);
        rooms[2][3] = findViewById(R.id.room_2_3);
        rooms[3][0] = findViewById(R.id.room_3_0);
        rooms[3][1] = findViewById(R.id.room_3_1);
        rooms[3][2] = findViewById(R.id.room_3_2);
        rooms[3][3] = findViewById(R.id.room_3_3);



        for (int i=0; i<quartos.length; i++) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.pickroom_size),
                    getResources().getDimensionPixelSize(R.dimen.pickroom_size)
            );
            quartos[i] = new AppCompatImageView(getBaseContext());
            quartos[i].setLayoutParams(layoutParams);
            quartos[i].setImageResource(drawables[i]);
            quartos[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
            quartos[i].bringToFront();
            root.addView(quartos[i]);
        }

        //---------------------------------------

        /*
        class QuartoTouchListener implements View.OnTouchListener {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                    view.startDrag(data, shadowBuilder, view, 0);
                    view.setAlpha(1f);
                    view.setVisibility(View.INVISIBLE);
                    return true;
                }
                return false;
            }
        }
        class MyDragListener implements View.OnDragListener {
            *//*Drawable enterShape = getResources().getDrawable(R.drawable.shape_droptarget);
            Drawable normalShape = getResources().getDrawable(R.drawable.shape);*//*
            @Override
            public boolean onDrag(View v, DragEvent event) {
                int action = event.getAction();
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        // do nothing
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        //v.setBackground(enterShape);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        //v.setBackground(normalShape);
                        break;
                    case DragEvent.ACTION_DROP:
                        // Dropped, reassign View to ViewGroup
                        *//*View view = (View) event.getLocalState();
                        ViewGroup owner = (ViewGroup) view.getParent();
                        owner.removeView(view);
                        LinearLayout container = (LinearLayout) v;
                        container.addView(view);
                        view.setVisibility(View.VISIBLE);*//*
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        //v.setBackground(normalShape);
                    default:
                        break;
                }
                return true;
            }
        }
        */


        final ViewTreeObserver observer = root.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        tableRoomsLocation[i][j] = LocationManager.getInstance(_this, root).create(rooms[i][j], true);
                    }
                }

                for (int i=0; i<pickrooms.length; i++) {
                    pickRoomsLocation[i] = LocationManager.getInstance(_this, root).create(pickrooms[i], false);
                    ((RelativeLayout.LayoutParams)quartos[i].getLayoutParams()).leftMargin = pickRoomsLocation[i].leftMargin;
                    ((RelativeLayout.LayoutParams)quartos[i].getLayoutParams()).topMargin = pickRoomsLocation[i].topMargin;
                    quartos[i].requestLayout();
                }

                QuartoManager quartoManager = new QuartoManager(getBaseContext(), tableRoomsLocation, pickRoomsLocation);
                for (int i=0; i<pickrooms.length; i++) {
                    quartos[i].setOnTouchListener(quartoManager.createOne());
                }

                root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }

    public void hideNavAndSts() {
        final int flags =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flags);

        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                decorView.setSystemUiVisibility(flags);
            }
        });

    }

}
