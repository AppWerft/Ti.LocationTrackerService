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

# Usage

```javascript

var GeoService = require("ti.locationupdatesservice");
GeoService.addEventListener("ServiceConnectionChanged",function(e) {
	console.log(e);
});
GeoService.config({
	database : "geolog",  // tablename the same		
	notification: {
		channel : "cannel1",
		title : "Sua posição",
		stopTracking : "Stop tracking", // or null if you don't need
		startTracking : "Omeçar a rastrear", // or null if you don't need
		
	},
	adapter : {  // not yet implemetented ;-(
		uri: "https://"
		requestHeaders: [],
		extraParameters : {
			uid : "1111"
		},
		method : "POST",
		timeout : 100000
	}
})
GeoService.addEventListener("LocationChanged",function(e){
	 Object.keys(e).forEach(function(k){
	 	console.log(k + " :  " + e[k])
	 	/* 
	 		location,time,latitude,longitude,accuracy,bearing,provider,speed 
	 	*/
	 });
});
GeoService.requestLocationUpdates({
	interval : 2 // sec.
	duration : 3600 // optional,
	priority : GeoService.PRIORITY_BALANCED_POWER_ACCURACY // or PRIORITY_LOW_POWER or PRIORITY_HIGH_ACCURACY
});

// later:
GeoService.removeLocationUpdates();

win.addEventListener('close',GeoService.removeLocationUpdates);

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
	<module version="0.1">ti.locationupdatesservice</module>
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
