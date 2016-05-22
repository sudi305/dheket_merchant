package com.bgs.dheket.accessingSensorPermission;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;

import com.facebook.login.LoginManager;

/**
 * Created by SND on 21/01/2016.
 */
public class ConfigInternetAndGPS extends Activity {
    private Context context;

    public ConfigInternetAndGPS(Context context){
        this.context = context;
    }

    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null){
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null){
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    public void showAlertInternet(){
        if (!isConnectingToInternet()){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Alert");
            builder.setMessage("No Internet Connection! Please turn on your internet connection!")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            LoginManager.getInstance().logOut();
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("No", null);
            builder.create().show();
        }
    }

    public boolean isGPSActived(){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null){
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled == true)
                return true;
        }
        return false;
    }

    public void showAlertGPSInternet(){
        if (!isGPSActived() || !isConnectingToInternet()){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Alert");
            String message = "";
            if (!isConnectingToInternet()) message="No Internet Connection! Please turn on your internet connection!";
            else if (!isGPSActived()) message="Your GPS is turn off! Please turn on your GPS!";
            else message="Your GPS is turn off and No Internet Connection! Please turn on your GPS and internet connection!";
            builder.setMessage(message)
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            LoginManager.getInstance().logOut();
                            Intent intent = null;
                            if (!isGPSActived())
                                intent = new Intent(Settings.ACTION_SETTINGS);
                            else intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("No", null);
            builder.create().show();
        }
    }

    public void turnGPSOn(){ //on android version 4.4 to up isn't
        Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
        intent.putExtra("enabled", true);
        this.context.sendBroadcast(intent);

        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            this.context.sendBroadcast(poke);


        }
    }
    // automatic turn off the gps
    public void turnGPSOff(){
        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(provider.contains("gps")){ //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            this.context.sendBroadcast(poke);
        }
    }

}
