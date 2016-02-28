package kodestudios.pinkroads;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("I/OpenGLRenderer")

/**
 * Created by kosba on 02/27/16.
 *
 * map tutorial from: http://www.tutorialspoint.com/android/android_google_maps.htm
 * map search frag: https://github.com/googlesamples/android-play-places
 * tutorial for toolbar: http://www.101apps.co.za/index.php/articles/using-toolbars-in-your-apps.html
 */
public class MainActivity
        extends AppCompatActivity
        implements PlaceSelectionListener,  // used for map fragment
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, // used for last known gps location
        GoogleMap.OnInfoWindowClickListener, // used for marker click on map
        DirectionCallback   // used for map directions
{



    // create client for getting user's location
    GoogleMap googleMap;
    public LocationRequest mLocationRequest;
    public GoogleApiClient mGoogleApiClient;
    public AutoCompleteAdapter mAdapter;
    boolean mapReady = false;

    // create location specific variables
    public Location currentLocation;
    public Place destinationPlace;
    public int zoomLevel = 13;

    public final static String RESERVED_SEARCH_PLACE = "com.kodestudios.safespace2.RESERVED_SEARCH_PLACE";
    public final static String RESERVED_PLACE_MYPLACE = "com.kodestudios.safespace2.RESERVED_PLACE_MYPLACE";
    public final static String GOOGLE_PLACES_API_KEY = "AIzaSyAdsK9u-kgmOG4hiAqpDI0t4koyQwlNbBU";
    public final static String GOOGLE_MAP_API_KEY = "AIzaSyAVmMwc_7180YL1gazX_HDyqzlRMTftbA8";
    public final static String GOOGLE_DIRECTIONS_API_KEY = "AIzaSyDX7NUWOqwpCJpgYUkxcd2MVYJNpFpTbzc";
    public final static String CREATIVE_SAFESPACE_KEY = "coco";

    public final int PLACE_PICKER_REQUEST = 1;
    public final static String TAG = MainActivity.class.getSimpleName() + " -----------------------------";
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // google maps directions api

    boolean showMenuExtras = false;

    LinearLayout linearLayout;
    Toolbar toolbar;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*
        Log.d(TAG,  Integer.toString(R.id.linearLayout2)) ;
        linearLayout = (LinearLayout) this.findViewById(R.id.linearLayout2);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        Log.d(TAG, linearLayout.toString());
        Log.d(TAG, toolbar.toString());
        */


        /*Button button  = new Button(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
        button.setLayoutParams(lp);
        button.setText("ADDED");
        linearLayout.addView(button);
        */


        // Retrieve the PlaceAutocompleteFragment.
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Register a listener to receive callbacks when a place has been selected or an error has occurred.
        autocompleteFragment.setOnPlaceSelectedListener(this);

        /// this looks for the user's last known geo-location
        // initialize places client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1 * 1000);

        // creates the map that fills up the entire page
        try {
            if (googleMap == null) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            }
            // check for location permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setMyLocationEnabled(true);

           Marker TP = googleMap.addMarker(new MarkerOptions().position(new LatLng(43.084483,-77.678554)).title("RIT")
                    .icon(BitmapDescriptorFactory.fromAsset("images.jpg")).snippet("Population: 5,137,400"));
            MarkerInfo markerInfos[] = new MarkerInfo[10];
            markerInfos[0] = new MarkerInfo("Attack", "Danger! Keep off!", new Date(),
                    43.084483,-77.678554);
            markerInfos[1] = new MarkerInfo("EmergencyButton", "Get some help!", new Date(),
                    43.0758263,-77.683919);
            markerInfos[2] = new MarkerInfo("GangsHangout", "Danger! Keep off!", new Date(),
                    43.087079,-77.6565819);
            markerInfos[3] = new MarkerInfo("StoreHours", "Get some help!", new Date(),
                    43.0871361,-77.6633631);
            markerInfos[4] = new MarkerInfo("Attack", "Keep off!", new Date(),
                    43.1094826,-77.6784479);
            markerInfos[5] = new MarkerInfo("EmergencyButton", "Get some help!", new Date(),
                    43.1019002,-77.6398241);
            markerInfos[6] = new MarkerInfo("GangsHangout", "Keep off!", new Date(),
                    43.1028089,-77.6235807);
            markerInfos[7] = new MarkerInfo("GangsHangout", "Keep off!", new Date(),
                    43.1222953,-77.6727617);
            markerInfos[8] = new MarkerInfo("PoliceStation", "Get some help!", new Date(),
                    43.1076937,-77.6678991);


            for(int i=0; i<10;i++){
                Marker TPp = googleMap.addMarker(new MarkerOptions().position(markerInfos[i].latLng).title(markerInfos[i].type)
                        .icon(BitmapDescriptorFactory.fromAsset(markerInfos[i].icon)).snippet(markerInfos[i].message+" "+markerInfos[i].date));

            }


            mapReady = true;
            //drawMap();

            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 13);
            googleMap.animateCamera(yourLocation);
            //Log.d(TAG, "Zoom - 1");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    void drawMap() {
            // zooming into current location
            // higher for more zoom
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 13);
            googleMap.animateCamera(yourLocation);
            //Log.d(TAG, "Zoom - 2");



        /*
        Log.d(TAG,  Integer.toString(R.id.linearLayout2));
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout2);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        Log.d(TAG, linearLayout.toString());
        Log.d(TAG, toolbar.toString());
        */
    }



    /* **************************************************** */
    /*          Callback Methods for Autofill Search        */
    /* **************************************************** */
    /**
     * Callback invoked when a place has been selected from the PlaceAutocompleteFragment.
     */
    @Override
    public void onPlaceSelected(Place place) {
        Log.d(TAG, "Place Selected: " + place.getName());
        destinationPlace = place;

        // add the place to the map
        Marker TP = googleMap.addMarker( new MarkerOptions().position(place.getLatLng()) );

        // form the url direction call
        String directionCallURL = "https://maps.googleapis.com/maps/api/directions/json?" +
                "&key=" + GOOGLE_DIRECTIONS_API_KEY +
                "&origin=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() +
                "&destination=" + destinationPlace.getLatLng().latitude + "," + destinationPlace.getLatLng().longitude;
        Log.d(TAG, directionCallURL);

        // call the direction api
        GoogleDirection.withServerKey(GOOGLE_DIRECTIONS_API_KEY)
                .from(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                .to(new LatLng(destinationPlace.getLatLng().latitude, destinationPlace.getLatLng().longitude))
                //.avoid(AvoidType.FERRIES)
                        //.avoid(AvoidType.HIGHWAYS)
                .execute(this);

    }

    /**
     * Required method for implementing the marker info message
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Info window clicked", Toast.LENGTH_SHORT).show();
    }
    /**
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */
    @Override
    public void onError(Status status) {
        Log.d(TAG, "onError: Status = " + status.toString());
        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
    }


    /* ******************************************************************** */
    /*          Callback Methods for Getting Last known location            */
    /* ******************************************************************** */
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mAdapter != null)
            mAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.d(TAG, "Location services connected.");

        // check for location permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }

        handleOnConnection();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    public void handleNewLocation(Location location) {
        currentLocation = location;
        drawMap();
    }

    public void handleOnConnection() {}





    /* ************************************************************** */
    /*          Methods Google Directions  from akexorcist            */
    /* ************************************************************** */
    // directions were successfully received
    @Override
    public void onDirectionSuccess(Direction direction) {
        String status = direction.getStatus();
        // Do something
        if (direction.isOK()) {
            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            googleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, R.color.colorPrimaryDark));


            // add button
            Button btn = new Button(context);
            btn.setText("testing");

            linearLayout.addView(btn);


        }
    }


    @Override
    public void onDirectionFailure(Throwable t) {
        Log.d(TAG, "onError: Status = " + t.getMessage());
        Toast.makeText(this, "Place selection failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }

}
