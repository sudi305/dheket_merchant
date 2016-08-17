package com.bgs.dheket.merchant;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bgs.dheket.accessingSensorPermission.HttpGetOrPost;
import com.bgs.dheket.sqlite.DBHelper;
import com.bgs.dheket.sqlite.ModelLocation;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by SND on 6/13/2016.
 */
public class AddNewOrUpdateLocationActivity_form_d extends AppCompatActivity implements View.OnClickListener{
    android.support.v7.app.ActionBar actionBar;

    String url = "",responseServer="",selectedHashtag="";
    String message = "";
    String location_name = "", location_address = "", phone = "", description = "", location_tag = "", user_email = "",location_cat_name="";
    long id_location, merchant_id, create_by;
    double latitude, longitude;
    int category_id, isPromo;
    ModelLocation modelLocation;

    DBHelper db;

    Bundle data;
    private JSONObject JsonObject, jsonobject;
    ArrayList<HashMap<String, String>> tagarraylist;
    private ArrayList<String> hashtags = new ArrayList<>();
    private String[] hashtagReady;
    String email,category_name,new_category_name,id_category,id_profile_tag,detail_tag,new_id_category, buttonName;
    ArrayList<String> selecthashtags = new ArrayList<>();
    ArrayList<String>categoryUser;

    LinearLayout ll_sh_search, ll_sh_result;
    EditText editText_search;
    TextView textView_SelectCategory;
    LayoutInflater mInflater;
    private TagFlowLayout tfl_search, tfl_result;
    private EditText editText;
    ImageButton imageButton_back;
    Button button_save;

    private TagAdapter<String> mAdapter ;

    ArrayAdapter<String> adapter;
    private List<String> filteredList = new ArrayList<>();
    private String[] newDataAfterRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location_form_d);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.d_ic_back));
        //actionBar.setHomeAsUpIndicator(R.drawable.logo);
        actionBar.setHomeButtonEnabled(true);
