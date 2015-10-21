package com.example.steve.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Created by Steve on 10/8/2015.
 */
public class CrimePagerActivity extends AppCompatActivity
    implements CrimeFragment.Callbacks{
    private static final String EXTRA_CRIME_ID = "extra_crime_id";
    private static final String TAG = "CrimePagerActivity";

    private ViewPager mViewPager;
    private List<Crime> mCrimes;

    public static Intent newIntent(Context sourceContext, UUID crimeId){
        Intent it = new Intent(sourceContext, CrimePagerActivity.class);
        it.putExtra(EXTRA_CRIME_ID, crimeId);
        return it;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);

        mViewPager = (ViewPager) findViewById(R.id.activity_crime_pager_view_pager);
        mCrimes = CrimeLab.getInstance(this).getCrimes();

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                Crime crime = mCrimes.get(position);
                return CrimeFragment.newIntent(crime.getId());
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Crime crime = mCrimes.get(position);
                if (crime.getTitle() != null) {
                    setTitle(crime.getTitle());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        for (int i = 0; i < mCrimes.size(); i++) {
            if(mCrimes.get(i).getId().equals(crimeId)){
                mViewPager.setCurrentItem(i);
                if(i == 0){ //solve onPageselected not called for first page creating issue
                    Crime crime = mCrimes.get(0);
                    if (crime.getTitle() != null) {
                        setTitle(crime.getTitle());
                    }
                }
                break;
            }
        }
    }


    @Override
    public void onCrimeUpdated(Crime crime) {

    }
}












