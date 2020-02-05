package Project.lottoinfo;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyInfoActivity extends AppCompatActivity
{
	final static int RESULT_FAIL = 0;
	final static int RESULT_OK = 1;
	final static int SHOW_DB = 3;

	private RecyclerView mRecyclerView;
	private ArrayList<LottoInfo> mLottoList;
	private LottoAdapter mAdapter;
	private int count = -1;
	Intent intentFromMain;
	LottoDataBaseManager lottoDBManager;

	private Button deleteBtn;

	int cnt = 0;

	private SparseBooleanArray selectedItems = new SparseBooleanArray();

	private ArrayList<String> deleteID;

	TextView tete;
	int t = 0;

	private StringBuffer deleteBuffer;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i("check", "myInfo onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_info_activity);

		tete = findViewById(R.id.tete);

		mRecyclerView = findViewById(R.id.myRecycle);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(linearLayoutManager);

		mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener()
		{
			final GestureDetector gestureDetector = new GestureDetector(MyInfoActivity.this, new GestureDetector.SimpleOnGestureListener()
			{
				@Override
				public boolean onSingleTapUp(MotionEvent e)
				{
					return true;
				}
			});//터치를 하고 손을 땔때만 값을 받으게. 로그값이 너무 많이 나와서

			@Override
			public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e)
			{
				int pos;
				Log.i("check", "myInfo onCreate() onintercepttouchevent");
				View child = rv.findChildViewUnder(e.getX(), e.getY());

				Log.i("check", "myInfo onCreate() child : " + child);
				if(child != null && gestureDetector.onTouchEvent(e))
				{
					pos = mRecyclerView.getChildAdapterPosition(child);
					Log.i("check", "myInfo onCreate() getchildAdapterPosition : " + rv.getChildAdapterPosition(child));
					Log.i("check", "myInfo onCreate() getChildLayoutPosition : " + rv.getChildLayoutPosition(child));
					Log.i("check", "myInfo onCreate() getChildViewHolder : " + rv.getChildViewHolder(child));
					if(selectedItems.get(pos, false))
					{
						Log.i("check", "myInfo onCreate() false");
						selectedItems.put(pos, false);
						rv.getChildViewHolder(child).itemView.setBackgroundResource(R.drawable.recycler_border);
						t--;
					}
					else
					{
						Log.i("check", "myInfo onCreate() true");
						selectedItems.put(pos, true);
						rv.getChildViewHolder(child).itemView.setBackgroundResource(R.drawable.recycler_focus_border);
						t++;
					}
				}
				tete.setText(t+"");
				return false;
			}

			@Override
			public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}

			@Override
			public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
		});

		mLottoList = new ArrayList<>();

		mAdapter = new LottoAdapter(mLottoList);
		mRecyclerView.setAdapter(mAdapter);

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), linearLayoutManager.getOrientation());
		mRecyclerView.addItemDecoration(dividerItemDecoration);

		//main에서 보낸 intent에서 db 가져와
		intentFromMain = getIntent();//삭제 혹은 수정
		//lottoDBManager = (LottoDataBaseManager) intentFromMain.getSerializableExtra("db");
		lottoDBManager = LottoDataBaseManager.getInstance(this);
		if(lottoDBManager != null) {
			Toast.makeText(getApplicationContext(), "디비 만들어짐", Toast.LENGTH_SHORT).show();
		}

		StringBuffer sb = new StringBuffer();
		String[] columns = new String[] {"round"};
		Cursor cursor = lottoDBManager.query(columns, null, null, null, null, null);
		while(cursor.moveToNext()) {
			if(cursor != null) {
				sb.append("findDB ").append(cursor.getInt(0) + " ");
			}
		}
		Toast.makeText(getApplicationContext(), sb.toString(), Toast.LENGTH_SHORT).show();
		showDB();

		deleteBtn = findViewById(R.id.deleteBtn);

		tete.setText(lottoDBManager.get_numb()+"");
	}

	private void showDB()
	{
		Log.i("check", "myInfo showDB()");
		String [] columns = new String[]{"id, round, alpha, result, numbers, hit"};
		Cursor cursor = lottoDBManager.query(columns, null, null, null, null, null);
		while(cursor.moveToNext())
		{
			if(cursor != null)
			{
				LottoInfo tmpLotto = new LottoInfo();
				tmpLotto.set_id(cursor.getString(0));
				tmpLotto.set_round(cursor.getInt(1));
				tmpLotto.set_alpha(cursor.getString(2));
				tmpLotto.set_result(cursor.getString(3));
				tmpLotto.set_numbers(cursor.getString(4));
				tmpLotto.set_hit(cursor.getString(5));
				mLottoList.add(tmpLotto);
				mAdapter.notifyDataSetChanged();
			}
		}

	}

	private void deleteItem()
	{
		deleteID = new ArrayList<>();//삭제
		deleteBuffer = new StringBuffer();//삭제
		int itemCnt = mAdapter.getItemCount();
		//db에서 삭제할거야
		for(int i = 0 ; i < itemCnt ; i++)
		{
			if(selectedItems.get(i) == true)
			{
				deleteDB(mLottoList.get(i).get_id());
				mLottoList.remove(i);
				mAdapter.notifyItemRemoved(i);
				mAdapter.notifyItemRangeChanged(i, mLottoList.size());
				selectedItems.delete(i);
			}
		}
		t = 0;
		mAdapter.notifyDataSetChanged();//onBindViewHolder 실행
	}

	private void deleteDB(String id)
	{
		Log.i("check", "delete : " + id);
		Log.i("check", "before db : " + lottoDBManager.get_numb());//삭제
		lottoDBManager.delete("id=?", new String[]{id});
		Log.i("check", "after db : " + lottoDBManager.get_numb());//삭제
	}

	private void resetDB()
	{
		lottoDBManager.reset();
		mLottoList.clear();
		mAdapter.notifyDataSetChanged();
	}

	public void clickHandler(View v)
	{
		switch(v.getId())
		{
			case R.id.returnBtn:
				setResult(RESULT_OK);
				finish();
				break;
			case R.id.deleteBtn:
				deleteItem();
				break;
			case R.id.resetBtn:
				resetDB();
				break;
		}
	}
}
