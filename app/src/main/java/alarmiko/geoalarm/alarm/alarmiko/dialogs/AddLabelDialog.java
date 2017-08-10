
package alarmiko.geoalarm.alarm.alarmiko.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import alarmiko.geoalarm.alarm.alarmiko.R;


public class AddLabelDialog extends BaseAlertDialogFragment {

    private EditText mEditText;
    private OnLabelSetListener mOnLabelSetListener;
    private CharSequence mInitialText;

    public interface OnLabelSetListener {
        void onLabelSet(String label);
    }

    /**
     * @param text the initial text
     */
    public static AddLabelDialog newInstance(OnLabelSetListener l, CharSequence text) {
        AddLabelDialog dialog = new AddLabelDialog();
        dialog.mOnLabelSetListener = l;
        dialog.mInitialText = text;
        return dialog;
    }

    @Override
    protected AlertDialog createFrom(AlertDialog.Builder builder) {
        mEditText = new AppCompatEditText(getActivity());
        // Views must have IDs set to automatically save instance state
        mEditText.setId(R.id.tv_label);
        mEditText.setText(mInitialText);
        mEditText.setInputType(
                EditorInfo.TYPE_CLASS_TEXT // Needed or else we won't get automatic spacing between words
                | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);

        // TODO: We can use the same value for both directions.
        int spacingLeft = getResources().getDimensionPixelSize(R.dimen.item_padding_start);
        int spacingRight = getResources().getDimensionPixelSize(R.dimen.item_padding_end);

        builder.setTitle(R.string.label)
                .setView(mEditText, spacingLeft, 0, spacingRight, 0);

        AlertDialog dialog = super.createFrom(builder);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                showKeyboard(getActivity(), mEditText);
                mEditText.setSelection(0, mEditText.length());
            }
        });
        return dialog;
    }

    private void showKeyboard(FragmentActivity activity, View v) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    protected void onOk() {
        if (mOnLabelSetListener != null) {
            // If we passed the text back as an Editable (subtype of CharSequence
            // used in EditText), then there may be text formatting left in there,
            // which we don't want.
            mOnLabelSetListener.onLabelSet(mEditText.getText().toString());
        }
        dismiss();
    }

    public void setOnLabelSetListener(OnLabelSetListener l) {
        mOnLabelSetListener = l;
    }
}
