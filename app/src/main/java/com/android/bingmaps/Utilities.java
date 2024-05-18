package com.android.bingmaps;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.microsoft.maps.Geopoint;

import java.util.ArrayList;

public class Utilities {
    Activity activity;

    public Utilities(Activity activity) {
        this.activity = activity;
    }

    /**
     * Searches for the given location in the GeoModel list and returns the index of the matching object.
     * If no match is found, it returns -1.
     *
     * @param location The location value to search for.
     * @return The index of the found GeoModel or -1 if no match is found.
     */

    public int findGeoModelIndexByLocation(String location, ArrayList<Placemark> geoModelArrayList) {
        for (int i = 0; i < geoModelArrayList.size(); i++) {
            Placemark placemark = geoModelArrayList.get(i);
            if (placemark.getCoordinates().equals(location)) {
                return i;
            }
        }
        return -1;  // Eşleşme bulunamadı
    }

    /**
     * Converts a string of coordinates into a Geopoint object.
     * The coordinates string should be in the format "longitude,latitude".
     * If the conversion fails due to a NumberFormatException, null is returned.
     *
     * @param coordinates The string containing the coordinates to convert.
     * @return A Geopoint object representing the latitude and longitude, or null if conversion fails.
     */
    public Geopoint convertToGeoPoint(String coordinates) {
        try {
            // Split the coordinates by comma
            String[] parts = coordinates.split(",");
            double longitude = Double.parseDouble(parts[0].trim());
            double latitude = Double.parseDouble(parts[1].trim());
            return new Geopoint(latitude, longitude);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Searches for a placemark by name in the given list and traverses backwards to find the first placemark with an empty location.
     * Returns the name of the placemark with an empty location, or null if no such placemark is found.
     *
     * @param name The name of the placemark to start the search from.
     * @return The name of the placemark with an empty location, or null if not found.
     */
    public String findFirstEmptyLocationBefore(String name, ArrayList<Placemark> geoModelArrayList) {
        for (int i = 0; i < geoModelArrayList.size(); i++) {
            Placemark placemark = geoModelArrayList.get(i);
            if (placemark.getName().equals(name)) {
                // Found the starting placemark, now traverse backwards
                for (int j = i - 1; j >= 0; j--) {
                    Placemark previousPlacemark = geoModelArrayList.get(j);
                    if (previousPlacemark.getCoordinates().isEmpty()) {
                        return previousPlacemark.getName();
                    }
                }
                break; // Stop the search once the starting placemark is found
            }
        }
        return null; // Return null if no matching placemark with an empty location is found
    }
    /**
     * Converts a vector drawable resource into a Bitmap.
     * This method handles compatibility for devices running versions lower than Lollipop.
     *
     * @param drawableId The resource ID of the vector drawable to convert.
     * @return A Bitmap representation of the vector drawable.
     */
    public Bitmap getBitmapFromVectorDrawable(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(activity, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public Bitmap getBitmapFromVectorDrawableWithTint( int drawableId, int color) {
        Drawable drawable = ContextCompat.getDrawable(activity, drawableId);
        drawable.setTint(color);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
