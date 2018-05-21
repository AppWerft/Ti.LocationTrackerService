package ti.locationtrackerservice;

import org.apache.commons.lang.StringUtils;
import org.appcelerator.kroll.KrollDict;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServerAdapter {
	private static int MODE_PRIVATE = 0;
	private Context ctx;
	final private String DATABASE = "geologger";
	final private String TABLE = DATABASE;
	final private String LCAT = LocationtrackerserviceModule.LCAT;
	private KrollDict opts = null;

	public ServerAdapter(Context ctx, KrollDict adapterOpts) {
		this.ctx = ctx;
		this.opts = adapterOpts;

	}

	public void setOpts(KrollDict opts) {
		this.opts = opts;
	}

	public void Sync() {
		JSONArray resultList = new JSONArray();
		JSONObject payload = new JSONObject();
		List<Double> timestamps = new ArrayList<>();
		SQLiteDatabase db = ctx.openOrCreateDatabase(DATABASE, MODE_PRIVATE,
				null);

		if (this.opts.containsKeyAndNotNull("ttl")) {
			int oldest = (int) (System.currentTimeMillis() - this.opts
					.getInt("ttl"));
			db.rawQuery("DELETE * FROM " + TABLE + " WHERE time < " + oldest,
					null);
		}
		Cursor c = db.rawQuery("SELECT * FROM " + TABLE
				+ " WHERE Done=0 ORDER BY Time DESC", null);
		try {
			// https://stackoverflow.com/questions/32284135/sqlite-identify-long-value
			while (c.moveToNext()) {
				timestamps.add(c.getDouble(c.getColumnIndex("time")));
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
		try {
			payload.put("list", resultList);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (this.opts != null) {
			try {
				send(opts.getString("method"), opts.getString("uri"),
						payload.toString(), timestamps);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void updateDB(List<Double> timestamps) {
		SQLiteDatabase db = ctx.openOrCreateDatabase(DATABASE, MODE_PRIVATE,
				null);
		String sql = "UPDATE " + TABLE + " SET done=1 WHERE time IN ("
				+ StringUtils.join(timestamps, ",") + ")";
		db.rawQuery(sql, null);
		Log.d(LCAT, sql);
		db.close();
	}

	private void send(String method, String uri, String json,
			final List<Double> timestamps) throws IOException {
		Callback loginCallback = new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				try {
					Log.i(LCAT, "login failed: " + call.execute().code());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void onResponse(Call call, Response response)
					throws IOException {
				boolean success = false;
				int code = response.code();
				if (opts.containsKeyAndNotNull("successCode")) {
					if (opts.getInt("successCode") == code) {
						success = true;
					}
				} else {
					if (code >= 200 && code < 300) {
						success = true;
					}
				}
				if (success)
					updateDB(timestamps);

			}
		};
		final MediaType JSON = MediaType
				.parse("application/json; charset=utf-8");
		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(JSON, json);
		Request.Builder builder = new Request.Builder().url(uri);
		switch (method) {
		case "post":
			builder = builder.post(body);
			break;
		case "put":
			builder = builder.put(body);
			break;
		}

		if (opts.containsKeyAndNotNull("userName")
				&& opts.containsKeyAndNotNull("password")) {
			// builder = builder.addInterceptor(new BasicAuthInterceptor(opts
			// .getString("userName"), opts.getString("password")));
		}
		client.newCall(builder.build()).enqueue(loginCallback);

	}
}
