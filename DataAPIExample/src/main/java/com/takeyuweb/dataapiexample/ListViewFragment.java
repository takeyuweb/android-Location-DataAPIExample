package com.takeyuweb.dataapiexample;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by uzuki05 on 14/01/25.
 */
public class ListViewFragment extends Fragment {

    private class ListViewItemAdapter extends ArrayAdapter<MTEntry> {
        private LayoutInflater layoutInflater;

        public ListViewItemAdapter(Context context, int textViewResourceId, List<MTEntry> entries) {
            super(context, textViewResourceId, entries);
            layoutInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.listview_item, null);
            }

            MTEntry entry = getItem(position);
            TextView textView = (TextView)convertView.findViewById(R.id.textView_Item);
            textView.setText(entry.title);
            TextView excerptView = (TextView)convertView.findViewById(R.id.textView_Item_excerpt);
            excerptView.setText(entry.excerpt);
            Log.d("DataAPIExample", "getView: eid:" + Integer.toString(entry.id));
            return convertView;
        }
    }

    private class DistanceComparator implements Comparator<MTEntry> {
        private LatLng latLng;
        public DistanceComparator(LatLng latLng) {
            super();
            this.latLng = latLng;
        }

        @Override
        public int compare(MTEntry e1, MTEntry e2) {
            int result;
            Double distance1 = calcDistance(latLng, new LatLng(e1.lat, e1.lng));
            Double distance2 = calcDistance(latLng, new LatLng(e2.lat, e2.lng));
            return distance1.compareTo(distance2);
        }

        private double calcDistance(LatLng a, LatLng b) {
            double lata = Math.toRadians(a.latitude);
            double lnga = Math.toRadians(a.longitude);
            double latb = Math.toRadians(b.latitude);
            double lngb = Math.toRadians(b.longitude);
            double r = 6378.137;
            return r * Math.acos(Math.sin(lata) * Math.sin(latb) + Math.cos(lata) * Math.cos(latb) * Math.cos(lngb - lnga));
        }
    }

    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listview, container, false);

        List<MTEntry> list = new ArrayList<MTEntry>();
        ListViewItemAdapter adapter = new ListViewItemAdapter(getActivity(), 0, list);
        listView = (ListView)view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView)parent;
                MTEntry entry = (MTEntry)listView.getItemAtPosition(position);
                MyMapFragment mapFragment = getMyMapFragment();
                if (mapFragment != null && entry != null) {
                    mapFragment.selectEntry(entry);
                }
            }
        });
        return view;
    }

    public void updateList(List<MTEntry> entries) {
        GoogleMap map = getMap();
        if (map == null) return;
        if (listView == null) return;
        DistanceComparator comparator = new DistanceComparator(map.getCameraPosition().target);
        Collections.sort(entries, comparator);
        ListViewItemAdapter adapter = (ListViewItemAdapter)listView.getAdapter();
        adapter.clear();
        adapter.addAll(entries);
    }

    public void addList(List<MTEntry> entries) {
        GoogleMap map = getMap();
        if (map == null) return;
        if (listView == null) return;
        ListViewItemAdapter adapter = (ListViewItemAdapter)listView.getAdapter();
        adapter.addAll(entries);
    }

    private GoogleMap getMap() {
        MyMapFragment myMapFragment = getMyMapFragment();
        if (myMapFragment != null) {
            return myMapFragment.getMap();
        }
        return null;
    }

    private MyMapFragment getMyMapFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            MyMapFragment myMapFragment = (MyMapFragment)fragmentManager.findFragmentById(R.id.map);
            return myMapFragment;
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.listView = null;
    }

}