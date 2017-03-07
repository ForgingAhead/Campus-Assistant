package com.chen;

import android.location.*;
import android.os.Bundle;
import com.chen.common.Utilities;
import java.util.Iterator;


class GeneralLocationListener implements LocationListener, GpsStatus.Listener
{

    private static LocationService mainActivity;

    GeneralLocationListener(LocationService activity)
    {
        System.out.println("GeneralLocationListener constructor");
        mainActivity = activity;
    }

    /**
     * Event raised when a new fix is received.
     */
    public void onLocationChanged(Location loc)
    {


        try
        {
            if (loc != null)
            {
            	System.out.println("GeneralLocationListener.onLocationChanged");
                mainActivity.OnLocationChanged(loc);
            }

        }
        catch (Exception ex)
        {
        	System.out.println("GeneralLocationListener.onLocationChanged " + ex);
            mainActivity.SetStatus(ex.getMessage());
        }

    }

    public void onProviderDisabled(String provider)
    {
    	System.out.println("Provider disabled");
    	System.out.println(provider);
        mainActivity.RestartGpsManagers();
    }

    public void onProviderEnabled(String provider)
    {

    	System.out.println("Provider enabled");
    	System.out.println(provider);
        mainActivity.RestartGpsManagers();
    }

    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        if (status == LocationProvider.OUT_OF_SERVICE)
        {
            System.out.println(provider + " is out of service");
            mainActivity.StopManagerAndResetAlarm();
        }

        if (status == LocationProvider.AVAILABLE)
        {
            System.out.println(provider + " is available");
        }

        if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
        {
            System.out.println(provider + " is temporarily unavailable");
            mainActivity.StopManagerAndResetAlarm();
        }
    }

    public void onGpsStatusChanged(int event)
    {

        switch (event)
        {
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                System.out.println("GPS Event First Fix");
                mainActivity.SetStatus(mainActivity.getString(R.string.fix_obtained));
                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

                System.out.println("GPS Satellite status obtained");
                GpsStatus status = mainActivity.gpsLocationManager.getGpsStatus(null);

                int maxSatellites = status.getMaxSatellites();

                Iterator<GpsSatellite> it = status.getSatellites().iterator();
                int count = 0;

                while (it.hasNext() && count <= maxSatellites)
                {
                    it.next();
                    count++;
                }

                mainActivity.SetSatelliteInfo(count);
                break;

            case GpsStatus.GPS_EVENT_STARTED:
            	System.out.println("GPS started, waiting for fix");
                mainActivity.SetStatus(mainActivity.getString(R.string.started_waiting));
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
            	System.out.println("GPS Stopped");
                mainActivity.SetStatus(mainActivity.getString(R.string.gps_stopped));
                break;

        }
    }

}
