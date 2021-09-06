package com.collisiondetector.project_anaptujh.Mqtt;

import android.util.Log;


public class SubscriberThread extends Thread {
    private String threadName = null;
    private MqttConnection mqttCon = null;
    private String deviceId = null;

    public SubscriberThread(String threadName, MqttConnection mqttCon, String deviceId) {
        this.threadName = threadName;
        this.mqttCon = mqttCon;
        this.deviceId = deviceId;
    }

    @Override
    public void run() {
        String topic = "Android/" + deviceId;
        Log.d("MQTTsub", "Hello from " + threadName + " and topic " + topic);
        int qos = 2;



        //Connect	client	to	MQTT	Broker
            if (!mqttCon.isConnected()) {
                mqttCon.connect();

            }
        //Subscribe	to	a	topic
            Log.d("MQTTsub", "Subscribing	to	topic	\"" + topic + "\"	qos " + qos);
            if (mqttCon.isConnected()) {
                mqttCon.subscribe(topic, qos);
            }


    }
}