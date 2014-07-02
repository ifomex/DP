package pb.project.travelhelper;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import pb.project.travelhelper.database.DBAdapter;
import pb.project.travelhelper.database.DBContentProvider;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link NewExpenceFragment.OnFragmentInteractionListener} interface to handle
 * interaction events.
 * 
 */
public class NewExpenceFragment extends Fragment {
	
	EditText mNameText, mValueText;
	DatePicker mDatePick;
	Spinner mCurrSpin, mCatgSpin;
	
	long mMaxDate, mMinDate;
	
	Boolean mIsEdit = false;
	long mExpeId;
	
	private static final String TAG = "NewExpenceFragment";

	public NewExpenceFragment() {
		// Required empty public constructor
	}

	@Override
	public void onActivityCreated(Bundle sIS) {
		super.onActivityCreated(sIS);
		
		if (sIS != null){
			mNameText.setText(sIS.getString("e_name"));
			mValueText.setText(sIS.getString("e_valu"));
			mCurrSpin.setSelection(sIS.getInt("e_curr"));
			mCatgSpin.setSelection(sIS.getInt("e_catg"));
			mDatePick.init(sIS.getInt("e_dyer"), sIS.getInt("e_dmon"), sIS.getInt("e_dday"), null);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater
				.inflate(R.layout.fragment_new_expence, container, false);
		setHasOptionsMenu(true);
		
		mNameText = (EditText)v.findViewById(R.id.nexpe_name);
		mValueText = (EditText)v.findViewById(R.id.nexpe_value);
		mDatePick = (DatePicker)v.findViewById(R.id.nexpe_date);
		mCurrSpin = (Spinner)v.findViewById(R.id.nexpe_curr);
		mCatgSpin = (Spinner)v.findViewById(R.id.nexpe_categ);
		
		//nastavení mìny dle lokalizace zaøízení
		Currency c=Currency.getInstance(Locale.getDefault());
		List<String> curr_array = Arrays.asList(getResources().getStringArray(R.array.Currencies)); 				
		int i =curr_array.indexOf(c.getCurrencyCode());
		mCurrSpin.setSelection(i);
		
		//nastavení omezení data dle aktivity
		long j_id = getArguments().getLong("j_id");
		Uri u = DBContentProvider.JOURNEYES_URI;
		Cursor curs = getActivity().getContentResolver().query(Uri.withAppendedPath(u, String.valueOf(j_id)), null, null, null, null);
		curs.moveToFirst();
		mMinDate = curs.getLong(curs.getColumnIndex(DBAdapter.J_KEY_START_DATE));
		mMaxDate = curs.getLong(curs.getColumnIndex(DBAdapter.J_KEY_END_DATE));
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			mDatePick.setMinDate(mMinDate);
			mDatePick.setMaxDate(mMaxDate);
		}else{
			Log.w(TAG, "API Level < 11 so not restricting date range...");
		}
		
		
		//vyplnení údajù pøi editaci
		mExpeId = getArguments().getLong("expe_id");
		if (mExpeId > 0){
			Uri eu = DBContentProvider.EXPENCESLIST_URI;
			Cursor ecur = getActivity().getContentResolver().query(
					Uri.withAppendedPath(eu, String.valueOf(mExpeId)), null, null, null, null);
			ecur.moveToFirst();
			
			mNameText.setText(ecur.getString(ecur.getColumnIndex(DBAdapter.E_KEY_NAME)));
			mValueText.setText(ecur.getString(ecur.getColumnIndex(DBAdapter.E_KEY_VALUE)));
			i =curr_array.indexOf(ecur.getString(ecur.getColumnIndex(DBAdapter.E_KEY_CURRENCY)));
			mCurrSpin.setSelection(i);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(ecur.getLong(ecur.getColumnIndex(DBAdapter.E_KEY_DATE)));
			mDatePick.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
			i =ecur.getInt(ecur.getColumnIndex(DBAdapter.E_KEY_CATEGORY_ID)); 
			mCatgSpin.setSelection(i);
			
			mIsEdit = true;
		}else {
			mIsEdit = false;
		}
		
		// Inflate the layout for this fragment
		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.new_item, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
		case R.id.menu_newitem_done:
			saveExpence();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void saveExpence(){
		Bundle args = getArguments();
		long mJourneyId = args.getLong("j_id");
		
		int day = mDatePick.getDayOfMonth();
		int mon = mDatePick.getMonth();
		int year = mDatePick.getYear();
		Calendar s = Calendar.getInstance();
		s.set(year, mon, day);
		long dat = s.getTimeInMillis();
		if (dat < mMinDate || dat > mMaxDate){
			Toast.makeText(getActivity(), R.string.new_expe_wrogdate, Toast.LENGTH_SHORT).show();
			return;
		}
		
		String name = mNameText.getText().toString();
		if (name.isEmpty()){
			Toast.makeText(getActivity(), R.string.new_expe_name_error, Toast.LENGTH_SHORT).show();
			mNameText.requestFocus();
			return;
		}
		double val = 0;
		try {
			val = Double.valueOf(mValueText.getText().toString());
		} catch (Exception e) {
			Toast.makeText(getActivity(), R.string.new_expe_value_error, Toast.LENGTH_SHORT).show();
			return;
		}
		
		ContentValues values = new ContentValues();
		values.put(DBAdapter.E_KEY_NAME, name);
		values.put(DBAdapter.E_KEY_VALUE, val);
		values.put(DBAdapter.E_KEY_CURRENCY, mCurrSpin.getSelectedItem().toString());
		values.put(DBAdapter.E_KEY_DATE, dat);
		values.put(DBAdapter.E_KEY_JOURNEY_ID, mJourneyId);
		values.put(DBAdapter.E_KEY_CATEGORY_ID, mCatgSpin.getSelectedItemPosition());
		
		if (mIsEdit){
			Uri eu = DBContentProvider.EXPENCESLIST_URI;
			getActivity().getContentResolver().update(Uri.withAppendedPath(eu, String.valueOf(mExpeId)), values, null, null); 
			Log.i(TAG, "Save edit");
		}else{
			getActivity().getContentResolver().insert(DBContentProvider.EXPENCESLIST_URI, values);
			Log.i(TAG, "Save new");
		}
		getActivity().finish();
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		String n = mNameText.getText().toString();
		if(!n.isEmpty())
			outState.putString("e_name", n);
		String v = mValueText.getText().toString();
		if(!v.isEmpty())
			outState.putString("e_valu", v);
		outState.putInt("e_curr", mCurrSpin.getSelectedItemPosition());
		outState.putInt("e_dday", mDatePick.getDayOfMonth());
		outState.putInt("e_dmon", mDatePick.getMonth());
		outState.putInt("e_d_yer", mDatePick.getYear());
		outState.putInt("e_catg", mCatgSpin.getSelectedItemPosition());
		super.onSaveInstanceState(outState);
	}
}
