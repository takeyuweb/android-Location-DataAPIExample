package com.takeyuweb.dataapiexample;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;
import com.takeyuweb.util.HttpAsyncLoader;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by uzuki05 on 14/02/11.
 */
abstract class MTLoader extends AsyncTaskLoader<String> {
    protected String baseUrl;
    protected String endpoint;
    public MTLoader(Context context, String baseUrl, String endpoint) {
        super(context);
        this.baseUrl = baseUrl;
        this.endpoint = endpoint;
    }

    @Override
    public String loadInBackground() {
        Log.d(getClass().getSimpleName(), baseUrl + endpoint);
        HttpUriRequest request = buildRequest();
        HttpClient httpClient = new DefaultHttpClient();
        try {
            String responseBody = httpClient.execute(request,
                    new ResponseHandler<String>() {
                        @Override
                        public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                            if (HttpStatus.SC_OK == httpResponse.getStatusLine().getStatusCode()) {
                                return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                            } else {
                                Log.e("MTLoader", EntityUtils.toString(httpResponse.getEntity(), "UTF-8"));
                            }
                            return null;
                        }
                    });
            return responseBody;
        }
        catch(Exception e) {
            String message = e.getMessage();
            if (message != null) {
                Log.e(getClass().getSimpleName(), message);
            } else {
                Log.e(getClass().getSimpleName(), e.getClass().toString());
            }
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
        return null;
    }

    abstract HttpUriRequest buildRequest();

}

class MTGetLoader extends MTLoader {
    private List<NameValuePair> queryParams = null;
    public MTGetLoader(Context context, String baseUrl, String endpoint, List<NameValuePair> queryParams) {
        super(context, baseUrl, endpoint);
        this.queryParams = queryParams;
    }

    @Override
    protected HttpUriRequest buildRequest() {
        String url = baseUrl + endpoint;
        if (queryParams != null) {
            String query = "";
            Iterator iter = queryParams.iterator();
            while (iter.hasNext()) {
                BasicNameValuePair pair = (BasicNameValuePair)iter.next();
                String snip = null;
                try {
                    snip = URLEncoder.encode(pair.getName(), "UTF-8") + "=" + URLEncoder.encode(pair.getValue(), "UTF-8");
                }
                catch (UnsupportedEncodingException e) {
                    Log.d(this.getClass().getSimpleName(), e.getMessage());
                }
                if (snip != null) {
                    query = query + "&" + snip;
                }
            }
            if (!query.isEmpty()) {
                url = url + "?" + query;
            }
        }
        HttpUriRequest request = new HttpGet(url);
        return request;
    }
}

class MTPostLoader extends MTLoader {
    private List<NameValuePair> postData = null;
    private String username = null;
    private String password = null;
    public MTPostLoader(Context context, String baseUrl, String endpoint, List<NameValuePair> postData, String username, String password) {
        super(context, baseUrl, endpoint);
        this.postData = postData;
        this.username = username;
        this.password = password;
    }

    @Override
    protected HttpUriRequest buildRequest() {
        String url = baseUrl + endpoint;
        HttpPost request = new HttpPost(url);
        if (username != null && password != null) {
            MTSession session = getSession(username, password);
            if (session != null) {
                request.setHeader("X-MT-Authorization", "MTAuth accessToken=" + session.accessToken);
            }
        }
        try {
            request.setEntity(new UrlEncodedFormEntity(postData, "UTF-8"));
        } catch(UnsupportedEncodingException e) {
            Log.e("MTPostLoader", e.getMessage());
        }
        return request;
    }

    private MTSession getSession(String username, String password) {
        HttpPost request = new HttpPost(baseUrl + "/v1/authentication");
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("username", username));
        postData.add(new BasicNameValuePair("password", password));
        postData.add(new BasicNameValuePair("remember", "0"));
        postData.add(new BasicNameValuePair("clientId", "MTLoader"));
        try {
            request.setEntity(new UrlEncodedFormEntity(postData, "UTF-8"));
        } catch(UnsupportedEncodingException e) {
            Log.e("MTPostLoader", e.getMessage());
        }
        HttpClient httpClient = new DefaultHttpClient();
        try {
            Log.d("MTPostLoader", request.getURI().toString());
            String responseBody = httpClient.execute(request,
                    new ResponseHandler<String>() {
                        @Override
                        public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                            if (HttpStatus.SC_OK == httpResponse.getStatusLine().getStatusCode()) {
                                return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                            } else {
                                Log.e("MTLoader", EntityUtils.toString(httpResponse.getEntity(), "UTF-8"));
                            }
                            return null;
                        }
                    });
            ParseSession sessionParser = new ParseSession();
            sessionParser.loadJson(responseBody);
            return sessionParser.getSession();
        }
        catch(Exception e) {
            String message = e.getMessage();
            if (message != null) {
                Log.e("MTPostLoader", message);
            } else {
                Log.e("MTPostLoader", e.getClass().toString());
            }
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
        return null;
    }
}