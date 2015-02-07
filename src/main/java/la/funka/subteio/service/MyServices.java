package la.funka.subteio.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Niger on 03/02/2015.
 */
public class MyServices extends Service {

    //private int mInterval = 900000;
    private int mInterval = 30000;

    @Override
    public void onCreate() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                myTaskRepeat();
            }
        }, 0, mInterval);
    }

    public void myTaskRepeat(){
        System.out.println("Se ejecuta el TASK");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
