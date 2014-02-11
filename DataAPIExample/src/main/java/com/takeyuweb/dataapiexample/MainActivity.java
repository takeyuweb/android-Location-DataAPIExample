package com.takeyuweb.dataapiexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import com.takeyuweb.util.HttpAsyncLoader;
import android.app.LoaderManager.LoaderCallbacks;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity
        implements LoaderCallbacks<String> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.landscape);
        } else {
            setContentView(R.layout.portrait);
        }
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle bundle) {
        MTLoader mtLoader = null;
        String apiBase = getResources().getString(R.string.data_api_base);
        String endpoint = null;
        HashMap<String, String> data = null;
        switch (id) {
            case 0:
                endpoint = bundle.getString("endpoint");
                data = (HashMap<String, String>)bundle.getSerializable("data");
                List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
                if (data != null) {
                    for (Map.Entry<String, String> entry : data.entrySet()) {
                        queryParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                    }
                }
                mtLoader = new MTGetLoader(this, apiBase, endpoint, queryParams);
                mtLoader.forceLoad();
                break;
            case 2:
                endpoint = bundle.getString("endpoint");
                data = (HashMap<String, String>)bundle.getSerializable("data");
                List<NameValuePair> postData = new ArrayList<NameValuePair>();
                if (data != null) {
                    for (Map.Entry<String, String> entry : data.entrySet()) {
                        postData.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                    }
                }
                mtLoader = new MTPostLoader(this, apiBase, endpoint, postData, getResources().getString(R.string.mtuser), getResources().getString(R.string.mtpass));
                mtLoader.forceLoad();
                break;
        }
        return mtLoader;
    }


    @Override
    public void onLoadFinished(Loader<String> loader, String body) {
        if (body == null) return;
        FragmentManager fragmentManager = getFragmentManager();
        MyMapFragment mapFragment = (MyMapFragment)fragmentManager.findFragmentById(R.id.map);
        switch (loader.getId()) {
            case 0:
                ParseEntries parse = new ParseEntries();
                parse.loadJson(body);
                mapFragment.setMarker(parse);
                break;
            case 2:
                ParseEntries parseEntries = new ParseEntries();
                parseEntries.loadJson(body);
                mapFragment.addMarker(parseEntries);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    private static int changingConfiguration;
    @Override
    protected void onDestroy(){
        super.onDestroy();
        changingConfiguration = getChangingConfigurations();
    }
    public int getSizeChangingConfigurations(){
        return changingConfiguration;
    }
}
