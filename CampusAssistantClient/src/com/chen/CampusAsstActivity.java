package com.chen;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.chen.common.AppSettings;
import com.chen.common.HttpClient;
import com.chen.common.IActionListener;
import com.chen.common.Session;
import com.chen.common.Utilities;
import java.io.File;
import java.io.FilenameFilter;
import java.sql.Timestamp;
import java.util.*;

public class CampusAsstActivity extends Activity implements 
        ILocationServiceClient, View.OnClickListener, IActionListener
{

    /**
     * General all purpose handler used for updating the UI from threads.
     */
    private static Intent serviceIntent;
    private LocationService locationService;
    private static String ServerURL = "49.140.18.229/service";

    /**
     * Provides a connection to the GPS Logging Service
     */
    private final ServiceConnection gpsServiceConnection = new ServiceConnection()
    {

        public void onServiceDisconnected(ComponentName name)
        {
            locationService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service)
        {
            locationService = ((LocationService.LocationBinder) service).getService();
            LocationService.SetServiceClient(CampusAsstActivity.this);


            // Form setup - toggle button, display existing location info
            ToggleButton buttonOnOff = (ToggleButton) findViewById(R.id.buttonOnOff);
           // buttonOnOff.setOnCheckedChangeListener(CampusAsstActivity.this);
        }
    };


    /**
     * Event raised when the form is created for the first time
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        //Utilities.LogDebug("CampusAsstActivity.onCreate");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String lang = prefs.getString("locale_override", "");

        if (!lang.equalsIgnoreCase(""))
        {
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getApplicationContext().getResources().updateConfiguration(config,
                    getApplicationContext().getResources().getDisplayMetrics());
        }

        super.onCreate(savedInstanceState);

       // setContentView(R.layout.main);

        GetPreferences();

        StartAndBindService();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        StartAndBindService();
    }

    @Override
    protected void onResume()
    {
        //Utilities.LogDebug("CampusAsstactivity.onResume");
        super.onResume();
        StartAndBindService();
    }

    /**
     * Starts the service and binds the activity to it.
     */
    private void StartAndBindService()
    {
        System.out.println("StartAndBindService - binding now");
        serviceIntent = new Intent(this, LocationService.class);
        // Start the service in case it isn't already running
        startService(serviceIntent);
        // Now bind to service
        bindService(serviceIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
        Session.setBoundToService(true);
    }


    /**
     * Stops the service if it isn't logging. Also unbinds.
     */
    private void StopAndUnbindServiceIfRequired()
    {
        System.out.println("CampusAsstActivity.StopAndUnbindServiceIfRequired");
        if (Session.isBoundToService())
        {
            unbindService(gpsServiceConnection);
            Session.setBoundToService(false);
        }

        if (!Session.isStarted())
        {
        	System.out.println("StopServiceIfRequired - Stopping the service");
            //serviceIntent = new Intent(this, LocationService.class);
            stopService(serviceIntent);
        }

    }

    @Override
    protected void onPause()
    {

    	System.out.println("CampusAsstActivity.onPause");
        StopAndUnbindServiceIfRequired();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {

    	System.out.println("CampusAsstActivity.onDestroy");
        StopAndUnbindServiceIfRequired();
        super.onDestroy();

    }



    /**
     * Called when the single point button is clicked
     */
    public void onClick(View view)
    {
    	System.out.println("CampusAsstActivity.onClick");

        if (!Session.isStarted())
        {
            SetMainButtonEnabled(false);
            locationService.StartLogging();
        }
        else
        {
            locationService.StopLogging();
            SetMainButtonEnabled(true);
        }
    }


    public void SetMainButtonEnabled(boolean enabled)
    {
        ToggleButton buttonOnOff = (ToggleButton) findViewById(R.id.buttonOnOff);
        buttonOnOff.setEnabled(enabled);
    }

    public void SetMainButtonChecked(boolean checked)
    {
        ToggleButton buttonOnOff = (ToggleButton) findViewById(R.id.buttonOnOff);
        buttonOnOff.setChecked(checked);
    }

    /**
     * Gets preferences chosen by the user
     */
    private void GetPreferences()
    {
        Utilities.PopulateAppSettings(getApplicationContext());
        ShowPreferencesSummary();
    }

    /**
     * Displays a human readable summary of the preferences chosen by the user
     * on the main form
     */
    private void ShowPreferencesSummary()
    {
    	System.out.println("CampusAsstActivity.ShowPreferencesSummary");
        try
        {
            TextView txtFrequency = (TextView) findViewById(R.id.txtFrequency);
           
            if (AppSettings.getMinimumSeconds() > 0)
            {
                String descriptiveTime = Utilities.GetDescriptiveTimeString(AppSettings.getMinimumSeconds(),
                        getApplicationContext());

                txtFrequency.setText(descriptiveTime);
            }
            else
            {
                txtFrequency.setText(R.string.summary_freq_max);

            }

        }
        catch (Exception ex)
        {
          //  Utilities.LogError("ShowPreferencesSummary", ex);
        }


    }

    /**
     * Handles the hardware back-button press
     */
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        //Utilities.LogInfo("KeyDown - " + String.valueOf(keyCode));

        if (keyCode == KeyEvent.KEYCODE_BACK && Session.isBoundToService())
        {
            StopAndUnbindServiceIfRequired();
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Called when the menu is created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionsmenu, menu);

        return true;

    }

    /**
     * Called when one of the menu items is selected.
     */
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int itemId = item.getItemId();
        System.out.println("Option item selected - " + String.valueOf(item.getTitle()));

        switch (itemId)
        {
            case R.id.mnuSettings:
                Intent settingsActivity = new Intent(getApplicationContext(), GpsSettingsActivity.class);
                startActivity(settingsActivity);
                break;
            case R.id.mnuShare:
                //Share();
                break;
            case R.id.mnuExit:
                locationService.StopLogging();
                locationService.stopSelf();
                finish();
                break;
        }
        return false;
    }

    
    /**
     * Clears the table, removes all values.
     */
    public void ClearForm()
    {

        System.out.println("CampusAsstActivity.ClearForm");

        TextView tvLocation = (TextView) findViewById(R.id.txtLocation);
        TextView tvFrequency = (TextView) findViewById(R.id.txtFrequency);
        TextView tvDateTime = (TextView) findViewById(R.id.txtDateTimeAndProvider);
        TextView tvNews = (TextView) findViewById(R.id.txtNews);

        tvLocation.setText("");
        tvFrequency.setText("");
        tvDateTime.setText("");
        tvNews.setText("");

    }

    public void OnStopLogging()
    {
    	System.out.println("CampusAsstActivity.OnStopLogging");
        SetMainButtonChecked(false);
    }

    /**
     * Sets the message in the top status label.
     *
     * @param message The status message
     */
    private void SetStatus(String message)
    {
    	System.out.println("CampusAsstActivity.SetStatus: " + message);
        TextView tvStatus = (TextView) findViewById(R.id.textStatus);
        tvStatus.setText(message);
        System.out.println(message);
    }


    public void OnLocationUpdate(Location loc)
    {
    	System.out.println("CampusAsstActivity.OnLocationUpdate");
        //DisplayLocationInfo(loc);
  	
    	//缺少判断机制。。。。。。。。。。。。。。。。。。。。。。
    	HttpClient.connect(ServerURL, Session.getCurrentContextDataInfo());
    	
        TextView tvLocation = (TextView) findViewById(R.id.txtLocation);
        TextView tvFrequency = (TextView) findViewById(R.id.txtFrequency);
        TextView tvDateTime = (TextView) findViewById(R.id.txtDateTimeAndProvider);
        TextView tvNews = (TextView) findViewById(R.id.txtNews);
        String info = "";
        for(int i=0; i<ServiceInfo.getNo(); i++) {
            info = info + "\n地点： " + ServiceInfo.getAddress().get(i) + "\n信息内容： " + ServiceInfo.getContent().get(i);       	
        }
        tvLocation.setText(ServiceInfo.getAddress().get(0));
        tvNews.setText(info);
    	
        ShowPreferencesSummary();
        SetMainButtonChecked(true);

    }

    public void OnStatusMessage(String message)
    {
        SetStatus(message);
    }

    public void OnFatalMessage(String message)
    {
        //Utilities.MsgBox(getString(R.string.sorry), message, this);//////////////////////////////////////////////
    }

    public Activity GetActivity()
    {
        return this;
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

	@Override
	public void OnSatelliteCount(int count) {
		// TODO Auto-generated method stub
		
	}
}
