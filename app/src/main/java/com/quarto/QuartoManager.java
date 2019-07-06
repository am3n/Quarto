package com.quarto;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class QuartoManager implements QuartoListener {

    private Location[][] tableRooms;
    private Location[] pickRooms;
    private List<QuartoTouchListener> listeners;
    private int r;

    QuartoManager(Context context, Location[][] tableRooms, Location[] pickRooms) {
        this.tableRooms = tableRooms;
        this.pickRooms = pickRooms;
        listeners = new ArrayList<>();
        r = context.getResources().getDimensionPixelSize(R.dimen.room_size) / 2;
    }

    QuartoTouchListener createOne() {
        listeners.add(new QuartoTouchListener(listeners.size(), this));
        return listeners.get(listeners.size()-1);
    }

    @Override
    public void onDrop(int qid, View quarto) {

        Location quartoLocation = LocationManager.getInstance().create(quarto, false);
        Log.d("Meeeee", "quarto location: "+quartoLocation.centerX+", "+quartoLocation.centerY);

        int tableRoomIndex;
        if ((tableRoomIndex = isQuartoInTableRoom(quartoLocation))>=0) {
            int i = tableRoomIndex / 4;
            int j = tableRoomIndex % 4;
            Log.d("Meeeee", "quarto is in: "+i+", "+j);
            Location location = tableRooms[i][j];

            int d = (int) Math.sqrt(Math.pow(r, 2) + Math.pow(r, 2));
            int p = quarto.getContext().getResources().getDimensionPixelSize(R.dimen.room_padding) * 2;
            int f = (d - r) / 2 - p;

            ((RelativeLayout.LayoutParams)quarto.getLayoutParams()).leftMargin = location.leftMargin + (quarto.getMeasuredWidth()/2) + f;
            ((RelativeLayout.LayoutParams)quarto.getLayoutParams()).topMargin = location.topMargin - (quarto.getMeasuredHeight()/2);
        } else {
            Log.d("Meeeee", "quarto is out of table");
            Location location = pickRooms[qid];
            ((RelativeLayout.LayoutParams)quarto.getLayoutParams()).leftMargin = location.leftMargin;
            ((RelativeLayout.LayoutParams)quarto.getLayoutParams()).topMargin = location.topMargin;
        }

        quarto.requestLayout();

    }

    private int isQuartoInTableRoom(Location quartoLocation) {
        for (int index=0; index<16; index++) {
            int i = index/4;
            int j = index%4;
            int xd = Math.abs(quartoLocation.centerX - tableRooms[i][j].centerX);
            int yd = Math.abs(quartoLocation.centerY - tableRooms[i][j].centerY);
            int d = (int) Math.sqrt(Math.pow(xd, 2) + Math.pow(yd, 2));
            if (d<=r)
                return index;
        }
        return -1;
    }

}
