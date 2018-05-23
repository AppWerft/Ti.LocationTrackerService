# Ti.LocationTrackerService
 
 Note: for apps running in the background on "O" devices (regardless of the targetSdkVersion),  location may be computed less frequently than requested when the app is not in the foreground.
 
Apps that use a foreground service -  which involves displaying a non-dismissable notification -  can bypass the background location limits and request location updates as before.
 
 This Titanium module uses a long-running bound and started service for location updates. The service is aware of foreground status of the window  which is the only bound client in this sample. 

After requesting location updates, when the window ceases to be in the foreground, the service promotes itself to a foreground service and continues receiving location updates.

 When the window comes back to the foreground, the foreground service stops, and the notification associated with that foreground service is removed.
 
 While the foreground service notification is displayed, the user has the option to launch the appfrom the notification. 
 

# Adding ti.playservices

```xml
<modules>
      <module platform="android">ti.playservices</module>
      <module platform="android">ti.locationtrackerservice</module>
 </modules>
```    

# Manifest

Don't forget these entries in you manifest:

```xml
<manifest>
       <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
       <uses-feature android:name="android.hardware.location.gps"/>
       <uses-sdk android:minSdkVersion="16" android:targetSdkVersion="26"/>
       <application>
                <service android:name="ti.locationtrackerservice.LocationUpdatesService"/>
       </application>
</manifest>
```
# Constants
## Location

* LOCATION\_PRIORITY\_BALANCED\_POWER_ACCURACY
* LOCATION\_PRIORITY\_HIGH_ACCURACY
* LOCATION\_PRIORITY\_LOW_POWER

## Visibility of notification
* NOTIFICATION\_VISIBILITY_PUBLIC
* NOTIFICATION\_VISIBILITY_PRIVATE
* NOTIFICATION\_VISIBILITY_SECRET

## Priority of notification	
Set the relative priority for this notification. Priority is an indication of how much of the user's valuable attention should be consumed by this notification. Low-priority notifications may be hidden from the user in certain situations, while the user might be interrupted for a higher-priority notification. The system sets a notification's priority based on various factors including the setPriority value. The effect may differ slightly on different platforms.

* NOTIFICATION\_PRIORITY_MAX
* NOTIFICATION\_PRIORITY_DEFAULT
* NOTIFICATION\_PRIORITY_HIGHT
* NOTIFICATION\_PRIORITY_MIN
* NOTIFICATION\_PRIORITY_LOW

## Importance of notification (since OREON)
Android uses the importance of a notification to determine how much the notification should interrupt the user (visually and audibly). The higher the importance of a notification, the more interruptive the notification will be.

Android uses the importance of a notification to determine how much the notification should interrupt the user (visually and audibly). The higher the importance of a notification, the more interruptive the notification will be.

On Android 8.0 (API level 26) and above, importance of a notification is determined by the importance of the channel the notification was posted to. Users can change the importance of a notification channel in the system settings (figure 12). On Android 7.1 (API level 25) and below, importance of each notification is determined by the notification's priority.

* NOTIFICATION\_IMPORTANCE_NONE
* NOTIFICATION\_IMPORTANCE_MIN
* NOTIFICATION\_IMPORTANCE_LOW
* NOTIFICATION\_IMPORTANCE_DEFAULT
* NOTIFICATION\_IMPORTANCE_HIGHT
* NOTIFICATION\_IMPORTANCE_MAX


# Usage

```javascript

var GeoService = require("ti.locationtrackerservice");
GeoService.addEventListener("ServiceConnectionChanged",function(e) {
	console.log(e);
});
´
var Tracker = GeoService.createTracker({
	lifecycleContainer : win, 
	interval : 10, //sec.
	priority : GeoService.LOCATION_PRIORITY_BALANCED_POWER_ACCURACY
	},
	onLocation // second paramter is optional, see below 
});

Tracker.setNotification({
	channelName : "My Geolocation",
	subText : "Text nearby (on left) of titlebar",
	contentTitle : "Title above the text",
	bigText : "The expandable text in more then two lines",
	contentText : "Longer text below {LOCATION}",
	importance : GeoService.NOTIFICATION_IMPORTANCE_HIGHT,
	lockscreenVisibility : GeoService.VISIBILITY_PRIVATE, // || SECRET || PUBLIC
	largeIcon : "https://avatars0.githubusercontent.com/u/2996237?s=460&v=4"  // optionalfor icon on right side

});

Tracker.setAdapter({  
	uri: "https://mybackend.com/endpoint?my_extra_paramter=1234",
	requestHeaders: ["Accesstoken:DE34B6721"],
	method : "POST", // or PUT
	timeout : 5, // for http request
	userName : "geotracker",
	password : "toosecrettoknow",
	ttl : 60,   // optional, life cycle of location, older will deleted and not send
	successCode  : 200  // this http result state is for db updating
});

function onLocation(e) {
	console.log(e.coords)
}

Tracker.addEventListener('location', onLocation);

//Alternatively you can use:
Tracker.onLocation = onLocation;
// or:
Tracker.setLocationCallback(onLocation);

Tracker.start();

// later:
Tracker.stop();

var link = Ti.Database.open(GeoService.DATABASE);
link.execSQL("SELECT * FROM " + GeoService.TABLE + " WHERE done=0 ORDER BY time");

```
The parameters for notification you can modify in res folder of module or as defaults.

## Reading location

You have fore ways:

* second parameter in createTracker()
* event `location`
* property of Tracker
* setLocationCallback() method of tracker

The result of event listener has the format:

```
{
	"provider":"fused",
	"bearing":0,
	"latitude":53.5644567,
	"accuracy":600,
	"time":1527063126671,
	"speed":0,
	"longitude":9.9829678
}
```




INSTALL YOUR MODULE
-------------------

Mac OS X
--------
Copy the distribution zip file into the `~/Library/Application Support/Titanium` folder

Linux
-----
Copy the distribution zip file into the `~/.titanium` folder

Windows
-------
Copy the distribution zip file into the `C:\ProgramData\Titanium` folder


REGISTER YOUR MODULE
--------------------

Register your module with your application by editing `tiapp.xml` and adding your module.
Example:

<modules>
	<module version="0.1">ti.locationtrackerservice</module>
</modules>

When you run your project, the compiler will combine your module along with its dependencies
and assets into the application.


USING YOUR MODULE IN CODE
-------------------------

To use your module in code, you will need to require it.

For example,

	var my_module = require('ti.locationupdatesservice');
	my_module.foo();


TESTING YOUR MODULE
-------------------

To test with the script, execute:

	titanium run --dir=YOURMODULEDIR

This will execute the app.js in the example folder as a Titanium application.


DISTRIBUTING YOUR MODULE
-------------------------

You can choose to manually distribute your module distribution zip file or through the Titanium Marketplace!


Cheers!
