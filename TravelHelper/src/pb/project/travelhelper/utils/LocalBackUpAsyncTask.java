package pb.project.travelhelper.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pb.project.travelhelper.R;
import pb.project.travelhelper.database.DBAdapter;
import pb.project.travelhelper.database.DBContentProvider;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

public class LocalBackUpAsyncTask extends AsyncTask<String, Void, Integer>{

	Activity mActivity;
	ContentResolver mContentRes;
	
	@Override
	protected Integer doInBackground(String... params) {
		String j_id = params[0];
		mContentRes = mActivity.getContentResolver();
		
		Uri u = DBContentProvider.JOURNEYES_URI;
		Cursor c = mContentRes.query(Uri.withAppendedPath(u, j_id), null, null, null, null);
		c.moveToFirst();

		String jourName = c.getString(c.getColumnIndex(DBAdapter.J_KEY_NAME)),
				jourDesc = c.getString(c.getColumnIndex(DBAdapter.J_KEY_DESCRIPTION)),
				jourgdid = c.getString(c.getColumnIndex(DBAdapter.J_KEY_DRIVEID));
		long jourSDate = c.getLong(c.getColumnIndex(DBAdapter.J_KEY_START_DATE)),
			jourEDate = c.getLong(c.getColumnIndex(DBAdapter.J_KEY_END_DATE));
		
		
		if(!isExternalStorageWritable())
			return null;
		
		File sdFolder = Environment.getExternalStorageDirectory();
		File thFolder = new File(sdFolder.getAbsolutePath(), "TravelHelper");
		thFolder.mkdir();
		if (thFolder.isDirectory()){
			File file = new File(thFolder, jourName+".json");
			try {
				FileOutputStream outStream = new FileOutputStream(file);
				//naplnìní daty
		    	OutputStreamWriter osw = new OutputStreamWriter(outStream);

	    		
	    		Cursor a_cur = mContentRes.query(DBContentProvider.ACTIVITIES_URI, null, 
	    				DBAdapter.A_KEY_JOURNEY_ID+"="+j_id, null, "yes");
	    		a_cur.moveToFirst();
	    		JSONObject jsonObj = new JSONObject();
				
				jsonObj.accumulate("jour_name", jourName);
				jsonObj.accumulate("jour_sdate", jourSDate);
				jsonObj.accumulate("jour_edate", jourEDate);
				jsonObj.accumulate("jour_desc", jourDesc);
				jsonObj.accumulate("jour_gdid", jourgdid);
				JSONArray jsonArray = new JSONArray();
				//while(a_cur.moveToNext()) {
				for (int i = 0; i< a_cur.getCount(); i++) {
					JSONObject jsonRow = new JSONObject();
					jsonRow.accumulate("a_name", a_cur.getString(a_cur.getColumnIndex(DBAdapter.A_KEY_NAME)));
					jsonRow.accumulate("a_stime", a_cur.getString(a_cur.getColumnIndex(DBAdapter.A_KEY_START_TIME)));
					jsonRow.accumulate("a_etime", a_cur.getString(a_cur.getColumnIndex(DBAdapter.A_KEY_END_TIME)));
					jsonRow.accumulate("a_price", a_cur.getDouble(a_cur.getColumnIndex(DBAdapter.A_KEY_PRICE)));
					jsonRow.accumulate("a_curr", a_cur.getString(a_cur.getColumnIndex(DBAdapter.A_KEY_CURRENCY)));
					jsonRow.accumulate("a_categ", a_cur.getString(a_cur.getColumnIndex(DBAdapter.A_KEY_CATEGORY_ID)));
					jsonRow.accumulate("a_plid", a_cur.getLong(a_cur.getColumnIndex(DBAdapter.A_KEY_PLACES_ID)));
					jsonRow.accumulate("pl_name", a_cur.getString(a_cur.getColumnIndex(DBAdapter.M_KEY_NAME)));
					jsonRow.accumulate("pl_addr", a_cur.getString(a_cur.getColumnIndex(DBAdapter.M_KEY_ADDRESS)));
					jsonRow.accumulate("pl_lat", a_cur.getString(a_cur.getColumnIndex(DBAdapter.M_KEY_LATITUDE)));
					jsonRow.accumulate("pl_lon", a_cur.getString(a_cur.getColumnIndex(DBAdapter.M_KEY_LONGITUDE)));
					jsonRow.put("pl_web", a_cur.getString(a_cur.getColumnIndex(DBAdapter.M_KEY_WEB)));
					jsonRow.accumulate("pl_ratg", a_cur.getString(a_cur.getColumnIndex(DBAdapter.M_KEY_RATING)));
					jsonRow.accumulate("pl_fav", a_cur.getString(a_cur.getColumnIndex(DBAdapter.M_KEY_FAVORITE)));
					jsonRow.accumulate("pl_categ", a_cur.getString(a_cur.getColumnIndex(DBAdapter.M_KEY_CATEGORY_ID)));
					
					
					jsonArray.put(jsonRow);
					
					a_cur.moveToNext();
				}
				jsonObj.accumulate("activities", jsonArray);
			

				osw.write(jsonObj.toString());
				osw.flush();
				
				osw.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return 0;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		
		if (result == null){
			Toast.makeText(mActivity, R.string.jour_backup_fail, Toast.LENGTH_SHORT).show();
		}else if (result == 0){
			Toast.makeText(mActivity, R.string.jour_backup_ok, Toast.LENGTH_SHORT).show();
		}
	}
	
	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	public void setActivity(Activity a){
		mActivity = a;
	}

}
