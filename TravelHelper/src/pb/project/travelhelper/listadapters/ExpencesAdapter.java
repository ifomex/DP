package pb.project.travelhelper.listadapters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import pb.project.travelhelper.R;
import pb.project.travelhelper.database.DBAdapter;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ExpencesAdapter extends SimpleCursorAdapter {

	public ExpencesAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		mCursList = new HashMap<String, Double>();
		
		mDefCurr = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
	}

	private static final String TAG = "ExpencesAdapter";
	
	private double mSum = 0; 
	private String mDefCurr;
	private HashMap<String, Double> mCursList;
	
	@Override
	public void bindView(View v, Context context, Cursor c) {
		super.bindView(v, context, c);
		TextView tn = (TextView)v.findViewById(R.id.expe_item_name);
		TextView td = (TextView)v.findViewById(R.id.expe_item_date);
		TextView tv = (TextView)v.findViewById(R.id.expe_item_value);
		TextView tc = (TextView)v.findViewById(R.id.expe_item_curr);
		ImageView ic = (ImageView)v.findViewById(R.id.expe_categ_image);
		
		tn.setText(c.getString(c.getColumnIndex(DBAdapter.E_KEY_NAME)));
		
		java.text.DateFormat df = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, Locale.getDefault());
		long dat = c.getLong(c.getColumnIndex(DBAdapter.E_KEY_DATE));
		td.setText(df.format(new Date(dat))); 
		double value = c.getDouble(c.getColumnIndex(DBAdapter.E_KEY_VALUE));
		tv.setText(String.valueOf(value));
		String curr = c.getString(c.getColumnIndex(DBAdapter.E_KEY_CURRENCY));
		tc.setText(curr);
		
		switch (c.getInt(c.getColumnIndex(DBAdapter.E_KEY_CATEGORY_ID))) {
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
		
		if (curr.equals(mDefCurr)){
			mSum += value;
		}
		else {
			new ConvertVal().execute(String.valueOf(value),curr);
		}
	}
	
	public void setSumNull(){
		mSum = 0;
	}
	
	public double getSumValue(){
		return mSum;
	}
	
	/**
	 * Tøída pro pøevod èástky do výchozí mìny
	 * @author Petr
	 *
	 */
	class ConvertVal extends AsyncTask<String, Void, Double> {
		
		@Override
		protected Double doInBackground(String... params) {
			double val = Double.valueOf(params[0]);
			String curr = params[1];
			Double curs = mCursList.get(curr);
			
			if (curs == null){	//kurs není v listu, je tøeba vytvoøit dotaz na web
				String url = "http://quote.yahoo.com/d/quotes.csv?s="+curr+mDefCurr+"=X&f=l1&e=.csv";
				String data = null;
				
                data = downloadUrl(url);
	            
				if (data != null){
					curs = Double.parseDouble(data);
					mCursList.put(curr, curs);
					val *= curs;
				}else{
					val = 0;
				}
			}else{ //kurs je v listu, použije se
				val *= curs;
			}
			return val;
		}
		
		@Override
		protected void onPostExecute(Double result) {
			mSum += result;
		}
	}
	
	private String downloadUrl(String myurl){
	    InputStream is = null;
	        
	    try {
	        URL url = new URL(myurl);
	        
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000 ); //milliseconds
	        conn.setConnectTimeout(15000 ); //milliseconds
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        // Starts the query 
	        conn.connect();
	        int response = conn.getResponseCode();
	        Log.d(TAG, "Connection response is: " + response);
	        is = conn.getInputStream();

	        // Convert the InputStream into a string
	        Reader reader = new InputStreamReader(is, "UTF-8");        
	        BufferedReader br = new BufferedReader(reader);
	        StringBuffer sb = new StringBuffer();
	        sb.append(br.readLine());
	        br.close();
		   /* char[] buffer = new char[len];
		    reader.read(buffer);
	        String contentAsString = new String(buffer);
	        return contentAsString;*/

		    // Makes sure that the InputStream is closed after the app is
		    // finished using it.
	        if (is != null) {
	            is.close();
	        } 
	        
	        return sb.toString();
	    }catch (IOException e){
	    	Log.w(TAG, e.getLocalizedMessage());

	    }
		return null;
	}
}
