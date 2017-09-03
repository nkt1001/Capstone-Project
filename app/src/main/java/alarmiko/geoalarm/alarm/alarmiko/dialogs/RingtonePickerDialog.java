package alarmiko.geoalarm.alarm.alarmiko.dialogs;

import android.content.DialogInterface;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import alarmiko.geoalarm.alarm.alarmiko.R;
import alarmiko.geoalarm.alarm.alarmiko.ringtone.playback.RingtoneLoop;

public class RingtonePickerDialog extends BaseAlertDialogFragment {
    private static final String TAG = "RingtonePickerDialog";
    private static final String KEY_RINGTONE_URI = "key_ringtone_uri";

    private RingtoneManager mRingtoneManager;
    private OnRingtoneSelectedListener mOnRingtoneSelectedListener;
    private Uri mRingtoneUri;
    private RingtoneLoop mRingtone;

    public interface OnRingtoneSelectedListener {
        void onRingtoneSelected(Uri ringtoneUri);
    }

    /**
     * @param ringtoneUri the URI of the ringtone to show as initially selected
     */
    public static RingtonePickerDialog newInstance(OnRingtoneSelectedListener l, Uri ringtoneUri) {
        RingtonePickerDialog dialog = new RingtonePickerDialog();
        dialog.mOnRingtoneSelectedListener = l;
        dialog.mRingtoneUri = ringtoneUri;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mRingtoneUri = savedInstanceState.getParcelable(KEY_RINGTONE_URI);
        }
        mRingtoneManager = new RingtoneManager(getActivity());
        mRingtoneManager.setType(RingtoneManager.TYPE_ALARM);
    }

    @Override
    protected AlertDialog createFrom(AlertDialog.Builder builder) {
        // TODO: We set the READ_EXTERNAL_STORAGE permission. Verify that this includes the user's
        // custom ringtone files.
        Cursor cursor = mRingtoneManager.getCursor();
        int checkedItem = mRingtoneManager.getRingtonePosition(mRingtoneUri);
        String labelColumn = cursor.getColumnName(RingtoneManager.TITLE_COLUMN_INDEX);

        builder.setTitle(R.string.ringtones)
                .setSingleChoiceItems(cursor, checkedItem, labelColumn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mRingtone != null) {
                            destroyLocalPlayer();
                        }
                        // Here, 'which' param refers to the position of the item clicked.
                        mRingtoneUri = mRingtoneManager.getRingtoneUri(which);
                        mRingtone = new RingtoneLoop(getActivity(), mRingtoneUri);
                        mRingtone.play();
                    }
                });
        return super.createFrom(builder);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        destroyLocalPlayer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_RINGTONE_URI, mRingtoneUri);
    }

    @Override
    protected void onOk() {
        if (mOnRingtoneSelectedListener != null) {
            // Here, 'which' param refers to the position of the item clicked.
            mOnRingtoneSelectedListener.onRingtoneSelected(mRingtoneUri);
        }
        dismiss();
    }

    public void setOnRingtoneSelectedListener(OnRingtoneSelectedListener onRingtoneSelectedListener) {
        mOnRingtoneSelectedListener = onRingtoneSelectedListener;
    }

    private void destroyLocalPlayer() {
        if (mRingtone != null) {
            mRingtone.stop();
            mRingtone = null;
        }
    }
}
