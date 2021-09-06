package com.collisiondetector.project_anaptujh;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.collisiondetector.project_anaptujh.ConnectionListener.ConnectivityReceiver;
import com.collisiondetector.project_anaptujh.ConnectionListener.OnlineModeTracker;
import com.collisiondetector.project_anaptujh.Mqtt.AsyncTaskParameters;
import com.collisiondetector.project_anaptujh.Mqtt.Connect_and_PublishTask;
import com.collisiondetector.project_anaptujh.ConnectionListener.MessageReceiverTracker;
import com.collisiondetector.project_anaptujh.Mqtt.MqttConnection;
import com.collisiondetector.project_anaptujh.Mqtt.MsgQueue;
import com.collisiondetector.project_anaptujh.Mqtt.SubscriberThread;
import com.collisiondetector.project_anaptujh.Settings.Main2Activity;
import com.collisiondetector.project_anaptujh.Settings.OnlineModeSettings;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.widget.Toast.LENGTH_SHORT;


public class MainActivity extends AppCompatActivity
        implements ConnectivityReceiver.ConnectivityReceiverListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnlineModeTracker.OnModeChangeListener,
        MessageReceiverTracker.OnMessageReceivedListener {

    //Basic variable declarations
    TextView textLIGHT_available, textLIGHT_reading, textACC_available, textGRAV_available, textACC_reading, textGRAV_reading, textSeeklight, textSeekgrav;
    //Sensor declarations
    private SensorManager mySensorManager = null;
    private Sensor LightSensor = null;
    private Sensor AccelerationSensor = null;
    private Sensor GravitySensor = null;
    //Switch variables declarations
    private TextView switchStatus;
    public Switch mySwitch;
    private TextView switchOnlineStatus;
    public Switch myOnlineSwitch;
    //Sensor data variable declarations
    float lastUpdate = 0;
    float lightData1 = 0;
    float lightData2 = 0;
    float lightData3 = 0;
    float critical_value = 0;
    double accX, accY, accZ = 0;
    double gX, gY, gZ = 0;
    boolean criticalPosition = false;
    //Seekbar variable declarations
    int frequency;
    int sensivity;
    //Handler for periodic runnable declaration
    Handler handler = new Handler();
    //Online mode Variable
    boolean onlineMode = false;
    boolean subThreadRunning = false;
    //audio declaration
    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

    private static MainActivity mInstance;
    LocationManager manager = null;
    Double longitude = null;
    Double latitude = null;

    Calendar c = null;
    String deviceId = null;
    String currentDate = null;
    SimpleDateFormat df = null;


    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    MqttConnection mqttPubCon = null;
    MqttConnection mqttSubCon = null;

    //boolean currentState = false;
    AsyncTaskParameters taskParams = null;
    MsgQueue msgQ = null;
    SubscriberThread subThread = null;

    OnlineModeTracker onlineModeTracker;
    public MessageReceiverTracker messageReceiverTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setLogo(R.drawable.app_logo);
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        onlineModeTracker = new OnlineModeTracker();
        messageReceiverTracker = new MessageReceiverTracker();

        mInstance = this;
        MainActivity.getInstance().setConnectivityListener(this);
        MainActivity.getInstance().setOnlineModeListener(this);
        MainActivity.getInstance().setMessageReceiverListener(this);

        //new connection and values for it.
        msgQ = new MsgQueue();
        mqttPubCon = new MqttConnection();
        mqttSubCon = new MqttConnection(msgQ);
        taskParams = new AsyncTaskParameters();


        //get device unique id.
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        //Prepare to get date/time.
        c = Calendar.getInstance();
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");



        onlineModeTracker.set(false);

        //Setting a location manager
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        //checks availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }

        //main screen data values
        textLIGHT_available
                = (TextView) findViewById(R.id.LIGHT_available);
        textACC_available
                = (TextView) findViewById(R.id.ACC_available);
        textGRAV_available
                = (TextView) findViewById(R.id.GRAV_available);

        textLIGHT_reading
                = (TextView) findViewById(R.id.LIGHT_reading);
        textACC_reading
                = (TextView) findViewById(R.id.ACC_reading);
        textGRAV_reading
                = (TextView) findViewById(R.id.GRAVITY_reading);

        textSeeklight
                = (TextView) findViewById(R.id.aa);

        textSeekgrav
                = (TextView) findViewById(R.id.bb);



        /*Setting a sensor manager and initializing sensor listeners for light,
         *accelerometer and gravity sensor IF they exist.
         */
        mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        LightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        AccelerationSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        GravitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        if (LightSensor != null) {
            textLIGHT_available.setText("Sensor.TYPE_LIGHT Available");

            mySensorManager.registerListener(
                    mSensorListener,
                    LightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            textLIGHT_available.setText("Sensor.TYPE_LIGHT NOT Available");
        }

        if (AccelerationSensor != null) {
            textACC_available.setText("Sensor.TYPE_ACCELERATION Available");
            mySensorManager.registerListener(
                    mSensorListener,
                    AccelerationSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);


        } else {
            textACC_available.setText("Sensor.TYPE_ACCELERATION NOT Available");
        }

        if (GravitySensor != null) {
            textGRAV_available.setText("Sensor.TYPE_GRAVITY Available");
            mySensorManager.registerListener(
                    mSensorListener,
                    GravitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);


        } else {
            textGRAV_available.setText("Sensor.TYPE_GRAVITY NOT Available");
        }


        //Initializing seekbar values via shared preferences
        SharedPreferences prefs = this.getSharedPreferences(
                "com.collisiondetector.project_anaptujh.sharedPrefs", Context.MODE_PRIVATE);

        prefs.edit().putInt("frequency_Seekbar", 800).apply();
        frequency = 800;
        prefs.edit().putInt("sensivity_Seekbar", 80).apply();
        sensivity = 80;
        prefs.edit().putString("ip", prefs.getString("ip", "localhost")).apply();
        prefs.edit().putString("port", prefs.getString("port", "1883")).apply();


        //Set a Click Listener for Switch Button
        switchStatus = (TextView) findViewById(R.id.switchtext);
        mySwitch = (Switch) findViewById(R.id.mySwitch);
        mySwitch.setChecked(true);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean bChecked) {

                if (bChecked) {
                    switchStatus.setText("Switch is currently ON");
                    if (LightSensor != null && AccelerationSensor != null && GravitySensor != null) {
                        mySensorManager.registerListener(
                                mSensorListener,
                                LightSensor,
                                SensorManager.SENSOR_DELAY_NORMAL);

                        mySensorManager.registerListener(
                                mSensorListener,
                                AccelerationSensor,
                                SensorManager.SENSOR_DELAY_NORMAL);

                        mySensorManager.registerListener(
                                mSensorListener,
                                GravitySensor,
                                SensorManager.SENSOR_DELAY_NORMAL);

                    }
                } else {
                    switchStatus.setText("Switch is currently OFF");
                    mySensorManager.unregisterListener(mSensorListener);
                }

            }
        });

        if (mySwitch.isChecked()) {
            switchStatus.setText("Switch is currently ON");


        } else {
            switchStatus.setText("Switch is currently OFF");

        }

        //Set a Click Listener for Online Switch Button
        switchOnlineStatus = (TextView) findViewById(R.id.switchOnlinetext);
        myOnlineSwitch = (Switch) findViewById(R.id.myOnlineSwitch);
        //myOnlineSwitch.setChecked(false);
        myOnlineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean bChecked) {

                if (bChecked) {
                    switchOnlineStatus.setText("Online Mode ENABLED");
                    if (!ConnectivityReceiver.isConnected()) {
                        AlertMessageNoInternetAccess();
                    }
                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        AlertMessageNoGPS();
                    }
                    onlineMode = true;
                } else {
                    switchOnlineStatus.setText("Online Mode DISABLED");
                    onlineMode = false;
                    onlineModeTracker.set(false);
                    Toast toast = Toast.makeText(MainActivity.getInstance().getApplicationContext(), "Not Connected", LENGTH_SHORT);
                    toast.show();
                }

            }
        });

        if (myOnlineSwitch.isChecked()) {
            switchOnlineStatus.setText("Online Mode ENABLED");


        } else {
            switchOnlineStatus.setText("Online Mode DISABLED");

        }


        //Automatic assignment to online/offline mode at the app start
        if (ConnectivityReceiver.isConnected() && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            myOnlineSwitch.setChecked(true);
            onlineMode = true;
            Toast toast = Toast.makeText(getApplicationContext(), "Online Mode Activated", LENGTH_SHORT);
            toast.show();
        } else {
            myOnlineSwitch.setChecked(false);
            onlineMode = false;
            Toast toast = Toast.makeText(getApplicationContext(), "Offline Mode Activated", LENGTH_SHORT);
            toast.show();
        }

        //starting runnable function
        handler.post(runnableCode);
    }


    /*Prints the data values
     *AND
     *checks if our data suggest a collision
     */
    private Runnable runnableCode = new Runnable() {


        @Override
        public void run() {
            //String a = messageReceiverTracker.get();
            textLIGHT_reading.setText("LIGHT SENSOR: " + String.format("%.2f", lightData1) + " LUX " );
            textACC_reading.setText("ACC SENSOR: x: " + String.format("%.2f", accX) + " m/s²" + " y: " + String.format("%.2f", accY) + " m/s²" + " z: " + String.format("%.2f", accZ) + " m/s²");
            textGRAV_reading.setText("GRAVITY SENSOR: x: " + String.format("%.2f", gX) + " m/s²" + " y: " + String.format("%.2f", gY) + " m/s²" + " z: " + String.format("%.2f", gZ) + " m/s²");

            if (onlineMode) {
                if (!ConnectivityReceiver.isConnected() || !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    myOnlineSwitch.setChecked(false);
                    onlineMode = false;
                    Toast toast = Toast.makeText(getApplicationContext(), "Offline Mode Activated", LENGTH_SHORT);
                    toast.show();


                } else {
                    //Updates longtitute and latitude
                    updateLocation();
                    //gets current date/time
                    //Prepare to get date/time.
                    c = Calendar.getInstance();
                    //df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    currentDate = df.format(c.getTime());

                    String message = deviceId + ";" + "lightsensor" + ";" + String.format("%.2f", lightData1) + ";" + String.format("%.2f", lightData2) + ";" + String.format("%.2f", lightData3) + ";" +
                            "accsensor" + ";" + String.format("%.2f", accX) + ";" + String.format("%.2f", accY) + ";" + String.format("%.2f", accZ)
                            + ";" + "gravitysensor" + ";" + String.format("%.2f", gX) + ";" + String.format("%.2f", gY) + ";" + String.format("%.2f", gZ)
                            + ";" + currentDate + ";" + String.valueOf(longitude) + ";" + String.valueOf(latitude);


                    taskParams.setValues(message,mqttPubCon);

                    Connect_and_PublishTask cNpt = new Connect_and_PublishTask();
                    cNpt.execute(taskParams);


                    if(mqttPubCon.isConnected() && !onlineModeTracker.get()){
                        onlineModeTracker.set(true);
                        Toast toast = Toast.makeText(MainActivity.getInstance().getApplicationContext(), "Connected", LENGTH_SHORT);
                        toast.show();

                    }else if(!mqttPubCon.isConnected() && onlineModeTracker.get()){
                        onlineModeTracker.set(false);
                        Toast toast = Toast.makeText(MainActivity.getInstance().getApplicationContext(), "Not Connected", LENGTH_SHORT);
                        toast.show();
                    }



                    messageReceiverTracker.set(mqttSubCon.getMessage());


                }

            } else {

                //checks for collision probability
                if (criticalPosition == false) {
                    if ((lightData3 * (sensivity / 100.0) > lightData2 && lightData2 * (sensivity / 100.0) > lightData1 && (accZ - gZ) > 0.6) || (accZ - gZ) > 30) {
                        //BEEPING sound
                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500);
                        //WARNING icon
                        LayoutInflater inflater = getLayoutInflater();
                        View view = inflater.inflate(R.layout.image_toast,
                                (ViewGroup) findViewById(R.id.image_toast_layout));


                        final Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.setView(view);
                        toast.show();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                toast.cancel();
                            }
                        }, 500);
                        ////
                        critical_value = lightData1;
                        criticalPosition = true;
                    }
                }
                //if a collision probability is detected beeps while the chance persists
                else if (criticalPosition == true) {
                    if (critical_value * (1 + (100 - sensivity) / 100.0) > lightData1 && critical_value * (sensivity / 100.0) < lightData1) {
                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500);

                        LayoutInflater inflater = getLayoutInflater();
                        View view = inflater.inflate(R.layout.image_toast,
                                (ViewGroup) findViewById(R.id.image_toast_layout));


                        final Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.setView(view);
                        toast.show();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                toast.cancel();
                            }
                        }, 500);
                    } else {
                        criticalPosition = false;
                    }

                }
            }
            handler.postDelayed(runnableCode, 1000 * (1 / frequency));
        }
    };

    public static synchronized MainActivity getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (!isConnected) {
            onlineMode = false;
            myOnlineSwitch.setChecked(false);
        }
    }

    public void setOnlineModeListener(OnlineModeTracker.OnModeChangeListener listener) {
        OnlineModeTracker.listener = listener;
    }

    @Override
    public void onOnlineModeChanged(boolean currentState) {
        if (currentState) {
            subThread = new SubscriberThread("Subscribe Thread", mqttSubCon, deviceId);
            subThread.start();
            subThreadRunning = true;
        }
        else if(!currentState && subThreadRunning){
            mqttSubCon.disconnect();
            subThreadRunning = false;
        }
    }

    public void setMessageReceiverListener(MessageReceiverTracker.OnMessageReceivedListener listener) {
        MessageReceiverTracker.listener = listener;
    }

    @Override
    public void onMessageChanged(String newMessage) {
        if(newMessage != null){
            if(newMessage.equals("0")){
                //BEEPING sound and toast
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 250);
                final Toast toast = Toast.makeText(MainActivity.getInstance().getApplicationContext(), "WARNING: COLLISION MAY HAPPEN", Toast.LENGTH_SHORT);
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                    }
                }, 250);

            }
            else if(newMessage.equals("1")){
                //BEEPING sound and toast
                toneG.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 250);

                final Toast toast = Toast.makeText(MainActivity.getInstance().getApplicationContext(), "WARNING: IMMINENT COLLISION", Toast.LENGTH_SHORT);
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                    }
                }, 250);

            }
        }
    }

    //Function that "listens" to the sensors
    public SensorEventListener mSensorListener
            = new SensorEventListener() {

        //no need to be implemented
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        //function that collects the data from listeners
        @Override
        public void onSensorChanged(SensorEvent event) {


            if (lastUpdate != 0) {


                //collects data every 1/frequency
                if ((event.timestamp - lastUpdate) / 1000000000 < 1 / frequency) {
                    return;
                }
                if (event.sensor.getType() == Sensor.TYPE_LIGHT) {

                    lightData3 = lightData2;
                    lightData2 = lightData1;
                    lightData1 = event.values[0];

                } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                    if (Math.abs(event.values[0] - accX) > 0.1) {
                        accX = event.values[0];
                    }
                    if (Math.abs(event.values[1] - accY) > 0.1) {
                        accY = event.values[1];
                    }
                    if (Math.abs(event.values[2] - accZ) > 0.1) {
                        accZ = event.values[2];
                    }

                } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {

                    if (Math.abs(event.values[0] - gX) > 0.1) {
                        gX = event.values[0];
                    }
                    if (Math.abs(event.values[1] - gY) > 0.1) {
                        gY = event.values[1];
                    }
                    if (Math.abs(event.values[2] - gZ) > 0.1) {
                        gZ = event.values[2];
                    }

                }
            }


            lastUpdate = event.timestamp;
        }


    };

    //Upper-right home button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_id:
                if(onlineMode == false){
                    Intent i = new Intent(MainActivity.this, Main2Activity.class); // starts an intent to get data from the seekbars
                    startActivityForResult(i, 1);
                }
                else if(onlineMode == true){
                    Intent i = new Intent(MainActivity.this, OnlineModeSettings.class); // starts an intent to get data from the seekbars
                    startActivityForResult(i, 2);
                }


                return true;

            case R.id.exit_id:
                mySensorManager.unregisterListener(mSensorListener);
                System.exit(0);
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Confirmation!");
        builder.setMessage("Are you sure you want to quit?");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mySensorManager.unregisterListener(mSensorListener);
                System.exit(0);

            }
        });
        builder.setNegativeButton("no", null);
        builder.show();
    }

    //collects seekbar data
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                Bundle extras = data.getExtras();
                frequency = extras.getInt("frequency");
                sensivity = extras.getInt("sensivity");
                textSeeklight.setText("frequency: " + frequency);
                textSeekgrav.setText("sensivity: " + sensivity);

            }
        }
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {

                Bundle extras = data.getExtras();
                frequency = extras.getInt("frequency");
                textSeeklight.setText("frequency: " + frequency);

            }
        }
    }

    private void AlertMessageNoGPS() {


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS is disabled, please enable before switching to this mode.")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();

                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }

    private void AlertMessageNoInternetAccess() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Internet Access is disabled, please enable before switching to this mode.")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }
    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        updateLocation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    /**
     * Method to display the location on UI
     * */
    private void updateLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        }

    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }

        }
    }


}
