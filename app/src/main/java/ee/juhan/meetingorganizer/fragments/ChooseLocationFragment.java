package ee.juhan.meetingorganizer.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.List;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.adapters.CheckBoxAdapter;
import ee.juhan.meetingorganizer.models.server.LocationType;

import static android.R.layout.simple_spinner_item;

public class ChooseLocationFragment extends Fragment {

    private static CheckBoxAdapter adapter;
    private final String title = "Choose location";
    private MainActivity activity;
    private LinearLayout chooseLocationLayout;
    private List<String> filtersList = Arrays.asList("Cafe", "Restaurant", "Park");

    public ChooseLocationFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(title);
        chooseLocationLayout = (LinearLayout) inflater.inflate(R.layout.fragment_choose_location, container, false);
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
                if (NewMeetingFragment.newMeetingModel.getLocationType() ==
                        LocationType.SPECIFIC_LOCATION &&
                        NewMeetingFragment.newMeetingModel.getLocationLatitude() != 0) {
                    activity.changeFragment(new ChooseContactsFragment());
                } else {
                    activity.showToastMessage("Please choose a location!");
                }
            }
        });

    }

    public void setLocationSpinner() {
        Spinner spinner = (Spinner) chooseLocationLayout.findViewById(R.id.location_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(activity,
                R.array.location_items, simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
                switch (pos) {
                    case 0:
                        NewMeetingFragment.newMeetingModel.setLocationType(
                                LocationType.SPECIFIC_LOCATION);
                        removeLocationChild();
                        CustomMapFragment customMapFragment = new CustomMapFragment();
                        customMapFragment.setIsClickableMap(true);
                        if (NewMeetingFragment.newMeetingModel.getLocationLatitude() != 0) {
                            customMapFragment.setLocation(
                                    new LatLng(NewMeetingFragment.newMeetingModel.getLocationLatitude(),
                                            NewMeetingFragment.newMeetingModel.getLocationLongitude()));
                        }
                        getFragmentManager().beginTransaction().replace(R.id.location_frame, customMapFragment).commit();
                        FrameLayout layout = (FrameLayout) chooseLocationLayout.findViewById(R.id.location_frame);
                        layout.setBackgroundResource(R.drawable.view_border);
                        break;
                    case 1:
                        NewMeetingFragment.newMeetingModel.setLocationType(
                                LocationType.GENERATED_FROM_PREDEFINED_LOCATIONS);
                        removeLocationChild();
                        break;
                    case 2:
                        NewMeetingFragment.newMeetingModel.setLocationType(
                                LocationType.GENERATED_FROM_PARAMETERS);
                        removeLocationChild();
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

    public void addLocationChild(View child) {
        FrameLayout layout = (FrameLayout) chooseLocationLayout.findViewById(R.id.location_frame);
        layout.addView(child);
    }

    public void removeLocationChild() {
        FrameLayout layout = (FrameLayout) chooseLocationLayout.findViewById(R.id.location_frame);
        layout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        layout.removeAllViews();
    }

}
