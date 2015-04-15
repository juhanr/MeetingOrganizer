package ee.juhan.meetingorganizer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.models.Meeting;

public class MeetingsAdapter extends ArrayAdapter<Meeting> {

    private HashMap<Meeting, Integer> mIdMap = new HashMap<>();
    private Context context;
    private List<Meeting> meetingsList;

    public MeetingsAdapter(Context context, List<Meeting> meetingsList) {
        super(context, R.layout.list_item_meetings, meetingsList);
        this.context = context;
        this.meetingsList = meetingsList;
        for (int i = 0; i < meetingsList.size(); ++i) {
            mIdMap.put(meetingsList.get(i), i);
        }
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        LinearLayout listItemView = (LinearLayout) inflater.inflate(
                R.layout.list_item_meetings, parent, false);

        Meeting meeting = meetingsList.get(position);

        TextView meetingTitleView = (TextView) listItemView
                .findViewById(R.id.meeting_title);
        TextView meetingDateView = (TextView) listItemView
                .findViewById(R.id.meeting_date);
        TextView meetingTimeView = (TextView) listItemView
                .findViewById(R.id.meeting_time);

        meetingTitleView.setText(meeting.getTitle());
        meetingDateView.setText(meeting.getDate() + "");
        meetingTimeView.setText(meeting.getStartTime() + " - " + meeting.getEndTime());

        return listItemView;
    }

    @Override
    public long getItemId(int position) {
        Meeting item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}
