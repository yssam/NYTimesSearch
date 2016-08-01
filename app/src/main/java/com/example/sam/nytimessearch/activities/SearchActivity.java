package com.example.sam.nytimessearch.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.astuetz.PagerSlidingTabStrip;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.example.sam.nytimessearch.PageFragment;
import com.example.sam.nytimessearch.R;
import com.example.sam.nytimessearch.adapter.ArticleArrayAdapter;
import com.example.sam.nytimessearch.adapter.SmartFragmentStatePagerAdapter;
import com.example.sam.nytimessearch.adapter.searchFragmentPagerAdapter;
import com.example.sam.nytimessearch.model.Article;
import com.example.sam.nytimessearch.model.Query;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.parceler.Parcels;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.sam.nytimessearch.R.array.sortItems;

public class SearchActivity extends AppCompatActivity implements CalendarDatePickerDialogFragment.OnDateSetListener{

    @BindView(R.id.rvResults) RecyclerView rvResults;
    @BindView(R.id.toolbar) Toolbar toolbar;
    Query query;
    private static final String TAG_CODE = "2431"; // DialogFragment Tag

    ArrayList<Article> articles;
    ArticleArrayAdapter adapter;
    ViewPager viewPager;
    private SmartFragmentStatePagerAdapter adapterViewPager;
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
        Toast.makeText(SearchActivity.this,
                "is network available: " + isNetworkAvailable(), Toast.LENGTH_SHORT).show();
        Toast.makeText(SearchActivity.this,
                "is online: " + isOnline(), Toast.LENGTH_SHORT).show();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void setupViews() {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setClipToPadding(false);
        viewPager.setPageMargin(12);
        adapterViewPager = new searchFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);

        // Give the PagerSlidingTabStrip the ViewPager
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                Toast.makeText(SearchActivity.this,
                        "Selected page position: " + position, Toast.LENGTH_SHORT).show();
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
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
                        final MaterialDialog md = new MaterialDialog.Builder(SearchActivity.this)
                                .title("Progress Dialog")
                                .content("Please wait for a while...")
                                .progress(true, 0)
                                .show();
                        int vpCurrent = viewPager.getCurrentItem();
                        PageFragment pageFragment = (PageFragment) adapterViewPager.getRegisteredFragment(vpCurrent);
                        query = pageFragment.getQuery();
                        query.setQ(q);
                        pageFragment.setQuery(query);

                        // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                        // see https://code.google.com/p/android/issues/detail?id=24599
                        searchView.clearFocus();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                // Actions to do after 10 seconds
                                md.dismiss();
                            }
                        }, 1000);
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
                                                final MaterialDialog md = new MaterialDialog.Builder(SearchActivity.this)
                                                        .title("Progress Dialog")
                                                        .content("Please wait for a while...")
                                                        .progress(true, 0)
                                                        .show();
                                                int vpCurrent = viewPager.getCurrentItem();
                                                PageFragment pageFragment = (PageFragment) adapterViewPager.getRegisteredFragment(vpCurrent);
                                                if(which == 0){
                                                    query = pageFragment.getQuery();
                                                    query.setSort("newest");
                                                    pageFragment.setQuery(query);
                                                }
                                                else{
                                                    query = pageFragment.getQuery();
                                                    query.setSort("oldest");
                                                    pageFragment.setQuery(query);
                                                }
                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    public void run() {
                                                        // Actions to do after 10 seconds
                                                        md.dismiss();
                                                    }
                                                }, 1000);
                                                return true;
                                            }
                                        })
                                        .positiveText(R.string.choose)
                                        .negativeText(R.string.cancel)
                                        .show();
                            break;
                            case 1: //begin date
                                Calendar cal = Calendar.getInstance();
                                CalendarDatePickerDialogFragment cdp =
                                        new CalendarDatePickerDialogFragment()
                                                //.setThemeCustom(R.style.DateTheme)
                                                .setOnDateSetListener(SearchActivity.this)
                                                .setPreselectedDate(cal.get(Calendar.YEAR),
                                                        cal.get(Calendar.MONTH),
                                                        cal.get(Calendar.DATE));
                                cdp.show(getSupportFragmentManager(), TAG_CODE);

                            break;
                            default:
                            break;
                        }
                    }
                })
                .show();
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

    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog,
                          int year,
                          int monthOfYear,
                          int dayOfMonth) {
        // TODO: try other libraries instead of android-betterpickers
        final MaterialDialog md = new MaterialDialog.Builder(this)
                .title("Progress Dialog")
                .content("Please wait for a while...")
                .progress(true, 0)
                .show();
        Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String formattedDate = df.format(cal.getTime());
        Toast.makeText(SearchActivity.this,
                "time =  " + formattedDate, Toast.LENGTH_SHORT).show();

        int vpCurrent = viewPager.getCurrentItem();
        PageFragment pageFragment = (PageFragment) adapterViewPager.getRegisteredFragment(vpCurrent);
        query = pageFragment.getQuery();
        query.setBegin_date(formattedDate);
        pageFragment.setQuery(query);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // Actions to do after 10 seconds
                md.dismiss();
            }
        }, 1000);

    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }
}
