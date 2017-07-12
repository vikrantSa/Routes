package in.vikrant.routes.views;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.vikrant.routes.R;
import in.vikrant.routes.api.ApiClient;
import in.vikrant.routes.api.ApiInterface;
import in.vikrant.routes.models.GetDirection;
import in.vikrant.routes.models.Route;
import in.vikrant.routes.models.Step;
import in.vikrant.routes.utils.DirectionUtils;
import in.vikrant.routes.widget.BottomSheetListView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnPolylineClickListener {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private GoogleApiClient mGoogleApiClient;

    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int ACTIVITY_RESULT_FOR_START = 11;
    private static final int ACTIVITY_RESULT_FOR_DESTINATION = 22;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private final LatLng mDefaultLocation = new LatLng(12.8992071, 77.6582388);
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;

    @BindView(R.id.tv_start)
    TextView tv_start;
    @BindView(R.id.tv_destination)
    TextView tv_destination;
    private Marker startMarker;
    private Marker destinationMarker;
    private LatLng startLatLng, destinationLatLng;
    private List<Polyline> polylines;
    private List<Route> routes;
    private ArrayList<Spanned> directions;
    private BottomSheetBehavior behavior;
    private ArrayAdapter<Spanned> listAdapter;


    @OnClick(R.id.tv_destination)
    void getDestinationLocation() {
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setCountry("IN").build();
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .setFilter(typeFilter)
                            .build(MapsActivity.this);
            startActivityForResult(intent, ACTIVITY_RESULT_FOR_DESTINATION);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @OnClick(R.id.tv_start)
    public void getStartLocation() {
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setCountry("IN").build();
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .setFilter(typeFilter)
                            .build(MapsActivity.this);
            startActivityForResult(intent, ACTIVITY_RESULT_FOR_START);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_maps);

        ButterKnife.bind(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();


        View bottomSheet = findViewById(R.id.design_bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        polylines = new ArrayList<>();
        routes = new ArrayList<>();

        BottomSheetListView listView = (BottomSheetListView) bottomSheet.findViewById(R.id.listView);
        directions = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, R.layout.row_direction, R.id.text,directions);
        listView.setAdapter(listAdapter);

    }


    // A place has been received; use requestCode to track the request.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_RESULT_FOR_START) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                startLatLng = place.getLatLng();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, DEFAULT_ZOOM));
                if (startMarker != null)
                    startMarker.remove();
                startMarker = mMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_ORANGE)));
                tv_start.setText(place.getAddress());
                drawMapRoute();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                // TODO: Handle the error.
            } else if (resultCode == RESULT_CANCELED) {
                // TODO: Handle the error.
            }
        } else if (requestCode == ACTIVITY_RESULT_FOR_DESTINATION) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                destinationLatLng = place.getLatLng();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, DEFAULT_ZOOM));
                if (destinationMarker != null)
                    destinationMarker.remove();
                destinationMarker = mMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_ORANGE)));
                tv_destination.setText(place.getAddress());
                drawMapRoute();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                // TODO: Handle the error.
            } else if (resultCode == RESULT_CANCELED) {
                // TODO: Handle the error.
            }
        }
    }

    private void drawMapRoute() {
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        if (startLatLng == null)
            return;
        else if (destinationLatLng == null)
            return;
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetDirection> call = apiService.getDirection(
                String.valueOf(new StringBuilder().append(startLatLng.latitude).append(",").append(startLatLng.longitude)),
                String.valueOf(new StringBuilder().append(destinationLatLng.latitude).append(",").append(destinationLatLng.longitude)),
                "true",
                getString(R.string.google_maps_key));
        call.enqueue(new Callback<GetDirection>() {
            @Override
            public void onResponse(Call<GetDirection> call, Response<GetDirection> response) {
                if (response.isSuccessful()) {
                    for (Polyline polyline : polylines) {
                        polyline.remove();
                    }
                    routes.clear();
                    routes.addAll(response.body().getRoutes());
                    int counter = 0;
                    for (Route route : routes) {
                        List<LatLng> line = DirectionUtils.decode(route.getOverview_polyline().getPoints());
                        Polyline polyline = mMap.addPolyline(new PolylineOptions()
                                .addAll(line)
                                .clickable(true)
                                .color(Color.BLACK));
                        polyline.setTag(counter);
                        polylines.add(polyline);
                        LatLng southwest = new LatLng(route.getBounds().getSouthwest().getLat(), route.getBounds().getSouthwest().getLng());
                        LatLng northeast = new LatLng(route.getBounds().getNortheast().getLat(), route.getBounds().getNortheast().getLng());
                        LatLngBounds latLngBounds = new LatLngBounds(southwest, northeast);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 200));
                        mCameraPosition = mMap.getCameraPosition();
                        counter++;

                    }
                    showRoute(0);
                }
            }

            @Override
            public void onFailure(Call<GetDirection> call, Throwable t) {
                Log.i(TAG, "onFailure: ");
            }
        });
    }

    private void showRoute(int routeNo) {
        if (routes.size() == 0){
            directions.clear();
            directions.add(new SpannableString("No routes Found"));
            listAdapter.notifyDataSetChanged();
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }
        directions.clear();
        Route route = routes.get(routeNo);
        List<Step> steps = route.getLegs().get(0).getSteps();
        for (Step step : steps) {
            directions.add(Html.fromHtml(step.getHtml_instructions()));
        }
        listAdapter.notifyDataSetChanged();
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Builds the map when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Play services connection suspended");
    }


    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        try {
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_map));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        getDeviceLocation();
        updateLocationUI();
        mMap.setOnPolylineClickListener(this);
    }


    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }
        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }


    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            /*ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);*/
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        int counter = -1;
        counter = (int) polyline.getTag();
        if (counter != -1) {
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            showRoute(counter);
        }
    }

}