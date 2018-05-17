/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 * Warning: This file is GENERATED, and should not be modified
 */
var bootstrap = kroll.NativeModule.require("bootstrap"),
	invoker = kroll.NativeModule.require("invoker"),
	Titanium = kroll.binding("Titanium").Titanium;

function moduleBootstrap(moduleBinding) {
	function lazyGet(object, binding, name, namespace) {
		return bootstrap.lazyGet(object, binding,
			name, namespace, moduleBinding.getBinding);
	}

	var module = moduleBinding.getBinding("ti.locationtrackerservice.LocationtrackerserviceModule")["Locationtrackerservice"];
	var invocationAPIs = module.invocationAPIs = [];
	module.apiName = "Locationtrackerservice";

	function addInvocationAPI(module, moduleNamespace, namespace, api) {
		invocationAPIs.push({ namespace: namespace, api: api });
	}

	addInvocationAPI(module, "Locationtrackerservice", "Locationtrackerservice", "createAdapter");addInvocationAPI(module, "Locationtrackerservice", "Locationtrackerservice", "createTracker");
		if (!("__propertiesDefined__" in module)) {Object.defineProperties(module, {
"Adapter": {
get: function() {
var Adapter =  lazyGet(this, "ti.locationtrackerservice.AdapterProxy", "Adapter", "Adapter");
return Adapter;
},
configurable: true
},
"Tracker": {
get: function() {
var Tracker =  lazyGet(this, "ti.locationtrackerservice.TrackerProxy", "Tracker", "Tracker");
return Tracker;
},
configurable: true
},

});
module.constructor.prototype.createAdapter = function() {
return new module["Adapter"](arguments);
}
module.constructor.prototype.createTracker = function() {
return new module["Tracker"](arguments);
}
}
module.__propertiesDefined__ = true;
return module;

}
exports.bootstrap = moduleBootstrap;
