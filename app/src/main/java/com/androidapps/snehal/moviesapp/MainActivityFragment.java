package com.androidapps.snehal.moviesapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.androidapps.snehal.moviesapp.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    //    //private ArrayAdapter<String> mMovieAdapter;
////    public static ImageListAdapter mMovieAdapter;
//    public static String[] eatFoodyImages
//            = {
//            "http://i.imgur.com/rFLNqWI.jpg",
//            "http://i.imgur.com/C9pBVt7.jpg",
//            "http://i.imgur.com/rT5vXE1.jpg",
//            "http://i.imgur.com/aIy5R2k.jpg",
//            "http://i.imgur.com/MoJs9pT.jpg",
//            "http://i.imgur.com/S963yEM.jpg",
//            "http://i.imgur.com/rLR2cyc.jpg",
//            "http://i.imgur.com/SEPdUIx.jpg",
//            "http://i.imgur.com/aC9OjaM.jpg",
//            "http://i.imgur.com/76Jfv9b.jpg",
//            "http://i.imgur.com/fUX7EIB.jpg",
//            "http://i.imgur.com/syELajx.jpg",
//            "http://i.imgur.com/COzBnru.jpg",
//            "http://i.imgur.com/Z3QjilA.jpg",
//
//    };
    private static final int MOVIE_LOADER = 0;

    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
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

    private MovieAdapter mMovieAdapter;


    public MainActivityFragment() {
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void  onItemSelected(Uri dateUri);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The CursorAdapter will take data from our cursor and populate the ListView.
        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movielist);
//        mMovieAdapter = new ImageListAdapter(getActivity().getApplicationContext(), eatFoodyImages);

        gridView.setAdapter(mMovieAdapter);
        // We'll call our MainActivity
//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                // CursorAdapter returns a cursor at the correct position for getItem(), or null
//                // if it cannot seek to that position.
//                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
//                if (cursor != null) {
//                    Log.i("SVS", "-----" + cursor.getInt(COL_MOVIE_ID));
//                    Intent intent = new Intent(getActivity(), MovieDetails.class)
//                            .setData(MovieContract.MovieEntry.buildMovieWithMovieId(cursor.getString(COL_MOVIE_ID)));
//                    startActivity(intent);
//                }
//                mPosition = position;
//            }
//        });

        // We'll call our MainActivity
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(MovieContract.MovieEntry.buildMovieWithMovieId(cursor.getString(COL_MOVIE_ID)
                            ));
                }
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }
//    @Override
//    public void onStart() {
//        super.onStart();
//        updateMovies();
//        mMovieAdapter.refreshEvents(eatFoodyImages);
//    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onSortBySettingChanged() {
        updateMovies();
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    private void updateMovies() {
        FetchMovieData fetchMovieData = new FetchMovieData(getActivity());
        String sortby = Utility.getSortBySetting(getActivity());
        fetchMovieData.execute(sortby);
    }

    /***********************************************************************************************
     * MEthods to be implemented for Loader Callback.
     **********************************************************************************************/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortby_setting = Utility.getSortBySetting(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        Uri weatherForLocationUri = MovieContract.MovieEntry.buildMovieListWithSortSetting(sortby_setting);

        Log.i(LOG_TAG, "SVS " +weatherForLocationUri);
        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                MOVIE_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

    /***********************************************************************************************
     *
     **********************************************************************************************/
    public class ImageListAdapter extends ArrayAdapter {
        private Context context;
        private LayoutInflater inflater;

        private String[] imageUrls;

        public ImageListAdapter(Context context, String[] imageUrls) {
            super(context, R.layout.grid_view_img, imageUrls);

            this.context = context;
            this.imageUrls = imageUrls;

            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return imageUrls.length;
        }

        public void refreshEvents(String[] imageUrls) {
            this.imageUrls = imageUrls;
            //this.events.addAll(events);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.grid_view_img, parent, false);
            }

            Picasso
                    .with(context)
                    .load(imageUrls[position])
                    .placeholder(R.drawable.ic_launcher_movie)
                    .fit() // will explain later
                    .into((ImageView) convertView);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String forecast = imageUrls[pos]; //(String) mMovieAdapter.getItem(position);// .getItem(position);
                    Intent intent = new Intent(getActivity(), MovieDetails.class)
                            .putExtra(Intent.EXTRA_TEXT, forecast);
                    startActivity(intent);
                }
            });

            return convertView;
        }
    }


}
