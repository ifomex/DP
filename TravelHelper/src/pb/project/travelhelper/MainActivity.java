package pb.project.travelhelper;

import pb.project.travelhelper.database.DBAdapter;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements OnItemClickListener {

	private ListView mFuncList;
	
	public static final String PREF_NAME = "TravelHelperPrefs";
	private static final String PREF_ONETIME = "isOneTime";
	
	@SuppressWarnings("rawtypes")
	private final Class[] sActivities = new Class[] {
		DiaryActivity.class,
		NearPlacesActivity.class,
		TranslatorActivity.class,
		CurrencyConvActivity.class,
		ExpencesActivity.class,
		PackActivity.class
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mFuncList = (ListView)findViewById(R.id.main_list_func);
		mFuncList.setOnItemClickListener(this);
		mFuncList.setAdapter(new mainFuncArrayAdapter(this, R.layout.mainfunc_item, getResources().getStringArray(R.array.array_main_func)));
		
		//mFuncList.setAdapter(new ArrayAdapter<String>(this, R.layout.mainfunc_item, R.id.mainfunc_name, getResources().getStringArray(R.array.array_main_func)));
				
		SharedPreferences sett = getSharedPreferences(PREF_NAME, 0);
		Boolean onetime = sett.getBoolean(PREF_ONETIME, true);
		
		if (onetime) {
			//init database
			DBAdapter dba = new DBAdapter(this);
			dba.open();
			dba.initDatabase();
			dba.close();
			
			SharedPreferences.Editor ed = sett.edit();
			ed.putBoolean(PREF_ONETIME, false);
			
			ed.commit();
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
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int pos, long id) {
		Intent i = new Intent(this, sActivities[pos]);

		startActivity(i);
	}
	
	class mainFuncArrayAdapter extends ArrayAdapter<String> {

		public mainFuncArrayAdapter(Context context, int resource,
				String[] objects) {
			super(context, resource, objects);
			
			data_array = objects;
		}


		String[] data_array;
		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = getLayoutInflater().inflate(R.layout.mainfunc_item, parent, false);

			TextView tv_nm = (TextView) v.findViewById(R.id.mainfunc_name);
			tv_nm.setText(data_array[position]);
			
			ImageView im_co = (ImageView) v.findViewById(R.id.mainfunc_color);
			
			switch (position) {
			case 0:
				im_co.setBackgroundColor(0xff669900);
				break;
			case 1:
				im_co.setBackgroundColor(0xffcc0000);
				break;
			case 2:
				im_co.setBackgroundColor(0xff003399);
				break;
			case 3:
				im_co.setBackgroundColor(0xffff8800);
				break;
			case 4:
				im_co.setBackgroundColor(0xff7711aa);
				break;
			case 5:
				im_co.setBackgroundColor(0xffffbb33);
				break;
			}
			
			return v;//super.getView(position, convertView, parent);
		}
		
	}

}
