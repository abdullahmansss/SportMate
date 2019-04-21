package marwa.hameed.sportmate;

import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import marwa.hameed.sportmate.Fragments.JoinedActivitiesFragment;
import marwa.hameed.sportmate.Fragments.MyActivitiesFragment;

public class MyActivity extends AppCompatActivity
{
    FragmentPagerAdapter fragmentPagerAdapter;
    ViewPager viewPager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabs);

        fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
        {
            private final Fragment[] mFragments = new Fragment[]
                    {
                            new MyActivitiesFragment(),
                            new JoinedActivitiesFragment()
                    };
            private final String[] mFragmentNames = new String[]
                    {
                            "MY ACTIVITIES",
                            "JOINED ACTIVITIES",
                    };

            @Override
            public Fragment getItem(int position)
            {
                return mFragments[position];
            }

            @Override
            public int getCount()
            {
                return mFragments.length;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position)
            {
                return mFragmentNames[position];
            }
        };

        viewPager.setAdapter(fragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}
