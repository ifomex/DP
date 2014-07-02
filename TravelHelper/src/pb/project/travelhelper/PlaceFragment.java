package pb.project.travelhelper;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

import pb.project.travelhelper.database.DBAdapter;
import pb.project.travelhelper.database.DBContentProvider;
import pb.project.travelhelper.utils.HotelsJSONParser;
import pb.project.travelhelper.utils.PlaceJSONParser;


import com.facebook.widget.FacebookDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.PlusShare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


public class PlaceFragment extends SupportMapFragment {

	private static final String TAG = "PalceFragment";
	private static final String GPLACES_API_KEY = "AIzaSyCE4AwI12pWL2vcv4uL9CEHNYgfrAWqRKQ";
	private static final String EANHOTELS_API_KEY = "8phaee3f386beqakzt2htmj5";
	
	private static final String TYPE_TRANSPORTATION = "airport|bus_station|parking|subway_station|taxi_stand|car_rental|gas_station";
	private static final String TYPE_CULTURE = "art_gallery|church|library|movie_theater|museum|synagogue";
	private static final String TYPE_FOOD = "bar|cafe|food|meal_takyaway|restaurant";
	private static final String TYPE_ACTIVE = "amusement_park|aquarium|bowling_alley|casino|gym|park|spa|stadium|zoo";
	private static final String TYPE_FINANCE = "accounting|atm|bank|finance|insurance_agency";
	private static final String TYPE_SHOPING = "clothing_store|grocery_or_supermarket|shoe_store|shopping_mall";
	private static final String TYPE_HEALTH = "dentist|doctor|hospital|pharmacy";
	
	public static final int FTYPE_NEAR = 0X01;
	public static final int FTYPE_NEW = 0x02;
	
	public static final int SHARE_ITEM_GP = 1;
	public static final int SHARE_ITEM_FA = 2;
	
	private GoogleMap mMap;
	private int mLayerType = GoogleMap.MAP_TYPE_NORMAL;
	private List<Integer> mSelectedTypes = new ArrayList<Integer>();
	//private List mSelectedTypes;

	private int mFragType;

	List<HashMap<String, String>> markerList = new ArrayList<HashMap<String,String>>();
	
	public PlaceFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		setHasOptionsMenu(true);
		mFragType = getArguments().getInt("frag_type");
		
