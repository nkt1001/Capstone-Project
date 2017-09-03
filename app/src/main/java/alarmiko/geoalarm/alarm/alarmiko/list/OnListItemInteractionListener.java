
package alarmiko.geoalarm.alarm.alarmiko.list;

public interface OnListItemInteractionListener<T> {
    void onListItemClick(T item, int position);
    void onListItemDeleted(T item);
    void onListItemUpdate(T item, int position);
}
