package pb.project.travelhelper.listadapters;

import java.util.Calendar;
import java.util.Date;

import pb.project.travelhelper.R;
import pb.project.travelhelper.database.DBAdapter;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class DayPlanAdapter extends SimpleCursorAdapter{

	public DayPlanAdapter(Context context, int layout, Cursor c, String[] from,
			int[] to) {
		super(context, layout, c, from, to);
	}
	
	@Override
	public void bindView(View v, Context ctx, Cursor c) {
		TextView tv_st, tv_et, tv_nm;
		ImageView i_cat;
		
		tv_st = (TextView)v.findViewById(R.id.daypl_item_stime);
		tv_et = (TextView)v.findViewById(R.id.daypl_item_etime);
		tv_nm = (TextView)v.findViewById(R.id.daypl_item_name);
		i_cat = (ImageView)v.findViewById(R.id.daypl_item_catg);
		
		long stim = c.getLong(c.getColumnIndex(DBAdapter.A_KEY_START_TIME));
		long etim = c.getLong(c.getColumnIndex(DBAdapter.A_KEY_END_TIME));
		

		//tv_st.setText(DateFormat.format("hh:mm", stim));
		//tv_et.setText(DateFormat.format("hh:mm", etim));
		tv_nm.setText(c.getString(c.getColumnIndex(DBAdapter.A_KEY_NAME)));
		
		java.text.DateFormat df = DateFormat.getDateFormat(ctx);
		
		tv_st.setText(df.format(new Date(stim)));

		Time st = new Time();
		Time et = new Time();
		st.set(stim);
		et.set(etim);
		
		tv_et.setText(st.format("%H:%M")+" - "+et.format("%H:%M"));
		switch (c.getInt(c.getColumnIndex(DBAdapter.A_KEY_CATEGORY_ID))) {
		case 0:
			i_cat.setImageResource(R.drawable.category_accommodation);
			break;
		case 1:
			i_cat.setImageResource(R.drawable.category_transport);
			break;
		case 2:
			i_cat.setImageResource(R.drawable.category_culture);
			break;
		case 3:
			i_cat.setImageResource(R.drawable.category_food);
			break;
		case 4:
			i_cat.setImageResource(R.drawable.category_active);
			break;
		case 5:
			i_cat.setImageResource(R.drawable.category_finance);
			break;
		case 6:
			i_cat.setImageResource(R.drawable.category_shopping);
			break;
		case 7:
			i_cat.setImageResource(R.drawable.category_health);
			break;
		default:
			break;
		}
		//TODO dodìlat kategorii
	}

}
