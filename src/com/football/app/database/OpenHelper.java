package com.football.app.database;

import java.util.ArrayList;

import org.json.JSONException;

import com.football.app.util.Constants;
import com.football.app.util.MetadataParser;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class OpenHelper extends SQLiteOpenHelper {
	/*
	 * DB name : fifa_worldcup.db DB version : 1
	 */
	private static final String DB_NAME = "fifa_worldcup.db";
	private static final int DB_VERSION = 1;

	private SQLiteDatabase db;

	public OpenHelper(Context context) {

		super(context, DB_NAME, null, DB_VERSION);
		// Android will look for the database defined by DB_NAME
		// And if not found will invoke your onCreate method
		this.db = this.getWritableDatabase();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		/*
		 * creating fifa_wc_info and diaries tables in DB
		 */
		String query = String.format("CREATE TABLE " + Constants.FIFA_WC_INFO
				+ "( " + Constants.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Constants.CITY + " TEXT NOT NULL, " + Constants.COUNTRY
				+ " TEXT NOT NULL," + Constants.YEAR + " INTEGER NOT NULL,"
				+ Constants.SCOTLAND_PARTICIPATED + " INTEGER NOT NULL,"
				+ Constants.ICON_RES + " TEXT," + Constants.LATITUDE
				+ " TEXT NOT NULL," + Constants.LONGITUDE + " TEXT NOT NULL,"
				+ Constants.STADIUM + " TEXT)");

		db.execSQL(query);

		String query1 = String.format("CREATE TABLE " + Constants.DIARIES
				+ "( " + Constants.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Constants.MESSAGE + " TEXT NOT NULL," + Constants.DATE
				+ " TEXT NOT NULL," + Constants.LOCATION_ID
				+ " INTEGER NOT NULL)");

		db.execSQL(query1);

		/*
		 * getting meta data from metadate files using MetadataParser and
		 * populate the WC data in the fifa_wc_info table when DB is created
		 */
		MetadataParser parser = new MetadataParser();
		ArrayList<FifaWCLocation> list = null;

		try {
			list = parser.parseMetadata();
			if (list != null) {
				populateWCData(list, db);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * insert metadat to database to be used for associating diaries to WC
	 * locations
	 */
	public void populateWCData(ArrayList<FifaWCLocation> list, SQLiteDatabase db) {

		if (db != null) {
			if (list != null) {
				for (FifaWCLocation location : list) {
					try {
						String sql = "INSERT INTO " + Constants.FIFA_WC_INFO
								+ " (" + Constants.CITY + " , "
								+ Constants.COUNTRY + " , " + Constants.YEAR
								+ " , " + Constants.SCOTLAND_PARTICIPATED
								+ " , " + Constants.ICON_RES + " , "
								+ Constants.LATITUDE + " , "
								+ Constants.LONGITUDE + " , "
								+ Constants.STADIUM + ")  Values('"
								+ location.getCity() + "','"
								+ location.getCountry() + "',"
								+ location.getYear() + ","
								+ location.getScotlandParticipated() + ",'"
								+ location.getIcon() + "','"
								+ location.getLatitude().toString() + "','"
								+ location.getLongitude() + "','"
								+ location.getStadium() + "')";
						db.execSQL(sql);

					} catch (SQLException e) {
						// TODO: handle exception
						Log.i("sql exception", e.toString());
					}
				}
			}
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
}
