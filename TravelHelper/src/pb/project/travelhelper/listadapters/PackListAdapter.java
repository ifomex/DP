package pb.project.travelhelper.listadapters;

import pb.project.travelhelper.R;
import pb.project.travelhelper.R.id;
import pb.project.travelhelper.database.DBAdapter;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class PackListAdapter extends SimpleCursorAdapter {

	private int layout;
	private Cursor c;
	
	public PackListAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.layout = layout;
		this.c = c;
	}

	@Override
	public void bindView(View view, Context context, Cursor c) {
		String name = c.getString(c.getColumnIndex(DBAdapter.P_KEY_NAME));
		int check = c.getInt(c.getColumnIndex(DBAdapter.P_KEY_SEL));
		
		TextView p_name = (TextView)view.findViewById(R.id.PackLstItem_name);
		p_name.setText(name);
		
		CheckBox p_check = (CheckBox)view.findViewById(R.id.PackLstItem_check);
		p_check.setChecked(check == 0 ? false : true);
	}
	
	
}