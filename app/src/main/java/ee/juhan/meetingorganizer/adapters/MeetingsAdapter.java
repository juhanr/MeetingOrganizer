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
import ee.juhan.meetingorganizer.models.server.MeetingDTO;
import ee.juhan.meetingorganizer.util.DateParserUtil;

public class MeetingsAdapter extends ArrayAdapter<MeetingDTO> {

    private HashMap<MeetingDTO, Integer> mIdMap = new HashMap<>();
    private Context context;
    private List<MeetingDTO> meetingsList;

    public MeetingsAdapter(Context context, List<MeetingDTO> meetingsList) {
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

        MeetingDTO meeting = meetingsList.get(position);

        TextView meetingTitleView = (TextView) listItemView
                .findViewById(R.id.meeting_title);
        TextView meetingDateView = (TextView) listItemView
                .findViewById(R.id.meeting_date);
        TextView meetingTimeView = (TextView) listItemView
                .findViewById(R.id.meeting_time);

        meetingTitleView.setText("Title: " + meeting.getTitle());
        meetingDateView.setText("Date: " + DateParserUtil.formatDate(meeting.getStartDateTime()));
        meetingTimeView.setText("Time: " + DateParserUtil.formatTime(meeting.getStartDateTime())
                + " - " + DateParserUtil.formatTime(meeting.getEndDateTime()));

        return listItemView;
    }

    @Override
    public long getItemId(int position) {
        MeetingDTO item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}
