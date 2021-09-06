package com.collisiondetector.project_anaptujh.ConnectionListener;


public class OnlineModeTracker
{


    public static  OnModeChangeListener listener;
    private boolean value;

    public OnlineModeTracker() {
        super();
    }

    public boolean get()
    {
        return value;
    }

    public void set(boolean value)
    {
        this.value = value;

        if(listener != null)
        {
            listener.onOnlineModeChanged(value);
        }
    }


    public interface OnModeChangeListener
    {
        void onOnlineModeChanged(boolean newValue);
    }
}