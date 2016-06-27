package com.bgs.dheket.merchant;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bgs.dheket.accessingSensorPermission.HttpGetOrPost;
import com.bgs.dheket.general.Utility;
import com.bgs.dheket.sqlite.DBHelper;
import com.bgs.dheket.sqlite.ModelLocation;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
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
import com.facebook.AccessToken;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by SND on 6/5/2016.
 */
public class SearchLocationByNameActivity extends AppCompatActivity {
    final static double ZOOM_BY = 15;

    MapView mMapView = null;
    SpatialReference mMapSr = null;
    GraphicsLayer mResultsLayer = null;

    PictureMarkerSymbol mcat;
    int[] id_cat;
    String[] icon_cat;
    Menu menu;
    // Views to show selected search result information.

    android.support.v7.app.ActionBar actionBar;

    Locator mLocator;
    Location locationTouch;
    Location location;
    ArrayList<String> mFindOutFields = new ArrayList<>();

    LocationDisplayManager mLDM;

    Utility formatNumber = new Utility();

    private JSONObject jObject;
    private String jsonResult = "";
    double radius = 0.0;
    double latitude, longitude;
    String urls = "", keyword = "", url_claim = "";
    String parameters, email, icon,category;
    String response = "", responseServer = "";
    int id_Loc = 0;
    LinearLayout linearLayout_contentlist;

    ArrayList<HashMap<String, String>> arraylist;
    /*String[] loc_name, loc_address, loc_pic;
    int[] loc_promo, id_loc;
    double[] loc_distance, loc_lat, loc_lng;*/

    boolean isFirst = true, maxView = true, minView = true;
    CallWebPageTask task;
    Bundle paket;
    DBHelper db;

    EditText editText_searchLoc;
    Button btn_searchLoc;
    ScrollView scroll;
    TextView textView_notFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_map_loc_search);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.d_ic_back));
        //actionBar.setHomeAsUpIndicator(R.drawable.logo);
        actionBar.setHomeButtonEnabled(true);

        //Retrieve the map and initial extent from XML layout
        mMapView = (MapView) findViewById(R.id.map_single);
        mMapView.setOnStatusChangedListener(statusChangedListener);
        mMapView.setOnSingleTapListener(mapTapCallback);
        //mMapView.setOnLongPressListener(mapLongPress);

        db = new DBHelper(getApplicationContext());
        email = db.getMerchantTopId();
        db.closeDB();

        paket = getIntent().getExtras();
        latitude = paket.getDouble("latitude");
        longitude = paket.getDouble("longitude");

        actionBar.setTitle("Find Locations");
