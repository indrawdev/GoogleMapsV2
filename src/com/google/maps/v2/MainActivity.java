package com.google.maps.v2;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener {

	// LogCat tag
	private static final String TAG = MainActivity.class.getSimpleName();
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
	private Location mLastLocation;

	// Google client to interact with Google API
	private GoogleApiClient mGoogleApiClient;

	// Boolean flag to Google periodic location updates
	private boolean mRequestingLocationUpdates = false;

	private LocationRequest mLocationRequest;

	// Location update intervals in second
	private static int UPDATE_INTERVAL = 10000; // 10 sec
	private static int FASTEST_INTERVAL = 5000; // 5 sec
	private static int DISPLACEMENT = 10; // 10 meter

	// UI elements
	private TextView lblLocation;
	private Button btnShowLocation, btnStartLocationUpdate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		lblLocation = (TextView) findViewById(R.id.lblLocation);
		btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
		btnStartLocationUpdate = (Button) findViewById(R.id.btnLocationUpdates);

		// first we need to check availability of play services
		if (checkPlayServices()) {
			// building the google API client
			buildGoogleApiClient();

			createLocationRequest();
		}

		// show location button click listener
		// and
		// toggling the periodic location updates
		btnShowLocation.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				displayLocation();
				togglePeriodicLocationUpdates();
			}
		});

	}

	protected void togglePeriodicLocationUpdates() {
		if (!mRequestingLocationUpdates) {
			// change the button text
			btnStartLocationUpdate
					.setText(getString(R.string.btn_stop_location_updates));
			mRequestingLocationUpdates = true;

			// starting the location updates
			startLocationUpdates();
			Log.d(TAG, "Periodic location updates started!");
		} else {
			// change the button text
			btnStartLocationUpdate
					.setText(getString(R.string.btn_start_location_updates));
			mRequestingLocationUpdates = false;

			// stopping the location updates
			stopLocationUpdates();
			Log.d(TAG, "Periodic location updates stopped!");
		}

	}

	private void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
	}

	// method to display the location on UI
	protected void displayLocation() {
		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);

		if (mLastLocation != null) {
			double latitude = mLastLocation.getLatitude();
			double longtitude = mLastLocation.getLongitude();

			lblLocation.setText(latitude + ", " + longtitude);
		} else {
			lblLocation
					.setText("Couldn't get the location. Make sure location is enabled on the device");
		}

	}

	// creating google api client object
	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}

	// method to verify google play services on the device
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Toast.makeText(getApplicationContext(),
						"This device is not supported", Toast.LENGTH_LONG)
						.show();
				finish();
			}
			return false;
		}
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mGoogleApiClient != null) {
			mGoogleApiClient.connect();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkPlayServices();

		// resuming the periodic location updates
		if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
			startLocationUpdates();
		}
	}

	private void startLocationUpdates() {
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopLocationUpdates();
	}

	private void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(
				mGoogleApiClient, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
				+ result.getErrorCode());
	}

	@Override
	public void onConnected(Bundle arg0) {
		// once connected with google api, get the location
		displayLocation();

		if (mRequestingLocationUpdates) {
			startLocationUpdates();
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
	}

	@Override
	public void onLocationChanged(Location location) {
		// assign the new location
		mLastLocation = location;
		Toast.makeText(getApplicationContext(), "Location changed!",
				Toast.LENGTH_LONG).show();
		// displaying the new location on UI
		displayLocation();
	}
}
