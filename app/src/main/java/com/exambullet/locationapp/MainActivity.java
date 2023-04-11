package com.exambullet.locationapp;

import static com.exambullet.locationapp.DatabaseHelper.COLUMN_LONGITUDE;
import static com.exambullet.locationapp.DatabaseHelper.COLUMN_ODOMETER_END;
import static com.exambullet.locationapp.DatabaseHelper.COLUMN_ODOMETER_START;
import static com.exambullet.locationapp.DatabaseHelper.COLUMN_OTHER_INFO;
import static com.exambullet.locationapp.DatabaseHelper.COLUMN_TIMESTAMP;
import static com.exambullet.locationapp.DatabaseHelper.COLUMN_TRIP_NUMBER;
import static com.exambullet.locationapp.DatabaseHelper.COLUMN_VEHICLE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.exambullet.locationapp.adapters.LocationsAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  {

    RecyclerView recyclerView;

    LocationsAdapter adapter;
    List<LocationData> locationData;
    Button showAll;

    boolean current=false;

    LocationData data;
    Calendar calendar;
    Button trackTrip;

    Location pLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestLocationPermission();
        calendar=Calendar.getInstance();
        DatabaseHelper helper=new DatabaseHelper(MainActivity.this);
        recyclerView=findViewById(R.id.recyclerview);
        locationData=helper.getAllLocations();
        adapter=new LocationsAdapter(locationData,calendar);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setHasFixedSize(true);
        TextView textView=findViewById(R.id.noLocations);
        if(!helper.getAllLocations().isEmpty()) textView.setVisibility(View.GONE);
        showAll=findViewById(R.id.myButton2);
        showAll.setOnClickListener(v->{
            adapter=new LocationsAdapter(locationData,calendar);
            recyclerView.setAdapter(adapter);
            findViewById(R.id.currentLocation).setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        });
        if(locationData.isEmpty()) findViewById(R.id.progressBar).setVisibility(View.GONE);
        trackTrip=findViewById(R.id.trackTrip);
        trackTrip.setOnClickListener(this::onButtonShowPopupWindowClick);
    }
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Explain why the app needs access to the user's locationdfsdfs
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Permission");
            builder.setMessage("This app needs access to your location to function properly.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_PERMISSION);
                }
            });
            builder.create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    private void requestReadPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Explain why the app needs access to the user's locationdfsdfs
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Storage Permission");
            builder.setMessage("This app needs access to your storage to export csv file.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_LOCATION_PERMISSION);
                }
            });
            builder.create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with getting the user's location
                getUserLocation();
            } else {
                // Permission denied, handle accordingly
            }
        }else if(requestCode==READ_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with getting the user's location
                exportDatabaseToCsv();
            } else {
                // Permission denied, handle accordingly
            }
        }
    }


    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;

    private void getUserLocation() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000); // 10 seconds

        // Check if the user has granted permission to access their location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            ProgressBar progressBar=findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            // Store location data
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            float accuracy = location.getAccuracy();
                            progressBar.setVisibility(View.GONE);
                            // Display location data on button click
                            Button button = findViewById(R.id.myButton);
                            button.setOnClickListener(v -> {
                                addLocation(location,-1,-1,-1);
                            });
                            pLocation=location;
                        }
                    }
                }
            }, null);
        }
    }

    public void addLocation(Location location,int tripNumber,double start,double end){
        ProgressBar progressBar=findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        DatabaseHelper helper=new DatabaseHelper(MainActivity.this);
        Address address=getUserLocationInfo(location);
        helper.insertLocation(location,address.toString(),tripNumber,start,end);
        locationData=helper.getAllLocations();
        adapter=new LocationsAdapter(locationData,calendar);
        recyclerView.setAdapter(adapter);
        adapter.getFilter().filter("");
        recyclerView.smoothScrollToPosition(locationData.size()-1);
        if(tripNumber==-1) {
            View view = findViewById(R.id.currentLocation);
            view.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            TextView latitudeText = view.findViewById(R.id.latitude);
            TextView otherInfo = view.findViewById(R.id.otherinfo);
            latitudeText.setText("Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude());
            otherInfo.setText("Location Info:\n" + "Address Line : " + address.getAddressLine(0) + "\nLocality : " + address.getLocality() + "\nCountry : " + address.getCountryName() + "\nPostal Code:" + address.getPostalCode());
        }
        progressBar.setVisibility(View.GONE);
        findViewById(R.id.noLocations).setVisibility(View.GONE);
    }
    private Address getUserLocationInfo(Location mUserLocation) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(mUserLocation.getLatitude(), mUserLocation.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                String city = address.getLocality();
                // Do something with the city name

                String street = address.getAddressLine(0);
                // Do something with the street name

                Log.d("locations",address.toString());
                // Add additional code to extract other information from the Address object
                return address;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    // create an action bar button


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.filter) {
            DatePickerDialog dialog=new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    calendar.set(year,month,dayOfMonth);
                    adapter.getFilter().filter("");
                }
            },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONDAY),calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        }else if (id == R.id.export) {
            exportDatabaseToCsv();
        }
        return super.onOptionsItemSelected(item);
    }
    public void onButtonShowPopupWindowClick(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        if(pLocation!=null) {
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

            View container = (View) popupWindow.getContentView().getParent();
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
            p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            p.dimAmount = 0.6f;
            wm.updateViewLayout(container, p);
            popupWindow.setOnDismissListener(() -> {
                p.dimAmount = 1f;
                container.setAlpha(1);
            });

            Button button=container.findViewById(R.id.save);
            button.setOnClickListener(v->{
                int tripNumber;
                double start;
                double end;
                try{
                    tripNumber=Integer.parseInt(((EditText)container.findViewById(R.id.tripNumber)).getText().toString());
                    start=Double.parseDouble(((EditText)container.findViewById(R.id.odometerStart)).getText().toString());
                    end=Double.parseDouble(((EditText)container.findViewById(R.id.odometerEnd)).getText().toString());
                }catch (Exception e){
                    tripNumber=-1;
                    start=-1;
                    end=-1;
                }
                addLocation(pLocation,tripNumber,start,end);
                popupWindow.dismiss();
            });
            Button cancel=container.findViewById(R.id.cancel);
            cancel.setOnClickListener(v->{
                popupWindow.dismiss();
            });
        }
        // dismiss the popup window when touched
    }
    public static int READ_PERMISSION=101;
    @SuppressLint("Range")
    public void exportDatabaseToCsv() {
        if(checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)requestReadPermission();
        // Get a reference to the SQLite database
        SQLiteDatabase db = new DatabaseHelper(MainActivity.this).getWritableDatabase();

        // Define the file name and path for the CSV file
        String fileName = new SimpleDateFormat("ddMMMyyyy-HHmmss",Locale.US).format(Calendar.getInstance().getTime())+".csv";
        File csvFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName);


        try {
            // Create a FileWriter object to write the CSV file
            FileWriter csvWriter = new FileWriter(csvFile);

            // Execute a query to retrieve the data from the database
            Cursor cursor = db.rawQuery("SELECT * FROM locations", null);

            // Write the column headers to the CSV file
            csvWriter.write("Latitude, Longitude, Timestamp,other info,Vehicle,Trip Number,Odometer Start,Odometer End\n");

            // Loop through the query results and write each row to the CSV file
            while (cursor.moveToNext()) {
                double latitude = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
                long timestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP));
                String otherInfo = cursor.getString(cursor.getColumnIndex(COLUMN_OTHER_INFO));
                otherInfo=otherInfo.replace(","," ");
                String vehicleData = cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE));
                int tripNumber = (cursor.getColumnIndex(COLUMN_TRIP_NUMBER) == -1) ? -1 : cursor.getInt(cursor.getColumnIndex(COLUMN_TRIP_NUMBER));
                int odometerStart = cursor.getColumnIndex(COLUMN_ODOMETER_START) == -1 ? -1 : cursor.getInt(cursor.getColumnIndex(COLUMN_ODOMETER_START));
                int odometerEnd = cursor.getColumnIndex(COLUMN_ODOMETER_END) == -1 ? -1 : cursor.getInt(cursor.getColumnIndex(COLUMN_ODOMETER_END));

                csvWriter.write(latitude + "," + longitude + "," + timestamp+ "," + otherInfo + "," + vehicleData+ "," + tripNumber+ "," + odometerStart+ "," + odometerEnd+ "\n");
            }

            // Close the FileWriter and the database cursor
            csvWriter.close();
            cursor.close();

            // Show a toast message to indicate that the CSV file was saved
            Toast.makeText(this, "CSV file saved to Documents folder", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}