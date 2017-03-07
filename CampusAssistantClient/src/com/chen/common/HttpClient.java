package com.chen.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.text.SimpleDateFormat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.chen.ContextData;
import com.chen.ServiceInfo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

public class HttpClient {
	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * This is a test function which will connects to a given
	 * rest service and prints it's response to Android Log with,
	 *  this is used to 
	 * 
	 * @param url: the server's URL.
	 * @param context: the information to be sent to server for info.
	 */
	public static boolean connect(String url, ContextData context, TextView infoMessage)
	{
		DefaultHttpClient httpclient = new DefaultHttpClient();
		
		// Prepare a request object
		HttpPost httpPost = new HttpPost(url); 

		// Execute the request
		HttpResponse response;
		
		List <NameValuePair> params=new ArrayList<NameValuePair>();
		
		SimpleDateFormat   sDateFormat   =   new   SimpleDateFormat("yyyy-MM-dd   hh:mm:ss"); 
		String   date   =   sDateFormat.format(new   java.util.Date());
		Log.i("Time", date);


		params.add(new BasicNameValuePair("longtidue",Double.toString(context.getLongitude())));
		params.add(new BasicNameValuePair("latitude", Double.toString(context.getLatitude())));
		params.add(new BasicNameValuePair("timestamp", context.getTimestamp().toString()));
	
		try {
			
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			
			Log.i("The URL", httpPost.getURI().toString());
			
			for(int i = 0; i < params.size(); i++){
				System.out.println("send url"+params.get(i).toString());
			}
			
			response = httpclient.execute(httpPost);
			// Examine the response status
			System.out.println("response "+response.getStatusLine().toString());

			System.out.println("response "+response.getParams().toString());
			
			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release
			
			
			if (entity != null) {

				InputStream instream = entity.getContent();
				System.out.println("The inputStream is OK");
				
				String result= convertStreamToString(instream);
				System.out.println("response result: "+result);

				
				// A Simple JSONObject Creation
				JSONObject json=new JSONObject(result);

				// A Simple JSONObject Parsing
				JSONArray nameArray=json.names();
				JSONArray valArray=json.toJSONArray(nameArray);
				
				Log.i("length", ((Integer)valArray.length()).toString());
				for(int i=0; i < valArray.length(); i++)
				{
					Log.i("Praeda","<jsonname"+i+">\n"+nameArray.getString(i)+"\n</jsonname"+i+">\n"
							+"<jsonvalue"+i+">\n"+valArray.getString(i)+"\n</jsonvalue"+i+">");
				
				
					//infoMessage.setText("The training fail");
				}
				
				infoMessage.setText(json.getString("message"));			
				

				// Closing the input stream will trigger connection release
				instream.close();
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (JSONException e) {
			Log.i("json", "jsonError");
			e.printStackTrace();
			return false;
		} catch (Exception e){
			Log.i("json", "jsonError");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	
	/**
	 * This method is used to connect to the server and call service, and any service information 
	 * will be stored in the ServiceInfo, which is a class using a Singleton pattern in
	 * order to record all these information.
	 * @param url: the URL of the server.
	 * @param context: the information to be sent to server for info.
	 */
	public static boolean connect(String url, ContextData context){
		DefaultHttpClient httpclient = new DefaultHttpClient();

		// Prepare a request object
		HttpPost httpPost = new HttpPost(url); 

		// Execute the request
		HttpResponse response;
		
		List <NameValuePair> params=new ArrayList<NameValuePair>();		
		

		params.add(new BasicNameValuePair("longtidue",Double.toString(context.getLongitude())));
		params.add(new BasicNameValuePair("latitude", Double.toString(context.getLatitude())));
		params.add(new BasicNameValuePair("timestamp", context.getTimestamp().toString()));
		try {
			
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			
			Log.i("The URL", httpPost.getURI().toString());
			
			for(int i = 0; i < params.size(); i++){
				System.out.println("url params: "+ params.get(i).toString());
			}
			
			response = httpclient.execute(httpPost);
			
			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release
		
			if (entity != null) {			

				InputStream instream = entity.getContent();
				
				String result= convertStreamToString(instream);
				
				System.out.println("response result: "+result);

				//JSONObject Creation
				JSONObject json=new JSONObject(result);
				if(json.get("Status") == "success") {
					ServiceInfo.clear();
					JSONArray info = new JSONArray(json.get("ServiceInfo").toString());
					for(int i=0; i < info.length(); i++)
					{
						JSONObject servinfo = new JSONObject(info.getJSONObject(i).toString());
						ServiceInfo.add(servinfo.getString("Address"), servinfo.getString("Content"));
					}
					if(ServiceInfo.getNo() > 0) {
						ServiceInfo.setIsNew(true);
					}
							
				}
				else {
					//handle here when the status =  "failure"
					}
				
				// Closing the input stream will trigger connection release
				instream.close();
			}
			else {
				System.out.println("HTTP communivation failure.");
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (JSONException e) {
			Log.i("json", "jsonError");
			e.printStackTrace();
			return false;
		} catch (Exception e){
			Log.i("json", "jsonError");
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
