package ti.locationupdatesservice;

import java.util.Date;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

public class SaveAndSend {
	private static int MODE_PRIVATE = 0;

	public SaveAndSend(Context ctx, Location location) {
		saveToSQL(ctx, location);
		sendToServer();
	}

	static public void saveToSQL(Context ctx, Location location) {
		SQLiteDatabase db = ctx.openOrCreateDatabase(
				LocationupdatesserviceModule.dbName, MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS geologger(Latitude Real,Longitude Real, Ctime Integer, Done Integer);");
		db.execSQL("INSERT INTO geologgerVALUES(" + location.getLatitude()
				+ "," + location.getLongitude() + "," + location.getTime()
				+ ",0);");
		db.close();
	}

	static private void sendToServer() {

	}

}
