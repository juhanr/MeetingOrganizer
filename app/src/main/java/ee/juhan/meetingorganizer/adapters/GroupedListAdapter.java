package ee.juhan.meetingorganizer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ee.juhan.meetingorganizer.R;

public class GroupedListAdapter extends ArrayAdapter<GroupedListAdapter.GroupedListItem> {

	private final LayoutInflater inflater;
	private final int layoutId;
	private ViewGroup adapterLayout;
	private int currentPosition;
	private List<GroupedListItem> items;

	public GroupedListAdapter(Context context, int layoutId, List<GroupedListItem> items) {
		super(context, layoutId, items);
		this.layoutId = layoutId;
		this.items = items;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@SuppressLint("ViewHolder")
	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		currentPosition = position;
		if (getCurrentItem().isGroupItem()) {
			adapterLayout =
					(ViewGroup) inflater.inflate(R.layout.list_item_group_header, parent, false);
			TextView groupNameTxt = (TextView) adapterLayout.findViewById(R.id.txt_group_name);
			groupNameTxt.setText(getCurrentItem().getGroupName());
		} else {
			adapterLayout = (ViewGroup) inflater.inflate(layoutId, parent, false);
			populateLayout();
		}
		return adapterLayout;
	}

	@Override
	public boolean isEnabled(int position) {
		return !getItem(position).isGroupItem();
	}

	protected void populateLayout() {}

	protected final void addIcon(int iconResource) {
		addIcon(iconResource, 0);
	}

	protected final void addIcon(int iconResource, int tintColorId) {
		ImageView icon = new ImageView(getContext());
		icon.setImageResource(iconResource);
		if (tintColorId != 0) {
			icon.setColorFilter(ContextCompat.getColor(getContext(), tintColorId));
		}
		if (adapterLayout != null) {
			adapterLayout.addView(icon);
		}
	}

	protected final ViewGroup getLayout() {
		return adapterLayout;
	}

	protected final GroupedListItem getCurrentItem() {
		return getItem(currentPosition);
	}

	protected final LayoutInflater getInflater() {
		return inflater;
	}

	public final List<GroupedListItem> getItems() {
		return items;
	}

	public static class GroupedListItem<T> {

		private String groupName;
		private T object;
		private boolean isGroupItem;

		public GroupedListItem(String groupName) {
			this.groupName = groupName;
			this.isGroupItem = true;
		}

		public GroupedListItem(T object) {
			this.object = object;
			this.isGroupItem = false;
		}

		public String getGroupName() {
			return groupName;
		}

		public T getObject() {
			return object;
		}

		public boolean isGroupItem() {
			return isGroupItem;
		}
	}

}