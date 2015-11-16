package location.weather;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class MainActivity  extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private TextView cityField;
    private TextView dateField;
    private TextView temperatureField;
    private TextView humidityField;
    private TextView pressureField;
    private TextView windSpeedField;
    private TextView conditionField;
    private ImageView iconField;
    GoogleApiClient mGoogleApiClient;
    LatLng latLng;
    GoogleMap mGoogleMap;
    SupportMapFragment mFragment;
    LocationRequest mLocationRequest;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mGoogleMap = mFragment.getMap();
        mGoogleMap.setMyLocationEnabled(true);

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        mLocationRequest= new LocationRequest();

        cityField = (TextView) findViewById(R.id.city);
        dateField = (TextView) findViewById(R.id.date);
        temperatureField = (TextView) findViewById(R.id.temperature);
        humidityField = (TextView) findViewById(R.id.humidity);
        pressureField = (TextView) findViewById(R.id.pressure);
        windSpeedField = (TextView) findViewById(R.id.windSpeed);
        conditionField = (TextView) findViewById(R.id.condition);
        iconField = (ImageView) findViewById(R.id.icon);

    }

    protected synchronized void buildGoogleApiClient() {
        Toast.makeText(this,"buildGoogleApiClient",Toast.LENGTH_SHORT).show();
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            //place marker at current position
            mGoogleMap.clear();
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(50.464816, 30.483162));
            //place marker at current position
            //markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_marker));
            Marker m = mGoogleMap.addMarker(markerOptions);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.464816, 30.483162), 18));
            //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 18));

            String lat = String.valueOf(mLastLocation.getLatitude());
            String lng = String.valueOf(mLastLocation.getLongitude());
            GetJsonWeather weather = new GetJsonWeather();
            weather.execute(new String[]{lat, lng});

        } else {
        cityField.setText("Location not available");
        }

        mLocationRequest.setInterval(1);
        mLocationRequest.setFastestInterval(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }


    public void onLocationChanged(Location location) {


        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            //place marker at current position
            mGoogleMap.clear();
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(50.464816, 30.483162));
            //place marker at current position
            //markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_marker));
            Marker m = mGoogleMap.addMarker(markerOptions);

            String lat = String.valueOf(mLastLocation.getLatitude());
            String lng = String.valueOf(mLastLocation.getLongitude());
            GetJsonWeather weather = new GetJsonWeather();
            weather.execute(new String[]{lat, lng});

        } else {
            cityField.setText("Location not available");

        }

        mLocationRequest.setInterval(1);
        mLocationRequest.setFastestInterval(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }


    @Override
    public void onConnectionSuspended(int i) {


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private class GetJsonWeather extends AsyncTask<String, Void, String> {

        private String cityName;
        private String cityCountry;
        private String temperature;
        private String temperatureUnits;
        private String humidity;
        private String humidityUnits;
        private String pressure;
        private String pressureUnits;
        private String windSpeed;
        private String windSpeedUnits;
        private String condition;
        private String date;
        private String icon;



        @Override
        protected String doInBackground(final String... params) {

            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpGet httpget = new HttpGet("http://api.openweathermap.org/data/2.5/weather?lat=50.464816&lon=30.483162");
           // HttpGet httpget = new HttpGet("http://api.openweathermap.org/data/2.5/weather?lat=" +  params[0] + "&lon=" + params[1]);
            httpget.setHeader("x-api-key", getString(R.string.open_weather_maps_app_id));

            InputStream inputStream = null;
            String result = null;
            try {
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();
                // json is UTF-8 by default
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                result = sb.toString();
            } catch (Exception e) {

            } finally {
                try {
                    if (inputStream != null) inputStream.close();
                } catch (Exception squish) {
                }
            }

            try{

               JSONObject rootObj = new JSONObject(result);

               JSONObject jsonChild = rootObj.getJSONObject("coord");
               double lon = jsonChild.getDouble("lon");
               double lat = jsonChild.getDouble("lat");

                JSONObject sysObj = rootObj.getJSONObject("sys");
                cityCountry = sysObj.getString("country");
                cityName = rootObj.getString("name");

                //main child
                JSONObject  mainObj = rootObj.getJSONObject("main");
                double temp = (int) mainObj.getDouble("temp");
                if (params[0] != (new String("Celsius"))) {
                    temperature = String.valueOf(temp / 100);
                    temperatureUnits = getResources().
                            getString(R.string.tempUnitsCelsius);
                } else {
                    temperature = String.valueOf(temp/100 * 9 / 5 + 32);
                    temperatureUnits = getResources().
                            getString(R.string.tempUnitsFarhenheit);
                }
                humidity = String.valueOf(mainObj.getInt("humidity"));
                humidityUnits = getResources().
                        getString(R.string.unitsPercent);

                pressure = String.valueOf(mainObj.getInt("pressure"));
                pressureUnits = getResources().
                        getString(R.string.pressureUnitsHpa);
                //wind child
                JSONObject  windObj = rootObj.getJSONObject("wind");
                windSpeed = String.valueOf(windObj.getInt("speed"));
                windSpeedUnits = getResources().
                        getString(R.string.windSpeedUnitsMps);

                JSONArray jsonArrayWeather = rootObj.getJSONArray("weather");
                JSONObject jsonWeather = jsonArrayWeather.getJSONObject(0);
                condition = jsonWeather.getString("description");
                icon = jsonWeather.getString("icon");

            } catch (JSONException e1) {
                e1.printStackTrace();
            }

            SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy", Locale.US);
            date = df.format(Calendar.getInstance().getTime());

            return null;

        }


        @Override
        protected void onPostExecute(final String result) {
            dateField.setText(date);
            cityField.setText(cityName +" "+ cityCountry );
            temperatureField.setText(temperature+" "+temperatureUnits);
            humidityField.setText("Humidity"+"-"+humidity+" "+humidityUnits);
            pressureField.setText("Pressure"+"-"+pressure+" "+pressureUnits);
            windSpeedField.setText("Wind speed"+"-"+windSpeed+" "+windSpeedUnits);
            conditionField.setText(condition);

            try{
                URL icon_url = new URL("http://openweathermap.org/img/w/" + icon + ".png");
                Bitmap icon = BitmapFactory.decodeStream(icon_url.openConnection().getInputStream());
                iconField.setImageBitmap(icon);
            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}

