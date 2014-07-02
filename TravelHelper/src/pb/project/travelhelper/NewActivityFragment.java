package pb.project.travelhelper;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import pb.project.travelhelper.database.DBAdapter;
import pb.project.travelhelper.database.DBContentProvider;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


public class NewActivityFragment extends Fragment 
		implements OnClickListener{

	private static final String TAG = "NewActivityFragment";
	private EditText mNameText, mPriceText;
	private Spinner mCategSpin, mCurrSpin;
	private TimePicker mSTimePick, mETimePick;
	private DatePicker mDatePick;
	private ImageButton mMapBtn, mFavBtn;
	private CheckBox mExpeChB;
	private TextView mPlaceName;
	
	long mActiId = -1, mJourId = -1;
	boolean mIsEdit = false;
	long mMaxDate, mMinDate;
	
	long mSelectedPlaceId=-1;
	Bundle mPlaceArgs = null;
	
	OnNewActivityFragmentListener mListener;
	
	public NewActivityFragment() {
		// Required empty public constructor
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		
		Bundle args = getArguments();
		mActiId = args.getLong("a_id");
		mJourId = args.getLong("j_id");
		
		Log.i(TAG, "jour_id:"+mJourId);
		
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_new_activity, container, false);
		
		mNameText = (EditText)v.findViewById(R.id.nacti_name);
		mPriceText = (EditText)v.findViewById(R.id.nacti_price);
		mCategSpin = (Spinner)v.findViewById(R.id.nacti_categ);
		mCurrSpin = (Spinner)v.findViewById(R.id.nacti_curr);
		mSTimePick = (TimePicker)v.findViewById(R.id.nacti_stime);
		mETimePick = (TimePicker)v.findViewById(R.id.nacti_etime);
		mDatePick = (DatePicker)v.findViewById(R.id.nacti_date);
		mMapBtn = (ImageButton)v.findViewById(R.id.nacti_map_btn);
		mFavBtn = (ImageButton)v.findViewById(R.id.nacti_fav_btn);
		mExpeChB = (CheckBox)v.findViewById(R.id.nacti_expe_chb);
		mPlaceName = (TextView) v.findViewById(R.id.nacti_placename);
		mSTimePick.setIs24HourView(true);
		mETimePick.setIs24HourView(true);
		mMapBtn.setOnClickListener(this);
		mFavBtn.setOnClickListener(this);
		
		//nastavení mìny dle lokalizace zaøízení
		Currency c=Currency.getInstance(Locale.getDefault());
		List<String> curr_array = Arrays.asList(getResources().getStringArray(R.array.Currencies)); 				
		int i =curr_array.indexOf(c.getCurrencyCode());
		mCurrSpin.setSelection(i);
		
		//nastavení omezení data dle aktivity
		Uri u = DBContentProvider.JOURNEYES_URI;
		Cursor curs = getActivity().getContentResolver().query(Uri.withAppendedPath(u, String.valueOf(mJourId)), null, null, null, null);
		curs.moveToFirst();
		mMinDate = curs.getLong(curs.getColumnIndex(DBAdapter.J_KEY_START_DATE));
		mMaxDate = curs.getLong(curs.getColumnIndex(DBAdapter.J_KEY_END_DATE));
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			mDatePick.setMinDate(mMinDate);
			mDatePick.setMaxDate(mMaxDate);
		}else{
			Log.w(TAG, "API Level < 11 so not restricting date range...");
		}
		
		//vyplnìní údajù uložených pøi pøedchozím ukonèení
		if (savedInstanceState != null) {
			try {
			mNameText.setText(savedInstanceState.getString("a_name"));
			mPriceText.setText(savedInstanceState.getString("a_price"));
			mCategSpin.setSelection(savedInstanceState.getInt("a_categ"));
			mCurrSpin.setSelection(savedInstanceState.getInt("a_curr"));
			mSTimePick.setCurrentHour(savedInstanceState.getInt("a_stime_h"));
			mSTimePick.setCurrentMinute(savedInstanceState.getInt("a_stime_m"));
			mETimePick.setCurrentHour(savedInstanceState.getInt("a_etime_h"));
			mETimePick.setCurrentMinute(savedInstanceState.getInt("a_etime_m"));
			mDatePick.init(savedInstanceState.getInt("a_date_y"),
					savedInstanceState.getInt("a_date_m"), 
					savedInstanceState.getInt("a_date_d"), null);			
			}catch (Exception e){
				Log.w(TAG, e.getLocalizedMessage());
			}
		}
				
		//vypnìní údajù z databáze pøi editaci
		if (mActiId > 0){
			u = DBContentProvider.ACTIVITIES_URI;
			Cursor acur = getActivity().getContentResolver().query(Uri.withAppendedPath(u, String.valueOf(mActiId)), null, null, null, null);
			acur.moveToFirst();
			
			u = DBContentProvider.PLACES_URI;
			mSelectedPlaceId = acur.getLong(acur.getColumnIndex(DBAdapter.A_KEY_PLACES_ID));
			Cursor pcur = getActivity().getContentResolver().query(Uri.withAppendedPath(u, String.valueOf(mSelectedPlaceId)), null, null, null, null);
			pcur.moveToFirst();
			
			mNameText.setText(acur.getString(acur.getColumnIndex(DBAdapter.A_KEY_NAME)));
			mPriceText.setText(acur.getString(acur.getColumnIndex(DBAdapter.A_KEY_PRICE)));
			mCategSpin.setSelection(acur.getInt(acur.getColumnIndex(DBAdapter.A_KEY_CATEGORY_ID)));
			int cidx =curr_array.indexOf(acur.getString(acur.getColumnIndex(DBAdapter.A_KEY_CURRENCY)));
			if (cidx > 0)			
				mCurrSpin.setSelection(i);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(acur.getLong(acur.getColumnIndex(DBAdapter.A_KEY_START_TIME)));
			mDatePick.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
			mSTimePick.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
			mSTimePick.setCurrentMinute(cal.get(Calendar.MINUTE));
			cal.setTimeInMillis(acur.getLong(acur.getColumnIndex(DBAdapter.A_KEY_END_TIME)));
			mETimePick.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
			mETimePick.setCurrentMinute(cal.get(Calendar.MINUTE));			
			mPlaceName.setText(pcur.getString(pcur.getColumnIndex(DBAdapter.M_KEY_NAME)));
			
			mIsEdit = true;
		}else{
			mIsEdit = false;
		}
		
		return v;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
		mListener = (OnNewActivityFragmentListener)activity;
		}catch (ClassCastException e){
			throw new ClassCastException(activity.toString()
                    + " must implement OnNewActivityFragmentListener");
		}
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
			saveActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		String n = mNameText.getText().toString();
		if (!n.isEmpty())
			outState.putString("a_name", n);
		n = mPriceText.getText().toString();
		if (!n.isEmpty())
			outState.putString("a_price", n);
		outState.putInt("a_categ", mCategSpin.getSelectedItemPosition());
		outState.putInt("a_curr", mCurrSpin.getSelectedItemPosition());
		outState.putInt("a_stime_h", mSTimePick.getCurrentHour());
		outState.putInt("a_stime_m", mSTimePick.getCurrentMinute());
		outState.putInt("a_etime_h", mETimePick.getCurrentHour());
		outState.putInt("a_etime_m", mETimePick.getCurrentMinute());
		outState.putInt("a_date_d", mDatePick.getDayOfMonth());
		outState.putInt("a_date_m", mDatePick.getMonth());
		outState.putInt("a_date_y", mDatePick.getYear());
		
		super.onSaveInstanceState(outState);
	}
	
	/**
	 * Metoda pro uložení údajù do DB
	 */
	private void saveActivity(){
		//oveøení korektnosti údajù
		String name = mNameText.getText().toString();
		if (name.isEmpty()) {
			Toast.makeText(getActivity(), R.string.new_acti_name_error, Toast.LENGTH_SHORT).show();
			return;
		}
		String price = mPriceText.getText().toString();
		
		int day = mDatePick.getDayOfMonth();
		int mon = mDatePick.getMonth();
		int year = mDatePick.getYear();
		int hour = mSTimePick.getCurrentHour();
		int min = mSTimePick.getCurrentMinute();
		Calendar cal = Calendar.getInstance();
		cal.set(year, mon, day, hour, min);
		long sdat = cal.getTimeInMillis();
		if (sdat < mMinDate || sdat > mMaxDate){
			Toast.makeText(getActivity(), R.string.new_acti_date_error, Toast.LENGTH_SHORT).show();
			return;
		}
		
		hour = mETimePick.getCurrentHour();
		min = mETimePick.getCurrentMinute();
		cal.set(year, mon, day, hour, min);
		long edat = cal.getTimeInMillis();
		if (edat < mMinDate || edat > mMaxDate){
			Toast.makeText(getActivity(), R.string.new_acti_date_error, Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (!mIsEdit)
		if (mSelectedPlaceId < 0 && mPlaceArgs == null){
			Toast.makeText(getActivity(), R.string.new_acti_place_error, Toast.LENGTH_SHORT).show();
			return;
		}
		
		//uložení údajù
		ContentValues values = new ContentValues();
		values.put(DBAdapter.A_KEY_NAME, name);
		values.put(DBAdapter.A_KEY_START_TIME, sdat);
		values.put(DBAdapter.A_KEY_END_TIME, edat);
		values.put(DBAdapter.A_KEY_CURRENCY, mCurrSpin.getSelectedItem().toString());
		if (!price.isEmpty()) {
			values.put(DBAdapter.A_KEY_PRICE, price);
			
			if (mExpeChB.isChecked()){
				ContentValues eval = new ContentValues();
				eval.put(DBAdapter.E_KEY_NAME, name);
				eval.put(DBAdapter.E_KEY_VALUE, price);
				eval.put(DBAdapter.E_KEY_CURRENCY, mCurrSpin.getSelectedItem().toString());
				eval.put(DBAdapter.E_KEY_DATE, sdat);
				eval.put(DBAdapter.E_KEY_JOURNEY_ID, mJourId);
				eval.put(DBAdapter.E_KEY_CATEGORY_ID, mCategSpin.getSelectedItemPosition());
				
				
				getActivity().getContentResolver().insert(DBContentProvider.EXPENCESLIST_URI, eval);
				Toast.makeText(getActivity(), R.string.new_acit_expe_save, Toast.LENGTH_SHORT).show();
			}
		}
		values.put(DBAdapter.A_KEY_CATEGORY_ID, mCategSpin.getSelectedItemPosition());
		values.put(DBAdapter.A_KEY_JOURNEY_ID, mJourId);
		
		if (mPlaceArgs != null){
			ContentValues cv = new ContentValues();
			cv.put(DBAdapter.M_KEY_NAME, mPlaceArgs.getString(DBAdapter.M_KEY_NAME));
			cv.put(DBAdapter.M_KEY_ADDRESS, mPlaceArgs.getString(DBAdapter.M_KEY_ADDRESS));
			cv.put(DBAdapter.M_KEY_LATITUDE, mPlaceArgs.getString(DBAdapter.M_KEY_LATITUDE));
			cv.put(DBAdapter.M_KEY_LONGITUDE, mPlaceArgs.getString(DBAdapter.M_KEY_LONGITUDE));
			cv.put(DBAdapter.M_KEY_RATING, mPlaceArgs.getString(DBAdapter.M_KEY_RATING));
			cv.put(DBAdapter.M_KEY_WEB, mPlaceArgs.getString(DBAdapter.M_KEY_WEB));
			cv.put(DBAdapter.M_KEY_CATEGORY_ID, mPlaceArgs.getString(DBAdapter.M_KEY_CATEGORY_ID));
			cv.put(DBAdapter.M_KEY_FAVORITE, 0);
			Uri u = getActivity().getContentResolver().insert(DBContentProvider.PLACES_URI, cv);
			
			mSelectedPlaceId =Long.valueOf(u.getLastPathSegment()); 
			if (mSelectedPlaceId < 0){
				Toast.makeText(getActivity(), R.string.new_acti_place_error, Toast.LENGTH_SHORT).show();				
				return;
			}
				
		}
		values.put(DBAdapter.A_KEY_PLACES_ID, mSelectedPlaceId);		
		
		if (mIsEdit){
			Uri u = DBContentProvider.ACTIVITIES_URI;
			getActivity().getContentResolver().update(Uri.withAppendedPath(u, String.valueOf(mActiId)), values, null, null);
		}else {
			getActivity().getContentResolver().insert(DBContentProvider.ACTIVITIES_URI, values);
		}
		getActivity().finish();
	}

/*
 * Buttons onClickListener
 */
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.nacti_map_btn:
			Log.i(TAG, "map btn clicked");
			int categ = mCategSpin.getSelectedItemPosition();
			mListener.OnNewPlaceSelected(categ);
			break;
		case R.id.nacti_fav_btn:
			Log.i(TAG, "fav btn clicked");
			showFavPlaces();
			break;
			//TODO pøidat search place
		}
	}
	
	private void showFavPlaces(){
		AlertDialog.Builder build = new Builder(getActivity());
		String selection = DBAdapter.M_KEY_FAVORITE+"=1";
		final Cursor cursor = getActivity().getContentResolver().query(DBContentProvider.PLACES_URI, null, selection, null, null);
		
		
		build.setTitle(R.string.new_acti_fav_dialog_title)
			.setCursor(cursor, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String name="";
				if (cursor.moveToPosition(which)){
					name = cursor.getString(cursor.getColumnIndex(DBAdapter.M_KEY_NAME));
					mSelectedPlaceId = cursor.getLong(cursor.getColumnIndex(DBAdapter.M_KEY_ID));
				}
				mPlaceArgs = null;
				mPlaceName.setText(name);
			}
		}, DBAdapter.M_KEY_NAME);
		build.show();
	}
	
	public void setPlaceArgs(Bundle args) {
		mPlaceArgs = args;
		mSelectedPlaceId = -1;
		
		mPlaceName.setText(args.getString(DBAdapter.M_KEY_NAME));
	}
	
	public void setOnNewActivityFragmentListener(OnNewActivityFragmentListener listener){
		mListener = listener;
	}
	
	public interface OnNewActivityFragmentListener {
		public void OnNewPlaceSelected(int categId);
	}
}
