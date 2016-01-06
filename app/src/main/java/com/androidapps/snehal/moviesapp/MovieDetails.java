package com.androidapps.snehal.moviesapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidapps.snehal.moviesapp.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;


public class MovieDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }

    /***********************************************************************************************
     * A placeholder fragment containing a simple view.
     **********************************************************************************************/
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
        private String mForecastStr;

        private static final int DETAIL_LOADER = 0;
        private Uri mUri;
        static final String DETAIL_URI = "URI";

        private static final String[] MOVIE_COLUMNS = {
                // In this case the id needs to be fully qualified with a table name, since
                // the content provider joins the location & weather tables in the background
                // (both have an _id column)
                // On the one hand, that's annoying.  On the other, you can search the weather table
                // using the location set by the user, which is only in the Location table.
                // So the convenience is worth it.
                MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                MovieContract.MovieEntry.COLUMN_SORTBY,
                MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
                MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE,
                MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
                MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW,
                MovieContract.MovieEntry.COLUMN_GENRE_IDS,
                MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
                MovieContract.MovieEntry.COLUMN_VOTE_COUNT,
                MovieContract.MovieEntry.COLUMN_POPULARITY,
                MovieContract.MovieEntry.COLUMN_POSTER_PATH,
                MovieContract.MovieEntry.COLUMN_BACKDROP_PATH
        };

        // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
        // must change.
        static final int COL_ID = 0;
        static final int COL_MOVIE_ID = 1;
        static final int COL_SORTBY = 2;
        static final int COL_ORGINAL_TITLE = 3;
        static final int COL_ORGINAL_LANGUAGE = 4;
        static final int COL_RELEASE_DATE = 5;
        static final int COL_MOVIE_OVERVIEW = 6;
        static final int COL_GENRE_IDS = 7;
        static final int COL_VOTE_AVERAGE = 8;
        static final int COL_VOTE_COUNT = 9;
        static final int COL_POPULARITY = 10;
        static final int COL_POSTER_PATH = 11;
        static final int COL_BACKDROP_PATH = 12;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            }
            return inflater.inflate(R.layout.fragment_detail, container, false);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//            // Inflate the menu; this adds items to the action bar if it is present.
