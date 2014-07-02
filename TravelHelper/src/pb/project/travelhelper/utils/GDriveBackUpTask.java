package pb.project.travelhelper.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pb.project.travelhelper.R;
import pb.project.travelhelper.database.DBAdapter;
import pb.project.travelhelper.database.DBContentProvider;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class GDriveBackUpTask extends AsyncTask<String, Integer, String>{

	private static final String TAG = "gDriveAsyncTask";
	GoogleApiClient mGoogleApiClient;
	SharedPreferences mSett;
	ContentResolver mContentRes ;
	Activity mAct;
	
	@Override
	protected String doInBackground(String... params) {		
		mContentRes = mAct.getContentResolver();
		
		boolean isFolderExist = false;
		String foldName = mSett.getString("gDriveFolderId", null);
		DriveId folderTH = null;	
		
		DriveId fileID = null;
		//long j_id = Long.parseLong(params[0]);
		String j_id = params[0];
		Uri u = DBContentProvider.JOURNEYES_URI;
		
		while (!mGoogleApiClient.isConnected());
		Log.i(TAG, "start");
		
		//oveøení jestli už nebyla cesta sdílena
		Cursor c = mContentRes.query(Uri.withAppendedPath(u, j_id), null, null, null, null);
		c.moveToFirst();
		String id_string = c.getString(c.getColumnIndex(DBAdapter.J_KEY_DRIVEID));
		if (!id_string.isEmpty()){ 
			fileID = DriveId.decodeFromString(id_string);
			return id_string;
		}
		String jourName = c.getString(c.getColumnIndex(DBAdapter.J_KEY_NAME)),
				jourDesc = c.getString(c.getColumnIndex(DBAdapter.J_KEY_DESCRIPTION));
		long jourSDate = c.getLong(c.getColumnIndex(DBAdapter.J_KEY_START_DATE)),
			jourEDate = c.getLong(c.getColumnIndex(DBAdapter.J_KEY_END_DATE));
		
		
		//ovìøení složky
		if (foldName != null){
			folderTH = DriveId.decodeFromString(foldName);
			isFolderExist = true;
		}
		
		//vytvoøení složky pokud neexistuje
		if (!isFolderExist) {
			Log.i(TAG, "vytvaøim novou složku");
			MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
        			.setTitle("TravelHelper").build();
			DriveFolderResult dfResult = Drive.DriveApi.getRootFolder(mGoogleApiClient)
					.createFolder(mGoogleApiClient, changeSet).await();
			if (!dfResult.getStatus().isSuccess()) {
                Log.w(TAG, "Error while trying to create the folder");
                return null;
            }
			folderTH = dfResult.getDriveFolder().getDriveId();
			SharedPreferences.Editor ed = mSett.edit();
			ed.putString("gDriveFolderId", folderTH.encodeToString());
			ed.commit();
		}
		
		//vytvoøení souboru 
		ContentsResult ctResult = Drive.DriveApi.newContents(mGoogleApiClient).await();
		if (!ctResult.getStatus().isSuccess()){
			Log.w(TAG, "Failed to create new contents.");
            return null;
		}
		
		//naplnìní daty
		OutputStream outStream = ctResult.getContents().getOutputStream();
    	OutputStreamWriter osw = new OutputStreamWriter(outStream);
    	try {
    		
    		Cursor a_cur = mContentRes.query(DBContentProvider.ACTIVITIES_URI, null, 
    				DBAdapter.A_KEY_JOURNEY_ID+"="+j_id, null, "yes");
    		a_cur.moveToFirst();
    		JSONObject jsonObj = new JSONObject();
			
			jsonObj.accumulate("jour_name", jourName);
			jsonObj.accumulate("jour_sdate", jourSDate);
			jsonObj.accumulate("jour_edate", jourEDate);
			jsonObj.accumulate("jour_desc", jourDesc);
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
				jsonRow.accumulate("pl_web", a_cur.getString(a_cur.getColumnIndex(DBAdapter.M_KEY_WEB)));
				jsonRow.accumulate("pl_ratg", a_cur.getString(a_cur.getColumnIndex(DBAdapter.M_KEY_RATING)));
				jsonRow.accumulate("pl_fav", a_cur.getString(a_cur.getColumnIndex(DBAdapter.M_KEY_FAVORITE)));
				jsonRow.accumulate("pl_categ", a_cur.getString(a_cur.getColumnIndex(DBAdapter.M_KEY_CATEGORY_ID)));
				
				
				jsonArray.put(jsonRow);
				
				a_cur.moveToNext();
			}
			jsonObj.accumulate("activities", jsonArray);
		
			//osw.append("test:text:neco");
				osw.write(jsonObj.toString());
			osw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
    	//nastavení metadat
    	MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
        .setMimeType("text/plain")	
        .setTitle(jourName + ".json")
        .build();
        
    	//zapsání souboru
    	DriveFileResult dfResult = Drive.DriveApi.getFolder(mGoogleApiClient, folderTH)
    			.createFile(mGoogleApiClient, metadataChangeSet, ctResult.getContents()).await();
    	if (!dfResult.getStatus().isSuccess()) {
            Log.w(TAG, "Error while trying to create the file");
            return null;
        }
    	fileID = dfResult.getDriveFile().getDriveId();         
        
        //uložit id souboru do DB
        ContentValues values = new ContentValues();
        values.put(DBAdapter.J_KEY_DRIVEID, fileID.encodeToString());
        
		//získat link na soubor        
        mContentRes.update(Uri.withAppendedPath(u, j_id), values, null, null);
		
        
        
		return fileID.encodeToString();
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		
		//Log.i(TAG, "result:"+result);

		if (result != null)
			Toast.makeText(mAct, R.string.jour_backup_ok, Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(mAct, R.string.jour_backup_fail, Toast.LENGTH_SHORT).show();
		
		mGoogleApiClient.disconnect();
	}
	
	
	/* metody nastavující pomocné komponenty */
	public void setGoogleApiClient(GoogleApiClient client){
		mGoogleApiClient = client;
	}
	public void setSheredPreferences(SharedPreferences pref) {
		mSett = pref;
	}

	public void setContentResolver(ContentResolver cr) {
		mContentRes = cr;
	}
	
	public void setActivity(Activity a){
		mAct = a;
	}
	
}
