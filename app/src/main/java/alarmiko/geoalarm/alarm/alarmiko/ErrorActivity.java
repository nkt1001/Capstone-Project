package alarmiko.geoalarm.alarm.alarmiko;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ErrorActivity extends AppCompatActivity {

    public static final String EXTRA_DESCRIPTION = "alarmiko.geoalarm.alarm.alarmiko.ErrorActivity.extra.EXTRA_DESCRIPTION";
    @BindView(R.id.tv_error) TextView mTvError;
    @BindView(R.id.imageView_error) ImageView mImageViewError;
    @BindView(R.id.button_error) Button mBtnError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        String description = getIntent().getStringExtra(EXTRA_DESCRIPTION);

        ButterKnife.bind(this);

        if (description != null) {
            mTvError.setText(description);
        }
    }

    @OnClick(R.id.button_error)
    void onExitClicked() {
        this.finish();
    }
}