//            inflater.inflate(R.menu.detailfragment, menu);
//
//            // Retrieve the share menu item
//            MenuItem menuItem = menu.findItem(R.id.action_share);
//
//            // Get the provider and hold onto it to set/change the share intent.
//            ShareActionProvider mShareActionProvider =
//                    (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
//
//            // Attach an intent to this ShareActionProvider.  You can update this at any time,
//            // like when the user selects a new piece of data they might like to share.
//            if (mShareActionProvider != null ) {
//                mShareActionProvider.setShareIntent(createShareForecastIntent());
//            } else {
//                Log.d(LOG_TAG, "Share Action Provider is null?");
//            }
        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    mForecastStr + FORECAST_SHARE_HASHTAG);
            return shareIntent;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        void onSortBySettingChanged(String sortbySetting) {
            // replace the uri, since the location has changed
            Uri uri = mUri;
            if (null != uri) {
                Uri updatedUri = MovieContract.MovieEntry.buildMovieListWithSortSetting(sortbySetting);
                mUri = updatedUri;
                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
            }
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (null != mUri) {
                // Now create and return a CursorLoader that will take care of
                // creating a Cursor for the data being displayed.
                return new CursorLoader(
                        getActivity(),
                        mUri,
                        MOVIE_COLUMNS,
                        null,
                        null,
                        null
                );
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.v(LOG_TAG, "In onLoadFinished");
            Log.v("Cursor Object SVS", DatabaseUtils.dumpCursorToString(data));
            if (!data.moveToFirst()) {
                return;
            }
            Log.i(LOG_TAG, "SVS DATA.toString()" + data.toString());


//            String movieTitle = data.getString(COL_ORGINAL_TITLE);
//            String movieOverview = data.getString(COL_MOVIE_OVERVIEW);
//            String backdropPath = data.getString(COL_BACKDROP_PATH);

//            String weatherDescription =
//                    data.getString(COL_WEATHER_DESC);
//
//            boolean isMetric = Utility.isMetric(getActivity());
//
//            String high = Utility.formatTemperature(
//                    data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
//
//            String low = Utility.formatTemperature(
//                    data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
//
//            mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

//            TextView detailTextView = (TextView)getView().findViewById(R.id.detail_text);
//            detailTextView.setText(mForecast);


            //--------------------------------------------------------------------------------------
            // Fetch more movie details.

            //--------------------------------------------------------------------------------------

            int idx_movie_poster_path = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH);
            String movie_backdrop_path = data.getString(idx_movie_poster_path);

            ImageView imageView = (ImageView) getView().findViewById(R.id.iv_movie_backdrop);
            Picasso.with(getActivity()).load(movie_backdrop_path).placeholder(R.drawable.ic_launcher_movie).into(imageView);

            Log.i(LOG_TAG, "SVS " + movie_backdrop_path + " " + idx_movie_poster_path);
            //------------MOVIE TITLE-----------------------------------------------------
            int idx_movieTitle = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE);
            String movieTitle = data.getString(idx_movieTitle);

            TextView movieTitleTextView = (TextView) getView().findViewById(R.id.tv_movie_title);
            movieTitleTextView.setText(movieTitle);

            //-----------MOVIE OVERVIEW-------------------------------------------------------------
            int idx_movieOverview = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW);
            String movieOverview = data.getString(idx_movieOverview);

            TextView movieOverviewTextView = (TextView) getView().findViewById(R.id.tv_movie_overview);
            movieOverviewTextView.setText(movieOverview);

            //-----------MOVIE RATING-------------------------------------------------------------
            TextView movieRatingTextView = (TextView) getView().findViewById(R.id.tv_movie_rating);
            movieRatingTextView.setText(data.getString(COL_VOTE_AVERAGE));

            //-----------VOTED BY-------------------------------------------------------------
            TextView movieVotedByTextView = (TextView) getView().findViewById(R.id.tv_movie_votedby);
            movieVotedByTextView.setText(data.getString(COL_VOTE_COUNT) + " users");

            //-----------GENRE List-------------------------------------------------------------
            TextView movieGenreTextView = (TextView) getView().findViewById(R.id.tv_movie_genre);
            movieGenreTextView.setText(Utility.formatGenre(data.getString(COL_GENRE_IDS)));

            //----------- Movie Release Date -------------------------------------------------------
            TextView movieReleaseDate = (TextView) getView().findViewById(R.id.tv_movie_releasedate);
            movieReleaseDate.setText(Utility.formatDate(Long.parseLong(data.getString(COL_RELEASE_DATE))));

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
//            if (mShareActionProvider != null) {
//                mShareActionProvider.setShareIntent(createShareForecastIntent());
//            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    /***********************************************************************************************
     * FETCH MOVIE DETAILS
     **********************************************************************************************/
    public class FetchMovieDetails extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchMovieData.class.getSimpleName();
        private final Context mContext;

        public FetchMovieDetails(Context context) {
            mContext = context;
        }

        private String sortBySetting = "";

        @Override
        protected Void doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;


            try {

                //http://image.tmdb.org/t/p/w185//jjBgi2r5cRt36xF6iNUEhzscEcb.jpg
                //http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=f01dcea8e11e59d50adc583315f97d86
//            http://api.themoviedb.org/3/genre/movie/list?api_key=f01dcea8e11e59d50adc583315f97d86
//            https://api.themoviedb.org/3/movie/11?api_key=f01dcea8e11e59d50adc583315f97d86&append_to_response=releases,trailers

                final String FORECAST_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_BY = "sort_by";
                String sort_by = "popularity.desc";
                sortBySetting = sort_by; //Utility.getSortBySetting(mContext);
                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY, params[0]) //sort_by
                        .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                //Log.i(LOG_TAG, forecastJsonStr);
                populateMoviePosterURLS(forecastJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movies data, there's no point in attemping
                // to parse it.
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

//        try {
//            return populateMoviePosterURLS(forecastJsonStr);
//        } catch (JSONException e) {
//            Log.e(LOG_TAG, e.getMessage(), e);
//            e.printStackTrace();
//        }

            return null;
        }

        private String[] populateMoviePosterURLS(String forecastJsonStr) throws JSONException {


            String base_url = "http://image.tmdb.org/t/p/w185/";
            String[] results;

            final String JSON_PROP_RESULTS = "results";
            final String JSON_PROP_TITLE = "title";
            final String JSON_PROP_POSTER_PATH = "poster_path";
            final String JSON_PROP_OVERVIEW = "overview";
            final String JSON_PROP_RELEASE_DATE = "release_date";
            final String JSON_PROP_GENRE_IDS = "genre_ids";
            final String JSON_PROP_ID = "id";
            final String JSON_PROP_ORIGINAL_TITLE = "original_title";
            final String JSON_PROP_BACKDROP_PATH = "backdrop_path";
            final String JSON_PROP_POPULARITY = "popularity";
            final String JSON_PROP_VOTE_COUNT = "vote_count";
            final String JSON_PROP_VOTE_AVERAGE = "vote_average";
            final String JSON_PROP_ORIGINAL_LANGUAGE = "original_language";
            final String base_poster_path = "http://image.tmdb.org/t/p/w500/";


            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray movieArray = forecastJson.getJSONArray(JSON_PROP_RESULTS);
            results = new String[movieArray.length()];

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());


            for (int i = 0; i < movieArray.length(); i++) {

                JSONObject singleMovie = movieArray.getJSONObject(i);

                String title = singleMovie.getString(JSON_PROP_TITLE);
                String poster_path = singleMovie.getString(JSON_PROP_POSTER_PATH);
                String overview = singleMovie.getString(JSON_PROP_OVERVIEW);
                String release_date = singleMovie.getString(JSON_PROP_RELEASE_DATE);
                int movie_id = singleMovie.getInt(JSON_PROP_ID);
                String original_title = singleMovie.getString(JSON_PROP_ORIGINAL_TITLE);
                String original_language = singleMovie.getString(JSON_PROP_ORIGINAL_LANGUAGE);
                String backdrop_path = singleMovie.getString(JSON_PROP_BACKDROP_PATH);
                long popularity = singleMovie.getLong(JSON_PROP_POPULARITY);
                int vote_count = singleMovie.getInt(JSON_PROP_VOTE_COUNT);
                long vote_average = singleMovie.getLong(JSON_PROP_VOTE_AVERAGE);
                JSONArray genre_ids_array = singleMovie.getJSONArray(JSON_PROP_GENRE_IDS);
                String genre_ids = "";
                for (int id_count = 0; id_count < genre_ids_array.length(); id_count++) {
                    genre_ids = genre_ids + genre_ids_array.getInt(id_count) + ",";
                }

                ContentValues movieValues = new ContentValues();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date = null;
                try {
                    date = (Date) formatter.parse(release_date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
//            Log.i(LOG_TAG,"DATE SVS--" + date);
                long mills;
                if (date != null) {
                    mills = date.getTime();
                } else {
                    mills = System.currentTimeMillis();
                }
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie_id);
                movieValues.put(MovieContract.MovieEntry.COLUMN_SORTBY, sortBySetting);
                movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, original_title);
                movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, original_language);
                movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, mills);
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, overview);
                movieValues.put(MovieContract.MovieEntry.COLUMN_GENRE_IDS, genre_ids);
                movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, vote_average);
                movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, vote_count);
                movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, popularity);
                movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, base_poster_path + poster_path);
                movieValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, base_poster_path + backdrop_path);

                cVVector.add(movieValues);

//            results[i] = base_url + poster_path;
//            Log.i(LOG_TAG, base_url + url);
            }
            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                mContext.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, null, null); //.bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
                inserted = mContext.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchMovieData task Complete. " + inserted + " Inserted");


            return results;
        }

//    @Override
//    protected void onPostExecute(String[] result) {
//        Log.i("SVS", "Into ONPOSTEXECUTE");
//        if (result != null) {
////                for (String moviePosterURLS : result) {
////                    Log.i("SVS", moviePosterURLS);
////                }
//            MainActivityFragment.mMovieAdapter.refreshEvents(result);
//            // New data is back from the server.  Hooray!
//        }
//
//    }
    }
}
