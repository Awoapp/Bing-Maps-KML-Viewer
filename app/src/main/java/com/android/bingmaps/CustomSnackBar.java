package com.android.bingmaps;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.MapElement;

public class CustomSnackBar {
    private Activity activity;
    private View view;
    private CustomSnackBarListener listener;

    public CustomSnackBar(Activity activity, View view) {
        this.activity = activity;
        this.view = view;
    }

    public interface CustomSnackBarListener {
        void onSnackbarDismissed(MapElement mapElement);
    }

    public void setCustomSnackBarListener(CustomSnackBarListener listener) {
        this.listener = listener;
    }

    /**
     * Displays a custom Snackbar with the provided Geopoint and MapElement information.
     * The Snackbar shows the formatted latitude and longitude of the Geopoint and provides
     * options to copy the coordinates to the clipboard or perform other actions.
     * When the Snackbar is dismissed, the corresponding map element is removed.
     *
     * @param geopoint   The Geopoint containing the latitude and longitude to display.
     * @param mapElement The MapElement to be removed from the map when the Snackbar is dismissed.
     */
    public void showCustomSnackbar(Geopoint geopoint, MapElement mapElement) {
        // Create a Snackbar with an empty message
        Snackbar snackbar = Snackbar.make(view, "", Snackbar.LENGTH_LONG);
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                // when the Snackbar is dismissed, remove the map element
                if (listener != null) {
                    listener.onSnackbarDismissed(mapElement);
                }
            }
        });

        // inflate the custom layout for the Snackbar
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View customSnackView = inflater.inflate(R.layout.layout_snackbar, null);

        // find the TextViews in the custom layout
        TextView tvMessage = customSnackView.findViewById(R.id.tvMessage);
        TextView tvCopy = customSnackView.findViewById(R.id.tvCopy);
        TextView tvOpen = customSnackView.findViewById(R.id.tvOpen);

        // set the formatted latitude and longitude to the TextView
        double latitude = geopoint.getPosition().getLatitude();
        double longitude = geopoint.getPosition().getLongitude();
        // format the latitude and longitude to 4 decimal places
        String formattedText = String.format("%.4f, %.4f", latitude, longitude);
        tvMessage.setText(formattedText);

        // listen for clicks on the copy TextView
        tvCopy.setOnClickListener(v -> {
            // copy the formatted latitude and longitude to the clipboard
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(latitude + ", " + longitude);
            Toast.makeText(activity, "Copied: " + latitude + ", " + longitude,
                    Toast.LENGTH_LONG).show();
        });

        // listen for clicks on the open TextView
        tvOpen.setOnClickListener(v -> {
            // open the location in Google Maps
            String uri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude;
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
        });

        // add the custom layout to the Snackbar
        snackbarLayout.setPadding(0, 0, 0, 0);
        snackbarLayout.addView(customSnackView, 0);
        snackbar.show();
    }
}
