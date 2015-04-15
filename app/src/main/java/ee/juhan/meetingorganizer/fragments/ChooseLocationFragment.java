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

import static android.R.layout.simple_spinner_item;

public class ChooseLocationFragment extends Fragment {

    private MainActivity activity;
    private final String title = "Choose location";
    private static CheckBoxAdapter adapter;
    private LinearLayout chooseLocationLayout;
    public static LatLng location;
    private List<String> filtersList = Arrays.asList("Cafe", "Restaurant", "Park");
    private int locationType;
    private final int SPECIFIC_LOCATION = 1;
    private final int GENERATED_FROM_LOCATIONS = 2;
    private final int GENERATED_FROM_FILTERS = 3;

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
                activity.changeFragment(new ChooseContactsFragment());
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
                        locationType = SPECIFIC_LOCATION;
                        removeLocationChild();
                        CustomMapFragment customMapFragment = new CustomMapFragment();
                        getFragmentManager().beginTransaction().replace(R.id.location_frame, customMapFragment).commit();
                        FrameLayout layout = (FrameLayout) chooseLocationLayout.findViewById(R.id.location_frame);
                        layout.setBackgroundResource(R.drawable.view_border);
                        break;
                    case 1:
                        locationType = GENERATED_FROM_LOCATIONS;
                        removeLocationChild();
                        break;
                    case 2:
                        locationType = GENERATED_FROM_FILTERS;
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
//        View child = activity.getLayoutInflater().inflate(childId, null);
        layout.addView(child);
    }

    public void removeLocationChild() {
        FrameLayout layout = (FrameLayout) chooseLocationLayout.findViewById(R.id.location_frame);
        layout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        layout.removeAllViews();
    }

}
