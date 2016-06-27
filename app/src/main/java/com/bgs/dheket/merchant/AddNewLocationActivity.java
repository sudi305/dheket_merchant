package com.bgs.dheket.merchant;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.bgs.dheket.sqlite.DBHelper;
import com.bgs.dheket.sqlite.ModelLocation;

/**
 * Created by SND on 6/13/2016.
 */
public class AddNewLocationActivity extends AppCompatActivity {
    android.support.v7.app.ActionBar actionBar;

    String location_name = "", location_address = "", phone = "", description = "", location_tag = "", user_email = "";
    long id_location, merchant_id;
    double latitude, longitude;
    int category_id, isPromo;
    ModelLocation location;

    EditText editText_loc_name, editText_loc_address, editText_loc_phone, editText_loc_description;
    ImageButton imageButton_next_to_b;

    DBHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location_form_a);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.d_ic_back));
        //actionBar.setHomeAsUpIndicator(R.drawable.logo);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("New Location");
        actionBar.setSubtitle("Primary Information");

        //urls = String.format(getResources().getString(R.string.lin));//"http://dheket.esy.es/getLocationByCategory.php"
        dataLoad();

        editText_loc_name = (EditText)findViewById(R.id.editText_anl_loc_name);
        editText_loc_address = (EditText)findViewById(R.id.editText_anl_loc_address);
        editText_loc_phone = (EditText)findViewById(R.id.editText_anl_loc_phone);
        editText_loc_description = (EditText)findViewById(R.id.editText_anl_loc_description);
        imageButton_next_to_b = (ImageButton)findViewById(R.id.imageButton_anl_next_b);
        imageButton_next_to_b.setOnClickListener(toFormB);

        editText_loc_name.setText("" + location_name);
        editText_loc_address.setText("" + location_address);
        editText_loc_phone.setText("" + phone);
        editText_loc_description.setText("" + description);

        editText_loc_name.addTextChangedListener(checkValidation);
        editText_loc_address.addTextChangedListener(checkValidation);
        editText_loc_phone.addTextChangedListener(checkValidation);
        editText_loc_description.addTextChangedListener(checkValidation);

        enableButton();

    }

    public void dataLoad(){
        db = new DBHelper(getApplicationContext());
        location = new ModelLocation();
        user_email = db.getMerchantTopId();
        location = db.getLocationByEmail(user_email);
        location_name = location.getLocation_name().toString();
        location_address = location.getLocation_address().toString();
        phone = location.getPhone();
        description = location.getDescription();
        location_tag = location.getLocation_tag();
        id_location = location.getId_location();
        merchant_id = location.getMerchant_id();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        category_id = location.getCategory_id();
        isPromo = location.getIsPromo();
    }

    public void dataPrepareSave(){
        location_name = editText_loc_name.getText().toString();
        location_address = editText_loc_address.getText().toString();
        phone = editText_loc_phone.getText().toString();
        description = editText_loc_description.getText().toString();
        ModelLocation loc = new ModelLocation(0,location_name,location_address,latitude,longitude,category_id,
                phone,isPromo,merchant_id,description,location_tag,user_email);
        db.updateLocation(loc);
        db.closeDB();
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
        if (!editText_loc_name.getText().toString().isEmpty() && !editText_loc_address.getText().toString().isEmpty()){
            imageButton_next_to_b.setEnabled(true);
        } else {
            imageButton_next_to_b.setEnabled(false);
        }
    }

    public View.OnClickListener toFormB = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dataPrepareSave();
            Intent nextToFormB = new Intent(getApplicationContext(),AddNewLocationActivity_form_b.class);
            startActivity(nextToFormB);
            finish();
        }
    };

    public void onBackPressed() {
        toMainMenu();
    }

    public void toMainMenu(){
        ModelLocation location = new ModelLocation(0,"","",0.0,0.0,0,"",0,0,"","",user_email);
        db.updateLocation(location);
        Intent toMainMenu = new Intent(getApplicationContext(),MainMenuActivity.class);
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
}
