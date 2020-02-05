package Project.lottoinfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.Serializable;

public class LottoDataBaseManager implements Serializable
{
	static final String LOTTO_DB = "Lotto.db";//DB 이름
	static final String LOTTO_INFO_TABLE = "LottoInfo";//TABLE 이름
	static final int DB_VERSION = 1;

	Context myContext = null;

	private static LottoDataBaseManager lottoDBManager = null;
	private SQLiteDatabase lottoDB;


	//싱글톤 패턴
	public static LottoDataBaseManager getInstance(Context context)
	{
		if(lottoDBManager == null)
		{
			lottoDBManager = new LottoDataBaseManager(context);
		}

		return lottoDBManager;
	}

	private LottoDataBaseManager(Context context)
	//public LottoDataBaseManager(Context context)
	{
		myContext = context;

		//db 오픈
		lottoDB = context.openOrCreateDatabase(LOTTO_DB, context.MODE_PRIVATE, null);

		//먼저 table 있으면 없앰
		//lottoDB.execSQL("DROP TABLE IF EXISTS " + LOTTO_INFO_TABLE);
		//table 생성
		create();
		Log.i("check", "LottoDateBaseManager create");
	}

	private void create()
	{
		lottoDB.execSQL("CREATE TABLE IF NOT EXISTS " + LOTTO_INFO_TABLE +
				"(" +
				"id TEXT PRIMARY KEY," +
				"round INTEGER," +
				"alpha TEXT," +
				"result TEXT," +
				"numbers TEXT," +
				"hit TEXT);");
	}

	public void reset()
	{
		Log.i("check", "LottoDateBaseManager reset");
		lottoDB.execSQL("DROP TABLE IF EXISTS " + LOTTO_INFO_TABLE);
		create();
	}

	public long insert(ContentValues addRowValue)
	{
		Log.i("check", "LottoDateBaseManager insert");
		return lottoDB.insert(LOTTO_INFO_TABLE, null, addRowValue);
	}

	public int update(ContentValues updateRowValue, String whereClause, String[] whereArgs)
	{
		//whereClause : 수정할 레코드 검색 조건
		//whereArags : whereClause 와 관련
		Log.i("check", "LottoDateBaseManager update");
		return lottoDB.update(LOTTO_INFO_TABLE, updateRowValue, whereClause, whereArgs);
	}

	public int delete(String whereClause, String[] whereArgs)
	{
		Log.i("check", "LottoDateBaseManager delete");
		return lottoDB.delete(LOTTO_INFO_TABLE, whereClause, whereArgs);
	}

	public Cursor query(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderby)
	{
		//columns : new String[]{"alpha", "numbers"} 조회하고자 하는 colums
		//selection : 조건 지정
		//groupBy : 같은 값이 들어간 colum 그룹
		//having : groupby 조건
		//orderby : 정렬
		Log.i("check", "LottoDateBaseManager query");
		return lottoDB.query(LOTTO_INFO_TABLE, columns, selection, selectionArgs, groupBy, having, orderby);
	}

	public int get_latestRound()
	{
		Log.i("check", "LottoDateBaseManager get_latestRound");
		String[] columns = new String[]{"round"};
		Cursor c = lottoDBManager.query(columns, null, null, null, null, null);
		int max = 0;
		while(c.moveToNext())
		{
			if(c != null)
			{
				if(max <= c.getInt(0))
				{
					max = c.getInt(0);
				}
			}
		}
		return max;
	}

	public int get_numb()//삭제
	{
		Log.i("check", "LottoDateBaseManager get_latestRound");
		String[] columns = new String[]{"round"};
		Cursor c = lottoDBManager.query(columns, null, null, null, null, null);
		int cnt = 0;
		while(c.moveToNext())
		{
			if(c != null)
			{
				cnt++;
			}
		}
		return cnt;
	}
}