//        actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>Location in Radius " + formatter.format(radius) + " Km</font>"));

        urls = String.format(getResources().getString(R.string.link_getSearchLocation));
        url_claim = String.format(getResources().getString(R.string.link_claimOrRemoveLocation));//<!--claimlocationformerchant/{email}/{loc_id}/{loc_hashtag}-->

        mResultsLayer = new GraphicsLayer();
        mResultsLayer.setSelectionColorWidth(6);
        mMapView.addLayer(mResultsLayer);
        mMapView.setAllowRotationByPinch(true);

        // Enabled wrap around map.
        mMapView.enableWrapAround(true);

        //mAdd = new PictureMarkerSymbol(rootView.getContext().getApplicationContext(), ContextCompat.getDrawable(rootView.getContext().getApplicationContext(), R.drawable.pin_add));
        linearLayout_contentlist = (LinearLayout)findViewById(R.id.linearLayout_result_lm);
        setupLocator();
        setupLocationListener();

        editText_searchLoc = (EditText)findViewById(R.id.lmls_editText_search);
        editText_searchLoc.addTextChangedListener(textWatcher);
        textView_notFound = (TextView)findViewById(R.id.lmls_textView_result);
        btn_searchLoc = (Button)findViewById(R.id.lmls_button_search);
        btn_searchLoc.setEnabled(false);
        scroll = (ScrollView)findViewById(R.id.lmls_scrollView_result);
        scroll.setVisibility(View.INVISIBLE);

        btn_searchLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataFromServer();
            }
        });
    }

    final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (editText_searchLoc.getText().length() > 2) btn_searchLoc.setEnabled(true);
            else btn_searchLoc.setEnabled(false);
        }
    };

    public void getDataFromServer() {
        task = new CallWebPageTask();
        task.applicationContext = getApplicationContext();
        String key = editText_searchLoc.getText().toString();
        keyword = key.replaceAll("\\s", "%20"); //replaceAll(" ","%20");
        /*getlocationbycategoryid/{rad}/{center_lat}/{center_lng}/{cat}*/
        parameters = urls +keyword+"/" + latitude + "/" + longitude;
        Log.e("OK Connecting Sukses", "" + parameters);
        //Log.e("Sukses", parameters);
        task.execute(new String[]{parameters});
    }

    /**
     * When the map is tapped, select the graphic at that location.
     */
    final OnSingleTapListener mapTapCallback = new OnSingleTapListener() {
        @Override
        public void onSingleTap(float x, float y) {
            // Find out if we tapped on a Graphic
            /*Intent gotoMapExtend = new Intent(getApplicationContext(),MapViewExtendActivity.class);
            Bundle paket = new Bundle();
            paket.putInt("cat_id", cat_id);
            paket.putString("kategori", category);
            paket.putDouble("radius", radius);
            paket.putDouble("latitude", latitude);
            paket.putDouble("longitude", longitude);
            paket.putString("icon", icon);
            gotoMapExtend.putExtras(paket);
            startActivity(gotoMapExtend);
            finish();*/
        }
    };

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

    private void setupLocator() {
        // Parameterless constructor - uses the Esri world geocoding service.
        mLocator = Locator.createOnlineLocator();

        // Set up the outFields parameter for the search.
        mFindOutFields.add("id_loc");
        mFindOutFields.add("loc_name");
        mFindOutFields.add("loc_address");
        mFindOutFields.add("loc_distance");
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
                        locationChanged = true;
                        Log.e("sukses location ", "lat " + loc.getLatitude() + " | lng " + loc.getLongitude() + " | point " + getAsPoint(loc));
                        location = loc;
                        locationTouch = location;
                        // After zooming, turn on the Location pan mode to show the location
                        // symbol. This will disable as soon as you interact with the map.
                        if (!isFirst) {
                            latitude = loc.getLatitude();
                            longitude = loc.getLongitude();
                        }

                        //getDataFromServer();
                        /*Toast.makeText(getApplicationContext(), "location change " + (looping++), Toast.LENGTH_SHORT).show();*/
                        mLDM.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
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
     * @param loc the location to center the MapView at
     */
    private void zoomToLocation(Location loc) {
        Point mapPoint = getAsPoint(loc);
        Unit mapUnit = mMapSr.getUnit();
        double zoomFactor = Unit.convertUnits(ZOOM_BY,
                Unit.create(LinearUnit.Code.MILE_US), mapUnit);
        Envelope zoomExtent = new Envelope(mapPoint, zoomFactor, zoomFactor);
        mMapView.setExtent(zoomExtent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        /*this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main_slider, menu);
        menu.getItem(0).setVisible(false);*/
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

        /*if (item.getItemId() == R.id.goto_setting) {
            *//*if (formRadius.is)*//*
            return super.onOptionsItemSelected(item);
        }

        if (item.getItemId() == R.id.goto_search) {
            return super.onOptionsItemSelected(item);
        }*/

        /*//noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout_user();
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        toMainMenu();
    }

    public void toMainMenu(){
        Intent toMainMenu = new Intent(getApplicationContext(),MainMenuActivity.class);
        startActivity(toMainMenu);
        finish();
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
        String title = attributes.get("loc_name").toString();
        //textView_loc_name.setText(title);

        String address = attributes.get("loc_address").toString();
        //textView_loc_address.setText(address);

        String id_loc = attributes.get("id_loc").toString();
        //textView_id_loc.setText(id_loc);

        String distance = attributes.get("loc_distance").toString();
        double meters = Double.parseDouble(distance);
        //textView_loc_distance.setText("" + formatNumber.changeFormatNumber(meters) + " Km");
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

    private class CallWebPageTask extends AsyncTask<String, Void, String> {

        private ProgressDialog dialog;
        protected Context applicationContext;

        @Override
        protected void onPreExecute() {
            /*if (isFirst==true){
                this.dialog = ProgressDialog.show(applicationContext, "Retrieving Data", "Please Wait...", true);
            }*/
        }

        @Override
        protected String doInBackground(String... url) {
            HttpGetOrPost httpGetOrPost = new HttpGetOrPost();
            response = httpGetOrPost.getRequest(url[0]);
            try {
                //simpan data dari web ke dalam array
                Log.e("response ", "|"+response+"|" + (response.isEmpty()));
                if (!response.isEmpty()) {
                    JSONArray menuItemArray = null;
                    jObject = new JSONObject(response);
                    menuItemArray = jObject.getJSONArray("dheket_searchLoc");
                    arraylist = new ArrayList<HashMap<String, String>>();
                    Log.e("Data dari server", "" + menuItemArray.length());
                    for (int i = 0; i < menuItemArray.length(); i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("id_loc", menuItemArray.getJSONObject(i).getString("id_location"));
                        map.put("loc_name", menuItemArray.getJSONObject(i).getString("location_name"));
                        map.put("loc_address", menuItemArray.getJSONObject(i).getString("location_address"));
                        map.put("loc_phone", menuItemArray.getJSONObject(i).getString("phone"));
                        map.put("loc_lat", menuItemArray.getJSONObject(i).getString("latitude"));
                        map.put("loc_lng", menuItemArray.getJSONObject(i).getString("longitude"));
                        map.put("cat_name", menuItemArray.getJSONObject(i).getString("category_name"));
                        map.put("loc_distance", "" + Double.parseDouble(formatNumber.changeFormatNumber(menuItemArray.getJSONObject(i).getDouble("distance"))));
                        map.put("merchant_name", menuItemArray.getJSONObject(i).getString("merchant_name"));
                        map.put("loc_tag", menuItemArray.getJSONObject(i).getString("location_tag"));
                        map.put("icon", menuItemArray.getJSONObject(i).getString("icon"));
                        arraylist.add(map);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("cek 2", "error" + e);
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            updateData();
        }
    }

    public void updateData() {
        MultiPoint fullExtent = new MultiPoint();
        Symbol symbol = null;
        //-6.21267000, 106.61778566
        Map<String, Object> attr = new HashMap<String, Object>();
        if (response.isEmpty()){
            textView_notFound.setVisibility(View.VISIBLE);
            scroll.setVisibility(View.INVISIBLE);
        } else {
            scroll.setVisibility(View.VISIBLE);
            textView_notFound.setVisibility(View.GONE);
            mResultsLayer.removeAll();
            clearCurrentResults();
            linearLayout_contentlist.removeAllViews();
        }

        if (arraylist != null) {
            for (int i = 0; i < arraylist.size() ; i++) {
                icon = arraylist.get(i).get("icon").toString();
                if (!icon.isEmpty() && !icon.equalsIgnoreCase("null") && !icon.equalsIgnoreCase("")){
                    Log.e("icon tidak kosong","["+icon+"]");
                    mcat= new PictureMarkerSymbol();
                    mcat.setUrl(icon);
                } else {
                    Log.e("icon kosong","["+icon+"]");
                    mcat= new PictureMarkerSymbol(getApplicationContext(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.pin_blue));
                }

                Location locationPin = location;
                locationPin.setLatitude(Double.parseDouble(arraylist.get(i).get("loc_lat").toString()));
                locationPin.setLongitude(Double.parseDouble(arraylist.get(i).get("loc_lng").toString()));
                Point point = getAsPoint(locationPin);
                attr.put("id_loc", arraylist.get(i).get("id_loc").toString());
                attr.put("loc_name", arraylist.get(i).get("loc_name").toString());
                attr.put("loc_address", arraylist.get(i).get("loc_address").toString());
                attr.put("loc_distance", arraylist.get(i).get("loc_distance").toString());

                symbol = mcat;

                mResultsLayer.addGraphic(new Graphic(point, symbol, attr));
                fullExtent.add(point);

                LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                final View ll = inflater.inflate(R.layout.item_listmod, null);
                final TextView id = (TextView)ll.findViewById(R.id.textView_il_id);
                id.setText(arraylist.get(i).get("id_loc").toString());
                final TextView nama = (TextView)ll.findViewById(R.id.textView_il_nama);
                nama.setText(arraylist.get(i).get("loc_name").toString());
                TextView alamat = (TextView)ll.findViewById(R.id.textView_il_alamat);
                alamat.setText(arraylist.get(i).get("loc_address").toString());
                TextView jarak = (TextView)ll.findViewById(R.id.textView_il_jarak);
                jarak.setText(arraylist.get(i).get("loc_distance").toString() + " Km");
                ImageView foto = (ImageView)ll.findViewById(R.id.imageView_il_foto);
                linearLayout_contentlist.addView(ll);

                final int index = i;

                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDetail(index);
                        //Toast.makeText(ll.getContext().getApplicationContext(),nama.getText().toString(),Toast.LENGTH_SHORT).show();
                        /*Intent goToScreen = new Intent(getApplicationContext(), DetailLocationWithNoMerchantActivity.class);
                        Bundle paket = new Bundle();
                        paket.putInt("location_id", Integer.parseInt(id.getText().toString()));
                        paket.putInt("cat_id", cat_id);
                        paket.putString("kategori", category);
                        paket.putDouble("radius", radius);
                        paket.putDouble("latitude", latitude);
                        paket.putDouble("longitude", longitude);
                        paket.putString("icon", icon);
                        goToScreen.putExtras(paket);
                        startActivity(goToScreen);
                        finish();*/
                    }
                });
            }

            mMapView.setExtent(fullExtent, 100);
            if (arraylist.size() < 2) {
                if ((mLDM != null) && (mLDM.getLocation() != null)) {
                    // Keep current scale and go to current location, if there is one.
                    zoomToLocation(location);
                    mLDM.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
                }
            }
        }
    }

    public void showDetail(int index){
        Button btn_claim, btn_close;
        TextView tv_locname, tv_locaddres, tv_locnumber, tv_catname, tv_loctag, tv_distance, tv_owner, tv_locid;
        LayoutInflater mInflater = LayoutInflater.from(this);

        View v = mInflater.inflate(R.layout.activity_preview_detail_location, null);

        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setView(v);
        dialog.setCancelable(true);

        btn_claim = (Button) v.findViewById(R.id.button_pdl_claim);
        btn_close = (Button) v.findViewById(R.id.button_pdl_close);
        if ((arraylist.get(index).get("merchant_name").toString()).equalsIgnoreCase("null") ||
                (arraylist.get(index).get("merchant_name").toString()).isEmpty() ||
                (arraylist.get(index).get("merchant_name").toString()).equalsIgnoreCase("-")) btn_claim.setEnabled(true);
        else btn_claim.setEnabled(false);
        //name = ((city.getName() == null) ? "N/A" : city.getName());

        tv_locid = (TextView) v.findViewById(R.id.textView_pdl_idloc);
        tv_locname = (TextView) v.findViewById(R.id.textView_pdl_locname);
        tv_locaddres = (TextView) v.findViewById(R.id.textView_pdl_locaddress);
        tv_locnumber = (TextView) v.findViewById(R.id.textView_pdl_locnumber);
        tv_catname = (TextView) v.findViewById(R.id.textView_pdl_catname);
        tv_loctag = (TextView) v.findViewById(R.id.textView_pdl_loctag);
        tv_distance = (TextView) v.findViewById(R.id.textView_pdl_distance);
        tv_owner = (TextView) v.findViewById(R.id.textView_pdl_owner);
        tv_locid.setText(arraylist.get(index).get("id_loc").toString());
        tv_locname.setText(arraylist.get(index).get("loc_name").toString());
        tv_locaddres.setText(arraylist.get(index).get("loc_address").toString());
        tv_locnumber.setText(arraylist.get(index).get("loc_phone").toString());
        tv_catname.setText(arraylist.get(index).get("cat_name").toString());
        tv_loctag.setText(arraylist.get(index).get("loc_tag").toString());
        tv_distance.setText(arraylist.get(index).get("loc_distance").toString()+" Km");
        tv_owner.setText(arraylist.get(index).get("merchant_name").toString());

        id_Loc = Integer.parseInt(tv_locid.getText().toString());

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_claim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                final AlertDialog.Builder builder = new AlertDialog.Builder(SearchLocationByNameActivity.this);
                builder.setTitle("Confirmation");
                String message = "";
                message="Are you sure to claim this location?";
                builder.setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                    /*LoginManager.getInstance().logOut();*/
                                claimLocation();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                builder.create().dismiss();
                            }
                        });
                builder.create().show();
            }
        });

        dialog.show();
    }

    public void claimLocation() {
        AsyncTAddingDataToServer asyncT = new AsyncTAddingDataToServer();
        asyncT.execute();
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
            String url = url_claim;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);

            try {
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("email", email);
                jsonobj.put("loc_id", id_Loc);
                jsonobj.put("loc_hashtag", "-");

                Log.e("mainToPost", "mainToPost" + jsonobj.toString());
                httppost.setEntity(new StringEntity(jsonobj.toString())); //json without header {"a"="a","b"=1}
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                InputStream inputStream = response.getEntity().getContent();
                InputStreamToStringExample str = new InputStreamToStringExample();
                responseServer = str.getStringFromInputStream(inputStream);
                Log.e("response", "response ----- " + responseServer.toString() + "|");
                Log.e("response", "response ----- " + responseServer.toString().equalsIgnoreCase("{\"success\":1}") + "|");
                Log.e("Success","7");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e("Success", "8");
            if (responseServer!=null && responseServer.equalsIgnoreCase("{\"success\":1}")) {
                Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
                responseServer="";
                Log.e("Success", "8a");
                getDataFromServer();
            } else {
                if (responseServer.equalsIgnoreCase("") || responseServer.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Ops, Error! Please Try Again!",Toast.LENGTH_SHORT).show();
                    Log.e("Success", "10");
                }
            }
        }
    }
}
