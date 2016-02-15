package com.bgs.dheket.merchant;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;

import com.bgs.common.Utility;
import com.bgs.imageOrView.PagerAdapterDetailLoc;
import com.facebook.login.LoginManager;

/**
 * Created by SND on 01/02/2016.
 */
public class DetailLocationActivity extends AppCompatActivity {
    android.support.v7.app.ActionBar actionBar;
    Bundle paket;
    String loc_name, loc_address, kategori;
    int id_loc, loc_promo, cat_id;
    double radius, latitude, longitude;

    //NumberFormat formatter = new DecimalFormat("#0.000");
    Utility formatNumber = new Utility();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_location);

        paket = getIntent().getExtras();
        loc_name = paket.getString("loc_name");
        id_loc = paket.getInt("id_loc");
        loc_promo = paket.getInt("loc_promo");
        loc_address = paket.getString("loc_address");
        radius = paket.getDouble("radius");
        cat_id = paket.getInt("cat_id");
        latitude = paket.getDouble("latitude");
        longitude = paket.getDouble("longitude");
        kategori = paket.getString("kategori");

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeAsUpIndicator(R.drawable.logo);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(""+loc_name);

        /*actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>Category " + paket.getString("kategori") + " in Radius "
                + formatter.format(paket.getDouble("radius")) + " Km</font>"));*/
        actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'> "+loc_address+" </font>"));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout_loc_detail);
        tabLayout.addTab(tabLayout.newTab().setText("Detail"));
        tabLayout.addTab(tabLayout.newTab().setText("Chat"));
        if (loc_promo==1)tabLayout.addTab(tabLayout.newTab().setText("Promo"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager_loc_detail);
        final PagerAdapterDetailLoc adapter = new PagerAdapterDetailLoc(getSupportFragmentManager(), tabLayout.getTabCount(),id_loc,
                cat_id, radius, latitude, longitude, kategori);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (item.getItemId() == android.R.id.home) {
            back_to_previous_screen();
            return super.onOptionsItemSelected(item);
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout_user();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logout_user(){
        String logout = getResources().getString(com.facebook.R.string.com_facebook_loginview_log_out_action);
        String cancel = getResources().getString(com.facebook.R.string.com_facebook_loginview_cancel_action);
        String message= "Are you sure?";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton(logout, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        LoginManager.getInstance().logOut();
                        Intent logout_user_fb = new Intent(DetailLocationActivity.this, FormLoginActivity.class);
                        startActivity(logout_user_fb);
                        finish();
                    }
                })
                .setNegativeButton(cancel, null);
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        back_to_previous_screen();
    }

    public void back_to_previous_screen(){
        Intent intent = new Intent(DetailLocationActivity.this,ListAndMapAllLocActivity.class);
        paket.putString("kategori",kategori);
        paket.putInt("cat_id",cat_id);
        paket.putDouble("radius",radius);
        paket.putDouble("latitude",latitude);
        paket.putDouble("longitude",longitude);
        intent.putExtras(paket);
        startActivity(intent);
        finish();
    }
}
