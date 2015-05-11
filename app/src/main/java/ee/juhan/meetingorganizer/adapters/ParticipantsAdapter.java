package ee.juhan.meetingorganizer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.models.server.ParticipantDTO;
import ee.juhan.meetingorganizer.models.server.ParticipationAnswer;

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
        if (participant.getParticipationAnswer() == ParticipationAnswer.PARTICIPATING) {
            addIcon(listItemView, R.drawable.ic_check_mark);
        } else if (participant.getParticipationAnswer() == ParticipationAnswer.NOT_ANSWERED) {
            addIcon(listItemView, R.drawable.ic_question_mark);
        }
        if (participant.getAccountId() != 0) {
            addIcon(listItemView, R.drawable.ic_account);
        }
        return listItemView;
    }

    private void addIcon(ViewGroup listItemView, int iconResource) {
        ImageView accountIcon = new ImageView(context);
        accountIcon.setBackgroundResource(iconResource);
        listItemView.addView(accountIcon);
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
