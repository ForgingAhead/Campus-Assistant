package com.chen.common;


import android.app.Application;
import com.chen.ContextData;

public class Session extends Application
{

	// ---------------------------------------------------
	// Session values - updated as the app runs
	// ---------------------------------------------------
	private static boolean towerEnabled;
	private static boolean gpsEnabled;
	private static boolean isStarted;
	private static boolean isUsingGps;
	private static boolean notificationVisible;
	private static boolean hasUpdate;//check whether there has update ( about location)
	private static long latestTimeStamp;
	private static ContextData currentContextDataInfo;
	private static ContextData lastContextDataInfo;
	private static boolean isBound;

    // ---------------------------------------------------
	/**
	 * @return whether GPS (tower) is enabled
	 */
	public static boolean isTowerEnabled()
	{
		return towerEnabled;
	}

	/**
	 * @param towerEnabled
	 *            set whether GPS (tower) is enabled
	 */
	public static void setTowerEnabled(boolean towerEnabled)
	{
		Session.towerEnabled = towerEnabled;
	}

	public static boolean updated() {
		return hasUpdate;
	}
	
	public static void setHasUpdate(boolean t) {
		hasUpdate = t;
	}
	
	/**
	 * @return whether GPS (satellite) is enabled
	 */
	public static boolean isGpsEnabled()
	{
		return gpsEnabled;
	}

	/**
	 * @param gpsEnabled
	 *            set whether GPS (satellite) is enabled
	 */
	public static void setGpsEnabled(boolean gpsEnabled)
	{
		Session.gpsEnabled = gpsEnabled;
	}

	/**
	 * @return whether logging has started
	 */
	public static boolean isStarted()
	{
		return isStarted;
	}

	/**
	 * @param isStarted
	 *            set whether logging has started
	 */
	public static void setStarted(boolean isStarted)
	{
		Session.isStarted = isStarted;

	}

	/**
	 * @return the isUsingGps
	 */
	public static boolean isUsingGps()
	{
		return isUsingGps;
	}

	/**
	 * @param isUsingGps
	 *            the isUsingGps to set
	 */
	public static void setUsingGps(boolean isUsingGps)
	{
		Session.isUsingGps = isUsingGps;
	}


	/**
	 * @return the notificationVisible
	 */
	public static boolean isNotificationVisible()
	{
		return notificationVisible;
	}

	/**
	 * @param notificationVisible
	 *            the notificationVisible to set
	 */
	public static void setNotificationVisible(boolean notificationVisible)
	{
		Session.notificationVisible = notificationVisible;
	}

	/**
	 * @return the currentLatitude
	 */
	public static double getCurrentLatitude()
	{
		if (getCurrentContextDataInfo() != null)
		{
			return getCurrentContextDataInfo().getLatitude();
		}
		else
		{
			return 0;
		}
	}
	
	public static double getLastLatitude() {
		if(getLastContextDataInfo() != null) 
			return getLastContextDataInfo().getLatitude();
		else return 0;
	}
	
	public static void setLastContextData(ContextData lastLoc) {
		Session.lastContextDataInfo = lastLoc;
	}
	
	/**
	 * Determines whether a valid location is available
	 */
	public static boolean hasValidContextData()
	{
		return (getCurrentContextDataInfo() != null && getCurrentLatitude() != 0 && getCurrentLongitude() != 0);
	}

	/**
	 * @return the currentLongitude
	 */
	public static double getCurrentLongitude()
	{
		if (getCurrentContextDataInfo() != null)
		{
			return getCurrentContextDataInfo().getLongitude();
		}
		else
		{
			return 0;
		}
	}
	
	public static double getLastLongitude() {
		if(getLastContextDataInfo() != null) {
			return getLastContextDataInfo().getLongitude();
		}
		else return 0;
	}

	/**
	 * @return the latestTimeStamp (for location info)
	 */
	public static long getLatestTimeStamp()
	{
		return latestTimeStamp;
	}

	/**
	 * @param latestTimeStamp
	 *            the latestTimeStamp (for location info) to set
	 */
	public static void setLatestTimeStamp(long latestTimeStamp)
	{
		Session.latestTimeStamp = latestTimeStamp;
	}


	/**
	 * @param currentContextDataInfo
	 *            the latest ContextData class
	 */
	public static void setCurrentContextDataInfo(ContextData currentContextDataInfo)
	{
		Session.currentContextDataInfo = currentContextDataInfo;
		hasUpdate = true;
	}

	/**
	 * @return the ContextData class containing latest lat-long information
	 */
	public static ContextData getCurrentContextDataInfo()
	{
		return currentContextDataInfo;
	}
	
	public static ContextData getLastContextDataInfo() {
		return lastContextDataInfo;
	}
	

	/**
	 * @param isBound
	 *            set whether the activity is bound to the LocationService
	 */
	public static void setBoundToService(boolean isBound)
	{
		Session.isBound = isBound;
	}

	/**
	 * @return whether the activity is bound to the LocationService
	 */
	public static boolean isBoundToService()
	{
		return isBound;
	}

}
