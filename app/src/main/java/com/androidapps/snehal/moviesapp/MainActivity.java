package com.androidapps.snehal.moviesapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.androidapps.snehal.moviesapp.MovieDetails.DetailFragment;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {

    private final String DETAILFRAGMENT_TAG = "FFTAG";
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private String mSortBySetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSortBySetting = Utility.getSortBySetting(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment, new MainActivityFragment(), DETAILFRAGMENT_TAG)
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //MainActivityFragment.mMovieAdapter.notifyDataSetChanged();
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
//        super.onResume();
//        String sortBySetting = Utility.getSortBySetting(this);
//        // update the location in our second pane using the fragment manager
//        if (sortBySetting != null && !sortBySetting.equals(mSortBySetting)) {
//            MainActivityFragment ff = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
//            if (null != ff) {
//                ff.onSortBySettingChanged();
//            }
//            mSortBySetting = sortBySetting;
//        }
        super.onResume();
        String sortBySetting = Utility.getSortBySetting(this);
        // update the location in our second pane using the fragment manager
        if (sortBySetting != null && !sortBySetting.equals(mSortBySetting)) {
            MainActivityFragment ff = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
            if (null != ff) {
                ff.onSortBySettingChanged();
            }
            DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (null != df) {
                df.onSortBySettingChanged(sortBySetting);
            }
            mSortBySetting = sortBySetting;
        }
    }

    @Override
    public void onItemSelected(Uri contentUri) {
//        if (mTwoPane) {
//            // In two-pane mode, show the detail view in this activity by
//            // adding or replacing the detail fragment using a
//            // fragment transaction.
//            Bundle args = new Bundle();
//            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);
//
//            DetailFragment fragment = new DetailFragment();
//            fragment.setArguments(args);
//
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
//                    .commit();
//        } else {
        Intent intent = new Intent(this, MovieDetails.class)
                .setData(contentUri);
        startActivity(intent);
//        }
    }
}
