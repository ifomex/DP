package pb.project.travelhelper;

import pb.project.travelhelper.utils.GDriveRestoreTask;
import pb.project.travelhelper.utils.LocalBackUpAsyncTask;
import pb.project.travelhelper.utils.LocalRestoreAsyncTask;
import pb.project.travelhelper.utils.GDriveBackUpTask;
import pb.project.travelhelper.utils.ShareTask;
import pb.project.travelhelper.utils.ShareTask.ShareTaskListener;

import com.facebook.widget.FacebookDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.plus.PlusShare;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.annotation.TargetApi;
import android.content.IntentSender.SendIntentException;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Build;

public class DiaryActivity extends ActionBarActivity implements 
		JourneyFragment.OnJourneyFragmentListener,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		ShareTaskListener {

	
	private static final String TAG = "DiaryActivity";
	private static final int REQUEST_CODE_RESOLUTION = 1;
	public static final int REQUEST_CODE_GDRIVE_FILE = 123;
	public static final int REQUEST_CODE_LOCAL_FILE = 234;
	GoogleApiClient mGoogleApiClient = null;
	private int mGDriveMode;
	long mJ_id = -1;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_diary);
		// Show the Up button in the action bar.
		setupActionBar();
	}
	


	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	



	@Override
	public void onJourneySelected(String name, long id) {
		
		Intent i = new Intent(this, DayPlanActivity.class);
		i.putExtra("j_id", id);
		i.putExtra("j_name", name);
		startActivity(i);
	}

	@Override
	public void onNewJourney(long j_id) {
		Intent i = new Intent(this, NewItemActivity.class);
		i.putExtra("show_frag", NewItemActivity.FRAG_JOUR);
		i.putExtra("j_from", NewItemActivity.JFROM_D);
		i.putExtra("j_id", j_id);
		startActivity(i);
	}

	@Override
	public void onGDriveBackupRestore(int mode, long j_id) {
		if (mGoogleApiClient == null){
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addApi(Drive.API)
		            .addScope(Drive.SCOPE_FILE)
		            .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
		            .addConnectionCallbacks(this)
		            .addOnConnectionFailedListener(this)
					.build();
		}
		mGDriveMode = mode;
		mJ_id = j_id;
		mGoogleApiClient.connect();
	}
	
	@Override
	public void onLocalBackupRestore(int mode, long j_id) {
		switch(mode){
		case JourneyFragment.BR_MODE_BACKUP:
			LocalBackUpAsyncTask bupTask = new LocalBackUpAsyncTask();
			bupTask.setActivity(this);
			bupTask.execute(String.valueOf(j_id));
			break;
		case JourneyFragment.BR_MODE_RESTORE:
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
		    intent.setType("*/*"); 
		    intent.addCategory(Intent.CATEGORY_OPENABLE);

		    startActivityForResult(Intent.createChooser(intent, 
		    		getResources().getString(R.string.jour_restor_select_file))
		    		, REQUEST_CODE_LOCAL_FILE);
		    
			break;
		}
	}
	
	@Override
	public void shareJourney(long j_id, int shareOption) {
		ShareTask shTask = new ShareTask();
		shTask.setContext(this);
		shTask.setShareTaskListener(this);
		shTask.execute(String.valueOf(j_id), String.valueOf(shareOption));
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
        case REQUEST_CODE_GDRIVE_FILE:
            if (resultCode == RESULT_OK) {
                DriveId driveId = (DriveId) data.getParcelableExtra(
                        OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                Log.i(TAG, "Selected file's ID: " + driveId);
                
                GDriveRestoreTask gdDownTask = new GDriveRestoreTask();
                gdDownTask.setActivity(this);
                gdDownTask.setGoogleApiClient(mGoogleApiClient);
                
                gdDownTask.execute(driveId.encodeToString());
            }
            break;
        case REQUEST_CODE_LOCAL_FILE:
        	Uri fileuri = data.getData();
        	Log.i(TAG, fileuri.toString());
        	LocalRestoreAsyncTask resTask = new LocalRestoreAsyncTask();
        	resTask.setActivity(this);
        	resTask.execute(fileuri.toString());
        default:
            super.onActivityResult(requestCode, resultCode, data);
        }
	}



	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!result.hasResolution()) {
            // show the localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }	
	}



	@Override
	public void onConnected(Bundle arg0) {
		Log.i(TAG, "GoogleApiClient connected");
		SharedPreferences sett = getSharedPreferences(MainActivity.PREF_NAME, 0);
		
		switch(mGDriveMode){
		case JourneyFragment.BR_MODE_RESTORE:
			DriveId folderId = DriveId.decodeFromString(sett.getString("gDriveFolderId", null));
			
			Log.i(TAG, folderId.toString());
			
			IntentSender intsender = Drive.DriveApi
					.newOpenFileActivityBuilder()
					.setMimeType(new String[]{"text/plain"})
					.setActivityStartFolder(folderId)
					.build(mGoogleApiClient);
			
			try {
				startIntentSenderForResult(intsender, REQUEST_CODE_GDRIVE_FILE, null, 0, 0, 0);
			} catch (SendIntentException e) {
				e.printStackTrace();
			}
			break;
		case JourneyFragment.BR_MODE_BACKUP:			
			GDriveBackUpTask gdTask = new GDriveBackUpTask();
			gdTask.setActivity(this);
			gdTask.setGoogleApiClient(mGoogleApiClient);
			gdTask.setSheredPreferences(sett);
			gdTask.execute(String.valueOf(mJ_id));
			break;
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Log.i(TAG, "GoogleApiClient connection suspended");
	}
	
	@Override
	public void onShareTaskDone(String result, int shareOption, String JourName) {
		if (result != null){
			switch (shareOption) {
			case ShareTask.SHARE_ITEM_GP:
				Intent shareIntent = new PlusShare.Builder(DiaryActivity.this)
				.setType("text/plain")
				.setText(getResources().getString(R.string.daypl_share_text) + " \"" + JourName + "\" " 
						+ getResources().getString(R.string.daypl_share_text2))
				.setContentUrl(Uri.parse(result))
				.getIntent();
		
				startActivityForResult(shareIntent, 0);
				break;
			case ShareTask.SHARE_ITEM_FA:
				FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(DiaryActivity.this)
					.setName(getResources().getString(R.string.daypl_share_text) + " \"" + JourName + "\"")
					.setDescription(getResources().getString(R.string.daypl_share_text2))
					.setLink(result)
					.build();
					shareDialog.present();
				
				break;
			}
		}else{
			Toast.makeText(this, R.string.daypl_share_error, Toast.LENGTH_SHORT).show();
		}		
	}

}
