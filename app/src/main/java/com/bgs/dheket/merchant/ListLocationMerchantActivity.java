package com.bgs.dheket.merchant;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bgs.dheket.accessingSensorPermission.HttpGetOrPost;
import com.bgs.dheket.general.Utility;
import com.bgs.dheket.sqlite.DBHelper;
import com.bgs.dheket.sqlite.ModelLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SND on 6/13/2016.
 */
public class ListLocationMerchantActivity extends AppCompatActivity implements LocationListener, View.OnClickListener{
    android.support.v7.app.ActionBar actionBar;
    int[] id_cat;
    String[] icon_cat;
    Menu menu;

    Utility formatNumber = new Utility();

    private JSONObject jObject;
    private String jsonResult = "";

    double latitude, longitude;
    String urls = "";
    String parameters, email, icon,category;
    int cat_id;

    LinearLayout linearLayout_contentlist;
    TextView textView_result;
    Button btn_claim, btn_add;

    ArrayList<HashMap<String, String>> arraylist;
    /*String[] loc_name, loc_address, loc_pic;
    int[] loc_promo, id_loc;
    double[] loc_distance, loc_lat, loc_lng;*/

    boolean isFirst = true;
    CallWebPageTask task;
    Bundle paket;

    LocationManager myLocationManager;
    Criteria criteria;
    String provider;
    Location location;

    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_location_merchant);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.d_ic_back));
        //actionBar.setHomeAsUpIndicator(R.drawable.logo);
        actionBar.setHomeButtonEnabled(true);

        paket = getIntent().getExtras();
        latitude = paket.getDouble("latitude");
        longitude = paket.getDouble("longitude");

        actionBar.setTitle("My Locations");
