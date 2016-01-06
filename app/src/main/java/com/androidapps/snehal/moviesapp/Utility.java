package com.androidapps.snehal.moviesapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by snehalsutar on 12/24/15.
 */
public class Utility {
    public static HashMap<Integer, String> mhashMapGenre;

    public static String getSortBySetting(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sortby_key),
                context.getString(R.string.pref_sortby_default));
    }


    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    static String formatGenre(String genre_list_id) {
        create_hashmapGenre();
        String genre_list_names = "";
        String[] genre_list_id_arr = genre_list_id.split(",");
        int i;
            for ( i=0;i<genre_list_id_arr.length-1; i++){
                if(mhashMapGenre.containsKey(Integer.parseInt(genre_list_id_arr[i]))){
                    genre_list_names = genre_list_names +mhashMapGenre.get(Integer.parseInt(genre_list_id_arr[i])) + " , ";
                }
            }
        //For last genre which should not be appended by comma.
        if(mhashMapGenre.containsKey(Integer.parseInt(genre_list_id_arr[i]))){
            genre_list_names = genre_list_names+ mhashMapGenre.get(Integer.parseInt(genre_list_id_arr[i]));
        }

        return genre_list_names;
    }

    private static void create_hashmapGenre() {
        mhashMapGenre = new HashMap<>();

        mhashMapGenre.put(28, "Action");
        mhashMapGenre.put(12, "Adventure");
        mhashMapGenre.put(16, "Animation");
        mhashMapGenre.put(35, "Comedy");
        mhashMapGenre.put(80, "Crime");
        mhashMapGenre.put(99, "Documentary");
        mhashMapGenre.put(18, "Drama");
        mhashMapGenre.put(10751, "Family");
        mhashMapGenre.put(14, "Fantasy");
        mhashMapGenre.put(10769, "Foreign");
        mhashMapGenre.put(36, "History");
        mhashMapGenre.put(10402, "Music");
        mhashMapGenre.put(9648, "Mystery");
        mhashMapGenre.put(10749, "Romance");
        mhashMapGenre.put(53, "Thriller");
        mhashMapGenre.put(10752, "War");
        mhashMapGenre.put(37, "Western");
        mhashMapGenre.put(10770, "TV Movie");
        mhashMapGenre.put(878, "Science Fiction");
    }


}
