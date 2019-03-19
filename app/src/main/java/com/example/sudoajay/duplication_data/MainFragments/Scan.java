package com.example.sudoajay.duplication_data.MainFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sudoajay.duplication_data.MainNavigation;
import com.example.sudoajay.duplication_data.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class Scan extends Fragment {

    // global variable
    private MainNavigation main_navigation;
    private View layout;

    public Scan() {
        // Required empty public constructor
    }

    public Scan createInstance(MainNavigation main_navigation){
        this.main_navigation = main_navigation;
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
         layout =layout =inflater.inflate(R.layout.main_fragment_scan, container, false);

        // Reference and Create Object
        Reference();

        return layout;
    }

    // Reference and Create Object
    private void Reference() {


    }


}

