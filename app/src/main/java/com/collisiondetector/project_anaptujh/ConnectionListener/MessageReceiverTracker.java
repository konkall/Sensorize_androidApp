package com.collisiondetector.project_anaptujh.ConnectionListener;

public class MessageReceiverTracker
{


    public static  OnMessageReceivedListener listener;
    private String value;

    public MessageReceiverTracker() {
        super();
    }

    public String get()
    {
        return value;
    }

    public void set(String value)
    {
        this.value = value;

        if(listener != null)
        {
            listener.onMessageChanged(value);
        }
    }


    public interface OnMessageReceivedListener
    {
        void onMessageChanged(String newValue);
    }
}