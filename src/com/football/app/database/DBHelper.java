package com.football.app.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.football.app.util.Constants;

/*
 * 
 * This class is a helper class to query data from DB
 * It creates open helper instance and open DB to read or write data to it
 * All the queries to DB are added in this class 
 */

public class DBHelper {

	private static final String TAG = "Database";
	// Database fields
	private SQLiteDatabase database;
	private OpenHelper dbHelper;

	public DBHelper(Context context) {
		dbHelper = new OpenHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	/*
	 * Gets and returns the current WC locations from database table FIFA_WC_INFO 
	 */
	public ArrayList<FifaWCLocation> getCurrentWorldCupLocations() {
		ArrayList<FifaWCLocation> list = null;
		Cursor cursor = null;
		if (database != null) {
			try {
				cursor = database.rawQuery("select * from "
						+ Constants.FIFA_WC_INFO + " where " + Constants.YEAR
						+ "=" + Constants.CURRENT_YEAR, null);
				if (cursor != null) {
					list = new ArrayList<FifaWCLocation>();
					while (cursor.moveToNext()) {
						FifaWCLocation l = new FifaWCLocation();
						l.setCity(cursor.getString(cursor
								.getColumnIndex(Constants.CITY)));
						l.setCountry(cursor.getString(cursor
								.getColumnIndex(Constants.COUNTRY)));
						l.setIcon(cursor.getString(cursor
								.getColumnIndex(Constants.ICON_RES)));
						l.setId(cursor.getInt(cursor
								.getColumnIndex(Constants.ID)));
						l.setLatitude(Double.parseDouble(cursor
								.getString(cursor
										.getColumnIndex(Constants.LATITUDE))));
						l.setLongitude(Double.parseDouble(cursor
								.getString(cursor
										.getColumnIndex(Constants.LONGITUDE))));
						l.setScotlandParticipated(cursor.getInt(cursor
								.getColumnIndex(Constants.SCOTLAND_PARTICIPATED)));
						l.setStadium(cursor.getString(cursor
								.getColumnIndex(Constants.STADIUM)));
						l.setYear(cursor.getInt(cursor
								.getColumnIndex(Constants.YEAR)));

						list.add(l);

					}
				}
			} catch (SQLException e) {
				Log.e(TAG, "sql exception in dbio_count", e);
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		return list;
	}
	
	/*
	 * Gets and returns the last 10 WC locations from database table FIFA_WC_INFO 
	 */

	public ArrayList<FifaWCLocation> getLast10WorldCupLocations() {
		ArrayList<FifaWCLocation> list = null;
		Cursor cursor = null;
		if (database != null) {
			try {
				cursor = database.rawQuery("select * from "
						+ Constants.FIFA_WC_INFO + " where " + Constants.YEAR
						+ "!=" + Constants.CURRENT_YEAR, null);
				if (cursor != null) {
					list = new ArrayList<FifaWCLocation>();
					while (cursor.moveToNext()) {
						FifaWCLocation l = new FifaWCLocation();
						l.setCity(cursor.getString(cursor
								.getColumnIndex(Constants.CITY)));
						l.setCountry(cursor.getString(cursor
								.getColumnIndex(Constants.COUNTRY)));
						l.setIcon(cursor.getString(cursor
								.getColumnIndex(Constants.ICON_RES)));
						l.setId(cursor.getInt(cursor
								.getColumnIndex(Constants.ID)));
						l.setLatitude(Double.parseDouble(cursor
								.getString(cursor
										.getColumnIndex(Constants.LATITUDE))));
						l.setLongitude(Double.parseDouble(cursor
								.getString(cursor
										.getColumnIndex(Constants.LONGITUDE))));
						l.setScotlandParticipated(cursor.getInt(cursor
								.getColumnIndex(Constants.SCOTLAND_PARTICIPATED)));
						l.setStadium(cursor.getString(cursor
								.getColumnIndex(Constants.STADIUM)));
						l.setYear(cursor.getInt(cursor
								.getColumnIndex(Constants.YEAR)));

						list.add(l);

					}
				}
			} catch (SQLException e) {
				Log.e(TAG, "sql exception in dbio_count", e);
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		return list;
	}
	
	/*
	 * query database to select the diaries entries based on the location provided
	 * and returns the ArrayList of Diary objects
	 * */

	public ArrayList<Diary> getDiariesForLocation(FifaWCLocation location) {
		ArrayList<Diary> list = null;
		Cursor cursor = null;
		if (database != null) {
			try {
				cursor = database.rawQuery(
						"select * from " + Constants.DIARIES + " where "
								+ Constants.LOCATION_ID + "="
								+ location.getId() + " Order by "
								+ Constants.ID + " DESC ", null);
				if (cursor != null && cursor.getCount() > 0) {
					list = new ArrayList<Diary>();
					while (cursor.moveToNext()) {
						Diary l = new Diary();
						l.setId(cursor.getInt(cursor
								.getColumnIndex(Constants.ID)));
						l.setLocationId(cursor.getInt(cursor
								.getColumnIndex(Constants.LOCATION_ID)));
						l.setMessage(cursor.getString(cursor
								.getColumnIndex(Constants.MESSAGE)));
						l.setDate(cursor.getString(cursor
								.getColumnIndex(Constants.DATE)));
						list.add(l);

					}
				}
			} catch (SQLException e) {
				Log.e(TAG, "sql exception in dbio_count", e);
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		return list;
	}


	/*
	 * insert new Diary entry to DIARIES table in
	 * */
	public int addDiary(Diary diary) {
		int id = 0;
		if (database != null) {
			try {
				ContentValues values = new ContentValues();
				values.put(Constants.MESSAGE, diary.getMessage());
				values.put(Constants.LOCATION_ID, diary.getLocationId());
				values.put(Constants.DATE, diary.getDate());
				id = (int) database.insert(Constants.DIARIES, null, values);

			} catch (SQLException e) {

				Log.i("sql exception", e.toString());
			}
		}
		return id;
	}

}
