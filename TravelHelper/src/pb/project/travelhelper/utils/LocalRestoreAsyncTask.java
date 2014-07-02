package pb.project.travelhelper.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pb.project.travelhelper.R;
import pb.project.travelhelper.database.DBAdapter;
import pb.project.travelhelper.database.DBContentProvider;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

public class LocalRestoreAsyncTask extends AsyncTask<String, Void, Integer> {

	Context mContext;
	
	@Override
	protected Integer doInBackground(String... params) {		
		try {
			InputStream inStream = null;
			/* otevøení souboru */
			if (params[0]!= null){
				Uri fileuri = Uri.parse(params[0]);
				File file = new File(fileuri.getPath());
				if(!file.canRead())
					return null;
				inStream = new FileInputStream(file);
			}else {
				inStream = mContext.getAssets().open(DBAdapter.DATABASE_INIT_ASSET);
			}
			/* ètení dat ze souboru */
			  
			InputStreamReader isr = new InputStreamReader(inStream);
			BufferedReader br = new BufferedReader(isr);
			
			String row = null;
			StringBuffer dataBuffer = new StringBuffer();
			while((row = br.readLine())!= null)
				dataBuffer.append(row);
			
			inStream.close();
			
			JSONObject jsObject = new JSONObject(dataBuffer.toString());
			String jname = jsObject.getString("jour_name"),
					jdesc = jsObject.getString("jour_desc"),
					jgdid = jsObject.getString("jour_gdid");
			long jsdate = jsObject.getLong("jour_sdate"),
					jedate = jsObject.getLong("jour_edate");
			
			/* Ovìøení na existenci stejné cesty v databázi */
			String selection = DBAdapter.J_KEY_NAME+" = ? AND "+DBAdapter.J_KEY_DESCRIPTION+" =? AND "
								+DBAdapter.J_KEY_START_DATE+"=? AND "+DBAdapter.J_KEY_END_DATE+"=?";
			String[] selectionArgs = {jname, jdesc, String.valueOf(jsdate), String.valueOf(jedate)};
			Cursor jcur = mContext.getContentResolver().query(DBContentProvider.JOURNEYES_URI, null, selection, selectionArgs, null);
			//Log.i(TAG, "cur count:"+jcur.getCount());
			if (jcur.getCount()!=0)
				return 1;
			
			/* Vložení cesty do db */
			ContentValues values = new ContentValues();
			values.put(DBAdapter.J_KEY_NAME, jname);
			values.put(DBAdapter.J_KEY_DESCRIPTION, jdesc);
			values.put(DBAdapter.J_KEY_START_DATE, jsdate);
			values.put(DBAdapter.J_KEY_END_DATE, jedate);
			values.put(DBAdapter.J_KEY_DRIVEID, jgdid);
			Uri juri = mContext.getContentResolver().insert(DBContentProvider.JOURNEYES_URI, values);
			String jid = juri.getLastPathSegment();
			
			JSONArray jsActivities = jsObject.getJSONArray("activities");
			for(int i = 0; i<jsActivities.length(); i++){
				JSONObject jsActi = jsActivities.getJSONObject(i);
				String plid = jsActi.getString("a_plid"),
						plname = jsActi.getString("pl_name"),
						pllat = jsActi.getString("pl_lat"),
						pllon = jsActi.getString("pl_lon");
				
				/* oveøení místa aktivity */
				String sel = DBAdapter.M_KEY_ID + "=? AND "+
							DBAdapter.M_KEY_NAME + "=? AND "+
							DBAdapter.M_KEY_LATITUDE + "=? AND "+
							DBAdapter.M_KEY_LONGITUDE + "=?";
				String[] selArgs = {plid, plname, pllat, pllon};
				Cursor m_cur = mContext.getContentResolver().query(DBContentProvider.PLACES_URI, null, sel, selArgs, null);
				if (m_cur.getCount() == 0){ /* vytvoøení nového pokud neexistuje */
					values = new ContentValues();
					values.put(DBAdapter.M_KEY_NAME, plname);
					values.put(DBAdapter.M_KEY_LATITUDE, pllat);
					values.put(DBAdapter.M_KEY_LONGITUDE, pllon);
					values.put(DBAdapter.M_KEY_ADDRESS, jsActi.getString("pl_addr"));
					values.put(DBAdapter.M_KEY_CATEGORY_ID, jsActi.getString("pl_categ"));
					values.put(DBAdapter.M_KEY_FAVORITE, jsActi.getString("pl_fav"));
					values.put(DBAdapter.M_KEY_RATING, jsActi.getString("pl_ratg"));
					values.put(DBAdapter.M_KEY_WEB, jsActi.getString("pl_web"));
					
					Uri muri = mContext.getContentResolver().insert(DBContentProvider.PLACES_URI, values);
					plid = muri.getLastPathSegment();
				}
				
				//hodnoty pro aktivity
				values = new ContentValues();
				values.put(DBAdapter.A_KEY_NAME, jsActi.getString("a_name"));
				values.put(DBAdapter.A_KEY_START_TIME, jsActi.getString("a_stime"));
				values.put(DBAdapter.A_KEY_END_TIME, jsActi.getString("a_etime"));
				values.put(DBAdapter.A_KEY_PRICE, jsActi.getString("a_price"));
				values.put(DBAdapter.A_KEY_CURRENCY, jsActi.getString("a_curr"));
				values.put(DBAdapter.A_KEY_CATEGORY_ID, jsActi.getString("a_categ"));
				values.put(DBAdapter.A_KEY_PLACES_ID, plid);
				values.put(DBAdapter.A_KEY_JOURNEY_ID, jid);
				
				mContext.getContentResolver().insert(DBContentProvider.ACTIVITIES_URI, values);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return 0;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		
		if (result == null)
			Toast.makeText(mContext, R.string.jour_restore_fail, Toast.LENGTH_SHORT).show();
		else if (result == 1)
			Toast.makeText(mContext, R.string.jour_restore_exist, Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(mContext, R.string.jour_restore_ok, Toast.LENGTH_SHORT).show();
	}
	
	public void setActivity(Context a){
		mContext = a;
	}

}
