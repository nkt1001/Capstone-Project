package alarmiko.geoalarm.alarm.alarmiko;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ErrorActivity extends AppCompatActivity {

    @BindView(R.id.tv_error) TextView mTvError;
    @BindView(R.id.imageView_error) ImageView mImageViewError;
    @BindView(R.id.button_error) Button mBtnError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        ButterKnife.bind(this);
    }
}
