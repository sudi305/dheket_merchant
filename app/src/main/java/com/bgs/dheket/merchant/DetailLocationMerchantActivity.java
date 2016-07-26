package com.bgs.dheket.merchant;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bgs.dheket.accessingSensorPermission.Compass;
import com.bgs.dheket.sqlite.DBHelper;
import com.bgs.dheket.sqlite.ModelLocation;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.core.tasks.geocode.Locator;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by SND on 28/03/2016.
 */
public class DetailLocationMerchantActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener{

    android.support.v7.app.ActionBar actionBar;

    String location_name = "", location_address = "", phone = "", description = "", location_tag = "", user_email = "", location_cat_name;
    String tempDataTag = "";
    long id_location, merchant_id;
    double latitude, longitude;
    int category_id, isPromo;
    ModelLocation modelLocation;
    DBHelper db;

    String url = "",responseServer="";
    private JSONObject JsonObject, jsonobject;
    boolean deleteLoc = false;

    Button button_delete, button_unclaimed, button_updade;
    TextView textView_name, textView_address, textView_phone, textView_description, textView_category,
            textView_latitude, textView_longitude, textView_tag;
    MapView mMapView;

    final static double ZOOM_BY = 10;

    SpatialReference mMapSr = null;
    GraphicsLayer mResultsLayer = null;
    PictureMarkerSymbol mAdd;

    Locator mLocator;
    Location locationTouch;
    Location location;
    ArrayList<String> mFindOutFields = new ArrayList<>();

    LocationDisplayManager mLDM;
    Compass mCompass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_loc_merchant_body);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.d_ic_back));
        //actionBar.setHomeAsUpIndicator(R.drawable.logo);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Detail Location");
