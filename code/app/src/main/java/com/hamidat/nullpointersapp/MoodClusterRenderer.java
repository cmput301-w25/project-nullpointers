package com.hamidat.nullpointersapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class MoodClusterRenderer extends DefaultClusterRenderer<MoodClusterItem> {
    private final Context context;

    public MoodClusterRenderer(Context context, GoogleMap map, ClusterManager<MoodClusterItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull MoodClusterItem item, @NonNull MarkerOptions markerOptions) {
        BitmapDescriptor icon = createCircleBitmap(item.getLetter());
        markerOptions.icon(icon);
        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    private BitmapDescriptor createCircleBitmap(String letter) {
        int width = 100;
        int height = 100;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        int circleColor = "ME".equals(letter) ? Color.GREEN : Color.BLUE;
        paint.setColor(circleColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(width/2f, height/2f, width/2f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);

        Rect bounds = new Rect();
        paint.getTextBounds(letter, 0, letter.length(), bounds);
        float y = height/2f - (bounds.top + bounds.bottom)/2f;
        canvas.drawText(letter, width/2f, y, paint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}