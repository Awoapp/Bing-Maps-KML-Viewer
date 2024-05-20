package com.android.bingmaps;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.bingmaps.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.MapAnimationKind;
import com.microsoft.maps.MapElement;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapFlyout;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapImage;
import com.microsoft.maps.MapRenderMode;
import com.microsoft.maps.MapScene;
import com.microsoft.maps.MapStyleSheets;
import com.microsoft.maps.MapUserInterfaceOptions;
import com.microsoft.maps.MapView;

import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoToLocation {
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 100;
    private static final int REQUEST_CODE_PICK_FILE = 101;
    private PreferencesHelper preferencesHelper;
    private CustomSnackBar customSnackBar;
    private Utilities utilities;
    ActivityMainBinding binding;
    private MapView mMapView;
    private LocationManager locationManager;

    private ArrayList<Placemark> geoModelArrayList;
    private MapIcon locationPin;
    public MapElementLayer userLocationLayer;
   private Dialog placemarkDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferencesHelper = new PreferencesHelper(this);
        utilities = new Utilities(this);
        // Initialize CustomSnackBar
        customSnackBar = new CustomSnackBar(this, binding.getRoot());
        customSnackBar.setCustomSnackBarListener(mapElement -> {
            // Remove the map element from the map when the Snackbar is dismissed
            userLocationLayer.getElements().remove(mapElement);
        });
        //mapview create and add to layout
        mMapView = new MapView(this, MapRenderMode.VECTOR);
        userLocationLayer = new MapElementLayer();
        mMapView.getLayers().add(userLocationLayer);
        mMapView.setCredentialsKey(BuildConfig.CREDENTIALS_KEY);
        binding.mapView.addView(mMapView);
        mMapView.onCreate(savedInstanceState);
        // channge map style
        mMapView.setMapStyleSheet(MapStyleSheets.aerialWithOverlay());
        //mapview user interface options
        MapUserInterfaceOptions uiOptions = mMapView.getUserInterfaceOptions();
        uiOptions.setZoomButtonsVisible(false);
        uiOptions.setTiltGestureEnabled(true);
        uiOptions.setRotateGestureEnabled(true);
        uiOptions.setUserLocationButtonVisible(true);
        //mapview user location button tapped listener
        uiOptions.addOnUserLocationButtonTappedListener(mapUserLocationButtonTappedEventArgs -> {
            getCurrentLocation();
            return false;
        });

        //mapview element tapped listener
        userLocationLayer.addOnMapElementTappedListener(mapElementTappedEventArgs -> {
            MapElement mapElement = mapElementTappedEventArgs.mapElements.get(0);
            if (mapElement instanceof MapIcon) {
                MapIcon mapIcon = (MapIcon) mapElement;
                if (((MapIcon) mapElement).getTitle().equals("Your Location")) {
                    return false;
                }
                mapIcon.getFlyout().hide();
                //show info dialog
                showFlayoutInfoDialog(utilities.findGeoModelIndexByLocation(mapIcon.getContentDescription().toString(),geoModelArrayList));
                return true;

            }
            return false;
        });
        mMapView.addOnMapTappedListener(mapTappedEventArgs -> {
            Geopoint geopoint = mapTappedEventArgs.location;
            MapIcon mapIcon = new MapIcon();
            mapIcon.setLocation(geopoint);
            mapIcon.setImage(new MapImage(utilities.getBitmapFromVectorDrawable(R.drawable.baseline_location_on_24)));
            userLocationLayer.getElements().add(mapIcon);
            customSnackBar.showCustomSnackbar(geopoint, mapIcon);
            return false;
        });

        // Create a LocationManager instance
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // check location permission
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        // Check if the KML file path is saved in SharedPreferences
        checkReadPermissionAndLoadKmlFile();
        //show all placemarks
        binding.fabList.setOnClickListener(v -> showPlacemarkListDialog());

    }

    /**
     * Checks if the app has permission to read external storage and loads the KML file.
     * If the device is running Android 34 or above, it directly reads the KML file.
     * For devices running below Android 34, it checks for the READ_EXTERNAL_STORAGE permission.
     * If the permission is not granted, it requests the permission.
     * If the permission is granted, it reads the KML file from external storage.
     */
    private void checkReadPermissionAndLoadKmlFile() {
        if (Build.VERSION.SDK_INT >= 34) {
            readKMLFileFromExternalStorage(Uri.parse(preferencesHelper.getKMLPath()));
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_READ_EXTERNAL_STORAGE);
            } else {
                readKMLFileFromExternalStorage(Uri.parse(preferencesHelper.getKMLPath()));
            }
        }
    }




    /**
     * Displays a dialog with information about a selected placemark.
     * The dialog shows the title, description, and location of the placemark.
     * The location input is filtered to accept only numbers, dots, and commas.
     * Options to open the location in Google Maps or copy the location to the clipboard are provided.
     * The dialog can be dismissed with a close button.
     *
     * @param index The index of the placemark in the geoModelArrayList to display information for.
     */
    private void showFlayoutInfoDialog(int index) {
        // Get the selected placemark from the list
        Placemark placemark = geoModelArrayList.get(index);
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_flayout_info, null);
        builder.setView(view);
        Dialog infoDialog = builder.create();
        infoDialog.setCancelable(false);
        // Find the EditText views in the dialog layout
        EditText etCategory = view.findViewById(R.id.tvCategory);
        EditText etTitle = view.findViewById(R.id.tvTitle);
        EditText etDescription = view.findViewById(R.id.tvDescription);
        EditText etLocation = view.findViewById(R.id.tvLocation);
        // Set the title, description, and location values
        etTitle.setText(placemark.getName());
        etDescription.setText(placemark.getDescription());
        String[] parts = placemark.getCoordinates().split(",");
        String cordinates=parts[1].trim() + "," + parts[0].trim();
        etLocation.setText(cordinates);
        etCategory.setText(utilities.findFirstEmptyLocationBefore(placemark.getName(),geoModelArrayList));
        //open location with google map
        view.findViewById(R.id.btnOpenWithGoogleMap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = placemark.getCoordinates();
                String[] parts = location.split(",");
                double latitude = Double.parseDouble(parts[1]);
                double longitude = Double.parseDouble(parts[0]);
                String uri = "http://maps.google.com/maps?q=loc:" +cordinates;
                startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri)));
            }
        });
        //copy location to clipboard
        etLocation.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(etLocation.getText().toString());
            Toast.makeText(MainActivity.this, "Copied: " + etLocation.getText().toString(),
                    Toast.LENGTH_SHORT).show();
        });
        //close dialog
        view.findViewById(R.id.btnClose).setOnClickListener(v -> infoDialog.dismiss());
        infoDialog.show();
        // Set the dialog background to transparent
        if (infoDialog.getWindow() != null) {
            infoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void showPlacemarkListDialog() {
        // Create a dialog builder
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomAlertDialog);

        // Inflate the dialog layout
        View view = getLayoutInflater().inflate(R.layout.dialog_list, null);
        builder.setView(view);
         placemarkDialog = builder.create();
        placemarkDialog.setCancelable(false);

        // Find the RecyclerView in the dialog layout
        RecyclerView recyclerView = view.findViewById(R.id.dialogRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (geoModelArrayList == null) {
            return;
        }
        ArrayList<Placemark> placemarkArrayList= new ArrayList<>();
        for (Placemark placemark : geoModelArrayList) {
            if (!placemark.getCoordinates().isEmpty()) {
                placemarkArrayList.add(placemark);
            }
        }
        // Set the adapter for the RecyclerView
        PlacemarkAdapter adapter = new PlacemarkAdapter(this, placemarkArrayList,this::goToLocation);
        recyclerView.setAdapter(adapter);

        // Close dialog button
        view.findViewById(R.id.dialogTitle).setOnClickListener(v -> placemarkDialog.dismiss());

        // Show the dialog
        placemarkDialog.show();
// Set the dialog background to transparent
        if (placemarkDialog.getWindow() != null) {
            placemarkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Set the dialog size
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(placemarkDialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.75);
            placemarkDialog.getWindow().setAttributes(layoutParams);
        }

    }


    /**
     * Sets all locations on the map by iterating through the geoModelArrayList.
     * For each Placemark that has non-empty coordinates, a MapIcon is created and added to the userLocationLayer.
     * The MapIcon includes a title, description, and flyout information.
     */
    public void setAllLocation() {
        userLocationLayer.getElements().clear();
        for (Placemark placemark : geoModelArrayList) {
            if (!placemark.getCoordinates().isEmpty()) {
                MapIcon pin = new MapIcon();
                pin.setLocation(utilities.convertToGeoPoint(placemark.getCoordinates()));
                pin.setTitle(placemark.getName());
                MapFlyout mapFlyout = new MapFlyout();
                mapFlyout.setTitle(placemark.getName());
                mapFlyout.setDescription(placemark.getDescription());

                pin.setFlyout(mapFlyout);
                pin.setTag(placemark.getName());
                pin.setImage(new MapImage(utilities.getBitmapFromVectorDrawableWithTint(R.drawable.baseline_location_on_24, Color.parseColor(placemark.getColor()))));
                pin.setContentDescription(placemark.getCoordinates());
                userLocationLayer.getElements().add(pin);
            }
        }
    }


    /**
     * Gets the current location of the user and updates the map with the user's position.
     * If permissions are granted, a single location update is requested.
     * The user's current location is shown on the map with a custom icon.
     * If the current zoom level is lower than the minimum desired zoom level, the map zooms in.
     */
    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        //request single location update
        locationManager.requestSingleUpdate(LocationManager.FUSED_PROVIDER, location -> {
            // When the location changes, update the map with the new location
            if (location != null) {
                Toast.makeText(MainActivity.this, "Navigating to the current location...",
                        Toast.LENGTH_LONG).show();
                if (locationPin != null) {
                    userLocationLayer.getElements().remove(locationPin);
                }
                //set zoom level
                double currentZoomLevel = mMapView.getZoomLevel();
                double minimumZoomLevel = 15.0;
                if (currentZoomLevel < minimumZoomLevel) {
                    currentZoomLevel = minimumZoomLevel;
                }
                //set map scene
                MapScene mapScene =
                        MapScene.createFromLocationAndZoomLevel(new Geopoint(location.getLatitude(), location.getLongitude()), currentZoomLevel);
                mMapView.setScene(mapScene, MapAnimationKind.LINEAR);
                //create location pin
                locationPin = null;
                locationPin = new MapIcon();
                locationPin.setLocation(new Geopoint(location.getLatitude(), location.getLongitude()));
                locationPin.setTitle("Your Location");
                locationPin.setFlat(false);
                //set location pin image
                locationPin.setImage(new MapImage(utilities.getBitmapFromVectorDrawable(R.drawable.baseline_location_on_24)));
                userLocationLayer.getElements().add(locationPin);


            }
        }, null);
    }



    /**
     * Handles the result of permission requests.
     * If the location permission is granted, it starts location updates.
     * If the read external storage permission is granted, it reads the KML file from external storage.
     *
     * @param requestCode The request code passed in requestPermissions(android.app.Activity, String[], int).
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either PackageManager.PERMISSION_GRANTED or PackageManager.PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permissions granted, start location updates
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }

        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readKMLFileFromExternalStorage(Uri.parse(preferencesHelper.getKMLPath()));
            } else {
                Toast.makeText(this, "Permission denied. The application will not function properly.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Handles the result of an activity started for result.
     * Specifically handles the result of the file picker activity.
     * If a file is selected, it takes persistable URI permissions, saves the URI to SharedPreferences, and reads the KML file from external storage.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    // Take persistable URI permissions
                    final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);

                    // Save the URI to SharedPreferences
                    preferencesHelper.saveKMLPath(uri.toString());

                    // Read the KML file from external storage
                    readKMLFileFromExternalStorage(uri);
                }
            }
        }
    }


    /**
     * Reads the KML file from external storage using the provided URI.
     * Parses the KML file and populates the geoModelArrayList with the parsed data.
     * Updates the map with the parsed placemarks.
     *
     * @param uri The URI of the KML file to be read.
     */
    private void readKMLFileFromExternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                // Parse KML file
                KMLReader kmlReader = new KMLReader(this);
                geoModelArrayList = kmlReader.readKMLFile(inputStream);

                for (Placemark placemark : geoModelArrayList) {
                    System.out.println("  Name: " + placemark.getName());
                    System.out.println("  Description: " + placemark.getDescription());
                    System.out.println("  Coordinates: " + placemark.getCoordinates());
                    Log.i("AMCIKMAKARNASI", "Name: " + placemark.getColor());
                }

                inputStream.close();
                setAllLocation();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "File could not be read. " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.i("AMCIKMAKARNASI", "File could not be read. " + e.getMessage());
        }
    }



    /**
     * Initiates an intent to open a document picker to select a KML file.
     * The selected file will be processed in the onActivityResult method.
     *
     * @param view The view that triggered this method.
     */
    public void addKmlFile(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }




    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        getCurrentLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }


    @Override
    public void goToLocation(String coordinates) {
        String[] parts = coordinates.split(",");
        double latitude = Double.parseDouble(parts[1]);
        double longitude = Double.parseDouble(parts[0]);
        MapScene mapScene = MapScene.createFromLocationAndZoomLevel(new Geopoint(latitude, longitude), 15.0);
        mMapView.setScene(mapScene, MapAnimationKind.LINEAR);
        if (placemarkDialog != null) {
            placemarkDialog.dismiss();
        }
    }
}