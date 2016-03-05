package com.android.bike.janus;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.Place;

import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import MapHelperClasses.ParseJSON;
import MapHelperClasses.Route;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final String TOOLBAR_TITLE = "Janus";
    static final String TAG = MainActivity.class.getSimpleName();

    //Navigation Drawer variables
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView nvDrawer;

    private Toolbar toolbar;

    //Google Maps Api variables
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng mStartLatLng;
    private LatLng mEndLatLng;
    private LatLng mPrevLatLng;
    private LatLng end[];
    private String maneuver[];
    private LocationRequest mLocationRequest;
    private Marker marker;
    private boolean mRequestingLocationUpdates = true;
    private ArrayList<Polyline> polylines;
    private ArrayList<Route> routes;
    private int routeNum = 0;
    private boolean directionsSearched = false;
    private boolean isNewSession = true;
    private int turn = 0;

    //Metrics Variables
    private Double speed = 0.0;
    private Double distance = 0.0;
    private Double time = 0.0;
    private String rideStartLocation = "";
    private String rideEndLocation = "";

    //Google Places Api variables
    private TextView placeCompleteTextView;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;

    private ProgressDialog progressDialog;

    //Bluetooth Api variables
    private BluetoothAdapter BTAdapter;
    public static int REQUEST_BLUETOOTH = 1;
    private OutputStream outStream;
    private InputStream inStream;
    private String[] light = {"f", "l", "r", "u", "c"};

    //SharedPreferences variables
    private SharedPreferences sharedPreferences;
    //private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set a Toolbar to replace the ActionBar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(TOOLBAR_TITLE);

        sharedPreferences = getSharedPreferences(Config.JANUS_LOGIN_PREF, Context.MODE_PRIVATE);

        Boolean firstTime = sharedPreferences.getBoolean("first", true);

        if (firstTime) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        //Find the DrawerView and NavigationView
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        //Set the DrawerToggle
        drawerToggle = setDrawerToggle();
        //Tie DrawerLayout events to the ActionBarToggle
        mDrawer.setDrawerListener(drawerToggle);
        //Setup DrawerView
        setupDrawerContent(nvDrawer);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setUpMapForUI();
        mMap.getUiSettings().setMapToolbarEnabled(false);

        polylines = new ArrayList<>();
        routes = new ArrayList<>();

        if (isOnline(MainActivity.this)) {
            if (checkPlayServices()) {
                Log.i(TAG, "checkPlayServices if condition");
                buildGoogleApiClient();
                Log.i(TAG, "checkPlayServices if condition LocationRequest");
                createLocationRequest();
            }
        } else {
            Toast.makeText(this, "No internet connection found.", Toast.LENGTH_LONG).show();
        }

        // Open the autocomplete activity when the button is clicked.
        placeCompleteTextView = (TextView) findViewById(R.id.placesCompleteTextView);
        placeCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAutocompleteActivity();
            }
        });

        List<Integer> filterTypes = new ArrayList<>();
        filterTypes.add(Place.TYPE_ADMINISTRATIVE_AREA_LEVEL_2);

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (BTAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not Compatible")
                    .setMessage("Your phone does not support Bluetooth!")
                    .setPositiveButton("Exit", new Dialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else if (!BTAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_BLUETOOTH);
        } else {
            BluetoothDevice mmDevice;
            Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
            Log.i(TAG, "Paired Devices are:" + pairedDevices);
            BluetoothDevice[] devices = pairedDevices.toArray(new BluetoothDevice[pairedDevices.size()]);
            for (int i = 0; i < pairedDevices.size(); i++) {
                Log.i(TAG, "Device " + i + " : " + devices[i].getName());
            }
            if (pairedDevices.size() > 0) {
                for (int i = 0; i < pairedDevices.size(); i++) {
                    if (devices[i].getName().equals("HC-05")) {
                        Log.i(TAG, "HC05 Present!" + devices[i].getName());
                        mmDevice = devices[i];
                        connectBluetooth(mmDevice);
                        break;
                    }
                }
            }
        }
    }

    public void selectDrawerItem(MenuItem menuItem) {
        //Open an activity based on the  position of the menu item selected from the navigation drawer

        Intent intent;
        switch (menuItem.getItemId()) {
            case R.id.nav_second_fragment:
                intent = new Intent(this, RoutesActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_third_fragment:
                intent = new Intent(this, PeopleActivity.class);
                startActivity(intent);
                break;
        }

        //Highlight the selected item, update the title , and close the drawer
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        //The action bar home/up action should open or close the drawer.
        switch (id) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //Sync the toggle state after onRestoreInstanceEvent has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private ActionBarDrawerToggle setDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, R.string.drawer_open, R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                }
        );
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private boolean checkPlayServices() {
        Log.i(TAG, "checkPlayServices");
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        Config.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Please install Google Play Services to continue.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "buildApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API).build();
    }

    protected void createLocationRequest() {
        Log.i(TAG, "createLocationRequest");
        //mRequestingLocationUpdates = true;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Config.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(Config.FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        isNewSession = true;

        if (mGoogleApiClient == null) {
            if (isOnline(MainActivity.this)) {
                if (checkPlayServices()) {
                    Log.i(TAG, "checkPlayServices if condition");
                    buildGoogleApiClient();
                    Log.i(TAG, "checkPlayServices if condition LocationRequest");
                    createLocationRequest();
                }
            } else {
                Toast.makeText(this, "No internet connection found.", Toast.LENGTH_LONG).show();
            }
        }

        mGoogleApiClient.connect();

        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG, "mGoogleApiClient Connected.");
            startLocationUpdates();
        }

        if (mLastLocation != null) {
            addMarker();
        }
    }

    protected void startLocationUpdates() {
        Log.i(TAG, "requesting location update");
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Log.i(TAG, "location update requested");
        } catch (SecurityException e) {
            Log.i(TAG, "" + e);
            Log.i(TAG, "Requesting Location Error: Permission not granted by the user.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        isNewSession = true;

        if (mGoogleApiClient == null) {
            if (isOnline(MainActivity.this)) {
                if (checkPlayServices()) {
                    Log.i(TAG, "checkPlayServices if condition");
                    buildGoogleApiClient();
                    Log.i(TAG, "checkPlayServices if condition LocationRequest");
                    createLocationRequest();
                }
            } else {
                Toast.makeText(this, "No internet connection found.", Toast.LENGTH_LONG).show();
            }
        }

        mGoogleApiClient.connect();

        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG, "mGoogleApiClient Connected.");
            startLocationUpdates();
        }

        if (mLastLocation != null) {
            addMarker();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            //stopLocationUpdates();
        }
    }

    protected void stopLocationUpdates() {
        Log.i(TAG, "stopLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            //mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "onConnected callback. got a call.");
        Log.i(TAG, "Requesting Location");
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            Log.i(TAG, "Location Requested");
        } catch (SecurityException e) {
            Log.i(TAG, "" + e);
            Log.i(TAG, "Requesting Location Error: Permission not granted by the user.");
        }
        Log.i(TAG, "last location" + mLastLocation);
        if (mLastLocation != null) {
            Log.i(TAG, "Location is not null");
            Log.i(TAG, "initializing mStartLatLng");
            mStartLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            if (!directionsSearched) {
                addMarker();
            }
        }
        if (mRequestingLocationUpdates) {
            Log.i(TAG, "going to startLocationUpdates");
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged Callback");
        mLastLocation = location;
        //sendHello();
        if (mLastLocation != null) {
            Log.i(TAG, "initializing mStartLatLng");
            mPrevLatLng = mStartLatLng;
            mStartLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            Log.i(TAG, "going to UpdateUI");
            if (!directionsSearched) {
                addMarker();
            } else {
                time += Config.FASTEST_INTERVAL;
                updateRouteMarkers();
                new NavigationAsyncTask().execute();
                new MetricsAsyncTask().execute();
            }
        }
    }

    private void addMarker() {
        Log.d(TAG, mLastLocation.toString());
        Log.i(TAG, "Adding Marker");

        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        if (mMap == null) {
            setUpMapForUI();
        }

        if (marker != null) {
            marker.remove();
        }

        mMap.clear();

        //MarkerOptions options = new MarkerOptions().position(latLng).title("Current Location");
        marker = mMap.addMarker(new MarkerOptions().position(latLng).title("You"));
        if (isNewSession) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            isNewSession = false;
        }
        Log.i(TAG, "Marker Added");
    }

    private void setUpMapForUI() {
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();
    }

    private void openAutocompleteActivity() {
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Log.e(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called after the autocomplete activity has finished to return its result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place Selected: " + place.getName());

                if (mLastLocation != null) {
                    Log.i(TAG, "initializing mStartLatLng");
                    mStartLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                } else {
                    Toast.makeText(this, "Could not find your current location.", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "initializing mEndLatLng");
                mEndLatLng = place.getLatLng();

                checkPlace();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e(TAG, "Error: Status = " + status.toString());
            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
            }
        }
    }

    private void checkPlace() {
        if (mStartLatLng != null && mEndLatLng != null) {
            progressDialog = ProgressDialog.show(MainActivity.this, "Processing....",
                    "Fetching the shortest route.", true);
            Log.i(TAG, "Going to Route");
            route();
        } else {
            if (mStartLatLng == null) {
                Log.i(TAG, "At least one mStartLatLng is null");
                Toast.makeText(this, "Could not find your current location.", Toast.LENGTH_SHORT).show();
            }
            if (mEndLatLng == null) {
                Log.i(TAG, "At least one mEndLatLng is null");
            }
        }
    }

    private void route() {
        Log.i(TAG, "Reached Route");
        if (mEndLatLng == null) {
            Toast.makeText(this, "Please select a destination.", Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "Going to AsyncTask");
            new RequestAsyncTask().execute();
        }
    }

    public class RequestAsyncTask extends AsyncTask<Void, Void, String> {

        private final String TAG = RequestAsyncTask.class.getSimpleName();

        ParseJSON parseJSON = new ParseJSON(mStartLatLng, mEndLatLng);

        @Override
        protected String doInBackground(Void... params) {

            Log.i(TAG, "Reached AsyncTask");
            Log.i(TAG, "Current LatLng is = " + mStartLatLng);
            Log.i(TAG, "Destination LatLng is = " + mEndLatLng);

            HttpURLConnection mUrlConnection = null;
            StringBuilder mJsonResults = new StringBuilder();

            String result;

            try {
                URL url = new URL(parseJSON.makeURL());
                mUrlConnection = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(mUrlConnection.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    mJsonResults.append(buff, 0, read);
                }

                result = mJsonResults.toString();

            } catch (MalformedURLException e) {
                Log.e(TAG, "Error processing Distance Matrix API URL");
                return null;

            } catch (IOException e) {
                System.out.println("Error connecting to Distance Matrix");
                return null;
            } finally {
                if (mUrlConnection != null) {
                    mUrlConnection.disconnect();
                }
            }

            Log.i(TAG, "Result = " + result);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "Reached onPostExecute");

            routes = parseJSON.parse(result);
            if (routes != null) {
                Log.i(TAG, "Routes is not null. Going to polyline.");
                directionsSearched = true;
                addPolylines();
            } else {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "No routes to the destination could be found.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addPolylines() {
        Log.i(TAG, "Reached addPolyLine");

        int i, j, size = routes.size();
        int polylineColor = R.color.colorPrimary;

        addRouteMarkers();

        if (polylines.size() > 0) {
            Log.i(TAG, "Removing polyline");
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.

        for (i = 0; i < size; i++) {
            for (j = 0; j < i; j++) {
                if ((routes.get(i).getDistanceValue() < routes.get(j).getDistanceValue()) &&
                        routes.get(i).getDurationValue() < routes.get(j).getDurationValue()) {
                    routeNum = i;
                } else if ((routes.get(i).getDistanceValue() < routes.get(j).getDistanceValue()) &&
                        routes.get(i).getDurationValue() > routes.get(j).getDurationValue()) {
                    routeNum = i;
                }
            }
        }

        rideStartLocation = routes.get(routeNum).getStartAddressText();
        rideEndLocation = routes.get(routeNum).getEndAddressText();

        end = new LatLng[routes.get(routeNum).getEndLocation().size()];
        maneuver = new String[routes.get(routeNum).getManeuver().size()];

        for(i = 0; i < routes.get(routeNum).getEndLocation().size(); i++){
            end[i] = routes.get(routeNum).getEndLocation().get(i);
        }

        Log.i(TAG, "End locations are: "+end.toString());

        for(i = 0; i < routes.get(routeNum).getManeuver().size(); i++){
            maneuver[i] = routes.get(routeNum).getManeuver().get(i);
        }

        Log.i(TAG, "Maneuvers are: " + maneuver);

        Log.i(TAG, "Adding Polyline");

        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(getResources().getColor(polylineColor));
        polyOptions.width(10 + i * 3);
        polyOptions.addAll(routes.get(routeNum).getPoints());
        Polyline polyline = mMap.addPolyline(polyOptions);
        polylines.add(polyline);

        progressDialog.dismiss();
    }

    private void addRouteMarkers() {
        Log.d(TAG, mStartLatLng.toString());
        Log.d(TAG, mEndLatLng.toString());
        Log.i(TAG, "Adding route markers");

        LatLngBounds mRouteLatLngBounds = routes.get(routeNum).getLatLgnBounds();

        if (mMap == null) {
            setUpMapForUI();
        }
        if (marker != null) {
            marker.remove();
        }

        mMap.clear();

        //MarkerOptions options = new MarkerOptions().position(latLng).title("Current Location");
        marker = mMap.addMarker(new MarkerOptions().position(mStartLatLng).title("You"));
        marker = mMap.addMarker(new MarkerOptions().position(mEndLatLng).title("Destination"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mRouteLatLngBounds.getCenter(), 13));
        Log.i(TAG, "Route markers added");
    }

    private void updateRouteMarkers() {
        mStartLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        Log.d(TAG, mStartLatLng.toString());
        Log.d(TAG, mEndLatLng.toString());
        Log.i(TAG, "Updating route markers");

        //LatLngBounds mRouteLatLngBounds = routes.get(0).getLatLgnBounds();

        if (mMap == null) {
            setUpMapForUI();
        }
        if (marker != null) {
            marker.remove();
        }

        //mMap.clear();

        //MarkerOptions options = new MarkerOptions().position(latLng).title("Current Location");
        marker = mMap.addMarker(new MarkerOptions().position(mStartLatLng).title("You"));
        marker = mMap.addMarker(new MarkerOptions().position(mEndLatLng).title("Destination"));
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mRouteLatLngBounds.getCenter(), 13));
        Log.i(TAG, "Route markers updated");
    }

    public class NavigationAsyncTask extends AsyncTask<Void, Void, String> {

        private final String TAG = RequestAsyncTask.class.getSimpleName();

        ParseJSON parseJSON = new ParseJSON(mStartLatLng, end[turn]);

        @Override
        protected String doInBackground(Void... params) {

            Log.i(TAG, "Reached AsyncTask");
            Log.i(TAG, "Current LatLng is = " + mStartLatLng);
            Log.i(TAG, "Destination LatLng is = " + end[turn]);

            HttpURLConnection mUrlConnection = null;
            StringBuilder mJsonResults = new StringBuilder();

            String result;

            try {
                URL url = new URL(parseJSON.makeURL());
                mUrlConnection = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(mUrlConnection.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    mJsonResults.append(buff, 0, read);
                }

                result = mJsonResults.toString();

            } catch (MalformedURLException e) {
                Log.e(TAG, "Error processing Distance Matrix API URL");
                return null;

            } catch (IOException e) {
                System.out.println("Error connecting to Distance Matrix");
                return null;
            } finally {
                if (mUrlConnection != null) {
                    mUrlConnection.disconnect();
                }
            }

            Log.i(TAG, "Result = " + result);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "Reached onPostExecute");

            routes = parseJSON.parse(result);
            if (routes != null) {
                Log.i(TAG, "Routes is not null. Updating navigation.");

                int i, j, n = 0, size = routes.size();

                for (i = 0; i < size; i++) {
                    for (j = 0; j < i; j++) {
                        if ((routes.get(i).getDistanceValue() < routes.get(j).getDistanceValue()) &&
                                routes.get(i).getDurationValue() < routes.get(j).getDurationValue()) {
                            n = i;
                        } else if ((routes.get(i).getDistanceValue() < routes.get(j).getDistanceValue()) &&
                                routes.get(i).getDurationValue() > routes.get(j).getDurationValue()) {
                            n = i;
                        }
                    }
                }

                if(routes.get(n).getDistanceValue() < 2){
                    if(turn < maneuver.length-1) {
                        turn++;
                    }
                }
                else if (routes.get(n).getDistanceValue() < 5) {
                    String direction[] = maneuver[turn].split("-");
                    Log.i(TAG, " maneuver = " + maneuver[turn]);

                    if (direction[direction.length - 1].equals("left")) {
                        sendLeft();
                        Log.i(TAG, "Turn Left!");
                        Toast.makeText(getApplicationContext(), "Turn Left!", Toast.LENGTH_SHORT).show();
                    } else if (direction[direction.length - 1].equals("right")) {
                        sendRight();
                        Log.i(TAG, "Turn Right!");
                        Toast.makeText(getApplicationContext(), "Turn Right!", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    sendStraight();
                    Log.e(TAG, "Head Straight!");
                    Toast.makeText(getApplicationContext(), "Head Straight!", Toast.LENGTH_SHORT).show();
                }
            } else {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "No routes to the destination could be found.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class MetricsAsyncTask extends AsyncTask<Void, Void, String> {

        private final String TAG = MetricsAsyncTask.class.getSimpleName();

        ParseJSON parseJSON = new ParseJSON(mPrevLatLng, mStartLatLng);

        @Override
        protected String doInBackground(Void... params) {

            Log.i(TAG, "Reached AsyncTask");
            Log.i(TAG, "Current LatLng is = " + mStartLatLng);
            Log.i(TAG, "Destination LatLng is = " + end[turn]);

            HttpURLConnection mUrlConnection = null;
            StringBuilder mJsonResults = new StringBuilder();

            String result;

            try {
                URL url = new URL(parseJSON.makeURL());
                mUrlConnection = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(mUrlConnection.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    mJsonResults.append(buff, 0, read);
                }

                result = mJsonResults.toString();

            } catch (MalformedURLException e) {
                Log.e(TAG, "Error processing Distance Matrix API URL");
                return null;

            } catch (IOException e) {
                System.out.println("Error connecting to Distance Matrix");
                return null;
            } finally {
                if (mUrlConnection != null) {
                    mUrlConnection.disconnect();
                }
            }

            Log.i(TAG, "Result = " + result);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "Reached onPostExecute");

            try {
                final JSONObject json = new JSONObject(result);
                JSONArray jsonRoutes = json.getJSONArray("routes");
                JSONObject jsonRoute = jsonRoutes.getJSONObject(0);
                final JSONObject leg = jsonRoute.getJSONArray("legs").getJSONObject(0);

                rideEndLocation = leg.getString("end_address");
                distance += leg.getJSONObject("distance").getDouble("value");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopNavigation(){
        directionsSearched = false;
        speed = distance/time;
        distance /= 1000;
        time /= 60.0;
        SendDataAsyncTask asyncTask = new SendDataAsyncTask();
        asyncTask.execute();
    }

    public class SendDataAsyncTask extends AsyncTask<Void, Void, String> {

        private final String TAG = SendDataAsyncTask.class.getSimpleName();

        @Override
        protected String doInBackground(Void... params) {

            Log.i(TAG, "Reached RegisterAsyncTask");

            String http = "http://janus-59642.onmodulus.net/pushactivity";
            HttpURLConnection mUrlConnection = null;

            String response = "";

            try {
                URL url = new URL(http);

                String urlParams = encodeString();
                mUrlConnection = (HttpURLConnection) url.openConnection();
                mUrlConnection.setReadTimeout(15000);
                mUrlConnection.setConnectTimeout(15000);
                mUrlConnection.setRequestMethod("POST");
                mUrlConnection.setDoInput(true);
                mUrlConnection.setDoOutput(true);
                mUrlConnection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                mUrlConnection.setRequestProperty("Content-Length", "" +
                        Integer.toString(urlParams.getBytes().length));

                OutputStream os = mUrlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(urlParams);

                writer.flush();
                writer.close();
                os.close();

                int responseCode = mUrlConnection.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    Log.e(TAG, "Good response from server: "+responseCode);
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(mUrlConnection.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response += line;
                    }
                }
                else {
                    Log.e(TAG, "Bad response from server: "+responseCode);
                    response="";
                }

            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException: "+e);
                return null;

            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e);
                return null;
            } finally {
                if (mUrlConnection != null) {
                    mUrlConnection.disconnect();
                }
            }

            Log.i(TAG, "Response = " + response);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "Reached onPostExecute");
        }
    }

    private String encodeString() throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        result.append("&");

        result.append("email=");
        result.append(URLEncoder.encode("", "UTF-8"));
        result.append("&activity=");
        result.append(URLEncoder.encode(rideEndLocation, "UTF-8"));

        Log.i(TAG, "Result URL=" + result.toString());

        return result.toString();
    }

    private void connectBluetooth(BluetoothDevice mmDevice) {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        BluetoothSocket mmSocket = null;
        try {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            BTAdapter.cancelDiscovery();
            mmSocket.connect();
            sendConnected();
            Log.i(TAG, "Socket Connected");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outStream = mmSocket.getOutputStream();
            Log.i(TAG, "outStream Connected");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inStream = mmSocket.getInputStream();
            Log.i(TAG, "inStream Connected");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendStraight(){
        try {
            outStream.write(light[0].getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendLeft(){
        try {
            outStream.write(light[1].getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRight(){
        try {
            outStream.write(light[2].getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendUturn(){
        try {
            outStream.write(light[3].getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendConnected(){
        try {
            outStream.write(light[3].getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}