var win = Ti.UI.createWindow();

var LUS = require("ti.locationupdatesservice");
win.addEventListener("open", function() {
	LUS.requestLocationUpdates({
		interval : 10,
		priority : LUS.PRIORITY_BALANCED_POWER_ACCURACY,
		onlocation : function(e) {
			e.coords && Object.keys(e.coords).forEach(function(k) {
				console.log(k + "=" + e.coords[k]);
			});
		}
	});
});
win.addEventListener("close", function() {
	console.log("TiGeoLogger: close");
	LUS.removeLocationUpdates();
});

LUS.config({
	database : "geolog", // tablename the same
	notification : {
		channel : "cannel1",
		title : "Your position",
		subText : "Text neben Kopflinie",
		contentText : "Sie sind hier {LOCATION}",
		contentTitle : "Es ist {TIME}",
		stopTracking : "Stop tracking", // or null if you don't need
		startTracking : "Start tracking", // or null if you don't need

	}
});
LUS.addEventListener("ServiceConnectionChanged", function(e) {
	console.log(e.connected);
});
LUS.addEventListener("LocationChanged", function(e) {
	console.log(e.latitude + ',' + e.longitude + ' @' + e.time);
});

win.open();
