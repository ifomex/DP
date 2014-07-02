package pb.project.travelhelper;

import java.util.Calendar;

import pb.project.travelhelper.database.DBAdapter;
import pb.project.travelhelper.database.DBContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

public class NewJourneyFragment extends Fragment{//TODO uložit hodnoty pøi otoèení obrazovky

	private static final String TAG = "NewJourneyFragment";
	
	TextView mNameText, mDescText;
	DatePicker mSDate, mEDate;
	
	long mJourId;
	Boolean mIsEdit = false;
	
	public NewJourneyFragment() {
	}
	
	@Override
	public void onActivityCreated(Bundle sIS) {
		super.onActivityCreated(sIS);
		try {
			mNameText.setText(sIS.getString("j_name"));
			mDescText.setText(sIS.getString("j_desc"));
			mSDate.init(sIS.getInt("j_sday"), sIS.getInt("j_smon"), sIS.getInt("j_syer"), null);
			mEDate.init(sIS.getInt("j_eday"), sIS.getInt("j_emon"), sIS.getInt("j_eyer"), null);
		}catch (Exception e){
			Log.w(TAG, "Firts time init: "+e.getLocalizedMessage());
		}

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_new_journey, container, false);
		setHasOptionsMenu(true);
		
		mNameText = (EditText)v.findViewById(R.id.njour_name);
		mDescText = (EditText)v.findViewById(R.id.njour_desc);
		mSDate = (DatePicker)v.findViewById(R.id.njour_sdate);
		mEDate = (DatePicker)v.findViewById(R.id.njour_edate);
		
		//vyplnìní hodnot pøi editaci
		mJourId = getArguments().getLong("j_id");
		if (mJourId > 0){
			Uri u = DBContentProvider.JOURNEYES_URI;
			Cursor jc = getActivity().getContentResolver().query(Uri.withAppendedPath(u, String.valueOf(mJourId)), null, null, null, null);
			jc.moveToFirst();
			
			mNameText.setText(jc.getString(jc.getColumnIndex(DBAdapter.J_KEY_NAME)));
			mDescText.setText(jc.getString(jc.getColumnIndex(DBAdapter.J_KEY_DESCRIPTION)));
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(jc.getLong(jc.getColumnIndex(DBAdapter.J_KEY_START_DATE)));
			mSDate.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
			cal.setTimeInMillis(jc.getLong(jc.getColumnIndex(DBAdapter.J_KEY_END_DATE)));
			mEDate.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
			mIsEdit = true;
		}else {
			mIsEdit = false;
		}
		
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
				if (mNameText.getText().toString().isEmpty()){
					Toast.makeText(getActivity(), R.string.new_jour_name_error, Toast.LENGTH_SHORT).show();
					mNameText.requestFocus();
					return true;
				}
				
				saveJourney();
				
				getActivity().finish();
				return true;
			default: 
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void saveJourney(){	
		ContentValues values = new ContentValues();
		values.put(DBAdapter.J_KEY_NAME, mNameText.getText().toString());
		values.put(DBAdapter.J_KEY_DESCRIPTION, mDescText.getText().toString());
		int day = mSDate.getDayOfMonth();
		int mon = mSDate.getMonth();
		int year = mSDate.getYear();
		Calendar s = Calendar.getInstance();
		//s.set(year, mon, day);
		s.set(year, mon, day, 0, 0, 0);
		values.put(DBAdapter.J_KEY_START_DATE, s.getTimeInMillis());
		
		day = mEDate.getDayOfMonth();
		mon = mEDate.getMonth();
		year = mEDate.getYear();
		//s.set(year, mon, day);
		s.set(year, mon, day, 23, 59, 59);
		values.put(DBAdapter.J_KEY_END_DATE, s.getTimeInMillis());
		values.put(DBAdapter.J_KEY_DRIVEID, new String());
		
		if (mIsEdit){
			Uri u = DBContentProvider.JOURNEYES_URI;
			getActivity().getContentResolver().update(Uri.withAppendedPath(u, String.valueOf(mJourId)), values, null, null);
		}else {
			getActivity().getContentResolver().insert(DBContentProvider.JOURNEYES_URI, values);
		}
	}

	
	@Override
		public void onSaveInstanceState(Bundle outState) {
			String n = mNameText.getText().toString();
			if (!n.isEmpty())
				outState.putString("j_name", n);
			String d = mDescText.getText().toString();
			if (!d.isEmpty())
				outState.putString("j_desc", d);	
			outState.putInt("j_sday", mSDate.getDayOfMonth());
			outState.putInt("j_smon", mSDate.getMonth());
			outState.putInt("j_syer", mSDate.getYear());
			outState.putInt("j_eday", mEDate.getDayOfMonth());
			outState.putInt("j_emon", mEDate.getMonth());
			outState.putInt("j_eyer", mEDate.getYear());
			
			super.onSaveInstanceState(outState);
		}
	
}
