package com.androidapps.snehal.moviesapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.androidapps.snehal.moviesapp.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * Created by snehalsutar on 12/15/15.
 */
public class TestUtilities extends AndroidTestCase{
    static final String TEST_MOVIE_ID = "99705";
    static final String TEST_RATING = "3";
    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        Students: Use this to create some default weather values for your database tests.
     */
//    static ContentValues createMovieValues(long locationRowId) {
//        ContentValues weatherValues = new ContentValues();
//        weatherValues.put(MovieContract.MovieEntry.COLUMN_LOC_KEY, locationRowId);
//        weatherValues.put(MovieContract.MovieEntry.COLUMN_DATE, TEST_DATE);
//        weatherValues.put(MovieContract.MovieEntry.COLUMN_DEGREES, 1.1);
//        weatherValues.put(MovieContract.MovieEntry.COLUMN_HUMIDITY, 1.2);
//        weatherValues.put(MovieContract.MovieEntry.COLUMN_PRESSURE, 1.3);
//        weatherValues.put(MovieContract.MovieEntry.COLUMN_MAX_TEMP, 75);
//        weatherValues.put(MovieContract.MovieEntry.COLUMN_MIN_TEMP, 65);
//        weatherValues.put(MovieContract.MovieEntry.COLUMN_SHORT_DESC, "Asteroids");
//        weatherValues.put(MovieContract.MovieEntry.COLUMN_WIND_SPEED, 5.5);
//        weatherValues.put(MovieContract.MovieEntry.COLUMN_WEATHER_ID, 321);
//
//        return weatherValues;
//    }

    /*
        Students: You can uncomment this helper function once you have finished creating the
        LocationEntry part of the WeatherContract.
     */
    static ContentValues createMovieValues() {

        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "123456");
        testValues.put(MovieContract.MovieEntry.COLUMN_SORTBY, "popularity");
        testValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, "Tinkerbell 0");
        testValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, "EN0");
        testValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, TEST_DATE);
        testValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, "North Pole");
        testValues.put(MovieContract.MovieEntry.COLUMN_GENRE_IDS, "1,2,3");
        testValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 4);
        testValues.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, 22);
        testValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, "4");
        testValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "abc.xyz");
        testValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, "abc.svs");

        return testValues;
    }

    static ContentValues createMovieValues1() {

        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "99999");
        testValues.put(MovieContract.MovieEntry.COLUMN_SORTBY, "popularity");
        testValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, "Tinkerbell 0");
        testValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, "EN0");
        testValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, TEST_DATE);
        testValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, "North Pole");
        testValues.put(MovieContract.MovieEntry.COLUMN_GENRE_IDS, "1,2,3");
        testValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 4);
        testValues.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, 22);
        testValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, "4");
        testValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "abc.xyz");
        testValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, "abc.svs");

        return testValues;
    }

    /*
        Students: You can uncomment this function once you have finished creating the
        LocationEntry part of the WeatherContract as well as the WeatherDbHelper.
     */
    static long insertMovieValues(Context context) {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createMovieValues();

        long locationRowId;
        locationRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert North Pole Location Values", locationRowId != -1);

        return locationRowId;
    }

    /*
        Students: The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
