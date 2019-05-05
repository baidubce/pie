package com.pie.demo.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.pie.demo.R;
import com.pie.demo.view.fragment.AsrFragment;
import com.pie.demo.view.fragment.TtsFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private AsrFragment asrFragment;
    private BottomNavigationView mBnv;
    private ArrayList<Fragment> mList;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TtsFragment ttsFragment;

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_main);
        initPermission();
        initView();
        initLisener();
    }

    private void initPermission() {
        if (ActivityCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 100);
        }
    }

    private void initView() {
        this.mToolbar = ((Toolbar) findViewById(R.id.mToolbar));
        setSupportActionBar(this.mToolbar);
        this.mViewPager = ((ViewPager) findViewById(R.id.mViewPager));
        this.mBnv = ((BottomNavigationView) findViewById(R.id.mBnv));
        this.asrFragment = new AsrFragment();
        this.ttsFragment = new TtsFragment();
        this.mList = new ArrayList();
        this.mList.add(this.asrFragment);
        this.mList.add(this.ttsFragment);
        MyViewPagerAdapter localMyViewPagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager());
        this.mViewPager.setAdapter(localMyViewPagerAdapter);
    }

    private void initLisener() {

        this.mBnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull MenuItem paramAnonymousMenuItem) {
                switch (paramAnonymousMenuItem.getItemId()) {
                    default:
                        break;
                    case R.id.mTitleTts:
                        MainActivity.this.mToolbar.setTitle("百度TTS");
                        MainActivity.this.mViewPager.setCurrentItem(1, false);
                        MainActivity.this.asrFragment.isRecord();
                        break;
                    case R.id.mTitleAsr:
                        MainActivity.this.mToolbar.setTitle("百度ASR");
                        MainActivity.this.mViewPager.setCurrentItem(0, false);
                        MainActivity.this.hideSoftKeyboard();
                        MainActivity.this.ttsFragment.stopAll();
                }
                return false;
            }
        });

        this.mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            public void onPageScrollStateChanged(int paramAnonymousInt) {
            }

            public void onPageScrolled(int paramAnonymousInt1, float paramAnonymousFloat, int paramAnonymousInt2) {
            }

            public void onPageSelected(int paramAnonymousInt) {
                MainActivity.this.mBnv.getMenu().getItem(paramAnonymousInt).setChecked(true);
                switch (paramAnonymousInt) {
                    default:
                        break;
                    case 1:
                        MainActivity.this.mToolbar.setTitle("百度TTS");
                        break;
                    case 0:
                        MainActivity.this.mToolbar.setTitle("百度ASR");
                        MainActivity.this.hideSoftKeyboard();
                }
            }
        });
    }


    public void hideSoftKeyboard() {
        View localView = getCurrentFocus();
        if (localView != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(localView.getWindowToken(), 2);
        }
    }

    public void onRequestPermissionsResult(int paramInt, @NonNull String[] paramArrayOfString, @NonNull int[] paramArrayOfInt) {
        super.onRequestPermissionsResult(paramInt, paramArrayOfString, paramArrayOfInt);

    }

    class MyViewPagerAdapter extends FragmentPagerAdapter {


        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public int getCount() {
            return MainActivity.this.mList.size();
        }

        public Fragment getItem(int paramInt) {
            return (Fragment) MainActivity.this.mList.get(paramInt);
        }
    }
}