//        actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>Location in Radius " + formatter.format(radius) + " Km</font>"));
        dataLoad();
        splitTag();

        button_unclaimed = (Button)findViewById(R.id.button_det_loc_unclaimed);
        button_unclaimed.setOnClickListener(this);
        button_delete = (Button)findViewById(R.id.button_det_loc_delete);
        button_delete.setOnClickListener(this);
        button_updade = (Button)findViewById(R.id.button_det_loc_update);
        button_updade.setOnClickListener(this);

        textView_name = (TextView)findViewById(R.id.textView_dl_name);
        textView_address = (TextView)findViewById(R.id.textView_dl_address);
        textView_phone = (TextView)findViewById(R.id.textView_dl_hp);
        textView_category = (TextView)findViewById(R.id.textView_dl_category);
        textView_description = (TextView)findViewById(R.id.textView_dl_description);
        textView_latitude = (TextView)findViewById(R.id.textView_dl_latitude);
        textView_longitude = (TextView)findViewById(R.id.textView_dl_longitude);
        textView_tag = (TextView)findViewById(R.id.textView_dl_tag);

        textView_name.setText(location_name);
        textView_address.setText(location_address);
        textView_phone.setText(phone);
        textView_description.setText(description);
        textView_category.setText(location_cat_name);
        textView_latitude.setText("" + latitude);
        textView_longitude.setText(""+longitude);
        textView_tag.setText(tempDataTag);

        mMapView = (MapView)findViewById(R.id.map_selection_det_loc);
        mMapView.setOnStatusChangedListener(statusChangedListener);
        mResultsLayer = new GraphicsLayer();
        mResultsLayer.setSelectionColorWidth(6);
        mMapView.addLayer(mResultsLayer);
        mMapView.setAllowRotationByPinch(true);

        // Enabled wrap around map.
        mMapView.enableWrapAround(true);

        // Create the Compass custom view, and add it onto the MapView.
        mCompass = new Compass(DetailLocationMerchantActivity.this, null, mMapView);
        mMapView.addView(mCompass);
        mAdd = new PictureMarkerSymbol(getApplicationContext(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.pin_add));
        setupLocator();
        setupLocationListener();
        setPinLocation(latitude,latitude);
    }

    final OnStatusChangedListener statusChangedListener = new OnStatusChangedListener() {

        private static final long serialVersionUID = 1L;

        @Override
        public void onStatusChanged(Object source, STATUS status) {
            if (source == mMapView && status == STATUS.INITIALIZED) {
                mMapSr = mMapView.getSpatialReference();
                if (mLDM == null) {
                    setupLocationListener();
                }
            }
        }
    };

    private void centerViewMap(){
        if (mMapView.isLoaded()) {
            // If LocationDisplayManager has a fix, pan to that location. If no
            // fix yet, this will happen when the first fix arrives, due to
            // callback set up previously.
            if ((mLDM != null) && (mLDM.getLocation() != null)) {
                // Keep current scale and go to current location, if there is one.
                mLDM.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
            }
        }
    }

    private void setupLocator() {
        // Parameterless constructor - uses the Esri world geocoding service.
        mLocator = Locator.createOnlineLocator();
        // Set up the outFields parameter for the search.
        mFindOutFields.add("loc_lat");
        mFindOutFields.add("loc_lng");
    }

    private void setupLocationListener() {
        if ((mMapView != null) && (mMapView.isLoaded())) {
            mLDM = mMapView.getLocationDisplayManager();
            mLDM.setLocationListener(new LocationListener() {

                boolean locationChanged = false;

                // Zooms to the current location when first GPS fix arrives.
                @Override
                public void onLocationChanged(Location loc) {
                    if (!locationChanged) {
                        Log.e("location change", "" + loc);
                        locationChanged = true;
                        locationTouch = loc;
                        location = loc;
                        mLDM.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
                        zoomToLocation(loc);
                    }
                }

                @Override
                public void onProviderDisabled(String arg0) {
                }

                @Override
                public void onProviderEnabled(String arg0) {
                }

                @Override
                public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
                }
            });

            mLDM.start();
        }
    }

    private void zoomToLocation(Location loc) {
        Point mapPoint = getAsPoint(loc);
        Unit mapUnit = mMapSr.getUnit();
        double zoomFactor = Unit.convertUnits(ZOOM_BY,
                Unit.create(LinearUnit.Code.MILE_US), mapUnit);
        Envelope zoomExtent = new Envelope(mapPoint, zoomFactor, zoomFactor);
        mMapView.setExtent(zoomExtent);
    }

    private void clearCurrentResults() {
        if (mResultsLayer != null) {
            mResultsLayer.removeAll();
        }
    }

    private Point getAsPoint(Location loc) {
        Point wgsPoint = new Point(loc.getLongitude(), loc.getLatitude());
        return (Point) GeometryEngine.project(wgsPoint, SpatialReference.create(4326),
                mMapSr);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.pause();
        if (mLDM != null) {
            mLDM.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.unpause();
        if (mLDM != null) {
            mLDM.resume();
        }
        setupLocator();
        setupLocationListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mLDM != null) {
            mLDM.stop();
        }
    }

    public void setPinLocation(double lat, double lng){
        MultiPoint fullExtent = new MultiPoint();
        Symbol symbol = null;
        //-6.21267000, 106.61778566
        mResultsLayer.removeAll();
        clearCurrentResults();

        Location locationPin = new Location(LocationManager.GPS_PROVIDER);
        Toast.makeText(this,"lat long "+lat+" "+lng, Toast.LENGTH_SHORT).show();
        locationPin.setLatitude(lat);
        locationPin.setLongitude(lng);
        Point point = getAsPoint(locationPin);
        symbol = mAdd;

        mResultsLayer.addGraphic(new Graphic(point, symbol));
        fullExtent.add(point);

        mMapView.setExtent(fullExtent, 100);
        if ((mLDM != null) && (mLDM.getLocation() != null)) {
            // Keep current scale and go to current location, if there is one.
            zoomToLocation(locationPin);
            mLDM.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
        }

    }

    public void dataLoad(){
        db = new DBHelper(getApplicationContext());
        modelLocation = new ModelLocation();
        user_email = db.getMerchantTopId();
        modelLocation = db.getLocationByEmail(user_email);
        location_name = modelLocation.getLocation_name().toString();
        location_address = modelLocation.getLocation_address().toString();
        phone = modelLocation.getPhone();
        location_cat_name = modelLocation.getCategory_name();
        description = modelLocation.getDescription();
        location_tag = modelLocation.getLocation_tag();
        id_location = modelLocation.getId_location();
        merchant_id = modelLocation.getMerchant_id();
        latitude = modelLocation.getLatitude();
        longitude = modelLocation.getLongitude();
        category_id = modelLocation.getCategory_id();
        isPromo = modelLocation.getIsPromo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        //getMenuInflater().inflate(R.menu.menu_detail_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            back_to_previous_screen();
            return super.onOptionsItemSelected(item);
        }

        /*if (item.getItemId() == R.id.select_map){
            toMapScreen();
            return super.onOptionsItemSelected(item);
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        dataPrepareSave();
    }

    /*public void toMapScreen(){
        MapViewSingleActivity.startFromLocationNoMerchant(getApplicationContext(), lokasi, currentBestLocation);
        *//*
        //paket.putInt("location_id", Integer.parseInt(arraylist.get(0).get("loc_id")));
        *//*
        finish();
    }
*/
    public void back_to_previous_screen(){
        Intent intent = new Intent(this, ListLocationMerchantActivity.class);
        Bundle dataPaket = new Bundle();
        dataPaket.putDouble("latitude", 0);
        dataPaket.putDouble("longitude", 0);
        intent.putExtras(dataPaket);
        startActivity(intent);
        finish();
    }

    /*public void getDetailLocation() {
        CallWebPageTask task = new CallWebPageTask(this);
        double latitude =0, longitude = 0;
        if ( currentBestLocation != null) {
            latitude = currentBestLocation.getLatitude();
            longitude = currentBestLocation.getLongitude();
        }
        String urls = url + "/" + latitude + "/" + longitude + "/" + lokasi.getId();
        Log.d(Constants.TAG, "Get Detail Lokasi url => " + urls);
        task.execute(new String[]{urls});
    }*/

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    public void splitTag(){
        tempDataTag = "#"+location_tag;
        tempDataTag = tempDataTag.replaceAll(",\\s"," #");
    }

    /*@Override
    public void onPageSelected(int position) {
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));
        }

        dots[position].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));

        if (position + 1 == dotsCount) {
            *//*btnNext.setVisibility(View.GONE);
            btnFinish.setVisibility(View.VISIBLE);*//*
        } else {
            *//*btnNext.setVisibility(View.VISIBLE);
            btnFinish.setVisibility(View.GONE);*//*
        }
        currentPage = position;
    }*/

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        String message = "";
        if (v.equals(button_updade)){
            Intent toUpdate = new Intent(this,AddNewOrUpdateLocationActivity.class);
            startActivity(toUpdate);
        }
        if (v.equals(button_delete)){
            deleteLoc = true;
            final AlertDialog.Builder builder = new AlertDialog.Builder(DetailLocationMerchantActivity.this);
            builder.setTitle("Warning");
            message = "Are you sure to delete this location ("+location_name+")?";
            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                                    /*LoginManager.getInstance().logOut();*/
                            AsyncTAddingDataToServer asyncTAddingDataToServer = new AsyncTAddingDataToServer();
                            asyncTAddingDataToServer.execute();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            builder.create().dismiss();
                        }
                    });
            builder.create().show();
        }
        if (v.equals(button_unclaimed)){
            deleteLoc = false;
            final AlertDialog.Builder builder = new AlertDialog.Builder(DetailLocationMerchantActivity.this);
            builder.setTitle("Confirmation");
            message = "Unclaimed this location ("+location_name+"), are you sure?";
            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                                    /*LoginManager.getInstance().logOut();*/
                            AsyncTAddingDataToServer asyncTAddingDataToServer = new AsyncTAddingDataToServer();
                            asyncTAddingDataToServer.execute();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            builder.create().dismiss();
                        }
                    });
            builder.create().show();
        }
    }

    public static class InputStreamToStringExample {

        public static void main(String[] args) throws IOException {

            // intilize an InputStream
            InputStream is = new ByteArrayInputStream("file content is process".getBytes());

            String result = getStringFromInputStream(is);

            System.out.println(result);
            System.out.println("Done");

        }

        // convert InputStream to String
        private static String getStringFromInputStream(InputStream is) {

            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();

            String line;
            try {

                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return sb.toString();
        }
    }

    /* Inner class to get response */
    class AsyncTAddingDataToServer extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            String url = "";
            if (deleteLoc==true){
                url = String.format(getResources().getString(R.string.link_deleteLocationById));
            } else {
                url = String.format(getResources().getString(R.string.link_claimOrRemoveLocation));
            }
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            Log.e("link", url + " | " + category_id + " | " + user_email);
            try {
                JSONObject jsonobj = new JSONObject();
                if (deleteLoc==true){
                    jsonobj.put("loc_id",id_location);
                } else {
                    jsonobj.put("email", " ");
                    jsonobj.put("loc_hashtag", location_tag);
                    jsonobj.put("loc_id", id_location);
                }

                Log.e("mainToPost", "mainToPost" + jsonobj.toString());
                httppost.setEntity(new StringEntity(jsonobj.toString())); //json without header {"a"="a","b"=1}
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                InputStream inputStream = response.getEntity().getContent();
                InputStreamToStringExample str = new InputStreamToStringExample();
                responseServer = str.getStringFromInputStream(inputStream);
                Log.e("response", "response ----- " + responseServer.toString() + "|");
                Log.e("response", "response ----- " + responseServer.toString().equalsIgnoreCase("{\"success\":1}") + "|");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (responseServer!=null && responseServer.equalsIgnoreCase("{\"success\":1}")) {
                Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
                responseServer="";
                dataPrepareSave();
            } else {
                if (responseServer.equalsIgnoreCase("") || responseServer.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Ops, Error! Please Try Again!",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void dataPrepareSave(){
        ModelLocation location = new ModelLocation(0,"","",0.0,0.0,0,"","",0,0,"","",user_email);
        long result = db.updateLocation(location);
        if (result==1)back_to_previous_screen();
        db.closeDB();
    }

    /*private class CallWebPageTask extends AsyncTask<String, Void, String> {
        protected Context context;
        private Dialog dialog;
        //private Lokasi lokasiDetail;

        public CallWebPageTask(Context context) {
            this.context = context;
            //this.dialog = DialogUtils.LoadingSpinner(context);
        }

        @Override
        protected void onPreExecute() {
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpGetOrPost httpGetOrPost = new HttpGetOrPost();
            response = httpGetOrPost.getRequest(urls[0]);
            try {
                HashMap<String, String> map = new HashMap<String,String>();
                JSONObject joResponse = new JSONObject(response);
                JSONArray joArrayLokasi = joResponse.getJSONArray("dheket_singleLoc");
                //jika banyak data cuma diambil 1
                final JSONObject data =  joArrayLokasi.getJSONObject(0);

            } catch (JSONException e) {
                Log.e(Constants.TAG, e.getMessage(), e);
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            updateData(lokasiDetail);

            if ( this.dialog.isShowing())
                this.dialog.dismiss();
        }
    }*/

    /*public void updateData(Lokasi lokasiDetail) {
        Log.d(Constants.TAG, "lokasiDetail => " + lokasiDetail);
        if (lokasiDetail != null){
            setReference();

            textView_namaloc.setText(lokasiDetail.getName());
            actionBar.setTitle(textView_namaloc.getText());
            textView_alamatloc.setText("@"+lokasiDetail.getAddress());
            Log.d(Constants.TAG, "D1 => " + lokasiDetail.getDistance());
            Log.d(Constants.TAG, "D2 => " + Utility.changeFormatNumber(lokasiDetail.getDistance()));
            double distance = lokasiDetail.getDistance();
            textView_distanceloc.setText(Utility.andjustDistanceUnit(distance));

            textView_descriptionloc.setText(lokasiDetail.getDescription());
            //textView_simpledescloc = (TextView)findViewById(R.id.textView_dl_nm_loc_simple_description);
            //textView_pricepromo = (TextView)findViewById(R.id.textView_dl_nm_loc_pricepromo);
            textView_gotoloc.setText("GO TO " + lokasiDetail.getName().toUpperCase()+"  ");
        }

    }*/

    /*public void setReference() {
        mAdapter = new ViewPagerAdapter(getApplicationContext(), arraylist_foto);
        Log.d(Constants.TAG, "foto => "+arraylist_foto.toString());
        intro_images.setAdapter(mAdapter);
        intro_images.setCurrentItem(0);
        intro_images.setOnPageChangeListener(this);
        intro_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        setUiPageViewController();

        NUM_PAGES = arraylist_foto.length;

        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                }
                intro_images.setCurrentItem(currentPage++, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 3000, 3000);
    }

    private void setUiPageViewController() {

        dotsCount = mAdapter.getCount();
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(4, 0, 4, 0);

            pager_indicator.addView(dots[i], params);
        }

        dots[0].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));
    }*/
}
