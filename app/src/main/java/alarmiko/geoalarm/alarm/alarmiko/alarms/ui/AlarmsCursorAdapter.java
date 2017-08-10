
package alarmiko.geoalarm.alarm.alarmiko.alarms.ui;

import android.view.ViewGroup;

import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.alarms.data.AlarmCursor;
import alarmiko.geoalarm.alarm.alarmiko.alarms.list.BaseCursorAdapter;
import alarmiko.geoalarm.alarm.alarmiko.alarms.list.OnListItemInteractionListener;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.AlarmController;

public class AlarmsCursorAdapter extends BaseCursorAdapter<Alarm, AlarmViewHolder, AlarmCursor> {
    private static final String TAG = "AlarmsCursorAdapter";

    private final AlarmController mAlarmController;

    public AlarmsCursorAdapter(OnListItemInteractionListener<Alarm> listener,
                               AlarmController alarmController) {
        super(listener);
        mAlarmController = alarmController;
    }

    @Override
    protected AlarmViewHolder onCreateViewHolder(ViewGroup parent, OnListItemInteractionListener<Alarm> listener, int viewType) {
        return new AlarmViewHolder(parent, listener, mAlarmController);
    }

//    @Override
//    public int getItemViewType(int position) {
//        final long stableId = getItemId(position);
//        return stableId != RecyclerView.NO_ID && stableId == mExpandedId
////                position == mExpandedPosition
//                ? VIEW_TYPE_EXPANDED : VIEW_TYPE_COLLAPSED;
//    }

//    // TODO
//    public void saveInstance(Bundle outState) {
//        outState.putLong(KEY_EXPANDED_ID, mExpandedId);
//    }

//    public boolean expand(int position) {
//        if (position == RecyclerView.NO_POSITION)
//            return false;
//        final long stableId = getItemId(position);
//        if (stableId == RecyclerView.NO_ID || mExpandedId == stableId)
//            return false;
//        mExpandedId = stableId;
//        // If we can call this, the item is in view, so we don't need to scroll to it?
////        mScrollHandler.smoothScrollTo(position);
//        if (mExpandedPosition >= 0) {
//            // Collapse this position first. getItemViewType() will be called
//            // in onCreateViewHolder() to verify which ViewHolder to create
//            // for the position.
//            notifyItemChanged(mExpandedPosition);
//        }
//        mExpandedPosition = position;
//        notifyItemChanged(position);
//        return true;
//
//        // This would be my alternative solution. But we're keeping Google's
//        // because the stable ID *could* hold up better for orientation changes
//        // than the position? I.e. when saving instance state we save the id.
////        int oldExpandedPosition = mExpandedPosition;
////        mExpandedPosition = position;
////        if (oldExpandedPosition >= 0) {
////            notifyItemChanged(oldExpandedPosition);
////        }
////        notifyItemChanged(mExpandedPosition);
//    }

//    public void collapse(int position) {
//        mExpandedId = RecyclerView.NO_ID;
//        mExpandedPosition = RecyclerView.NO_POSITION;
//        notifyItemChanged(position);
//    }

//    public int getExpandedPosition() {
//        return mExpandedPosition;
//    }
}
