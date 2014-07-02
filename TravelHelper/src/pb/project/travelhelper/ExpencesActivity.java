package pb.project.travelhelper;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

public class ExpencesActivity extends ActionBarActivity implements
		JourneyFragment.OnJourneyFragmentListener,
		ExpenceFragment.OnExpenceFragmentListener {

	JourneyFragment mJourneyFrag;
	ExpenceFragment mExpenceFrag;

	private static final String TAG = "ExpenceActivity";

	static final String JFRAG_TAG = "jour_frag";
	static final String EFRAG_TAG = "expe_frag";

	private static final int F_JOUR = 1;
	private static final int F_EXPE = 2;

	private int mFragShown = F_JOUR;
	
	private String mJourneyName;
	private long mJourneyId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_expences);

		mJourneyFrag = new JourneyFragment();
		mJourneyFrag.setOnJourneySelectedListener(this);

		mExpenceFrag = new ExpenceFragment();

		try{
			mFragShown = savedInstanceState.getInt("show_frag", F_JOUR);	
			mJourneyId = savedInstanceState.getLong("jour_id");
			mJourneyName = savedInstanceState.getString("jour_name");
		}catch(Exception e){
			Log.w(TAG, "First-time create: "+e.toString());
		}
		
		switch (mFragShown) {
		case F_JOUR:
			showJFrag();
			break;
		case F_EXPE:
			showEFrag(mJourneyId, mJourneyName);
			break;
		}
		
		// Show the Up button in the action bar.
		setupActionBar();
	}

	void showJFrag() {
		FragmentManager frm = getSupportFragmentManager();

		FragmentTransaction ft = frm.beginTransaction();
		ft.replace(R.id.expe_frag_layout, mJourneyFrag, JFRAG_TAG);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();

		mFragShown = F_JOUR;

		getSupportActionBar().setTitle(
				getResources().getString(R.string.title_activity_expences));
	}

	void showEFrag(long j_id, String j_name) {
		Bundle arg = new Bundle();
		arg.putLong("j_id", j_id);
		mExpenceFrag.setArguments(arg);

		FragmentManager frm = getSupportFragmentManager();
		FragmentTransaction ft = frm.beginTransaction();
		ft.replace(R.id.expe_frag_layout, mExpenceFrag, EFRAG_TAG);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();

		mFragShown = F_EXPE;

		getSupportActionBar().setTitle(
				getResources().getString(R.string.title_activity_expences_to)
						+ j_name);
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("show_frag", mFragShown);
		outState.putLong("jour_id", mJourneyId);
		outState.putString("jour_name", mJourneyName);
		super.onSaveInstanceState(outState);
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
	public void onBackPressed() {
		switch (mFragShown) {
		case F_JOUR:
			finish();
			break;
		case F_EXPE:
			showJFrag();
			break;
		}

		// super.onBackPressed();
	}

	

	@Override
	public void onJourneySelected(String name, long id) {
		showEFrag(id, name);
		mJourneyName = name;
		mJourneyId = id;
	}

	@Override
	public void onNewJourney(long j_id) {
		Intent i = new Intent(this, NewItemActivity.class);
		i.putExtra("show_frag", NewItemActivity.FRAG_JOUR);
		i.putExtra("j_from", NewItemActivity.JFROM_E);
		i.putExtra("j_id", j_id);
		startActivity(i);
	}
	
	@Override
	public void onGDriveBackupRestore(int mode, long j_id) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onLocalBackupRestore(int mode, long j_id) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void onNewExpence(long expe_id, long j_id) {		
		Bundle arg = new Bundle();
		arg.putInt("show_frag", NewItemActivity.FRAG_EXPE);
		arg.putLong("expe_id", expe_id);
		arg.putLong("j_id", j_id);
		
		Intent i = new Intent(this, NewItemActivity.class);
		i.putExtras(arg);
		
		startActivity(i);
	}

	@Override
	public void shareJourney(long j_id, int shareOption) {
		// TODO Auto-generated method stub
		
	}



}
