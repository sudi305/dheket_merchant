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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bgs.common.Constants;
import com.bgs.dheket.App;
import com.bgs.dheket.accessingSensorPermission.HttpGetOrPost;
import com.bgs.dheket.sqlite.DBHelper;
import com.bgs.dheket.sqlite.ModelLocation;
import com.bgs.dheket.sqlite.ModelMerchant;
import com.bgs.dheket.viewmodel.UserApp;
import com.bgs.domain.chat.model.UserType;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

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

/**
 * Created by SND on 18/01/2016.
 */

public class FormLoginActivity extends AppCompatActivity implements LocationListener {
    CallbackManager callbackManager;
    LoginButton login;
    Button signup;
    TextView loading, login_fb, check_email;
    //android.support.v7.app.ActionBar actionBar;

    String url = "";
    String urlCreateAccount = "";
    String temp_email = "",email = "",username="",password="",facebook_id="",responseServer="",
            gender = "", facebook_photo = "", merchant_name;
    double latitude=0, longitude=0;
    long id_merchant = 0;

    LocationManager myLocationManager;
    Criteria criteria;
    String provider;
    Location location;

    private JSONObject jObject;
    JSONArray menuItemArray = null;

    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_form_login);

        /*actionBar = getSupportActionBar();

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeAsUpIndicator(R.drawable.logo);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Login to Dheket");*/

        getServiceFromGPS();

        callbackManager = CallbackManager.Factory.create();
        login = (LoginButton)findViewById(R.id.login_button);
        login.setReadPermissions("public_profile email");
        signup = (Button)findViewById(R.id.signup_button);
        loading = (TextView)findViewById(R.id.textView_formLogin_loading);
        login_fb = (TextView)findViewById(R.id.textView_login_fb);
        check_email = (TextView)findViewById(R.id.textView_checkemail);

        url = String.format(getResources().getString(R.string.link_cekUserLogin));
        urlCreateAccount = String.format(getResources().getString(R.string.link_addUserMerchantByEmail));

        db = new DBHelper(getApplicationContext());
