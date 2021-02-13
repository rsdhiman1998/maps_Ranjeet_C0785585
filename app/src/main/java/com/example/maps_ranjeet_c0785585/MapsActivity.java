package com.example.maps_ranjeet_c0785585 ;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.Transliterator;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;

    private static final int REQUEST_CODE = 1;
    private Marker homeMarker;
    private Marker destMarker;
    private Location homeLocation;
    Polyline line;
    Polygon shape;
    private static final int POLYGON_SIDES = 4;
    List<Marker> markers = new ArrayList();
    String letters[] =  {"A","B","C","D"};

    // location with location manager and listener
    LocationManager locationManager;
    LocationListener locationListener;
    private Geocoder mGeocoder;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mGeocoder = new Geocoder(this, Locale.getDefault());

    }

    private String getCityNameByCoordinates(double lat, double lon) throws IOException {

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String zip = addresses.get(0).getPostalCode();
            String country = addresses.get(0).getCountryName();
            return address;
        }
        return null;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        homeLocation = new Location("");
        homeLocation.setLatitude(43.7315);
        homeLocation.setLongitude(-79.7624);
        setHomeMarker(homeLocation);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

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

        if (!hasLocationPermission())
            requestLocationPermission();
        else
            startUpdateLocation();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setMarker(latLng);

            }
        });


        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                List<LatLng> polyList =  polyline.getPoints();
                double dis = 0;
                if(polyList.size() == 2)
                {
                    dis = GetDistanceFromCurrentPosition(polyList.get(0).latitude,polyList.get(0).longitude,polyList.get(1).latitude,polyList.get(1).longitude);
                    Toast.makeText(getApplicationContext(),"Distance Between 2 Markers:-"+String.format("%.2f",dis)+" km",Toast.LENGTH_SHORT).show();
                }



            }
        });


        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                List<LatLng> points = polygon.getPoints();
                double dis = 0;
                if(points.size()==5)
                {
                    dis += GetDistanceFromCurrentPosition(points.get(0).latitude,points.get(0).longitude,points.get(1).latitude,points.get(1).longitude);
                    dis += GetDistanceFromCurrentPosition(points.get(1).latitude,points.get(1).longitude,points.get(2).latitude,points.get(2).longitude);
                    dis += GetDistanceFromCurrentPosition(points.get(2).latitude,points.get(2).longitude,points.get(3).latitude,points.get(3).longitude);
                    dis += GetDistanceFromCurrentPosition(points.get(3).latitude,points.get(3).longitude,points.get(4).latitude,points.get(4).longitude);
//                  dis += GetDistanceFromCurrentPosition(points.get(4).latitude,points.get(4).longitude,points.get(5).latitude,points.get(5).longitude);
                    Toast.makeText(getApplicationContext(),"Total Distance between points: "+String.format("%.2f",dis)+" km",Toast.LENGTH_SHORT).show();
                }


            }
        });


        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
//            marker.remove();
                LatLng f = marker.getPosition();
                for(int m = 0; m < markers.size(); m++) {
                    if(Math.abs(markers.get(m).getPosition().latitude - f.latitude) < 0.05 && Math.abs(markers.get(m).getPosition().longitude - f.longitude) < 0.05) {

                        markers.remove(m);
                        break;
                    }
                }
                if(shape!=null)
                {
                    shape.remove();
                }


            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
