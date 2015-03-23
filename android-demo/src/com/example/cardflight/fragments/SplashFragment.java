package com.example.cardflight.fragments;

import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.cardflight.R;
import com.getcardflight.models.CardFlight;

/**
 * Created by pcedrowski on 8/26/14.
 */
public class SplashFragment extends Fragment {

    private TextView sdkVersionText;
    private TextView androidOsText;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.splash_fragmant, container, false);

        sdkVersionText = (TextView) rootView.findViewById(R.id.sdk_version_text);
        androidOsText = (TextView) rootView.findViewById(R.id.android_version_text);

        setSplashContent();

        return rootView;
    }

    private void setSplashContent(){
        String sdkVersion;
        String androidVersion;

        sdkVersion = String.format("SDK %s", CardFlight.getVersion());

        androidVersion = String.format("Android %s", Build.VERSION.RELEASE);

        sdkVersionText.setText(sdkVersion);
        androidOsText.setText(androidVersion);
    }
}
