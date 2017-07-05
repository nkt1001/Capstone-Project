package alarmiko.geoalarm.alarm.alarmiko.ui;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import alarmiko.geoalarm.alarm.alarmiko.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nkt01 on 19.03.2017. Address view loads and show address by {@link LatLng}
 */

public class AddressView extends FrameLayout {

    private static final int MESSAGE_HANDLER = 682;
    private static final int ANIMATION_DURATION = 300;

    @BindView(R.id.tv_address_view)
    TextView tvAddress;
    @BindView(R.id.pb_address_view)
    ProgressBar pbLoad;

    private AddressLoader mAddressLoader;
    private int loadHashCode;

    private AddressLoaderListener mAddressLoaderListener;

    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            String result = (String) message.obj;

            if (message.arg1 == loadHashCode) {
                setAddress(result);
            }

            mAddressLoaderListener.onAddressLoaded(result);

            return true;
        }
    });

    public AddressView(Context context) {
        this(context, null);
    }

    public AddressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View rootView = inflate(getContext(), R.layout.address_view_layout, this);
        ButterKnife.bind(this, rootView);
//        pbLoad = (ProgressBar) rootView.findViewById(R.id.pb_address_view);
//        tvAddress = (TextView) rootView.findViewById(R.id.tv_address_view);
        pbLoad.setAlpha(0f);

        tvAddress.setAlpha(0f);
    }

    public void addOnAddressLoadedListener(AddressLoaderListener listener) {
        mAddressLoaderListener = listener;
    }

    public void hide() {
        interruptLoading();

        tvAddress.animate().alpha(0f).setDuration(ANIMATION_DURATION).start();
        pbLoad.animate().alpha(0f).setDuration(ANIMATION_DURATION).start();
    }

    public void loadAddress(LatLng latLng) {
        pbLoad.animate().alpha(1f).setDuration(ANIMATION_DURATION).start();
        executeLoading(latLng);
    }

    private void executeLoading(LatLng latLng) {

        interruptLoading();

        mAddressLoader = new AddressLoader(getContext(), latLng);
        loadHashCode = mAddressLoader.hashCode();
        mAddressLoader.start();
    }

    private void interruptLoading() {
        if (mAddressLoader != null) {

            loadHashCode = 0;

            mAddressLoader.interrupt();
            mAddressLoader = null;
        }
    }

    public String getAddressString() {
        return tvAddress.getText().toString();
    }

    private void setAddress(String result) {
        if (result == null) {
            hide();
            return;
        }

        tvAddress.setText(result);
        pbLoad.animate().alpha(0f).setDuration(ANIMATION_DURATION).start();
        tvAddress.animate().alpha(1f).setDuration(ANIMATION_DURATION).start();
    }

    public interface AddressLoaderListener {
        void onAddressLoaded(String address);
    }

    private class AddressLoader extends Thread {

        private Geocoder mmGeoCoder;
        private LatLng mmLatLng;

        public AddressLoader(Context context, LatLng mmLatLng) {
            this.mmLatLng = mmLatLng;
            mmGeoCoder = new Geocoder(context, Locale.getDefault());
        }

        @Override
        public void run() {
            String address = null;

            try {
                List<Address> addresses = mmGeoCoder.getFromLocation(mmLatLng.latitude, mmLatLng.longitude, 1);

                if (addresses.size() > 0) {
                    Address addressObj = addresses.get(0);
                    address = addressObj.getAddressLine(0);
                }

            } catch (IOException e) {
                Log.d("AddressView", "run: catched");
                e.printStackTrace();
            } finally {
                mHandler.obtainMessage(MESSAGE_HANDLER, hashCode(), 0, address).sendToTarget();
            }
        }
    }
}
