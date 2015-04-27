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
import ee.juhan.meetingorganizer.models.server.ParticipantDTO;

public class ParticipantsAdapter extends ArrayAdapter<ParticipantDTO> {

    private HashMap<ParticipantDTO, Integer> mIdMap = new HashMap<>();
    private Context context;
    private List<ParticipantDTO> participantsList;

    public ParticipantsAdapter(Context context, List<ParticipantDTO> participantsList) {
        super(context, R.layout.list_item_participants, participantsList);
        this.context = context;
        this.participantsList = participantsList;
        for (int i = 0; i < participantsList.size(); ++i) {
            mIdMap.put(participantsList.get(i), i);
        }
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout listItemView = (LinearLayout) inflater.inflate(
                R.layout.list_item_participants, parent, false);
        ParticipantDTO participant = participantsList.get(position);
        TextView participantNameView = (TextView) listItemView
                .findViewById(R.id.participant_name);
        participantNameView.setText(participant.getName());
        return listItemView;
    }

    @Override
    public long getItemId(int position) {
        ParticipantDTO item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}
