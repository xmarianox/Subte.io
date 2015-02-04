package la.funka.subteio.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import la.funka.subteio.TraerEstadoSubteTask;

/**
 * Service tutorial
 * http://www.vogella.com/tutorials/AndroidServices/article.html
 * https://www.udacity.com/course/viewer#!/c-ud853/l-1614738811/e-1664298683/m-1664298684
 * */
public class UpdaterService extends IntentService {
    // Log
    private final static String LOG_TAG = UpdaterService.class.getSimpleName();
    private int mInterval = 30000;

    public UpdaterService() {
        super("UpdaterService");
    }

    @Override
    public void onCreate() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                loopTasck();
            }
        }, 0, mInterval);
    }

    private void loopTasck() {
        Log.d(LOG_TAG, "Se ejecuta el loopTask");
        new TraerEstadoSubteTask().execute("http://www.metrovias.com.ar/Subterraneos/Estado?site=Metrovias");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }
}
