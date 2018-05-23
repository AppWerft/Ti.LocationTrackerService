package ti.locationtrackerservice;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.greenrobot.eventbus.EventBus;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;

//import ti.locationtrackerservice.LocationupdatesserviceModule.MyReceiver;

@Kroll.proxy(creatableInModule = LocationtrackerserviceModule.class)
public class TrackerProxy extends KrollProxy {
	private KrollFunction onLocationCallback;
	private static String LCAT = LocationtrackerserviceModule.LCAT;

	private static final String PACKAGE_NAME = TiApplication.getInstance()
			.getPackageName();
	static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
	// The BroadcastReceiver used to listen from broadcasts from the service.
	private MyReceiver myReceiver;
	static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
	private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME
			+ ".started_from_notification";

	private Context ctx;
	public static String dbName = "geologger";
	public static String notificationChannel = "channel1";

	public static String database = null;
	public static int interval = 10; // sec.
	public static int duration = 0;
	public static int priority = LocationRequest.PRIORITY_LOW_POWER;
	final static String ACTION = "LocationUpdatesServiceAction";

	private KrollDict adapterOpts = null;
	private KrollDict notificationOpts = null;
	private KrollDict trackerOpts = null;

	private Messenger mService = null;

	final static String SERVICE_COMMAND_KEY = "SERVICECOMMANDKEY";
	SharedPreferences sharedPreferences;

	// A reference to the service used to get location updates.
	private LocationUpdatesService locationTrackingService = null;

	// Tracks the bound state of the service.
	private boolean boundState = false;
	// Monitors the state of the connection to the service.
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(LCAT, "ServiceConnection: locationTrackingService connected");
			locationTrackingService = ((LocationUpdatesService.LocalBinder) service)
					.getService();
			Log.i(LCAT, service.toString());

			// mService = new Messenger(service);
			// Log.i(LCAT, mService.toString());

			boundState = true;

