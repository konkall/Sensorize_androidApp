package com.collisiondetector.project_anaptujh.Mqtt;


import android.os.AsyncTask;

public class Connect_and_PublishTask extends AsyncTask<AsyncTaskParameters, Void, Void> {


    @Override
    protected void onPreExecute(){

    }

    @Override
    protected Void doInBackground(AsyncTaskParameters... params){

        if(!params[0].mqttConnection.isConnected()){
            params[0].mqttConnection.connect();
        }

        if(params[0].mqttConnection.isConnected()){
            params[0].mqttConnection.publish("Android/LocationData",params[0].message);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... voids){}

    @Override
    protected void onPostExecute(Void param){

    }

}