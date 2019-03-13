package com.ecs193.speedometerconverter;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RadioGroup;
import android.widget.ListAdapter;
import android.view.ViewGroup;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.Toast;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.os.Handler;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.ToggleButton;
import android.content.res.Resources;
//import com.ecs193.speedometerconverter.BluetoothActivity;
import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.Collections;
import android.widget.Toolbar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Scanner;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    public TextView mTextMessage;
    public static String EXTRA_ADDRESS = "device_address";

    Button btnPaired;
    ListView devicelist;

    String[] settings;
    String output;
    TextView tv;
    private Set<BluetoothDevice> pairedDevices;

    //Toolbar mToolbar = findViewById(R.id.toolbar);
    ListView mListView;

    ListView mListVal;

    // led
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    TextView unitsText;
    TextView maxSpeedText;
    TextView magnetsText;
    TextView finalDriveText;
    TextView meterRatioText;
    TextView wheelSizeText;
    TextView wheelCircText;
    Button btnOn, btnOff, btnDis;
    String wheelUnit;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // here we set the listener
            switch (item.getItemId()) {
                case R.id.bottombar_bluetooth:
                    BluetoothActivity();
                    meterSettings();
                    return true;
                case R.id.bottombar_settings:
                    findViewById(R.id.searchDevices).setVisibility(View.GONE);
                    findViewById(R.id.meterSettings).setVisibility(View.GONE);
                    //setTitle("Calibration Settings");
                    //findViewById(R.id.bluetoothLayout).setVisibility(ConstraintLayout.GONE);
                    return true;
                case R.id.bottombar_data:
                    findViewById(R.id.searchDevices).setVisibility(View.GONE);
                    findViewById(R.id.meterSettings).setVisibility(View.GONE);
                    //setTitle("Data");
                    //findViewById(R.id.bluetoothLayout).setVisibility(ConstraintLayout.GONE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // change opacity of calibration layout
        findViewById(R.id.meterSettings).setAlpha((float)0.3);

        BluetoothActivity();
        meterSettings();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        //Integer btID = getResources().getIdentifier("@string/menu_bluetooth","layout", getPackageName());
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    void BluetoothActivity() {

        // Get layout of bluetooth tab
        findViewById(R.id.searchDevices).setVisibility(View.VISIBLE);

        btnPaired = findViewById(R.id.findButton);
        devicelist = findViewById(R.id.listBluetooth);
        myBluetooth = BluetoothAdapter.getDefaultAdapter();


        if (myBluetooth == null) {
            //Show a message that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        } else if (!myBluetooth.isEnabled()) {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);

        } else {
            pairedDevicesList();
        }


        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList();
            }
        });


    }

    public void pairedDevicesList() {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);

        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
        //setListViewHeightBasedOnChildren(devicelist, findViewById(R.id.searchDevices), 1);

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            address = info.substring(info.length() - 17);


            // Initialize a TextView for ListView each Item
            tv = v.findViewById(android.R.id.text1);

            // Set the text color of TextView (ListView Item)
            tv.setTextColor(Color.WHITE);

            new ConnectBT().execute(); //Call the class to connect

            // Make an intent to start next activity.
            //Intent i = new Intent(MainActivity.this, ledControl.class);

            //Change the activity.
            //i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
            //startActivity(i);
        }
    };

    void meterSettings() {
        findViewById(R.id.meterSettings).setVisibility(View.VISIBLE);
        /*try {
            // Read user settings
            FileInputStream fin = openFileInput("SpeedometerSettings");

            int c;
            String temp="";
            while( (c = fin.read()) != -1) {
                temp = temp + Character.toString((char) c);
            }

            fin.close();

            settings = temp.split("\n", 6);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        // Get layout of meter settings
        findViewById(R.id.calibrationTitle).setVisibility(View.VISIBLE);
        findViewById(R.id.listSettings).setVisibility(View.VISIBLE);

        mListView = findViewById(R.id.listSettings);
        maxSpeedText = findViewById(R.id.maxSpeedText);
        magnetsText = findViewById(R.id.magnetsText);
        finalDriveText = findViewById(R.id.finalDriveText);
        meterRatioText = findViewById(R.id.meterRatioText);
        wheelSizeText = findViewById(R.id.wheelSizeText);
        wheelCircText = findViewById(R.id.wheelCircText);
        unitsText = findViewById(R.id.unitsText);
        btnOn = (Button)findViewById(R.id.button2);
        btnOff = (Button)findViewById(R.id.button3);
        btnDis = (Button)findViewById(R.id.button4);

        final ArrayAdapter mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.settings_array));

        String output;
        //final ArrayAdapter mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, myResArrayList);

        //mListView.setAdapter(lviewAdapter);
        //lviewAdapter = new ListViewAdapter(this, getResources().getStringArray(R.array.settings_array), getResources().getStringArray(R.array.settings_array));

        mListView.setAdapter(mAdapter);

        //setListViewHeightBasedOnChildren(mListView, findViewById(R.id.meterSettings), 0.8);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, final View view, int i, long l) {

                final int itemNum = i;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getResources().getStringArray(R.array.settings_array)[i]);


                if (i == 0) {
                    // set default unit
                    unitsText.setText(getResources().getStringArray(R.array.units_array)[0]
                            .replaceAll(".* ", "")
                            .replaceAll("\\(","")
                            .replaceAll("\\)",""));

                    builder.setSingleChoiceItems(getResources().getStringArray(R.array.units_array),
                            i, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int selectedIndex) {
                            switch (itemNum) {
                                case 0: // units
                                    unitsText.setText(getResources().getStringArray(R.array.units_array)[selectedIndex]
                                            .replaceAll(".* ", "")
                                            .replaceAll("\\(","")
                                            .replaceAll("\\)",""));
                            }
                            sendUnits(selectedIndex);
                        }
                    });
                    builder.setPositiveButton("Confirm", null);
                    builder.setNegativeButton("Cancel", null);


                } else if (i == 5) { // wheel size

                    LinearLayout layout = new LinearLayout(MainActivity.this);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    // Add a TextView here for the "Title" label, as noted in the comments
                    final EditText tireSize1 = new EditText(MainActivity.this);
                    tireSize1.setInputType(InputType.TYPE_CLASS_NUMBER);
                    tireSize1.setHint("Wheel size 1");
                    layout.addView(tireSize1); // Notice this is an add method

                    // Add another TextView here for the "Description" label
                    final EditText tireSize2 = new EditText(MainActivity.this);
                    tireSize2.setInputType(InputType.TYPE_CLASS_NUMBER);
                    tireSize2.setHint("Wheel size 2");
                    layout.addView(tireSize2); // Another add method

                    // Add another TextView here for the "Description" label
                    final EditText tireSize3 = new EditText(MainActivity.this);
                    tireSize3.setInputType(InputType.TYPE_CLASS_NUMBER);
                    tireSize3.setHint("Wheel size 3");
                    layout.addView(tireSize3); // Another add method

                    builder.setView(layout); // Again this is a set method, not add

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (itemNum) {
                                case 5: // wheel size
                                    if ((tireSize1.getText() != null) &&
                                            (tireSize2.getText() != null) &&
                                            (tireSize3.getText() != null)) {
                                        wheelSizeText.setText(tireSize1.getText().toString() + ", " +
                                                tireSize2.getText().toString() + ", " +
                                                tireSize3.getText().toString());
                                    }
                                    sendWheelSizeCalc(wheelSizeText, tireSize1.getText().toString(),
                                            tireSize2.getText().toString(),
                                            tireSize3.getText().toString(),
                                            unitsText.getText().toString());
                                    break;
                            }
                        }
                    });

                } else if (i == 6) { // wheel circumference

                    LinearLayout layout = new LinearLayout(MainActivity.this);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    // Add a TextView here for the "Title" label, as noted in the comments
                    final EditText wheelCirc = new EditText(MainActivity.this);
                    wheelCirc.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    wheelCirc.setHint("Wheel circumference");
                    layout.addView(wheelCirc); // Notice this is an add method

                    //final RadioGroup circUnits = new RadioGroup(MainActivity.this);
                    // Add another TextView here for the "Description" label

                    final RadioButton circInch = new RadioButton(MainActivity.this);
                    circInch.setText("Inch (in)");
                    layout.addView(circInch); // Another add method

                    final RadioButton circCM = new RadioButton(MainActivity.this);
                    circCM.setText("Centimeter (cm)");
                    layout.addView(circCM); // Another add method

                    builder.setView(layout); // Again this is a set method, not add

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            switch (itemNum) {
                                case 6: // wheel circ

                                    // Check which radio button was clicked
                                    if (circInch.isChecked()) {
                                        wheelUnit = "inch";
                                        circCM.setChecked(false);
                                    } else {
                                        wheelUnit = "cm";
                                        circInch.setChecked(false);
                                    }

                                    wheelCircText.setText(wheelCirc.getText().toString() + " " +
                                            wheelUnit);
                                    sendWheelCircCalc(wheelCircText, wheelUnit);
                                    break;
                            }
                        }
                    });

                } else if (itemNum == 1 || itemNum == 3) { // max speed or final drive
                    final EditText input = new EditText(MainActivity.this);
                    input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (itemNum == 1) {
                                maxSpeedText.setText(input.getText().toString());
                                sendInfo(maxSpeedText, "M:");
                            } else { // 3
                                finalDriveText.setText(input.getText().toString());
                                sendCalc(finalDriveText, "F:");
                            }
                        }
                    });


                        } else {
                    // Set up the input
                    final EditText input = new EditText(MainActivity.this);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    //input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    //input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    builder.setView(input);


                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            switch (itemNum) {
                                case 0: // units
                                    break;
                                case 1: // max speed
                                    //maxSpeedText.setText(input.getText().toString());
                                    //sendInfo(maxSpeedText, "M:");
                                    break;
                                case 2: // magnets
                                    magnetsText.setText(input.getText().toString());
                                    sendInfo(magnetsText, "N:");
                                    break;
                                case 3: // final drive
                                    //finalDriveText.setText(input.getText().toString());
                                    //sendCalc(finalDriveText, "F:");
                                    break;
                                case 4: // meter ratio
                                    meterRatioText.setText(input.getText().toString());
                                    sendCalc(meterRatioText, "S:");
                                    break;
                                case 5: // wheel size
                                    //wheelSizeText.setText(input.getText().toString());
                                    //sendWheelSizeCalc(wheelSizeText, );
                                    break;
                                case 6: // wheel circumference
                                    //wheelSizeText.setText(input.getText().toString());
                                    //sendWheelCircCalc(wheelSizeText);
                                    break;
                            }

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                }
                builder.show();

                //Toast.makeText(MainActivity.this, adapterView.getItemAtPosition(i).toString(), Toast.LENGTH_SHORT).show();
                // Initialize a TextView for ListView each Item
                tv = view.findViewById(android.R.id.text1);

                // Set the text color of TextView (ListView Item)
                tv.setTextColor(Color.WHITE);
            }
        });

        /*mListVal = findViewById(R.id.listValue);

        ArrayList wordList = new ArrayList(Arrays.asList(settings));

        final ArrayAdapter mAdapterVal = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                wordList);
        mListVal.setAdapter(mAdapterVal);*/

        //for (String eachStr : settings)
        //    output += eachStr + "\n";


        /*try {
            FileOutputStream outputStream = getApplicationContext().openFileOutput("SpeedometerSettings",
                    Context.MODE_PRIVATE);
            outputStream.write(output.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        btnOn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                turnOnLed();      //method to turn on
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                turnOffLed();   //method to turn off
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s, Toast.LENGTH_LONG).show();
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
            /*progress.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    progress.dismiss();
                }
            }, 1500);  // 1500 milliseconds*/
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            myBluetooth = null;
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
                // change opacity of calibration layout
                findViewById(R.id.meterSettings).setAlpha((float)1);
            }
            progress.dismiss();
        }
    }

    void sendUnits(int index) {

        if (index == 0) { // kph
            if (btSocket != null) {
                try {
                    String str = "U:";
                    str = str + "0" + '\0';

                    System.out.println(str);

                    btSocket.getOutputStream().write(str.getBytes());
                } catch (IOException e) {
                    msg("Error");
                }
            }
        } else { // mph
            if (btSocket != null) {
                try {
                    String str = "U:";
                    str = str + "1" + '\0';
                    System.out.println(str);

                    btSocket.getOutputStream().write(str.getBytes());
                } catch (IOException e) {
                    msg("Error");
                }
            }
        }
    }

    void sendInfo(final TextView tv, final String extraStr) {

        tv.setOnEditorActionListener(new TextView.OnEditorActionListener() { //TXTFIELD
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {

                    if (btSocket!=null) {
                        try
                        {
                            String str = v.getText().toString();
                            str=extraStr + str + '\0';
                            //System.out.println(str);
                            msg(str);

                            btSocket.getOutputStream().write(str.getBytes());
                        }
                        catch (IOException e)
                        {
                            msg("Error");
                        }
                    }

                    handled = true;
                }
                return handled;
            }
        });
    }

    void sendCalc(final TextView tv, final String extraStr) {
        tv.setOnEditorActionListener(new TextView.OnEditorActionListener() { //TXTFIELD 3
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {

                    if (btSocket!=null) {
                        try
                        {
                            //String str = getText();
                            String str = v.getText().toString();
                            int result = Integer.parseInt(str);
                            result=result*1000000;
                            str=new Integer(result).toString();
                            str=extraStr + str + '\0';
                            msg(str);
                            btSocket.getOutputStream().write(str.getBytes());
                        }
                        catch (IOException e)
                        {
                            msg("Error");
                        }
                    }

                    handled = true;
                }
                return handled;
            }
        });
    }

    void sendWheelSizeCalc(final TextView tv, final String size1, final String size2,
                           final String size3, final String unit) {

        tv.setOnEditorActionListener(new TextView.OnEditorActionListener() { //TXTFIELD 5
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {

                    if (btSocket != null) {
                        try {

                            int result1 = Integer.parseInt(size1);
                            int result2 = Integer.parseInt(size2);
                            int result3 = Integer.parseInt(size3);

                            double diameter;
                            double result;

                            if (unit == "kph") {
                                diameter = (result1 * result2 / 500) + (result3 * 2.54);
                                result = (diameter / 100) * Math.PI;
                            } else { // mph
                                diameter = (result1 * result2 / 1270) + result3;
                                result = (diameter * 1000 / 63360) * Math.PI;
                            }
                            result = result * 100000000;
                            String str = new Integer((int)Math.floor(result + 0.5d)).toString();
                            str = "W:" + str + '\0';
                            msg(str);

                            btSocket.getOutputStream().write(str.getBytes());
                        } catch (IOException e) {
                            msg("Error");
                        }
                    }

                    handled = true;
                }
                return handled;
            }
        });
    }

    void sendWheelCircCalc(final TextView tv, final String wheelUnit) {

        tv.setOnEditorActionListener(new TextView.OnEditorActionListener() { //TXTFIELD 5
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {

                    if (btSocket != null) {
                        try {
                            //String str = getText();
                            String str = v.getText().toString();
                            int result = Integer.parseInt(str);

                            if (wheelUnit == "inch") {
                                result = result * 100000000;
                                result = result / 63360;
                            } else {
                                result = result * 10000;
                            }

                            str = new Integer(result).toString();
                            str = "W:" + str + '\0';
                            msg(str);

                            btSocket.getOutputStream().write(str.getBytes());
                        } catch (IOException e) {
                            msg("Error");
                        }
                    }

                    handled = true;
                }
                return handled;
            }
        });
    }

    public  void setListViewHeightBasedOnChildren(final ListView listView, final View largeLinear, double weight) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();

            // for title
            if (i == (len - 1)) {
                totalHeight += listItem.getMeasuredHeight();
            }
        }

        //totalHeight += listItem.getMeasuredHeight();


        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));

        largeLinear.setLayoutParams(params);
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }

    private void turnOffLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("ab".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void turnOnLed()
    {
        if (btSocket!=null)
        {
            try
            {
                InputStream a=null;
                btSocket.getOutputStream().write("m1 on".toString().getBytes());

                a = btSocket.getInputStream();
                if (a!=null) {
                    msg("InputStream a is not null");
                    int str=a.read();
                    System.out.println(str);

                }
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

}

