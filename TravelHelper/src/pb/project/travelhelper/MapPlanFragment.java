package pb.project.travelhelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import pb.project.travelhelper.database.DBAdapter;
import pb.project.travelhelper.database.DBContentProvider;
import pb.project.travelhelper.utils.ShareTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;


public class MapPlanFragment extends SupportMapFragment
			implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final int ACTIVITIES_LOADER = 0x04;
	private static final String TAG = "MapPlanFragment";
	
	private OnMapPlanFragmentListener mListener;
	
	private GoogleMap mMap;
	private long mJourId;
	private Cursor mCur;
	private List<HashMap<String, String>> mMarkerList = new ArrayList<HashMap<String,String>>();
	
	public MapPlanFragment() {
		// Required empty public constructor
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		
		mJourId = getArguments().getLong("j_id");
		Log.i(TAG, "jour id:"+mJourId);
		
	}
/*
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment		
		return inflater.inflate(R.layout.fragment_map_plan, container, false);
	}
*/
	@Override
	public void onResume() {
		super.onResume();
		
		mMap = getMap();
		//mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.mappl_map)).getMap();
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setMyLocationButtonEnabled(true);
		
		mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
			@Override
			public View getInfoWindow(Marker mark) {
				return null;
			}
			
			@Override
			public View getInfoContents(Marker mark) {
				View v = null;
				for (HashMap<String, String> hmPlace : mMarkerList){
					if (hmPlace.get("mark_id").equals(mark.getId())){
						int pos = Integer.valueOf(hmPlace.get("cur_pos"));
						mCur.moveToPosition(pos);
						
						v = getActivity().getLayoutInflater().inflate(R.layout.activities_item, null);
						
						TextView tn = (TextView) v.findViewById(R.id.daypl_item_name),
								 td = (TextView) v.findViewById(R.id.daypl_item_stime),
								 tt = (TextView) v.findViewById(R.id.daypl_item_etime);
						ImageView ic = (ImageView) v.findViewById(R.id.daypl_item_catg);
						
						tn.setText(mCur.getString(mCur.getColumnIndex(DBAdapter.A_KEY_NAME)));
						long stim = mCur.getLong(mCur.getColumnIndex(DBAdapter.A_KEY_START_TIME));
						long etim = mCur.getLong(mCur.getColumnIndex(DBAdapter.A_KEY_END_TIME));						

						//ts.setText(DateFormat.format("HH:mm", stim));
						//te.setText(DateFormat.format("HH:mm", etim));
						
						java.text.DateFormat df = DateFormat.getDateFormat(getActivity());
						
						td.setText(df.format(new Date(stim)));
						Time st = new Time();
						Time et = new Time();
						st.set(stim);
						et.set(etim);
						
						tt.setText(st.format("%H:%M")+" - "+et.format("%H:%M"));
						
						switch (mCur.getInt(mCur.getColumnIndex(DBAdapter.A_KEY_CATEGORY_ID))) {
						case 0:
							ic.setImageResource(R.drawable.category_accommodation);
							break;
						case 1:
							ic.setImageResource(R.drawable.category_transport);
							break;
						case 2:
							ic.setImageResource(R.drawable.category_culture);
							break;
						case 3:
							ic.setImageResource(R.drawable.category_food);
							break;
						case 4:
							ic.setImageResource(R.drawable.category_active);
							break;
						case 5:
							ic.setImageResource(R.drawable.category_finance);
							break;
						case 6:
							ic.setImageResource(R.drawable.category_shopping);
							break;
						case 7:
							ic.setImageResource(R.drawable.category_health);
							break;
						default:
							break;
						}
					}
				}
				return v;
			}
		});
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker mark) {
				for (HashMap<String, String> hmPlace : mMarkerList){
					if (hmPlace.get("mark_id").equals(mark.getId())){
						int pos = Integer.valueOf(hmPlace.get("cur_pos"));
						mCur.moveToPosition(pos);

						String name = mCur.getString(mCur.getColumnIndex(DBAdapter.A_KEY_NAME)),
								plname = mCur.getString(mCur.getColumnIndex(DBAdapter.M_KEY_NAME)),
								addr = mCur.getString(mCur.getColumnIndex(DBAdapter.M_KEY_ADDRESS));
						final String link = mCur.getString(mCur.getColumnIndex(DBAdapter.M_KEY_WEB));
						long stim = mCur.getLong(mCur.getColumnIndex(DBAdapter.A_KEY_START_TIME)),
								etim = mCur.getLong(mCur.getColumnIndex(DBAdapter.A_KEY_END_TIME));
						Boolean fav = (mCur.getInt(mCur.getColumnIndex(DBAdapter.M_KEY_FAVORITE)) == 1 ? true : false);
						final long place_id = mCur.getLong(mCur.getColumnIndex(DBAdapter.A_KEY_PLACES_ID));
						
						AlertDialog.Builder builder = new Builder(getActivity());
						View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_acti_place_detail, null);
						
						TextView tv_nm = (TextView) v.findViewById(R.id.acti_det_name_text),
								tv_da = (TextView)v.findViewById(R.id.acti_det_date_text),
								tv_tm = (TextView)v.findViewById(R.id.acti_det_times_text),
								tv_pn = (TextView) v.findViewById(R.id.acti_det_plname_text),
								tv_pa = (TextView) v.findViewById(R.id.acti_det_pladdr_text);
						Button web_btn = (Button) v.findViewById(R.id.acti_det_web_btn);
						ToggleButton fav_btn = (ToggleButton) v.findViewById(R.id.acti_det_fav_btn);
						
						tv_nm.setText(name);
						tv_pn.setText(plname);
						tv_pa.setText(addr);
						java.text.DateFormat df = DateFormat.getDateFormat(getActivity());
						
						tv_da.setText(df.format(new Date(stim)));
						Time st = new Time();
						Time et = new Time();
						st.set(stim);
						et.set(etim);
						
						tv_tm.setText(st.format("%H:%M")+" - "+et.format("%H:%M"));
						
						if(link == null || link.isEmpty()){
							web_btn.setEnabled(false);
						}
						web_btn.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
								startActivity(browserIntent);
							}
						});
						
						fav_btn.setChecked(fav);
						fav_btn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
								ContentValues values = new ContentValues();
								
								values.put(DBAdapter.M_KEY_FAVORITE, (isChecked ? 1 : 0));
								Uri u = DBContentProvider.PLACES_URI;
								getActivity().getContentResolver().update(Uri.withAppendedPath(u, String.valueOf(place_id)), values, null, null);
								
							}
						});
						
						builder.setView(v)
								.setTitle(R.string.acti_detail_title)
								.create()
								.show();
						break;
					}
				}
			}
		});
		
		getLoaderManager().initLoader(ACTIVITIES_LOADER, null, this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnMapPlanFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnMapPlanFragmentInteractionListener");
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
		switch (item.getItemId()) {
		case R.id.menu_daypl_new:
			mListener.newActivity(-1);
			return true;
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

	public void setOnMapPlanFragmentListener(OnMapPlanFragmentListener listener){
		mListener = listener;
	}

	public interface OnMapPlanFragmentListener {
		public void newActivity(long a_id);
		public void shareJourney(long j_id, int shareOption);
	}

/*
 * LoaderManager.LoaderCallbacks<Cursor>  methods
 */
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(getActivity(), 
				DBContentProvider.ACTIVITIES_URI, null, 
				DBAdapter.A_KEY_JOURNEY_ID+"="+mJourId, null, "yes");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> lc, Cursor c) {
		mCur = c;
		mMarkerList.clear();

		new MarkerAdapter().execute(c);		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mMap.clear();
		mCur.close();
		mMarkerList.clear();
	}
	
	class MarkerAdapter extends AsyncTask<Cursor, Void, Void>{

		class markObj {
			MarkerOptions mo;
			int idx;
			public markObj(MarkerOptions mo, int idx) {
				this.mo = mo;
				this.idx = idx;
			}
		}
		
		double  plat = 0, plon = 0;
		int count = 0;
		List<markObj> markList = new ArrayList<MapPlanFragment.MarkerAdapter.markObj>();
		
		@Override
		protected Void doInBackground(Cursor... params) {
			Cursor c = params[0];
			count = c.getCount();			
						
			c.moveToFirst();
			for (int i = 0; i<c.getCount(); i++){
				String name = c.getString(c.getColumnIndex(DBAdapter.A_KEY_NAME));
				String pl_name = c.getString(c.getColumnIndex(DBAdapter.M_KEY_NAME));
				int categ = c.getInt(c.getColumnIndex(DBAdapter.A_KEY_CATEGORY_ID));
				
				double lat = c.getDouble(c.getColumnIndex(DBAdapter.M_KEY_LATITUDE));
				double lon = c.getDouble(c.getColumnIndex(DBAdapter.M_KEY_LONGITUDE));

				MarkerOptions mo = new MarkerOptions();
				mo.title(name+": "+pl_name);
				mo.position(new LatLng(lat, lon));
				switch (categ){
				case 0:
					mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_accommodation));
					break;
	            case 1:
	            	mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_transport));
	            	break;
	            case 2:
	            	mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_culture));
	            	break;
	            case 3:
	            	mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_food));
	            	break;
	            case 4:
	            	mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_active));
	            	break;
	            case 5:
	            	mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_finance));
	            	break;
	            case 6:
	            	mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_shopping));
	            	break;
	            case 7:
	            	mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_health));
	            	break;
	            }
				markList.add(new markObj(mo, i));
				
				c.moveToNext();
				
				plat += lat; plon += lon;
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
						
			for (markObj mObj : markList){
				
				Marker mark = mMap.addMarker(mObj.mo);
				
				HashMap<String, String> hmPlace = new HashMap<String, String>();
				hmPlace.put("cur_pos", String.valueOf(mObj.idx));
				hmPlace.put("mark_id", mark.getId());
				mMarkerList.add(hmPlace);
			}
			
			//posunutí pohledu nad støed bodù
			if (count > 0){
				plat /=count; plon /=count;
				mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(plat, plon)));
				mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
			}
		}
		
	}

}
