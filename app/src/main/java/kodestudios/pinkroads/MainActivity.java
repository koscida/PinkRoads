package kodestudios.pinkroads;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

@SuppressWarnings("I/OpenGLRenderer")

/**
 * Created by kosba on 02/27/16.
 */
public class MainActivity
        extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    public final static String RESERVED_SEARCH_PLACE = "com.kodestudios.pinkroads.RESERVED_SEARCH_PLACE";
    public final static String RESERVED_PLACE_MYPLACE = "com.kodestudios.pinkroads.RESERVED_PLACE_MYPLACE";
    public final static String GOOGLE_PLACES_API_KEY = "AIzaSyAdsK9u-kgmOG4hiAqpDI0t4koyQwlNbBU";
    public final static String GOOGLE_MAP_API_KEY = "AIzaSyAVmMwc_7180YL1gazX_HDyqzlRMTftbA8";
    public final static String CREATIVE_PINKROADS_KEY = "coco";
    public final int PLACE_PICKER_REQUEST = 1;
    public final static String TAG = MainActivity.class.getSimpleName() + " ----------";
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    // Defining Drawer and Toolbar Variables
    public Toolbar toolbar;
    public NavigationView navigationView;
    public DrawerLayout drawerLayout;

    // Define user's location variables
    public Location currentLocation;
    public double currentLatitude;
    public double currentLongitude;
    public LocationRequest mLocationRequest;

    // Define places variables
    public GoogleApiClient mGoogleApiClient;
    public AutoCompleteAdapter mAdapter;
    public TextView mTextView;
    public AutoCompleteTextView mPredictTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(TAG, "BaseActivity - Begin");
        setContentView(R.layout.activity_main);


        // initialize places client
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage( this, 0, this )
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


        //Log.d(TAG, "BaseActivity - End");
    }




    /* ********************************************* */
    /*          Google Places API - Generic          */
    /* ********************************************* */

    @Override
    protected void onStart() {
        super.onStart();
        if( mGoogleApiClient != null )
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected( Bundle bundle ) {
        if( mAdapter != null )
            mAdapter.setGoogleApiClient( mGoogleApiClient );
        Log.d(TAG, "Location services connected.");

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
    public void onConnectionSuspended( int i ) {
        Log.d(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed( ConnectionResult connectionResult ) {
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
        /*Log.d(TAG, currentLocation.toString());
        Log.d(TAG, new Double(currentLatitude).toString());
        Log.d(TAG, new Double(currentLongitude).toString());*/
    }

    public void handleOnConnection() {}





    /* ************************************************** */
    /*          Google Places API - Place Picker          */
    /* ************************************************** */

    public void findPlaceById( String id ) {
        if( TextUtils.isEmpty(id) || mGoogleApiClient == null || !mGoogleApiClient.isConnected() )
            return;

        Places.GeoDataApi.getPlaceById(mGoogleApiClient, id) .setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(PlaceBuffer places) {
                if (places.getStatus().isSuccess()) {
                    Place place = places.get(0);
                    displayPlace(place);
                    mPredictTextView.setText("");
                    mAdapter.clear();
                }

                //Release the PlaceBuffer to prevent a memory leak
                places.release();
            }
        });
    }

    public void guessCurrentPlace() {
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace( mGoogleApiClient, null );
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {

                PlaceLikelihood placeLikelihood = likelyPlaces.get(0);
                String content = "";
                if (placeLikelihood != null && placeLikelihood.getPlace() != null && !TextUtils.isEmpty(placeLikelihood.getPlace().getName()))
                    content = "Most likely place: " + placeLikelihood.getPlace().getName() + "\n";
                if (placeLikelihood != null)
                    content += "Percent change of being there: " + (int) (placeLikelihood.getLikelihood() * 100) + "%";
                mTextView.setText(content);

                likelyPlaces.release();
            }
        });
    }

    public void displayPlacePicker() {
        if( mGoogleApiClient == null || !mGoogleApiClient.isConnected() )
            return;

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult( builder.build( this ), PLACE_PICKER_REQUEST );
        } catch ( GooglePlayServicesRepairableException e ) {
            Log.d("PlacesAPI Demo", "GooglePlayServicesRepairableException thrown");
        } catch ( GooglePlayServicesNotAvailableException e ) {
            Log.d( "PlacesAPI Demo", "GooglePlayServicesNotAvailableException thrown" );
        }
    }

    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if( requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK ) {
            displayPlace( PlacePicker.getPlace(data, this) );
        }
    }

    private void displayPlace( Place place ) {
        if( place == null )
            return;

        String content = "";
        if( !TextUtils.isEmpty( place.getName() ) ) {
            content += "Name: " + place.getName() + "\n";
        }
        if( !TextUtils.isEmpty( place.getAddress() ) ) {
            content += "Address: " + place.getAddress() + "\n";
        }
        if( !TextUtils.isEmpty( place.getPhoneNumber() ) ) {
            content += "Phone: " + place.getPhoneNumber();
        }

        mTextView.setText(content);
    }



}