		// Inflate the layout for this fragment		
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_place, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		//set map parameters
		mMap =((SupportMapFragment) getFragmentManager().findFragmentById(R.id.places_map)).getMap();
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setMyLocationButtonEnabled(true);
		mMap.setMapType(mLayerType);
		
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				for (HashMap<String, String> hmPlace : markerList){
					if (marker.getId().equals(hmPlace.get("marker_id"))){
						//Toast.makeText(getActivity(), hmPlace.get("place_name"), Toast.LENGTH_SHORT).show();
						showDetails(hmPlace);
						break;
					}
				}
				
			}
		});
		
		// center to user position 
		LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);

	        
	    LatLng lalo = new LatLng(location.getLatitude(),location.getLongitude());	
		mMap.moveCamera(CameraUpdateFactory.newLatLng(lalo));
		mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
		
		if (mFragType == FTYPE_NEW){
			int categ = getArguments().getInt("categId");
			findPlaces(categ);
		}
	}
	
	/**
	 * Zobrazení dialogu s detailem o místu
	 * @param hmPlace
	 */
	void showDetails(final HashMap<String, String> hmPlace){
		AlertDialog.Builder buil = new Builder(getActivity());
		LayoutInflater infl = getActivity().getLayoutInflater();
		View v = infl.inflate(R.layout.dilog_place_detail, null);
		
		TextView tvname = (TextView)v.findViewById(R.id.pldet_name);
		tvname.setText(hmPlace.get("place_name"));
		
		TextView tvaddr = (TextView)v.findViewById(R.id.pldet_address);
		tvaddr.setText(hmPlace.get("address"));
		//web button
		Button btn_web = (Button)v.findViewById(R.id.pldet_web_btn);
		final String link;
		link = hmPlace.get("link");
		if (link == null || link.isEmpty()){
			btn_web.setEnabled(false);
		}				
		btn_web.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
				startActivity(browserIntent);
			}
		});
		//favorites button
		Button btn_fav = (Button)v.findViewById(R.id.pldet_fav_btn);
		btn_fav.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ContentValues cv = new ContentValues();
				cv.put(DBAdapter.M_KEY_NAME, hmPlace.get("place_name"));
				cv.put(DBAdapter.M_KEY_ADDRESS, hmPlace.get("address"));
				cv.put(DBAdapter.M_KEY_LATITUDE, hmPlace.get("lat"));
				cv.put(DBAdapter.M_KEY_LONGITUDE, hmPlace.get("lng"));
				cv.put(DBAdapter.M_KEY_RATING, hmPlace.get("rating"));
				cv.put(DBAdapter.M_KEY_WEB, hmPlace.get("link"));
				cv.put(DBAdapter.M_KEY_CATEGORY_ID, hmPlace.get("category"));
				cv.put(DBAdapter.M_KEY_FAVORITE, 1);
				
				getActivity().getContentResolver().insert(DBContentProvider.PLACES_URI, cv);
				Toast.makeText(getActivity(), 
						getResources().getString(R.string.pldetail_fav_toast1)+
						" "+hmPlace.get("place_name")+ " "+
						getResources().getString(R.string.pldetail_fav_toast2), 
						Toast.LENGTH_SHORT).show();
			}
		});
		//share button
		Button btn_share = (Button) v.findViewById(R.id.pldet_sha_btn);
		btn_share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				final String name = hmPlace.get("place_name");
				String lat = hmPlace.get("lat"), 
						lon = hmPlace.get("lng");
				final String link = "http://maps.google.com/?q="+Uri.encode(name)+"@"+lat+","+lon;
				
				final String []items ={"Google+", "Facebook"};

				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.pldetail_sha_btn);
				builder.setItems(items, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				        switch (item){
				        case 0:
				        	Intent shareIntent = new PlusShare.Builder(getActivity())
									.setType("text/plain")
									.setText(getResources().getString(R.string.plac_share_title1) + " \"" + name + "\" " +
												getResources().getString(R.string.plac_share_title2))
									.setContentUrl(Uri.parse(link))
									.getIntent();
							
				        	startActivityForResult(shareIntent, 0);
				        	break;
				        case 1:
				        	FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(getActivity())
									.setName(getResources().getString(R.string.plac_share_title1) + " \"" + name + "\"")
									.setDescription(getResources().getString(R.string.plac_share_title2))
									.setLink(link)
									.build();
							//mFbUiHelper.trackPendingDialogCall(shareDialog.present());
				        	shareDialog.present();
				        	break;				        
				        }
				    }
				})
				.show();		
				
			}
		});
		
		RatingBar rb = (RatingBar)v.findViewById(R.id.pldet_ratingBar);
		rb.setNumStars(5);
		rb.setRating(Float.valueOf(hmPlace.get("rating")));
		
		if (mFragType == FTYPE_NEAR){
			buil.setPositiveButton(R.string.pldetail_ok_btn, null);
		}else{
			buil.setPositiveButton(R.string.pldetail_sel_btn, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent data = new Intent();
					data.putExtra(DBAdapter.M_KEY_NAME, hmPlace.get("place_name"));
					data.putExtra(DBAdapter.M_KEY_ADDRESS, hmPlace.get("address"));
					data.putExtra(DBAdapter.M_KEY_LATITUDE, hmPlace.get("lat"));
					data.putExtra(DBAdapter.M_KEY_LONGITUDE, hmPlace.get("lng"));
					data.putExtra(DBAdapter.M_KEY_RATING, hmPlace.get("rating"));
					data.putExtra(DBAdapter.M_KEY_WEB, hmPlace.get("link"));
					data.putExtra(DBAdapter.M_KEY_CATEGORY_ID, hmPlace.get("category"));
					getActivity().setResult(Activity.RESULT_OK, data);
					getActivity().finish();
					
				}
			})
			.setNegativeButton(R.string.pldetail_can_btn, null);
		}
		
		buil.setView(v)
			.setTitle(R.string.pldetail_title)
			.create()
			.show();
	}

