package pb.project.travelhelper;

import pb.project.travelhelper.database.DBAdapter;
import pb.project.travelhelper.database.DBContentProvider;
import pb.project.travelhelper.listadapters.DayPlanAdapter;
import pb.project.travelhelper.utils.ShareTask;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;


public class DayPlanFragment extends ListFragment
				implements LoaderManager.LoaderCallbacks<Cursor>{

	private static final int ACTIVITIES_LOADER = 0x03;
	public static final int CMENU_EDIT = 0x01;
	public static final int CMENU_DEL = 0x02;
	private static final String TAG = "DayPlanFragment";
	
	private OnDayPlanFragmentListener mListener;
	
	private DayPlanAdapter mAdapter;
	private long mJourId;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public DayPlanFragment() {
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setHasOptionsMenu(true);
		registerForContextMenu(getListView());
		
		
		
		
		mAdapter = new DayPlanAdapter(getActivity(), R.layout.activities_item, null, 
				new String[] {DBAdapter.A_KEY_NAME}, new int[] {android.R.layout.simple_list_item_1});
		
		setEmptyText(getResources().getString(R.string.daypl_noitem));
		setListAdapter(mAdapter);
		getListView().setDivider(new ColorDrawable(Color.LTGRAY));
		getListView().setDividerHeight(1);
		
		
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		getLoaderManager().initLoader(ACTIVITIES_LOADER, null, this);
		try {
			mListener = (OnDayPlanFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnDayPlanFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.day_plan, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_daypl_new:
			mListener.newActivity(-1);
			return true;
		/*case R.id.menu_daypl_share:
			mListener.shareJourney(mJourId, 0);
			return true;*/
		case R.id.menu_daypl_gpshare:
			mListener.shareJourney(mJourId, ShareTask.SHARE_ITEM_GP);
			return true;
		case R.id.menu_daypl_fashare:
			mListener.shareJourney(mJourId, ShareTask.SHARE_ITEM_FA);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
		menu.add(Menu.NONE, CMENU_EDIT, Menu.NONE, R.string.cmenu_edit);
		menu.add(Menu.NONE, CMENU_DEL, Menu.NONE, R.string.cmenu_del);
		
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo a = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()){
		case CMENU_EDIT:
			Log.i(TAG, "edit id:"+a.id+" pos:"+a.position);
			mListener.newActivity(a.id);
			return true;
		case CMENU_DEL:			
			Log.i(TAG, "delete item:"+a.id);
			Uri u = DBContentProvider.ACTIVITIES_URI;
			getActivity().getContentResolver().delete(Uri.withAppendedPath(u, String.valueOf(a.id)), null, null);
			TextView tn = (TextView) a.targetView.findViewById(R.id.daypl_item_name);
			Toast.makeText(getActivity(), 
					getResources().getString(R.string.cm_del_pre_toast)+
					" "+tn.getText().toString()+" "+
					getResources().getString(R.string.cm_del_post_toast), 
					Toast.LENGTH_SHORT).show();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	public interface OnDayPlanFragmentListener {
		public void newActivity(long a_id);
		public void shareJourney(long j_id, int shareOption);
	}
	
	public void setOnDayPlanFragmentListener(OnDayPlanFragmentListener a){
		mListener = a;
	}

/*
 *	LoaderManager methods 
 */
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		mJourId = getArguments().getLong("j_id");
		Log.i(TAG, "jour id:"+mJourId);
		
		return new CursorLoader(getActivity(), 
				DBContentProvider.ACTIVITIES_URI, null, 
				DBAdapter.A_KEY_JOURNEY_ID+"="+mJourId, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
		mAdapter.swapCursor(c);
		setListShown(true);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
	}

}
