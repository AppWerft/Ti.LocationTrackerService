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
	private KrollDict adapterOpts;

	public ServerAdapter(Context ctx, KrollDict adapterOpts) {
		this.ctx = ctx;
		this.adapterOpts = adapterOpts;
	}

	private void saveToSQL(Location location) {

	}

	public void Sync() {
		JSONArray resultList = new JSONArray();
		SQLiteDatabase db = ctx.openOrCreateDatabase(this.database,
				MODE_PRIVATE, null);
		Cursor resultSet = db.rawQuery("Select * from " + this.database
				+ " Where done=0", null);
		while (!resultSet.isLast()) {
			JSONObject res = new JSONObject();
			try {
				res.put("latitude", resultSet.getFloat(0));
				res.put("longitude", resultSet.getFloat(1));
				res.put("time", resultSet.getInt(2));
				res.put("speed", resultSet.getInt(3));
				res.put("accuracy", resultSet.getInt(4));
				resultList.put(res);
				resultSet.moveToNext();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		resultSet.close();
		db.close();

		JSONObject payload = new JSONObject();
		try {
			payload.put("list", resultList);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			String status = send("post", adapterOpts.getString("endpoint"),
					payload.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	String send(String method, String url, String json) throws IOException {
		final MediaType JSON = MediaType
				.parse("application/json; charset=utf-8");
		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(JSON, json);
		Request request = null;
		switch (method) {
		case "post":
			request = new Request.Builder().url(url).post(body).build();
			break;
		case "put":
			request = new Request.Builder().url(url).put(body).build();
			break;

		}
		try (Response response = client.newCall(request).execute()) {
			return response.body().string();
		}
	}

}
