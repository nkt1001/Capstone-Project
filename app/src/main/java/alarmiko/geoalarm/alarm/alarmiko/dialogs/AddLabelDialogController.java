
package alarmiko.geoalarm.alarm.alarmiko.dialogs;

import android.support.v4.app.FragmentManager;
import android.util.Log;

public final class AddLabelDialogController extends DialogFragmentController<AddLabelDialog> {
    private static final String TAG = "AddLabelController";

    private final AddLabelDialog.OnLabelSetListener mListener;

    public AddLabelDialogController(FragmentManager fragmentManager, AddLabelDialog.OnLabelSetListener listener) {
        super(fragmentManager);
        mListener = listener;
    }

    public void show(CharSequence initialText, String tag) {
        AddLabelDialog dialog = AddLabelDialog.newInstance(mListener, initialText);
        show(dialog, tag);
    }

    @Override
    public void tryRestoreCallback(String tag) {
        AddLabelDialog labelDialog = findDialog(tag);
        if (labelDialog != null) {
            Log.i(TAG, "Restoring add label callback");
            labelDialog.setOnLabelSetListener(mListener);
        }
    }
}
