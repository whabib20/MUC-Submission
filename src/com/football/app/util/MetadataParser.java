package com.football.app.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.football.app.database.FifaWCLocation;

/*
 * This is the class to read meta data from asset(i-e text file)
 * and parse the JSON contained in the asset and return the repsective object 
 * */
public class MetadataParser {

	/*
	 * parse the meta data and retruns the ArrayList of type FifaWCLocation
	 */

	public ArrayList<FifaWCLocation> parseMetadata() throws JSONException {
		// TODO Auto-generated method stub
		ArrayList<FifaWCLocation> list = null;
		try {

			// reading metedata as string from the Constants.METADATA_PATH
			String metadata = loadJSONFromAsset();
			// convert the string to JSON object
			JSONObject obj = new JSONObject(metadata);
			// read from JSON object the array named Constants.FIFA_WC_INFO
			JSONArray infoArray = obj.getJSONArray(Constants.FIFA_WC_INFO);

			if (infoArray != null) {
				// creating a new list to add the FifaWCLocations
				list = new ArrayList<FifaWCLocation>();
				// loop to array and create FifaWCLocation objects and add to
				// list
				for (int i = 0; i < infoArray.length(); i++) {

					JSONObject info = infoArray.getJSONObject(i);
					FifaWCLocation location = new FifaWCLocation();

					location.setCity(info.getString(Constants.CITY));
					location.setCountry(info.getString(Constants.COUNTRY));
					location.setIcon(info.getString(Constants.ICON_RES));
					location.setLatitude(info.getDouble(Constants.LATITUDE));
					location.setLongitude(info.getDouble(Constants.LONGITUDE));
					location.setScotlandParticipated(info
							.getInt(Constants.SCOTLAND_PARTICIPATED));
					location.setStadium(info.getString(Constants.STADIUM));
					location.setYear(info.getInt(Constants.YEAR));

					list.add(location);

				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;

	}

	/*
	 * open the metadata file from METADATA_PATH defined in constants and return
	 * the JSON
	 */

	public String loadJSONFromAsset() {
		String json = null;
		try {
			InputStream is = getClass().getResourceAsStream(
					Constants.METADATA_PATH);

			int size = is.available();

			byte[] buffer = new byte[size];

			is.read(buffer);

			is.close();

			json = new String(buffer, "UTF-8");

		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return json;

	}

}
