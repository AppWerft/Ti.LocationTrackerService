package ti.locationtrackerservice;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;

import android.content.Context;

//import ti.locationtrackerservice.LocationupdatesserviceModule.MyReceiver;

@Kroll.proxy(creatableInModule = LocationtrackerserviceModule.class)
public class AdapterProxy extends KrollProxy {

	Context ctx;
	KrollDict adapter;

	public AdapterProxy() {
		super();
		ctx = TiApplication.getInstance().getApplicationContext();

	}

	@Override
	public void handleCreationDict(KrollDict opts) {
		adapter = opts;
	}

	public KrollDict getAdapter() {
		return adapter;
	}
}
