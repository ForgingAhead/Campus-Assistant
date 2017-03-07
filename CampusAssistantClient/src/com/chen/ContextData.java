package com.chen;

import java.sql.Timestamp;

public class ContextData {

	private double longitude;
	private double latitude;
	private Timestamp timestamp;
	
	public ContextData(double lng, double lat, Timestamp t) {
		longitude = lng;
		latitude = lat;
		timestamp = t;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public Timestamp getTimestamp() {
		return timestamp;
	}
	
	public void setLongitude(double lng) {
		longitude = lng;
	}
}
