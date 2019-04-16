package com.ecs193.speedometerconverter;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import android.graphics.Color;
import java.io.IOException;
import java.io.DataInputStream;
import java.util.List;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.LinearLayout;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    Button pairDevice;
    //TextView butUnits;
    Button butUnits;
    Button butMaxSpeed;
    Button butMagnets;
    Button butFinalDrive;
    Button butRatio;
    Button butSize;

    TextView unitsText;
    TextView maxSpeedText;
    TextView magnetsText;
    TextView finalDriveText;
    TextView meterRatioText;
    TextView wheelSizeText;
    TextView wheelCircText;

    TextView tv;
    ListView mListView;
    BluetoothSocket btSocket = null;

    AlertDialog alertDialog1;

    String wheelUnit;
    final static int BT_INTENT_FLAG = 0;
    //public final static String EXTRA_ADDRESS = "com.example.myfirstapp.MESSAGE";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // here we set the listener
            switch (item.getItemId()) {
                case R.id.bottombar_bluetooth:
                    setContentView(R.layout.activity_main);
                    //getMeterSettings();
                    //putMeterSettings();
                    return true;
                case R.id.bottombar_settings:
                    findViewById(R.id.searchDevicesTitle).setVisibility(View.GONE);
                    findViewById(R.id.meterSettings).setVisibility(View.GONE);
                    //setTitle("Calibration Settings");
                    //findViewById(R.id.bluetoothLayout).setVisibility(ConstraintLayout.GONE);
                    return true;
                case R.id.bottombar_data:
                    findViewById(R.id.searchDevicesTitle).setVisibility(View.GONE);
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
        //findViewById(R.id.meterSettings).setAlpha((float)0.3);
        findViewById(R.id.meterSettings).setVisibility(View.VISIBLE);
        findViewById(R.id.meterSettings).setAlpha((float)0.3);
        // Define widgets

        //mListView = findViewById(R.id.listSettings);

        //wheelSizeText = findViewById(R.id.wheelSizeText);


        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        pairDevice = findViewById(R.id.deviceArrow);
        pairDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent btIntent = new Intent(MainActivity.this, BtConnection.class);
            startActivityForResult(btIntent, BT_INTENT_FLAG);

            }
        });

        butUnits = findViewById(R.id.but_units);
        unitsText = findViewById(R.id.unitsText);
        butUnits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getButtonDialog("Choose Units", R.array.units_array, unitsText);
            }
        });

        butMaxSpeed= findViewById(R.id.but_maxSpeed);
        maxSpeedText = findViewById(R.id.maxSpeedText);
        butMaxSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTextBoxDialog("Enter Max Speed", maxSpeedText, "M:");
            }
        });

        butMagnets= findViewById(R.id.but_magnets);
        magnetsText = findViewById(R.id.magnetsText);
        butMagnets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getButtonDialog("Choose Number of Magnets", R.array.magnet_array, magnetsText);
            }
        });

        butFinalDrive= findViewById(R.id.but_finalDrive);
        finalDriveText = findViewById(R.id.finalDriveText);
        butFinalDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTextBoxDialog("Enter Final Drive", finalDriveText, "F:");
            }
        });

        butRatio= findViewById(R.id.but_ratio);
        meterRatioText = findViewById(R.id.meterRatioText);
        butRatio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTextBoxDialog("Enter Speedometer Ratio", meterRatioText, "S:");
            }
        });

        butSize= findViewById(R.id.but_size);
        wheelCircText = findViewById(R.id.wheelCircText);
        butSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getMultiTextBoxDialog(view,"Enter Tire Circumference", wheelCircText);
                LinearLayout layout = new LinearLayout(MainActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                // Set text box input as decimal only
                final EditText wheelCirc = new EditText(MainActivity.this);
                wheelCirc.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                wheelCirc.setHint("Wheel circumference");
                layout.addView(wheelCirc); // Notice this is an add method

                // Set up radio button for units
                final RadioButton circInch = new RadioButton(MainActivity.this);
                circInch.setText("Inch (in)");
                layout.addView(circInch);

                final RadioButton circCM = new RadioButton(MainActivity.this);
                circCM.setText("Centimeter (cm)");
                layout.addView(circCM);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Enter Tire Circumference")
                        //.setIcon(R.drawable.ic_baseline_create_24px)
                        .setView(layout)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // Check which radio button was clicked
                                if (circInch.isChecked()) {
                                    wheelUnit = "inch";
                                    circCM.setChecked(false); // Make sure the other option is unchecked
                                } else {
                                    wheelUnit = "cm";
                                    circInch.setChecked(false);
                                }

                                wheelCircText.setText(wheelCirc.getText().toString() + " " + wheelUnit);

                                if (wheelCirc.getText().toString().length() != 0) {
                                    sendWheelCircCalc(wheelCirc.getText().toString(), wheelUnit);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null);

                alertDialog1 = builder.create();
                alertDialog1.show();

                // Initialize a TextView for ListView each Item
                //tv = view.findViewById(android.R.id.text1);

                // Set the text color of TextView (ListView Item)
                //tv.setTextColor(Color.WHITE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check which request we're responding to
        if (requestCode == BT_INTENT_FLAG) {

            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                btSocket = BtConnection.getBtConnection();
                findViewById(R.id.meterSettings).setAlpha((float)1);
                getMeterSettings();
            }
        }
    }

    void getButtonDialog(final String title, int arrID, final TextView textBox) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
            .setTitle(title)
            .setIcon(R.drawable.ic_baseline_create_24px)
            .setSingleChoiceItems(getResources().getStringArray(arrID),
                    -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {

                    if (title == "Choose Units") {
                        switch(item) {
                            case 0:
                                textBox.setText("mph");
                                break;
                            case 1:
                                textBox.setText("kph");
                                break;
                        }
                    } else if (title == "Choose Number of Magnets") {
                        switch(item) {
                            case 0:
                                textBox.setText("1");
                                break;
                            case 1:
                                textBox.setText("2");
                                break;
                            case 2:
                                textBox.setText("4");
                                break;
                        }
                    }
                }
            })
            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (title == "Choose Units") {

                        if (textBox.getText().toString().equalsIgnoreCase("mph")) {
                            sendUnits(Integer.toString(0));
                        } else if (textBox.getText().toString().equalsIgnoreCase("kph")) {
                            sendUnits(Integer.toString(1));
                        }

                    } else if (title == "Choose Number of Magnets") {

                        if (textBox.getText().toString().equalsIgnoreCase("1")) {
                            sendInfo(Integer.toString(1), "N:");
                        } else if (textBox.getText().toString().equalsIgnoreCase("2")) {
                            sendInfo(Integer.toString(2), "N:");
                        } else if (textBox.getText().toString().equalsIgnoreCase("4")) {
                            sendInfo(Integer.toString(4), "N:");
                        }

                    }
                }
            })
            .setNegativeButton("Cancel", null);

        alertDialog1 = builder.create();
        alertDialog1.show();
    }

    void getTextBoxDialog(final String title, final TextView textBox, final String arduinoStr) {

        // Set text box input as int only
        final EditText input = new EditText(MainActivity.this);
        if (arduinoStr == "M:") { // magnet
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if ((arduinoStr == "F:") || (arduinoStr == "S:")) {
            input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
            .setTitle(title)
            .setIcon(R.drawable.ic_baseline_create_24px)
            .setView(input)
            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    textBox.setText(input.getText().toString());

                    if (input.getText().toString().length() != 0) {
                        if (arduinoStr == "M:") {
                            sendInfo(input.getText().toString(), arduinoStr);
                        } else if ((arduinoStr == "F:") || (arduinoStr == "S:")) {
                            sendCalc(input.getText().toString(), arduinoStr);
                        }
                    }
                }
            })
            .setNegativeButton("Cancel", null);

        alertDialog1 = builder.create();
        alertDialog1.show();
    }


    void getMultiTextBoxDialog (View view, final String title, final TextView textBox) {

        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Set text box input as decimal only
        final EditText wheelCirc = new EditText(MainActivity.this);
        wheelCirc.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        wheelCirc.setHint("Wheel circumference");
        layout.addView(wheelCirc); // Notice this is an add method

        // Set up radio button for units
        final RadioButton circInch = new RadioButton(MainActivity.this);
        circInch.setText("Inch (in)");
        layout.addView(circInch);

        final RadioButton circCM = new RadioButton(MainActivity.this);
        circCM.setText("Centimeter (cm)");
        layout.addView(circCM);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
            .setTitle(title)
            //.setIcon(R.drawable.ic_baseline_create_24px)
            .setView(layout)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // Check which radio button was clicked
                    if (circInch.isChecked()) {
                        wheelUnit = "inch";
                        circCM.setChecked(false); // Make sure the other option is unchecked
                    } else {
                        wheelUnit = "cm";
                        circInch.setChecked(false);
                    }

                    textBox.setText(wheelCirc.getText().toString() + " " + wheelUnit);

                    if (wheelCirc.getText().toString().length() != 0) {
                        sendWheelCircCalc(wheelCirc.getText().toString(), wheelUnit);
                    }
                }
            })
            .setNegativeButton("Cancel", null);

        alertDialog1 = builder.create();
        alertDialog1.show();

        // Initialize a TextView for ListView each Item
        tv = view.findViewById(android.R.id.text1);

        // Set the text color of TextView (ListView Item)
        tv.setTextColor(Color.WHITE);

        /*// Set up layout for alert dialog
        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Set text box input as int only
        final EditText tireSize1 = new EditText(MainActivity.this);
        tireSize1.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        tireSize1.setHint("Wheel size 1");
        layout.addView(tireSize1); // Notice this is an add method

        final EditText tireSize2 = new EditText(MainActivity.this);
        tireSize2.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        tireSize2.setHint("Wheel size 2");
        layout.addView(tireSize2);

        final EditText tireSize3 = new EditText(MainActivity.this);
        tireSize3.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        tireSize3.setHint("Wheel size 3");
        layout.addView(tireSize3);

        // Set layout for alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
            .setTitle(title)
            .setIcon(R.drawable.ic_baseline_create_24px)
            .setView(layout)
            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    boolean result = false;

                    textBox.setText(tireSize1.getText().toString() + ", " +
                            tireSize2.getText().toString() + ", " +
                            tireSize3.getText().toString());

                    if ((tireSize1.getText().toString().length() != 0) &&
                            (tireSize2.getText().toString().length() != 0)  &&
                            (tireSize3.getText().toString().length() != 0)) {

                         sendWheelSizeCalc(tireSize1.getText().toString(),
                                tireSize2.getText().toString(),
                                tireSize3.getText().toString(),
                                unitsText.getText().toString());
                    }
                }
            })
            .setNegativeButton("Cancel", null);

        alertDialog1 = builder.create();
        alertDialog1.show();*/
    }

    void getMeterSettings() {

        findViewById(R.id.meterSettings).setVisibility(View.VISIBLE);
        if (btSocket != null) {
            try {

                String rtnStr = null;
                String[] splitArr;

                btSocket.getOutputStream().write("L:1\0".getBytes());
                for (int rtnBuff = 0; ((rtnBuff = btSocket.getInputStream().read()) >= 0) && rtnBuff != 13;) { // 13 == '\0'
                    //Log.d("Test", b + " " + (char) b);
                    if (rtnStr == null) {
                        rtnStr = Character.toString((char) rtnBuff);
                    } else {
                        rtnStr += Character.toString((char) rtnBuff);
                    }
                }

                if (!rtnStr.startsWith("D:")) {
                    msg("error reading in values");
                    return;
                } else {

                    splitArr = rtnStr.split(":");

                    if (Integer.valueOf(splitArr[1]) == 0) { // units = MPH
                        unitsText.setText("mph");
                    } else if (Integer.valueOf(splitArr[1]) == 1) { // units = KPH
                        unitsText.setText("kph");
                    } else {
                        msg("error reading units");
                        return;
                    }

                    maxSpeedText.setText(splitArr[2]);
                    magnetsText.setText(splitArr[3]);

                    getOrigData(splitArr[4], finalDriveText, 1000000, false);
                    getOrigData(splitArr[5], meterRatioText, 1000000, false);
                    getOrigData(splitArr[6], wheelCircText, 100000000, true);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void getOrigData(String src, TextView dest, int divider, boolean wheelSize) {

        if (src != null) {

            if (wheelSize) {
                double val = Double.valueOf(src) / divider / Math.PI;
                if (unitsText.getText().toString().equalsIgnoreCase("mph")) {
                    dest.setText(String.valueOf(val * 63360 / 1000 / 3)); // divided by 3
                } else if (unitsText.getText().toString().equalsIgnoreCase("kph")) {

                    dest.setText(String.valueOf(val * 100));
                }
            } else {
                dest.setText(String.valueOf((Double.valueOf(src)) / divider));
            }
        }
    }

    void sendUnits(String indexStr) {
        //System.out.println(indexStr);
        //System.out.println("string length is: "+indexStr.length());

        if (btSocket != null) {
            try {
                String str = "U:";
                //System.out.println(indexStr);
                str = str + indexStr + '\0';
                btSocket.getOutputStream().write(str.getBytes());
                msg(str);

                //System.out.println("3");
            } catch (IOException e) {
                msg("Error");
            }
        }

    }

    void sendInfo(String textStr, String extraStr) {

        if (btSocket != null) {
            try {
                String str = extraStr + textStr + '\0';
                btSocket.getOutputStream().write(str.getBytes());
                msg(str);

            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    void sendCalc(String textStr, String extraStr) {

        if (btSocket!=null) {
            try {
                String str = textStr;
                float result = Float.parseFloat(str);
                result=result*1000000;
                str = extraStr + Integer.toString((int) result) + '\0';
                btSocket.getOutputStream().write(str.getBytes());
                msg(str);

            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    /*void sendWheelSizeCalc(String size1, String size2, String size3, String unit) {

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

                String str = "W:" + Integer.toString((int) result) + '\0';
                btSocket.getOutputStream().write(str.getBytes());
                msg(str);

            } catch (IOException e) {
                msg("Error");
            }
        }
    }*/

    void sendWheelCircCalc(String textStr, String wheelUnit) {

        if (btSocket != null) {
            try {

                String str = textStr;
                float result = Float.parseFloat(str);

                if (wheelUnit == "inch") {
                    result = result * 1000000000;
                    result = result / 63360;
                } else { // cm
                    result = result * 10000;
                }

                str = "W:" + Integer.toString((int) result) + '\0';
                btSocket.getOutputStream().write(str.getBytes());
                msg(str);

            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s, Toast.LENGTH_LONG).show();
    }

    /*void putMeterSettings() {

        // Get layout for meter settings
        findViewById(R.id.meterSettings).setVisibility(View.VISIBLE);

        final ArrayAdapter mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.settings_array));

        mListView.setAdapter(mAdapter);

        //setListViewHeightBasedOnChildren(mListView, findViewById(R.id.meterSettings), 0.8);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, final View view, int i, long l) {

                final int itemNum = i;

                // Set up alert dialog with title
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getResources().getStringArray(R.array.settings_array)[i]);

                if (itemNum == 0) { // units

                    builder.setSingleChoiceItems(getResources().getStringArray(R.array.units_array),
                            -1, new DialogInterface.OnClickListener() {


                                @Override
                                public void onClick(DialogInterface dialogInterface, int selectedIndex) {

                                    unitsText.setText(getResources()
                                            .getStringArray(R.array.units_array)[selectedIndex]
                                            .replaceAll(".* ", "")
                                            .replaceAll("\\(", "")
                                            .replaceAll("\\)", ""));
                                }
                            });

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //sendUnits(Integer.toString(which));
                            //msg(unitsText.getText().toString());
                            if (unitsText.getText().toString().equalsIgnoreCase("mph")) {
                                sendUnits(Integer.toString(0));
                            } else if (unitsText.getText().toString().equalsIgnoreCase("kph")) {
                                sendUnits(Integer.toString(1));
                            }

                        }
                    });

                    builder.setNegativeButton("Cancel", null);


                } else if (itemNum == 1) { // max speed

                    // Set text box input as int only
                    final EditText input = new EditText(MainActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        boolean result = false;

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            maxSpeedText.setText(input.getText().toString());

                            if (input.getText().toString().length() != 0) {
                                sendInfo(input.getText().toString(), "M:");
                            }
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                } else if (itemNum == 2) { // magnets

                    builder.setSingleChoiceItems(getResources().getStringArray(R.array.magnet_array),
                            -1, new DialogInterface.OnClickListener() {



                        @Override
                        public void onClick(DialogInterface dialogInterface, int selectedIndex) {

                            magnetsText.setText(getResources()
                                    .getStringArray(R.array.magnet_array)[selectedIndex]);
                        }
                    });

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                        private DialogInterface mDialog;
                        boolean result = false;

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //result = sendInfo(Integer.toString(which), "N:");
                            //msg(magnetsText.getText().toString());
                            if (magnetsText.getText().toString().equalsIgnoreCase("1")) {
                                sendInfo(Integer.toString(1), "N:");
                            } else if (magnetsText.getText().toString().equalsIgnoreCase("2")) {
                                sendInfo(Integer.toString(2), "N:");
                            } else if (magnetsText.getText().toString().equalsIgnoreCase("4")) {
                                sendInfo(Integer.toString(4), "N:");
                            }
                            //result = sendInfo(magnetsText.getText().toString(), "N:");
                            //msg(Boolean.toString(result));
                        }

                    });
                    builder.setNegativeButton("Cancel", null);
                } else if (itemNum == 3 || itemNum == 4) { // max speed or final drive

                    // Set text box input as decimal only
                    final EditText input = new EditText(MainActivity.this);
                    input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        boolean result = false;

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (itemNum == 3) {

                                finalDriveText.setText(input.getText().toString());

                                if (input.getText().toString().length() != 0) {
                                    result = sendCalc(input.getText().toString(), "F:");
                                }

                            } else if (itemNum == 4) { // 4

                                meterRatioText.setText(input.getText().toString());

                                if (input.getText().toString().length() != 0) {
                                    result = sendCalc(input.getText().toString(), "S:");
                                }
                            }

                            if (result == false) {
                                msg("error");
                            }
                        }
                    });

                } else if (itemNum == 5) { // wheel size

                    // Set up layout for alert dialog
                    LinearLayout layout = new LinearLayout(MainActivity.this);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    // Set text box input as int only
                    final EditText tireSize1 = new EditText(MainActivity.this);
                    tireSize1.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    tireSize1.setHint("Wheel size 1");
                    layout.addView(tireSize1); // Notice this is an add method

                    final EditText tireSize2 = new EditText(MainActivity.this);
                    tireSize2.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    tireSize2.setHint("Wheel size 2");
                    layout.addView(tireSize2);

                    final EditText tireSize3 = new EditText(MainActivity.this);
                    tireSize3.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    tireSize3.setHint("Wheel size 3");
                    layout.addView(tireSize3); // Another add method

                    // Set layout for alert dialog
                    builder.setView(layout);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            boolean result = false;

                            wheelSizeText.setText(tireSize1.getText().toString() + ", " +
                                    tireSize2.getText().toString() + ", " +
                                    tireSize3.getText().toString());

                            if ((tireSize1.getText().toString().length() != 0) &&
                            (tireSize2.getText().toString().length() != 0)  &&
                                    (tireSize3.getText().toString().length() != 0)) {

                                result = sendWheelSizeCalc(tireSize1.getText().toString(),
                                        tireSize2.getText().toString(),
                                        tireSize3.getText().toString(),
                                        unitsText.getText().toString());
                            }

                            if (result == false) {
                                msg("error");
                            }
                        }
                    });

                } else if (itemNum == 6) { // wheel circumference

                    // Set up layout for alert dialog
                    LinearLayout layout = new LinearLayout(MainActivity.this);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    // Set text box input as decimal only
                    final EditText wheelCirc = new EditText(MainActivity.this);
                    wheelCirc.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    wheelCirc.setHint("Wheel circumference");
                    layout.addView(wheelCirc); // Notice this is an add method

                    // Set up radio button for units
                    final RadioButton circInch = new RadioButton(MainActivity.this);
                    circInch.setText("Inch (in)");
                    layout.addView(circInch);

                    final RadioButton circCM = new RadioButton(MainActivity.this);
                    circCM.setText("Centimeter (cm)");
                    layout.addView(circCM);

                    // Set layout for alert dialog
                    builder.setView(layout);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            boolean result = false;

                            // Check which radio button was clicked
                            if (circInch.isChecked()) {
                                wheelUnit = "inch";
                                circCM.setChecked(false); // Make sure the other option is unchecked
                            } else {
                                wheelUnit = "cm";
                                circInch.setChecked(false);
                            }

                            wheelCircText.setText(wheelCirc.getText().toString() + " " + wheelUnit);

                            if (wheelCirc.getText().toString().length() != 0) {
                                result = sendWheelCircCalc(wheelCirc.getText().toString(), wheelUnit);
                            }

                            if (result == false) {
                                msg("error");
                            }
                        }
                    });

                }
                builder.show();

                // Initialize a TextView for ListView each Item
                tv = view.findViewById(android.R.id.text1);

                // Set the text color of TextView (ListView Item)
                tv.setTextColor(Color.WHITE);
            }
        });
    }*/

}

