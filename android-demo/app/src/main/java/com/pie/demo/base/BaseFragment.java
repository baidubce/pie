package com.pie.demo.base;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {

    protected Activity mActivity;

    public void onAttach(Context paramContext) {
        super.onAttach(paramContext);
        this.mActivity = ((Activity) paramContext);
    }
}
