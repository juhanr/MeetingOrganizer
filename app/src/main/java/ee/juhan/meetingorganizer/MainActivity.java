package ee.juhan.meetingorganizer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import ee.juhan.meetingorganizer.fragments.LoginFragment;
import ee.juhan.meetingorganizer.fragments.MeetingsListFragment;
import ee.juhan.meetingorganizer.fragments.NewMeetingFragment;
import ee.juhan.meetingorganizer.fragments.RegistrationFragment;
import ee.juhan.meetingorganizer.fragments.dialog.LoadingFragment;
import ee.juhan.meetingorganizer.rest.RestClient;

public class MainActivity extends Activity {

    public HashMap<String, Integer> drawerItemsHashMap = new HashMap<String, Integer>();
    private ActionBar actionBar;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence drawerTitle;
    private CharSequence title;
    private String[] drawerItems;
    private int currentDrawerItemPosition;
    private TextView emailTextView;

    private SharedPreferences sharedPref;
    private Toast toast;

    private boolean isLoggedIn;
    private LoadingFragment loadingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getActionBar();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        checkIfLoggedIn();
    }

    private void checkIfLoggedIn() {
        if (getSID() == null) {
            setUpDrawer();
            setEmail(getString(R.string.textview_not_logged_in));
        } else {
            RestClient.setSID(getSID());
            isLoggedIn = true;
            setUpDrawer();
            setEmail(sharedPref.getString("email", ""));
        }
    }

    private void createDrawerItemsHashMap() {
        drawerItemsHashMap.clear();
        if (isLoggedIn)
            drawerItems = getResources().getStringArray(R.array.array_drawer_items_online);
        else
            drawerItems = getResources().getStringArray(R.array.array_drawer_items_offline);
        Integer position = 1;
        for (String item : drawerItems) {
            drawerItemsHashMap.put(item, position);
            position++;
        }
    }

    public Integer getDrawerItemPosition(String item) {
        return drawerItemsHashMap.get(item);
    }

    public String getSID() {
        return sharedPref.getString("sid", null);
    }

    public Integer getUserId() {
        return sharedPref.getInt("userId", 0);
    }

    public void logIn(String email, String sid, Integer userId) {
        sharedPref.edit().putString("email", email).putString("sid", sid)
                .putInt("userId", userId).commit();
        RestClient.setSID(sid);
        setEmail(email);
        isLoggedIn = true;
        setUpDrawer();
    }

    private void logOut() {
        sharedPref.edit().putString("email", null).putString("sid", null)
                .putInt("userId", 0).commit();
        RestClient.setSID(null);
        setEmail(getString(R.string.textview_not_logged_in));
        isLoggedIn = false;
        setUpDrawer();
    }

    private void setEmail(String email) {
        if (emailTextView != null) {
            emailTextView.setText(email);
        }
    }

    private void setUpDrawer() {
        title = drawerTitle = getTitle();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListView = (ListView) findViewById(R.id.left_drawer);
        createDrawerItemsHashMap();

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(title);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(drawerTitle);
                invalidateOptionsMenu();
            }
        };

        if (emailTextView == null) {
            LayoutInflater inflater = getLayoutInflater();
            ViewGroup mTop = (ViewGroup) inflater.inflate(R.layout.drawer_list_header, drawerListView, false);
            emailTextView = (TextView) mTop.findViewById(R.id.email_textView);
            drawerListView.addHeaderView(mTop);
        }

        drawerLayout.setDrawerListener(drawerToggle);
        drawerListView.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, drawerItems));

        currentDrawerItemPosition = 0;
        selectDrawerItem(1, false);
    }

    public void selectDrawerItem(int position) {
        selectDrawerItem(position, true);
    }

    public void selectDrawerItem(int position, boolean addToBackStack) {
        if (position != currentDrawerItemPosition && position != 0) {
            if (isLoggedIn) {
                MeetingsListFragment meetingsListFragment = null;
                if (position >= 2 && position <= 5) {
                    meetingsListFragment = new MeetingsListFragment(this, drawerItems[position - 1]);
                }
                switch (position) {
                    case 1:
                        changeFragment(new NewMeetingFragment(), addToBackStack);
                        break;
                    case 2:
                        meetingsListFragment.getMeetingsRequest(MeetingsListFragment.ONGOING_MEETINGS);
                        changeFragment(meetingsListFragment, addToBackStack);
                        break;
                    case 3:
                        meetingsListFragment.getMeetingsRequest(MeetingsListFragment.FUTURE_MEETINGS);
                        changeFragment(meetingsListFragment, addToBackStack);
                        break;
                    case 4:
                        meetingsListFragment.getMeetingsRequest(MeetingsListFragment.PAST_MEETINGS);
                        changeFragment(meetingsListFragment, addToBackStack);
                        break;
                    case 5:
                        meetingsListFragment.getMeetingsRequest(MeetingsListFragment.INVITATIONS);
                        changeFragment(meetingsListFragment, addToBackStack);
                        break;
                    default:
                        changeFragment(new NewMeetingFragment(), addToBackStack);
                        break;
                }
            } else {
                switch (position) {
                    case 1:
                        changeFragment(new LoginFragment(), addToBackStack);
                        break;
                    case 2:
                        changeFragment(new RegistrationFragment(), addToBackStack);
                        break;
                    default:
                        changeFragment(new LoginFragment(), addToBackStack);
                        break;
                }
            }
            drawerLayout.closeDrawer(drawerListView);
        } else {
            setDrawerItem(currentDrawerItemPosition);
        }
    }

    public void setDrawerItem(int position) {
        currentDrawerItemPosition = position;
        drawerListView.setItemChecked(position, true);
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
        getActionBar().setTitle(this.title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            logOut();
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeFragment(Fragment fragment) {
        changeFragment(fragment, true);
    }

    public void changeFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
                R.anim.slide_in_right, R.anim.slide_out_left);
        ft.replace(R.id.fragment_container, fragment);
        if (addToBackStack)
            ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * Displays a toast message with the given message.
     *
     * @param message
     */

    public void showToastMessage(final String message) {
        cancelToastMessage();
        toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Cancels the currently displayed toast message.
     */

    public void cancelToastMessage() {
        if (toast != null)
            toast.cancel();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (this.getFragmentManager().getBackStackEntryCount() != 0) {
                this.getFragmentManager().popBackStack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showLoadingFragment() {
        loadingFragment = new LoadingFragment();
        loadingFragment.show(getFragmentManager(), "LoaderFragment");
    }

    public void dismissLoadingFragment() {
        if (loadingFragment != null) {
            loadingFragment.dismiss();
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectDrawerItem(position);
        }
    }

}
