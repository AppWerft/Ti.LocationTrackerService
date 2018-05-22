/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2011-2017 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

/** This code is generated, do not edit by hand. **/

#include "ti.locationtrackerservice.LocationtrackerserviceModule.h"

#include "AndroidUtil.h"
#include "JNIUtil.h"
#include "JSException.h"
#include "TypeConverter.h"
#include "V8Util.h"



#include "ti.locationtrackerservice.AdapterProxy.h"
#include "ti.locationtrackerservice.TrackerProxy.h"

#include "org.appcelerator.kroll.KrollModule.h"

#define TAG "LocationtrackerserviceModule"

using namespace v8;

namespace ti {
namespace locationtrackerservice {


Persistent<FunctionTemplate> LocationtrackerserviceModule::proxyTemplate;
jclass LocationtrackerserviceModule::javaClass = NULL;

LocationtrackerserviceModule::LocationtrackerserviceModule() : titanium::Proxy()
{
}

void LocationtrackerserviceModule::bindProxy(Local<Object> exports, Local<Context> context)
{
	Isolate* isolate = context->GetIsolate();

	Local<FunctionTemplate> pt = getProxyTemplate(isolate);

	v8::TryCatch tryCatch(isolate);
	Local<Function> constructor;
	MaybeLocal<Function> maybeConstructor = pt->GetFunction(context);
	if (!maybeConstructor.ToLocal(&constructor)) {
		titanium::V8Util::fatalException(isolate, tryCatch);
		return;
	}

	Local<String> nameSymbol = NEW_SYMBOL(isolate, "Locationtrackerservice"); // use symbol over string for efficiency
	MaybeLocal<Object> maybeInstance = constructor->NewInstance(context);
	Local<Object> moduleInstance;
	if (!maybeInstance.ToLocal(&moduleInstance)) {
		titanium::V8Util::fatalException(isolate, tryCatch);
		return;
	}
	exports->Set(nameSymbol, moduleInstance);
}

void LocationtrackerserviceModule::dispose(Isolate* isolate)
{
	LOGD(TAG, "dispose()");
	if (!proxyTemplate.IsEmpty()) {
		proxyTemplate.Reset();
	}

	titanium::KrollModule::dispose(isolate);
}

Local<FunctionTemplate> LocationtrackerserviceModule::getProxyTemplate(Isolate* isolate)
{
	if (!proxyTemplate.IsEmpty()) {
		return proxyTemplate.Get(isolate);
	}

	LOGD(TAG, "LocationtrackerserviceModule::getProxyTemplate()");

	javaClass = titanium::JNIUtil::findClass("ti/locationtrackerservice/LocationtrackerserviceModule");
	EscapableHandleScope scope(isolate);

	// use symbol over string for efficiency
	Local<String> nameSymbol = NEW_SYMBOL(isolate, "Locationtrackerservice");

	Local<FunctionTemplate> t = titanium::Proxy::inheritProxyTemplate(isolate,
		titanium::KrollModule::getProxyTemplate(isolate)
, javaClass, nameSymbol);

	proxyTemplate.Reset(isolate, t);
	t->Set(titanium::Proxy::inheritSymbol.Get(isolate),
		FunctionTemplate::New(isolate, titanium::Proxy::inherit<LocationtrackerserviceModule>));

	// Method bindings --------------------------------------------------------

	Local<ObjectTemplate> prototypeTemplate = t->PrototypeTemplate();
	Local<ObjectTemplate> instanceTemplate = t->InstanceTemplate();

	// Delegate indexed property get and set to the Java proxy.
	instanceTemplate->SetIndexedPropertyHandler(titanium::Proxy::getIndexedProperty,
		titanium::Proxy::setIndexedProperty);

	// Constants --------------------------------------------------------------
	JNIEnv *env = titanium::JNIScope::getEnv();
	if (!env) {
		LOGE(TAG, "Failed to get environment in LocationtrackerserviceModule");
		//return;
	}


			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "LOCATION_PRIORITY_LOW_POWER", 104);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "NOTIFICATION_VISIBILITY_PRIVATE", 0);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "NOTIFICATION_PRIORITY_MAX", 2);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "NOTIFICATION_IMPORTANCE_NONE", 0);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "NOTIFICATION_IMPORTANCE_DEFAULT", 3);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "NOTIFICATION_IMPORTANCE_MAX", 5);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "LOCATION_PRIORITY_BALANCED_POWER_ACCURACY", 102);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "LOCATION_PRIORITY_HIGH_ACCURACY", 100);

			DEFINE_STRING_CONSTANT(isolate, prototypeTemplate, "TABLE", "geologger");

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "NOTIFICATION_PRIORITY_HIGHT", 1);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "NOTIFICATION_PRIORITY_MIN", -2);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "NOTIFICATION_VISIBILITY_PUBLIC", 1);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "NOTIFICATION_PRIORITY_LOW", -1);

			DEFINE_STRING_CONSTANT(isolate, prototypeTemplate, "DATABASE", "geologger");

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "NOTIFICATION_IMPORTANCE_LOW", 2);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "NOTIFICATION_IMPORTANCE_HIGHT", 4);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "NOTIFICATION_VISIBILITY_SECRET", -1);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "NOTIFICATION_PRIORITY_DEFAULT", 0);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "NOTIFICATION_IMPORTANCE_MIN", 1);


	// Dynamic properties -----------------------------------------------------

	// Accessors --------------------------------------------------------------

	return scope.Escape(t);
}

// Methods --------------------------------------------------------------------

// Dynamic property accessors -------------------------------------------------


} // locationtrackerservice
} // ti
