package la.funka.subteio.fragments;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import la.funka.subteio.R;

/**
 * Created by Mariano Molina on 28/08/2015.
 * Twitter: @xsincrueldadx
 */
public class AboutFragment extends Fragment {


    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /**
         * Set app version de manera programatica.
         */
        Context context = getActivity().getApplicationContext();
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        String myVersionName = "not available";

        try {
            myVersionName = packageManager.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Version text
        TextView textViewVersion = (TextView) getActivity().findViewById(R.id.app_version_text);
        textViewVersion.setText("Versión " + myVersionName);

        // App Image
        ImageView imageView = (ImageView) getActivity().findViewById(R.id.app_logo);
        Picasso.with(getActivity()).load(R.drawable.ic_icon_app).fit().centerCrop().into(imageView);

        ObjectAnimator moveAnim = ObjectAnimator.ofFloat(imageView, "Y", 300);
        moveAnim.setDuration(2000);
        moveAnim.setInterpolator(new BounceInterpolator());
        moveAnim.start();

        // Nano
        TextView dev1 = (TextView) getActivity().findViewById(R.id.twitter_dev_1);
        dev1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/xsincrueldadx"));
                getActivity().startActivity(linkIntent);
            }
        });

        // Chamo
        TextView dev2 = (TextView) getActivity().findViewById(R.id.twitter_dev_2);
        dev2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/emiliovirtual"));
                getActivity().startActivity(linkIntent);
            }
        });

        // Eze
        TextView dev3 = (TextView) getActivity().findViewById(R.id.twitter_dev_3);
        dev3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/itswhoyall"));
                getActivity().startActivity(linkIntent);
            }
        });

    }
}
