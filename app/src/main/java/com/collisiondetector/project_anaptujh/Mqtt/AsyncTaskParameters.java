package com.collisiondetector.project_anaptujh.Mqtt;


public class AsyncTaskParameters {
    String message;
    MqttConnection mqttConnection;

    public void setValues(String message, MqttConnection mqttConnection){
        this.message = message;
        this.mqttConnection = mqttConnection;

    }

}
