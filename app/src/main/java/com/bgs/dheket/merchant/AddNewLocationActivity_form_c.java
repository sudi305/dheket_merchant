package com.bgs.dheket.merchant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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
public class AddNewLocationActivity_form_c extends AppCompatActivity implements View.OnClickListener{
    android.support.v7.app.ActionBar actionBar;

    String location_name = "", location_address = "", phone = "", description = "", location_tag = "", user_email = "", cat_name="";
    long id_location, merchant_id;
    double latitude, longitude;
    int category_id, isPromo, cat_id;
    ModelLocation modelLocation;

    DBHelper db;

    String url = "",responseServer="";
    private JSONObject JsonObject, jsonobject;
    ArrayList<HashMap<String, String>> catarraylist;
    private ArrayList<String> categorys = new ArrayList<>();
    private String[] categoryReady;
    String email,category_name,new_category_name,id_category,id_profile_tag,detail_tag,new_id_category;
    String selectCategorys;
    ArrayList<String>categoryUser;
    Bundle paket;

    LinearLayout ll_sc_search, ll_sc_result;
    EditText editText_search;
    LayoutInflater mInflater;
    private TagFlowLayout tfl_search, tfl_result;
    private EditText editText;
    ImageButton imageButton_next, imageButton_prev;
    TextView textView_cat_id;

    private TagAdapter<String> mAdapter ;

    ArrayAdapter<String> adapter;
    private List<String> filteredList = new ArrayList<>();
    private String[] newDataAfterRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location_form_c);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.d_ic_back));
        //actionBar.setHomeAsUpIndicator(R.drawable.logo);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("New Location");
        actionBar.setSubtitle("Category");