//        actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>Location in Radius " + formatter.format(radius) + " Km</font>"));
        dataLoad();

        if (id_location>0) {
            actionBar.setTitle("Update Data Location");
            buttonName = "Save and Update";
            message="Are you sure to save and update this location?";
        } else {
            actionBar.setTitle("New Location");
            buttonName = "Save and Add";
            message="Are you sure to save and create a new location with this information?";
        }
        actionBar.setSubtitle("Hashtag");
        category_name = location_cat_name;

        url = String.format(getResources().getString(R.string.link_getHashtagByCatId));

        editText_search = (EditText)findViewById(R.id.editText_search_select_hashtags);
        editText_search.addTextChangedListener(textWatcher);

        textView_SelectCategory = (TextView)findViewById(R.id.textView_sh_cat_user);
        textView_SelectCategory.setText(category_name);

        button_save = (Button)findViewById(R.id.imageButton_anl_prev_b);
        button_save.setOnClickListener(this);
        button_save.setText(buttonName);
        imageButton_back = (ImageButton)findViewById(R.id.imageButton_anl_prev_e);
        imageButton_back.setOnClickListener(this);
        /*textView_SelectCategory.set
        textView_SelectCategory.getResources().getDrawable(R.drawable.flow_drawable_icon).setState();*/

        ll_sh_result = (LinearLayout)findViewById(R.id.ll_sh_result_tag);
        ll_sh_search = (LinearLayout)findViewById(R.id.ll_sh_search);

        tfl_result = (TagFlowLayout)findViewById(R.id.tfl_sh_result_hashtag);
        tfl_search = (TagFlowLayout)findViewById(R.id.tfl_sh_search);

        mInflater = LayoutInflater.from(getApplicationContext());

        getDataCategory();
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
            if (tagarraylist.size()!=0) {
                filterItems(editText_search.getText().toString());
            }
        }
    };

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

    public void dataPrepareSave(boolean upload){
        location_tag = selectedHashtag;
        if (upload==true) {
            ModelLocation location = new ModelLocation(0,"","",0.0,0.0,0,"","",0,0,0,"","",user_email);
            long result = db.updateLocation(location);
            if (result==1 && id_location>0) {

            } else if (result==1 && id_location==0) {
                gotoMain();
            }
        }
        else {
            ModelLocation loc = new ModelLocation(id_location,location_name,location_address,latitude,longitude,category_id,location_cat_name,
                    phone,isPromo,merchant_id,create_by,description,location_tag,user_email);
            db.updateLocation(loc);
        }
        db.closeDB();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_select_category_or_tag, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            back_to_previous_screen();
            return super.onOptionsItemSelected(item);
        }

        /*if (item.getItemId() == R.id.select_done) {
            //save
            String hashtag = "";
            for (int i = 0; i < selecthashtags.size() ; i++) {
                if (i==0){
                    hashtag = selecthashtags.get(i).toString();
                } else {
                    hashtag = hashtag+", "+selecthashtags.get(i).toString();
                }
            }
            selectedHashtag = hashtag;
            AsyncTAddingDataToServer asyncTAddingDataToServer = new AsyncTAddingDataToServer();
            asyncTAddingDataToServer.execute();
            return super.onOptionsItemSelected(item);
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        back_to_previous_screen();
    }

    public void back_to_previous_screen(){
        gotoSelecCat();
    }

    public void getDataCategory() {
        CallWebPageTask task = new CallWebPageTask();
        task.applicationContext = getApplicationContext();
        String urls = url+"/"+category_id;
        Log.e("Sukses", urls);
        task.execute(new String[]{urls});
    }

    @Override
    public void onClick(View v) {
        if (v.equals(button_save)){
            String hashtag = "";
            for (int i = 0; i < selecthashtags.size() ; i++) {
                if (i==0){
                    hashtag = selecthashtags.get(i).toString();
                } else {
                    hashtag = hashtag+", "+selecthashtags.get(i).toString();
                }
            }
            selectedHashtag = hashtag;
            location_tag = selectedHashtag;

            final AlertDialog.Builder builder = new AlertDialog.Builder(AddNewOrUpdateLocationActivity_form_d.this);
            builder.setTitle("Confirmation");
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
        if (v.equals(imageButton_back)){
            String hashtag = "";
            for (int i = 0; i < selecthashtags.size() ; i++) {
                if (i==0){
                    hashtag = selecthashtags.get(i).toString();
                } else {
                    hashtag = hashtag+", "+selecthashtags.get(i).toString();
                }
            }
            selectedHashtag = hashtag;
            location_tag = selectedHashtag;
            dataPrepareSave(false);
            back_to_previous_screen();
        }
    }

    private class CallWebPageTask extends AsyncTask<String, Void, String> {

        private ProgressDialog dialog;
        protected Context applicationContext;

        @Override
        protected void onPreExecute() {
            //this.dialog = ProgressDialog.show(applicationContext, "Login Process", "Please Wait...", true);
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpGetOrPost httpGetOrPost = new HttpGetOrPost();
            response = httpGetOrPost.getRequest(urls[0]);
            tagarraylist = new ArrayList<HashMap<String, String>>();
            try {
                HashMap<String, String> map = new HashMap<String,String>();
                JSONArray menuItemArray = null;
                JsonObject = new JSONObject(response);
                menuItemArray = JsonObject.getJSONArray("dheket_hashtag");
                int index = 0;
                boolean notEqual = true;
                for (int i = 0; i < menuItemArray.length(); i++) {
                    map = new HashMap<String, String>();
                    jsonobject = menuItemArray.getJSONObject(i);

                    map.put("id_tag", jsonobject.getString("id"));
                    map.put("tag_name", jsonobject.getString("tag_name"));
                    /*for (int j = 0; j < categoryUser.size(); j++){
                        if (jsonobject.getString("tag_name").equalsIgnoreCase(categoryUser.get(j).toString())){
                            notEqual = false;
                            Log.e("sama",""+jsonobject.getString("tag_name").equalsIgnoreCase(categoryUser.get(j).toString()));
                        }
                    }
                    if (notEqual==true){

                        index++;
                    } else {
                        notEqual=true;
                    }*/
                    hashtags.add(index,jsonobject.getString("tag_name"));
                    // Set the JSON Objects into the array
                    tagarraylist.add(map);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            updateData();
        }
    }

    public void updateData(){
        /*for (int i = 0; i < tagarraylist.size(); i++) {
            Log.e("arraylist", "ke-" + i + " = " + tagarraylist.get(i));
            String email = tagarraylist.get(i).get("category_name").toString();
            //Uri imgUrl = Math.random() > .7d ? null : Uri.parse("https://robohash.org/" + Math.abs(email.hashCode()));
        }*/
        if (tagarraylist.size()!=0) {
            hashtagReady = new String[hashtags.size()];
            for (int i = 0; i < hashtagReady.length; i++) {
                hashtagReady[i] = hashtags.get(i).toString();
            }
            newDataAfterRemove = hashtagReady;
            Collections.addAll(filteredList, hashtagReady);
            initResultCat();
        }
    }

    public void initResultCat(){
        //mFlowLayout.setMaxSelectCount(3);
        tfl_result.setMaxSelectCount(1);
        tfl_result.setAdapter(mAdapter = new TagAdapter<String>(hashtags) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                TextView tv = (TextView) mInflater.inflate(R.layout.flow_tv,
                        tfl_result, false);
                tv.setText(s);
                return tv;
            }
        });

        tfl_result.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                //Toast.makeText(getApplicationContext(), tagarraylist.get(position).get("category_name"), Toast.LENGTH_SHORT).show();
                //view.setVisibility(View.GONE);
                new_id_category = tagarraylist.get(position).get("id");
                new_category_name = tagarraylist.get(position).get("tag_name");
                boolean removeItem = false;
                for (int j = 0; j < selecthashtags.size() ; j++) {
                    if (mAdapter.getItem(position).toString().equalsIgnoreCase(selecthashtags.get(j).toString())){
                        selecthashtags.remove(j);
                        removeItem = true;
                    }
                }

                if (removeItem==false){
                    selecthashtags.add(mAdapter.getItem(position).toString());
                    mAdapter.setSelectedList(position);
                } else {
                    mAdapter.unSetSelectedList(position);
                    removeItem = false;
                }

                for (int k = 0; k < selecthashtags.size() ; k++) {
                    Log.e("isi pilih hashtag - "+k,selecthashtags.get(k).toString());
                }
                Log.e("------","------");

                return true;
            }
        });


        tfl_result.setOnSelectListener(new TagFlowLayout.OnSelectListener() {
            @Override
            public void onSelected(Set<Integer> selectPosSet) {
                //setTitle("choose:" + selectPosSet.toString());
            }
        });

        for (int i = 0; i < hashtagReady.length ; i++) {
            if (hashtagReady[i].equalsIgnoreCase(category_name)){
                //mAdapter.setSingleSelected(i,hashtags[i]);
                //Log.e("selected",""+mAdapter.setSingleSelected(i,hashtags[i])+" | "+hashtags[i]);
                mAdapter.setSingleSelected(i);
            }
        }
    }

    public void filterItems(CharSequence text) {
        int countNotFound = 0;
        filteredList.clear();
        Log.e("masuk", "" + text);
        if (TextUtils.isEmpty(text)) {
            Collections.addAll(filteredList, newDataAfterRemove);
            ll_sh_result.setVisibility(View.VISIBLE);
            ll_sh_search.setVisibility(View.GONE);
            changeViewSearch();
            Log.e("kosong","iya");
        } else {
            ll_sh_result.setVisibility(View.GONE);
            ll_sh_search.setVisibility(View.VISIBLE);
            for (String s : newDataAfterRemove) {
                if (s.toLowerCase().contains(text.toString().toLowerCase())) {
                    filteredList.add(s);
                    //Log.e("cari dan ketemu", ""+filteredList.add(s));
                    changeViewSearch();
                }
                else {
                    countNotFound++;
                }
                //Log.e("cari", ""+filteredList.add(s));
            }
            if (countNotFound==newDataAfterRemove.length){
                filteredList.clear();
                changeViewSearch();
            }
        }
        //notifyDataSetChanged();
    }

    public void changeViewSearch(){
        final int[] searchItem = {0};
        final String[] cari = {""};
        tfl_search.setMaxSelectCount(1);
        tfl_search.setAdapter(new TagAdapter<String>(filteredList)
        {

            @Override
            public View getView(FlowLayout parent, int position, String s)
            {
                TextView tv = (TextView) mInflater.inflate(R.layout.flow_tv,
                        tfl_search, false);
                tv.setText(s);
                return tv;
            }
        });

        tfl_search.setOnTagClickListener(new TagFlowLayout.OnTagClickListener()
        {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent)
            {
                //Toast.makeText(getApplicationContext(), filteredList.get(position), Toast.LENGTH_SHORT).show();
                //view.setVisibility(View.GONE);
                //Log.e("yang terpilih", "" + searchItem[0] + "|" + cari[0]);
                boolean removeItem = false;
                for (int i = 0; i < hashtagReady.length; i++) {
                    if (hashtagReady[i].equalsIgnoreCase(filteredList.get(position))){
                        //mAdapter.setSingleSelected(i,hashtags[i]);
                        //Log.e("selected",""+mAdapter.setSingleSelected(i,hashtags[i])+" | "+hashtags[i]);

                        for (int j = 0; j < selecthashtags.size() ; j++) {
                            if (mAdapter.getItem(i).toString().equalsIgnoreCase(selecthashtags.get(j).toString())){
                                selecthashtags.remove(j);
                                removeItem = true;
                            }
                        }
                        if (removeItem==false){
                            selecthashtags.add(mAdapter.getItem(i).toString());
                            mAdapter.setSelectedList(i);
                        } else {
                            mAdapter.unSetSelectedList(i);
                            removeItem = false;
                        }

                        for (int k = 0; k < selecthashtags.size() ; k++) {
                            Log.e("isi pilih hashtag - "+k,selecthashtags.get(k).toString());
                        }
                        Log.e("------","------");

                        ll_sh_search.setVisibility(View.GONE);
                        ll_sh_result.setVisibility(View.VISIBLE);/*
                        for (int j = 0; j < tagarraylist.size() ; j++) {
                            if (mAdapter.getItem(i).toString().equalsIgnoreCase(tagarraylist.get(j).get("category_name"))){
                                new_id_category = tagarraylist.get(i).get("id");
                                new_category_name = tagarraylist.get(position).get("tag_name");
                            }
                        }*/
                        editText_search.setText("");
                    }
                }
                mAdapter.notifyDataChanged();
                return true;
            }
        });


        tfl_search.setOnSelectListener(new TagFlowLayout.OnSelectListener()
        {
            @Override
            public void onSelected(Set<Integer> selectPosSet)
            {
                //searchItem = Integer.parseInt(selectPosSet.toString().replace("[","").replace("]",""));
                //setTitle("choose:" + selectPosSet.toString());
            }
        });
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
            String url = String.format(getResources().getString(R.string.link_addOrUpdateLocation));
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            Log.e("link", url + " | " + id_category + " | " + new_id_category + " | " + email);
            try {
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("loc_name", location_name);
                jsonobj.put("loc_address", location_address);
                jsonobj.put("loc_lat", latitude);
                jsonobj.put("loc_lng", longitude);
                jsonobj.put("loc_cat_id", category_id);
                jsonobj.put("loc_phone", phone);
                jsonobj.put("loc_promo", isPromo);
                jsonobj.put("loc_email_merchant", user_email);
                jsonobj.put("loc_description", description);
                jsonobj.put("loc_tag", location_tag);
                jsonobj.put("loc_id", id_location);

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
                dataPrepareSave(true);
            } else {
                if (responseServer.equalsIgnoreCase("") || responseServer.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Ops, Error! Please Try Again!",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void gotoMain(){
        Intent tonext = new Intent(getApplicationContext(), MainMenuActivity.class);
        startActivity(tonext);
        finish();
    }

    public void gotoSelecCat(){
        Intent intent = new Intent(getApplicationContext(), AddNewOrUpdateLocationActivity_form_c.class);
        startActivity(intent);
        finish();
    }
}
