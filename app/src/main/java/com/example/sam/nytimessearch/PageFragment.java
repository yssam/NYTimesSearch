package com.example.sam.nytimessearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sam.nytimessearch.activities.ArticleActivity;
import com.example.sam.nytimessearch.activities.EndlessRecyclerViewScrollListener;
import com.example.sam.nytimessearch.adapter.ArticleArrayAdapter;
import com.example.sam.nytimessearch.model.Article;
import com.example.sam.nytimessearch.model.Query;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Sam on 7/31/16.
 */

public class PageFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    RecyclerView rvResults;
    ArrayList<Article> articles;
    ArticleArrayAdapter adapter;
    Query query;
    private int mPage;

    public static PageFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);

    }



    // Inflate the fragment layout we defined above for this fragment
    // Set the associated text for the title
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        rvResults = (RecyclerView) view.findViewById(R.id.rvResults1);
        setupViews();
        setupListViewListener();
        query = new Query();
        fetchArticles(query, 0);
        //TextView tvTab = (TextView) view.findViewById(R.id.tvTab);
        //tvTab.setText("Fragment #" + mPage);
        return view;
    }

    public void setupViews() {

        // Initialize contacts
        articles = new ArrayList<>();
        // Create adapter passing in the sample user data
        adapter = new ArticleArrayAdapter(getActivity().getApplication(), articles);
        // Attach the adapter to the recyclerview to populate items
        rvResults.setAdapter(adapter);
        // Set layout manager to position the items
        // First param is number of columns and second param is orientation i.e Vertical or Horizontal
        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
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
                Intent intent = new Intent(getActivity().getApplication(), ArticleActivity.class);
                Article article = articles.get(position);
                Log.d("DEBUG", article.toString());
                intent.putExtra("article", Parcels.wrap(article));
                startActivity(intent);
                return;
            }
        });
    }

    // Executes an API call to the OpenLibrary search endpoint, parses the results
    // Converts them into an array of book objects and adds them to the adapter
    private void fetchArticles(Query query, final int page) {
        //Toast.makeText(this, "Searching for " + query, Toast.LENGTH_LONG).show();
        if(page == 0){//early cleanup
            articles.clear();
            adapter.notifyDataSetChanged();
        }
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
}