//        actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>Location in Radius " + formatter.format(radius) + " Km</font>"));

        urls = String.format(getResources().getString(R.string.link_getMerchantLocation));//<!--getlocationmerchantbyid/{center_lat}/{center_lng}/{email}-->

        textView_result = (TextView) findViewById(R.id.textView_llm_noLoc);
        textView_result.setVisibility(View.GONE);
        btn_claim = (Button)findViewById(R.id.button_llm_claimloc);
        btn_claim.setOnClickListener(this);
        btn_add = (Button)findViewById(R.id.button_llm_addloc);
        btn_add.setOnClickListener(this);

        db = new DBHelper(getApplicationContext());
        email = db.getMerchantTopId();
        db.closeDB();

        //mAdd = new PictureMarkerSymbol(rootView.getContext().getApplicationContext(), ContextCompat.getDrawable(rootView.getContext().getApplicationContext(), R.drawable.pin_add));
        linearLayout_contentlist = (LinearLayout)findViewById(R.id.linearLayout_result_lm);
        getServiceFromGPS();
    }

    public void getDataFromServer() {
        task = new CallWebPageTask();
        task.applicationContext = getApplicationContext();
        /*getlocationbycategoryid/{rad}/{center_lat}/{center_lng}/{cat}*/
        parameters = urls + latitude + "/" + longitude + "/" +email;
        Log.e("OK Connecting Sukses", "" + parameters);
        //Log.e("Sukses", parameters);
        task.execute(new String[]{parameters});
    }

    /**
     * When the map is tapped, select the graphic at that location.
     */

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

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        getDataFromServer();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        String message = "GPS enabled";
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        String message = "GPS disabled";
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void setGPSstopped() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myLocationManager.removeUpdates(this);
    }

    public void getServiceFromGPS() {
        myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        provider = myLocationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = myLocationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
        }
        myLocationManager.requestLocationUpdates(provider, 20000, 0, this);
    }

    @Override
    public void onClick(View v) {
        Intent gotoNextScreen = null;
        Bundle dataPaket = new Bundle();

        if (v.equals(btn_add)){
            gotoNextScreen = new Intent(getApplicationContext(),AddNewOrUpdateLocationActivity.class);
            dataPaket.putString("email", email);
            gotoNextScreen.putExtras(dataPaket);
        }
        if (v.equals(btn_claim)){
            gotoNextScreen = new Intent(getApplicationContext(),SearchLocationByNameActivity.class);
            //dataPaket.putString("email",textView_emailUser.getText().toString());
            dataPaket.putDouble("latitude", latitude);
            dataPaket.putDouble("longitude", longitude);
            gotoNextScreen.putExtras(dataPaket);
        }
        startActivity(gotoNextScreen);
        finish();
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
            String response = "";
            HttpGetOrPost httpGetOrPost = new HttpGetOrPost();
            response = httpGetOrPost.getRequest(url[0]);
            try {
                //simpan data dari web ke dalam array
                JSONArray menuItemArray = null;
                jObject = new JSONObject(response);
                menuItemArray = jObject.getJSONArray("dheket_merchantLoc");
                arraylist = new ArrayList<HashMap<String, String>>();
                /*Log.e("Data dari server", "" + menuItemArray.length());
                for (int i = 0; i < menuItemArray.length(); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("id_loc",menuItemArray.getJSONObject(i).getString("id_location"));
                    map.put("loc_name",menuItemArray.getJSONObject(i).getString("location_name"));
                    map.put("loc_address",menuItemArray.getJSONObject(i).getString("location_address"));
                    map.put("loc_lat",menuItemArray.getJSONObject(i).getString("latitude"));
                    map.put("loc_lng",menuItemArray.getJSONObject(i).getString("longitude"));
                    map.put("cat_id",menuItemArray.getJSONObject(i).getString("category_id"));
                    map.put("loc_distance",""+Double.parseDouble(formatNumber.changeFormatNumber(menuItemArray.getJSONObject(i).getDouble("distance"))));
                    arraylist.add(map);*/
                Log.e("Data dari server", "" + menuItemArray.length());
                for (int i = 0; i < menuItemArray.length(); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("id_loc",menuItemArray.getJSONObject(i).getString("id_location"));
                    map.put("loc_name",menuItemArray.getJSONObject(i).getString("location_name"));
                    map.put("loc_address",menuItemArray.getJSONObject(i).getString("location_address"));
                    map.put("loc_lat",menuItemArray.getJSONObject(i).getString("latitude"));
                    map.put("loc_lng",menuItemArray.getJSONObject(i).getString("longitude"));
                    map.put("cat_id",menuItemArray.getJSONObject(i).getString("category_id"));
                    map.put("loc_distance",""+Double.parseDouble(formatNumber.changeFormatNumber(menuItemArray.getJSONObject(i).getDouble("distance"))));
                    map.put("category_name",menuItemArray.getJSONObject(i).getString("category_name"));
                    map.put("phone",menuItemArray.getJSONObject(i).getString("phone"));
                    map.put("isPromo",menuItemArray.getJSONObject(i).getString("isPromo"));
                    map.put("merchant_id",menuItemArray.getJSONObject(i).getString("merchant_id"));
                    map.put("created_by",menuItemArray.getJSONObject(i).getString("created_by"));
                    map.put("description",menuItemArray.getJSONObject(i).getString("description"));
                    map.put("location_tag",menuItemArray.getJSONObject(i).getString("location_tag"));
                    arraylist.add(map);
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
        if (arraylist != null) {
            linearLayout_contentlist.removeAllViews();
            textView_result.setVisibility(View.GONE);
            for (int i = 0; i < arraylist.size() ; i++) {
                LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                final View ll = inflater.inflate(R.layout.item_listmod, null);
                final TextView id = (TextView)ll.findViewById(R.id.textView_il_id);
                id.setText(arraylist.get(i).get("id_loc").toString());
                final TextView nama = (TextView)ll.findViewById(R.id.textView_il_nama);
                nama.setText(arraylist.get(i).get("loc_name").toString());
                TextView alamat = (TextView)ll.findViewById(R.id.textView_il_alamat);
                alamat.setText(arraylist.get(i).get("loc_address").toString());
                TextView jarak = (TextView)ll.findViewById(R.id.textView_il_jarak);
                jarak.setText(arraylist.get(i).get("loc_distance").toString()+" Km");
                ImageView foto = (ImageView)ll.findViewById(R.id.imageView_il_foto);
                linearLayout_contentlist.addView(ll);
                final int index = i;

                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(ll.getContext().getApplicationContext(),nama.getText().toString(),Toast.LENGTH_SHORT).show();
                        toSaveDB(index);
                        setGPSstopped();
                        Intent goToScreen = new Intent(getApplicationContext(), DetailLocationMerchantActivity.class);
                        startActivity(goToScreen);
                        finish();
                    }
                });
            }
        } else {
            textView_result.setVisibility(View.VISIBLE);
        }
    }

    public void toSaveDB(int i){

        ModelLocation modelLocation = new ModelLocation(
                Long.parseLong(arraylist.get(i).get("id_loc").toString()),
                arraylist.get(i).get("loc_name").toString(),
                arraylist.get(i).get("loc_address").toString(),
                Double.parseDouble(arraylist.get(i).get("loc_lat").toString()),
                Double.parseDouble(arraylist.get(i).get("loc_lng").toString()),
                Integer.parseInt(arraylist.get(i).get("cat_id").toString()),
                arraylist.get(i).get("category_name").toString(),
                arraylist.get(i).get("phone").toString(),
                Integer.parseInt(arraylist.get(i).get("isPromo").toString()),
                Long.parseLong(arraylist.get(i).get("merchant_id").toString()),
                Long.parseLong(arraylist.get(i).get("created_by").toString()),
                arraylist.get(i).get("description").toString(),
                arraylist.get(i).get("location_tag").toString(),
                email
        );

        db.updateLocation(modelLocation);
        db.closeDB();
    }
}
