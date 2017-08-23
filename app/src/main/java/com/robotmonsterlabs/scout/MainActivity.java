package com.robotmonsterlabs.scout;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get our actionbar reference
        ActionBar actionBar = getActionBar();

        // set a few of the basic parameters
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);

        // new bluetooth tab
        Tab tabBluetooth = actionBar
                .newTab()
                .setText(R.string.action_bluetooth)
                .setTabListener(new TabListener<BluetoothFragment>(this, "bluetooth", BluetoothFragment.class));

        // add it to the actionbar
        actionBar.addTab(tabBluetooth);

        // new wifi tab
        Tab tabWifi = actionBar
                .newTab()
                .setText(R.string.action_wifi)
                .setTabListener(new TabListener<WifiFragment>(this, "wifi", WifiFragment.class));

        // also add it to the actionbar
        actionBar.addTab(tabWifi);

        // Testing generics
        String str = "ed";
        GenericTester<String> gt = new GenericTester<String>(str);

    }

    public class GenericTester<String> {

        private String s ;

        public GenericTester(String st) {
            this.s = st ;
            Log.d("ScoutDebug", st.toString());
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_credits) {
            new OpenWebAddress().open(this, "http://www.joduplessis.com.com");
        }

        return super.onOptionsItemSelected(item);
    }


    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {

        private Fragment mFragment;
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;

        public TabListener (Activity activity, String tag, Class<T> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
        }

        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            // Check if the fragment is initialized
            if (mFragment == null) {
                // if it doesn't exist - create it
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                // add it to the root element of the view
                ft.add(R.id.fragment_container, mFragment, mTag);
            } else {
                // attach the fragment - if it exists
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                // get rid of the fragment
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            Log.d("ScoutDebug", "Reselected the tab");
        }
    }

}


