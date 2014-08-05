package com.football.app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.football.app.R;
import com.football.app.database.DBHelper;
import com.football.app.database.Diary;
import com.football.app.database.FifaWCLocation;
import com.football.app.util.Connectivity;
import com.football.app.util.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/*
 * BrazilWCMapActivity is the main activity for the football app 
 * It is a fragment activity and its main layout is activity_main.xml that contains a map fragment
 * in which the google map is loaded by using getMap function
 * All the UI buttons are added in the layout and this class implements multiple interfaces.
 * OnClickListener is implemented to handle the clicks for the UI button added in the activity_main.xml.
 * OnInfoWindowClickListener is the GoogleMap interface to handle the marker info window click and to get 
 * customized behavior(i-e show diaries for location)on marker info click 
 * 
 * GooglePlayServicesClient.ConnectionCallbacks and GooglePlayServicesClient.OnConnectionFailedListener
 * are implemented to get the LocationClient to get current location for the user
 * 
 * */
public class BrazilWCMapActivity extends FragmentActivity implements
		OnClickListener, OnInfoWindowClickListener, LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	static final LatLng GERMANY = new LatLng(52.518825, 13.405543);
	static final LatLng BRAZIL = new LatLng(-7.338877, -54.696126);

	private static final String SHOW = "calculate";
	private static final String HIDE = "hide";
	private GoogleMap map;
	private DBHelper dbHelper;
	private Location myLocation; // contains the current location got by gps or
									// network
	private FifaWCLocation currentMarkerLocation = null;
	private HashMap<Marker, FifaWCLocation> mMarkersHashMap;
	// get the locations from database and store it this array
	private ArrayList<FifaWCLocation> mLocationArray = new ArrayList<FifaWCLocation>();
	private LocationClient mLocationClient;
	private TextView distanceTravelledText;
	private ListView diariesListView;
	private RelativeLayout listViewLayout;
	private RelativeLayout inputMessageLayout;
	private TextView tv;
	private TextView inputDialogTitle;
	private DiariesListViewAdapter diariesListViewAdapter = null;
	private ArrayList<Diary> currentLocationDiaries = null;
	private LocationManager locationManager;
	private MapFragment mapFragment;

	private Polyline line;

	private Button calculateDistance;
	private Button hideGuide;

	private Button mapView;

	private Button terrainView;

	private Button satelliteView;

	private Button last10Wc;

	private Button closeButton;

	private Button close;

	private Button gps_button;

	private Button btn_find;

	private boolean donotTakeTapOnInfo;

	private Button closeSearchUI;

	private RelativeLayout searchLayout;

	private Button enterLocation;

	private boolean locationEntered = false;

	private LatLng locationEnteredByUser;

	private Marker locationEnteredMarker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		dbHelper = new DBHelper(this);
		dbHelper.open();

		// creating a location client to get current location of the user
		mLocationClient = new LocationClient(this, this, this);

		listViewLayout = (RelativeLayout) findViewById(R.id.list_layout);
		inputMessageLayout = (RelativeLayout) findViewById(R.id.input_layout);
		searchLayout = (RelativeLayout) findViewById(R.id.search_layout);
		diariesListView = (ListView) findViewById(R.id.diaries);

		inputDialogTitle = (TextView) findViewById(R.id.title);

		distanceTravelledText = (TextView) findViewById(R.id.distance_travelled);
		distanceTravelledText.setTextColor(Color.YELLOW);
		distanceTravelledText.setTextSize(18f);

		// adding listeners to all UI buttons
		addListenersToUIButtons();

		mMarkersHashMap = new HashMap<Marker, FifaWCLocation>();

		// getting current WC locations list from DB and add to mLocationArray
		ArrayList<FifaWCLocation> currentWCLocations = dbHelper
				.getCurrentWorldCupLocations();

		for (FifaWCLocation location : currentWCLocations) {
			mLocationArray.add(location);
		}

		// getting last 10 world cup locations list from DB and add to
		// mLocationArray
		ArrayList<FifaWCLocation> last10WCLocations = dbHelper
				.getLast10WorldCupLocations();
		for (FifaWCLocation location : last10WCLocations) {
			mLocationArray.add(location);
		}

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		int errorCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (errorCode != ConnectionResult.SUCCESS) {
			GooglePlayServicesUtil.getErrorDialog(errorCode, this, 0).show();
		}

		// setting up map
		setUpMapAndFocusToBrazil();

		// add markers on all the locations of current and last 10 FIFA WC
		addMarkersOnMap(mLocationArray);
		// add marker info window click listener to this as BrazilWCMapActivity
		// implements InfoWindowClickListener
		// to show customized behavior on click of info window
		map.setOnInfoWindowClickListener(this);

	}

	private void addListenersToUIButtons() {
		// TODO Auto-generated method stub
		mapView = (Button) findViewById(R.id.mapview);
		mapView.setOnClickListener(this);

		terrainView = (Button) findViewById(R.id.terrainview);
		terrainView.setOnClickListener(this);

		satelliteView = (Button) findViewById(R.id.satelliteview);
		satelliteView.setOnClickListener(this);

		last10Wc = (Button) findViewById(R.id.last10_wc);
		last10Wc.setOnClickListener(this);

		closeButton = (Button) findViewById(R.id.close);
		closeButton.setOnClickListener(this);

		close = (Button) findViewById(R.id.close_dialog);
		close.setOnClickListener(this);

		Button addDiaryButton = (Button) findViewById(R.id.add_more);
		addDiaryButton.setOnClickListener(this);

		Button add = (Button) findViewById(R.id.add);
		add.setOnClickListener(this);

		gps_button = (Button) findViewById(R.id.gps_button);
		gps_button.setOnClickListener(this);

		calculateDistance = (Button) findViewById(R.id.calculate_distance);
		hideGuide = (Button) findViewById(R.id.hide_guide);

		calculateDistance.setTag("calculate");
		calculateDistance.setOnClickListener(this);
		hideGuide.setOnClickListener(this);

		// Setting button click event listener for the find button
		btn_find = (Button) findViewById(R.id.btn_find);
		btn_find.setOnClickListener(this);

		closeSearchUI = (Button) findViewById(R.id.close_search_dialog);
		closeSearchUI.setOnClickListener(this);

		enterLocation = (Button) findViewById(R.id.enter_location);
		enterLocation.setOnClickListener(this);

	}

	private void enableUIButtons() {
		mapView.setEnabled(true);
		terrainView.setEnabled(true);
		satelliteView.setEnabled(true);
		gps_button.setEnabled(true);
		calculateDistance.setEnabled(true);
		hideGuide.setEnabled(true);
		last10Wc.setEnabled(true);
	}

	private void disableUIButtons() {
		mapView.setEnabled(false);
		terrainView.setEnabled(false);
		satelliteView.setEnabled(false);
		gps_button.setEnabled(false);
		calculateDistance.setEnabled(false);
		hideGuide.setEnabled(false);
		last10Wc.setEnabled(false);

	}

	/*
	 * Add markers on the map by taking location list from database
	 */
	private void addMarkersOnMap(ArrayList<FifaWCLocation> locationList) {
		MarkerOptions markerOption;
		if (locationList != null && locationList.size() > 0) {
			for (FifaWCLocation location : locationList) {
				LatLng position = new LatLng(location.getLatitude(),
						location.getLongitude());

				// check if the map location from database is not of current
				// year world cup
				// and setting the marker image accordingly
				if (location.getYear() != Constants.CURRENT_YEAR) {

					if (!location.getIcon().equals("null")) {

						markerOption = new MarkerOptions().position(position)
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.new_pointer));
					} else {
						markerOption = new MarkerOptions().position(position)
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.new_pointer));
					}

				} else {
					// for current world cup R.drawable.map_pointer is used as
					// marker image
					markerOption = new MarkerOptions().position(position).icon(
							BitmapDescriptorFactory
									.fromResource(R.drawable.map_pointer));
				}

				Marker currentMarker = map.addMarker(markerOption);
				// add the location against marker to hashmap to contain the
				// pointer to location object
				// mMarkersHashMap will be used to show custom info window for
				// each marker
				mMarkersHashMap.put(currentMarker, location);

			}

			// setting custom window adapter on the map called
			// MarkerInfoWindowAdapter
			map.setInfoWindowAdapter(new MarkerInfoWindowAdapter());
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.enter_location:
			if (searchLayout != null) {
				searchLayout.setVisibility(View.VISIBLE);
			}
			// Getting reference to EditText to get the user input location and
			// setting the empty text
			EditText locationToSearch = (EditText) findViewById(R.id.et_location);
			locationToSearch.setText("");
			donotTakeTapOnInfo = true; // disable tap on map marker info
			disableUIButtons(); // disable UI buttons click for showing enter
								// location dialog
			break;

		case R.id.btn_find:

			if (locationEnteredMarker != null) {
				locationEnteredMarker.remove();
				locationEnteredMarker = null;
			}
			// Getting reference to EditText to get the user input location
			EditText etLocation = (EditText) findViewById(R.id.et_location);

			// Getting user input location
			String location = etLocation.getText().toString();

			// execute the search location task using GeocoderTask
			if (location != null && !location.equals("")) {
				new GeocoderTask().execute(location);
			}

			if (searchLayout != null) {
				searchLayout.setVisibility(View.GONE);
			}
			donotTakeTapOnInfo = false;
			enableUIButtons();

			break;

		case R.id.close_search_dialog:
			if (searchLayout != null) {
				searchLayout.setVisibility(View.GONE);
			}
			donotTakeTapOnInfo = false;
			enableUIButtons();
			break;

		case R.id.gps_button:
			/*
			 * move to current user location myLocation is calculated if the
			 * user is connected to network and location settings is active on
			 * the user's device myLocation will be null if no network was
			 * available to determine the location of the user or user doesn't
			 * allow the app to access his location
			 */

			if (myLocation == null)
				return;

			CameraPosition cameraPositionToMove = new CameraPosition.Builder()
					.target(new LatLng(myLocation.getLatitude(), myLocation
							.getLongitude())) // Sets the center of the map to
					.zoom(15)// Sets the zoom
					.bearing(35).build(); // Creates a CameraPosition from the
											// builder
			map.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPositionToMove)); // move the
																// camera to the
																// myLocation

			break;

		case R.id.mapview:
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;
		case R.id.terrainview:
			map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

			break;
		case R.id.satelliteview:
			map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			break;
		case R.id.last10_wc:
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(GERMANY) // Sets the center of the map to Mountain
										// View
					.zoom(2.0F) // Sets the zoom
					.bearing(35).build(); // Creates a CameraPosition from the
											// builder
			map.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			break;
		case R.id.hide_guide:
			removeLine();
			distanceTravelledText.setText("");
			v.setTag(SHOW);
			calculateDistance.setVisibility(View.VISIBLE);
			hideGuide.setVisibility(View.GONE);
			break;
		case R.id.calculate_distance:
			calculateDistance(v);
			break;
		case R.id.close:
			if (listViewLayout != null) {
				listViewLayout.setVisibility(View.GONE);
				currentMarkerLocation = null;
			}
			if (mapFragment != null) {
				mapFragment.getView().setClickable(true);
			}
			donotTakeTapOnInfo = false;
			enableUIButtons();
			break;

		case R.id.close_dialog:
			if (listViewLayout != null) {
				listViewLayout.setVisibility(View.VISIBLE);
			}
			if (inputMessageLayout != null) {
				inputMessageLayout.setVisibility(View.GONE);
			}
			if (mapFragment != null) {
				mapFragment.getView().setClickable(true);
			}
			donotTakeTapOnInfo = false;
			enableUIButtons();
			break;
		case R.id.add_more:
			if (inputMessageLayout != null) {
				inputMessageLayout.setVisibility(View.VISIBLE);
			}
			if (listViewLayout != null) {
				listViewLayout.setVisibility(View.GONE);
			}

			// Getting reference to EditText to get the user input diary and set
			// empty text
			EditText message = (EditText) findViewById(R.id.message);
			message.setText("");

			break;
		case R.id.add:

			addToDiariesAndUpdateList();
			break;

		default:
			break;
		}
	}

	/*
	 * Calculate distance based on location provided by user or current location
	 * got by gps or location entered by the user
	 */
	private void calculateDistance(final View v) {

		double distanceInKM;
		if (locationEntered) { // if location is entered by user and marked on
								// map
			if (locationEnteredByUser != null) {
				distanceInKM = calculateDistanceInKMAndAddLineOnMap(
						locationEnteredByUser.latitude,
						locationEnteredByUser.longitude, BRAZIL.latitude,
						BRAZIL.longitude) / 1000;
				Log.i("Distance Travelled", distanceInKM + "");
				distanceTravelledText.setText(((double) Math
						.round(distanceInKM * 100) / 100) + " KM");
				v.setTag(HIDE);
				calculateDistance.setVisibility(View.GONE);
				hideGuide.setVisibility(View.VISIBLE);

			}
		} else {
			if (myLocation != null) {
				distanceInKM = calculateDistanceInKMAndAddLineOnMap(
						myLocation.getLatitude(), myLocation.getLongitude(),
						BRAZIL.latitude, BRAZIL.longitude) / 1000;
				Log.i("Distance Travelled", distanceInKM + "");
				distanceTravelledText.setText(((double) Math
						.round(distanceInKM * 100) / 100) + " KM");
				v.setTag(HIDE);
				calculateDistance.setVisibility(View.GONE);
				hideGuide.setVisibility(View.VISIBLE);

			} else {
				if (checkLocationEnabled()) {

					if (!Connectivity.isConnected(this)) {
						showInfoMessageDialog("No Connection",
								"Please turn on WIFI and try again!");
						AlertDialog.Builder builder = new AlertDialog.Builder(
								this);
						builder.setTitle("No Connection")
								.setMessage(
										"Please turn on WIFI and try again!")
								.setCancelable(false)
								.setNegativeButton("Try Again",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
											}
										});
						AlertDialog alert = builder.create();
						alert.show();
					} else {
						myLocation = mLocationClient.getLastLocation();
						if (myLocation != null) {
							distanceInKM = calculateDistanceInKMAndAddLineOnMap(
									myLocation.getLatitude(),
									myLocation.getLongitude(), BRAZIL.latitude,
									BRAZIL.longitude) / 1000;
							Log.i("Distance Travelled", distanceInKM + "");
							distanceTravelledText.setText(((double) Math
									.round(distanceInKM * 100) / 100) + " KM");
							v.setTag(HIDE);

						}
					}
				}
			}
		}

	}

	/*
	 * add new diary entry to DB and update the list view
	 */
	private void addToDiariesAndUpdateList() {

		// Getting reference to EditText to get the user input diary
		EditText message = (EditText) findViewById(R.id.message);
		String text = message.getText().toString();
		if (!text.equals("")) {
			if (message != null && currentMarkerLocation != null) {
				Date dateNow = new Date();
				// change date into string yyyyMMdd format example "20110914"
				SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat(
						"MM:dd:yyyy \n HH:mm:ss a", Locale.US);
				String date_to_string = dateformatyyyyMMdd.format(dateNow);

				Diary diary = new Diary(message.getText().toString(),
						currentMarkerLocation.getId(), date_to_string);
				int id = dbHelper.addDiary(diary);
				if (currentLocationDiaries != null) {
					currentLocationDiaries = null;
					currentLocationDiaries = dbHelper
							.getDiariesForLocation(currentMarkerLocation);
				} else {
					currentLocationDiaries = new ArrayList<Diary>();
					diary.setId(id);
					currentLocationDiaries.add(diary);
				}
				if (diariesListViewAdapter != null) {
					diariesListViewAdapter = null;
				}

				diariesListViewAdapter = new DiariesListViewAdapter(
						currentLocationDiaries);
				diariesListView.setAdapter(diariesListViewAdapter);

				diariesListView.setVisibility(View.VISIBLE);
				if (tv != null) {
					tv.setVisibility(View.GONE);
					if (listViewLayout != null) {
						listViewLayout.removeView(tv);
					}
					tv = null;
				}

			}
		}

		if (inputMessageLayout != null) {
			inputMessageLayout.setVisibility(View.GONE);
			message.setText("");
		}
		if (listViewLayout != null) {
			// TODO update the adapter
			listViewLayout.setVisibility(View.VISIBLE);
		}

	}

	@Override
	protected void onResume() {
		dbHelper.open();
		super.onResume();
		// Connect the client.
		mLocationClient.connect();
	}

	@Override
	protected void onPause() {
		dbHelper.close();
		super.onPause();
	}

	protected void onStop() {
		dbHelper.close();
		mLocationClient.disconnect();
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	/*
	 * Calculate and return the distance between two location in KM it takes
	 * current location Lat and Lon values, and destination location Lat and Lon
	 * values Also it adds a line on map from current location to destination
	 * location by calling addLine()
	 */

	private double calculateDistanceInKMAndAddLineOnMap(
			double currentLocationLat, double currentLocationLon,
			double destinationLat, double destinationLon) {

		double dLat = Math.toRadians(destinationLat - currentLocationLat);
		double dLon = Math.toRadians(destinationLon - currentLocationLon);
		currentLocationLat = Math.toRadians(currentLocationLat);
		destinationLat = Math.toRadians(destinationLat);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
				* Math.sin(dLon / 2) * Math.cos(currentLocationLat)
				* Math.cos(destinationLat);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		addLine();
		return Constants.RADIUS * c;
	}

	/*
	 * Add line on the map from Brazil to user's current location also it sets
	 * the camera to the destination location
	 */
	private void addLine() {
		if (locationEntered) {
			if (locationEnteredByUser != null) {
				line = map.addPolyline((new PolylineOptions())
						.add(new LatLng(locationEnteredByUser.latitude,
								locationEnteredByUser.longitude), BRAZIL)
						.width(5).color(Color.BLUE).geodesic(true));

				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(new LatLng(locationEnteredByUser.latitude,
								locationEnteredByUser.longitude)) // Sets the
																	// center of
																	// the map
																	// to
						.zoom(2.0f) // Sets the zoom
						.bearing(35)// Sets the tilt of the camera to 35 degrees
						.build(); // Creates a CameraPosition from the builder
				map.animateCamera(CameraUpdateFactory
						.newCameraPosition(cameraPosition));
			}

		} else {
			line = map.addPolyline((new PolylineOptions())
					.add(new LatLng(myLocation.getLatitude(), myLocation
							.getLongitude()), BRAZIL).width(5)
					.color(Color.BLUE).geodesic(true));

			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(myLocation.getLatitude(), myLocation
							.getLongitude())) // Sets the center of the map to
					.zoom(2.0f) // Sets the zoom
					.bearing(35)// Sets the tilt of the camera to 35 degrees
					.build(); // Creates a CameraPosition from the builder
			map.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
		}

	}

	/*
	 * Remove the line added for the calculated distance and animate the camera
	 * to Brazil
	 */

	private void removeLine() {
		if (line != null) {
			line.remove();
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(BRAZIL) // Sets the center of the map to Mountain
					.zoom(4.3F) // Sets the zoom
					.bearing(35).tilt(30) // Sets the tilt of the camera to 30
											// degrees
					.build(); // Creates a CameraPosition from the builder
			map.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
		}
	}

	/*
	 * Setup map, set UI settings and animate camera to Brazil location
	 */
	private void setUpMapAndFocusToBrazil() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (map == null) {
			// Try to obtain the map from the SupportMapFragment.
			mapFragment = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map));
			map = mapFragment.getMap();

			// Check if we were successful in obtaining the map.

			if (map != null) {
				map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
					@Override
					public boolean onMarkerClick(
							com.google.android.gms.maps.model.Marker marker) {
						marker.showInfoWindow(); // show customized info window
													// for marker click
						return true;
					}
				});

				// zoom buttons are disabled
				map.getUiSettings().setZoomControlsEnabled(false);
				// customize button for getting current location is added so map
				// UI button is disabled
				map.getUiSettings().setMyLocationButtonEnabled(false);

				map.setMyLocationEnabled(true);

				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(BRAZIL) // Sets the center of the map to
										// Mountain View
						.zoom(4.3F) // Sets the zoom
						.bearing(35).tilt(30) // Sets the tilt of the camera to
												// 30 degrees
						.build(); // Creates a CameraPosition from the builder
				map.animateCamera(CameraUpdateFactory
						.newCameraPosition(cameraPosition));

			} else
				Toast.makeText(getApplicationContext(),
						"Unable to create Maps", Toast.LENGTH_SHORT).show();
		}
	}

	/*
	 * Get customize view for marker info window implemented custom info window
	 * click
	 */

	public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
		public MarkerInfoWindowAdapter() {
		}

		@Override
		public View getInfoWindow(Marker marker) {
			return null;
		}

		@Override
		// get customized view for marker info window
		public View getInfoContents(Marker marker) {
			View v = getLayoutInflater().inflate(R.layout.infowindow_layout,
					null);
			if (marker.equals(locationEnteredMarker)) {
				return null;
			}

			FifaWCLocation myMarker = mMarkersHashMap.get(marker);

			TextView title = (TextView) v.findViewById(R.id.title);
			title.setTextColor(Color.BLACK);
			title.setTextSize(16f);

			TextView stadiumNameTitle = (TextView) v
					.findViewById(R.id.marker_label1);

			TextView stadiumName = (TextView) v
					.findViewById(R.id.marker_label2);

			TextView markerSnippetTitle = (TextView) v
					.findViewById(R.id.snippet_label1);

			TextView markerSnippet = (TextView) v
					.findViewById(R.id.snippet_label);

			TextView yearTitle = (TextView) v.findViewById(R.id.year_title);

			TextView year = (TextView) v.findViewById(R.id.year);

			title.setText(myMarker.getCity());

			String scotlandParticipated = (myMarker.getScotlandParticipated() == 0) ? "No"
					: "Yes";

			// setting UI items on the custom info window for current world cup
			// and last 10 world cups
			if (myMarker.getYear() != Constants.CURRENT_YEAR) {

				stadiumNameTitle.setText("Country:");
				stadiumName.setText(myMarker.getCountry());

				year.setVisibility(View.VISIBLE);
				year.setText(myMarker.getYear() + "");
				yearTitle.setVisibility(View.VISIBLE);
				yearTitle.setText("Year:");

				markerSnippetTitle.setText("Scotland Participated:");
				markerSnippet.setText(scotlandParticipated);

			} else {

				year.setVisibility(View.GONE);
				yearTitle.setVisibility(View.GONE);
				stadiumNameTitle.setText("Stadium:");
				stadiumName.setText(myMarker.getStadium());
				markerSnippetTitle.setText("Scotland Participated:");
				markerSnippet.setText(scotlandParticipated);
			}

			return v;
		}
	}

	@Override
	// customized info window click function
	public void onInfoWindowClick(Marker marker) {
		if (!donotTakeTapOnInfo) {

			if (mMarkersHashMap != null) {
				// take the location against marker from the mMarkersHashMap to
				// show the diaries enties for that location
				FifaWCLocation location = mMarkersHashMap.get(marker);
				// check if location is not null then disable UI buttons and
				// show diaries dialog for the location
				if (location != null) {
					if (mapFragment != null) {
						mapFragment.getView().setClickable(false);
					}

					donotTakeTapOnInfo = true;
					disableUIButtons();
					showDiariesForLocation(location);
				}
			}
		}

	}

	/*
	 * Show diary entries for a specific location
	 */
	private void showDiariesForLocation(final FifaWCLocation location) {

		currentMarkerLocation = location;
		inputDialogTitle.setText("" + location.getCity());
		/*
		 * get current location diaries from DB and save to
		 * currentLocationDiaries list so that the DiariesListViewAdapter takes
		 * the list and show the UI for location Diaries Also by using this
		 * array we can update the DiariesListViewAdapter when new entry is
		 * added
		 */
		currentLocationDiaries = dbHelper.getDiariesForLocation(location);
		if (currentLocationDiaries != null) {
			// show list of diaries
			diariesListViewAdapter = new DiariesListViewAdapter(
					currentLocationDiaries);
			diariesListView.setAdapter(diariesListViewAdapter);
		} else {
			/*
			 * if no diaries for the provided location found then add a new
			 * TextView saying "No Diaries" and make diariesListView invisible
			 */

			diariesListView.setVisibility(View.GONE);
			if (tv != null)
				tv = null;
			tv = new TextView(this);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
			tv.setText("No Diaries");
			listViewLayout.addView(tv, params);

		}

		listViewLayout.setVisibility(View.VISIBLE);

	}

	@Override
	public void onConnected(Bundle dataBundle) {

		myLocation = mLocationClient.getLastLocation();

	}

	//
	// /*
	// * Called by Location Services if the connection to the
	// * location client drops because of an error.
	// */
	@Override
	public void onDisconnected() {
		// Display the connection status
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}

	//
	// /*
	// * Called by Location Services if the attempt to
	// * Location Services fails.
	// */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */

		}
	}

	public class DiariesListViewAdapter extends BaseAdapter {
		private ArrayList<Diary> innerClassDiariesArray;

		public DiariesListViewAdapter(ArrayList<Diary> diaries) {
			innerClassDiariesArray = diaries;
			// sortDataList();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return innerClassDiariesArray.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View vi = convertView;
			if (convertView == null) {

				LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				vi = layoutInflater.inflate(R.layout.list_item, null);

			}

			// adding a new diary item to diaries list view
			Diary item = new Diary();
			item = innerClassDiariesArray.get(position);
			if (item != null) {
				TextView messageText = (TextView) vi
						.findViewById(R.id.messgae_item);
				TextView dateText = (TextView) vi.findViewById(R.id.date);
				TextView timeText = (TextView) vi.findViewById(R.id.time);
				messageText.setText(item.getMessage());

				String[] parts = item.getDate().split("\n");
				String date = parts[0]; // 004
				String time = parts[1]; // 034556

				dateText.setText(date);
				timeText.setText(time);
			}

			return vi;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		myLocation = location;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	/*
	 * check network connectivity and setup map if network available
	 */
	private void checkConnectivity() {
		if (!Connectivity.isConnected(this)) {
			showInfoMessageDialog("No Connection",
					"Please turn on WIFI and try again!");
		} else {

			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			int errorCode = GooglePlayServicesUtil
					.isGooglePlayServicesAvailable(this);
			if (errorCode != ConnectionResult.SUCCESS) {
				GooglePlayServicesUtil.getErrorDialog(errorCode, this, 0)
						.show();
			}

			setUpMapAndFocusToBrazil();
			addMarkersOnMap(mLocationArray);
			map.setOnInfoWindowClickListener(this);
		}
	}

	private void showInfoMessageDialog(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title)
				.setMessage(message)
				.setCancelable(false)
				.setNegativeButton("Try Again",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								checkConnectivity();
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();

	}

	/*
	 * check if location settings enabled on device to get the current location
	 * from user device. if not enabled showing a dialog that allow user to open
	 * location settings of the device
	 * locationManager.isProviderEnabled(provider) is used to get the status of
	 * the GPS_PROVIDER or NETWORK_PROVIDER
	 */
	private boolean checkLocationEnabled() {
		if (locationManager == null)
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean gps_enabled = false;
		boolean network_enabled = false;
		try {
			gps_enabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
		}
		try {
			network_enabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
		}

		if (!gps_enabled && !network_enabled) {
			Builder dialog = new AlertDialog.Builder(this);
			dialog.setMessage("location services disabled");
			dialog.setPositiveButton("Open Location Settings",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(
								DialogInterface paramDialogInterface,
								int paramInt) {
							// TODO Auto-generated method stub
							Intent myIntent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(myIntent);

						}
					});
			dialog.setNegativeButton("cancel",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(
								DialogInterface paramDialogInterface,
								int paramInt) {
							// TODO Auto-generated method stub

						}
					});
			dialog.show();
			return false;
		}
		return true;
	}

	// An AsyncTask class for accessing the GeoCoding Web Service to search for
	// a location entered
	private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

		@Override
		protected List<Address> doInBackground(String... locationName) {
			// Creating an instance of Geocoder class
			Geocoder geocoder = new Geocoder(getBaseContext());
			List<Address> addresses = null;

			try {
				// Getting a maximum of 1 Address that matches the input text
				if (Geocoder.isPresent()) {
					addresses = geocoder
							.getFromLocationName(locationName[0], 1);
				}
			} catch (Exception e) {
				// Toast.makeText(getBaseContext(), "Service not available!",
				// Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			return addresses;
		}

		@Override
		protected void onPostExecute(List<Address> addresses) {

			if (addresses == null || addresses.size() == 0) {
				Toast.makeText(getBaseContext(), "No Location found",
						Toast.LENGTH_SHORT).show();
				return;
			}

			locationEntered = true;

			// Adding Markers on Google Map for each matching address
			for (int i = 0; i < addresses.size(); i++) {

				Address address = (Address) addresses.get(i);

				// Creating an instance of GeoPoint, to display in Google Map
				locationEnteredByUser = new LatLng(address.getLatitude(),
						address.getLongitude());

				String addressText = String.format(
						"%s, %s",
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "", address
								.getCountryName());

				MarkerOptions markerOptions = new MarkerOptions();
				markerOptions.position(locationEnteredByUser);
				markerOptions.title(addressText);

				locationEnteredMarker = map.addMarker(markerOptions);

				// Locate the first location
				if (i == 0)
					map.animateCamera(CameraUpdateFactory
							.newLatLng(locationEnteredByUser));

			}
		}
	}

}
