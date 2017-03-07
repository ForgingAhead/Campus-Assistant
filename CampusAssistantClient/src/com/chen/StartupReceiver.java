package com.chen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class StartupReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		try
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			boolean startImmediately = prefs.getBoolean("startonbootup", false);


			if (startImmediately)
			{
				Intent serviceIntent = new Intent(context, LocationService.class);
				serviceIntent.putExtra("immediate", true);
				context.startService(serviceIntent);
			}
		}
		catch (Exception ex)
		{
			System.out.println("StartupReceiver");

		}

	}

}
