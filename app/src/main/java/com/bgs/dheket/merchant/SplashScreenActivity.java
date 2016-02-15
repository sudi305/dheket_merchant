package com.bgs.dheket.merchant;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bgs.imageOrView.RoundImage;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;

/**
 * Created by SND on 18/01/2016.
 */
public class SplashScreenActivity extends Activity{
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;
    ImageView imgLogo;
    RoundImage crop_image_circle;
    int wH_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        FacebookSdk.sdkInitialize(getApplicationContext());
        imgLogo = (ImageView)findViewById(R.id.imgLogo);
        final Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);

        final float scale = getResources().getDisplayMetrics().density;
        wH_logo = (int)(200 * scale + 0.5f);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.logo);
        crop_image_circle = new RoundImage(bitmap,wH_logo);
        imgLogo.setImageDrawable(crop_image_circle);
        imgLogo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgLogo.setAnimation(animAlpha);



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AccessToken.getCurrentAccessToken() != null) {
                    Intent loginWithFb = new Intent(SplashScreenActivity.this, MainMenuActivity.class);
                    startActivity(loginWithFb);
                    finish();
                }else {
                    Intent i = new Intent(SplashScreenActivity.this, FormLoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }
}
