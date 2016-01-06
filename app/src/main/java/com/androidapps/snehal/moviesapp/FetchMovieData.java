package com.androidapps.snehal.moviesapp;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.androidapps.snehal.moviesapp.data.MovieContract;

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

/**
 * Created by snehalsutar on 12/17/15.
 */
public class FetchMovieData extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchMovieData.class.getSimpleName();
    private final Context mContext;

    public FetchMovieData(Context context) {
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
            if (date!=null) {
                mills = date.getTime();
            }else{
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
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, base_poster_path+poster_path);
            movieValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, base_poster_path+backdrop_path);

            cVVector.add(movieValues);

//            results[i] = base_url + poster_path;
//            Log.i(LOG_TAG, base_url + url);
        }
        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            mContext.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,null,null); //.bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
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