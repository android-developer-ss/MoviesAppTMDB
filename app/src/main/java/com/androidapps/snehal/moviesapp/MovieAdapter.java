package com.androidapps.snehal.moviesapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.androidapps.snehal.moviesapp.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by snehalsutar on 12/24/15.
 */
public class MovieAdapter extends CursorAdapter {
    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.grid_view_img, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        ImageView imageView = (ImageView)view;
        //tv.setText(convertCursorRowToUXFormat(cursor));
        Picasso.with(context).load(convertCursorRowToUXFormat(cursor)).placeholder(R.drawable.ic_launcher_movie).into(imageView);

    }

    //----------------------------------------------------------------------------------------------
    private String convertCursorRowToUXFormat(Cursor cursor) {
        final String base_poster_path = "http://image.tmdb.org/t/p/w185/";
        // get row indices for our cursor
//        int idx_max_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
//        int idx_min_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
//        int idx_date = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
//        int idx_short_desc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);
//
//        String highAndLow = formatHighLows(
//                cursor.getDouble(idx_max_temp),
//                cursor.getDouble(idx_min_temp));
//
//        return Utility.formatDate(cursor.getLong(idx_date)) +
//                " - " + cursor.getString(idx_short_desc) +
//                " - " + highAndLow;

        int idx_movie_poster_path = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
        String movie_poster_path = cursor.getString(idx_movie_poster_path);
//        Log.i("SVS", /movie_poster_path);
        return movie_poster_path;
    }

}
