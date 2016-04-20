package ee.juhan.meetingorganizer.fragments;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;

public class InvitationsFragment extends MeetingsListFragment {

	public static final String INVITATIONS = "invitations";

	public InvitationsFragment() {
		super("Invitations", INVITATIONS);
	}

	@Override
	public void checkDrawerItem() {
		((MainActivity) getActivity()).checkDrawerItem(R.id.nav_invitations);
	}
}
