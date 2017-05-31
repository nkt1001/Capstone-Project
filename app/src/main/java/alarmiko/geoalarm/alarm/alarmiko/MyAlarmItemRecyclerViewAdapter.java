package alarmiko.geoalarm.alarm.alarmiko;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import alarmiko.geoalarm.alarm.alarmiko.AlarmListFragment.OnListFragmentInteractionListener;
import alarmiko.geoalarm.alarm.alarmiko.dummy.DummyContent.DummyItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyAlarmItemRecyclerViewAdapter extends RecyclerView.Adapter<MyAlarmItemRecyclerViewAdapter.ViewHolder> {

    private final List<DummyItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyAlarmItemRecyclerViewAdapter(List<DummyItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_alarmitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mAliasView.setText(mValues.get(position).id);
        holder.mRadiusView.setText(mValues.get(position).content);
        holder.mInfoView.setText(mValues.get(position).details);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void deleteItem(RecyclerView.ViewHolder item) {
        mValues.remove(item.getAdapterPosition());
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mAliasView;
        public final TextView mRadiusView;
        public final TextView mInfoView;
        public final Switch mAlarmSwitch;
        public DummyItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mAliasView = (TextView) view.findViewById(R.id.tv_alarm_item_alias);
            mRadiusView = (TextView) view.findViewById(R.id.tv_alarm_item_radius);
            mInfoView = (TextView) view.findViewById(R.id.tv_alarm_item_info);
            mAlarmSwitch = (Switch) view.findViewById(R.id.alarm_switch);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mAliasView.getText() + "'";
        }
    }
}
