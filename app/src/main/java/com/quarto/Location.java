package com.quarto;

import android.view.View;

class Location {

    int centerX, centerY;
    int leftMargin, topMargin;

    Location(View pickroom, int topOffset, boolean rotated) {
        int[] loc = new int[2];
        pickroom.getLocationOnScreen(loc);
        leftMargin = loc[0];
        topMargin = loc[1]-topOffset;
        int width = pickroom.getWidth();
        int height = pickroom.getHeight();
        centerX = (leftMargin + (rotated ? ((int) Math.sqrt(Math.pow(width, 2)*2)/2) : width/2));
        centerY = topMargin + (rotated ? 0 : height/2);
    }
}
