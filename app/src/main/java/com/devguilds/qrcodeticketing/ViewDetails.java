package com.devguilds.qrcodeticketing;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.w3c.dom.Text;


public class ViewDetails extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_details);

        TextView name = (TextView)findViewById(R.id.nameGet);
        TextView password = (TextView)findViewById(R.id.passwordGet);
        TextView contact = (TextView)findViewById(R.id.contactGet);
        TextView country = (TextView)findViewById(R.id.countryGet);



        Bundle bundle = getIntent().getExtras();
        String sender_name = bundle.getString("name");
        String sender_pass = bundle.getString("password");
        String sender_cont = bundle.getString("contact");
        String sender_count = bundle.getString("country");

        name.setText(sender_name);
        password.setText(sender_pass);
        contact.setText(sender_cont);
        country.setText(sender_count);


    }


}
