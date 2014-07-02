package pb.project.travelhelper;

import pb.project.travelhelper.database.DBAdapter;
import pb.project.travelhelper.database.DBContentProvider;
import pb.project.travelhelper.listadapters.PackListAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;




public class PackListItemFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	//SimpleCursorAdapter mAdapter;
	PackListAdapter mAdapter;
	public static final int PACK_LIST_LOADER = 0x01;
	public static final int CMENU_EDIT = 0x01;
	public static final int CMENU_DEL = 0x02;
	
	private static final String TAG = "PackListItemFragment";


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setHasOptionsMenu(true);
		registerForContextMenu(getListView());
													
		mAdapter = new PackListAdapter(getActivity(), R.layout.packlist_item,
				null, new String[]{DBAdapter.P_KEY_NAME, DBAdapter.P_KEY_SEL}, 
				new int[]{R.id.PackLstItem_name, R.id.PackLstItem_check});
	

		setListAdapter(mAdapter);
		
		getListView().setClipToPadding(false);
		getListView().setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		
		getListView().setDivider(new ColorDrawable(Color.GRAY));
		getListView().setDividerHeight(1);
		
		setEmptyText(getResources().getString(R.string.packlst_noitem));
		setListShown(false);
		getLoaderManager().initLoader(PACK_LIST_LOADER, null, this);
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.pack, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.menu_pack_new:
				newItem("", -1);
				return true;
			case R.id.menu_pack_clear:
				clearSelection();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
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
		TextView tn = (TextView) a.targetView.findViewById(R.id.PackLstItem_name);
		switch (item.getItemId()){
		case CMENU_EDIT:
			Log.i(TAG, "edit id:"+a.id);
			newItem(tn.getText().toString(), a.id);
			return true;
		case CMENU_DEL:			
			Log.i(TAG, "delete item:"+a.id);
			Uri u = DBContentProvider.PACKLIST_URI;
			getActivity().getContentResolver().delete(Uri.withAppendedPath(u, String.valueOf(a.id)), null, null);
			
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
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Uri u = DBContentProvider.PACKLIST_URI;
		Cursor plcur = getActivity().getContentResolver().query(Uri.withAppendedPath(u, String.valueOf(id)), null, null, null, null);
		plcur.moveToFirst();
		int checked = plcur.getInt(plcur.getColumnIndex(DBAdapter.P_KEY_SEL));
		
		ContentValues val = new ContentValues();	
		int a = (checked==0 ? 1 : 0);
		val.put(DBAdapter.P_KEY_SEL, a); 
		
		
		getActivity().getContentResolver().update(Uri.withAppendedPath(u, String.valueOf(id)),
				val, null, null);
		super.onListItemClick(l, v, position, id);
	}


	/**
	 * Metoda umožòující vytvoøení nového prvku.
	 * Zobrazí dialog pro zadání názvu položky a poté uloží do databáze.
	 */
	private void newItem(final String name, final long pl_id){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		if (pl_id <= 0){
			builder.setTitle(R.string.packlst_new_item_name_dialog_title);
		}else{
			builder.setTitle(R.string.packlst_edit_item_name_dialog_title);
		}
		final EditText nametext = new EditText(getActivity());
		nametext.setText(name);
		nametext.setHint(R.string.packlst_new_item_name_dialog_message);
		builder.setView(nametext)
			.setPositiveButton(R.string.packlst_new_item_dialog_ok, 
					new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String newName = nametext.getText().toString();

					ContentValues cv = new ContentValues();
					
					
					Uri u = DBContentProvider.PACKLIST_URI;
					if (pl_id <= 0){ //nová položka
						cv.put(DBAdapter.P_KEY_NAME, newName);
						cv.put(DBAdapter.P_KEY_SEL, false);
						getActivity().getContentResolver().insert(u, cv);
					}else{ //editace souèasné položky
						cv.put(DBAdapter.P_KEY_NAME, newName);
						getActivity().getContentResolver().update(Uri.withAppendedPath(u, String.valueOf(pl_id)), cv, null, null);
					}
				}
			});
		builder.create().show();
	}
		
	/**
	 * Metoda na vymazání výbìru seznamu. Provede úpravu databáze a zobrazí.
	 */
	private void clearSelection() {
		ContentValues vals = new ContentValues();
		vals.put(DBAdapter.P_KEY_SEL, 0);
		getActivity().getContentResolver().update(DBContentProvider.PACKLIST_URI, vals, null, null);
		
		getLoaderManager().restartLoader(PACK_LIST_LOADER, null, this);
	}

//LoaderManager.LoaderCallback<Cursor> methods:
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader cl = new CursorLoader(getActivity(), DBContentProvider.PACKLIST_URI, null, null, null, null);
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
		//c.moveToFirst();		
		mAdapter.swapCursor(c);
		
		setListShown(true);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
	}



}
