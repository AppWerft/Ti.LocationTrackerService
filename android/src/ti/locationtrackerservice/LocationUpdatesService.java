package ti.locationtrackerservice;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.TiApplication;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import ti.locationtrackerservice.Messages.AdapterEvent;
import ti.locationtrackerservice.Messages.NotificationEvent;
import ti.locationtrackerservice.Messages.TrackerEvent;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
//import android.support.v4.app.NotificationCompat.BigTextStyle;
//import android.support.annotation.NonNull;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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
	private static final String DATABASE = LocationtrackerserviceModule.DATABASE;
	private static final String TABLE = LocationtrackerserviceModule.TABLE;
	static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

	static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
	private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME
			+ ".started_from_notification";

	// Binder given to clients
	private final IBinder mBinder = new LocalBinder();

	/**
	 * The desired interval for location updates. Inexact. Updates may be more
	 * or less frequent.
	 */
	private KrollDict notificationOpts = new KrollDict();
	private KrollDict adapterOpts = new KrollDict();

	private KrollDict trackerOpts = new KrollDict();

	private static String contentTitle = null;
	private String contentText = null;

	private int priority = 102;
	private int interval = 10;
	private final long UPDATE_INTERVAL_IN_MILLISECONDS = interval;
	ServerAdapter serverAdapter;

	/**
	 * The fastest rate for active location updates. Updates will never be more
	 * frequent than this value.
	 */
	private final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

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
	private static String LCAT = "💎" + LocationtrackerserviceModule.LCAT;
	/**
	 * The current location.
	 */
	private Location mLocation;
	private String packageName;
	private String className;

	public LocationUpdatesService() {
		// super();
		trackerOpts.put("interval", 10000);
		trackerOpts.put("priority", 104);
		ctx = TiApplication.getInstance().getApplicationContext();
		packageName = TiApplication.getInstance().getPackageName();
		className = packageName
				+ "."
				+ TiApplication.getAppRootOrCurrentActivity()
						.getLocalClassName();
		notificationOpts.put("subText", getString(R("subText", "string")));
		notificationOpts.put("contentText",
				getString(R("contentText", "string")));
		notificationOpts.put("contentTitle",
				getString(R("contentTitle", "string")));
		notificationOpts.put("channel", "Channel");

	}

	@Override
	public void onCreate() {
		Log.i(LCAT, "------  onCreate of services");
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
			CharSequence name = notificationOpts.getString("channel");
			// Create the channel for the notification
			NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,
					name, NotificationManager.IMPORTANCE_DEFAULT);

			// Set the Notification Channel for the Notification Manager.
			mNotificationManager.createNotificationChannel(mChannel);

		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		EventBus.getDefault().register(this);
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
		Log.i(LCAT, "~~~~~~> in onBind()");
		stopForeground(true);
		mChangingConfiguration = false;
		EventBus.getDefault().register(this);
		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		// Called when a client (MainActivity in case of this sample) returns to
		// the foreground
		// and binds once again with this service. The service should cease to
		// be a foreground
		// service when that happens.
		Log.i(LCAT, "< ~~~~~ in onRebind()");
		stopForeground(true);
		mChangingConfiguration = false;
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(LCAT, "Last client unbound from service mChangingConfiguration="
				+ mChangingConfiguration + "  requestingLocationUpdates="
				+ Utils.requestingLocationUpdates(this));

		// Called when the last client (MainActivity in case of this sample)
		// unbinds from this
		// service. If this method is called due to a configuration change in
		// MainActivity, we
		// do nothing. Otherwise, we make this service a foreground service.
		if (!mChangingConfiguration && Utils.requestingLocationUpdates(this)) {
			Log.i(LCAT, "Starting foreground service");
			/*
			 * // TODO(developer). If targeting O, use the following code. if
			 * (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
			 * mNotificationManager.startServiceInForeground(new Intent(this,
			 * LocationUpdatesService.class), NOTIFICATION_ID,
			 * getNotification()); } else { startForeground(NOTIFICATION_ID,
			 * getNotification()); }
			 */
			startForeground(NOTIFICATION_ID, getNotification());
		} else
			Log.w(LCAT, "onUnbind: was only a confchanging");
		EventBus.getDefault().unregister(this);
		return true; // Ensures onRebind() is called when a client re-binds.
	}

	@Subscribe
	public void onNotificationEvent(NotificationEvent event) {
		for (String key : event.message.keySet()) {
			notificationOpts.put(key, event.message.getString(key));
		}
	}

	@Subscribe
	public void onTrackerEvent(TrackerEvent event) {
		if (event.message.containsKeyAndNotNull("interval"))
			interval = event.message.getInt("interval");
		if (event.message.containsKeyAndNotNull("priority"))
			priority = event.message.getInt("priority");
	}

	@Subscribe
	public void onAdapterEvent(AdapterEvent event) {
		for (String key : event.message.keySet()) {
			adapterOpts.put(key, event.message.getString(key));
		}
		serverAdapter = new ServerAdapter(ctx, adapterOpts);

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
		Log.i(LCAT, "––––––––-  Requesting location updates " + contentText);
		Utils.setRequestingLocationUpdates(this, true);
		startService(new Intent(getApplicationContext(),
				LocationUpdatesService.class));
		try {
			mFusedLocationClient.requestLocationUpdates(mLocationRequest,
					mLocationCallback, Looper.myLooper());
		} catch (SecurityException unlikely) {
			Utils.setRequestingLocationUpdates(this, false);
			Log.e(LCAT, "Lost location permission. Could not request updates. "
					+ unlikely);
		}
	}

	/**
	 * Removes location updates. Note that in this sample we merely log the
	 * {@link SecurityException}.
	 */
	public void removeLocationUpdates() {
		Log.i(LCAT, "Removing location updates inside service");
		mNotificationManager.cancelAll();
		try {
			mFusedLocationClient.removeLocationUpdates(mLocationCallback);
			Log.i(LCAT, "removedLocationUpdates from mFusedLocationClient");
			Utils.setRequestingLocationUpdates(this, false);
			stopSelf();
			Log.i(LCAT, "stopSelf ");
		} catch (SecurityException unlikely) {
			Utils.setRequestingLocationUpdates(this, true);
			Log.e(LCAT, "Lost location permission. Could not remove updates. "
					+ unlikely);
		}
	}

	/**
	 * Returns the {@link NotificationCompat} used as part of the foreground
	 * service.
	 */
	@SuppressWarnings("deprecation")
	private Notification getNotification() {
		Log.i(LCAT, "getNotification started");
		Intent intent = new Intent(this, LocationUpdatesService.class);
		intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);
		// The activityIntent calls the app
		Intent activityIntent = new Intent(Intent.ACTION_MAIN);
		activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		activityIntent.setComponent(new ComponentName(packageName, className));
		PendingIntent activityPendingIntent = PendingIntent.getActivity(ctx, 1,
				activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		// https://stackoverflow.com/questions/45462666/notificationcompat-builder-deprecated-in-android-o
		final Notification.Builder builder = new Notification.Builder(ctx);
		Uri defaultSoundUri = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		builder.setContentTitle(contentTitle).setOngoing(true)
				.setPriority(Notification.FLAG_HIGH_PRIORITY)
				.setContentIntent(activityPendingIntent)
				.setSmallIcon(R("ic_launcher", "mipmap"))
				.setSubText(notificationOpts.getString("subText"))
				.setSound(defaultSoundUri)
				.setContentText(notificationOpts.getString("contentText"))
				.setContentTitle(notificationOpts.getString("contentTitle"))
				.setVibrate(null).setWhen(System.currentTimeMillis());
		if (notificationOpts.containsKeyAndNotNull("bigText")) {
			CharSequence bigText = notificationOpts.getString("bigText");
			BigTextStyle style = new Notification.BigTextStyle()
					.bigText(bigText);
			// builder.setStyle(style);
		}
		if (notificationOpts.containsKeyAndNotNull("largeIcon")) {
			String largeIcon = notificationOpts.getString("largeIcon");
			final Target target = new Target() {
				@Override
				public void onBitmapLoaded(Bitmap bitmap,
						Picasso.LoadedFrom from) {
					builder.setLargeIcon(bitmap);

				}

				@Override
				public void onBitmapFailed(Drawable errorDrawable) {
					Log.e(LCAT, "bitMap failed ");
				}

				@Override
				public void onPrepareLoad(Drawable placeHolderDrawable) {
				}
			};
			Picasso.with(ctx).load(largeIcon).resize(150, 150).into(target);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			builder.setChannelId(CHANNEL_ID); // Channel ID
		}
		Log.i(LCAT, builder.toString());
		return builder.build();
	}

	private void getLastLocation() {
		try {
			mFusedLocationClient.getLastLocation().addOnCompleteListener(
					new OnCompleteListener<Location>() {
						@Override
						public void onComplete(Task<Location> task) {
							if (task.isSuccessful() && task.getResult() != null) {
								mLocation = task.getResult();
							} else {
								Log.w(LCAT, "Failed to get location.");
							}
						}
					});
		} catch (SecurityException unlikely) {
			Log.e(LCAT, "Lost location permission." + unlikely);
		}
	}

	private void onNewLocation(Location location) {
		mLocation = location;
		boolean isForeground = serviceIsRunningInForeground(ctx);
		if (isForeground || true) {
			Log.i(LCAT, "Foreground");
			mNotificationManager.notify(NOTIFICATION_ID, getNotification());
		} else
			Log.i(LCAT, "Backgroundground");
		// Notify anyone listening for broadcasts about the new location.
		Intent intent = new Intent(ACTION_BROADCAST);
		intent.putExtra(EXTRA_LOCATION, location);
		intent.putExtra("INFOREGROUND", isForeground);
		LocalBroadcastManager.getInstance(getApplicationContext())
				.sendBroadcast(intent);
		SQLiteDatabase db = ctx.openOrCreateDatabase(DATABASE, MODE_PRIVATE,
				null);
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TABLE
				+ "(Latitude Real,Longitude Real, time Integer,  Speed Real, Accuracy Real,Done Integer);");
		Object[] values = new Object[] { location.getLatitude(),
				location.getLongitude(), location.getTime(),
				location.getSpeed(), location.getAccuracy(), 0 };
		db.execSQL("INSERT INTO " + DATABASE + " VALUES(?,?,?,?,?,?,?)", values);

		db.close();
		serverAdapter.Sync();
	}

	/**
	 * Sets the location request parameters.
	 */
	private void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(trackerOpts.getInt("interval"));
		mLocationRequest.setFastestInterval(trackerOpts.getInt("interval"));
		Log.i(LCAT, "setPriority: " + priority);
		if (priority > 0)
			mLocationRequest.setPriority(trackerOpts.getInt("priority"));
	}

	/**
	 * Class used for the client Binder. Since this service runs in the same
	 * process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		LocationUpdatesService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return LocationUpdatesService.this;
		}
	}

	/**
	 * Returns true if this is a foreground service.
	 *
	 * @param context
	 *            The {@link Context}.
	 */
	@SuppressWarnings("deprecation")
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