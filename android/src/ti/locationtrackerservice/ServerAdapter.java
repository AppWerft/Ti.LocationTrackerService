package ti.locationtrackerservice;

import org.appcelerator.kroll.KrollDict;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServerAdapter {
	private static int MODE_PRIVATE = 0;
	private Context ctx;
	private String database = "geologger";
	private KrollDict opts;

	public ServerAdapter(Context ctx, KrollDict adapterOpts) {
		this.ctx = ctx;
		this.opts = adapterOpts;
	}

	public void Sync() {
		JSONArray resultList = new JSONArray();
		SQLiteDatabase db = ctx.openOrCreateDatabase(this.database,
				MODE_PRIVATE, null);
		Cursor c = db.rawQuery("SELECT * FROM " + this.database
				+ " WHERE Done=0 ORDER BY Time DESC", null);
		try {
			// https://stackoverflow.com/questions/32284135/sqlite-identify-long-value
			while (c.moveToNext()) {
				JSONObject res = new JSONObject();
				res.put("latitude", c.getFloat(c.getColumnIndex("Latitude")));
				res.put("longitude", c.getFloat(c.getColumnIndex("Longitude")));
				res.put("time", c.getDouble(c.getColumnIndex("time")));
				res.put("speed", c.getDouble(c.getColumnIndex("Speed")));
				res.put("accuracy", c.getDouble(c.getColumnIndex("Accuracy")));
				resultList.put(res);

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		c.close();
		db.close();

		JSONObject payload = new JSONObject();
		try {
			payload.put("list", resultList);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			String status = send(opts.getString("method"),
					opts.getString("uri"), payload.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String send(String method, String uri, String json)
			throws IOException {
		final MediaType JSON = MediaType
				.parse("application/json; charset=utf-8");
		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(JSON, json);
		Request request = null;
		switch (method) {
		case "post":
			request = new Request.Builder().url(uri).post(body).build();
			break;
		case "put":
			request = new Request.Builder().url(uri).put(body).build();
			break;

		}
		try (Response response = client.newCall(request).execute()) {
			return response.body().string();
		}
	}

}
