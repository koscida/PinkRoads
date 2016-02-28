package kodestudios.pinkroads;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener // used for last known gps location
{

    // CREATING A POINT
    static final LatLng RITPoint = new LatLng(43.085207, -77.671417);


    // create client for getting user's location
    GoogleMap googleMap;
    public LocationRequest mLocationRequest;
    public GoogleApiClient mGoogleApiClient;
    public AutoCompleteAdapter mAdapter;
    public Location currentLocation;
    public double currentLatitude = 0;
    public double currentLongitude = 0;
    boolean mapReady = false;

    public final static String RESERVED_SEARCH_PLACE = "com.kodestudios.safespace2.RESERVED_SEARCH_PLACE";
    public final static String RESERVED_PLACE_MYPLACE = "com.kodestudios.safespace2.RESERVED_PLACE_MYPLACE";
    public final static String GOOGLE_PLACES_API_KEY = "AIzaSyAdsK9u-kgmOG4hiAqpDI0t4koyQwlNbBU";
    public final static String GOOGLE_MAP_API_KEY = "AIzaSyAVmMwc_7180YL1gazX_HDyqzlRMTftbA8";
    public final static String CREATIVE_SAFESPACE_KEY = "coco";
    public final int PLACE_PICKER_REQUEST = 1;
    public final static String TAG = MainActivity.class.getSimpleName() + " ----------";
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            // ACTUALLY ADDING ON MAP
            Marker TP = googleMap.addMarker(new MarkerOptions().position(RITPoint).title("RIT"));

            mapReady = true;
            drawMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void drawMap() {
        if(currentLatitude != 0 && currentLongitude != 0 && mapReady) {
            // zooming into current location
            LatLng coordinate = new LatLng(currentLatitude, currentLongitude);
            // higher for more zoom
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 13);
            googleMap.animateCamera(yourLocation);
        }
    }



    /* **************************************************** */
    /*          Callback Methods for Autofill Search        */
    /* **************************************************** */

    /**
     * Callback invoked when a place has been selected from the PlaceAutocompleteFragment.
     */
    @Override
    public void onPlaceSelected(Place place) {
        Log.i(TAG, "Place Selected: " + place.getName());

        // Format the returned place's details and display them in the TextView.
        //mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(), place.getId(),place.getAddress(), place.getPhoneNumber(), place.getWebsiteUri()));

        CharSequence attributions = place.getAttributions();
        if (!TextUtils.isEmpty(attributions)) {
            //mPlaceAttribution.setText(Html.fromHtml(attributions.toString()));
        } else {
            //mPlaceAttribution.setText("");
        }

        Context context = getApplicationContext();
        Spanned s = formatPlaceDetails(getResources(), place.getName(), place.getId(), place.getAddress(), place.getPhoneNumber(), place.getWebsiteUri());
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, s, duration);
        toast.show();
    }

    /**
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */
    @Override
    public void onError(Status status) {
        Log.e(TAG, "onError: Status = " + status.toString());

        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Helper method to format information about a place nicely.
     */
    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id, CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

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
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        drawMap();
        /*Log.d(TAG, currentLocation.toString());
        Log.d(TAG, new Double(currentLatitude).toString());
        Log.d(TAG, new Double(currentLongitude).toString());*/

    }

    public void handleOnConnection() {}





}
