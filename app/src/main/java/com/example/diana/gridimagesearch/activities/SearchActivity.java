package com.example.diana.gridimagesearch.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.example.diana.gridimagesearch.R;
import com.example.diana.gridimagesearch.adapters.ImageResultsAdapter;
import com.example.diana.gridimagesearch.models.ImageResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class SearchActivity extends ActionBarActivity {
    private EditText etQuery;
    private GridView gvResults;
    private Button btnSearch;
    private ArrayList<ImageResult> imageResult;
    private ImageResultsAdapter aImageResults;
    int count = 0;
    Boolean newSearch = false;
    private String size;
    private String site;
    private String type;
    private String color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupViews();
        imageResult = new ArrayList<ImageResult>();
        aImageResults = new ImageResultsAdapter(this, imageResult);
        gvResults.setAdapter(aImageResults);

        gvResults.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int currentScrollState = 0;
            private int currentVisibleItemCount = 0;
            private int currentFirstVisibleItem = 0;

            private void isScrollCompleted() {
                if (this.currentVisibleItemCount > 0 && this.currentScrollState == SCROLL_STATE_IDLE) {
                    /* Scroll has completed, do the work. */
                    onImageSearch(getCurrentFocus());
                } else if (this.currentFirstVisibleItem < 16) {
                    onImageSearch(getCurrentFocus());
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //Log.d(">>>>>>>>>>>", "onScrollStateChanged");
                this.currentScrollState = scrollState;
                this.isScrollCompleted();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //Log.d(">>>>>>>>>>>", "onScroll");
                this.currentFirstVisibleItem = firstVisibleItem;
                this.currentVisibleItemCount = visibleItemCount;
            }
        });
    }

    private void setupViews() {
        etQuery = (EditText) findViewById(R.id.etQuery);
        gvResults = (GridView) findViewById(R.id.gvResults);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(SearchActivity.this, ImageDisplayActivity.class);
                ImageResult result = aImageResults.getItem(position);
                i.putExtra("result", result);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    public void onSearchButtonClicked(View v) {
        newSearch = true;
        onImageSearch(v);
    }

    public void onImageSearch(View v) {
        final String query = etQuery.getText().toString();
        AsyncHttpClient client = new AsyncHttpClient();
        // https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=android&rsz=8
        String url = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=8";
        String urlWithCount;

        if (newSearch == true) {
            count = 0;
            aImageResults.clear();
            newSearch = false;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            size = prefs.getString(getString(R.string.pref_size_key),
                    getString(R.string.pref_size_default));
            site = prefs.getString(getString(R.string.pref_site_key),
                    getString(R.string.pref_site_default));
            color = prefs.getString(getString(R.string.pref_color_key),
                    getString(R.string.pref_color_default));
            type = prefs.getString(getString(R.string.pref_type_key),
                    getString(R.string.pref_type_default));
        }
        if (!size.contentEquals("none")) url += "&imgsz=" + size;
        if (!color.contentEquals("none")) url += "&imgcolor=" + color;
        if (!type.contentEquals("none")) url += "&imgtype=" + type;
        if (!site.contentEquals("")) url += "&as_sitesearch=" + site;
        url += "&q=" + query;
        urlWithCount = url + "&start=" + count;

        if (count >= 64) return; // API limits results to 64 images.
        Log.d(">>>> URL is ", urlWithCount);
        client.get(urlWithCount, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("DEBUG", response.toString());
                try {
                    JSONObject jsonObject = response.getJSONObject("responseData");
                    JSONArray imageResultJson = jsonObject.getJSONArray("results");
                    aImageResults.addAll(ImageResult.fromJSONArray(imageResultJson));
                    count += imageResultJson.length();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
/*
        // If count==0, there were no hits and no need to search more.
        // If count>16, we've filled the first screen.
        if (count == 0 || count > 16) return;
        // else do another search to fill out the initial screen.

        url = urlWithCount + "&start=" + count;
        Log.d(">>>> URL is ", urlWithCount);
        client.get(urlWithCount, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("DEBUG", response.toString());
                try {
                    JSONObject jsonObject = response.getJSONObject("responseData");
                    JSONArray imageResultJson = jsonObject.getJSONArray("results");
                    aImageResults.addAll(ImageResult.fromJSONArray(imageResultJson));
                    count += imageResultJson.length();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        */
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
