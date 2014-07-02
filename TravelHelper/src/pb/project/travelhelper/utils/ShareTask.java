package pb.project.travelhelper.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pb.project.travelhelper.database.DBAdapter;
import pb.project.travelhelper.database.DBContentProvider;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class ShareTask extends AsyncTask<String, Void, String>{

	public static final int SHARE_ITEM_GP = 1;
	public static final int SHARE_ITEM_FA = 2;
	
	private static final String TAG = "ShareTask";
	int shareOption = 0;
	String mJourName = null;
	
	ShareTaskListener mListener;
	Context mContext;
	
	@Override
	protected String doInBackground(String... params) {
		long mJourId = Long.valueOf(params[0]);
		shareOption = Integer.valueOf(params[1]);
		
		String result = null;
		
		HttpClient aClient = new DefaultHttpClient();
		
		HttpPost aPost = new HttpPost("http://eva.fit.vutbr.cz/~xblatn03/DP/saveToDB.php");
		//aPost.addHeader(new BasicHeader("Content-Type", "aplication/json"));
		
		Uri u = DBContentProvider.JOURNEYES_URI;
		Cursor cur = mContext.getContentResolver().query(Uri.withAppendedPath(u, String.valueOf(mJourId)), null, null, null, null);
		cur.moveToFirst();
		mJourName = cur.getString(cur.getColumnIndex(DBAdapter.J_KEY_NAME));
		long sdate = cur.getLong(cur.getColumnIndex(DBAdapter.J_KEY_START_DATE)),
				edate = cur.getLong(cur.getColumnIndex(DBAdapter.J_KEY_END_DATE));
		
		cur = mContext.getContentResolver().query(DBContentProvider.ACTIVITIES_URI, null, DBAdapter.A_KEY_JOURNEY_ID+"="+mJourId, null, "yes");
		cur.moveToFirst();
		
		//naplnìní dat
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.accumulate("jour_name", mJourName);
			jsonObj.accumulate("j_count", cur.getCount());
			jsonObj.accumulate("jour_sdate", sdate);
			jsonObj.accumulate("jour_edate", edate);
			JSONArray jsonArray = new JSONArray();
			//while(cur.moveToNext()) {
			for (int i = 0; i< cur.getCount(); i++) {
				JSONObject jsonRow = new JSONObject();
				jsonRow.accumulate("a_name", cur.getString(cur.getColumnIndex(DBAdapter.A_KEY_NAME)));
				jsonRow.accumulate("a_stime", cur.getString(cur.getColumnIndex(DBAdapter.A_KEY_START_TIME)));
				jsonRow.accumulate("a_etime", cur.getString(cur.getColumnIndex(DBAdapter.A_KEY_END_TIME)));
				jsonRow.accumulate("pl_name", cur.getString(cur.getColumnIndex(DBAdapter.M_KEY_NAME)));
				jsonRow.accumulate("address", cur.getString(cur.getColumnIndex(DBAdapter.M_KEY_ADDRESS)));
				jsonRow.accumulate("a_lat", cur.getString(cur.getColumnIndex(DBAdapter.M_KEY_LATITUDE)));
				jsonRow.accumulate("a_lon", cur.getString(cur.getColumnIndex(DBAdapter.M_KEY_LONGITUDE)));
				jsonRow.accumulate("category", cur.getString(cur.getColumnIndex(DBAdapter.A_KEY_CATEGORY_ID)));
				
				jsonArray.put(jsonRow);
				
				cur.moveToNext();
			} 
			jsonObj.accumulate("activities", jsonArray);
			
			//Log.i(TAG, jsonObj.toString());
			
			//pøiøazení dat do POST
			StringEntity se = new StringEntity(jsonObj.toString(), "UTF-8");		
			aPost.setEntity(se);
			aPost.setHeader("Accept", "application/json");
			aPost.setHeader("Content-type", "application/json");
		
			HttpResponse aResp = aClient.execute(aPost);
			
			if(aResp != null){
				InputStream is = aResp.getEntity().getContent();
				
				String r = convertStreamToString(is);
				r.replaceAll("[^\\d]", "");
				//int id = Integer.parseInt(r);
				
				result = "http://eva.fit.vutbr.cz/~xblatn03/DP/dp.html?jid="+r;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		return result;
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		//Log.i(TAG, "url:"+result);

		mListener.onShareTaskDone(result, shareOption, mJourName);		
	}
	
	public void setContext(Context c){
		mContext = c;
	}
	
	private String convertStreamToString(InputStream is) {
		BufferedReader streamReader;
		try {
			streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		    StringBuilder responseStrBuilder = new StringBuilder();
	
		    int cc;
			while ((cc = streamReader.read()) != -1)
				if (cc >= '0' && cc <= '9')
					responseStrBuilder.append((char)cc);
			
			Log.i(TAG, responseStrBuilder.toString());
			return responseStrBuilder.toString();
		}
		catch (IOException e) {
			e.printStackTrace();
	    }  
		return null;
	}
	
	public interface ShareTaskListener{
		public void onShareTaskDone(String result, int shareOption, String JourName);
	}
	
	public void setShareTaskListener(ShareTaskListener l){
		mListener = l;
	}
}