//                drawShapeLine();
//                drawShapeLine();
//                if(markers.size()==4)
//                {
//                    drawShape();
//                }
            }
        });
        mMap.setOnMarkerClickListener(marker -> {
            LatLng currentMarker =  marker.getPosition();
            String place = "";
            try {
                place = getCityNameByCoordinates(currentMarker.latitude,currentMarker.longitude);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(),place,Toast.LENGTH_SHORT).show();
            marker.showInfoWindow();
            return true;
        });

        // apply long press gesture
        mMap.setOnMapLongClickListener(latLng -> {
//                Location location = new Location("Your Destination");
//                location.setLatitude(latLng.latitude);
//                location.setLongitude(latLng.longitude);
            // set marker


//            for(int m = 0; m < markers.size(); m++) {
//                if(Math.abs(markers.get(m).getPosition().latitude - latLng.latitude) < 0.05 && Math.abs(markers.get(m).getPosition().longitude - latLng.longitude) < 0.05) {
//
////                    markers.remove(m);
//                    break;
//                }
//            }
//                setMarker(latLng);
        });


    }

    int i = 0;
    private void setMarker(LatLng latLng) {

        MarkerOptions options = new MarkerOptions();
        if (markers.size() == POLYGON_SIDES)
            clearMap();


        if (i < 4)
        {


            float distance =  GetDistanceFromCurrentPosition(homeLocation.getLatitude(),homeLocation.getLongitude(),latLng.latitude,latLng.longitude);

            options = new MarkerOptions().position(latLng).title(letters[i]).snippet(String.format("%.2f",distance)+" km").visible(true);

            markers.add(mMap.addMarker(options));

            if(markers.size()>1)
            {
                drawShapeLine();
            }
            i++;


        }



        if (markers.size() == POLYGON_SIDES)
            drawShape();


    }
    Polyline poly;
    Polyline poly1;
    Polyline poly2;
    Polyline poly3;
    List<Polyline> polylines= new ArrayList<Polyline>();
    private void drawShapeLine() {

        for(int r =0 ;r<=polylines.size()-1;r++)
        {
            polylines.get(r).remove();
        }
        if(poly!=null) {
            poly.remove();
        }
        if(poly1!=null) {
            poly1.remove();
        }
        if(poly2!=null) {
            poly2.remove();
        }
        if(poly3!=null) {
            poly3.remove();
        }


        if(markers.size()==2) {
            PolylineOptions options = new PolylineOptions().color(Color.RED);
            options.add(markers.get(0).getPosition());
            options.add(markers.get(1).getPosition());
            poly =  mMap.addPolyline(options);
            poly.setClickable(true);
            polylines.add(poly);
        }
        else if(markers.size()==3) {


            PolylineOptions options = new PolylineOptions().color(Color.RED);
            options.add(markers.get(0).getPosition());
            options.add(markers.get(1).getPosition());
            poly =  mMap.addPolyline(options);
            poly.setClickable(true);
            polylines.add(poly);

            PolylineOptions options1 = new PolylineOptions().color(Color.RED);
            options1.add(markers.get(1).getPosition());
            options1.add(markers.get(2).getPosition());
            poly1 =  mMap.addPolyline(options1);
            poly1.setClickable(true);
            polylines.add(poly1);


        }
        else if(markers.size()==4) {
            PolylineOptions options = new PolylineOptions().color(Color.RED);
            options.add(markers.get(0).getPosition());
            options.add(markers.get(1).getPosition());
            poly =  mMap.addPolyline(options);
            poly.setClickable(true);
            polylines.add(poly);

            PolylineOptions options1 = new PolylineOptions().color(Color.RED);
            options1.add(markers.get(1).getPosition());
            options1.add(markers.get(2).getPosition());
            poly1 =  mMap.addPolyline(options1);
            poly1.setClickable(true);
            polylines.add(poly1);


            PolylineOptions options2 = new PolylineOptions().color(Color.RED);
            options2.add(markers.get(2).getPosition());
            options2.add(markers.get(3).getPosition());
            mMap.addPolyline(options2).setClickable(true);
            poly2 =  mMap.addPolyline(options2);
            poly2.setClickable(true);
            polylines.add(poly2);


            PolylineOptions options3 = new PolylineOptions().color(Color.RED);
            options3.add(markers.get(3).getPosition());
            options3.add(markers.get(0).getPosition());
            mMap.addPolyline(options3).setClickable(true);

            poly3 =  mMap.addPolyline(options2);
            poly3.setClickable(true);
            polylines.add(poly3);
        }








    }
    private void drawShape() {

        PolygonOptions options = new PolygonOptions()
                .strokeColor(Color.RED)
                .fillColor(Color.argb(35,0,128,0))
                .strokeWidth(5);

        for (int r=0; r<i; r++) {
            options.add(markers.get(r).getPosition());
        }
        shape = mMap.addPolygon(options);
        shape.setClickable(true);
    }

    private void clearMap() {
        mMap.clear();
        poly.remove();
        poly1.remove();
        poly2.remove();
        poly3.remove();
                /*if (destMarker != null) {
                    destMarker.remove();
                    destMarker = null;
                }
                line.remove();*/
//
        for (Marker marker: markers)
            marker.remove();
//
        markers.clear();
//
//        if(shape != null) { shape.remove(); }
//        shape = null;

        i = 0;
    }

    private void drawLine() {
        PolylineOptions options = new PolylineOptions()
                .color(Color.BLACK)
                .width(10)
                .add(homeMarker.getPosition(), destMarker.getPosition());
        line = mMap.addPolyline(options);
    }

    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);

        /*Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        setHomeMarker(lastKnownLocation);*/
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setHomeMarker(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions options = new MarkerOptions().position(userLocation)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet("Your Location");
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            }
        }
    }

    public static float GetDistanceFromCurrentPosition(double lat1,double lng1, double lat2, double lng2)
    {
        double earthRadius = 3958.75;

        double dLat = Math.toRadians(lat2 - lat1);

        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double dist = earthRadius * c;

        int meterConversion = 1609;

        return new Float((dist * meterConversion)/1000).floatValue();

    }
}
