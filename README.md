# Ti.LocationUpdateService
A bound and started service that is promoted to a foreground service when location updates have been requested and all clients unbind.
 
 For apps running in the background on "O" devices, location is computed only
  once every 10 minutes and delivered batched every 30 minutes. This
  restriction applies even to apps targeting "N" or lower which are run on "O"
  devices.
 
  This module realizes a solution for it. When
  an activity is bound to this service, frequent location updates are
  permitted. When the activity is removed from the foreground, the service
  promotes itself to a foreground service, and location updates continue. When
  the activity comes back to the foreground, the foreground service stops, and
  the notification assocaited with that service is removed.
  
  For controlling  the  foreground service it is mandatory to add a `lifecycleContainer` property. This is for listening of lifecycle events inside the trackerproxy. 

# Manifest

Don't forget this entry in you manifest:

```xml
<manifest>
       <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
       <uses-feature android:name="android.hardware.location.gps"/>
       <application>
                <service android:name="ti.locationtrackerservice.LocationUpdatesService"/>
       </application>
</manifest>
```


# Usage

```javascript

var GeoService = require("ti.locationtrackerservice");
GeoService.addEventListener("ServiceConnectionChanged",function(e) {
	console.log(e);
});
var opts = {
	lifecycleContainer : win, 
	interval :10, //sec.
	priority : GeoService.PRIORITY_BALANCED_POWER_ACCURACY,
};
var Tracker = GeoService.createTracker(opts,function(){ // callback is optional
		console.log(e.coord);
	}
);
Tracker.setNotification({
	channel : "cannel1",
	subText : "Text nearby (on left) of titlebar",
	contentTitle : "Title above the text",
	bigText : "The expandable text in more then two lines",
	contentText : "Longer text above",
	largeIcon : "https://avatars0.githubusercontent.com/u/2996237?s=460&v=4"  // optionalfor icon on right side

});
var Adapter = GeoService.createAdapter({  
	uri: "https://mybackend.com/endpoint?my_extra_paramter=1234",
	requestHeaders: ["Accesstoken:DE34B6721"],
	method : "POST", // or PUT
	timeout : 100000,
	lastEntries : 1  // 10: last 10 locations, 0: for all
});
Tracker.setAdapter(Adapter);

Tracker.start();

// later:
Tracker.stop();

var link = Ti.Database.open(GeoService.DATABASE);
linl.execSQL("SELECT * FROM " + GeoSewrvice.TABLE + " WHERE done=0 ORDER BY time");

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
