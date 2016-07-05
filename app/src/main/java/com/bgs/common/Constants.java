package com.bgs.common;

import android.location.Location;
import android.location.LocationManager;

/**
 * Created by madhur on 3/1/15.
 */
public class Constants {

    public static final String TAG="dheket-log";
    public static final String TAG_CHAT="dheket-log-chat";
    public static final String CHAT_SERVER_URL = "http://136.243.214.45:3300/";
    //dheket
    //http://136.243.214.45:3300/
    //local with bolt http://192.168.1.100:3300/
    public static final Location DEMO_LOCATION = new Location(LocationManager.GPS_PROVIDER){{
        setLatitude(-6.212601);//-6.212601
        setLongitude(106.617825);//106.617825
    }};
}
