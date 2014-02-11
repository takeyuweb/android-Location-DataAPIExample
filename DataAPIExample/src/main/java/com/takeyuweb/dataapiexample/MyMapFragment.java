package com.takeyuweb.dataapiexample;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.*;
import org.json.JSONException;
import org.json.JSONStringer;

import java.util.*;

/**
 * Created by uzuki05 on 14/01/24.
 */
public class MyMapFragment extends MapFragment {
    private GoogleMap googleMap;
    private HashMap<Marker, MTEntry> entryMarkerMap;
    private HashMap<Integer, Marker> entryList;
    private CameraPosition centerPosition = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entryMarkerMap = new HashMap<Marker, MTEntry>();
        entryList = new HashMap<Integer, Marker>();
        // Fragmentを保存する（再生成しない=onCreate/onDestroyは1回しか呼ばれない）
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // setRetainInstance(true)のとき、 savedInstanceState は常に NULL
        super.onActivityCreated(savedInstanceState);
        try {
            googleMap = getMap();
            if (savedInstanceState == null) {
                mapInit();
            }
        } catch (Exception e) {
            Log.e("dataapiexample", e.getStackTrace().toString());
        }
        int changingConfiguration = ((MainActivity)getActivity()).getSizeChangingConfigurations();
        if (((ActivityInfo.CONFIG_ORIENTATION | ActivityInfo.CONFIG_SCREEN_SIZE) & changingConfiguration) != 0){
            execInBounds(googleMap.getProjection().getVisibleRegion().latLngBounds);
        }

    }

    // 2点間の距離を求める(km)
    private double calcDistance(CameraPosition a, CameraPosition b) {

        double lata = Math.toRadians(a.target.latitude);
        double lnga = Math.toRadians(a.target.longitude);

        double latb = Math.toRadians(b.target.latitude);
        double lngb = Math.toRadians(b.target.longitude);

        double r = 6378.137; // 赤道半径

        return r * Math.acos(Math.sin(lata) * Math.sin(latb) + Math.cos(lata) * Math.cos(latb) * Math.cos(lngb - lnga));
    }

    private void mapInit() {
        if (centerPosition != null) return;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);
        CameraPosition camerapos = new CameraPosition.Builder()
                .target(new LatLng(35.681382, 139.766084)).zoom(15.5f).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camerapos));

        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d("DataAPIExample", "onCameraChange " + cameraPosition.target.toString());
                if (centerPosition != null) {
                    if ( 0.05 < calcDistance(centerPosition, cameraPosition) ) {
                        execInBounds(googleMap.getProjection().getVisibleRegion().latLngBounds);
                        centerPosition = cameraPosition;
                    }
                } else {
                    execInBounds(googleMap.getProjection().getVisibleRegion().latLngBounds);
                    centerPosition = cameraPosition;
                }
            }
        });
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                MTEntry entry = entryMarkerMap.get(marker);
                if (entry != null) {
                    Intent intent = new Intent(getActivity(), SpotFormActivity.class);
                    intent.putExtra("id", entry.id);
                    intent.putExtra("title", entry.title);
                    intent.putExtra("lat", entry.lat);
                    intent.putExtra("lng", entry.lng);
                    startActivityForResult(intent, 2);
                }
            }
        });
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Intent intent = new Intent(getActivity(), SpotFormActivity.class);
                intent.putExtra("lat", latLng.latitude);
                intent.putExtra("lng", latLng.longitude);
                startActivityForResult(intent, 1);
            }
        });
        execInBounds(googleMap.getProjection().getVisibleRegion().latLngBounds);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("DataAPIExample", String.format("requestCode:%d resultCode:%d", requestCode, resultCode));
        if (requestCode == 1) {
            if (resultCode == 1) {
                MTEntry entry = new MTEntry();
                entry.title = data.getStringExtra("title");
                entry.lat = data.getDoubleExtra("lat", 0.0);
                entry.lng = data.getDoubleExtra("lng", 0.0);
                createSpot(entry);
            }
        } else if (requestCode == 2) {
            if (resultCode == 1) {
                int id = data.getIntExtra("id", 0);
                Marker marker = entryList.get(id);
                if (marker != null) {
                    MTEntry entry = entryMarkerMap.get(marker);
                    if (entry != null) {
                        entry.title = data.getStringExtra("title");
                        entry.lat = data.getDoubleExtra("lat", 0.0);
                        entry.lng = data.getDoubleExtra("lng", 0.0);
                        updateSpot(entry);
                    }
                }
            }
        }
    }

    public void execInBounds(LatLngBounds bounds) {
        Bundle bundle = new Bundle();
        bundle.putString("endpoint", "/v1/sites/"+getResources().getString(R.string.blog_id)+"/entries");

        HashMap<String, String> data = new HashMap<String, String>();
        data.put("sw_lat", Double.toString(bounds.southwest.latitude));
        data.put("sw_lng", Double.toString(bounds.southwest.longitude));
        data.put("ne_lat", Double.toString(bounds.northeast.latitude));
        data.put("ne_lng", Double.toString(bounds.northeast.longitude));
        data.put("limit", Integer.toString(100));
        bundle.putSerializable("data", data);

        LoaderManager loaderManager = getLoaderManager();
        if (loaderManager != null) {
            loaderManager.restartLoader(0, bundle, (MainActivity) getActivity());
        }
    }

    public void setMarker(ParseEntries parseEntries) {
        HashMap<Integer, Boolean> idSet = new HashMap<Integer, Boolean>();
        List<MTEntry> entries = new ArrayList<MTEntry>();
        for (int id : entryList.keySet()) {
            idSet.put(id, true);
        }
        for (MTEntry e : parseEntries.getEntries()) {
            String title = e.title;
            LatLng latLng = new LatLng(e.lat, e.lng);
            if (title.isEmpty()) {
                title = "(not set)";
            }
            Marker marker = entryList.get(e.id);
            if (marker == null) {
                marker = googleMap.addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .title(title));
                entryMarkerMap.put(marker, e);
                entryList.put(e.id, marker);
                Log.d("DataAPIExample", "e.id:"+Integer.toString(e.id));
            } else {
                marker.setTitle(title);
                marker.setPosition(latLng);
            }
            idSet.remove(e.id);
            entries.add(e);
        }
        for (int id : idSet.keySet()) {
            Marker marker = entryList.get(id);
            entryMarkerMap.remove(marker);
            marker.remove();
            entryList.remove(id);
        }
        updateList(entries);
    }

    public void addMarker(ParseEntries parseEntries) {
        List<MTEntry> entries = new ArrayList<MTEntry>();
        for (MTEntry e : parseEntries.getEntries()) {
            String title = e.title;
            LatLng latLng = new LatLng(e.lat, e.lng);
            if (title.isEmpty()) {
                title = "(not set)";
            }
            Marker marker = entryList.get(e.id);
            if (marker == null) {
                marker = googleMap.addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .title(title));
                entryMarkerMap.put(marker, e);
                entryList.put(e.id, marker);
                Log.d("DataAPIExample", "e.id:"+Integer.toString(e.id));
            } else {
                marker.setTitle(title);
                marker.setPosition(latLng);
            }
        }
        addList(entries);
    }

    public void createSpot(MTEntry entry) {
        Bundle bundle = new Bundle();
        bundle.putString("endpoint", "/v1/sites/"+getResources().getString(R.string.blog_id)+"/entries");

        JSONStringer jsonStringer = new JSONStringer();
        try {
            jsonStringer.object();
            jsonStringer.key("status").value("Publish");
            jsonStringer.key("title").value(entry.title);
            jsonStringer.key("body").value("");
            jsonStringer.key("useLocation").value(true);
            jsonStringer.key("lat").value(entry.lat);
            jsonStringer.key("lng").value(entry.lng);
            jsonStringer.endObject();
        } catch(JSONException e) {
            Log.d(getClass().getName(), e.getMessage());
        }
        String entryJson = jsonStringer.toString();

        HashMap<String, String> data = new HashMap<String, String>();
        data.put("entry", entryJson);
        bundle.putSerializable("data", data);

        LoaderManager loaderManager = getLoaderManager();
        if (loaderManager != null) {
            loaderManager.restartLoader(2, bundle, (MainActivity) getActivity());
        }
    }

    public void updateSpot(MTEntry entry) {
        Bundle bundle = new Bundle();
        bundle.putString("endpoint", "/v1/sites/"+getResources().getString(R.string.blog_id)+"/entries/"+Integer.toString(entry.id));

        JSONStringer jsonStringer = new JSONStringer();
        try {
            jsonStringer.object();
            jsonStringer.key("title").value(entry.title);
            jsonStringer.key("lat").value(entry.lat);
            jsonStringer.key("lng").value(entry.lng);
            jsonStringer.endObject();
        } catch(JSONException e) {
            Log.d(getClass().getName(), e.getMessage());
        }
        String entryJson = jsonStringer.toString();

        HashMap<String, String> data = new HashMap<String, String>();
        data.put("entry", entryJson);
        data.put("__method", "PUT");
        bundle.putSerializable("data", data);

        LoaderManager loaderManager = getLoaderManager();
        if (loaderManager != null) {
            loaderManager.restartLoader(2, bundle, (MainActivity) getActivity());
        }
    }

    private void updateList(List<MTEntry> entries) {
        ListViewFragment listViewFragment = (ListViewFragment)getFragmentManager().findFragmentById(R.id.listView_Fragment);
        if (listViewFragment != null) {
            listViewFragment.updateList(entries);
        }
    }

    private void addList(List<MTEntry> entries) {
        ListViewFragment listViewFragment = (ListViewFragment)getFragmentManager().findFragmentById(R.id.listView_Fragment);
        if (listViewFragment != null) {
            listViewFragment.addList(entries);
        }
    }

    public void selectEntry(MTEntry entry) {
        Marker marker = entryList.get(entry.id);
        if (marker != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(entry.getLatLng());
            googleMap.animateCamera(cameraUpdate);
            marker.showInfoWindow();
        }
    }
}