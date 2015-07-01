package la.funka.subteio;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.Window;

/**
 * Created by RetinaPro on 1/7/15.
 */
public class WindowCompatUtils {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarcolor(Window window, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(color);
        }
    }
}
