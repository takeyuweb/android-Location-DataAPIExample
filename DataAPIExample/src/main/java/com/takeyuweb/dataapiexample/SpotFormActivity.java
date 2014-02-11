package com.takeyuweb.dataapiexample;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SpotFormActivity extends Activity {
    private TextView textViewId;
    private EditText editTextTitle;
    private GoogleMap googleMap;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_form_main);

        textViewId = (TextView)findViewById(R.id.textViewId);
        editTextTitle = (EditText)findViewById(R.id.editTextTitle);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        textViewId.setText(Integer.toString(intent.getIntExtra("id", 0)));
        editTextTitle.setText(intent.getStringExtra("title"));

        MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.fragmentSpotMap);
        if (mapFragment != null) {
            googleMap = mapFragment.getMap();
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setMyLocationEnabled(true);
            LatLng latLng = new LatLng(intent.getDoubleExtra("lat", 0.0), intent.getDoubleExtra("lng", 0.0));
            CameraPosition camerapos = new CameraPosition.Builder()
                    .target(latLng).zoom(15.5f).build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camerapos));
            marker = googleMap.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .draggable(true)
                            .title(editTextTitle.getText().toString()));
        }

        Button button = (Button)findViewById(R.id.buttonSubmit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("id", Integer.valueOf(textViewId.getText().toString()));
                data.putExtra("title", editTextTitle.getText().toString());
                if (marker != null) {
                    LatLng latLng = marker.getPosition();
                    data.putExtra("lat", latLng.latitude);
                    data.putExtra("lng", latLng.longitude);
                    setResult(1, data);
                } else {
                    setResult(0, data);
                }
                finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("id", Integer.valueOf(textViewId.getText().toString()));
        outState.putString("title", editTextTitle.getText().toString());
        outState.putDouble("lat", marker.getPosition().latitude);
        outState.putDouble("lng", marker.getPosition().longitude);
        outState.putFloat("zoom", googleMap.getCameraPosition().zoom);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        textViewId.setText(Integer.toString(savedInstanceState.getInt("id", 0)));
        editTextTitle.setText(savedInstanceState.getString("title"));
        LatLng latLng = new LatLng(savedInstanceState.getDouble("lat", 0.0), savedInstanceState.getDouble("lng", 0.0));
        marker.setPosition(latLng);
        CameraPosition cameraPosition = CameraPosition.fromLatLngZoom(latLng, savedInstanceState.getFloat("zoom", 15.0f));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.moveCamera(cameraUpdate);
    }
}
