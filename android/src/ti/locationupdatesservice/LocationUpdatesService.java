package ti.locationupdatesservice;

import java.io.IOException;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.io.TiBaseFile;
import org.appcelerator.titanium.io.TiFileFactory;
import org.appcelerator.titanium.util.TiUIHelper;

import android.R;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * A bound and started service that is promoted to a foreground service when
 * location updates have been requested and all clients unbind.
 *
 * For apps running in the background on "O" devices, location is computed only
 * once every 10 minutes and delivered batched every 30 minutes. This
 * restriction applies even to apps targeting "N" or lower which are run on "O"
 * devices.
 *
 * This sample show how to use a long-running service for location updates. When
 * an activity is bound to this service, frequent location updates are
 * permitted. When the activity is removed from the foreground, the service
 * promotes itself to a foreground service, and location updates continue. When
 * the activity comes back to the foreground, the foreground service stops, and
 * the notification assocaited with that service is removed.
 */
public class LocationUpdatesService extends Service {

	private static final String PACKAGE_NAME = TiApplication.getInstance()
			.getPackageName();

	private static final String TAG = LocationUpdatesService.class
			.getSimpleName();
	Context ctx;
	/**
	 * The name of the channel for notifications.
	 */
	private static final String CHANNEL_ID = "channel_01";

	static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

	static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
	private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME
			+ ".started_from_notification";
	private Notification.Builder builder;
	private final IBinder mBinder = new LocalBinder();

	/**
	 * The desired interval for location updates. Inexact. Updates may be more
	 * or less frequent.
	 */
	private static final long UPDATE_INTERVAL_IN_MILLISECONDS = LocationupdatesserviceModule.interval;

	/**
	 * The fastest rate for active location updates. Updates will never be more
	 * frequent than this value.
	 */
	private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

	/**
	 * The identifier for the notification displayed for the foreground service.
	 */
	private static final int NOTIFICATION_ID = 12345678;

	/**
	 * Used to check whether the bound activity has really gone away and not
	 * unbound as part of an orientation change. We create a foreground service
	 * notification only if the former takes place.
	 */
	private boolean mChangingConfiguration = false;

	private NotificationManager mNotificationManager;

	/**
	 * Contains parameters used by
	 * {@link com.google.android.gms.location.FusedLocationProviderApi}.
	 */
	private LocationRequest mLocationRequest;

	/**
	 * Provides access to the Fused Location Provider API.
	 */
	private FusedLocationProviderClient mFusedLocationClient;

	/**
	 * Callback for changes in location.
	 */
	private LocationCallback mLocationCallback;

	private Handler mServiceHandler;
	private static String LCAT = LocationupdatesserviceModule.LCAT;
	/**
	 * The current location.
	 */
	private Location mLocation;

	public LocationUpdatesService() {
		super();
		ctx = TiApplication.getInstance().getApplicationContext();
	}

