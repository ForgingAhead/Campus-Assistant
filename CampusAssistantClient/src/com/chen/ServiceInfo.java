package com.chen;

import java.util.Vector;

public class ServiceInfo {
	
	private static Vector<String> address;
	private static Vector<String> content;
	private static boolean isnew;
	
	public ServiceInfo(){
		address = new Vector<String>();
		content = new Vector<String>();
	}
	
	public static void clear(){
		address.clear();
		content.clear();
	}
	
	public static boolean isNew() {
		return isnew;
	}
	
	public static void setIsNew(boolean updated) {
		isnew= updated;
	}
	
	public static void add(String ads, String cont) {
		address.add(ads);
		content.add(cont);
	}
	
	public static Vector<String> getAddress() {
		return address;
	}
	
	public static Vector<String> getContent() {
		return content;
	}
	
	public static int getNo() {
		return address.size();
	}
}
