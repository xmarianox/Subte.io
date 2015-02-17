package la.funka.subteio.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Util {

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
