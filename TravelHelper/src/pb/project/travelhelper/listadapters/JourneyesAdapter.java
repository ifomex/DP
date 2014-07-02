package pb.project.travelhelper.listadapters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pb.project.travelhelper.R;
import pb.project.travelhelper.database.DBAdapter;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

public class JourneyesAdapter extends SimpleCursorAdapter{

	public JourneyesAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
	}

	@Override
	public void bindView(View v, Context arg1, Cursor c) {
		TextView tn = (TextView) v.findViewById(R.id.jour_item_name);
		TextView ts = (TextView) v.findViewById(R.id.jour_item_sdate);
		//TextView te = (TextView) v.findViewById(R.id.jour_item_edate);
		
		
		
		tn.setText(c.getString(c.getColumnIndex(DBAdapter.J_KEY_NAME)));
		
		java.text.DateFormat df = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, Locale.getDefault());
		long dat = c.getLong(c.getColumnIndex(DBAdapter.J_KEY_START_DATE));
		long edat = c.getLong(c.getColumnIndex(DBAdapter.J_KEY_END_DATE));
		
		ts.setText(df.format(new Date(dat))+"  -  "+df.format(new Date(edat)));
		//te.setText(df.format(new Date(edat)));
		
		
	}
}
