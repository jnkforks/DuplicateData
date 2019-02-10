package com.example.sudoajay.duplication_data.Main_Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public Home() {
        // Required empty public constructor
    }

    public Home createInstance(Main_Navigation main_navigation){
        this.main_navigation = main_navigation;
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
         layout =layout =inflater.inflate(R.layout.main_fragment_home, container, false);

        // custom progress bar
        CircularProgressBar circularProgressBar = layout.findViewById(R.id.circularProgressBar);
        int animationDuration = 2500; // 2500ms = 2,5s
        circularProgressBar.setProgressWithAnimation(65, animationDuration); // Default duration = 1500ms

        // Reference and Create Object
        Reference();

        return layout;
    }

    // Reference and Create Object
    private void Reference() {


    }


}

