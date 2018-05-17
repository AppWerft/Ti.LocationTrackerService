package ti.locationtrackerservice;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.greenrobot.eventbus.EventBus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.location.Location;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.PreferenceManager;

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

	final static String SERVICE_COMMAND_KEY = "SERVICECOMMANDKEY";
	SharedPreferences sharedPreferences;

	// A reference to the service used to get location updates.
	private LocationUpdatesService locationTrackingService = null;
	// https://stackoverflow.com/questions/6454668/sending-stringdata-from-activity-to-service-android
	private Messenger mService = null;
	private KrollDict adapter;
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
			Log.i(LCAT, service.toString());

			KrollDict res = new KrollDict();
			res.put("connected", true);
			Log.i(LCAT, res.toString());
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
			KrollDict res = new KrollDict();
			res.put("time", location.getTime());
			res.put("latitude", location.getLatitude());
			res.put("longitude", location.getLongitude());
			res.put("accuracy", location.getAccuracy());
			res.put("bearing", location.getBearing());
			res.put("provider", location.getProvider());
			res.put("speed", location.getSpeed());
			Log.i(LCAT, res.toString());
			if (!isforeground) {
				try {
					if (location != null && hasListeners("location")) {
						fireEvent("location", res);
					}
					if (onLocationCallback != null) {
						onLocationCallback.call(getKrollObject(), res);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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
		if (args.length == 2 && args[0] instanceof KrollDict
				&& args[0] instanceof KrollFunction) {
			KrollDict opts = (KrollDict) args[0];
			onLocationCallback = (KrollFunction) args[1];
			EventBus.getDefault().post(new Messages.TrackerEvent(opts));
		}
	}

	@Kroll.method
	public void removeLocationUpdates(
			@Kroll.argument(optional = true) Object object) {
		Log.i(LCAT, "removeLocationUpdates from JS");
		if (locationTrackingService != null)
			locationTrackingService.removeLocationUpdates();
		else
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
	public void addAdapter(Object o) {
		if (o instanceof AdapterProxy) {
			adapter = ((AdapterProxy) o).getAdapter();
			EventBus.getDefault().post(new Messages.AdapterEvent(adapter));
		}
	}

	private static String getApplicationName(Context context) {
		ApplicationInfo applicationInfo = context.getApplicationInfo();
		int stringId = applicationInfo.labelRes;
		return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString()
				: context.getString(stringId);
	}

	/* Lifecycles */
	@Override
	public void onStart(Activity activity) {
		Intent intent = new Intent(ctx, LocationUpdatesService.class);
		Log.i(LCAT,
				"bindService in onStart of module was successful: "
						+ ctx.bindService(intent, mServiceConnection,
								Context.BIND_AUTO_CREATE));
		super.onStart(activity);
	}

}
