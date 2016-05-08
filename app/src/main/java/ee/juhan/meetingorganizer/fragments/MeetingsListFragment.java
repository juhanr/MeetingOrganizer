package ee.juhan.meetingorganizer.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.activities.MainActivity;
import ee.juhan.meetingorganizer.adapters.GeneralAdapter;
import ee.juhan.meetingorganizer.models.server.MeetingDTO;
import ee.juhan.meetingorganizer.rest.RestClient;
import ee.juhan.meetingorganizer.util.DateUtil;
import ee.juhan.meetingorganizer.util.UIUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

@SuppressLint("ValidFragment")
public class MeetingsListFragment extends Fragment {

	public static final String ACTIVE_MEETINGS = "active-meetings";
	private final String title;
	private final String meetingsType;
	private MainActivity activity;
	private ViewGroup meetingsListLayout;
	private MeetingsAdapter adapter;
	private List<MeetingDTO> meetingsList;

	public MeetingsListFragment() {
		this.title = "Meetings";
		this.meetingsType = ACTIVE_MEETINGS;
	}

	public MeetingsListFragment(String title, String meetingsType) {
		this.title = title;
		this.meetingsType = meetingsType;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
		getMeetingsRequest();
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity.setTitle(title);
		checkDrawerItem();
		if (meetingsList == null || meetingsList.size() == 0) {
			meetingsListLayout =
					(ViewGroup) inflater.inflate(R.layout.fragment_no_data, container, false);
			TextView infoText = (TextView) meetingsListLayout.findViewById(R.id.info_text);
			infoText.setText(getString(R.string.textview_no_meetings));
		} else {
			meetingsListLayout =
					(ViewGroup) inflater.inflate(R.layout.layout_listview, container, false);
			refreshListView();
		}
		return meetingsListLayout;
	}

	public void checkDrawerItem() {
		activity.checkDrawerItem(R.id.nav_meetings);
	}

	private void getMeetingsRequest() {
		final Fragment fragment = this;
		activity.showProgress(true);
		RestClient.get().getMeetingsRequest(meetingsType, activity.getUserId(),
				new Callback<List<MeetingDTO>>() {
					@Override
					public void success(final List<MeetingDTO> meetingDTOList, Response response) {
						activity.showProgress(false);
						meetingsList = meetingDTOList;
						for (MeetingDTO meeting : meetingsList) {
							meeting.toUTCTimeZone();
						}
						activity.refreshFragment(fragment);
					}

					@Override
					public void failure(RetrofitError error) {
						activity.showProgress(false);
						UIUtil.showToastMessage(activity, getString(R.string.toast_server_fail));
					}
				});
	}

	private void refreshListView() {
		ListView listview = (ListView) meetingsListLayout.findViewById(R.id.listView);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				MeetingDTO meeting = adapter.getItem(position);
				((MainActivity) getActivity()).changeFragmentToMeetingInfo(meeting);
			}
		});
		adapter = new MeetingsAdapter(getActivity(), meetingsList);
		listview.setAdapter(adapter);
	}

	private class MeetingsAdapter extends GeneralAdapter<MeetingDTO> {

		public MeetingsAdapter(Context context, List<MeetingDTO> meetingsList) {
			super(context, R.layout.list_item_meetings, meetingsList);
		}

		@Override
		protected void populateLayout() {
			MeetingDTO meeting = super.getCurrentItem();
			TextView meetingTitleView =
					(TextView) super.getLayout().findViewById(R.id.meeting_title);
			TextView meetingDateView = (TextView) super.getLayout().findViewById(R.id.meeting_date);
			TextView meetingTimeView = (TextView) super.getLayout().findViewById(R.id.meeting_time);
			meetingTitleView.setText(
					getContext().getString(R.string.textview_title) + ": " + meeting.getTitle());
			meetingDateView.setText(getContext().getString(R.string.textview_date) + ": " +
					DateUtil.formatDate(meeting.getStartDateTime()));
			meetingTimeView.setText(getContext().getString(R.string.textview_time) + ": " +
					DateUtil.formatTime(meeting.getStartDateTime()) + " - " +
					DateUtil.formatTime(meeting.getEndDateTime()));
		}

	}

}
