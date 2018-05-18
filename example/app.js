var win = Ti.UI.createWindow();

var GeoService = require("ti.locationtrackerservice");


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
		largeIcon : "https://avatars0.githubusercontent.com/u/2996237?s=460&v=4"  // optional for icon on right side

});
var Adapter = GeoService.createAdapter({  // not yet implemetented ;-(
		uri: "https://mybackend.com/endpoint?my_extra_paramter=1234",
		requestHeaders: ["Accesstoken:DE34B6721"],
		method : "POST", // or PUT
		timeout : 100000
});
Tracker.addAdapter(Adapter);
win.addEventListener("open", function() {
	Tracker.requestLocationUpdates({
		interval : 10,
		priority : GeoService.PRIORITY_BALANCED_POWER_ACCURACY,
		onlocation : function(e) {
			console.log(e.latitude + ',' + e.longitude + ' @' + e.time);
		}
	});
});

win.open();
