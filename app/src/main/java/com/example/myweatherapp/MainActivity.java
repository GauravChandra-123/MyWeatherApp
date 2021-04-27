package com.example.myweatherapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {

    final String APP_ID = "39a86427e2c2af79f519acd431f888bf";
    final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    final long MIN_TIME = 5000;
    final float MIN_DISTANCE = 1000;
    final int REQUEST_CODE = 101;
    private  long backpressed;
    static final int TIME_INTERVAL = 2000;
    ProgressDialog progressDialog;

    String Location_Provider = LocationManager.GPS_PROVIDER;
    TextView NameofCity, weatherState, Temperature;
    ImageView weatherIcon;
    RelativeLayout cityFinder;

    LocationManager mLocationManager;
    LocationListener mLocationListner;
    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherState = findViewById(R.id.weathercondition);
        Temperature = findViewById(R.id.temperature);
        weatherIcon = findViewById(R.id.weatherIcon);
        cityFinder = findViewById(R.id.cityFinder);
        NameofCity = findViewById(R.id.cityName);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Fetching your Current Location");
        progressDialog.setMessage("Currently Fetching your Location");
        button = findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
               System.exit(0);
            }
        });

        cityFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CityFinder.class);
                startActivity(intent);

            }
        });



    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);


    }
    /* @Override
    protected void onResume() {
        super.onResume();
        getweatherForCurrentLocation();
    } */

    @Override
    protected void onResume() {
        progressDialog.show();


        super.onResume();

        Intent mIntent = getIntent();
        String city = mIntent.getStringExtra("City");
        if (city != null){
            getWeatherForNewCiy(city);

        }
        else {
            getweatherForCurrentLocation();


        }

    }

    private void getWeatherForNewCiy(String city){
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid",APP_ID);
        letsdoSomeNetworking(params);

    }



    private void getweatherForCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListner = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String Latitude = String.valueOf(location.getLatitude());
                String Longitude = String.valueOf(location.getLongitude());

                RequestParams params = new RequestParams();
                params.put("lat",Latitude);
                params.put("lon",Longitude);
                params.put("appid",APP_ID);
                letsdoSomeNetworking(params);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(Location_Provider, MIN_TIME, MIN_DISTANCE, mLocationListner);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        progressDialog.show();

        if (requestCode == REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(MainActivity.this,"Location fetched Successfully",Toast.LENGTH_SHORT).show();
                getweatherForCurrentLocation();
            }
            else
            {
                //user denied the permission
            }
        }


    }

    private void letsdoSomeNetworking(RequestParams params){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(MainActivity.this,"Data Get Success",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

                weatherData weatherD=weatherData.fromJson(response);
                updateUI(weatherD);

                //super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }
    private  void updateUI(weatherData weather){


        Temperature.setText(weather.getmTemperature());
        NameofCity.setText(weather.getmCity());
        weatherState.setText(weather.getmWeatherType());
        int resourceID=getResources().getIdentifier(weather.getmIcon(),"drawable",getPackageName());
        weatherIcon.setImageResource(resourceID);


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationManager!=null)
        {
            mLocationManager.removeUpdates(mLocationListner);
        }
    }
}
