package com.bgs.dheket.merchant;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bgs.chat.ChatHistoryActivity;
import com.bgs.chat.services.ChatClientService;
import com.bgs.chat.widgets.CircleBackgroundSpan;
import com.bgs.common.Constants;
import com.bgs.dheket.App;
import com.bgs.dheket.sqlite.DBHelper;
import com.bgs.dheket.sqlite.ModelMerchant;
import com.bgs.dheket.viewmodel.UserApp;
import com.bgs.domain.chat.model.ChatContact;
import com.bgs.domain.chat.model.ChatMessage;
import com.bgs.domain.chat.model.UserType;
import com.bgs.domain.chat.repository.ContactRepository;
import com.bgs.domain.chat.repository.IContactRepository;
import com.bgs.domain.chat.repository.IMessageRepository;
import com.bgs.domain.chat.repository.MessageRepository;
import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainMenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
        LocationListener {
    android.support.v7.app.ActionBar actionBar;

    ImageButton imageButton_addLoc, imageButton_myLoc, imageButton_searchLoc,
                imageButton_settingUser, imageButton_promotionLoc, imageButton_chatLoc;
    ImageView imageView_userPhoto, imVi_nav_usrPro;
    TextView textView_nameUser, textView_emailUser, txt_nav_name, txt_nav_email;

    DBHelper db;

    String name = "", url_photoFb = "", email = "", dataEmailFromDb;
    double latitude = 0, longitude = 0;

    LocationManager myLocationManager;
    Criteria criteria;
    String provider;
    Location location;

    Picasso picasso;

    //CHATS
    private IMessageRepository messageRepository;
    private IContactRepository contactRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageRepository = new MessageRepository(getApplicationContext());
        contactRepository = new ContactRepository(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.d_ic_back));
        //actionBar.setHomeAsUpIndicator(R.drawable.logo);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00FFFFFF")));
        actionBar.setTitle("Dheket");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        View header=navigationView.getHeaderView(0);
        txt_nav_name = (TextView)header.findViewById(R.id.nav_hm_textview_name);
        txt_nav_email = (TextView)header.findViewById(R.id.nav_hm_textView_email);
        imVi_nav_usrPro = (ImageView)header.findViewById(R.id.nav_hm_imageView);

        getServiceFromGPS();

        imageButton_addLoc = (ImageButton)findViewById(R.id.imageButton_main_addLoc);
        imageButton_addLoc.setOnClickListener(this);
        imageButton_myLoc = (ImageButton)findViewById(R.id.imageButton_main_myLoc);
        imageButton_myLoc.setOnClickListener(this);
        imageButton_searchLoc = (ImageButton)findViewById(R.id.imageButton_main_searchLoc);
        imageButton_searchLoc.setOnClickListener(this);
        imageButton_settingUser = (ImageButton)findViewById(R.id.imageButton_main_settingsUser);
        imageButton_settingUser.setOnClickListener(this);
        imageButton_promotionLoc = (ImageButton)findViewById(R.id.imageButton_main_promotionLoc);
        imageButton_promotionLoc.setOnClickListener(this);
        imageButton_chatLoc = (ImageButton)findViewById(R.id.imageButton_main_chatLoc);
        imageButton_chatLoc.setOnClickListener(this);
        imageView_userPhoto = (ImageView)findViewById(R.id.imageView_userProfile);
        textView_nameUser = (TextView)findViewById(R.id.textView_usrNm);
        textView_emailUser = (TextView)findViewById(R.id.textView_userEmail);

        db = new DBHelper(getApplicationContext());
        dataEmailFromDb = db.getMerchantTopId();
        ModelMerchant merchant = new ModelMerchant();
        merchant = db.getMerchantByEmail(dataEmailFromDb);
        name = merchant.getMerchant_name().toString();
        url_photoFb = merchant.getFacebook_photo().toString();
        email = merchant.getEmail().toString();
        db.closeDB();


        UserApp userApp = App.getUserApp();
        if (userApp == null) userApp = new UserApp();
        userApp.setName(name);
        userApp.setEmail(email);
        userApp.setId(""+merchant.getId());
        userApp.setPicture(url_photoFb);
        userApp.setType(Constants.USER_TYPE);
        App.updateUserApp(userApp);

        Log.d(Constants.TAG, "App.getInstance().getUserApp()=" + App.getUserApp());

        //Log.e("db",db.getAllMerchant().toString());
        textView_nameUser.setText(name);
        txt_nav_name.setText(name);
        txt_nav_email.setText(email);
        if (!url_photoFb.isEmpty()) {
            picasso.with(getApplicationContext()).load(url_photoFb).transform(new CircleTransform()).into(imageView_userPhoto);
            picasso.with(getApplicationContext()).load(url_photoFb).transform(new CircleTransform()).into(imVi_nav_usrPro);
        }

        //update new message counter drawer menu
        updateNewMessageCounter();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_logout) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainMenuActivity.this);
            builder.setTitle("Confirmation");
            String message = "";
            message="Are you sure?";
            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            LoginManager.getInstance().logOut();
                            Intent toFormLogin = new Intent(getApplicationContext(),FormLoginActivity.class);
                            startActivity(toFormLogin);
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            builder.create().dismiss();
                        }
                    });
            builder.create().show();
        } /*else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        Intent gotoNextScreen = null;
        Bundle dataPaket = new Bundle();

        if (v.equals(imageButton_addLoc)) {
            gotoNextScreen = new Intent(getApplicationContext(),AddNewLocationActivity.class);
            dataPaket.putString("email",textView_emailUser.getText().toString());
            gotoNextScreen.putExtras(dataPaket);
        }

        if (v.equals(imageButton_myLoc)) {
            gotoNextScreen = new Intent(getApplicationContext(),ListLocationMerchantActivity.class);
            //dataPaket.putString("email",textView_emailUser.getText().toString());
            dataPaket.putDouble("latitude", latitude);
            dataPaket.putDouble("longitude", longitude);
            gotoNextScreen.putExtras(dataPaket);
        }

        if (v.equals(imageButton_searchLoc)) {
            gotoNextScreen = new Intent(getApplicationContext(),SearchLocationByNameActivity.class);
            //dataPaket.putString("email",textView_emailUser.getText().toString());
            dataPaket.putDouble("latitude", latitude);
            dataPaket.putDouble("longitude", longitude);
            gotoNextScreen.putExtras(dataPaket);
        }

        if (v.equals(imageButton_settingUser)) {
            gotoNextScreen = new Intent(getApplicationContext(),SettingActivity.class);
            dataPaket.putString("email",textView_emailUser.getText().toString());
            gotoNextScreen.putExtras(dataPaket);
        }

        if (v.equals(imageButton_promotionLoc)) {
            gotoNextScreen = new Intent(getApplicationContext(),ListPromotionActivity.class);
            dataPaket.putString("email",textView_emailUser.getText().toString());
            gotoNextScreen.putExtras(dataPaket);
        }

        if (v.equals(imageButton_chatLoc)) {
            gotoNextScreen = new Intent(getApplicationContext(), ChatHistoryActivity.class);
            //gotoNextScreen = new Intent(getApplicationContext(),ListChatActivity.class);
            //dataPaket.putString("email",textView_emailUser.getText().toString());
            //gotoNextScreen.putExtras(dataPaket);
        }

        startActivity(gotoNextScreen);
        finish();
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
        String message = "GPS enabled";
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        String message = "GPS disabled";
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
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

    public class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }

    //BEGIN SOCKET METHOD BLOCK
    public Map<String, BroadcastReceiver> makeReceivers(){
        Map<String, BroadcastReceiver> map = new HashMap<String, BroadcastReceiver>();
        map.put(ChatClientService.ActivityEvent.LIST_CONTACT, listContactReceiver);
        map.put(ChatClientService.ActivityEvent.NEW_MESSAGE, newMessageReceiver);
        return map;
    }

    private void updateNewMessageCounter() {
        int newMessageCount = (int)messageRepository.getNewMessageCount();
        //newMessageCount = 9;
        updateNewMessageCounter(newMessageCount);
    }
    /**
     * update new message counter inline chat menu
     */
    private void updateNewMessageCounter(int newMessageCount) {
        if ( newMessageCount < 1 ) return;
        //update chat meenu item
        TextView texView_chatLoc = (TextView)findViewById(R.id.textView_chat);
        String before = "Chat"; //texView_chatLoc.getText().toString();

        String counter = Integer.toString(newMessageCount) + " ";
        String s = before + " " + counter;
        s = StringUtils.repeat(" ", s.length()) + s;
        SpannableString sColored = new SpannableString(s);

        int textSize = getResources().getDimensionPixelSize(R.dimen.chat_counter);
        int start = s.length() - (counter.length());
        sColored.setSpan(new CircleBackgroundSpan(Color.RED, Color.WHITE, Color.WHITE, textSize, 2, 20), start, s.length(), 0);
        texView_chatLoc.setText(sColored);
    }

    private void loginToChatServer() {
        App.getChatEngine().emitDoLogin(App.getUserApp());
    }

    private BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ChatContact contact = intent.getParcelableExtra("contact");
                    ChatMessage msg = intent.getParcelableExtra("msg");
                    Log.d(Constants.TAG_CHAT, getClass().getName() + String.format(" => new message = %s from %s ", msg.getMessageText(), contact.getEmail()));
                    updateNewMessageCounter();
                }
            });
        }
    };

    private BroadcastReceiver listContactReceiver = new BroadcastReceiver()  {
        @Override
        public void onReceive(Context context, Intent intent){
            ArrayList<ChatContact> contactList = intent.getParcelableArrayListExtra("contactList");
            Log.d(Constants.TAG_CHAT, getClass().getName() + " => list contacts = " + contactList.size());
        }
    };

    //END SOCKET METHOD BLOCK

    @Override
    public void onPause() {
        super.onPause();
        Log.d(Constants.TAG_CHAT, getLocalClassName() + " => ON PAUSE");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(Constants.TAG, getLocalClassName() + " => ON RESUME");
        ChatClientService.registerReceivers(makeReceivers());
        loginToChatServer();
    }


}
