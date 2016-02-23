package com.mobstac.beaconstacexample.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "prosimity";
    // Database Name
    private static final String DATABASE_PATH = "/data/data/com.mobstac.beaconstacexample/databases/";
    private static DatabaseHandler mInstance;
    private SQLiteDatabase db;
    private static final String TABLE_RULES = "rules";
    // User Table Columns names
    public static final String KEY_ID = "_id";
    public static final String KEY_ACTION = "actions";
    public static final String KEY_NAME = "rule_name";
    public static final String KEY_RULE_ID = "rule_id";
    public static final String KEY_TYPE = "rule_type"; // 1 = Popup, 2 = Webpage, 3 = Card
    public static final String KEY_TEXT = "rule_text";
    public static final String KEY_MEDIA_URL = "rule_media_url";
    public static final String KEY_WEB_URL = "rule_web_url";
    public static final String KEY_IS_READ = "is_read";
//	public static final String KEY_UNIQUE = "unique_key";

    private Context mContext;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static DatabaseHandler getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new DatabaseHandler(ctx.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void copyDataBaseFromAsset() throws IOException {
        InputStream in = mContext.getAssets().open("prosimity.sqlite");
        Log.e("sample", "Starting copying");
        String outputFileName = DATABASE_PATH + DATABASE_NAME;
        File databaseFile = new File(DATABASE_PATH);
        // check if databases folder exists, if not create one and its subfolders
        if (!databaseFile.exists()) {
            databaseFile.mkdir();

            OutputStream out = new FileOutputStream(outputFileName);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            Log.e("sample", "Completed");
            out.flush();
            out.close();
            in.close();
        }

    }

    public void openDataBase() throws SQLException {
        String path = DATABASE_PATH + DATABASE_NAME;
        db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);
    }

    public void closeDatabase() {
        db.close();
    }


    public int getIfAvailable(String ruleId, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RULES, null, KEY_RULE_ID + "=? and " + KEY_TYPE + "=?", new String[]{ruleId, type}, null, null, KEY_ID + " DESC", null);
        return cursor.getCount();
    }

    // Getting not sync user badges
    public Cursor getRules() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RULES, null, null, null, null, null, KEY_ID + " DESC", null);
        return cursor;
    }

    public long addRules(ContentValues ruleValues) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = db.insert(TABLE_RULES, null, ruleValues);
        return id;
    }

    public long updateRule(ContentValues values, String rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = db.update(TABLE_RULES, values, KEY_ID + " = ?", new String[]{rowId});
        return id;
    }

    public int deleteRule(String rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int id = db.delete(TABLE_RULES, KEY_ID + " = ?", new String[]{rowId});
        return id;
    }


