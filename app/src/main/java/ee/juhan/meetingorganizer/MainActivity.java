package ee.juhan.meetingorganizer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

import ee.juhan.meetingorganizer.fragments.ChooseContactsFragment;
import ee.juhan.meetingorganizer.fragments.ChooseLocationFragment;
import ee.juhan.meetingorganizer.fragments.InvitationsListFragment;
import ee.juhan.meetingorganizer.fragments.MeetingInfoFragment;
import ee.juhan.meetingorganizer.fragments.MeetingsListFragment;
import ee.juhan.meetingorganizer.fragments.NewMeetingFragment;
import ee.juhan.meetingorganizer.fragments.ParticipantsListFragment;
import ee.juhan.meetingorganizer.models.Date;
import ee.juhan.meetingorganizer.models.Meeting;
import ee.juhan.meetingorganizer.models.Participant;
import ee.juhan.meetingorganizer.models.Time;


public class MainActivity extends Activity implements NewMeetingFragment.OnFragmentInteractionListener,
        MeetingInfoFragment.OnFragmentInteractionListener, MeetingsListFragment.OnFragmentInteractionListener,
        ChooseContactsFragment.OnFragmentInteractionListener, ChooseLocationFragment.OnFragmentInteractionListener,
        ParticipantsListFragment.OnFragmentInteractionListener, InvitationsListFragment.OnFragmentInteractionListener {
    private ActionBar actionBar;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle drawerToggle;

    private CharSequence drawerTitle;
    private CharSequence title;
    private String[] drawerItems;

    public static List<Meeting> exampleMeetings = Arrays.asList(
            new Meeting("Example meeting 1", new Date(10, 03, 2015), new Time(18, 00), new Time(19, 00),
                    "This is the first example meeting.", new Participant[]
                    {new Participant("John Smith", 37253974840L), new Participant("Bob Lake"),
                            new Participant("Lucy Allen")}, 59, 24),
            new Meeting("Example meeting 2", new Date(30, 05, 2015), new Time(11, 00), new Time(12, 00),
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
        setupDrawer();
        if (savedInstanceState == null) {
            selectDrawerItem(1);
        }
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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectDrawerItem(position);
        }
    }

    private void selectDrawerItem(int position) {
        if (position != 0) {
            List<Meeting> meetingsList = MainActivity.exampleMeetings;
            switch (position) {
                case 1:
                    changeFragment(new NewMeetingFragment());
                    break;
                case 2:
                    changeFragment(new MeetingInfoFragment(meetingsList.get(0)));
                    break;
                case 3:
                    changeFragment(new MeetingsListFragment(meetingsList));
                    break;
                case 4:
                    changeFragment(new MeetingsListFragment(meetingsList));
                    break;
                case 5:
                    changeFragment(new InvitationsListFragment());
                    break;
                default:
                    changeFragment(new NewMeetingFragment());
                    break;
            }

            drawerListView.setItemChecked(position, true);
            setTitle(drawerItems[position - 1]);
            drawerLayout.closeDrawer(drawerListView);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
        getActionBar().setTitle(this.title);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
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
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeFragment(Fragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        ft.replace(R.id.fragment_container, fragment).commit();
    }

    public void changeFragment(Fragment fragment, String newTitle) {
        setTitle(newTitle);
        changeFragment(fragment);
    }

}
