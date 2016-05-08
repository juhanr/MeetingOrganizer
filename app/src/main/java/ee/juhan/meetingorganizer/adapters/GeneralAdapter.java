package ee.juhan.meetingorganizer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

public abstract class GeneralAdapter<T> extends ArrayAdapter<T> {

	private final LayoutInflater inflater;
	private final int layoutId;
	private ViewGroup adapterLayout;
	private int currentPosition;
	private List<T> objects;

	public GeneralAdapter(Context context, int layoutId, List<T> objects) {
		super(context, layoutId, objects);
		this.layoutId = layoutId;
		this.objects = objects;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@SuppressLint("ViewHolder")
	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		currentPosition = position;
		adapterLayout = (ViewGroup) inflater.inflate(layoutId, parent, false);
		populateLayout();
		return adapterLayout;
	}

	protected abstract void populateLayout();

	protected final void addIcon(int iconResource) {
		ImageView accountIcon = new ImageView(getContext());
		accountIcon.setBackgroundResource(iconResource);
		if (adapterLayout != null) {
			adapterLayout.addView(accountIcon);
		}
	}

	protected final ViewGroup getLayout() {
		return adapterLayout;
	}

	protected final T getCurrentItem() {
		return getItem(currentPosition);
	}

	protected final LayoutInflater getInflater() {
		return inflater;
	}

	public final List<T> getObjects() {
		return objects;
	}

}