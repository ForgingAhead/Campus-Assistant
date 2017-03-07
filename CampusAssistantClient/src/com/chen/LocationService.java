package com.chen;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.chen.common.IActionListener;
import com.chen.GeneralLocationListener;
import com.chen.LocationService;
import com.chen.CampusAsstActivity;
import com.chen.ILocationServiceClient;
import com.chen.LocationService.LocationBinder;
import com.chen.common.AppSettings;
import com.chen.common.Session;
import com.chen.common.Utilities;

public class LocationService extends Service implements IActionListener{
	
    private static NotificationManager gpsNotifyManager;
    private static int NOTIFICATION_ID = 8675309;
	private final IBinder mBinder = new LocationBinder();
    private static ILocationServiceClient mainServiceClient;

    // ---------------------------------------------------
    // Helpers and managers
    // ---------------------------------------------------
    private GeneralLocationListener gpsLocationListener;
    private GeneralLocationListener towerLocationListener;
    LocationManager gpsLocationManager;
    private LocationManager towerLocationManager;

    private Intent alarmIntent;

    AlarmManager nextPointAlarmManager;

    // ---------------------------------------------------

    @Override
    public IBinder onBind(Intent arg0)
    {
        //("LocationService.onBind");
        return mBinder;
    }

    @Override
    public void onCreate()
    {
        //("LocationService.onCreate");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String lang = prefs.getString("locale_override", "");

        if (!lang.equalsIgnoreCase(""))
        {
            System.out.println("Setting app to user specified locale: " + lang);
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getApplicationContext().getResources().updateConfiguration(config,
                    getApplicationContext().getResources().getDisplayMetrics());
        }

        nextPointAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        System.out.println("GPSLoggerService created");
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        //("LocationService.onStart");
        HandleIntent(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        //("LocationService.onStartCommand");
        HandleIntent(intent);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy()
    {
    	System.out.println("LocationService is being destroyed by Android OS.");
        mainServiceClient = null;
        super.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
    	System.out.println("Android is low on memory.");
        super.onLowMemory();
    }

    private void HandleIntent(Intent intent)
    {

        //("LocationService.handleIntent");
        GetPreferences();

        //("Null intent? " + String.valueOf(intent == null));

        if (intent != null)
        {
            Bundle bundle = intent.getExtras();

            if (bundle != null)
            {
                boolean startRightNow = bundle.getBoolean("immediate");
                boolean sendEmailNow = bundle.getBoolean("emailAlarm");
                boolean getNextPoint = bundle.getBoolean("getnextpoint");

                //("startRightNow - " + String.valueOf(startRightNow));

                //("emailAlarm - " + String.valueOf(sendEmailNow));

                if (startRightNow)
                {
                	System.out.println("Auto starting logging");

                    StartLogging();
                }

                if (sendEmailNow)
                {

                    //("setEmailReadyToBeSent = true");

                   // Session.setEmailReadyToBeSent(true);
                    //AutoEmailLogFile();
                }

                if (getNextPoint && Session.isStarted())
                {
                    //("HandleIntent - getNextPoint");
                    StartGpsManager();
                }

            }
        }
        else
        {
            // A null intent is passed in if the service has been killed and
            // restarted.
            //("Service restarted with null intent. Start logging.");
            StartLogging();

        }
    }

    @Override
    public void OnComplete()
    {
        Utilities.HideProgress();
    }

    @Override
    public void OnFailure()
    {
        Utilities.HideProgress();
    }

    /**
     * Can be used from calling classes as the go-between for methods and
     * properties.
     */
    class LocationBinder extends Binder
    {
        public LocationService getService()
        {
            //("LocationBinder.getService");
            return LocationService.this;
        }
    }

 

    private void CancelAlarm()
    {
        //("LocationService.CancelAlarm");

        if (alarmIntent != null)
        {
            //("LocationService.CancelAlarm");
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            //("Pending alarm intent was null? " + String.valueOf(sender == null));
            am.cancel(sender);
        }

    }

 

    /**
     * Sets the activity form for this service. The activity form needs to
     * implement ILocationServiceClient.
     *
     * @param mainForm The calling client
     */
    protected static void SetServiceClient(ILocationServiceClient mainForm)
    {
        mainServiceClient = mainForm;
    }

    /**
     * Gets preferences chosen by the user and populates the AppSettings object.
     * Also sets up email timers if required.
     */
    private void GetPreferences()
    {
        //("LocationService.GetPreferences");
        Utilities.PopulateAppSettings(getApplicationContext());
    }

    /**
     * Resets the form, resets file name if required, reobtains preferences
     */
    protected void StartLogging()
    {
       // Session.setAddNewTrackSegment(true);

        if (Session.isStarted())
        {
            return;
        }

        System.out.println("Starting logging procedures");
        try
        {
            startForeground(NOTIFICATION_ID, new Notification());
        }
        catch (Exception ex)
        {
            System.out.print(ex.getMessage());
        }


        Session.setStarted(true);

        GetPreferences();
        Notify();
       // ResetCurrentFileName(true);
        ClearForm();
        StartGpsManager();

    }

    /**
     * Asks the main service client to clear its form.
     */
    private void ClearForm()
    {
        if (IsMainFormVisible())
        {
            mainServiceClient.ClearForm();
        }
    }

    /**
     * Stops logging, removes notification, stops GPS manager, stops email timer
     */
    protected void StopLogging()
    {
        //("LocationService.StopLogging");
       // Session.setAddNewTrackSegment(true);

        System.out.println("Stopping logging");
        Session.setStarted(false);
        // Email log file before setting location info to null
       // AutoEmailLogFileOnStop();
        CancelAlarm();
        //Session.setCurrentLocationInfo(null);
        stopForeground(true);

        RemoveNotification();
        StopAlarm();
        StopGpsManager();
        StopMainActivity();
    }

    /**
     * Manages the notification in the status bar
     */
    private void Notify()
    {

        //("LocationService.Notify");
        if (AppSettings.shouldShowInNotificationBar())
        {
            gpsNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            ShowNotification();
        }
        else
        {
            RemoveNotification();
        }
    }

    /**
     * Hides the notification icon in the status bar if it's visible.
     */
    private void RemoveNotification()
    {
       // //("LocationService.RemoveNotification");
        try
        {
            if (Session.isNotificationVisible())
            {
                gpsNotifyManager.cancelAll();
            }
        }
        catch (Exception ex)
        {
      //      Utilities.LogError("RemoveNotification", ex);
        }
        finally
        {
            Session.setNotificationVisible(false);
        }
    }

    /**
     * Shows a notification icon in the status bar for GPS Logger
     */
    private void ShowNotification()
    {
        //("LocationService.ShowNotification");
        // What happens when the notification item is clicked
        Intent contentIntent = new Intent(this, CampusAsstActivity.class);

        PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, contentIntent,
                android.content.Intent.FLAG_ACTIVITY_NEW_TASK);

        Notification nfc = new Notification(R.drawable.gpsloggericon2, null, System.currentTimeMillis());
        nfc.flags |= Notification.FLAG_ONGOING_EVENT;

        NumberFormat nf = new DecimalFormat("###.######");

        String contentText = getString(R.string.gpslogger_still_running);
        if (Session.updated())
        {
            contentText = nf.format(Session.getCurrentLatitude()) + ","
                    + nf.format(Session.getCurrentLongitude());
        }

        nfc.setLatestEventInfo(getApplicationContext(), getString(R.string.gpslogger_still_running),
                contentText, pending);

        gpsNotifyManager.notify(NOTIFICATION_ID, nfc);
        Session.setNotificationVisible(true);
    }

    /**
     * Starts the location manager. There are two location managers - GPS and
     * Cell Tower. This code determines which manager to request updates from
     * based on user preference and whichever is enabled. If GPS is enabled on
     * the phone, that is used. But if the user has also specified that they
     * prefer cell towers, then cell towers are used. If neither is enabled,
     * then nothing is requested.
     */
    private void StartGpsManager()
    {
        //("LocationService.StartGpsManager");

        GetPreferences();

        if (gpsLocationListener == null)
        {
            gpsLocationListener = new GeneralLocationListener(this);
        }

        if (towerLocationListener == null)
        {
            towerLocationListener = new GeneralLocationListener(this);
        }


        gpsLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        towerLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        CheckTowerAndGpsStatus();

        if (Session.isGpsEnabled() && !AppSettings.shouldPreferCellTower())
        {
        	System.out.println("Requesting GPS location updates");
            // gps satellite based
            gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 0,
                    gpsLocationListener);

            gpsLocationManager.addGpsStatusListener(gpsLocationListener);

            Session.setUsingGps(true);
        }
        else if (Session.isTowerEnabled())
        {
        	System.out.println("Requesting tower location updates");
            Session.setUsingGps(false);
            // Cell tower and wifi based
            towerLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000, 0,
                    towerLocationListener);

        }
        else
        {
        	System.out.println("No provider available");
            Session.setUsingGps(false);
            SetStatus(R.string.gpsprovider_unavailable);
            SetFatalMessage(R.string.gpsprovider_unavailable);
            StopLogging();
            return;
        }

