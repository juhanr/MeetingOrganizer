package ee.juhan.meetingorganizer.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLng;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;

import static android.R.layout.simple_spinner_item;

public class ChooseLocationFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private MainActivity activity;
    private LinearLayout chooseLocationLayout;
    public static LatLng location;

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
                activity.changeFragment(new ChooseContactsFragment(), "Invite contacts");
            }
        });

    }

    public void setLocationSpinner() {
        Spinner spinner = (Spinner) chooseLocationLayout.findViewById(R.id.location_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
                R.array.location_items, simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
                switch (pos) {
                    case 0:
                        removeLocationChild();
                        getFragmentManager().beginTransaction().replace(R.id.location_frame, new CustomMapFragment()).commit();
                        FrameLayout layout = (FrameLayout) chooseLocationLayout.findViewById(R.id.location_frame);
                        layout.setBackgroundResource(R.drawable.view_border);
                        break;
                    case 1:
                        removeLocationChild();
                        break;
                    case 2:
                        removeLocationChild();
                        addLocationChild(R.layout.view_location_filters);
                        break;
                    default:
                        break;
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void addLocationChild(int childId) {
        FrameLayout layout = (FrameLayout) chooseLocationLayout.findViewById(R.id.location_frame);
        View child = activity.getLayoutInflater().inflate(childId, null);
        layout.addView(child);
    }

    public void removeLocationChild() {
        FrameLayout layout = (FrameLayout) chooseLocationLayout.findViewById(R.id.location_frame);
        layout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        layout.removeAllViews();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}
