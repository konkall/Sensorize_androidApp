package com.collisiondetector.project_anaptujh.Mqtt;

import java.util.*;

public class MsgQueue {

 boolean value = true;
    Queue<String> queue = new LinkedList<String>();



    public synchronized String get () {

        String msg = queue.poll();
        value = true;
        return msg;

    }

    public synchronized void put (String msg ) {

        queue.add(msg);
        value = false;
    }


}