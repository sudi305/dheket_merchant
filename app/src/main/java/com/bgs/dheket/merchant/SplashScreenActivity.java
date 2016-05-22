package com.bgs.dheket.merchant;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bgs.dheket.accessingSensorPermission.ConfigInternetAndGPS;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.splunk.mint.Mint;

/**
 * Created by SND on 18/01/2016.
 */
public class SplashScreenActivity extends AppCompatActivity {
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;
    ImageView imgLogo;
    int wH_logo;
    String url = "";
    ConfigInternetAndGPS gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //Mint.initAndStartSession(SplashScreenActivity.this, "609d861e");
        FacebookSdk.sdkInitialize(getApplicationContext());
        imgLogo = (ImageView)findViewById(R.id.imgLogo);

        final float scale = getResources().getDisplayMetrics().density;
        wH_logo = (int)(200 * scale + 0.5f);

        gps = new ConfigInternetAndGPS(getApplicationContext());

        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.dheket);
        //url = String.format(getResources().getString(R.string.link_cekUserLogin));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showAlertGPS();
                /*Intent intentRedirectionGPSSettings = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intentRedirectionGPSSettings.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivityForResult(intentRedirectionGPSSettings, 0);*/
            }
        }, SPLASH_TIME_OUT);
    }

    private void toNextScreen(){
        if (AccessToken.getCurrentAccessToken() != null) {
            Intent loginWithFb = new Intent(SplashScreenActivity.this, MainMenuActivity.class);
                    /*Intent loginWithFb = new Intent(SplashScreenActivity.this, MainActivity.class);*/
            startActivity(loginWithFb);
            finish();
        } else {
            Intent i = new Intent(SplashScreenActivity.this, FormLoginActivity.class);
            startActivity(i);
            finish();
        }
    }

    public void showAlertGPS(){
        if (!gps.isGPSActived()){
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
            builder.setTitle("Alert");
            String message = "";
            message="Your GPS is turn off! Please turn on your GPS for the best location!";
            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                                    /*LoginManager.getInstance().logOut();*/
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 1);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toNextScreen();
                        }
                    });
            builder.create().show();
        } else {
            toNextScreen();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*//if (resultCode == 1) {
            switch (requestCode) {
                case 1:
                    Log.e("test", "go to next screen here");
                    break;
                default:
                    Log.e("test", "stay here");
                    break;
            }
        //}*/
        if (!gps.isGPSActived()){
            showAlertGPS();
        } else {
            toNextScreen();
        }
    }
}
