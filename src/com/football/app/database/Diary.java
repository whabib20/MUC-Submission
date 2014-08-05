package com.football.app.database;

public class Diary {

	private int id;
	private String message;
	private int locationId;
	private String date;
	public Diary(String message, int locationId, String date) {
		// TODO Auto-generated constructor stub
		this.message = message;
		this.locationId = locationId;
		this.date = date;
	}
	
	public Diary() {
		// TODO Auto-generated constructor stub
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getLocationId() {
		return locationId;
	}
	public void setLocationId(int locationId) {
		this.locationId = locationId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
