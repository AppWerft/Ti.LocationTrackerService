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
	titanium::SetProtoMethod(isolate, t, "checkPermissions", LocationtrackerserviceModule::checkPermissions);

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


			DEFINE_STRING_CONSTANT(isolate, prototypeTemplate, "TABLE", "geologger");

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "PRIORITY_HIGH_ACCURACY", 100);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "PRIORITY_LOW_POWER", 104);

			DEFINE_INT_CONSTANT(isolate, prototypeTemplate, "PRIORITY_BALANCED_POWER_ACCURACY", 102);

			DEFINE_STRING_CONSTANT(isolate, prototypeTemplate, "DATABASE", "geologger");


	// Dynamic properties -----------------------------------------------------

	// Accessors --------------------------------------------------------------

	return scope.Escape(t);
}

// Methods --------------------------------------------------------------------
void LocationtrackerserviceModule::checkPermissions(const FunctionCallbackInfo<Value>& args)
{
	LOGD(TAG, "checkPermissions()");
	Isolate* isolate = args.GetIsolate();
	HandleScope scope(isolate);

	JNIEnv *env = titanium::JNIScope::getEnv();
	if (!env) {
		titanium::JSException::GetJNIEnvironmentError(isolate);
		return;
	}
	static jmethodID methodID = NULL;
	if (!methodID) {
		methodID = env->GetMethodID(LocationtrackerserviceModule::javaClass, "checkPermissions", "()Z");
		if (!methodID) {
			const char *error = "Couldn't find proxy method 'checkPermissions' with signature '()Z'";
			LOGE(TAG, error);
				titanium::JSException::Error(isolate, error);
				return;
		}
	}

	Local<Object> holder = args.Holder();
	// If holder isn't the JavaObject wrapper we expect, look up the prototype chain
	if (!JavaObject::isJavaObject(holder)) {
		holder = holder->FindInstanceInPrototypeChain(getProxyTemplate(isolate));
	}

	titanium::Proxy* proxy = NativeObject::Unwrap<titanium::Proxy>(holder);

	jvalue* jArguments = 0;

	jobject javaProxy = proxy->getJavaObject();
	if (javaProxy == NULL) {
		args.GetReturnValue().Set(v8::Undefined(isolate));
		return;
	}
	jboolean jResult = (jboolean)env->CallBooleanMethodA(javaProxy, methodID, jArguments);



	proxy->unreferenceJavaObject(javaProxy);



	if (env->ExceptionCheck()) {
		Local<Value> jsException = titanium::JSException::fromJavaException(isolate);
		env->ExceptionClear();
		return;
	}


	Local<Boolean> v8Result = titanium::TypeConverter::javaBooleanToJsBoolean(isolate, env, jResult);



	args.GetReturnValue().Set(v8Result);

}

// Dynamic property accessors -------------------------------------------------


} // locationtrackerservice
} // ti