        SetStatus(R.string.started);
    }

    /**
     * This method is called periodically to determine whether the cell tower /
     * gps providers have been enabled, and sets class level variables to those
     * values.
     */
    private void CheckTowerAndGpsStatus()
    {
        Session.setTowerEnabled(towerLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        Session.setGpsEnabled(gpsLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    /**
     * Stops the location managers
     */
    private void StopGpsManager()
    {

        ////("LocationService.StopGpsManager");

        if (towerLocationListener != null)
        {
          //  //("Removing towerLocationManager updates");
            towerLocationManager.removeUpdates(towerLocationListener);
        }

        if (gpsLocationListener != null)
        {
           // //("Removing gpsLocationManager updates");
            gpsLocationManager.removeUpdates(gpsLocationListener);
            gpsLocationManager.removeGpsStatusListener(gpsLocationListener);
        }

        SetStatus(getString(R.string.stopped));
    }


    /**
     * Gives a status message to the main service client to display
     *
     * @param status The status message
     */
    void SetStatus(String status)
    {
        if (IsMainFormVisible())
        {
            mainServiceClient.OnStatusMessage(status);
        }
    }

    /**
     * Gives an error message to the main service client to display
     *
     * @param messageId ID of string to lookup
     */
    void SetFatalMessage(int messageId)
    {
        if (IsMainFormVisible())
        {
            mainServiceClient.OnFatalMessage(getString(messageId));
        }
    }

    /**
     * Gets string from given resource ID, passes to SetStatus(String)
     *
     * @param stringId ID of string to lookup
     */
    private void SetStatus(int stringId)
    {
        String s = getString(stringId);
        SetStatus(s);
    }

    /**
     * Notifies main form that logging has stopped
     */
    void StopMainActivity()
    {
        if (IsMainFormVisible())
        {
            mainServiceClient.OnStopLogging();
        }
    }


    /**
     * Stops location manager, then starts it.
     */
    void RestartGpsManagers()
    {
        //("LocationService.RestartGpsManagers");
        StopGpsManager();
        StartGpsManager();
    }


    /**
     * This event is raised when the GeneralLocationListener has a new location.
     * This method in turn updates notification, writes to file, reobtains
     * preferences, notifies main service client and resets location managers.
     *
     * @param loc Location object
     */
    void OnLocationChanged(Location loc)
    {

        if (!Session.isStarted())
        {
            ////("OnLocationChanged called, but Session.isStarted is false");
            StopLogging();
            return;
        }

        ////("LocationService.OnLocationChanged");


        long currentTimeStamp = System.currentTimeMillis();

        // Wait some time even on 0 frequency so that the UI doesn't lock up

        if ((currentTimeStamp - Session.getLatestTimeStamp()) < 1000)
        {
            return;
        }

        // Don't do anything until the user-defined time has elapsed
        if ((currentTimeStamp - Session.getLatestTimeStamp()) < (AppSettings.getMinimumSeconds() * 1000))
        {
            return;
        }

        //Utilities.LogInfo("New location obtained");
	   	 java.util.Date date= new java.util.Date();
	   	 Timestamp ts = new Timestamp(date.getTime());
        Session.setLatestTimeStamp(System.currentTimeMillis());
    	ContextData contxtdata = new ContextData(loc.getLongitude(),loc.getLatitude(),ts);
        Session.setCurrentContextDataInfo(contxtdata);
        Notify();
        
        //WriteToFile(loc);
        GetPreferences();
        StopManagerAndResetAlarm();

        if (IsMainFormVisible())
        {
            mainServiceClient.OnLocationUpdate(loc);
        }
    }

    protected void StopManagerAndResetAlarm()
    {
       // //("LocationService.StopManagerAndResetAlarm");
        StopGpsManager();
        SetAlarmForNextPoint();
    }


    private void StopAlarm()
    {
       // //("LocationService.StopAlarm");
        Intent i = new Intent(this, LocationService.class);
        i.putExtra("getnextpoint", true);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        nextPointAlarmManager.cancel(pi);
    }


    private void SetAlarmForNextPoint()
    {

        ////("LocationService.SetAlarmForNextPoint");

        Intent i = new Intent(this, LocationService.class);

        i.putExtra("getnextpoint", true);

        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        nextPointAlarmManager.cancel(pi);

        nextPointAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AppSettings.getMinimumSeconds() * 1000, pi);

    }

    /**
     * Informs the main service client of the number of visible satellites.
     *
     * @param count Number of Satellites
     */
    void SetSatelliteInfo(int count)
    {
        if (IsMainFormVisible())
        {
            mainServiceClient.OnSatelliteCount(count);
        }
    }


    private boolean IsMainFormVisible()
    {
        return mainServiceClient != null;
    }



}
