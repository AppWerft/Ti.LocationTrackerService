package ti.locationtrackerservice;

import java.util.Date;

import org.json.JSONArray;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

public class SaveAndSend {
	private static int MODE_PRIVATE = 0;
	private Context ctx;
	private String database;

	public SaveAndSend(Context ctx, Location location, String database) {
		this.ctx = ctx;
		this.database = database;
		saveToSQL(location);
		sendToServer();
	}

	private void saveToSQL(Location location) {
		SQLiteDatabase db = ctx.openOrCreateDatabase(this.database,
				MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ this.database
				+ "(Latitude Real,Longitude Real, Ctime Integer, Done Integer, Speed Real, time Integer, Accuracy Real);");
		Object[] values = new Object[] { location.getLatitude(),
				location.getLongitude(), location.getTime(), 0,
				location.getSpeed(), location.getTime(), location.getAccuracy() };
		db.execSQL("INSERT INTO " + this.database + " VALUES(?,?,?,?,?,?,?)",
				values);

		db.close();
	}

	private void sendToServer() {
		JSONArray resultList = new JSONArray();
		SQLiteDatabase db = ctx.openOrCreateDatabase(this.database,
				MODE_PRIVATE, null);
		Cursor resultSet = db.rawQuery("Select * from " + this.database
				+ " Where done=0", null);

		db.close();
	}

}