	@Override
	public void onCreate() {
		mFusedLocationClient = LocationServices
				.getFusedLocationProviderClient(this);

		mLocationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				super.onLocationResult(locationResult);
				onNewLocation(locationResult.getLastLocation());
			}
		};

		createLocationRequest();
		getLastLocation();

		HandlerThread handlerThread = new HandlerThread(TAG);
		handlerThread.start();
		mServiceHandler = new Handler(handlerThread.getLooper());
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Android O requires a Notification Channel.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = LocationupdatesserviceModule.notificationName;
			// Create the channel for the notification
			NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,
					name, NotificationManager.IMPORTANCE_DEFAULT);

			// Set the Notification Channel for the Notification Manager.
			mNotificationManager.createNotificationChannel(mChannel);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Service started");
		boolean startedFromNotification = intent.getBooleanExtra(
				EXTRA_STARTED_FROM_NOTIFICATION, false);

		// We got here because the user decided to remove location updates from
		// the notification.
		if (startedFromNotification) {
			removeLocationUpdates();
			stopSelf();
		}
		// Tells the system to not try to recreate the service after it has been
		// killed.
		return START_NOT_STICKY;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mChangingConfiguration = true;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// Called when a client (MainActivity in case of this sample) comes to
		// the foreground
		// and binds with this service. The service should cease to be a
		// foreground service
		// when that happens.
		Log.i(TAG, "in onBind()");
		stopForeground(true);
		mChangingConfiguration = false;
		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		// Called when a client (MainActivity in case of this sample) returns to
		// the foreground
		// and binds once again with this service. The service should cease to
		// be a foreground
		// service when that happens.
		Log.i(TAG, "in onRebind()");
		stopForeground(true);
		mChangingConfiguration = false;
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "Last client unbound from service");

		// Called when the last client (MainActivity in case of this sample)
		// unbinds from this
		// service. If this method is called due to a configuration change in
		// MainActivity, we
		// do nothing. Otherwise, we make this service a foreground service.
		if (!mChangingConfiguration && Utils.requestingLocationUpdates(this)) {
			Log.i(TAG, "Starting foreground service");
			/*
			 * // TODO(developer). If targeting O, use the following code. if
			 * (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
			 * mNotificationManager.startServiceInForeground(new Intent(this,
			 * LocationUpdatesService.class), NOTIFICATION_ID,
			 * getNotification()); } else { startForeground(NOTIFICATION_ID,
			 * getNotification()); }
			 */
			startForeground(NOTIFICATION_ID, getNotification());
		}
		return true; // Ensures onRebind() is called when a client re-binds.
	}

	@Override
	public void onDestroy() {
		mServiceHandler.removeCallbacksAndMessages(null);
	}

	/**
	 * Makes a request for location updates. Note that in this sample we merely
	 * log the {@link SecurityException}.
	 */
	public void requestLocationUpdates() {
		Log.i(TAG, "Requesting location updates");
		Utils.setRequestingLocationUpdates(this, true);
		startService(new Intent(getApplicationContext(),
				LocationUpdatesService.class));
		try {
			mFusedLocationClient.requestLocationUpdates(mLocationRequest,
					mLocationCallback, Looper.myLooper());
		} catch (SecurityException unlikely) {
			Utils.setRequestingLocationUpdates(this, false);
			Log.e(TAG, "Lost location permission. Could not request updates. "
					+ unlikely);
		}
	}

	/**
	 * Removes location updates. Note that in this sample we merely log the
	 * {@link SecurityException}.
	 */
	public void removeLocationUpdates() {
		Log.i(TAG, "Removing location updates");
		try {
			mFusedLocationClient.removeLocationUpdates(mLocationCallback);
			Utils.setRequestingLocationUpdates(this, false);
			stopSelf();
		} catch (SecurityException unlikely) {
			Utils.setRequestingLocationUpdates(this, true);
			Log.e(TAG, "Lost location permission. Could not remove updates. "
					+ unlikely);
		}
	}

	/**
	 * Returns the {@link NotificationCompat} used as part of the foreground
	 * service.
	 */
	@SuppressWarnings("deprecation")
	private Notification getNotification() {
		Intent intent = new Intent(this, LocationUpdatesService.class);

		CharSequence text = Utils.getLocationText(mLocation);

		// Extra to help us figure out if we arrived in onStartCommand via the
		// notification or not.
		intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

		// The PendingIntent that leads to a call to onStartCommand() in this
		// service.
		PendingIntent servicePendingIntent = PendingIntent.getService(ctx, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// The activityIntent calls the app
		Intent activityIntent = new Intent(Intent.ACTION_MAIN);
		activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		String packageName = TiApplication.getInstance().getPackageName();
		String className = packageName
				+ "."
				+ TiApplication.getAppRootOrCurrentActivity()
						.getLocalClassName();

		activityIntent.setComponent(new ComponentName(packageName, className));

		PendingIntent activityPendingIntent = PendingIntent.getActivity(ctx, 1,
				activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// PendingIntent activityPendingIntent = PendingIntent.getActivity(this,
		// 0,
		// new Intent(this, MainActivity.class), 0);

		builder = new Notification.Builder(ctx, CHANNEL_ID)
				.addAction(R("tracker_launch_icon", "drawable"),
						LocationupdatesserviceModule.startTracking,
						activityPendingIntent)
				.addAction(R("tracker_cancel_icon", "drawable"),
						LocationupdatesserviceModule.stopTracking,
						servicePendingIntent)
				.setContentTitle(Utils.getLocationTitle(ctx)).setOngoing(true)
				.setPriority(Notification.PRIORITY_HIGH)
				// .setSmallIcon(R.mipmap.ic_launcher).setTicker(text)
				.setWhen(System.currentTimeMillis());

		return builder.build();
	}

	private void getLastLocation() {
		try {
			mFusedLocationClient.getLastLocation().addOnCompleteListener(
					new OnCompleteListener<Location>() {
						@Override
						public void onComplete(@NonNull Task<Location> task) {
							if (task.isSuccessful() && task.getResult() != null) {
								mLocation = task.getResult();
							} else {
								Log.w(TAG, "Failed to get location.");
							}
						}
					});
		} catch (SecurityException unlikely) {
			Log.e(TAG, "Lost location permission." + unlikely);
		}
	}

	private void onNewLocation(Location location) {
		Log.i(TAG, "New location: " + location);

		mLocation = location;

		// Notify anyone listening for broadcasts about the new location.
		Intent intent = new Intent(ACTION_BROADCAST);
		intent.putExtra(EXTRA_LOCATION, location);
		LocalBroadcastManager.getInstance(getApplicationContext())
				.sendBroadcast(intent);
		if (serviceIsRunningInForeground(ctx)) {
			mNotificationManager.notify(NOTIFICATION_ID, getNotification());
		}
	}

	/**
	 * Sets the location request parameters.
	 */
	private void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest
				.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	/**
	 * Class used for the client Binder. Since this service runs in the same
	 * process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		LocationUpdatesService getService() {
			return LocationUpdatesService.this;
		}
	}

	/**
	 * Returns true if this is a foreground service.
	 *
	 * @param context
	 *            The {@link Context}.
	 */
	public boolean serviceIsRunningInForeground(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (getClass().getName().equals(service.service.getClassName())) {
				if (service.foreground) {
					return true;
				}
			}
		}
		return false;
	}

	/* helper function for safety getting resources */
	private int R(String name, String type) {
		int id = 0;
		try {
			id = ctx.getResources().getIdentifier(name, type,
					ctx.getPackageName());
		} catch (Exception e) {
			return id;
		}
		return id;
	}

}