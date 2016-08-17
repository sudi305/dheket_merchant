package com.bgs.dheket.merchant;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bgs.dheket.accessingSensorPermission.Compass;
import com.bgs.dheket.sqlite.DBHelper;
import com.bgs.dheket.sqlite.ModelLocation;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.core.tasks.geocode.Locator;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by SND on 6/13/2016.
 */
public class AddNewOrUpdateLocationActivity_form_b extends AppCompatActivity implements View.OnClickListener{
    android.support.v7.app.ActionBar actionBar;
    String urls = "";
    String location_name = "", location_address = "", phone = "", description = "", location_tag = "", user_email = "", location_cat_name="";
    long id_location, merchant_id, create_by;
    double latitude, longitude;
    int category_id, isPromo;
    ModelLocation modelLocation;

    DBHelper db;

    EditText editText_loc_lat,editText_loc_lng;
    TextView textView_mapLatLng;
    MapView mMapView;
    Button button_centerLoc;
    ImageButton imageButton_getCurrentLoc,imageButton_next, imageButton_prev;

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
        setContentView(R.layout.activity_add_location_form_b);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.d_ic_back));
        //actionBar.setHomeAsUpIndicator(R.drawable.logo);
        actionBar.setHomeButtonEnabled(true);
        //actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>Location in Radius " + formatter.format(radius) + " Km</font>"));

        dataLoad();

        if (id_location>0) actionBar.setTitle("Update Data Location");
        else actionBar.setTitle("New Location");
        actionBar.setSubtitle("Coordinate");

        editText_loc_lat = (EditText)findViewById(R.id.editText_anl_loc_lat);
        editText_loc_lng = (EditText)findViewById(R.id.editText_anl_loc_lng);
        textView_mapLatLng = (TextView)findViewById(R.id.textView_anl_map_latlong);
        mMapView = (MapView)findViewById(R.id.map_selection);
        button_centerLoc = (Button)findViewById(R.id.button_centerLoc);
        button_centerLoc.setOnClickListener(buttonCenter);
        imageButton_getCurrentLoc = (ImageButton)findViewById(R.id.imageButton_anl_getCurrentLoc);
        imageButton_getCurrentLoc.setOnClickListener(buttonCurrentClik);
        imageButton_next = (ImageButton)findViewById(R.id.imageButton_anl_next_b);
        imageButton_next.setOnClickListener(this);
        imageButton_prev = (ImageButton)findViewById(R.id.imageButton_anl_prev_b);
        imageButton_prev.setOnClickListener(this);

        if (latitude!=0) editText_loc_lat.setText(""+latitude);
        if (longitude!=0)editText_loc_lng.setText(""+longitude);

        editText_loc_lat.addTextChangedListener(checkValidation);
        editText_loc_lng.addTextChangedListener(checkValidation);

        mMapView.setOnStatusChangedListener(statusChangedListener);
        mMapView.setOnSingleTapListener(mapTapCallback);
        mMapView.setOnLongPressListener(mapLongPress);
        mResultsLayer = new GraphicsLayer();
        mResultsLayer.setSelectionColorWidth(6);
        mMapView.addLayer(mResultsLayer);
        mMapView.setAllowRotationByPinch(true);

        // Enabled wrap around map.
        mMapView.enableWrapAround(true);

        // Create the Compass custom view, and add it onto the MapView.
        mCompass = new Compass(AddNewOrUpdateLocationActivity_form_b.this, null, mMapView);
        mMapView.addView(mCompass);
        mAdd = new PictureMarkerSymbol(getApplicationContext(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.pin_add));

        enableButton();
        setupLocator();
        setupLocationListener();
    }

    public TextWatcher checkValidation = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            enableButton();
        }
    };

    public void enableButton(){
        if (!editText_loc_lat.getText().toString().isEmpty() && !editText_loc_lng.getText().toString().isEmpty()){
            imageButton_next.setEnabled(true);
        } else {
            imageButton_next.setEnabled(false);
        }
    }

    public void dataLoad(){
        db = new DBHelper(getApplicationContext());
        modelLocation = new ModelLocation();
        user_email = db.getMerchantTopId();
        modelLocation = db.getLocationByEmail(user_email);
        location_name = modelLocation.getLocation_name().toString();
        location_address = modelLocation.getLocation_address().toString();
        location_cat_name = modelLocation.getCategory_name();
        phone = modelLocation.getPhone();
        description = modelLocation.getDescription();
        location_tag = modelLocation.getLocation_tag();
        id_location = modelLocation.getId_location();
        merchant_id = modelLocation.getMerchant_id();
        create_by = modelLocation.getCreate_by();
        latitude = modelLocation.getLatitude();
        longitude = modelLocation.getLongitude();
        category_id = modelLocation.getCategory_id();
        isPromo = modelLocation.getIsPromo();
    }

    public void dataPrepareSave(){
        latitude = Double.parseDouble(editText_loc_lat.getText().toString());
        longitude = Double.parseDouble(editText_loc_lng.getText().toString());
        ModelLocation loc = new ModelLocation(id_location,location_name,location_address,latitude,longitude,category_id,location_cat_name,
                phone,isPromo,merchant_id,create_by,description,location_tag,user_email);
        db.updateLocation(loc);
        db.closeDB();
    }

    public void onBackPressed() {
        toMainMenu();
    }

    public void toMainMenu(){
        Intent toMainMenu = new Intent(getApplicationContext(),AddNewOrUpdateLocationActivity.class);
        startActivity(toMainMenu);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //this.menu = menu;
        //getMenuInflater().inflate(R.menu.menu_main_slider, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (item.getItemId() == android.R.id.home) {
            toMainMenu();
            return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    final View.OnClickListener buttonCurrentClik = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            mMapView.setRotationAngle(0);
            // Also reset the compass angle.
            mCompass.setRotationAngle(0);
            //Point point = getAsPoint(location);
            mResultsLayer.removeAll();
            if ((mLDM != null) && (mLDM.getLocation() != null)) {
                // Keep current scale and go to current location, if there is one.
                Point point = new Point();
                point.setXY(mLDM.getLocation().getLongitude(), mLDM.getLocation().getLatitude());
                locationTouch.setLatitude(point.getY());
                locationTouch.setLongitude(point.getX());

                point = getAsPoint(locationTouch);
                Symbol symbol = mAdd;

                mResultsLayer.addGraphic(new Graphic(point, symbol));
                mMapView.setExtent(point, 100);


                editText_loc_lat.setText("" + locationTouch.getLatitude());
                editText_loc_lng.setText("" + locationTouch.getLongitude());
            }

            textView_mapLatLng.setText("lat:"+locationTouch.getLatitude()+"\nlng:"+locationTouch.getLongitude());
            centerViewMap();
        }
    };

    final View.OnClickListener buttonCenter = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            centerViewMap();
        }
    };

    final OnLongPressListener mapLongPress = new OnLongPressListener() {
        @Override
        public boolean onLongPress(float v, float v1) {
            //Toast.makeText(rootView.getContext().getApplicationContext(), "this location at x= " + v + " and y= " + v1 + " | point " + onSingleTaps(v, v1) + "", Toast.LENGTH_SHORT).show();
            setPinLocation(v,v1,1);
            mMapView.setRotationAngle(0);
            // Also reset the compass angle.
            mCompass.setRotationAngle(0);
            return false;
        }
    };

    /**
     * When the map is tapped, select the graphic at that location.
     */
    final OnSingleTapListener mapTapCallback = new OnSingleTapListener() {
        @Override
        public void onSingleTap(float x, float y) {
            // Find out if we tapped on a Graphic

            //Toast.makeText(rootView.getContext().getApplicationContext(),"this location at x= "+x+" and y= "+y+" | point on SingleTaps"+onSingleTaps(x,y)+"",Toast.LENGTH_SHORT).show();
            setPinLocation(x,y,0);
            int[] graphicIDs = mResultsLayer.getGraphicIDs(x, y, 25);
            if (graphicIDs != null && graphicIDs.length > 0) {
                // If there is more than one graphic, only select the first found.

            } else {
                mMapView.setRotationAngle(0);
                // Also reset the compass angle.
                mCompass.setRotationAngle(0);
            }
        }
    };

    public void setPinLocation(float x, float y, int longOrSingle){
        mResultsLayer.removeAll();
        Point point = null;
        point= onSingleTaps(x, y);
        locationTouch.setLatitude(point.getY());
        locationTouch.setLongitude(point.getX());

        point = getAsPoint(locationTouch);
        Symbol symbol = mAdd;

        mResultsLayer.addGraphic(new Graphic(point, symbol));
        mMapView.setExtent(point, 100);

        if (longOrSingle==1){//long click
            editText_loc_lat.setText("" + locationTouch.getLatitude());
            editText_loc_lng.setText("" + locationTouch.getLongitude());
        }
        textView_mapLatLng.setText("lat:"+locationTouch.getLatitude()+"\nlng:"+locationTouch.getLongitude());
    }

    /**
     * When map is ready, set up the LocationDisplayManager.
     */
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

    /**
     * Zoom to location using a specific size of extent.
     *
     * @param loc  the location to center the MapView at
     */
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

    public Point onSingleTaps(float x, float y) {
        Point pnt = (Point) GeometryEngine.project(mMapView.toMapPoint(x, y), mMapView.getSpatialReference(), SpatialReference.create(4326));
        return pnt;
    }

    public void updateContent(Map<String, Object> attributes) {
        // This is called from UI thread (MapTap listener)

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


    @Override
    public void onClick(View v) {
        if (v.equals(imageButton_next)){
            dataPrepareSave();
            Intent toNext = new Intent(getApplicationContext(), AddNewOrUpdateLocationActivity_form_c.class);
            startActivity(toNext);
            finish();
        }
        if (v.equals(imageButton_prev)){
            dataPrepareSave();
            toMainMenu();
        }
    }
}