/**
 * ActionBar actions
 * 	(non-Javadoc)
 * @see android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.map, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_map_type:
			mapLayerType();
			return true;
		case R.id.menu_map_filter:
			mMap.clear();
			markerList.clear();
			
			placeTypes();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
	/**
	 * Set map layer
	 */
	public void mapLayerType(){
		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setTitle(R.string.plac_map_lay_dial_title)
			.setSingleChoiceItems(R.array.array_map_layers, mLayerType-1, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mLayerType = which+1;
					mMap.setMapType(mLayerType);
					dialog.dismiss();
				}
			})
			.show();
	}
	
	/**
	 * Výbìr typu kategorie místa
	 */
	public void placeTypes(){
		 mSelectedTypes = new ArrayList<Integer>();
		 AlertDialog.Builder builder = new Builder(getActivity());
		 builder.setTitle(R.string.plac_type_dial_title)
		 	.setMultiChoiceItems(R.array.array_category, null, 
		 			new DialogInterface.OnMultiChoiceClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
							if (isChecked) {
			                       // If the user checked the item, add it to the selected items
		                       mSelectedTypes.add(which);
		                   } else if (mSelectedTypes.contains(which)) {
		                       // Else, if the item is already in the array, remove it 
		                	   mSelectedTypes.remove(Integer.valueOf(which));
		                   }
					}
				})
			.setPositiveButton(R.string.plac_type_dial_ok, 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						for (Object o : mSelectedTypes) {
							int cat_idx = (Integer) o;
							findPlaces(cat_idx);
						}							
					}
				})
			.show();
			
	}
	
	/**
	 * Vytvoøení dotazu dle kategorie místa
	 * @param categ_id Kategorie místa
	 */
	public void findPlaces(int categ_id){
		double lat = mMap.getCameraPosition().target.latitude;
		double lon = mMap.getCameraPosition().target.longitude;
		
		StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
		sb.append("location="+lat+","+lon);
		sb.append("&radius=5000");
		sb.append("&sensor=true");
		sb.append("&language="+Locale.getDefault().getDisplayLanguage());
		sb.append("&key="+GPLACES_API_KEY);
		sb.append("&types=");
		

		switch (categ_id){
		case 0:
			StringBuilder strb = new StringBuilder("http://dev.api.ean.com/ean-services/rs/hotel/v3/list?minorRev=[26]");
			strb.append("&cid=55505");
			strb.append("&apiKey="+EANHOTELS_API_KEY);
			strb.append("&latitude="+lat);
			strb.append("&longitude="+lon);
			strb.append("&searchRadius=5");
			strb.append("&searchRadiusUnit=KM");
			strb.append("&locale="+Locale.getDefault().toString());
			new HotelDownloadTask().execute(strb.toString());
			break;
		case 1:
			sb.append(TYPE_TRANSPORTATION);
			new PlaceDownloadTask().execute(sb.toString(), String.valueOf(categ_id));
			break;
		case 2:
			sb.append(TYPE_CULTURE);
			new PlaceDownloadTask().execute(sb.toString(), String.valueOf(categ_id));
			break;
		case 3:
			sb.append(TYPE_FOOD);
			Log.i(TAG, "dotaz: "+sb.toString());
			new PlaceDownloadTask().execute(sb.toString(), String.valueOf(categ_id));
			break;
		case 4:
			sb.append(TYPE_ACTIVE);
			new PlaceDownloadTask().execute(sb.toString(), String.valueOf(categ_id));
			break;
		case 5:
			sb.append(TYPE_FINANCE);
			new PlaceDownloadTask().execute(sb.toString(), String.valueOf(categ_id));
			break;
		case 6:
			sb.append(TYPE_SHOPING);
			new PlaceDownloadTask().execute(sb.toString(), String.valueOf(categ_id));
			break;
		case 7:
			sb.append(TYPE_HEALTH);
			new PlaceDownloadTask().execute(sb.toString(), String.valueOf(categ_id));
			break;
		}
		
	}
	
	/** 
	 * A method to download json data from url 
	 * */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
                URL url = new URL(strUrl);                                

                // Creating an http connection to communicate with url 
                urlConnection = (HttpURLConnection) url.openConnection();                
                // Connecting to url 
                urlConnection.connect();                
                // Reading data from url 
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
                StringBuffer sb  = new StringBuffer();

                String line = "";
                while( ( line = br.readLine())  != null){
                        sb.append(line);
                }
                data = sb.toString();
                
                br.close();

        }catch(Exception e){
                Log.d("Exception while downloading url", e.toString());
        }finally{
                iStream.close();
                urlConnection.disconnect();
        }

        return data;
    }  
	
	/**
	 * Class to download Google Places
	 *
	 */
	private class PlaceDownloadTask extends AsyncTask<String, Void, List<HashMap<String,String>>>{

		String data = null, categ = null;
		
		@Override
		protected List<HashMap<String,String>> doInBackground(String... urls) {
			try {
				data = downloadUrl(urls[0]);
				categ = urls[1];
			} catch (IOException e) {
				Log.d(TAG, e.toString());
				e.printStackTrace();
			}

			/*
			String filename = "try_file.txt";
			FileOutputStream outputStream;

			try {
			  outputStream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
			  outputStream.write(result.getBytes());
			  outputStream.close();
			} catch (Exception e) {
			  e.printStackTrace();
			}
			*/
			
			List<HashMap<String, String>> places = null;			
			PlaceJSONParser placeJsonParser = new PlaceJSONParser();
			JSONObject jObject;
        
	        try{
	        	jObject = new JSONObject(data);
	        	
	        	
	            /** Getting the parsed data as a List construct */
	            places = placeJsonParser.parse(jObject);
	            
	        }catch(Exception e){
	                Log.d("Exception",e.toString());
	        }
	        return places;
		}
		
		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(List<HashMap<String,String>> list){			
			
			// Clears all the existing markers 
			//mMap.clear();
			
			for(int i=0;i<list.size();i++){
			
				// Creating a marker
	            MarkerOptions markerOptions = new MarkerOptions();
	            
	            // Getting a place from the places list
	            HashMap<String, String> hmPlace = list.get(i);
	            
	            if (hmPlace.isEmpty())
	            	continue;
	            
	            double lat = Double.parseDouble(hmPlace.get("lat"));	            
	            double lng = Double.parseDouble(hmPlace.get("lng"));
	            
	            String name = hmPlace.get("place_name");
	            
	            LatLng latLng = new LatLng(lat, lng);
	            markerOptions.position(latLng);
	
	            markerOptions.title(name);	    
	            
	            switch (Integer.valueOf(categ)){
	            case 1:
	            	markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_transport));
	            	break;
	            case 2:
	            	markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_culture));
	            	break;
	            case 3:
	            	markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_food));
	            	break;
	            case 4:
	            	markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_active));
	            	break;
	            case 5:
	            	markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_finance));
	            	break;
	            case 6:
	            	markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_shopping));
	            	break;
	            case 7:
	            	markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_health));
	            	break;
	            }
	            	
	            // Placing a marker on the touched position
	            Marker mark = mMap.addMarker(markerOptions);            
	            
	            hmPlace.put("marker_id", mark.getId());
	            hmPlace.put("place_type", "place");
	            
	            hmPlace.put("category", categ);
	            
	            new PlaceDetailDownTask().execute(hmPlace);
