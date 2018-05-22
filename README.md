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

* PRIORITY\_BALANCED\_POWER_ACCURACY
* PRIORITY\_HIGH_ACCURACY
* PRIORITY\_LOW_POWER
* VISIBILITY_PUBLIC
* VISIBILITY_PRIVATE
* VISIBILITY_SECRET


# Usage

```javascript

var GeoService = require("ti.locationtrackerservice");
GeoService.addEventListener("ServiceConnectionChanged",function(e) {
	console.log(e);
});
´
var Tracker = GeoService.createTracker({
	lifecycleContainer : win, 
	interval :10, //sec.
	priority : GeoService.PRIORITY_BALANCED_POWER_ACCURACY,
   },function(){ // callback is optional
		console.log(e.coord);
	}
);
Tracker.setNotification({
	channel : "cannel1",
	subText : "Text nearby (on left) of titlebar",
	contentTitle : "Title above the text",
	bigText : "The expandable text in more then two lines",
	contentText : "Longer text above",
	lockscreenVisibility : GeoService.VISIBILITY_PRIVATE, // || SECRET || PUBLIC
	largeIcon : "https://avatars0.githubusercontent.com/u/2996237?s=460&v=4"  // optionalfor icon on right side

});

Tracker.setAdapter({  
	uri: "https://mybackend.com/endpoint?my_extra_paramter=1234",
	requestHeaders: ["Accesstoken:DE34B6721"],
	method : "POST", // or PUT
	timeout : 10000, // for http request
	ttl : 60000,   // optional
	userName : "locatrionupdater",
	password : "v3ryS3cr3t",
	successCode  : 200  // this http result state is for db updating
});
Tracker.addEventListener('location',function(e) {
};

Tracker.start();

// later:
Tracker.stop();

var link = Ti.Database.open(GeoService.DATABASE);
link.execSQL("SELECT * FROM " + GeoService.TABLE + " WHERE done=0 ORDER BY time");

```
The parameters for notification you can modify in res folder of module or as opts


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
