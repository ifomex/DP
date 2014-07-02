package pb.project.travelhelper.database;


import java.io.IOException;

import pb.project.travelhelper.R;
import pb.project.travelhelper.utils.LocalRestoreAsyncTask;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter{
	
	private static final String TAG = "DBAdapter";

	public static final String P_KEY_ID = "_id";
	public static final String P_KEY_NAME = "item_name";
	public static final String P_KEY_SEL = "selected";
	
	public static final String J_KEY_ID = "_id";
	public static final String J_KEY_NAME = "journey_name";
	public static final String J_KEY_START_DATE = "s_date";
	public static final String J_KEY_END_DATE = "e_date";
	public static final String J_KEY_DESCRIPTION = "description";
	public static final String J_KEY_DRIVEID = "drive_id"; 
	
	public static final String A_KEY_ID = "_id";
	public static final String A_KEY_NAME = "act_name" ;
	public static final String A_KEY_START_TIME = "s_time";
	public static final String A_KEY_END_TIME = "e_time";
	public static final String A_KEY_CATEGORY_ID = "ac_category_id";
	public static final String A_KEY_JOURNEY_ID = "journey_id";
	public static final String A_KEY_PRICE = "price";
	public static final String A_KEY_CURRENCY = "cuurency";
	public static final String A_KEY_PLACES_ID = "places_id";
		
	public static final String E_KEY_ID = "_id";
	public static final String E_KEY_NAME = "expe_name";
	public static final String E_KEY_DATE = "date";
	public static final String E_KEY_VALUE = "value";
	public static final String E_KEY_CURRENCY = "currency";
	public static final String E_KEY_CATEGORY_ID = "ex_category_id";
	public static final String E_KEY_JOURNEY_ID = "journey_id";
	
	public static final String C_KEY_ID = "_id";
	public static final String C_KEY_NAME = "cate_name";
	public static final String C_KEY_TYPE = "type";
	
	public static final String M_KEY_ID = "_id";
	public static final String M_KEY_NAME = "place_name";
	public static final String M_KEY_LATITUDE = "lat";
	public static final String M_KEY_LONGITUDE = "lon";
	public static final String M_KEY_ADDRESS = "address";
	public static final String M_KEY_WEB = "web";
	public static final String M_KEY_PHONE = "phone";
	public static final String M_KEY_RATING = "rating";
	public static final String M_KEY_FAVORITE = "favotite";
	public static final String M_KEY_CATEGORY_ID = "pl_category_id";
	
	 static final String PACKLIST_TABLE_NAME = "packlst";
	 static final String JOURNEY_TABLE_NAME = "journey";
	 static final String ACTIVITY_TABLE_NAME = "activity";
	 static final String PLACES_TABLE_NAME = "places";
	 static final String EXPENCES_TABLE_NAME = "expences";
	 static final String CATEGORY_TABLE_NAME = "category";
	
	//The Android's default system path of your application database.
	 private static final String DB_PATH = "/data/data/pb.project.travelhelper/databases/";
	 public static final String DATABASE_INIT_ASSET = "initDataJourney";
	 private static final String DATABASE_NAME = "data";
	 private static final int DATABASE_VERSION = 6;
	
	/**
	 * SQLite command for creating table PACKLIST
	 */
	private static final String CREATE_PACKLIST_TABLE = 
			"create table " + PACKLIST_TABLE_NAME
			+ "(_id integer primary key autoincrement, "
			+ P_KEY_NAME + " text not null, "
			+ P_KEY_SEL + " integer);"
			;
	
	private static final String CREATE_JOURNEY_TABLE = 
			"create table " + JOURNEY_TABLE_NAME 
			+ " (_id integer primary key autoincrement, "
			+ J_KEY_NAME + " text not null, "
			+ J_KEY_START_DATE + " integer not null, "
			+ J_KEY_END_DATE + " integer not null, "
			+ J_KEY_DESCRIPTION + " text, "
			+ J_KEY_DRIVEID + " text);"
			;
			
	private static final String CREATE_ACTIVITY_TABLE = 
			"create table " + ACTIVITY_TABLE_NAME
			+" (_id integer primary key autoincrement, "
			+A_KEY_NAME + " text not null, "
			+A_KEY_START_TIME + " integer not null, "
			+A_KEY_END_TIME + " integer not null, "
			+A_KEY_PRICE + " numeric, "
			+A_KEY_CURRENCY + " text, "
			+A_KEY_JOURNEY_ID + " integer references " + JOURNEY_TABLE_NAME + "("+J_KEY_ID+"), "
			+A_KEY_CATEGORY_ID + " integer references " + CATEGORY_TABLE_NAME + "("+C_KEY_ID+"), "
			+A_KEY_PLACES_ID + " integer references " + PLACES_TABLE_NAME + "("+M_KEY_ID+") "
			+");"
			;
	
	private static final String CREATE_EXPENCES_TABLE = 
			"create table " + EXPENCES_TABLE_NAME 
			+ " (_id integer primary key autoincrement, "
			+ E_KEY_NAME + " text not null, "
			+ E_KEY_DATE + " integer not null, "
			+ E_KEY_VALUE + " numeric not null, "
			+ E_KEY_CURRENCY + " text not null, "
			+ E_KEY_JOURNEY_ID + " integer not null, "
			+ E_KEY_CATEGORY_ID + " integer, "
			+ " foreign key("+E_KEY_JOURNEY_ID+") references " + JOURNEY_TABLE_NAME + "("+J_KEY_ID+"),"
			+ " foreign key("+E_KEY_CATEGORY_ID+") references " + CATEGORY_TABLE_NAME + "("+C_KEY_ID+")"
			+ ");";
	
	private static final String CREATE_CATEGORY_TABLE = 
			"create table " + CATEGORY_TABLE_NAME
			+ "(_id integer primary key autoincrement, "
			+ C_KEY_NAME + " text not null, "
			+ C_KEY_TYPE + " text not null);"
			;
	
	private static final String CREATE_PLACES_TABLE = 
			"create table " + PLACES_TABLE_NAME
			+ "(_id integer primary key autoincrement, "
			+ M_KEY_NAME + " text not null, "
			+ M_KEY_LATITUDE + " integer not null unique, "
			+ M_KEY_LONGITUDE + " integer not null unique, "
			+ M_KEY_ADDRESS + " text, "
			+ M_KEY_WEB + " text, "
			+ M_KEY_PHONE + " text, "
			+ M_KEY_RATING + " integer, "
			+ M_KEY_FAVORITE + " integer, "
			+ M_KEY_CATEGORY_ID + " integer references " + CATEGORY_TABLE_NAME + "("+C_KEY_ID+") "
			+ ");"
			;
	
	private class DatabaseHelper extends SQLiteOpenHelper {
		
		Context mContext;
		
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_PACKLIST_TABLE);
			db.execSQL(CREATE_CATEGORY_TABLE);
			db.execSQL(CREATE_JOURNEY_TABLE);
			db.execSQL(CREATE_EXPENCES_TABLE);
			db.execSQL(CREATE_ACTIVITY_TABLE);
			db.execSQL(CREATE_PLACES_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
			db.execSQL("drop table if exists " + PACKLIST_TABLE_NAME);
			db.execSQL("drop table if exists " + JOURNEY_TABLE_NAME);
			db.execSQL("drop table if exists " + ACTIVITY_TABLE_NAME);
			db.execSQL("drop table if exists " + PLACES_TABLE_NAME);
			db.execSQL("drop table if exists " + EXPENCES_TABLE_NAME);
			db.execSQL("drop table if exists " + CATEGORY_TABLE_NAME);
			onCreate(db);
		}
		
	}
	
	private Context mCtx;
	private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
	
	public DBAdapter(Context ctx) {
    	this.mCtx = ctx;
    }
    
    public DBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
    
    /**
     * Metoda pro inicializaci databáze
     */
    public void initDatabase(){
    	String[] def_list = mCtx.getResources().getStringArray(R.array.array_pack_default_items);
    	for (String it : def_list){
    		ContentValues val = new ContentValues();
    		val.put(P_KEY_NAME, it);
    		val.put(P_KEY_SEL, 0);
    		
    		createPackItem(val);
    	}
    	Log.i(TAG, "Default PackList created");
    	
    	LocalRestoreAsyncTask lrt = new LocalRestoreAsyncTask();
    	lrt.setActivity(mCtx);
    	//mCtx.getAssets().open("initDataJourney.json");
    	lrt.execute(new String[]{null});
    }
    
    /*
     * Metody pro práci s tabulkou PACKLIST
     */
    public long createPackItem(ContentValues values){
    	long pId = mDb.insert(PACKLIST_TABLE_NAME, null, values);
    	return pId;
    } 
    
    public int updatePackItem(ContentValues val, String whereClause){    	
    	return mDb.update(PACKLIST_TABLE_NAME, val, whereClause, null);
    }    
    
    public long deletePackItem(String whereClause){
    	return mDb.delete(PACKLIST_TABLE_NAME, whereClause, null);
    }
    
    public Cursor fetchIdPackItem(String selection){
    	return mDb.query(PACKLIST_TABLE_NAME, new String[] {P_KEY_ID, P_KEY_NAME, P_KEY_SEL}, selection, null, null, null, null);
    }
    
    public Cursor fetchAllPackItems() {
    	return mDb.query(PACKLIST_TABLE_NAME, new String[] {P_KEY_ID, P_KEY_NAME, P_KEY_SEL}, null, null, null, null, null);
    }

    /*
     * Metody pro práci s tabulkou EXPENCES
     */
    public long insertExpence(ContentValues values) {
		return mDb.insert(EXPENCES_TABLE_NAME, null, values);
	}
    
    public long deleteExpence(String whereClause){
    	return mDb.delete(EXPENCES_TABLE_NAME, whereClause, null);
    }
    
    public int updateExpence(ContentValues values, String whereClause) {
    	return mDb.update(EXPENCES_TABLE_NAME, values, whereClause, null);
    }
    
    public Cursor fetchExpence(String selection){
    	return mDb.query(EXPENCES_TABLE_NAME, 
    			new String[]{E_KEY_ID,E_KEY_NAME,E_KEY_DATE,E_KEY_VALUE,E_KEY_CURRENCY, E_KEY_CATEGORY_ID},
    			selection, null, null, null, null);
    }
    
    public Cursor fetchAllExpences(String selection, String[] selectionArgs) { 
    	return mDb.query(EXPENCES_TABLE_NAME, 
				new String[]{E_KEY_ID,E_KEY_NAME,E_KEY_DATE,E_KEY_VALUE,E_KEY_CURRENCY, E_KEY_CATEGORY_ID}, 
				selection, selectionArgs, null, null, null);
	}
    
    public Cursor fetchAllExpenceSUM(String selection){
    	String[] proj = {"sum(value) as sum_value"};
    	return mDb.query(EXPENCES_TABLE_NAME, proj, selection, null, null, null, null);
    }
    
    /*
     * Metody pro práci s tabulkou JOURNEY
     */
    public long insertJourney(ContentValues values) {
    	return mDb.insert(JOURNEY_TABLE_NAME, null, values);
    }
    
    public long deleteJourney(String whereClause){
    	//mazání závislých výdajů
    	mDb.delete(EXPENCES_TABLE_NAME, "journey"+whereClause, null);
    	
    	//mazání závislých aktivit
    	mDb.delete(ACTIVITY_TABLE_NAME, "journey"+whereClause, null);
    	
    	return mDb.delete(JOURNEY_TABLE_NAME, whereClause, null);
    }
    
    public int updateJourney(ContentValues values, String whereClause) {
    	return mDb.update(JOURNEY_TABLE_NAME, values, whereClause, null);
    }
    
    public Cursor fetchAllJourneyes(String selection, String[] selectionArgs) {
    	return mDb.query(JOURNEY_TABLE_NAME, 
    			new String[] {J_KEY_ID, J_KEY_NAME, J_KEY_START_DATE, J_KEY_END_DATE, J_KEY_DESCRIPTION, J_KEY_DRIVEID},
    			selection, selectionArgs, null, null, null);
    }
    
    public Cursor fetchJourney(long j_id) {
    	return mDb.query(JOURNEY_TABLE_NAME, 
    			new String[] {J_KEY_ID, J_KEY_NAME, J_KEY_START_DATE, J_KEY_END_DATE, J_KEY_DESCRIPTION, J_KEY_DRIVEID}, 
    			J_KEY_ID+"="+j_id, null, null, null, null);
    }
     
    
    /*
     * Metody pro práci s tabulkou PLACES
     */
    public long insertPlace(ContentValues values){
    	//return mDb.insert(PLACES_TABLE_NAME, null, values);   	
    	return mDb.replace(PLACES_TABLE_NAME, null, values);
    }
    
    public int updateIdPlace(ContentValues values, String whereClause) {
    	return mDb.update(PLACES_TABLE_NAME, values, whereClause, null);
    }
    
    public Cursor fetchPlace(String selection){
    	return mDb.query(PLACES_TABLE_NAME, new String[] {M_KEY_ID, M_KEY_NAME}, selection, null, null, null, null);
    }
    
    public Cursor fetchPlaceWebId(String selection, String[] selectionArgs){
    	return mDb.query(PLACES_TABLE_NAME, 
    			new String[] {M_KEY_ID, M_KEY_NAME}, 
    			selection, selectionArgs, null, null, null);
    }
    
    
    /*
     * Metody pro práci s tabulkou ACTIVITY
     */
    public long insertActivity(ContentValues values) {
    	return mDb.insert(ACTIVITY_TABLE_NAME, null, values);
    }
    
    public int updateIdActivity(ContentValues values, String whereClause){
    	return mDb.update(ACTIVITY_TABLE_NAME, values, whereClause, null);
    }
    
    public int deleteActivity(String whereClause) {
    	return mDb.delete(ACTIVITY_TABLE_NAME, whereClause, null);
    }
    
    public Cursor fetchAllActivities(String selection) {
    	return mDb.query(ACTIVITY_TABLE_NAME, 
    			new String[] {A_KEY_ID, A_KEY_NAME, A_KEY_START_TIME, A_KEY_END_TIME, 
    			A_KEY_PLACES_ID, A_KEY_JOURNEY_ID, A_KEY_CATEGORY_ID, A_KEY_PRICE, A_KEY_CURRENCY},
    			selection, null, null, null, A_KEY_START_TIME, null);
    }
    
    public Cursor fetchAllActivitiesWithPlace(String selection) {
    	String sql = "SELECT * FROM "+ ACTIVITY_TABLE_NAME+" AS ac INNER JOIN "+ PLACES_TABLE_NAME + " AS pl ON ac."+A_KEY_PLACES_ID+"=pl."+M_KEY_ID+
    			" WHERE "+selection;
    	
    	return mDb.rawQuery(sql, null);
    }

	public Cursor fetchIdActivity(String selection) {
		return mDb.query(ACTIVITY_TABLE_NAME, 
    			new String[] {A_KEY_ID, A_KEY_NAME, A_KEY_START_TIME, A_KEY_END_TIME, 
    			A_KEY_PLACES_ID, A_KEY_JOURNEY_ID, A_KEY_CATEGORY_ID, A_KEY_PRICE, A_KEY_CURRENCY},
    			selection, null, null, null, A_KEY_START_TIME, null);
	}
}
