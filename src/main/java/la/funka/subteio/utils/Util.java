package la.funka.subteio.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Util {

    Context context;

    public Util(Context context) {
        this.context = context;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        } else {
            return true;
        }
    }

    public double convertStringToDouble(String str){
        try {
            double doubleNum = Double.parseDouble(str);
            return doubleNum;
        }catch (Exception err){
            return 0.0;
        }
    }

    public double calculateFrequency(String str){
        double frequency = convertStringToDouble(str);
        if (frequency != 0.0){
            frequency = frequency / 60.0;
        }
        return frequency;
    }
}