			KrollDict res = new KrollDict();
			res.put("connected", true);
			if (hasListeners("ServiceConnectionChanged"))
				fireEvent("ServiceConnectionChanged", boundState);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			locationTrackingService = null;
			mService = null;
			boundState = false;
			KrollDict res = new KrollDict();
			res.put("connected", false);
			if (hasListeners("ServiceConnectionChanged"))
				fireEvent("ServiceConnectionChanged", res);
		}
	};

	private class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Location location = intent
					.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
			boolean isforeground = intent
					.getBooleanExtra("INFOREGROUND", false);
			KrollDict coords = new KrollDict();
			coords.put("time", location.getTime());
			coords.put("latitude", location.getLatitude());
			coords.put("longitude", location.getLongitude());
			coords.put("accuracy", location.getAccuracy());
			coords.put("bearing", location.getBearing());
			coords.put("provider", location.getProvider());
			coords.put("speed", location.getSpeed());
			KrollDict result = new KrollDict();
			result.put("coords", coords);
			// if (!isforeground) {
			try {
				if (location != null && hasListeners("location")) {
					fireEvent("location", result);
				}
				if (onLocationCallback != null) {
					onLocationCallback.call(getKrollObject(), result);
				}
				if (hasProperty("onLocation")) {
					KrollFunction onLocation = (KrollFunction) getProperty("onLocation");
					onLocation.call(getKrollObject(), result);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			// }
		}
	}

	public TrackerProxy() {
		super();
		ctx = TiApplication.getInstance().getApplicationContext();
		myReceiver = new MyReceiver();
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		Log.i(LCAT, "Module constructor ready");
	}

	@Override
	public void handleCreationArgs(KrollModule createdInModule, Object[] args) {
		if (args.length > 0 && args[0] instanceof HashMap) {
			this.trackerOpts = new KrollDict((HashMap) args[0]);
		}
		if (args.length == 2 && args[1] instanceof KrollFunction) {
			onLocationCallback = (KrollFunction) args[1];
		}
		Intent intent = new Intent(ctx, LocationUpdatesService.class);
		Log.i(LCAT,
				"bindService in onStart of module was successful: "
						+ ctx.bindService(intent, mServiceConnection,
								Context.BIND_AUTO_CREATE));
		super.handleCreationArgs(createdInModule, args);
	}

	@Kroll.method
	public void setNotification(KrollDict opts) {
		notificationOpts = opts;
	}

	@Kroll.method
	public void setLocationCallback(KrollFunction fn) {
		onLocationCallback = fn;
	}

	@Kroll.method
	public void start() {
		if (checkPermissions())
			requestLocationUpdates();
		else
			Toast.makeText(TiApplication.getAppCurrentActivity(),
					"Module needs location permission", Toast.LENGTH_LONG)
					.show();

	}

	@Kroll.method
	public void requestLocationUpdates() {
		if (locationTrackingService != null) {

			locationTrackingService.requestLocationUpdates(adapterOpts,
					notificationOpts, trackerOpts);
		} else
			Log.e(LCAT,
					"locationTrackingService was null, cannot requestLocationUpdates");
	};

	@Kroll.method
	public void stop() {
		removeLocationUpdates();
	}

	@Kroll.method
	public void removeLocationUpdates() {
		Log.i(LCAT, "removeLocationUpdates from JS");
		if (locationTrackingService != null) {
			locationTrackingService.removeLocationUpdates();

		} else
			Log.e(LCAT,
					"locationTrackingService was null, cannot removeLocationUpdates");
		if (boundState && mServiceConnection != null) {
			// Unbind from the service. This signals to the service that this
			// activity is no longer
			// in the foreground, and the service can respond by promoting
			// itself to a foreground
			// service.
			ctx.unbindService(mServiceConnection);
			boundState = false;
		}

	}

	@Kroll.method
	public void setAdapter(KrollDict opts) {
		adapterOpts = opts;
	}

	private static String getApplicationName(Context context) {
		ApplicationInfo applicationInfo = context.getApplicationInfo();
		int stringId = applicationInfo.labelRes;
		return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString()
				: context.getString(stringId);
	}

	/* Lifecycles */

	/*
	 * @Override public void onCreate(Bundle b) { super.onCreate(b); }
	 */

	@Override
	public void onStart(Activity activity) {
		super.onStart(activity);
		Log.d(LCAT, ">>>>> onStart called");
		// Bind to the service. If the service is in foreground mode, this
		// signals to the service
		// that since this activity is in the foreground, the service can exit
		// foreground mode.
		ctx.bindService(new Intent(ctx, LocationUpdatesService.class),
				mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onResume(Activity activity) {
		super.onResume(activity);
		Log.d(LCAT, ">>>>> onResume called");
		LocalBroadcastManager.getInstance(ctx).registerReceiver(myReceiver,
				new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
	}

	@Override
	public void onPause(Activity activity) {
		Log.d(LCAT, "<<<<<< onPause called");
		LocalBroadcastManager.getInstance(ctx).unregisterReceiver(myReceiver);
		super.onPause(activity);
	}

	@Override
	public void onStop(Activity activity) {
		Log.d(LCAT, "<<<<<< onStop called");
		if (boundState) {
			// Unbind from the service. This signals to the service that this
			// activity is no longer
			// in the foreground, and the service can respond by promoting
			// itself to a foreground
			// service.
			ctx.unbindService(mServiceConnection);
			boundState = false;
		}
		super.onStop(activity);
	}

	@Override
	public void onDestroy(Activity activity) {
		if (locationTrackingService != null)
			locationTrackingService.removeLocationUpdates();
		super.onDestroy(activity);
	}

	private boolean checkPermissions() {
		return PackageManager.PERMISSION_GRANTED == ActivityCompat
				.checkSelfPermission(ctx,
						Manifest.permission.ACCESS_FINE_LOCATION)
				|| PackageManager.PERMISSION_GRANTED == ActivityCompat
						.checkSelfPermission(ctx,
								Manifest.permission.ACCESS_COARSE_LOCATION);
	}

}
