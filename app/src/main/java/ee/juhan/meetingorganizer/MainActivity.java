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
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import ee.juhan.meetingorganizer.fragments.InvitationsListFragment;
import ee.juhan.meetingorganizer.fragments.LoginFragment;
import ee.juhan.meetingorganizer.fragments.MeetingInfoFragment;
import ee.juhan.meetingorganizer.fragments.MeetingsListFragment;
import ee.juhan.meetingorganizer.fragments.NewMeetingFragment;
import ee.juhan.meetingorganizer.models.Date;
import ee.juhan.meetingorganizer.models.Meeting;
import ee.juhan.meetingorganizer.models.Participant;
import ee.juhan.meetingorganizer.models.Time;

public class MainActivity extends Activity {

    private ActionBar actionBar;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle drawerToggle;

    private CharSequence drawerTitle;
    private CharSequence title;
    private String[] drawerItems;

    private SharedPreferences sharedPref;
    private Toast toast;

    private boolean isLoggedIn;

    public static List<Meeting> exampleMeetings = Arrays.asList(
            new Meeting("Example meeting 1", new Date(10, 03, 2015), new Time(18, 00), new Time(19, 00),
                    "This is the first example meeting.", new Participant[]
                    {new Participant("John Smith", 37253974840L), new Participant("Bob Lake"),
                            new Participant("Lucy Allen")}, 59, 24),
            new Meeting("Example meeting 2", new Date(30, 03, 2015), new Time(11, 00), new Time(12, 00),
                    "This is the second example meeting.", new Participant[]
                    {new Participant("John Smith", 37253974840L), new Participant("Jane Fitzgerald"),
                            new Participant("Jonathan Grassfield")}, 59, 24),
            new Meeting("Example meeting 3", new Date(01, 04, 2015), new Time(9, 30), new Time(12, 00),
                    "This is the third example meeting.", new Participant[]
                    {new Participant("John Smith", 37253974840L), new Participant("Robert Green"),
                            new Participant("Rachel Sky")}, 59, 24));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getActionBar();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        setupDrawer();
        checkIfLoggedIn();
    }

    private void checkIfLoggedIn() {
        if (getSID() == null) {
            addFirstFragment(new LoginFragment());
        } else {
            isLoggedIn = true;
            selectDrawerItem(-1);
        }
    }

    public String getSID() {
        return sharedPref.getString("sid", null);
    }

    public Integer getUserId() {
        return sharedPref.getInt("userId", 0);
    }

    public void logIn(String sid, Integer userId) {
        sharedPref.edit().putString("sid", sid).putInt("userId", userId).commit();
        isLoggedIn = true;
        changeFragment(new NewMeetingFragment(), false);
    }

    private void logOut() {
        sharedPref.edit().putString("sid", null).commit();
        isLoggedIn = false;
        changeFragment(new LoginFragment(), false);
    }

    private void setupDrawer() {
        title = drawerTitle = getTitle();
        drawerItems = getResources().getStringArray(R.array.drawer_items);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListView = (ListView) findViewById(R.id.left_drawer);

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
        drawerLayout.setDrawerListener(drawerToggle);

        LayoutInflater inflater = getLayoutInflater();
        ViewGroup mTop = (ViewGroup) inflater.inflate(R.layout.drawer_list_header, drawerListView, false);
        drawerListView.addHeaderView(mTop);
        drawerListView.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, drawerItems));
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectDrawerItem(position);
        }
    }

    private void selectDrawerItem(int position) {
        if (position != 0 && isLoggedIn) {
            List<Meeting> meetingsList = MainActivity.exampleMeetings;
            switch (position) {
                case -1:
                    addFirstFragment(new NewMeetingFragment());
                    position = 1;
                    break;
                case 1:
                    changeFragment(new NewMeetingFragment());
                    break;
                case 2:
                    changeFragment(new MeetingInfoFragment(meetingsList.get(0)));
                    break;
                case 3:
                    changeFragment(new MeetingsListFragment(meetingsList, drawerItems[position - 1]));
                    break;
                case 4:
                    changeFragment(new MeetingsListFragment(meetingsList, drawerItems[position - 1]));
                    break;
                case 5:
                    changeFragment(new InvitationsListFragment());
                    break;
                default:
                    changeFragment(new NewMeetingFragment());
                    break;
            }

            drawerListView.setItemChecked(position, true);
            drawerLayout.closeDrawer(drawerListView);
        } else if (!isLoggedIn) {
            drawerLayout.closeDrawer(drawerListView);
        }
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

    private void addFirstFragment(Fragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment).commit();
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

}
