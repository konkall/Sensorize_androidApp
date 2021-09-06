package com.collisiondetector.project_anaptujh.Mqtt;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.collisiondetector.project_anaptujh.MainActivity;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttConnection implements MqttCallback{

    private MqttClient client;
    private String BROKER = null;
    MemoryPersistence memPer;

    String ipValue, portValue;

    private MsgQueue messageQ = null;

    public MqttConnection( ){}

    public MqttConnection( MsgQueue obj){

        MainActivity.getInstance().setMessageReceiverListener(MainActivity.getInstance());
        this.messageQ = obj;
    }


    public void connect() {
        memPer = new MemoryPersistence();

        try {

            client = new MqttClient(getBroker(), MqttClient.generateClientId(), null);
            client.setCallback(this);

        } catch (MqttException e1) {
            e1.printStackTrace();
        }
        MqttConnectOptions options = new MqttConnectOptions();
        try {
            options.setConnectionTimeout(1);
            client.connect(options);

        } catch (MqttException e) {
            Log.d(getClass().getCanonicalName(), "Connection attempt failed with reason code = " + e.getReasonCode() + ":" + e.getCause());
        }

    }




    public void disconnect() {
        if (client.isConnected()) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.getMessage();
            }
        }
    }

    public boolean isConnected() {
        if(client != null){return client.isConnected();}
        return false;
    }


    public void publish(String topic, String m) {
        try {
            MqttMessage message = new MqttMessage();
            message.setQos(2);
            message.setPayload(m.getBytes());
            client.publish(topic, message);
        } catch (MqttException e) {
            Log.d(getClass().getCanonicalName(), "Publish failed with reason code = " + e.getReasonCode());        }
    }

    public void subscribe(String topic, int Qos) {
        try {

            client.subscribe(topic, Qos);
        } catch (MqttException e) {
            Log.d(getClass().getCanonicalName(), "Subscribe failed with reason code = " + e.getReasonCode());        }
    }

    private String getBroker(){
        SharedPreferences prefs =  MainActivity.getInstance().getSharedPreferences("com.collisiondetector.project_anaptujh.sharedPrefs", Context.MODE_PRIVATE);
        ipValue = prefs.getString("ip", ipValue);
        portValue = prefs.getString("port", portValue);
        BROKER = "tcp://" + ipValue + ":" + portValue;

        return BROKER;
    }

    public String getMessage(){
        return messageQ.get();
    }


    @Override
    public void connectionLost(Throwable cause) {
        Log.d("MQTT", "MQTT Server connection lost" + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        String msg = new String(message.getPayload());

        messageQ.put(msg);

        Log.d("MQTTsub", "Message arrived:" + topic + ":" + msg);


    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d("MQTT", "Delivery complete");
    }
}