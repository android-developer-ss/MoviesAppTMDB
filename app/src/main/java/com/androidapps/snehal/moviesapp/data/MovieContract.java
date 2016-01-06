package com.androidapps.snehal.moviesapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;
import android.util.Log;

/**
 * Created by snehalsutar on 12/15/15.
 */
public class MovieContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.androidapps.snehal.moviesapp";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_MOVIE = "movie";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
//    public static long normalizeDate(long startDate) {
//        // get the supported ids for GMT-08:00 (Pacific Standard Time)
//        String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
//        // if no ids were returned, something is wrong. get out.
//        if (ids.length == 0)
//            System.exit(0);
//        // create a Pacific Standard Time time zone
//        SimpleTimeZone pdt = new SimpleTimeZone(-8 * 60 * 60 * 1000, ids[0]);
//        // normalize the start date to the beginning of the (UTC) day
//        GregorianCalendar time = new GregorianCalendar(pdt);
//        time.setTimeInMillis(startDate);
////        Time time = new Time();
////        time.set(startDate);
////        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
//        return time.setJulianDay(julianDay);
//    }

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /***********************************************************************************************
     * Inner class that defines the contents of the movies table
     *********************************************************************************************/
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_SORTBY = "sortby_setting";

        // Movie Id returned from movie database
        public static final String COLUMN_MOVIE_ID = "movie_id";

        // Original movie title, provided by the API.
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";

        // Original release date of the movie provided by the movies API.
        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final String COLUMN_MOVIE_OVERVIEW = "movie_overview";
        public static final String COLUMN_GENRE_IDS = "genre_ids";

        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";




        /*******************************************************************************************
         * BUILD Queries
         ******************************************************************************************/
        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        //------------------------------------------------------------------------------------------
        // The following query will be built
        // content://com.androidapps.snehal.moviesapp/movie_id/

        public static Uri buildMovieWithMovieId(String movie_id) {
            //Log.i("SVS ", "InMovie Contract -- " + movie_id + " PATH  " + CONTENT_URI.buildUpon().appendPath(movie_id).build());
            return CONTENT_URI.buildUpon().appendPath(movie_id).build();
        }
        //------------------------------------------------------------------------------------------

        public static Uri buildMovieListWithSortSetting(String sortby) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_SORTBY, sortby).build();
        }
        //------------------------------------------------------------------------------------------
        // The following query will be built
        // content://com.androidapps.snehal.moviesapp/vote_average=rating&release_date=date
        public static Uri buildMovieWithRatingAndReleaseDate(String rating, long release_date) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_VOTE_AVERAGE, rating)
                    .appendQueryParameter(COLUMN_RELEASE_DATE, Long.toString(normalizeDate(release_date))).build();
        }

        /*******************************************************************************************
         * GET parameters from queries.
         ******************************************************************************************/

        public static String getMovieIdFromUri(Uri uri) {

//            return uri.getPathSegments().get(1);
            Log.i("SVS", " in getMovieIdFromUri--- " + uri.getPathSegments().get(1));
//            return uri.getQueryParameter(COLUMN_MOVIE_ID);
            return uri.getPathSegments().get(1);
        }

        //------------------------------------------------------------------------------------------
        public static long getRatingFromUri(Uri uri) {
            String rating = uri.getQueryParameter(COLUMN_VOTE_AVERAGE);
            if (null != rating && rating.length() > 0)
                return Long.parseLong(rating);
            else
                return 0;
        }
        //------------------------------------------------------------------------------------------
        public static String getSortSettingFromUri(Uri uri) {
            String sortSetting = uri.getQueryParameter(COLUMN_SORTBY);
            if (null != sortSetting && sortSetting.length() > 0)
                return sortSetting;
            else
                return null;
        }
        //------------------------------------------------------------------------------------------
        public static long getReleaseDateFromUri(Uri uri) {
            String dateString = uri.getQueryParameter(COLUMN_RELEASE_DATE);
            if (null != dateString && dateString.length() > 0)
                return Long.parseLong(dateString);
            else
                return 0;
        }
        //------------------------------------------------------------------------------------------
    }
}
