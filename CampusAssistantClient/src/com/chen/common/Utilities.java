package com.chen.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.chen.R;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utilities
{

    private static ProgressDialog pd;


    /**
     * Gets user preferences, populates the AppSettings class.
     */
    public static void PopulateAppSettings(Context context)
    {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        AppSettings.setUseImperial(prefs.getBoolean("useImperial", false));

        
        AppSettings.setShowInNotificationBar(prefs.getBoolean(
                "show_notification", true));

        AppSettings.setPreferCellTower(prefs.getBoolean("prefer_celltower",
                false));


        String minimumSecondsString = prefs.getString("time_before_logging",
                "60");

        if (minimumSecondsString != null && minimumSecondsString.length() > 0)
        {
            AppSettings
                    .setMinimumSeconds(Integer.valueOf(minimumSecondsString));
        }
        else
        {
            AppSettings.setMinimumSeconds(60);
        }


    }

    public static void ShowProgress(Context ctx, String title, String message)
    {
        if (ctx != null)
        {
            pd = new ProgressDialog(ctx, ProgressDialog.STYLE_HORIZONTAL);
            pd.setMax(100);
            pd.setIndeterminate(true);

            pd = ProgressDialog.show(ctx, title, message, true, true);
        }
    }

    public static void HideProgress()
    {
        if (pd != null)
        {
            pd.dismiss();
        }
    }


    /**
     * Converts seconds into friendly, understandable description of time.
     *
     * @param numberOfSeconds
     * @return
     */
    public static String GetDescriptiveTimeString(int numberOfSeconds,
                                                  Context context)
    {

        String descriptive;
        int hours;
        int minutes;
        int seconds;

        int remainingSeconds;

        // Special cases
        if (numberOfSeconds == 1)
        {
            return context.getString(R.string.time_onesecond);
        }

        if (numberOfSeconds == 30)
        {
            return context.getString(R.string.time_halfminute);
        }

        if (numberOfSeconds == 60)
        {
            return context.getString(R.string.time_oneminute);
        }

        if (numberOfSeconds == 900)
        {
            return context.getString(R.string.time_quarterhour);
        }

        if (numberOfSeconds == 1800)
        {
            return context.getString(R.string.time_halfhour);
        }

        if (numberOfSeconds == 3600)
        {
            return context.getString(R.string.time_onehour);
        }

        if (numberOfSeconds == 4800)
        {
            return context.getString(R.string.time_oneandhalfhours);
        }

        if (numberOfSeconds == 9000)
        {
            return context.getString(R.string.time_twoandhalfhours);
        }

        // For all other cases, calculate

        hours = numberOfSeconds / 3600;
        remainingSeconds = numberOfSeconds % 3600;
        minutes = remainingSeconds / 60;
        seconds = remainingSeconds % 60;

        // Every 5 hours and 2 minutes
        // XYZ-5*2*20*

        descriptive = context.getString(R.string.time_hms_format,
                String.valueOf(hours), String.valueOf(minutes),
                String.valueOf(seconds));

        return descriptive;

    }

 

    /**
     * Given a Date object, returns an ISO 8601 date time string in UTC.
     * Example: 2010-03-23T05:17:22Z but not 2010-03-23T05:17:22+04:00
     *
     * @param dateToFormat The Date object to format.
     * @return The ISO 8601 formatted string.
     */
    public static String GetIsoDateTime(Date dateToFormat)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return sdf.format(dateToFormat);
    }

    public static String GetReadableDateTime(Date dateToFormat)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        return sdf.format(dateToFormat);
    }


    /**
     * Checks if a string is null or empty
     * @param text
     * @return
     */
    public static boolean IsNullOrEmpty(String text)
    {
        return text == null || text.length() == 0;
    }


    /**
     * Loops through an input stream and converts it into a string, then closes the input stream
     * @param is
     * @return
     */
    public static String GetStringFromInputStream(InputStream is)
    {
        String line;
        StringBuilder total = new StringBuilder();

        // Wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        // Read response until the end
        try
        {
            while ((line = rd.readLine()) != null)
            {
                total.append(line);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                is.close();
            }
            catch(Exception e)
            {
            	System.out.println("wrong when GetStringFromInputStream.");
            }
        }

        // Return full string
        return total.toString();
    }


    /**
     * Converts an input stream containing an XML response into an XML Document object
     * @param stream
     * @return
     */
    public static Document GetDocumentFromInputStream(InputStream stream)
    {
        Document doc;

        try
        {
            DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
            xmlFactory.setNamespaceAware(true);
            DocumentBuilder builder = xmlFactory.newDocumentBuilder();
            doc = builder.parse(stream);
        }
        catch (Exception e)
        {
            doc = null;
        }

        return doc;
    }
}
