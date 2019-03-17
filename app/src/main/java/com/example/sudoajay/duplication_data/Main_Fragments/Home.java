package com.example.sudoajay.duplication_data.Main_Fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sudoajay.duplication_data.Main_Navigation;
import com.example.sudoajay.duplication_data.R;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class Home extends Fragment {

    // global variable
    private Main_Navigation main_navigation;
    private View layout;
    private CircularProgressBar circularProgressBar;
    private final int animationDuration = 2000;
    private TextView textViewInternal2,textViewInternal3;

    public Home() {
        // Required empty public constructor
    }

    public Home createInstance(Main_Navigation main_navigation){
        this.main_navigation = main_navigation;
        return this;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
         layout  =inflater.inflate(R.layout.main_fragment_home, container, false);

        // Reference and Create Object
        Reference();

        // custom progress bar

        circularProgressBar.setProgressWithAnimation(65, animationDuration); // Default duration = 1500ms

        return layout;
    }

    // Reference and Create Object
    private void Reference() {

        textViewInternal2 = layout.findViewById(R.id.textViewInternal2);
        textViewInternal3 = layout.findViewById(R.id.textViewInternal3);
         circularProgressBar = layout.findViewById(R.id.circularProgressBarInternal);
    }

    // on click listener
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void OnClick(View v){
        switch (v.getId()){
            case R.id.cardViewInternal3:
                if(circularProgressBar.getColor() == -10044566){
                    circularProgressBar.setBackgroundColor(ContextCompat.getColor
                            (Objects.requireNonNull(getContext()),R.color.progressBarColor));
                    circularProgressBar.setColor(ContextCompat.getColor
                            (Objects.requireNonNull(getContext()),R.color.background_Progress_Bar_Color));
                    circularProgressBar.setProgressWithAnimation(35, 0); // Default duration = 1500ms

                    textViewInternal2.setText("9.7 GB");
                    textViewInternal3.setText("Free");

                }else {
                    circularProgressBar.setBackgroundColor(ContextCompat.getColor
                            (Objects.requireNonNull(getContext()),R.color.background_Progress_Bar_Color));
                    circularProgressBar.setColor(ContextCompat.getColor
                            (Objects.requireNonNull(getContext()),R.color.progressBarColor));

                    circularProgressBar.setProgressWithAnimation(65, 0); // Default duration = 1500ms

                    textViewInternal2.setText("14.3 GB");
                    textViewInternal3.setText("Used");

                }
                break;
        }
    }


}

