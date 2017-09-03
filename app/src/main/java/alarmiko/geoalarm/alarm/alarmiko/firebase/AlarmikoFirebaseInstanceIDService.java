package alarmiko.geoalarm.alarm.alarmiko.firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class AlarmikoFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "AlarmikoFirebaseInstanc";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
    }
}
