package com.example.sam.nytimessearch.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.sam.nytimessearch.R;
import com.example.sam.nytimessearch.adapter.ArticleArrayAdapter;
import com.example.sam.nytimessearch.model.Article;
import com.example.sam.nytimessearch.model.Query;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static com.example.sam.nytimessearch.R.array.sortItems;

public class SearchActivity extends AppCompatActivity {

    @BindView(R.id.rvResults)
    RecyclerView rvResults;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    Query query;

    ArrayList<Article> articles;
    ArticleArrayAdapter adapter;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setupViews();
        setupListViewListener();
        query = new Query();
        fetchArticles(query, 0);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void setupViews() {
        // Initialize contacts
        articles = new ArrayList<>();
        // Create adapter passing in the sample user data
        adapter = new ArticleArrayAdapter(this, articles);

        // Attach the adapter to the recyclerview to populate items
        rvResults.setAdapter(adapter);

        // Set layout manager to position the items
        // First param is number of columns and second param is orientation i.e Vertical or Horizontal
        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        // Attach the layout manager to the recycler view
        rvResults.setLayoutManager(gridLayoutManager);
        // That's all!

        rvResults.addOnScrollListener(new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                fetchArticles(query, page);
            }
        });

    }

    private void setupListViewListener() {
        adapter.setOnItemClickListener(new ArticleArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //first parameter is the context, second is the class of the activity to launch
                Intent intent = new Intent(SearchActivity.this, ArticleActivity.class);
                Article article = articles.get(position);
                Log.d("DEBUG", article.toString());
                intent.putExtra("article", Parcels.wrap(article));
                startActivity(intent);
                return;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_search:
                Toast.makeText(this, "Search selected", Toast.LENGTH_SHORT)
                        .show();
                final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String q) {
                        // perform query here
                        query.setQ(q);
                        fetchArticles(query, 0);

                        // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                        // see https://code.google.com/p/android/issues/detail?id=24599
                        searchView.clearFocus();

                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });
                break;
            // action with ID action_settings was selected
            case R.id.action_filter:
                Toast.makeText(this, "Filter selected", Toast.LENGTH_SHORT)
                        .show();
                showMaterialDialog();
                break;
            default:
                break;
        }
        return true;
    }

    private void showMaterialDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.filter)
                .items(R.array.filterItems)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which){
                            case 0: //sort
                                new MaterialDialog.Builder(SearchActivity.this)
                                        .title(R.string.sort)
                                        .items(sortItems)
                                        .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                                            @Override
                                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                /**
                                                 * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                                                 * returning false here won't allow the newly selected radio button to actually be selected.
                                                 **/
                                                if(which == 0) query.setSort("newest");
                                                else query.setSort("oldest");
                                                fetchArticles(query, 0);
                                                return true;
                                            }
                                        })
                                        .positiveText(R.string.choose)
                                        .negativeText(R.string.cancel)
                                        .show();
                            break;
                            case 1: //News desk
                            break;
                            case 2: //begin date
                            break;
                            default:
                            break;
                        }
                    }
                })
                .show();
    }

    // Executes an API call to the OpenLibrary search endpoint, parses the results
    // Converts them into an array of book objects and adds them to the adapter
    private void fetchArticles(Query query, final int page) {
        //Toast.makeText(this, "Searching for " + query, Toast.LENGTH_LONG).show();
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://api.nytimes.com/svc/search/v2/articlesearch.json";
        RequestParams params = new RequestParams();
        params.put("api-key", "4976251588704a209f2432f634e21445");
        params.put("page", page);
        params.put("q", query.getQ());
        if (!TextUtils.isEmpty(query.getBegin_date()))
            params.put("begin_date", query.getBegin_date());
        if (!TextUtils.isEmpty(query.getSort())) params.put("sort", query.getSort());
        if (!TextUtils.isEmpty(query.getFq())) params.put("fq", query.getFq());
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("DEBUG", response.toString());
                JSONArray articleJsonResults = null;
                try {
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    if(page == 0){
                        articles.clear();
                        articles.addAll(Article.fromJSONArray(articleJsonResults));
                        Log.d("DEBUG", articles.toString());
                        adapter.notifyDataSetChanged();
                    }
                    else {
                        int curSize = adapter.getItemCount();
                        articles.addAll(Article.fromJSONArray(articleJsonResults));
                        adapter.notifyItemRangeInserted(curSize, articles.size() - 1);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Search Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2.connect();
        AppIndex.AppIndexApi.start(client2, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client2, getIndexApiAction());
        client2.disconnect();
    }
}