//			            markerList.add(hmPlace);
			}		
			
		}
	}

	
	
	/**
	 * Class to download EAN Hotels
	 *
	 */
	private class HotelDownloadTask extends AsyncTask<String, Void, List<HashMap<String,String>>>{

		String data = null;
		
		@Override
		protected List<HashMap<String,String>> doInBackground(String... urls) {
			try {
				data = downloadUrl(urls[0]);
			} catch (IOException e) {
				Log.d(TAG, e.toString());
				e.printStackTrace();
			}
			
			/*
			String filename = "try_file.txt";
			FileOutputStream outputStream;

			try {
			  outputStream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
			  outputStream.write(result.getBytes());
			  outputStream.close();
			} catch (Exception e) {
			  e.printStackTrace();
			}
			*/
			
			JSONObject job;
			List<HashMap<String, String>> hotels = null;
			HotelsJSONParser hp = new HotelsJSONParser();
			
			try {
				job = new JSONObject(data);
				
				hotels = hp.parse(job);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return hotels;
		}
		
		@Override
		protected void onPostExecute(List<HashMap<String, String>> list) {
			// Clears all the existing markers 
		//	mMap.clear();
			
			if (list == null){
				Log.w(TAG, "No hotels");
				return;
			}
			
			for(int i=0;i<list.size();i++){
			
				// Creating a marker
	            MarkerOptions markerOptions = new MarkerOptions();
	            
	            // Getting a place from the places list
	            HashMap<String, String> hmPlace = list.get(i);

	            double lat = Double.parseDouble(hmPlace.get("lat"));
	            double lng = Double.parseDouble(hmPlace.get("lng"));
	            
	            String name = hmPlace.get("place_name");
	            
	            LatLng latLng = new LatLng(lat, lng);
	            
	            // Setting the position for the marker
	            markerOptions.position(latLng);
	
	            // Setting the title for the marker. 
	            //This will be displayed on taping the marker
	            markerOptions.title(name);	       //    + " : " + vicinity  
	            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.categ_marker_accommodation));
	            
	            // Placing a marker on the touched position
	            Marker mark = mMap.addMarker(markerOptions);            
	            
	            hmPlace.put("marker_id", mark.getId());
	            hmPlace.put("place_type", "hotel");           
	            hmPlace.put("category", "0");
	            
	            markerList.add(hmPlace);
			}
		}
	}
	
	/**
	 * Class to download extra detail of GooglePlaces
	 *
	 */
	private class PlaceDetailDownTask extends AsyncTask<HashMap<String, String>, Void, String>{
		@Override
		protected String doInBackground(HashMap<String, String>... params) {
			String ref = params[0].get("reference");
			
			StringBuilder sb =  new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
			sb.append("key="+GPLACES_API_KEY);
			sb.append("&reference="+ref);
			sb.append("&sensor=true");
			sb.append("&language="+Locale.getDefault().getDisplayLanguage());
			
			String data = null;
			try {
				data = downloadUrl(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				JSONObject jPlDet = new JSONObject(data);
				
				JSONObject place = jPlDet.getJSONObject("result");
				String web = null;
				if (!place.isNull("website")){
					web = place.getString("website");
				}else{
					web = "";
				}
					
				String address="";
				if (!place.isNull("formatted_address")){
					address = place.getString("formatted_address");
				}
				params[0].put("address", address);
				params[0].put("link", web);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			markerList.add(params[0]);
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}
	}

}
