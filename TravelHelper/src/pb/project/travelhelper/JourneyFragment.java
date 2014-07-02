package pb.project.travelhelper;

import pb.project.travelhelper.database.DBAdapter;
import pb.project.travelhelper.database.DBContentProvider;
import pb.project.travelhelper.listadapters.JourneyesAdapter;
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


public class JourneyFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private JourneyesAdapter mAdapter;
	private OnJourneyFragmentListener mListener;
	
	private static final String TAG = "JourneyFragment";
	
	private static final int JOUR_LOADER = 0x02;
	
	public static final int BR_MODE_BACKUP = 0;
	public static final int BR_MODE_RESTORE = 1;

	/**
	 * Selected Item on context menu
	 */
	long mContextMenuListItemSelected = -1;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public JourneyFragment() {
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		registerForContextMenu(getListView());
		
		mAdapter = new JourneyesAdapter(getActivity(), R.layout.journey_item, null, 
				new String[] {DBAdapter.J_KEY_NAME, DBAdapter.J_KEY_START_DATE, DBAdapter.J_KEY_END_DATE}, 
				new int[] {R.id.jour_item_name, R.id.jour_item_sdate}); //, R.id.jour_item_edate
		
		setListAdapter(mAdapter);
		setEmptyText(getResources().getString(R.string.jour_noitem));
		getListView().setDivider(new ColorDrawable(Color.GRAY));
		getListView().setDividerHeight(1);
		
		getLoaderManager().initLoader(JOUR_LOADER, null, this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
		mListener = (OnJourneyFragmentListener)activity;
		}catch (ClassCastException e){
			throw new ClassCastException(activity.toString()
                    + " must implement OnJourneySelectedListener");
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		TextView tn = (TextView)v.findViewById(R.id.jour_item_name);
		Log.i(TAG, "selected item name: "+tn.getText().toString());
		mListener.onJourneySelected(tn.getText().toString(), id);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.jour, menu); 
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
		case R.id.menu_jour_new:
			mListener.onNewJourney(-1);
			return true;
		case R.id.menu_jour_restore_local:
			mListener.onLocalBackupRestore(BR_MODE_RESTORE, -1);
			return true;
		case R.id.menu_jour_restore_gdrive:
			mListener.onGDriveBackupRestore(BR_MODE_RESTORE, -1);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		getActivity().getMenuInflater().inflate(R.menu.jour_ctx, menu);

		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo a = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()){
		case R.id.jour_cmenu_edit:
			Log.i(TAG, "edit id:"+a.id);
			mListener.onNewJourney(a.id);
			return true;
		case R.id.jour_cmenu_del:
			Log.i(TAG, "delete item:"+a.id);
			Uri u = DBContentProvider.JOURNEYES_URI;
			getActivity().getContentResolver().delete(Uri.withAppendedPath(u, String.valueOf(a.id)), null, null);
			
			TextView tn = (TextView) a.targetView.findViewById(R.id.jour_item_name);
			Toast.makeText(getActivity(),
					getResources().getString(R.string.cm_del_pre_toast)+
					" "+tn.getText().toString()+" "+
					getResources().getString(R.string.cm_del_post_toast),
					Toast.LENGTH_SHORT).show();
			return true;
		case R.id.jour_cmenu_bup:
			mContextMenuListItemSelected = a.id;
			return true;
		case R.id.jour_cmenu_bup_local:
			mListener.onLocalBackupRestore(BR_MODE_BACKUP, mContextMenuListItemSelected);
			return true;
		case R.id.jour_cmenu_bup_gdrive:
			mListener.onGDriveBackupRestore(BR_MODE_BACKUP, mContextMenuListItemSelected); //a.id
			return true;
		case R.id.jour_cmenu_share:
			mContextMenuListItemSelected = a.id;
			return true;
		case R.id.jour_cmenu_gpshare:
			mListener.shareJourney(mContextMenuListItemSelected, ShareTask.SHARE_ITEM_GP);
			return true;
		case R.id.jour_cmenu_fashare:
			mListener.shareJourney(mContextMenuListItemSelected, ShareTask.SHARE_ITEM_FA);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated to
	 * the activity and potentially other fragments contained in that activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnJourneyFragmentListener {
		public void onJourneySelected(String name, long id);
		public void onNewJourney(long j_id); 
		public void onGDriveBackupRestore(int mode, long j_id);
		public void onLocalBackupRestore(int mode, long j_id);
		public void shareJourney(long j_id, int shareOption);
	}
	
	public void setOnJourneySelectedListener(OnJourneyFragmentListener listener){
		mListener = listener;
	}
	
//LoaderManager.LoaderCallbacks<Cursor> methods
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(getActivity(), DBContentProvider.JOURNEYES_URI, null, null, null, null);
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
