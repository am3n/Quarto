package com.quarto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

class LocationManager {

    private static LocationManager instance;
    private int topOffset;

    static LocationManager getInstance(@NonNull AppCompatActivity activity, @NonNull View root) {
        if (instance==null)
            instance = new LocationManager(activity, root);
        return instance;
    }

    static LocationManager getInstance() {
        if (instance==null)
            instance = new LocationManager(null, null);
        return instance;
    }

    private LocationManager(@Nullable AppCompatActivity activity, @Nullable View root) {
        /*if (activity!=null && root!=null) {
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            topOffset = dm.heightPixels - root.getMeasuredHeight();
            topOffset*=-1;
            topOffset/=2;
        }*/
    }

    Location create(View view, boolean rotated) {
        return new Location(view, topOffset, rotated);
    }
}