//        actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>Location in Radius " + formatter.format(radius) + " Km</font>"));
        dataLoad();
        cat_id = category_id;

        url = String.format(getResources().getString(R.string.link_getAllCategory));

        editText_search = (EditText)findViewById(R.id.editText_search_select_category);
        editText_search.addTextChangedListener(textWatcher);
        textView_cat_id = (TextView)findViewById(R.id.textView_anl_cat_id);
        textView_cat_id.setText("" + cat_id);

        ll_sc_result = (LinearLayout)findViewById(R.id.ll_sc_result_cat);
        ll_sc_search = (LinearLayout)findViewById(R.id.ll_sc_search);

        tfl_result = (TagFlowLayout)findViewById(R.id.tfl_sc_result_cat);
        tfl_search = (TagFlowLayout)findViewById(R.id.tfl_sc_search);

        imageButton_next = (ImageButton)findViewById(R.id.imageButton_anl_next_b);
        imageButton_next.setOnClickListener(this);
        imageButton_prev = (ImageButton)findViewById(R.id.imageButton_anl_prev_b);
        imageButton_prev.setOnClickListener(this);

        textView_cat_id.addTextChangedListener(dataValidation);
        buttonEnable();

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
            if (catarraylist.size()!=0) {
                filterItems(editText_search.getText().toString());
            }
        }
    };

    public TextWatcher dataValidation = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            buttonEnable();
        }
    };

    public void buttonEnable(){
        if (cat_id>0){
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
        phone = modelLocation.getPhone();
        description = modelLocation.getDescription();
        location_tag = modelLocation.getLocation_tag();
        id_location = modelLocation.getId_location();
        merchant_id = modelLocation.getMerchant_id();
        latitude = modelLocation.getLatitude();
        longitude = modelLocation.getLongitude();
        category_id = modelLocation.getCategory_id();
        isPromo = modelLocation.getIsPromo();
    }

    public void dataPrepareSave(){
        category_id = cat_id;
        ModelLocation loc = new ModelLocation(0,location_name,location_address,latitude,longitude,category_id,
                phone,isPromo,merchant_id,description,location_tag,user_email);
        db.updateLocation(loc);
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
            new_id_category = id_category;
            gotoSelectHashtag();
            return super.onOptionsItemSelected(item);
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        back_to_previous_screen();
    }

    public void back_to_previous_screen(){
        Intent intent = new Intent(getApplicationContext(),AddNewLocationActivity_form_b.class);
        startActivity(intent);
        finish();
    }

    public void getDataCategory() {
        CallWebPageTask task = new CallWebPageTask();
        task.applicationContext = getApplicationContext();
        String urls = url;
        Log.e("Sukses", urls);
        task.execute(new String[]{urls});
    }

    @Override
    public void onClick(View v) {
        if (v.equals(imageButton_next)){
            dataPrepareSave();
            gotoSelectHashtag();
        }
        if (v.equals(imageButton_prev)){
            dataPrepareSave();
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
            catarraylist = new ArrayList<HashMap<String, String>>();
            try {
                HashMap<String, String> map = new HashMap<String,String>();
                JSONArray menuItemArray = null;
                JsonObject = new JSONObject(response);
                menuItemArray = JsonObject.getJSONArray("dheket_allCat");
                int index = 0;
                boolean notEqual = true;
                for (int i = 0; i < menuItemArray.length(); i++) {
                    map = new HashMap<String, String>();
                    jsonobject = menuItemArray.getJSONObject(i);

                    map.put("id_category", jsonobject.getString("id_category"));
                    map.put("category_name", jsonobject.getString("category_name"));
                    map.put("icon", jsonobject.getString("icon"));
                    categorys.add(index,jsonobject.getString("category_name"));
                    // Set the JSON Objects into the array
                    catarraylist.add(map);
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
        /*for (int i = 0; i < catarraylist.size(); i++) {
            Log.e("arraylist", "ke-" + i + " = " + catarraylist.get(i));
            String email = catarraylist.get(i).get("category_name").toString();
            //Uri imgUrl = Math.random() > .7d ? null : Uri.parse("https://robohash.org/" + Math.abs(email.hashCode()));
        }*/
        categoryReady = new String[categorys.size()];
        for (int i = 0; i < categoryReady.length; i++) {
            categoryReady[i]=categorys.get(i).toString();
        }
        newDataAfterRemove = categoryReady;
        Collections.addAll(filteredList, categoryReady);
        initResultCat();
    }

    public void initResultCat(){
        //mFlowLayout.setMaxSelectCount(3);
        tfl_result.setMaxSelectCount(1);
        tfl_result.setAdapter(mAdapter = new TagAdapter<String>(categorys) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                TextView tv = (TextView) mInflater.inflate(R.layout.flow_tv_without_icon,
                        tfl_result, false);
                tv.setText(s);
                return tv;
            }
        });

        tfl_result.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                /*Toast.makeText(getApplicationContext(), catarraylist.get(position).get("category_name") +" - "+
                        categorys.get(position), Toast.LENGTH_SHORT).show();*/
                //view.setVisibility(View.GONE);
                category_name = catarraylist.get(position).get("category_name");
                for (int i = 0; i < catarraylist.size(); i++) {
                    if (categorys.get(position).equalsIgnoreCase(catarraylist.get(i).get("category_name"))) {
                        id_category = catarraylist.get(i).get("id_category");
                        category_name = catarraylist.get(i).get("category_name");
                        cat_id = Integer.parseInt(id_category);
                        cat_name = category_name;
                        new_id_category = id_category;
                        new_category_name = category_name;
                        textView_cat_id.setText("" + cat_id);
                    }
                }
                if (tfl_result.getSelectedList().size() != 0) {
                    cat_id = Integer.parseInt(id_category);
                    textView_cat_id.setText("" + cat_id);
                    //Toast.makeText(getApplicationContext(),"baru di klik",Toast.LENGTH_SHORT).show();
                } else {
                    cat_id = 0;
                    textView_cat_id.setText("" + cat_id);
                    //Toast.makeText(getApplicationContext(),"di klik lagi",Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        Log.e("jumlah data", "categorys " + categorys.size() + " | catarraylist " + catarraylist.size() + " | categoryready " + categoryReady.length);

        tfl_result.setOnSelectListener(new TagFlowLayout.OnSelectListener() {
            @Override
            public void onSelected(Set<Integer> selectPosSet) {
                //setTitle("choose:" + selectPosSet.toString());
            }
        });


        for (int i = 0; i < catarraylist.size() ; i++) {
            if ((""+cat_id).equalsIgnoreCase(catarraylist.get(i).get("id_category"))){
                id_category = catarraylist.get(i).get("id_category");
                category_name = catarraylist.get(i).get("category_name");
                cat_id = Integer.parseInt(id_category);
                cat_name = category_name;
                new_id_category = id_category;
                new_category_name = category_name;
                textView_cat_id.setText("" + cat_id);

                for (int j = 0; j < categorys.size(); j++) {
                    if (catarraylist.get(i).get("category_name").equalsIgnoreCase(categorys.get(j))){
                        mAdapter.setSingleSelected(j);
                    }
                }
                //mAdapter -> categorys
                //data yang sesuai adalah data catarraylist
            }
        }
    }

    public void filterItems(CharSequence text) {
        int countNotFound = 0;
        filteredList.clear();
        Log.e("masuk", "" + text);
        if (TextUtils.isEmpty(text)) {
            Collections.addAll(filteredList, newDataAfterRemove);
            ll_sc_result.setVisibility(View.VISIBLE);
            ll_sc_search.setVisibility(View.GONE);
            changeViewSearch();
            Log.e("kosong","iya");
        } else {
            ll_sc_result.setVisibility(View.GONE);
            ll_sc_search.setVisibility(View.VISIBLE);
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
                TextView tv = (TextView) mInflater.inflate(R.layout.flow_tv_without_icon,
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
                Toast.makeText(getApplicationContext(), filteredList.get(position), Toast.LENGTH_SHORT).show();
                //view.setVisibility(View.GONE);
                //Log.e("yang terpilih", "" + searchItem[0] + "|" + cari[0]);
                for (int i = 0; i < categoryReady.length; i++) {
                    if (categoryReady[i].equalsIgnoreCase(filteredList.get(position))){
                        //mAdapter.setSingleSelected(i,categorys[i]);
                        //Log.e("selected",""+mAdapter.setSingleSelected(i,categorys[i])+" | "+categorys[i]);
                        mAdapter.setSingleSelected(i);
                        ll_sc_search.setVisibility(View.GONE);
                        ll_sc_result.setVisibility(View.VISIBLE);
                        for (int j = 0; j < catarraylist.size() ; j++) {
                            if (mAdapter.getItem(i).toString().equalsIgnoreCase(catarraylist.get(j).get("category_name"))){

                                Log.e("data asli", "catarraylis " + catarraylist.get(j).get("category_name"));

                                id_category = catarraylist.get(j).get("id_category");
                                category_name = catarraylist.get(j).get("category_name");
                                cat_id = Integer.parseInt(id_category);
                                cat_name = category_name;
                                new_id_category = id_category;
                                new_category_name = category_name;
                                textView_cat_id.setText("" + cat_id);
                            }
                        }
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

    public void gotoSelectHashtag(){
        Intent tonext = new Intent(getApplicationContext(), AddNewLocationActivity_form_d.class);
        Bundle paket = new Bundle();
        paket.putString("category_name", new_category_name);
        tonext.putExtras(paket);
        startActivity(tonext);
        finish();
    }
}
