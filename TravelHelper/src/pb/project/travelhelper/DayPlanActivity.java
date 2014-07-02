package pb.project.travelhelper;

import pb.project.travelhelper.utils.ShareTask;
import pb.project.travelhelper.utils.ShareTask.ShareTaskListener;

import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.google.android.gms.plus.PlusShare;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;

public class DayPlanActivity extends ActionBarActivity
			implements TabListener,
			DayPlanFragment.OnDayPlanFragmentListener,
			MapPlanFragment.OnMapPlanFragmentListener,
			ShareTaskListener{

	ActSectionPageAdapter mPageAdapter;
	
	private long mJourId = -1;
	private String mJourName;
	ViewPager mViewPager;
	
	private UiLifecycleHelper mFbUiHelper;
	
	private static final String TAG = "DayPlanActivity";
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_day_plan);
		
		mPageAdapter = new ActSectionPageAdapter(getSupportFragmentManager());
		
		mJourId = getIntent().getExtras().getLong("j_id");
		mJourName = getIntent().getExtras().getString("j_name");
		
		mViewPager = (ViewPager) findViewById(R.id.dplan_pager);
		mViewPager.setAdapter(mPageAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getSupportActionBar().setSelectedNavigationItem(position);
			}
		});
		
		// Show the Up button in the action bar.
		setupActionBar();
		
		mFbUiHelper = new UiLifecycleHelper(this, null);
		mFbUiHelper.onCreate(savedInstanceState);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	private void setupActionBar() {
		ActionBar ab = getSupportActionBar();
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		ab.setTitle(getResources().getString(R.string.title_activity_day_plan)+
				" " + mJourName);
		
		Tab tab = ab.newTab()
				.setText(R.string.daypl_tab_list)
				.setTabListener(this);
		ab.addTab(tab);
		
		tab = ab.newTab()
				.setText(R.string.daypl_tab_map)
				.setTabListener(this);
				
		ab.addTab(tab);
		
		
		
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
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
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mFbUiHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);

	    mFbUiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
	        @Override
	        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
	            Log.e("Activity", String.format("Error: %s", error.toString()));
	        }

	        @Override
	        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
	            Log.i("Activity", "Success!");
	        }
	    });
	}
	
	
/*
 * 	TabListener methods
 */
	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {	
	}
	
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// When the given tab is selected, switch to the corresponding page in the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
		ft.commit();
	}
	
	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {	
	}

	
	
/**
 * {@link FragmentPagerAdapter} vrací jeden z fragmenù, tvoøící aktivitu
 */
	public class ActSectionPageAdapter extends FragmentPagerAdapter {

		public ActSectionPageAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment frag = null;
			Bundle args = new Bundle();
			args.putLong("j_id", mJourId);
			switch (position){
			case 0:
				frag = new DayPlanFragment();
				frag.setArguments(args);
				return frag;
			case 1:
				frag = new MapPlanFragment();
				frag.setArguments(args);
				return frag;
			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}
		
	}


	@Override
	public void newActivity(long a_id) {
		Intent i = new Intent(this, NewItemActivity.class);
		i.putExtra("show_frag", NewItemActivity.FRAG_ACTI);
		i.putExtra("a_id", a_id);
		i.putExtra("j_id", mJourId);
		startActivity(i);
	}

	@Override
	public void shareJourney(long j_id, int shareOption) {
		ShareTask shTask = new ShareTask();
		shTask.setContext(this);
		shTask.setShareTaskListener(this);
		shTask.execute(String.valueOf(j_id), String.valueOf(shareOption));
		
	}

	@Override
	public void onShareTaskDone(String result, int shareOption, String JourName) {
		if (result != null){
			switch (shareOption) {
			case ShareTask.SHARE_ITEM_GP:
				Intent shareIntent = new PlusShare.Builder(DayPlanActivity.this)
				.setType("text/plain")
				.setText(getResources().getString(R.string.daypl_share_text) + " \"" + JourName + "\" " 
						+ getResources().getString(R.string.daypl_share_text2))
				.setContentUrl(Uri.parse(result))
				.getIntent();
		
				startActivityForResult(shareIntent, 0);
				break;
			case ShareTask.SHARE_ITEM_FA:
				FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(DayPlanActivity.this)
					.setName(getResources().getString(R.string.daypl_share_text) + " \"" + JourName + "\"")
					.setDescription(getResources().getString(R.string.daypl_share_text2))
					.setLink(result)
					.build();
					mFbUiHelper.trackPendingDialogCall(shareDialog.present());
				
				break;
			}
		}else{
			Toast.makeText(this, R.string.daypl_share_error, Toast.LENGTH_SHORT).show();
		}		
	}	
}
