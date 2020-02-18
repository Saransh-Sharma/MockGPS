package saransh1337.mockgps;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class MockLocationProvider2 implements SharedPreferences.OnSharedPreferenceChangeListener {
    static String TAG = "MockGPS";
    static String locationProviderName = LocationManager.GPS_PROVIDER;

    static private MockLocationProvider2 instance = new MockLocationProvider2();
    static public MockLocationProvider2 getInstance() { return instance; }
    private MockLocationProvider2() {}

    protected Context mContext;
    protected LocationManager mLocationManager;
    protected SharedPreferences mPref = null;
    protected int accuracy = 10;

    static public void init(Context context) {
        getInstance()._init(context);
    }
    static public void register() { getInstance()._register(); } //Call these in base test case setup

    static public void unregister() { getInstance()._unregister(); } //Call this in BaseTestCase tear down

    static public void unregisterGPS_PROVIDER() { getInstance()._unregister(); }

    static public void setMockLocation(double longitude, double latitude) {
        getInstance()._verifyInitiated();
        getInstance()._setMockLocation(longitude, latitude, 0, -1);
    }

    static public void setMockLocation(double longitude, double latitude, double altitude) {
        getInstance()._verifyInitiated();
        getInstance()._setMockLocation(longitude, latitude, altitude, -1);
    }

    static public void setMockLocation(double longitude, double latitude, double altitude,
                                       int satellites) {
        getInstance()._verifyInitiated();
        getInstance()._setMockLocation(longitude, latitude, altitude, satellites);
    }


    static public Location getLocation() {
        return new Location(locationProviderName);
    }

    protected void _init(Context context) {
        if (mContext != null) {
            Log.d(TAG,"Inti called twice !");
            throw new AssertionError(TAG+".init called twice!");
        }
        mContext = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
        mPref.registerOnSharedPreferenceChangeListener(this);
        accuracy = parseAccuracy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) { //this is how we should pass location data from the test to MockLocationProvider
        if (key.equals("location")) {
            accuracy = parseAccuracy();
        }
    }

    protected void _register() {
        // if the test provider already exists, android handles this fine
        try {
            mLocationManager.addTestProvider(locationProviderName, false, false, false,
                    false, true, true, true, 0, accuracy);
            mLocationManager.setTestProviderEnabled(locationProviderName, true);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "IllegalArgumentException thrown in _register");
        }
    }

    protected void _unregister() {
        try {
            mLocationManager.removeTestProvider(locationProviderName);
        } catch(Exception ignored) {}
    }



    protected void _setMockLocation(double longitude, double latitude, double altitude, int satellites) {
        Location mockLocation = new Location(locationProviderName); // a string
        mockLocation.setLatitude(latitude);  // double
        mockLocation.setLongitude(longitude);
        mockLocation.setAltitude(altitude);
        if (satellites != -1) {
            Bundle bundle = new Bundle();
            bundle.putInt("satellites", satellites);
            mockLocation.setExtras(bundle);
        }
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setAccuracy(10);

        mockLocation.setElapsedRealtimeNanos(200);

        Log.d(TAG,"****************************************");
        Log.d(TAG,"Provider: "+mockLocation.getProvider());
        Log.d(TAG,"Altitude is: "+mockLocation.getAltitude());
        Log.d(TAG,"Longitude: "+mockLocation.getLongitude());
        Log.d(TAG,"Latitude: "+mockLocation.getLatitude());
        Log.d(TAG,"****************************************\n");

        _setMockLocation(mockLocation);

    }

    protected void _setMockLocation(Location mockLocation) {
        if (!mockLocation.hasAccuracy()) {
            mockLocation.setAccuracy(accuracy);
        }
        if (!mockLocation.hasAltitude()) {
            mockLocation.setAltitude(0);
        }

        mLocationManager.setTestProviderLocation(locationProviderName, mockLocation); // actual location bing set

    }


    protected void _verifyInitiated() {
        if (mContext == null) {
            Log.d(TAG, "APP CONTEXT IS NULL !");
            throw new AssertionError("CommandDispatcher.init has not been called!");
        }
    }

    private int parseAccuracy() {
        String str = mPref.getString("accuracy", "1");
        int ret;
        try {
            ret = Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            Log.e(TAG, String.format("Invalid accuracy %s. Defaulting to 1", str));
            ret = 1;
        }
        return ret;
    }

}
