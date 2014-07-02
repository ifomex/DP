package pb.project.travelhelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.annotation.TargetApi;
import android.os.Build;

public class TranslatorActivity extends ActionBarActivity {

	private static final String DEBUG_TAG = "TranslatorActivity";
	private Button DoBtn;
	private ImageButton SwitchLangBtn;
	private EditText InText, OutText;
	private Spinner InLang, OutLang;
	private Language mDefLang;
	private Map<String, Language> mLangMap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_translator);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Translate.setClientId("pb_project_TravelHelper");
		Translate.setClientSecret("qtUNoulXQvBp3q8LfMRPu3SzLqDTCv31SHkR3stAnMw=");
		Language.setClientId("pb_project_TravelHelper");
		Language.setClientSecret("qtUNoulXQvBp3q8LfMRPu3SzLqDTCv31SHkR3stAnMw=");
		
		
		// ZjiötÏnÌ jazyku za¯ÌzenÌ
		String code = Locale.getDefault().getLanguage();
		mDefLang = Language.fromString(code); 

		new AskLangList().execute();
		
		/*
		 * Seznamy vstupnÌho a v˝stupnÌho jazyku
		 */
		InLang = (Spinner)findViewById(R.id.in_lang_transl_spin);
		OutLang = (Spinner)findViewById(R.id.out_lang_transl_spin);

		/*
		 *TlaËÌtko pro vykon·nÌ p¯ekladu 
		 */
		DoBtn = (Button)findViewById(R.id.do_transl_btn);
		DoBtn.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (OutLang.getSelectedItemPosition() == 0) {
					Toast.makeText(TranslatorActivity.this, R.string.transl_lang_warning, Toast.LENGTH_LONG).show();
					return;
				}
				new AskForTranslate().execute(InText.getText().toString());
			}
		});
		
		/*
		 * TlaËÌtko p¯ehozenÌ jazyk˘
		 */
		SwitchLangBtn = (ImageButton)findViewById(R.id.switch_transl_btn);
		SwitchLangBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int position = OutLang.getSelectedItemPosition();
				OutLang.setSelection(InLang.getSelectedItemPosition());
				InLang.setSelection(position);
			}
		});
		
		/*
		 * VstupnÌ a v˝stupnÌ textov· pole
		 */
		InText = (EditText)findViewById(R.id.in_text_transl_text);
		OutText = (EditText)findViewById(R.id.out_text_transl_text);
		
		
		//nastavenÌ p¯edchozÌch jazyk˘
		if (savedInstanceState != null){
			InLang.setSelection(savedInstanceState.getInt("inlang_pos"));
			OutLang.setSelection(savedInstanceState.getInt("outlang_pos"));
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setTitle(R.string.title_activity_translator);
		}
	}

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
		outState.putInt("inlang_pos", InLang.getSelectedItemPosition());
		outState.putInt("outlang_pos", OutLang.getSelectedItemPosition());
		super.onSaveInstanceState(outState);
	}

	/**
	 * T¯Ìda pro p¯eklad textu na pozadÌ (komunikace posÌti)
	 * @author Petr
	 *
	 */
	private class AskForTranslate extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {			
			try {
				return Translate.execute(params[0], 
						mLangMap.get(InLang.getSelectedItem()), 
						mLangMap.get(OutLang.getSelectedItem()));	//Language.AUTO_DETECT, Language.CZECH
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			OutText.setText(result);
			super.onPostExecute(result);
		}
		
	}
	
	/**
	 * T¯Ìda zjiöùujÌcÌ seznam dostupn˝ch jazyk˘ pro p¯eklad
	 * @author Petr
	 *
	 */
	class AskLangList extends AsyncTask<Void, Void, List<String>>{

		String mDefLangString;
		
		@Override
		protected List<String> doInBackground(Void... params) {
			try {
				//ZÌsk·nÌ lokalozovan˝ch n·zv˘ a kÛd˘ jazyk˘
				mLangMap = Language.values(mDefLang);
				mDefLangString = mDefLang.getName(mDefLang);
				
				//vytvo¯enÌ seznamu pro rozbalovacÌ seznam
				return new ArrayList<String>(mLangMap.keySet());

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<String> result) {	
			Log.i(DEBUG_TAG, "dokonËeno zikavani listu jazyk˘");
			ArrayAdapter<String> adapt = new ArrayAdapter<String>(TranslatorActivity.this, android.R.layout.simple_spinner_item, result);
			adapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			InLang.setAdapter(adapt);
			OutLang.setAdapter(adapt);
			
			OutLang.setSelection(result.indexOf(mDefLangString));
			
			super.onPostExecute(result);
		}
		
	}

}
