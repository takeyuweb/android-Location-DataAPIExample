package com.takeyuweb.util;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;
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
import java.util.*;

/**
 * Created by uzuki05 on 14/01/23.
 */
public class HttpAsyncLoader extends AsyncTaskLoader<String> {
    private HttpUriRequest request = null;
    public HttpAsyncLoader(Context context, String url) {
        super(context);
        this.request = new HttpGet(url);
    }
    public HttpAsyncLoader(Context context, String url, List<NameValuePair> postData) {
        super(context);
        HttpPost request = new HttpPost(url);
        try {
            request.setEntity(new UrlEncodedFormEntity(postData, "UTF-8"));
            this.request = request;
        } catch(UnsupportedEncodingException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
    }
    public HttpAsyncLoader(Context context, String url, List<NameValuePair> headers, List<NameValuePair> postData) {
        super(context);
        HttpPost request = new HttpPost(url);
        Iterator iter = headers.iterator();
        while (iter.hasNext()) {
            NameValuePair pair = (BasicNameValuePair)iter.next();
            request.setHeader(pair.getName(), pair.getValue());
        }
        try {
            request.setEntity(new UrlEncodedFormEntity(postData, "UTF-8"));
            this.request = request;
        } catch(UnsupportedEncodingException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public String loadInBackground() {
        HttpClient httpClient = new DefaultHttpClient();
        try {
            String responseBody = httpClient.execute(getRequest(),
                    new ResponseHandler<String>() {
                        @Override
                        public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                            if (HttpStatus.SC_OK == httpResponse.getStatusLine().getStatusCode()) {
                                return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                            } else {
                                Log.e("HttpAsyncLoader", EntityUtils.toString(httpResponse.getEntity(), "UTF-8"));
                            }
                            return null;
                        }
                    });
            return responseBody;
        }
        catch(Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
        return null;
    }

    protected HttpUriRequest getRequest() {
        return this.request;
    }
}
