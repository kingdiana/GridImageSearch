package com.example.diana.gridimagesearch.activities;

import android.content.Intent;
import android.os.Bundle;
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
    private String previousQuery = "";
    private GridView gvResults;
    private Button btnSearch;
    private ArrayList<ImageResult> imageResult;
    private ImageResultsAdapter aImageResults;
    int count = 0;

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

    public void onImageSearch(View v) {
        final String query = etQuery.getText().toString();
        if (!query.contentEquals(previousQuery)) {
            count = 0;
            previousQuery = query;
            aImageResults.clear();
        }
        AsyncHttpClient client = new AsyncHttpClient();
        // https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=android&rsz=8
        String url = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=" +
                     query + "&rsz=8&start=" + count;
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (count < 64) {  // api limits results to 64 total
                    Log.d("DEBUG", response.toString());
                    try {
                        JSONObject jsonObject = response.getJSONObject("responseData");
                        JSONArray imageResultJson = jsonObject.getJSONArray("results");
                        aImageResults.addAll(ImageResult.fromJSONArray(imageResultJson));
                        count += imageResultJson.length();

                        if (count < 16 && imageResultJson.length() >= 8) {
                            btnSearch.callOnClick();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