//	public ArrayList<Badges> getUserBadgesList(String badgeid) {
//		ArrayList<Badges> badges = new ArrayList<Badges>();
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.query(TABLE_BADGETABLE, null, KEY_BADGEID + "<=?", new String[] { badgeid }, null, null, null, null);
//		if (cursor != null && cursor.getCount() > 0) {
//			cursor.moveToFirst();
//			do {
//				Badges badge = new Badges(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_TITLE)),
//						cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_IMAGEPATH)), cursor.getString(cursor
//								.getColumnIndex(DatabaseHandler.KEY_BADGEID)));
//				badges.add(badge);
//			} while (cursor.moveToNext());
//		}
//
//		return badges;
//
//	}
//
//	public ArrayList<Badges> getUserBadgesList(int distance) {
//		ArrayList<Badges> badges = new ArrayList<Badges>();
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db
//				.query(TABLE_BADGETABLE, null, KEY_GOAL + "<=?", new String[] { distance + "" }, null, null, null, null);
//		if (cursor != null && cursor.getCount() > 0) {
//			cursor.moveToFirst();
//			do {
//				Badges badge = new Badges(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_TITLE)),
//						cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_IMAGEPATH)), cursor.getString(cursor
//								.getColumnIndex(DatabaseHandler.KEY_BADGEID)));
//				badges.add(badge);
//			} while (cursor.moveToNext());
//		}
//
//		return badges;
//
//	}
//
//	public Cursor getBadges() {
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.query(TABLE_BADGETABLE, null, null, null, null, null, null);
//		return cursor;
//	}
//
//	public Cursor getAppliedBadge(String distance) {
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.query(TABLE_BADGETABLE, null, KEY_GOAL + "<=?", new String[] { distance + "" }, null, null, KEY_GOAL
//				+ " DESC");
//		return cursor;
//	}
//
//	// Getting not sync settings
//	public Cursor getUnSyncedSettings() {
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.query(TABLE_SETTING, null, KEY_ISSYNC + "=?", new String[] { "N" }, null, null, null, null);
//		return cursor;
//	}
//
//	public long addWorkout(ContentValues workoutValues) {
//		SQLiteDatabase db = this.getWritableDatabase();
//		long id = db.insert(TABLE_RULES, null, workoutValues);
//		return id;
//	}
//
//	public long editWorkout(ContentValues workoutValues) {
//		SQLiteDatabase db = this.getWritableDatabase();
//		long id = db.update(TABLE_RULES, workoutValues, KEY_WORKOUTID + " = ?",
//				new String[] { (String) workoutValues.get(KEY_WORKOUTID) });
//		return id;
//	}
//
//	public long addUserBadge(ContentValues userBadgeValues) {
//		SQLiteDatabase db = this.getWritableDatabase();
//		long id = db.insert(TABLE_USERBADGE, null, userBadgeValues);
//		return id;
//	}

    // public void addAchivedBadges(String userid, String badgeid) {
    // SQLiteDatabase db = this.getWritableDatabase();
    //
    // ContentValues userBadgeValues = new ContentValues();
    // userBadgeValues.put(KEY_USERID, userid);
    //
    // Cursor cursor = db.query(TABLE_USERBADGE, null, KEY_USERID + " = ?", new String[] { userid }, null, null, null);
    // if (cursor != null && cursor.getCount() > 0) {
    // for (int i = 0; i < cursor.getCount(); i++) {
    // String bageId = cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_BADGEID));
    //
    // userBadgeValues.put(KEY_BADGEID, badgeid);
    // userBadgeValues.put(KEY_ISSYNC, "N");
    // db.insert(TABLE_USERBADGE, null, userBadgeValues);
    // cursor.moveToNext();
    // }
    //
    // } else {
    // int badge = Integer.parseInt(badgeid);
    // for (int i = 1; i <= badge; i++) {
    // userBadgeValues.put(KEY_BADGEID, badgeid);
    // userBadgeValues.put(KEY_ISSYNC, "N");
    // db.insert(TABLE_USERBADGE, null, userBadgeValues);
    // }
    // }
    //
    // }

//	public long addBadge(ContentValues badgeValues) {
//		SQLiteDatabase db = this.getWritableDatabase();
//		long id = db.insert(TABLE_BADGETABLE, null, badgeValues);
//		return id;
//	}
//
//	public long updateBadge(ContentValues badgeValues, String badgeId) {
//		SQLiteDatabase db = this.getWritableDatabase();
//		long id = db.update(TABLE_BADGETABLE, badgeValues, KEY_BADGEID + " = ?", new String[] { badgeId });
//		return id;
//	}
//
//	public int updateBadge(ContentValues badgeValues) {
//		SQLiteDatabase db = this.getWritableDatabase();
//		int rowsAffected = db.update(TABLE_BADGETABLE, badgeValues, null, null);
//		return rowsAffected;
//	}
//
//	public long addSettings(String userid, String username, String firstname, String lastname, String email, String gender,
//			String dob, String country, String height, String weight, String distanceunit, String heightunit, String weightunit,
//			String imageurl, String imagepath, String goal, String activityViewedBy, String isSync, String view) {
//		SQLiteDatabase db = this.getWritableDatabase();
//
//		ContentValues settingsValues = new ContentValues();
//		settingsValues.put(KEY_USERID, userid);
//		settingsValues.put(KEY_USERNAME, username);
//		settingsValues.put(KEY_FIRSTNAME, firstname);
//		settingsValues.put(KEY_LASTNAME, lastname);
//		settingsValues.put(KEY_EMAIL, email);
//		settingsValues.put(KEY_GENDER, gender);
//		settingsValues.put(KEY_DOB, dob);
//		settingsValues.put(KEY_COUNTRY, country);
//		settingsValues.put(KEY_HEIGHT, height);
//		settingsValues.put(KEY_WEIGHT, weight);
//		settingsValues.put(KEY_DISTANCEUNIT, distanceunit);
//		settingsValues.put(KEY_HEIGHTUNIT, heightunit);
//		settingsValues.put(KEY_WEIGHTUNIT, weightunit);
//		if (!TextUtils.isEmpty(imageurl)) {
//			if (imageurl.startsWith("http"))
//				settingsValues.put(DatabaseHandler.KEY_IMAGEURL, imageurl);
//			else
//				settingsValues.put(DatabaseHandler.KEY_IMAGEURL, UserFunctions.SERVER + imageurl);
//		}
//		settingsValues.put(KEY_IMAGEPATH, imagepath);
//		settingsValues.put(KEY_GOAL, goal);
//		settingsValues.put(KEY_ACTIVITYVIEWEDBY, activityViewedBy);
//		settingsValues.put(KEY_ISSYNC, isSync);
//		settingsValues.put(KEY_ACTIVITYVIEWEDBY, view);
//		long id = db.insert(TABLE_SETTING, null, settingsValues);
//		return id;
//	}
//
//	public ArrayList<Workout> getWorkoutList(String userid) {
//		String workoutDate;
//		String workoutDistance;
//		String workoutTime;
//		String workoutCalories;
//		SQLiteDatabase db = this.getReadableDatabase();
//		int totalDistance = getTotalDistance(db, userid);
//		Cursor cursor = db.query(TABLE_RULES, null, KEY_USERID + "=?", new String[] { userid }, null, null, KEY_WORKOUTDATE
//				+ " DESC");
//		ArrayList<Workout> workoutList = new ArrayList<Workout>();
//		if (cursor != null && cursor.getCount() > 0) {
//			cursor.moveToFirst();
//			do {
//				workoutDate = cursor.getString(cursor.getColumnIndex(KEY_WORKOUTDATE));
//				workoutDistance = cursor.getString(cursor.getColumnIndex(KEY_DISTANCE));
//				workoutTime = cursor.getString(cursor.getColumnIndex(KEY_TIME));
//				workoutCalories = cursor.getString(cursor.getColumnIndex(KEY_CALORIES));
//				String workoutLocation = cursor.getString(cursor.getColumnIndex(KEY_LOCATION));
//				String workoutImageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGEURL));
//				String workoutImagePath = cursor.getString(cursor.getColumnIndex(KEY_IMAGEPATH));
//				String workoutNotes = cursor.getString(cursor.getColumnIndex(KEY_NOTES));
//				String workoutRate = cursor.getString(cursor.getColumnIndex(KEY_RATE));
//				String workoutId = cursor.getString(cursor.getColumnIndex(KEY_WORKOUTID));
//				Workout workoutObj = new Workout(workoutDate, workoutDistance, workoutTime, workoutCalories, workoutImageUrl,
//						workoutImagePath, totalDistance, workoutLocation, workoutNotes, workoutRate, workoutId);
//				workoutList.add(workoutObj);
//			} while (cursor.moveToNext());
//		}
//
//		return workoutList;
//	}
//
//	public int getTotalDistance(SQLiteDatabase db, String userid) {
//		if (db == null)
//			db = this.getWritableDatabase();
//		String[] columns = new String[] { "sum(" + KEY_DISTANCE + ")" };
//		Cursor sum = db.query(TABLE_RULES, columns, KEY_USERID + "=?", new String[] { userid }, null, null, null);
//		if (sum.moveToFirst()) {
//			return sum.getInt(0);
//		}
//		return 0;
//	}
//
//	public int getTotalWorkouts(String userid) {
//		db = this.getWritableDatabase();
//
//		Cursor sum = db.query(TABLE_RULES, null, KEY_USERID + "=?", new String[] { userid }, null, null, null);
//		if (sum != null && sum.moveToFirst()) {
//			return sum.getCount();
//		}
//		return 0;
//	}
//
//	public int getTotalTime(String userid) {
//		if (db == null)
//			db = this.getWritableDatabase();
//		String[] columns = new String[] { "sum(" + KEY_TIME + ")" };
//		Cursor sum = db.query(TABLE_RULES, columns, KEY_USERID + "=?", new String[] { userid }, null, null, null);
//		if (sum.moveToFirst()) {
//			return sum.getInt(0);
//		}
//
//		return 0;
//	}
//
//	public int getTotalCalories(String userid) {
//		if (db == null)
//			db = this.getWritableDatabase();
//		String[] columns = new String[] { "sum(" + KEY_CALORIES + ")" };
//		Cursor sum = db.query(TABLE_RULES, columns, KEY_USERID + "=?", new String[] { userid }, null, null, null);
//		if (sum.moveToFirst()) {
//			return sum.getInt(0);
//		}
//
//		return 0;
//	}
//
//	public long updateSettings(ContentValues settingsValues, String userid) {
//		SQLiteDatabase db = this.getWritableDatabase();
//		long id = db.update(TABLE_SETTING, settingsValues, KEY_USERID + " = ?", new String[] { userid });
//		return id;
//	}
//
//	public long updateWorkout(ContentValues workoutValues, String userid, String workoutdate) {
//		SQLiteDatabase db = this.getWritableDatabase();
//		long id = db.update(TABLE_RULES, workoutValues, KEY_USERID + " = ?" + " AND " + KEY_WORKOUTDATE + " = ?",
//				new String[] { userid, workoutdate });
//		return id;
//	}
//
//	public Cursor getSettings(String userid) {
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.query(TABLE_SETTING, null, KEY_USERID + " = ?", new String[] { userid }, null, null, null);
//		return cursor;
//	}
//
//	public boolean checkBadge(String userid, String bageId) {
//		Cursor getcursor = db.query(TABLE_USERBADGE, null, null, null, null, null, null);
//		if (getcursor != null && getcursor.getCount() == 0) {
//			return true;
//		}
//
//		Cursor cursor = db.query(TABLE_USERBADGE, null, KEY_USERID + " = ?" + " AND " + KEY_BADGEID + " = ?", new String[] {
//				userid, bageId }, null, null, null);
//		if (cursor.getCount() > 0)
//			return false;
//		else
//			return true;
//	}
//
//	public Cursor getGraphData(String userid, String startDay, String endDay, int selected, int type) {
//		String rawQuery = "";
//		String function = "";
//		String key = "";
//
//		switch (type) {
//		case 1:
//			function = "%w";
//			break;
//		case 2:
//			function = "%d";
//			break;
//		case 3:
//			function = "%m";
//			break;
//
//		default:
//			break;
//		}
//		switch (selected) {
//		case 1:
//			key = KEY_DISTANCE;
//			break;
//		case 2:
//			key = KEY_TIME;
//			break;
//		case 3:
//			key = KEY_CALORIES;
//			break;
//		default:
//			break;
//		}
//
//		rawQuery = "SELECT SUM(" + key + ") as total,strftime('" + function + "'," + KEY_WORKOUTDATE
//				+ ",'unixepoch') as wday FROM " + TABLE_RULES + " where " + KEY_WORKOUTDATE + " >=" + startDay + " AND "
//				+ KEY_WORKOUTDATE + " <=" + endDay + " AND " + KEY_USERID + " = " + userid + " group by wday";
//
//		return db.rawQuery(rawQuery, null);
//	}
}
