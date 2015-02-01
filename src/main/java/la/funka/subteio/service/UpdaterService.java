package la.funka.subteio.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import la.funka.subteio.TraerEstadoSubteTask;

/**
 * Service tutorial
 * https://www.youtube.com/watch?v=yfWsp9IHX1Y
 *
 * http://www.vogella.com/tutorials/AndroidBackgroundProcessing/article.html
 * */
public class UpdaterService extends Service {
    private final static String LOG_TAG = UpdaterService.class.getSimpleName();
    private Updater updater;

    @Override
    public void onCreate() {
        super.onCreate();

        updater = new Updater();
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public synchronized void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        // Start the updater
        if (!updater.isRunning()) {
            updater.start();
            updater.isRunning = true;
        }

        Log.d(LOG_TAG, "onStart");
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();

        // Stop the updater
        if (updater.isRunning()) {
            updater.interrupt();
        }
        updater = null;

        Log.d(LOG_TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    ////// Updater Thread
    class Updater extends Thread {
        static final long DELAY = 60000; // one minute
        private boolean isRunning = false;

        public Updater () {
            super("Updater");
        }

        @Override
        public void run() {
            isRunning = true;
            while (isRunning) {
                try {
                    // Log
                    Log.d(LOG_TAG, "Updater Running!");

                    //new TraerEstadoSubteTask().execute("http://www.metrovias.com.ar/Subterraneos/Estado?site=Metrovias");

                    // Sleep
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    // Interrupted
                    e.printStackTrace();
                    isRunning = false;
                }
            } //while
        }

        public boolean isRunning() {
            return this.isRunning();
        }
    }

}
