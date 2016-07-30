package ee.juhan.meetingorganizer.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.activities.MainActivity;
import ee.juhan.meetingorganizer.adapters.GroupedListAdapter;
import ee.juhan.meetingorganizer.adapters.GroupedListAdapter.GroupedListItem;
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
			infoText.setText(getString(R.string.meeting_no_meetings));
		} else {
			meetingsListLayout =
					(ViewGroup) inflater.inflate(R.layout.layout_list, container, false);
			refreshMeetingsListView();
		}
		return meetingsListLayout;
	}

	public void checkDrawerItem() {
		activity.checkDrawerItem(R.id.nav_meetings);
	}

	private void getMeetingsRequest() {
		final Fragment fragment = this;
		activity.showProgress(true);
		RestClient.get().getMeetingsRequest(meetingsType, activity.getAccountId(),
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
						UIUtil.showToastMessage(activity, getString(R.string.error_server_fail));
					}
				});
	}

	private void refreshMeetingsListView() {
		ListView listview = (ListView) meetingsListLayout.findViewById(R.id.list_view);
		listview.setOnItemClickListener((parent, view, position, id) -> {
			GroupedListItem item = adapter.getItem(position);
			if (!item.isGroupItem()) {
				MeetingDTO meeting = (MeetingDTO) item.getObject();
				((MainActivity) getActivity()).changeFragmentToMeetingInfo(meeting);
			}
		});
		adapter = new MeetingsAdapter(getActivity(), getGroupedListItems());
		listview.setAdapter(adapter);
	}

	private List<GroupedListAdapter.GroupedListItem> getGroupedListItems() {
		List<GroupedListItem> groupedListItems = new ArrayList<>();
		Date currentGroupDate = new Date(Long.MIN_VALUE);
		for (MeetingDTO meeting : meetingsList) {
			if (!DateUtil.dateEquals(meeting.getStartDateTime(), currentGroupDate)) {
				currentGroupDate = meeting.getStartDateTime();
				GroupedListItem groupItem;
				if (DateUtil.isYesterday(currentGroupDate)) {
					groupItem = new GroupedListItem(getString(R.string.msg_yesterday));
				} else if (DateUtil.isToday(currentGroupDate)) {
					groupItem = new GroupedListItem(getString(R.string.msg_today));
				} else if (DateUtil.isTomorrow(currentGroupDate)) {
					groupItem = new GroupedListItem(getString(R.string.msg_tomorrow));
				} else {
					groupItem = new GroupedListItem(DateUtil.formatDate(currentGroupDate));
				}
				groupedListItems.add(groupItem);
			}
			groupedListItems.add(new GroupedListItem<>(meeting));
		}
		return groupedListItems;
	}

	private class MeetingsAdapter extends GroupedListAdapter {

		public MeetingsAdapter(Context context, List<GroupedListItem> listItems) {
			super(context, R.layout.list_item_meetings, listItems);
		}

		@Override
		protected void populateLayout() {
			MeetingDTO meeting = (MeetingDTO) super.getCurrentItem().getObject();
			TextView meetingTitleView =
					(TextView) super.getLayout().findViewById(R.id.txt_meeting_title);
			TextView meetingDescView =
					(TextView) super.getLayout().findViewById(R.id.txt_meeting_description);
			TextView meetingTimeView =
					(TextView) super.getLayout().findViewById(R.id.txt_meeting_time);
			meetingTitleView.setText(meeting.getTitle());
			if (meeting.getDescription().equals("") || meeting.getDescription() == null) {
				meetingDescView.setVisibility(View.GONE);
			} else {
				meetingDescView.setText(meeting.getDescription());
			}
			meetingTimeView.setText(
					String.format("%s - %s", DateUtil.formatTime(meeting.getStartDateTime()),
							DateUtil.formatTime(meeting.getEndDateTime())));
		}

	}

}
