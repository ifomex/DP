package pb.project.travelhelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

public class NewItemActivity extends ActionBarActivity
			implements NewActivityFragment.OnNewActivityFragmentListener{

	NewJourneyFragment mJourneyFrag;
	NewExpenceFragment mExpenceFrag;
	NewActivityFragment mActivityFrag;
	PlaceFragment mPlaceFragment;
	
	public static final int FRAG_EXPE = 0X01;
	public static final int FRAG_JOUR = 0X02;
	public static final int FRAG_ACTI = 0X03;
	public static final int FRAG_PLAC = 0X04;
	
	static final String JFRAG_TAG = "jour_frag";
	static final String EFRAG_TAG = "expe_frag";
	static final String AFRAG_TAG = "acti_frag";
	static final String PFRAG_TAG = "plac_frag";
	
	public static final int JFROM_D = 0X08;
	public static final int JFROM_E = 0X09;
	
	private static final int REQUEST_CODE = 0x04;
	//private static final String TAG = "NewItemActivity";
	
	private int mShowFrag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mShowFrag = getIntent().getExtras().getInt("show_frag");
		
		switch (mShowFrag) {
		case FRAG_JOUR:
			if(getIntent().getExtras().getInt("j_from") == JFROM_D){
				setTheme(R.style.Theme_Diary);
			}else{ //JFROM_E
				setTheme(R.style.Theme_Expen);
			}
			break;
		case FRAG_ACTI:
		case FRAG_PLAC:
			setTheme(R.style.Theme_Diary);
			break;
		case FRAG_EXPE:
			setTheme(R.style.Theme_Expen);
			break;
		}
		
		setContentView(R.layout.activity_new_item);
		
		mJourneyFrag = new NewJourneyFragment();
		mExpenceFrag = new NewExpenceFragment();
		mActivityFrag = new NewActivityFragment();
		mPlaceFragment = new PlaceFragment();
		
		
		
		FragmentManager frm = getSupportFragmentManager();
		FragmentTransaction ft = frm.beginTransaction();		
		
		Bundle args = new Bundle();
		switch (mShowFrag) {
		case FRAG_JOUR:
			long j_id = getIntent().getExtras().getLong("j_id");
			args.putLong("j_id", j_id);
			
			
			mJourneyFrag.setArguments(args);
			
			ft.replace(R.id.new_item_frag_layout, mJourneyFrag, JFRAG_TAG);
			
			if (j_id > 0){
				getSupportActionBar().setTitle(R.string.title_fragment_edit_journey);
			}else{
				getSupportActionBar().setTitle(R.string.title_fragment_new_journey);
			}				
			
			break;
			
		case FRAG_EXPE:
			long e_id =  getIntent().getExtras().getLong("expe_id");
			args.putLong("expe_id",e_id);
			args.putLong("j_id",  getIntent().getExtras().getLong("j_id"));
			
			
			mExpenceFrag.setArguments(args);
			
			ft.replace(R.id.new_item_frag_layout, mExpenceFrag, EFRAG_TAG);
			
			if (e_id > 0){
				getSupportActionBar().setTitle(R.string.title_fragment_editexpence);
			}else{
				getSupportActionBar().setTitle(R.string.title_fragment_newexpence);
			}
			
			break;
		case FRAG_ACTI:
			long a_id = getIntent().getExtras().getLong("a_id");
			args.putLong("a_id", a_id);
			args.putLong("j_id",  getIntent().getExtras().getLong("j_id"));
			
			mActivityFrag.setArguments(args);
			
			ft.replace(R.id.new_item_frag_layout, mActivityFrag, AFRAG_TAG);
			
			if (a_id > 0){
				getSupportActionBar().setTitle(R.string.title_fragment_editactivity);
			}else{
				getSupportActionBar().setTitle(R.string.title_fragment_newactivity);
			}
			
			break;
		case FRAG_PLAC:
			int categ = getIntent().getExtras().getInt("categId");
			
			args.putInt("frag_type", PlaceFragment.FTYPE_NEW);
			args.putInt("categId", categ);
			mPlaceFragment.setArguments(args);
			
			ft.replace(R.id.new_item_frag_layout, mPlaceFragment, PFRAG_TAG);
			
			getSupportActionBar().setTitle(R.string.title_fragment_new_place);
			setTheme(R.style.Theme_Diary);
			break;
		}
		
			
		
		if (savedInstanceState != null) {
			mJourneyFrag = (NewJourneyFragment) getSupportFragmentManager().getFragment(savedInstanceState, JFRAG_TAG);
			mExpenceFrag = (NewExpenceFragment) getSupportFragmentManager().getFragment(savedInstanceState, EFRAG_TAG);
			mActivityFrag = (NewActivityFragment) getSupportFragmentManager().getFragment(savedInstanceState, AFRAG_TAG);
			mPlaceFragment = (PlaceFragment) getSupportFragmentManager().getFragment(savedInstanceState, PFRAG_TAG);
		}
		
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}

	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	/*	switch (mShowFrag){
		case FRAG_JOUR:
			getSupportFragmentManager().putFragment(outState, JFRAG_TAG, mJourneyFrag);
			break;
		case FRAG_EXPE:
			getSupportFragmentManager().putFragment(outState, EFRAG_TAG, mExpenceFrag);
			break;
		case FRAG_ACTI:
			getSupportFragmentManager().putFragment(outState, AFRAG_TAG, mActivityFrag);
			break;
		case FRAG_PLAC:
			getSupportFragmentManager().putFragment(outState, PFRAG_TAG, mPlaceFragment);
			break;
		}
		super.onSaveInstanceState(outState);*/
	}


	@Override
	public void OnNewPlaceSelected(int categId) {
		Intent i = new Intent(this, NewItemActivity.class);
		i.putExtra("show_frag", FRAG_PLAC);
		i.putExtra("categId", categId);
		startActivityForResult(i, REQUEST_CODE);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			Bundle args = intent.getExtras();
			//String name = args.getString(DBAdapter.M_KEY_NAME);
			
			mActivityFrag.setPlaceArgs(args);
		}
		
	}
	
}
