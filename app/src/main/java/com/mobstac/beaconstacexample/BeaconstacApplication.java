package com.mobstac.beaconstacexample;

import android.app.Application;

import com.mobstac.beaconstacexample.db.DatabaseHandler;

import java.io.IOException;

/**
 * Created by admin on 11/17/2015.
 */
public class BeaconstacApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseHandler dbHelper = DatabaseHandler.getInstance(this);
        try {
            dbHelper.copyDataBaseFromAsset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dbHelper.openDataBase();
    }
}
