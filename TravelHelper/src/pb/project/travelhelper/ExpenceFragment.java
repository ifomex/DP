package pb.project.travelhelper;

import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import pb.project.travelhelper.database.DBAdapter;
import pb.project.travelhelper.database.DBContentProvider;
import pb.project.travelhelper.listadapters.ExpencesAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class ExpenceFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

	public static final int EXPENCES_LIST_LOADER = 0x02;
	public static final int CMENU_EDIT = 0x01;
	public static final int CMENU_DEL = 0x02;
	
	private TextView mSumNameTV, mSumValueTV, mSumCurrTV,
			mStartDateFiltTV, mEndDateFiltTV;
	
	ExpencesAdapter mAdapter;
	long mCategory=-1, mJourney;
	boolean mIsCategFilt = false,
			mIsDateFilt = false;
	Calendar mSDateFilt = Calendar.getInstance(), 
			mEDateFilt = Calendar.getInstance();
	
	OnExpenceFragmentListener mListener;
	
	private static final String TAG = "ExpenceFragment";
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ExpenceFragment() {
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Bundle args = getArguments();
		mJourney = args.getLong("j_id");
		
		View sum_view = getActivity().getLayoutInflater().inflate(R.layout.expe_sum_item, null);
		mSumNameTV = (TextView)sum_view.findViewById(R.id.expe_item_name);
		mSumValueTV = (TextView)sum_view.findViewById(R.id.expe_item_value);
		mSumCurrTV = (TextView)sum_view.findViewById(R.id.expe_item_curr);
		getListView().addFooterView(sum_view, null, false);		
		
		mAdapter = new ExpencesAdapter(getActivity(), R.layout.expence_item, null,
				new String[]{DBAdapter.E_KEY_NAME,DBAdapter.E_KEY_DATE,DBAdapter.E_KEY_VALUE,DBAdapter.E_KEY_CURRENCY}, 
				new int[]{R.id.expe_item_name,R.id.expe_item_date,R.id.expe_item_value,R.id.expe_item_curr});
		
		setEmptyText(getResources().getString(R.string.expe_noitem));
		setListAdapter(mAdapter);
		getListView().setDivider(new ColorDrawable(Color.GRAY));
		getListView().setDividerHeight(1);
		
		getLoaderManager().initLoader(EXPENCES_LIST_LOADER, null, this);
		registerForContextMenu(getListView());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		View v = super.onCreateView(inflater, container, savedInstanceState);
		
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnExpenceFragmentListener)activity;
		}catch (ClassCastException e){
			throw new ClassCastException(activity.toString()
                    + " must implement OnExpenceFragmentListener");
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
			mListener.onNewExpence(a.id, mJourney);
			return true;
		case CMENU_DEL:			
			Log.i(TAG, "delete item:"+a.id);
			Uri u = DBContentProvider.EXPENCESLIST_URI;
			getActivity().getContentResolver().delete(Uri.withAppendedPath(u, String.valueOf(a.id)), null, null);
			TextView tn = (TextView) a.targetView.findViewById(R.id.expe_item_name);
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.expences, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_expe_new:
			mListener.onNewExpence(-1, mJourney);
			return true;
		case R.id.menu_expe_filt:
			Filter();
			return true;
		case R.id.menu_expe_sum:
			mSumNameTV.setText(R.string.expe_sum);
			mSumValueTV.setText(String.format("%.2f",mAdapter.getSumValue()));
			mSumCurrTV.setText(Currency.getInstance(Locale.getDefault()).getCurrencyCode() );
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}


	/**
	 * Metoda pro zobrazení dialogu s výbìrem filtrù
	 */
	private void Filter() {
		AlertDialog.Builder builder = new Builder(getActivity());
		View v = getActivity().getLayoutInflater().inflate(R.layout.dailog_expe_filter, null, false);
		final CheckBox dial_categ_check = (CheckBox)v.findViewById(R.id.dial_expe_filt_categ_check),
				dial_date_check = (CheckBox)v.findViewById(R.id.dial_expe_filt_date_check);
		final Spinner dial_categ_spin = (Spinner) v.findViewById(R.id.dial_expe_filt_categ_spin);
		Button dial_sdate_btn = (Button)v.findViewById(R.id.dial_expe_filt_sdate_btn),
				dial_edate_btn = (Button)v.findViewById(R.id.dial_expe_filt_edate_btn);
		mStartDateFiltTV = (TextView)v.findViewById(R.id.dial_expe_filt_sdate_text);
		mEndDateFiltTV = (TextView)v.findViewById(R.id.dial_expe_filt_edate_text);
		
		dial_sdate_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DatePickerDialog(getActivity(), startDateSet, 
						mSDateFilt.get(Calendar.YEAR), mSDateFilt.get(Calendar.MONTH),
						mSDateFilt.get(Calendar.DAY_OF_MONTH))
					.show();
				
			}
		});
		dial_edate_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DatePickerDialog(getActivity(), endDateSet, 
						mEDateFilt.get(Calendar.YEAR), mEDateFilt.get(Calendar.MONTH),
						mEDateFilt.get(Calendar.DAY_OF_MONTH))
					.show();
			}
		});
		
		builder.setTitle(R.string.expe_dial_filt_title)
				.setView(v)
				.setPositiveButton(R.string.expe_dial_filt_ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//category filter
						if (dial_categ_check.isChecked()){
							mCategory = dial_categ_spin.getSelectedItemPosition();
							mIsCategFilt = true;
						}else{
							mCategory = -1;
							mIsCategFilt = false;
						}
						
						//date filter
						if (dial_date_check.isChecked()){
							mIsDateFilt = true;
						}else{
							mIsDateFilt = false;
						}
						
						//restart loader
						mAdapter.setSumNull();
						getLoaderManager().restartLoader(EXPENCES_LIST_LOADER, null, ExpenceFragment.this);
					}
				})
				.show();
	}
	
	/**
	 * Reakce na výbìr poèáteèního data filtru
	 */
	DatePickerDialog.OnDateSetListener startDateSet=new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			//nastavení data a èasu
			mSDateFilt.set(Calendar.YEAR, year);
			mSDateFilt.set(Calendar.MONTH, monthOfYear);
			mSDateFilt.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			mSDateFilt.set(Calendar.HOUR, 0);
			mSDateFilt.set(Calendar.MINUTE, 0);
			mSDateFilt.set(Calendar.SECOND, 0);
			
			//zobrazení data
			java.text.DateFormat df = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, Locale.getDefault());
			mStartDateFiltTV.setText(df.format(new Date(mSDateFilt.getTimeInMillis())));
		}
	};
	
	/**
	 * Reakce na výbìr koneèného data filtru
	 */
	DatePickerDialog.OnDateSetListener endDateSet=new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			//nastavení data a èasu
			mEDateFilt.set(Calendar.YEAR, year);
			mEDateFilt.set(Calendar.MONTH, monthOfYear);
			mEDateFilt.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			mEDateFilt.set(Calendar.HOUR, 23);
			mEDateFilt.set(Calendar.MINUTE, 59);
			mEDateFilt.set(Calendar.SECOND, 59);
			
			//zobrazení data
			java.text.DateFormat df = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, Locale.getDefault());
			mEndDateFiltTV.setText(df.format(new Date(mEDateFilt.getTimeInMillis())));
		}
	};
	
	/**
	 * Rozhranní pro komunikaci s Activity
	 * @author Petr
	 *
	 */
	public interface OnExpenceFragmentListener {
		public void onNewExpence(long expe_id, long j_id);
	}
	
	public void setOnExpenceFragmentListener(OnExpenceFragmentListener listener){
		mListener = listener;
	}
	
	
//LoaderManager.LoaderCallbacks<Cursor> metods
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String selection = null;
		
		selection = DBAdapter.E_KEY_JOURNEY_ID+"="
					+String.valueOf(mJourney);
		
		if (mIsCategFilt){
			selection += " AND "+DBAdapter.E_KEY_CATEGORY_ID+"="
					+mCategory;
		}
		
		if (mIsDateFilt){
			selection += " AND "+DBAdapter.E_KEY_DATE+">"
					+String.valueOf(mSDateFilt.getTimeInMillis())
					+" AND "+DBAdapter.E_KEY_DATE+"<"
					+String.valueOf(mEDateFilt.getTimeInMillis());
		}
		
		
		Log.i(TAG, "sel:"+selection);
		
		CursorLoader cl = new CursorLoader(getActivity(), DBContentProvider.EXPENCESLIST_URI, null, selection, null, null);
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {		
		mAdapter.setSumNull();
		mSumNameTV.setText("");
		mSumValueTV.setText("");
		mSumCurrTV.setText("");		
		
		c.moveToFirst();		
		mAdapter.swapCursor(c);
		setListShown(true);		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
		//setListShown(true);
	}

}
