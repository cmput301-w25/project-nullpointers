package com.hamidat.nullpointersapp.utils.mapUtils;

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
    private static final int MARKER_SIZE = 100;
    private static final int TEXT_SIZE = 30;
    private final Context context;
    /**
     * Initializes the cluster renderer.
     *
     * @param context         Application context
     * @param map            GoogleMap instance
     * @param clusterManager Cluster manager instance
     */
    public MoodClusterRenderer(Context context, GoogleMap map, ClusterManager<MoodClusterItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }
    /**
     * Configures marker appearance before rendering.
     *
     * @param item           MoodClusterItem to render
     * @param markerOptions  Marker configuration object
     */
    @Override
    protected void onBeforeClusterItemRendered(@NonNull MoodClusterItem item, @NonNull MarkerOptions markerOptions) {
        BitmapDescriptor icon = createCircleBitmap(item.getEmotion());
        markerOptions.icon(icon);
        super.onBeforeClusterItemRendered(item, markerOptions);
    }
    /**
     * Creates a circular bitmap marker with emotion-specific coloring.
     *
     * @param emotion Emotion string to determine color and label
     * @return BitmapDescriptor for marker icon
     */
    private BitmapDescriptor createCircleBitmap(String emotion) {
        Bitmap bitmap = Bitmap.createBitmap(MARKER_SIZE, MARKER_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        int circleColor;
        switch (emotion) {
            case "Happy":
                circleColor = Color.YELLOW;
                break;
            case "Sad":
                circleColor = Color.BLUE;
                break;
            case "Angry":
                circleColor = Color.RED;
                break;
            case "Chill":
                circleColor = Color.GREEN;
                break;
            default:
                circleColor = Color.GRAY;
        }
        paint.setColor(circleColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(MARKER_SIZE/2f, MARKER_SIZE/2f, MARKER_SIZE/2f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(TEXT_SIZE);
        paint.setTextAlign(Paint.Align.CENTER);

        Rect bounds = new Rect();
        paint.getTextBounds(emotion, 0, emotion.length(), bounds);
        float y = MARKER_SIZE/2f - (bounds.top + bounds.bottom)/2f;
        canvas.drawText(emotion.substring(0, 1), MARKER_SIZE/2f, y, paint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}