package com.example.venkateshwaran.nittmap;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    public ArrayList<String> cat = new ArrayList<String>(100);
    public ArrayList<Float> lat = new ArrayList<Float>(100);
    public ArrayList<Float> lon = new ArrayList<Float>(100);

    GoogleApiClient mGoogleApiClient;
    Polyline line;
    float latitudeSum = 0, longitudeSum = 0;
    double currentLocationLatitude, currentLocationLongitude;
    boolean isDirectionLinePresent = false;

    JSONObject getLocation(String locationName, double latitude, double longitude) throws JSONException {
        JSONObject location = new JSONObject();
        location.put("name", locationName);
        location.put("latitude", latitude);
        location.put("longitude", longitude);

        return location;
    }

    public JSONObject getCategoryMap() throws JSONException {

        JSONArray messes = new JSONArray();
        messes.put(getLocation("Mega Mess 1", 10.766062, 78.815327));
        messes.put(getLocation("Mega Mess 2", 10.764668, 78.812444));

        JSONArray hostels = new JSONArray();
        hostels.put(getLocation("Amber", 10.768198, 78.813603));
        hostels.put(getLocation("Garnet", 10.763237, 78.811625));
        hostels.put(getLocation("Zircon", 10.766765, 78.817509));
        hostels.put(getLocation("Diamond", 10.763740, 78.814451));

        JSONArray departments = new JSONArray();
        departments.put(getLocation("Computer Science", 10.760196, 78.818233));
        departments.put(getLocation("Civil", 10.759146, 78.817208));

        JSONArray offices = new JSONArray();
        offices.put(getLocation("Administrative office", 10.759003, 78.813133));
        offices.put(getLocation("Hostel office", 10.762462, 78.814221));

        JSONArray shops = new JSONArray();
        shops.put(getLocation("Restaurant", 10.761485, 78.818890));
        shops.put(getLocation("Fresh Juice Shop", 10.761559, 78.818654));

        JSONObject categories = new JSONObject();
        categories.put("Mess", messes);
        categories.put("Hostels", hostels);
        categories.put("Departments", departments);
        categories.put("Offices", offices);
        categories.put("Shops", shops);

        return categories;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        setUpMapIfNeeded();
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.app_bar, null);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        final EditText mTitleTextView = mCustomView.findViewById(R.id.editText);

        mTitleTextView.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        int s = 1;
                        String ur = mTitleTextView.getText().toString();
                        for (int i = 0; i < cat.size(); i++) {
                            if (cat.get(i).equals(ur)) {
                                LatLng latLng = new LatLng(lat.get(i), lon.get(i));
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
                                mMap.animateCamera(cameraUpdate);
                                s = 0;
                            }
                        }
                        if (s == 1) {
                            Toast toast = Toast.makeText(getApplicationContext(), ur + " not found ", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker arg0) {
                final Toast toast = Toast.makeText(getApplicationContext(), arg0.getTitle(), Toast.LENGTH_SHORT);
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                    }
                }, 1500);

               /*  if (isDirectionLinePresent) {
                    line.remove();
                }
                isDirectionLinePresent = true;
                LatLng origin= new LatLng(arg0.getPosition().latitude,arg0.getPosition().longitude);
                LatLng dest=new LatLng(currentLocationLatitude,currentLocationLongitude);
                String url = getDirectionsUrl(origin, dest);
                fetchRoutesTask routesTask = new fetchRoutesTask();
                routesTask.execute(url);*/

                return true;
            }

        });

        //getCategoryMapAsyncTask task =new getCategoryMapAsyncTask();
        //task.execute("https://spider.nitt.edu/lateral/appdev/coordinates?category="+message);
        Bundle bundle = getIntent().getExtras();
        String category = bundle.getString("category");
        JSONArray locations = new JSONArray();
        try {
            locations = getCategoryMap().getJSONArray(category);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int locationCount = 0; locationCount < locations.length(); locationCount++) {
            JSONObject locationObject = new JSONObject();
            try {
                locationObject = locations.getJSONObject(locationCount);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String locationName = locationObject.optString("name").toString();
            float latitude = Float.parseFloat(locationObject.optString("latitude"));
            float longitude = Float.parseFloat(locationObject.optString("longitude"));
            latitudeSum += latitude;
            longitudeSum += longitude;
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(locationName));
            cat.add(locationName);
            lat.add(latitude);
            lon.add(longitude);
        }
        longitudeSum = longitudeSum / locations.length();
        latitudeSum = latitudeSum / locations.length();
        LatLng latLng = new LatLng(latitudeSum, longitudeSum);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mMap.animateCamera(cameraUpdate);
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            currentLocationLatitude = mLastLocation.getLatitude();
            currentLocationLongitude = mLastLocation.getLongitude();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                //setUpMap();
            }
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();

        } catch (Exception e) {
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    class getCategoryMapAsyncTask extends AsyncTask<String, String, Void> {

        private ProgressDialog progressDialog = new ProgressDialog(MapsActivity.this);
        InputStream inputStream = null;
        String result = "";

        protected void onPreExecute() {
            progressDialog.setMessage("Downloading location co-ordinates...");
            progressDialog.show();

        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                URL urlObj = new URL(params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
                inputStream = urlConnection.getInputStream();
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (IllegalStateException e3) {
                Log.e("IllegalStateException", e3.toString());
                e3.printStackTrace();
            } catch (IOException e4) {
                Log.e("IOException", e4.toString());
                e4.printStackTrace();
            }

            try {
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
                StringBuilder sBuilder = new StringBuilder();

                String line = null;
                while ((line = bReader.readLine()) != null) {
                    sBuilder.append(line + "\n");
                }

                inputStream.close();
                result = sBuilder.toString();


            } catch (Exception e) {

            }
            return null;
        }

        protected void onPostExecute(Void v) {


            try {
                JSONArray locations = new JSONArray(result);
                for (int locationCount = 0; locationCount < locations.length(); locationCount++) {
                    JSONObject locationObject = new JSONObject();
                    try {
                        locationObject = locations.getJSONObject(locationCount);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String locationName = locationObject.optString("name").toString();
                    float latitude = Float.parseFloat(locationObject.optString("latitude"));
                    float longitude = Float.parseFloat(locationObject.optString("longitude"));
                    latitudeSum += latitude;
                    longitudeSum += longitude;
                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(locationName));
                    cat.add(locationName);
                    lat.add(latitude);
                    lon.add(longitude);
                }
                longitudeSum = longitudeSum / locations.length();
                latitudeSum = latitudeSum / locations.length();
                LatLng latLng = new LatLng(latitudeSum, longitudeSum);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                mMap.animateCamera(cameraUpdate);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }


        }
    }

    private class fetchRoutesTask extends AsyncTask<String, Void, String> {
        private ProgressDialog progressDialog = new ProgressDialog(MapsActivity.this);

        protected void onPreExecute() {
            progressDialog.setMessage("Fetching routes...");
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... url) {
            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);
            }
            line = mMap.addPolyline(lineOptions);
        }
    }

    public class DirectionsJSONParser {


        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;

            try {
                jRoutes = jObject.getJSONArray("routes");
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<HashMap<String, String>>();
                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                                hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
            }

            return routes;
        }

        private List<LatLng> decodePoly(String encoded) {
            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;
            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;
                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }
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
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}