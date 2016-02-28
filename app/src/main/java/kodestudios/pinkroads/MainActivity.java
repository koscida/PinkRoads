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
import android.util.Log;
import android.widget.Toast;
import java.util.Date;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
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
        GoogleMap.OnInfoWindowClickListener
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
    public final static String TAG = MainActivity.class.getSimpleName() + " ----------";
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // google maps directions api

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
            // check for location permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setMyLocationEnabled(true);

           // Marker TP = googleMap.addMarker(new MarkerOptions().position(new LatLng(43.084483,-77.678554)).title("RIT")
                   // .icon(BitmapDescriptorFactory.fromAsset("images.jpg")).snippet("Population: 5,137,400"));
            MarkerInfo RIT = new MarkerInfo("Danger", "Danger! Keep off!", new Date(),
                    43.084483,-77.678554);
            Marker TP = googleMap.addMarker(new MarkerOptions().position(RIT.latLng).title(RIT.type)
                    .icon(BitmapDescriptorFactory.fromAsset(RIT.icon)).snippet(RIT.message));
            MarkerInfo PoliceStation = new MarkerInfo("Help", "Get some help!", new Date(),
                    43.0758263,-77.683919);
            Marker TP2 = googleMap.addMarker(new MarkerOptions().position(PoliceStation.latLng).title(PoliceStation.type)
                    .icon(BitmapDescriptorFactory.fromAsset(PoliceStation.icon)).snippet(PoliceStation.message));



            mapReady = true;
            drawMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void drawMap() {
        if(currentLocation.getLatitude() != 0 && currentLocation.getLongitude() != 0 && mapReady) {
            // zooming into current location
            LatLng coordinate = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            // higher for more zoom
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, zoomLevel);
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
        Log.d(TAG, "Place Selected: " + place.getName());
        destinationPlace = place;

        // for now, toast the call, so we have some info
        Context context = getApplicationContext();
        Spanned s = formatPlaceDetails(getResources(), place.getName(), place.getId(), place.getAddress(), place.getPhoneNumber(), place.getWebsiteUri());
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, s, duration);
        toast.show();

        // add the place to the map
        Marker TP = googleMap.addMarker( new MarkerOptions().position(place.getLatLng()) );
        // form the url direction call
        String directionCallURL = "https://maps.googleapis.com/maps/api/directions/json?" +
                "&key=" + GOOGLE_DIRECTIONS_API_KEY +
                "&origin=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() +
                "&destination=" + destinationPlace.getLatLng().latitude + "," + destinationPlace.getLatLng().longitude;
        Log.d(TAG, directionCallURL);






        GoogleDirection.withServerKey(GOOGLE_DIRECTIONS_API_KEY)
                .from(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                .to(new LatLng(destinationPlace.getLatLng().latitude, destinationPlace.getLatLng().longitude))
                //.avoid(AvoidType.FERRIES)
                //.avoid(AvoidType.HIGHWAYS)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction) {
                        if (direction.isOK()) {
                            // Do something
                        } else {
                            // Do something
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something
                    }
                });






    }

    /**
     * Required method for implementing the marker info message
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Info window clicked",
                Toast.LENGTH_SHORT).show();
    }
    /**
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */
    @Override
    public void onError(Status status) {
        Log.d(TAG, "onError: Status = " + status.toString());

        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Helper method to format information about a place nicely.
     */
    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id, CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.d(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
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
        drawMap();
        /*Log.d(TAG, currentLocation.toString());
        Log.d(TAG, new Double(currentLatitude).toString());
        Log.d(TAG, new Double(currentLongitude).toString());*/

    }

    public void handleOnConnection() {}





    /* ****************************************** */
    /*          Methods Google Map API            */
    /* ****************************************** */

}
