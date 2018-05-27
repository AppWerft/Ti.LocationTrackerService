var win = Ti.UI.createWindow();

var GeoService = require("ti.locationtrackerservice");



var Tracker = GeoService.createTracker({
	lifecycleContainer : win, 
	interval : 10, //millisec.
	priority : GeoService.PRIORITY_BALANCED_POWER_ACCURACY,
});

Tracker.setNotification({
		channel : "cannel1",
		subText : "Text nearby (on left) of titlebar",
		contentTitle : "Title above the text",
		bigText : "The expandable text in more then two lines",
		contentText : "Longer text below {LOCATION}",
		largeIcon : "https://avatars0.githubusercontent.com/u/2996237?s=460&v=4"  // optional for icon on right side
});
Tracker.setAdapter({  
		uri: "https://jsonplaceholder.typicode.com/posts/",
		requestHeaders: ["Accesstoken:DE34B6721"],
		method : "POST", // or PUT
		timeout : 5,
		successCode : 201,
		ttl : 6
});

win.addEventListener("open", function() {
	Tracker.start();
	Tracker.addEventListener('location',function(e){
	});
});

win.open();
