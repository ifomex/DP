package pb.project.travelhelper.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class HotelsJSONParser {

	public List<HashMap<String, String>> parse(JSONObject jsobject){
		
		JSONObject jHotList = null;
		// jHotels = null;
		
		try { 
			jHotList = jsobject.getJSONObject("HotelListResponse").getJSONObject("HotelList");
			int hotcount = jHotList.getInt("@size");
			switch (hotcount){
			case 0:
				return null;
			case 1:
				JSONObject JHotelO = jHotList.getJSONObject("HotelSummary");
				return getHotels(JHotelO);
			default:
				JSONArray jHotels = jHotList.getJSONArray("HotelSummary");
				return getHotels(jHotels);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	private List<HashMap<String, String>> getHotels(JSONArray jHotels){
		int hotelCount = jHotels.length();
		Log.i("HotelsParser", "hotel list count:"+hotelCount);
		
		List<HashMap<String, String>> hotelsList = new ArrayList<HashMap<String,String>>();
		HashMap<String, String> hotel = null;	

		/** Taking each place, parses and adds to list object */
		for(int i=0; i<hotelCount;i++){
			try {
				/** Call getPlace with place JSON object to parse the place */
				hotel = getHotel((JSONObject)jHotels.get(i));
				hotelsList.add(hotel);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return hotelsList;
	}
	
	private List<HashMap<String, String>> getHotels(JSONObject jHotels){
		List<HashMap<String, String>> hotelsList = new ArrayList<HashMap<String,String>>();
		HashMap<String, String> hotel = null;	
		
		hotel = getHotel(jHotels);
		hotelsList.add(hotel);
		
		return hotelsList;
	}
	
	private HashMap<String, String> getHotel(JSONObject jHotel){
		HashMap<String, String> hotel = new HashMap<String, String>();
		String placeName = "-NA-";
		String hotelId="";
		String address="-NA-";
		String city="";
		String rating="";
		String latitude="";
		String longitude="";
		String link = "";	
		
		try {
			// Extracting Place name, if available
			if(!jHotel.isNull("name")){
				placeName = jHotel.getString("name");
			}
			hotelId = jHotel.getString("hotelId");
			
			address = jHotel.getString("address1");
			city = jHotel.getString("city");
			
			rating = jHotel.getString("hotelRating");
			String loc = Locale.getDefault().toString();
			link = "http://travel.ian.com/index.jsp?pageName=hotAvail&cid=55505&mode=2&showInfo=true&locale="+loc+"&hotelID=" +hotelId; 
			
			latitude = jHotel.getString("latitude");
			longitude = jHotel.getString("longitude");			
			
			hotel.put("place_name", placeName);
			hotel.put("hotelId", hotelId);
			hotel.put("address", address);
			hotel.put("city", city);
			hotel.put("rating", rating);
			hotel.put("lat", latitude);
			hotel.put("lng", longitude);
			hotel.put("link", link);
			
			
		} catch (JSONException e) {			
			e.printStackTrace();
		}	
		return hotel;
	}
}
