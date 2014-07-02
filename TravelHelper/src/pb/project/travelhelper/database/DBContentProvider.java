package pb.project.travelhelper.database;

import pb.project.travelhelper.R.string;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DBContentProvider extends ContentProvider{

	static final int PACKITEMS = 100;
	static final int PACKITEMS_ID = 110;
	static final int EXPENCES = 200;
	static final int EXPENCE_ID = 210;
	static final int EXPENCE_SUM = 220;
	static final int JOURNEYES = 300;
	static final int JOURNEYES_ID = 310;
	static final int PLACES = 400;
	static final int PLACES_ID = 410;
	static final int ACTIVITIES = 500;
	static final int ACTIVITIES_ID = 510;
	
	private static final String TAG = "DBContentProvider";
	private static UriMatcher sUriMatcher;
	private DBAdapter mDBA;
	private static final String AUTHORITY = "pb.project.travelhelper.database.DBContentProvider";
	public static final Uri PACKLIST_URI = Uri.parse("content://" + AUTHORITY + "/" + DBAdapter.PACKLIST_TABLE_NAME);
	public static final Uri EXPENCESLIST_URI = Uri.parse("content://" + AUTHORITY + "/" + DBAdapter.EXPENCES_TABLE_NAME);
	public static final Uri EXPENCESLISTSUM_URI = Uri.parse("content://" + AUTHORITY + "/" + DBAdapter.EXPENCES_TABLE_NAME+"sum");
	public static final Uri JOURNEYES_URI = Uri.parse("content://" + AUTHORITY + "/" + DBAdapter.JOURNEY_TABLE_NAME);
	public static final Uri PLACES_URI = Uri.parse("content://" + AUTHORITY + "/" + DBAdapter.PLACES_TABLE_NAME);
	public static final Uri ACTIVITIES_URI = Uri.parse("content://" + AUTHORITY + "/" + DBAdapter.ACTIVITY_TABLE_NAME);
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, DBAdapter.PACKLIST_TABLE_NAME, PACKITEMS);
		sUriMatcher.addURI(AUTHORITY, DBAdapter.PACKLIST_TABLE_NAME+"/#", PACKITEMS_ID);
		
		sUriMatcher.addURI(AUTHORITY, DBAdapter.EXPENCES_TABLE_NAME, EXPENCES);
		sUriMatcher.addURI(AUTHORITY, DBAdapter.EXPENCES_TABLE_NAME+"/#", EXPENCE_ID);
		sUriMatcher.addURI(AUTHORITY, DBAdapter.EXPENCES_TABLE_NAME+"sum", EXPENCE_SUM);
		
		sUriMatcher.addURI(AUTHORITY, DBAdapter.JOURNEY_TABLE_NAME, JOURNEYES);
		sUriMatcher.addURI(AUTHORITY, DBAdapter.JOURNEY_TABLE_NAME+"/#", JOURNEYES_ID);
		
		sUriMatcher.addURI(AUTHORITY, DBAdapter.PLACES_TABLE_NAME, PLACES);
		sUriMatcher.addURI(AUTHORITY, DBAdapter.PLACES_TABLE_NAME+"/#", PLACES_ID);
		
		sUriMatcher.addURI(AUTHORITY, DBAdapter.ACTIVITY_TABLE_NAME, ACTIVITIES);
		sUriMatcher.addURI(AUTHORITY, DBAdapter.ACTIVITY_TABLE_NAME+"/#", ACTIVITIES_ID);
	}
	
	@Override
	public boolean onCreate() {
		mDBA = new DBAdapter(getContext());
		mDBA.open();
		return false;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		long rowId;
		switch (sUriMatcher.match(uri)){
		case PACKITEMS_ID:
			String id = uri.getLastPathSegment();
			rowId = mDBA.deletePackItem(DBAdapter.P_KEY_ID+"="+id);
			if(rowId > 0){
				Uri _uri = ContentUris.withAppendedId(PACKLIST_URI, rowId);
				getContext().getContentResolver().notifyChange(_uri, null);
			}
			break;
		case EXPENCE_ID:
			String e_id = uri.getLastPathSegment();
			rowId = mDBA.deleteExpence(DBAdapter.E_KEY_ID+"="+e_id);
			if(rowId > 0){
				Uri _uri = ContentUris.withAppendedId(EXPENCESLIST_URI, rowId);
				getContext().getContentResolver().notifyChange(_uri, null);
			}
			break;
		case JOURNEYES_ID:
			String j_id = uri.getLastPathSegment();
			rowId = mDBA.deleteJourney(DBAdapter.J_KEY_ID+"="+j_id);
			if(rowId > 0){
				Uri _uri = ContentUris.withAppendedId(JOURNEYES_URI, rowId);
				getContext().getContentResolver().notifyChange(_uri, null);
			}
			break;
		case ACTIVITIES_ID:
			String a_id = uri.getLastPathSegment();
			rowId = mDBA.deleteActivity(DBAdapter.A_KEY_ID+"="+a_id);
			if(rowId > 0){
				Uri _uri = ContentUris.withAppendedId(ACTIVITIES_URI, rowId);
				getContext().getContentResolver().notifyChange(_uri, null);
			}
			break;
		}
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId;
		switch (sUriMatcher.match(uri)) {
		case PACKITEMS:
			rowId = mDBA.createPackItem(values);
			if(rowId > 0){
				Uri _uri = ContentUris.withAppendedId(PACKLIST_URI, rowId);
				getContext().getContentResolver().notifyChange(_uri, null);
				return _uri;
			}
			break;
		case EXPENCES:
			rowId = mDBA.insertExpence(values);
			if(rowId > 0){
				Uri _uri = ContentUris.withAppendedId(EXPENCESLIST_URI, rowId);
				getContext().getContentResolver().notifyChange(_uri, null);
				return _uri;
			}
			break;
		case JOURNEYES:
			rowId = mDBA.insertJourney(values);
			if(rowId > 0){
				Uri _uri = ContentUris.withAppendedId(JOURNEYES_URI, rowId);
				getContext().getContentResolver().notifyChange(_uri, null);
				return _uri;
			}
			break;
		case PLACES:
			rowId = mDBA.insertPlace(values);
			if(rowId > 0){
				Uri _uri = ContentUris.withAppendedId(PLACES_URI, rowId);
				getContext().getContentResolver().notifyChange(_uri, null);
				return _uri;
			}
			break;
		case ACTIVITIES:
			rowId = mDBA.insertActivity(values);
			if(rowId > 0){
				Uri _uri = ContentUris.withAppendedId(ACTIVITIES_URI, rowId);
				getContext().getContentResolver().notifyChange(_uri, null);
				return _uri;
			}
			break;
		default:
			throw new IllegalArgumentException("Unknow uri "+uri);
		}
		
		return null;
	}
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor c = null;
		switch (sUriMatcher.match(uri)){
			case PACKITEMS:
				c = mDBA.fetchAllPackItems();
				break;
			case PACKITEMS_ID:
				String pl_id = uri.getLastPathSegment();
				c = mDBA.fetchIdPackItem(DBAdapter.P_KEY_ID+"="+pl_id);
				break;
			case EXPENCES:
				c = mDBA.fetchAllExpences(selection, selectionArgs);
				break;
			case EXPENCE_ID:
				String e_id = uri.getLastPathSegment();
				c = mDBA.fetchExpence(DBAdapter.E_KEY_ID+"="+e_id);
				break;
			case EXPENCE_SUM:
				String es_id = uri.getLastPathSegment();
				c = mDBA.fetchAllExpenceSUM(selection);
				break;
			case JOURNEYES:
				c = mDBA.fetchAllJourneyes(selection, selectionArgs);
				break;
			case JOURNEYES_ID:
				long j_id = Long.parseLong(uri.getLastPathSegment());
				c = mDBA.fetchJourney(j_id);
				break;
			case ACTIVITIES:
				if (sortOrder == null)
					c = mDBA.fetchAllActivities(selection);
				else
					c = mDBA.fetchAllActivitiesWithPlace(selection);
				break;
			case ACTIVITIES_ID:
				long a_id = Long.parseLong(uri.getLastPathSegment());
				c = mDBA.fetchIdActivity(DBAdapter.A_KEY_ID+"="+a_id);
				break;
			case PLACES:
				c = mDBA.fetchPlaceWebId(selection, selectionArgs);
				break;
			case PLACES_ID:
				String p_id = uri.getLastPathSegment();
				c = mDBA.fetchPlace(DBAdapter.M_KEY_ID+"="+p_id);
				break;
			default:
				throw new IllegalArgumentException("Unknow uri "+uri);
				
		}
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int rowCo;
		switch (sUriMatcher.match(uri)) {
		case PACKITEMS:
			rowCo = mDBA.updatePackItem(values, null);
			Log.i(TAG, "row affected: "+rowCo);
			if (rowCo>0){
				getContext().getContentResolver().notifyChange(PACKLIST_URI, null);
			}
			break;
		case PACKITEMS_ID:
			String id = uri.getLastPathSegment();
			rowCo = mDBA.updatePackItem(values, DBAdapter.P_KEY_ID+"="+id);
			if (rowCo>0){
				getContext().getContentResolver().notifyChange(PACKLIST_URI, null);
			}
			break;
		case EXPENCE_ID:
			String e_id = uri.getLastPathSegment();
			rowCo = mDBA.updateExpence(values, DBAdapter.E_KEY_ID+"="+e_id);
			if (rowCo>0){
				getContext().getContentResolver().notifyChange(EXPENCESLIST_URI, null);
			}
			break;
		case JOURNEYES_ID:
			String j_id = uri.getLastPathSegment();
			rowCo = mDBA.updateJourney(values, DBAdapter.E_KEY_ID+"="+j_id);
			if (rowCo>0){
				getContext().getContentResolver().notifyChange(JOURNEYES_URI, null);
			}
			break;
		case ACTIVITIES_ID:
			String a_id = uri.getLastPathSegment();
			rowCo = mDBA.updateIdActivity(values, DBAdapter.A_KEY_ID+"="+a_id);
			if (rowCo>0){
				getContext().getContentResolver().notifyChange(ACTIVITIES_URI, null);
			}
			break;
		case PLACES_ID:
			String m_id = uri.getLastPathSegment();
			rowCo = mDBA.updateIdPlace(values, DBAdapter.M_KEY_ID+"="+m_id);
			if (rowCo>0){
				getContext().getContentResolver().notifyChange(PLACES_URI, null);
				getContext().getContentResolver().notifyChange(ACTIVITIES_URI, null);
			}
			break;
		default:
			break;
		}
		return 0;
	}

}
