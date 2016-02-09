package com.androidapps.snehal.moviesapp;

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
import android.support.v7.app.ActionBar;
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
import android.widget.Toast;

import com.androidapps.snehal.moviesapp.data.MovieContract;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;
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


public class MovieDetails extends AppCompatActivity {

    static ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

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
    public static class DetailFragment extends Fragment
            implements LoaderManager.LoaderCallbacks<Cursor>, FetchMovieTrailerLink {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
        private String mForecastStr;

        private static final int DETAIL_LOADER = 0;
        private Uri mUri;
        static final String DETAIL_URI = "URI";
        protected Context mContext;
        protected View mRootView;
        // YouTube player view
        private YouTubePlayerView youTubeView;
        private static final int RECOVERY_DIALOG_REQUEST = 1;

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
            mRootView = inflater.inflate(R.layout.fragment_detail, container, false);
            return mRootView;
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
            mContext = getActivity();
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

            actionBar.setTitle(movieTitle);
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

            FetchMovieDetails fetchMovieDetails = new FetchMovieDetails(mContext, this);
            fetchMovieDetails.execute(data.getString(COL_MOVIE_ID));

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }

        @Override
        public void setTrailerLink(String str) {

            TextView textViewYL = (TextView) mRootView.findViewById(R.id.tv_youtube_link);
            textViewYL.setText(" LINK: " + str);
//
//            // Initializing video player with developer key
//            youTubeView.initialize(BuildConfig.YOUTUBE_KEY, this);
            PlayerYouTubeFrag myFragment = PlayerYouTubeFrag.newInstance(str);
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_youtube_container, myFragment).commit();
        }

    }

    /***********************************************************************************************
     * FETCH MOVIE DETAILS
     **********************************************************************************************/
    public static class FetchMovieDetails extends AsyncTask<String, String, String> {

        private final String LOG_TAG = FetchMovieData.class.getSimpleName();
        private final Context mContext;

        FetchMovieTrailerLink fetchMovieTrailerLink;

        public FetchMovieDetails(Context context, FetchMovieTrailerLink fetchMovieTrailerLink) {
            mContext = context;
            this.fetchMovieTrailerLink = fetchMovieTrailerLink;
        }

        private String sortBySetting = "";
        String youtubeSubLink = "";

        @Override
        protected String doInBackground(String... params) {

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
//                https://youtu.be/

                final String MOVIE_TRAILER_BASE_URL = "https://api.themoviedb.org/3/movie/";
                final String APPEND_TO_RESPONSE = "append_to_response";
                final String trailers = "trailers";
                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_TRAILER_BASE_URL).buildUpon().appendPath(params[0])
                        .appendQueryParameter(APPEND_TO_RESPONSE, trailers) //sort_by
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
                youtubeSubLink = populateMoviePosterURLS(forecastJsonStr);
                //tv_youtube_link

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

            return youtubeSubLink;
        }

        private String populateMoviePosterURLS(String jsonObjectString) throws JSONException {
            final String TRAILERS = "trailers";
            final String YOUTUBE = "youtube";
            final String SOURCE = "source";

            JSONObject movieDetailsJson = new JSONObject(jsonObjectString);
            JSONObject jsonObjectTrailers = movieDetailsJson.getJSONObject(TRAILERS);
            JSONArray jsonArrayYoutube = jsonObjectTrailers.getJSONArray(YOUTUBE);
            JSONObject jsonObjectYoutube0 = jsonArrayYoutube.getJSONObject(0);
            String youtubeSubLink = jsonObjectYoutube0.getString(SOURCE);

            return youtubeSubLink;
        }

        @Override
        protected void onPostExecute(String str) {
            super.onPostExecute(str);
            this.fetchMovieTrailerLink.setTrailerLink(str);
        }

    }

    public interface FetchMovieTrailerLink {
        void setTrailerLink(String str);
    }

    public static class PlayerYouTubeFrag extends YouTubePlayerSupportFragment {

        private String currentVideoID = "_oEA18Y8gM0";
        private YouTubePlayer activePlayer;

        public static PlayerYouTubeFrag newInstance(String url) {

            PlayerYouTubeFrag playerYouTubeFrag = new PlayerYouTubeFrag();

            Bundle bundle = new Bundle();
            bundle.putString("url", url);

            playerYouTubeFrag.setArguments(bundle);
            playerYouTubeFrag.init();

            return playerYouTubeFrag;
        }

        private void init() {

            initialize(BuildConfig.YOUTUBE_KEY, new YouTubePlayer.OnInitializedListener() {

                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
                    if (!wasRestored) {

                        // loadVideo() will auto play video
                        // Use cueVideo() method, if you don't want to play it automatically
                        youTubePlayer.cueVideo(getArguments().getString("url"), 0);

                        // Hiding player controls
                        youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);//.CHROMELESS);
                    }
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                    YouTubeInitializationResult errorReason) {

                    if (errorReason.isUserRecoverableError()) {
                        errorReason.getErrorDialog(getActivity(), 1).show();
                    } else {
                        String errorMessage = String.format(
                                getString(R.string.error_player), errorReason.toString());
                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
                
            });
        }


//        @Override
//        public void onYouTubeVideoPaused() {
//            activePlayer.pause();
//        }
    }
}
