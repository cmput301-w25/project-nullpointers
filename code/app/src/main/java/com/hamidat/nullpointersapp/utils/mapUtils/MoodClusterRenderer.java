/**
 * MoodClusterRenderer.java
 *
 * Custom renderer for mood markers on the map. Converts emotion-based vector icons to bitmap markers,
 * assigns them to individual mood events, and removes transparent padding for precise marker hitboxes.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.utils.mapUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.hamidat.nullpointersapp.R;

/**
 * Custom renderer for mood event markers.
 * For individual markers, it uses XML vector drawables based on the mood,
 * while cluster markers are rendered as circles.
 */
public class MoodClusterRenderer extends DefaultClusterRenderer<MoodClusterItem> {

    private final Context context;

    /**
     * Initializes the cluster renderer.
     *
     * @param context         Application context.
     * @param map             GoogleMap instance.
     * @param clusterManager  Cluster manager instance.
     */
    public MoodClusterRenderer(Context context, GoogleMap map, ClusterManager<MoodClusterItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    /**
     * Configures marker appearance for individual (non-cluster) items.
     * It converts a vector drawable resource into a BitmapDescriptor and uses that as the icon.
     *
     * @param item           MoodClusterItem to render.
     * @param markerOptions  Marker configuration object.
     */
    @Override
    protected void onBeforeClusterItemRendered(@NonNull MoodClusterItem item, @NonNull MarkerOptions markerOptions) {
        int drawableId;
        switch (item.getEmotion()) {
            case "Happy":
                drawableId = R.drawable.ic_pin_happy;
                break;
            case "Sad":
                drawableId = R.drawable.ic_pin_sad;
                break;
            case "Angry":
                drawableId = R.drawable.ic_pin_angry;
                break;
            case "Chill":
                drawableId = R.drawable.ic_pin_chill;
                break;
            case "Afraid":
                drawableId = R.drawable.ic_pin_fear;
                break;
            case "Disgusted":
                drawableId = R.drawable.ic_pin_disgust;
                break;
            case "Shameful":
                drawableId = R.drawable.ic_pin_shame;
                break;
            case "Surprised":
                drawableId = R.drawable.ic_pin_surprise;
                break;
            case "Confused":
                drawableId = R.drawable.ic_pin_confusion;
                break;
            default:
                drawableId = R.drawable.ic_pin_default;
                break;
        }
        BitmapDescriptor icon = getBitmapDescriptorFromVector(context, drawableId);
        markerOptions.icon(icon);
        markerOptions.anchor(0.5f, 1.0f);
    }


    /**
     * Converts a vector drawable resource into a BitmapDescriptor.
     * This method first creates a Bitmap from the vector drawable, then crops out the transparent
     * padding so that the clickable area more closely fits the visible icon.
     *
     * @param context     Application context.
     * @param vectorResId Resource ID of the vector drawable.
     * @return BitmapDescriptor created from the cropped bitmap.
     */
    private BitmapDescriptor getBitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }
        // Set the bounds for the drawable
        int width = vectorDrawable.getIntrinsicWidth();
        int height = vectorDrawable.getIntrinsicHeight();
        vectorDrawable.setBounds(0, 0, width, height);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        // Crop out transparent areas to shrink the clickable region.
        Bitmap croppedBitmap = cropTransparent(bitmap);
        return BitmapDescriptorFactory.fromBitmap(croppedBitmap);
    }

    /**
     * Crops out the transparent pixels from a bitmap.
     *
     * @param bitmap The original bitmap.
     * @return A new bitmap cropped to the non-transparent bounds.
     */
    private Bitmap cropTransparent(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int top = height, left = width, right = 0, bottom = 0;

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                // Check if pixel is not transparent (alpha > 0)
                if ((pixel >>> 24) != 0) {
                    if (x < left) left = x;
                    if (x > right) right = x;
                    if (y < top) top = y;
                    if (y > bottom) bottom = y;
                }
            }
        }
        // If no non-transparent pixel is found, return the original bitmap.
        if (left > right || top > bottom) {
            return bitmap;
        }
        return Bitmap.createBitmap(bitmap, left, top, right - left + 1, bottom - top + 1);
    }
}
