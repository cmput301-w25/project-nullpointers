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

/**
 * Custom renderer for mood event cluster markers.
 * <p>
 * Creates circular markers with centered text, using different colors
 * for the user's own event ("ME") versus others.
 * </p>
 */
public class MoodClusterRenderer extends DefaultClusterRenderer<MoodClusterItem> {
    private static final int MARKER_SIZE = 100;
    private static final int TEXT_SIZE = 40;
    private final Context context;

    /**
     * Constructs a new MoodClusterRenderer.
     *
     * @param context        Application context
     * @param map            GoogleMap instance
     * @param clusterManager ClusterManager managing the items
     */
    public MoodClusterRenderer(Context context, GoogleMap map, ClusterManager<MoodClusterItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    /**
     * Configures marker appearance before rendering.
     *
     * @param item          Cluster item to render
     * @param markerOptions Options to configure for the marker
     */
    @Override
    protected void onBeforeClusterItemRendered(@NonNull MoodClusterItem item, @NonNull MarkerOptions markerOptions) {
        BitmapDescriptor icon = createCircleBitmap(item.getLetter());
        markerOptions.icon(icon);
        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    private BitmapDescriptor createCircleBitmap(String letter) {
        Bitmap bitmap = Bitmap.createBitmap(MARKER_SIZE, MARKER_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        int circleColor = "ME".equals(letter) ? Color.GREEN : Color.BLUE;
        paint.setColor(circleColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(MARKER_SIZE/2f, MARKER_SIZE/2f, MARKER_SIZE/2f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(TEXT_SIZE);
        paint.setTextAlign(Paint.Align.CENTER);

        Rect bounds = new Rect();
        paint.getTextBounds(letter, 0, letter.length(), bounds);
        float y = MARKER_SIZE/2f - (bounds.top + bounds.bottom)/2f;
        canvas.drawText(letter, MARKER_SIZE/2f, y, paint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}