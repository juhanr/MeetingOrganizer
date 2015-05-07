package ee.juhan.meetingorganizer.fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.adapters.CheckBoxAdapter;
import ee.juhan.meetingorganizer.fragments.listeners.MyLocationListener;
import ee.juhan.meetingorganizer.models.server.LocationType;
import ee.juhan.meetingorganizer.models.server.MapCoordinate;

import static android.R.layout.simple_spinner_item;

public class ChooseLocationFragment extends Fragment {

    private String title;
    private MainActivity activity;
    private ViewGroup chooseLocationLayout;
    private CheckBoxAdapter adapter;
    private List<String> filtersList;
    private CustomMapFragment customMapFragment = new CustomMapFragment();
    private LinearLayout predefinedLocationsLayout;

    public ChooseLocationFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        title = getString(R.string.title_choose_location);
        filtersList = Arrays.asList(
                getResources().getStringArray(R.array.array_location_parameters));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(title);
        chooseLocationLayout = (ViewGroup) inflater
                .inflate(R.layout.fragment_choose_location, container, false);
        setLocationSpinner();
        setButtonListeners();
        return chooseLocationLayout;
    }

    private void setButtonListeners() {
        Button continueButton = (Button) chooseLocationLayout
                .findViewById(R.id.continue_button);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NewMeetingFragment.getNewMeetingModel().getLocationType() ==
                        LocationType.SPECIFIC_LOCATION &&
                        NewMeetingFragment.getNewMeetingModel().getLocation() != null) {
                    activity.changeFragment(new ChooseContactsFragment());
                } else if (NewMeetingFragment.getNewMeetingModel().getLocationType() ==
                        LocationType.GENERATED_FROM_PREDEFINED_LOCATIONS &&
                        NewMeetingFragment.getNewMeetingModel().getPredefinedLocations().size() > 0) {
                    if (MyLocationListener.getMyLocation() != null) {
                        activity.changeFragment(new ChooseContactsFragment());
                    } else {
                        activity.showToastMessage(getString(R.string.toast_please_get_your_location));
                    }
                } else {
                    activity.showToastMessage(getString(R.string.toast_please_choose_location));
                }
            }
        });

    }

    private void setLocationSpinner() {
        Spinner spinner = (Spinner) chooseLocationLayout.findViewById(R.id.location_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(activity,
                R.array.array_location_items, simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                switch (pos) {
                    case 0:
                        NewMeetingFragment.getNewMeetingModel().setLocationType(
                                LocationType.SPECIFIC_LOCATION);
                        removeLocationViews();
                        setUpMapLayout();
                        break;
                    case 1:
                        NewMeetingFragment.getNewMeetingModel().setLocationType(
                                LocationType.GENERATED_FROM_PREDEFINED_LOCATIONS);
                        removeLocationViews();
                        setUpMapLayout();
                        setUpPredefinedLocationLayout();
                        for (MapCoordinate location : NewMeetingFragment.getNewMeetingModel().getPredefinedLocations()) {
                            try {
                                Geocoder geocoder = new Geocoder(activity.getBaseContext());
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                String address = addresses.get(0).getAddressLine(0);
                                addPredefinedLocationView(location, address);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case 2:
                        NewMeetingFragment.getNewMeetingModel().setLocationType(
                                LocationType.GENERATED_FROM_PARAMETERS);
                        removeLocationViews();
                        ListView listView = new ListView(activity);
                        adapter = new CheckBoxAdapter(getActivity(), filtersList);
                        listView.setAdapter(adapter);
                        addLocationChild(listView);
                        break;
                    default:
                        break;
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setUpMapLayout() {
        setUpMapSearch();
        customMapFragment = new CustomMapFragment();
        customMapFragment.setIsClickableMap(true);
        if (NewMeetingFragment.getNewMeetingModel().getLocation() != null) {
            customMapFragment.setLocation(
                    new LatLng(NewMeetingFragment.getNewMeetingModel().getLocation().getLatitude(),
                            NewMeetingFragment.getNewMeetingModel().getLocation().getLongitude()));
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.location_frame, customMapFragment).commit();
        FrameLayout layout = (FrameLayout) chooseLocationLayout
                .findViewById(R.id.location_frame);
        layout.setBackgroundResource(R.drawable.view_border);
    }

    @SuppressLint("InflateParams")
    private void setUpMapSearch() {
        View mapSearchLayout = activity.getLayoutInflater()
                .inflate(R.layout.layout_map_search, null);
        FrameLayout mapSearchFrame = (FrameLayout) chooseLocationLayout
                .findViewById(R.id.map_search_frame);
        mapSearchFrame.addView(mapSearchLayout);

        final EditText searchEditText = (EditText) mapSearchLayout.findViewById(R.id.map_search_textbox);
        Button searchButton = (Button) mapSearchLayout.findViewById(R.id.map_search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customMapFragment.searchMap(searchEditText.getText().toString());
            }
        });
    }

    private void setUpPredefinedLocationLayout() {
        predefinedLocationsLayout = (LinearLayout) chooseLocationLayout
                .findViewById(R.id.predefined_locations_layout);
        Button addLocationButton = (Button) activity.getLayoutInflater()
                .inflate(R.layout.button_add_location, null);
        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!customMapFragment.getMarkerAddress().equals("")) {
                    MapCoordinate location = new MapCoordinate(customMapFragment.getLocation().latitude,
                            customMapFragment.getLocation().longitude);
                    NewMeetingFragment.getNewMeetingModel().addPredefinedLocation(location);
                    addPredefinedLocationView(location, customMapFragment.getMarkerAddress());
                }
            }
        });
        predefinedLocationsLayout.addView(addLocationButton);
    }

    private void addPredefinedLocationView(final MapCoordinate location, String address) {
        final LinearLayout predefinedLocationSubLayout = (LinearLayout) activity.getLayoutInflater()
                .inflate(R.layout.layout_predefined_location, null);
        EditText locationTextBox = (EditText) predefinedLocationSubLayout
                .findViewById(R.id.location_textbox);
        locationTextBox.setText(address);
        customMapFragment.clearMap();
        Button removeLocationButton = (Button) predefinedLocationSubLayout
                .findViewById(R.id.remove_location_button);
        removeLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewMeetingFragment.getNewMeetingModel().removePredefinedLocation(location);
                predefinedLocationSubLayout.setVisibility(View.GONE);
            }
        });
        predefinedLocationsLayout.addView(predefinedLocationSubLayout);
    }

    private void addLocationChild(View child) {
        FrameLayout layout = (FrameLayout) chooseLocationLayout.findViewById(R.id.location_frame);
        layout.addView(child);
    }

    private void removeLocationViews() {
        FrameLayout mapLayout = (FrameLayout) chooseLocationLayout.findViewById(R.id.location_frame);
        mapLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mapLayout.removeAllViews();
        FrameLayout searchLayout = (FrameLayout) chooseLocationLayout.findViewById(R.id.map_search_frame);
        searchLayout.removeAllViews();
        LinearLayout predefinedLocationsLayout = (LinearLayout) chooseLocationLayout
                .findViewById(R.id.predefined_locations_layout);
        predefinedLocationsLayout.removeAllViews();
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        final Animator anim = AnimatorInflater.loadAnimator(getActivity(), nextAnim);
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    customMapFragment.setMapVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    customMapFragment.setMapVisibility(View.VISIBLE);
                }
            });
        }
        return anim;
    }

}