//        if(AccessToken.getCurrentAccessToken() != null){
//            RequestData();
//            login.setVisibility(View.INVISIBLE);
//        } else {
//            login.setVisibility(View.VISIBLE);
//        }
        login_fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.performClick();
            }
        });
        login.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                loading.setVisibility(View.VISIBLE);
                login.setVisibility(View.GONE);
                signup.setVisibility(View.GONE);
                Log.e("Success", "1");
                RequestDataFromFB();
                Log.e("Success", "1a");
                login_fb.setVisibility(View.GONE);
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Login Canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Sign Up With Email", Toast.LENGTH_SHORT).show();
            }
        });

        check_email.addTextChangedListener(textWatcher);

    }

    final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.e("belum","belum");
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.e("belum","saat");
            /*if (check_email.getText().equals(temp_email) && !temp_email.isEmpty()){
                //checkExistingUser(check_email.getText().toString(),latitude,longitude);
                Log.e("belum","ok");
                Toast.makeText(getApplicationContext(),check_email.getText().toString(),Toast.LENGTH_SHORT).show();
            }*/
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.e("belum","sudah");
            checkExistingUser(check_email.getText().toString(), latitude, longitude);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (item.getItemId() == android.R.id.home) {
            finish();
            return super.onOptionsItemSelected(item);
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            //logout_user();
            Toast.makeText(getApplicationContext(),"About this app",Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void RequestDataFromFB() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                JSONObject json = response.getJSONObject();
                try {
                    if (json != null) {
                        Log.e("json", "" + json.toString());
                        String text = "<b>Name :</b> " + json.getString("name") + "<br><br><b>Email :</b> " + json.getString("email") + "<br><br><b>Profile link :</b> " + json.getString("link");
                        email = json.getString("email");
                        temp_email = email;
                        merchant_name = json.getString("name");
                        username = ""+merchant_name.replace(" ","");
                        username = ""+username.replace("'","");
                        password = "123456";
                        facebook_id = json.getString("id");
                        gender = json.getString("gender");
                        if (json.has("picture")) {
                            facebook_photo = json.getJSONObject("picture").getJSONObject("data").getString("url");
                            // set profile image to imageview using Picasso or Native methods
                        }
                        if (email.equalsIgnoreCase("")||email.isEmpty()){
                            temp_email = "user"+facebook_id+"@dheket.co.id";
                        }
                        check_email.setText(temp_email);

                        UserApp userApp = App.getUserApp();
                        if (userApp == null) userApp = new UserApp();
                        userApp.setName(username);
                        userApp.setEmail(email);
                        userApp.setId(facebook_id);
                        userApp.setPicture(facebook_photo);
                        userApp.setType(Constants.USER_TYPE);
                        App.updateUserApp(userApp);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email,gender,picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();

    }

    public void checkExistingUser(String email, double latitude, double longitude) {
        CallWebPageTaskCheckEmail task = new CallWebPageTaskCheckEmail();
        task.applicationContext = getApplicationContext();
        String urls = url + "/" + email;
        Log.e("Sukses", urls);
        task.execute(new String[]{urls});
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
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class CallWebPageTaskCheckEmail extends AsyncTask<String, Void, String> {

        private ProgressDialog dialog;
        protected Context applicationContext;
        String response = "";
        @Override
        protected void onPreExecute() {
            //this.dialog = ProgressDialog.show(getApplicationContext(), "Login Process", "Please Wait...", true);
        }

        @Override
        protected String doInBackground(String... urls) {
            HttpGetOrPost httpGetOrPost = new HttpGetOrPost();
            response = httpGetOrPost.getRequest(urls[0]);
            Log.e("Success","4"+response);
            try {
                //simpan data dari web ke dalam array
                JSONArray menuItemArrays = null;
                jObject = new JSONObject(response);
                menuItemArrays = jObject.getJSONArray("result_user");
                if (menuItemArrays.length()!=0) {
                    for (int i = 0; i < menuItemArrays.length(); i++) {
                        id_merchant = menuItemArrays.getJSONObject(i).getInt("id_merchant");
                        merchant_name = menuItemArrays.getJSONObject(i).getString("merchant_name").toString();
                        email = menuItemArrays.getJSONObject(i).getString("email").toString();
                        facebook_photo = menuItemArrays.getJSONObject(i).getString("facebook_photo").toString();
                    }
                }else{
                    email = "kosong";
                }
                Log.e("Success","4 c"+menuItemArrays.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            //this.dialog.cancel();
            if (email.equalsIgnoreCase("kosong")){
                Log.e("Success", "5");
                createUserAccountCustomer();
                Log.e("Success", "5a");
            } else {
                toDb(email);
                if (AccessToken.getCurrentAccessToken() != null) {
                    Intent loginWithFb = new Intent(FormLoginActivity.this, MainMenuActivity.class);
                    Log.e("Success", "6");
                    startActivity(loginWithFb);
                    finish();
                    /*LoginManager.getInstance().logOut();
                    Intent logout_user_fb = new Intent(getApplicationContext(), FormLoginActivity.class);
                    startActivity(logout_user_fb);
                    finish();*/
                }
            }
        }
    }

    public void createUserAccountCustomer() {
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
            String url = urlCreateAccount;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);

            try {
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("email", temp_email);
                jsonobj.put("username", username.toLowerCase());
                jsonobj.put("password", password);
                jsonobj.put("facebook_id", facebook_id);
                jsonobj.put("facebook_photo", facebook_photo);
                jsonobj.put("gender",gender);
                jsonobj.put("merchant_name",merchant_name);
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
                Toast.makeText(getApplicationContext(),"Success!",Toast.LENGTH_SHORT).show();
                responseServer="";
                Log.e("Success", "8a");
                toDb(temp_email);
                if (AccessToken.getCurrentAccessToken() != null) {
                    Intent loginWithFb = new Intent(FormLoginActivity.this, MainMenuActivity.class);
                    Log.e("Success", "8b");
                    startActivity(loginWithFb);
                    finish();
                }
                Log.e("Success","9");
            } else {
                if (responseServer.equalsIgnoreCase("") || responseServer.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Ops, Error! Please Try Again!",Toast.LENGTH_SHORT).show();
                    Log.e("Success", "10");
                }
            }
        }
    }

    public void toDb(String email){
        ModelMerchant merchants = new ModelMerchant(id_merchant,merchant_name,email,facebook_photo,null,1);
        ModelLocation location = new ModelLocation(0,"","",0.0,0.0,0,"","",0,0,"","",email);
        Log.e("db name ",db.getDatabaseName().toString());
        Log.e("tabel ",db.getAllMerchant().toString());
        if (db.findMerchantByEmail(email) == false){
            Log.e("db","buat baru");
            db.createMerchant(merchants);
            Log.e("data",""+db.getMerchantByEmail(email).getId());
            db.createLocation(location);
        } else {
            Log.e("db","update");
            db.updateMerchant(merchants);
            db.updateLocation(location);
        }
        db.closeDB();
    }
}
