package pb.project.travelhelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;

public class CurrencyConvActivity extends ActionBarActivity {
	
	
	private static final String TAG = "CurrencyConvActivity";
	private Button DoBtn;
	private EditText InText, OutText;
	private Spinner mInSpin, mOutSpin;
	private ImageButton mSwapBtn;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_currency_conv);
		// Show the Up button in the action bar.
		setupActionBar();
		
		/*
		 * Tlaèítko pro pøevedení
		 */
		DoBtn = (Button)findViewById(R.id.do_curconv_btn);
		DoBtn.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				OutText.setText("");
				
				String text = InText.getText().toString();
				if (text.isEmpty()) return;	
					
				String inCur = (String)mInSpin.getSelectedItem();
				String outCur = (String)mOutSpin.getSelectedItem();
					
					String url = "http://quote.yahoo.com/d/quotes.csv?s="+inCur+outCur+"=X&f=l1&e=.csv";
					Log.i(TAG, url);
					ConnectivityManager connMgr = (ConnectivityManager) 
				    getSystemService(Context.CONNECTIVITY_SERVICE);
			        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			        if (networkInfo != null && networkInfo.isConnected()) {
			            new DownloadPageTask().execute(url);
			        } else {
			        	Toast.makeText(getApplication(), R.string.curconv_connection_error, Toast.LENGTH_SHORT).show();
			        }
			}
		});
		
		
		/*
		 * Tlaèítko výmìny mìn
		 */
		mSwapBtn = (ImageButton)findViewById(R.id.switch_curconv_btn);
		mSwapBtn.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int position = mInSpin.getSelectedItemPosition();
				mInSpin.setSelection(mOutSpin.getSelectedItemPosition());
				mOutSpin.setSelection(position);
			}
		});
		
		/*
		 * Seznamy mìn
		 */
		mInSpin = (Spinner)findViewById(R.id.in_lang_curconv_btn);
		mOutSpin = (Spinner)findViewById(R.id.out_lang_curconv_btn);
		
		//nastavení výstupní mìny dle lokalizace zaøízení
		Currency c=Currency.getInstance(Locale.getDefault());
		String[] b = getResources().getStringArray(R.array.Currencies);
		List<String> a = Arrays.asList(b); 				
		int i =a.indexOf(c.getCurrencyCode());
		mOutSpin.setSelection(i);
				
		/*
		 * Textová pole
		 */
		InText = (EditText)findViewById(R.id.in_text_curconv_text);
		OutText = (EditText)findViewById(R.id.out_text_curconv_text);
		
		/*
		 * Naètení pøedchozího stavu
		 */
		if(savedInstanceState != null) {
			mInSpin.setSelection(savedInstanceState.getInt("incurr_pos"));
			mOutSpin.setSelection(savedInstanceState.getInt("outcurr_pos"));
		}
	}


	private void setupActionBar() {
		android.support.v7.app.ActionBar ab = getSupportActionBar();
		
		//ab.setDisplayHomeAsUpEnabled(true);
		ab.setTitle(R.string.title_activity_currency_conv);
	}

/**
 * ActionBar actions
 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("incurr_pos", mInSpin.getSelectedItemPosition());
		outState.putInt("outcurr_pos", mOutSpin.getSelectedItemPosition());
		super.onSaveInstanceState(outState);
	}
	
	/**
	 * Tøída pro stažení a zobrazení výsledku pøevodu
	 * @author Petr
	 */
	private class DownloadPageTask extends AsyncTask<String, Void, String>{
		
		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				Toast.makeText(getApplicationContext(), R.string.curconv_connection_error, Toast.LENGTH_SHORT).show();
				return;
			}
			Double curs = Double.parseDouble(result);
			
			Double value = Double.parseDouble(InText.getText().toString());
			
			OutText.setText( String.format("%.4f", (curs * value)));
		}

		@Override
		protected String doInBackground(String... urls) {
			// params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return null;

			}
		}

	}
	
	
	/**
	 * Metoda stažení dat z url
	 * @param myurl Adresa url dotazu
	 * @return Pøevedená hodnota
	 * @throws IOException
	 */
	private String downloadUrl(String myurl) throws IOException {
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
	        return sb.toString();
	        
	    // Makes sure that the InputStream is closed after the app is
	    // finished using it.
	    } finally {
	        if (is != null) {
	            is.close();
	        } 
	    }
	}
	


}
