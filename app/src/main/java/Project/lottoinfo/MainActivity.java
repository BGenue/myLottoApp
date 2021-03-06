package Project.lottoinfo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity
{
	//키보드 오르내림
	private InputMethodManager keyboard;

	//startActivityForResult 인수
	final static int SCAN_QR = 7777;
	final static int SHOW_QR = 8888;
	final static int THIS_IS_DB = 9999;
	final static int RESULT_FAIL = 0;
	final static int RESULT_OK = 1;
	final static int INSERT_DB = 2;
	final static int SHOW_DB = 3;
	final static int SHOW_MAP = 6666;
	private final static int LAST_STATE_SAVE = 4563;

	//MapActivity에서 저장할 요소들 관련
	//마지막 권한 설정 상태 와 마지막 위치 저장해둬
	private final static String KEY_PERMISSION = "permission";
	private final static String KEY_LOCATION = "location";
	private boolean permission_granted;
	private Location currentLocation;

	//스캔 결과
	private IntentResult result;

	//권한 설정
	private final int CAMERA_PERMISSION = 1001;
	int permissionCheck;

	final String basicUrl = "https://www.nlotto.co.kr/common.do?method=getLottoNumber&drwNo=";
	String lottoUri;

	//activity_main
	TextView dateText;
	TextView latestRoundText;

	String num1, num2, num3, num4, num5, num6, num7, latestDate;
	TextView num1Text,num2Text, num3Text, num4Text, num5Text, num6Text, num7Text;
	TextView showDBtext;
	EditText searchText;
	ImageView gps;

	/////정리 뷰
	ImageView top_list_Image;
	ImageView top_qr_Image;
	ConstraintLayout menu_qr;
	ConstraintLayout menu_list;

	Handler mHandler = null;

	private LottoDataBaseManager lottoDBManager;

	private Intent intentToQR;
	private Intent intentToMap;

	private int latestRound;

	private AdView adView;

	private int myLatestRound;


	private RecyclerView mRecyclerView;
	private LottoAdapter mmAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i("check", "main onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//updateValuesFromBundle(savedInstanceState);

		//광고
		adView = findViewById(R.id.adView);
		MobileAds.initialize(this, new OnInitializationCompleteListener()
		{
			@Override
			public void onInitializationComplete(InitializationStatus initializationStatus)
			{

			}
		});
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);

		//db
		lottoDBManager = LottoDataBaseManager.getInstance(this);

		initId();

		getLatestLotto();//최신 로또 답 가져와 -> 내 디비에서 최근 로또 확인 -> 업데이트 필요하면 하고 아니면 그냥 보여줘

		////삭제할 부분
		//지금 저장 되어 있는 db 체크
		showMyDB();

		getAppKeyHash();
	}

	//뭐지 이건
	private void getAppKeyHash() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md;
				md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String something = new String(Base64.encode(md.digest(), 0));
				Log.i("check", "hash " + something);
			}
		} catch (Exception e) {
			Log.e("name not found", e.toString());
		}
	}

	private void showMyDB()
	{
		ContentValues updateRowValue = new ContentValues();
		String [] columns = new String[]{"id, round, alpha, result, numbers, hit"};
		LottoInfo show = new LottoInfo();
		Cursor cursor = lottoDBManager.query(columns, null, null, null, null, null);
		Log.i("check", "main onCreate() showMyDB() before change");
		while(cursor.moveToNext())
		{
			if(cursor != null)
			{
				Log.i("check", "main onCreate() showMyDB() id : " + cursor.getString(0));
				Log.i("check", "main onCreate() showMyDB() round : " + cursor.getInt(1));
				Log.i("check", "main onCreate() showMyDB() alpha : " + cursor.getString(2));
				Log.i("check", "main onCreate() showMyDB() result : " + cursor.getString(3));
				Log.i("check", "main onCreate() showMyDB() numbers : " + cursor.getString(4));
				Log.i("check", "main onCreate() showMyDB() hit : " + cursor.getString(5));
			}
		}
	}

	private void initId()
	{
		Log.i("check", "main initId()");
		keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		mHandler = new Handler();

		num1Text = findViewById(R.id.num1Text);
		num2Text = findViewById(R.id.num2Text);
		num3Text = findViewById(R.id.num3Text);
		num4Text = findViewById(R.id.num4Text);
		num5Text = findViewById(R.id.num5Text);
		num6Text = findViewById(R.id.num6Text);
		num7Text = findViewById(R.id.num7Text);

		latestRoundText = findViewById(R.id.latestRoundText);
		dateText = findViewById(R.id.dateText);

		intentToQR = new Intent(this, ShowQRResult.class);

		gps = findViewById(R.id.gpsImage);
		top_qr_Image = findViewById(R.id.top_qr_Image);
		top_list_Image = findViewById(R.id.top_list_Image);
		menu_qr = findViewById(R.id.menu_qr);
		menu_list = findViewById(R.id.menu_list);
		gps.setOnTouchListener(mTouchEvent);
		top_qr_Image.setOnTouchListener(mTouchEvent);
		top_list_Image.setOnTouchListener(mTouchEvent);
	}

	///수정해야할듯
	private View.OnTouchListener mTouchEvent = new View.OnTouchListener()
	{
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			ImageView image = (ImageView)v;
			if(event.getAction() == MotionEvent.ACTION_DOWN)
			{
				image.setColorFilter(0xffa0a0a0, PorterDuff.Mode.DST_OVER);
			}
			else if(event.getAction() == MotionEvent.ACTION_UP)
			{
				image.setColorFilter(null);
			}
			return false;
		}
	};

	@Override
	public void onPause()
	{
		Log.i("check", "main onPause()");
		super.onPause();
	}

	@Override
	public void onResume()
	{
		Log.i("check", "main onResume()");
		super.onResume();
		//checkMyDB();
		myLatestRound = lottoDBManager.get_latestRound();
		showMyLatestLotto();
		Cursor arr = lottoDBManager.descendDB();
		//round, id, alpha, result, numbers, hit
		while(arr.moveToNext())
		{
			if(arr!=null)
			{
				Log.i("check", "main onResume() acsend " + arr.getInt(0));
			}
		}
	}

	@Override
	public void onRestart()
	{
		Log.i("check", "main onRestart()");
		super.onRestart();
		getLatestLotto();
	}

	@Override
	public void onStop()
	{
		Log.i("check", "main onStop()");
		super.onStop();
	}

	@Override
	public void onDestroy()
	{
		Log.i("check", "main onDestroy()");
		super.onDestroy();
	}

	//권한 요청 응답처리
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		Log.i("check", "main onRequestPermissionsResult()");
		switch(requestCode)
		{
			case CAMERA_PERMISSION:
					if(grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
					{
					Toast.makeText(this, "승인이 허가됨", Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(this, "아직 승인 안됨", Toast.LENGTH_SHORT).show();
				}
		}
	}

	public void myCheckPermission()
	{
		Log.i("check", "main myCheckPermission()");
		//앱의 권한 확인
		permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
		if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
			//카메라 사용 권한 없음
			Toast.makeText(this, "권한 승인 필요", Toast.LENGTH_SHORT).show();
			//사용자가 권한 거절을 한 경우 또는 처음하는 경우

			if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
				//이전에 거절을 한 적이 있으면 true 반환
				//권한 요청. 요청 결과는 onRequestPermissionResult에서 수신. 팝업으로 사진 촬열할지 물어봄
				ActivityCompat.requestPermissions( MainActivity.this,  new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
			}
			else {
				//이전에 거절을 한 적이 있고 다시 묻지 않음 옵션을 선택 / 기기 자체 금지 - false 반환
				AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
				localBuilder.setTitle("권한 설정")
						.setMessage("권한 거절로 인해 카메라 기능이 제한됩니다.")
						.setPositiveButton("권한 설정하러 가기", new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt){
								try {
									Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
											.setData(Uri.parse("package:" + getPackageName()));
									startActivity(intent);
								} catch (ActivityNotFoundException e) {
									e.printStackTrace();
									Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
									startActivity(intent);
								}
							}})
						.setNegativeButton("취소하기", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
								Toast.makeText(getApplication(),"취소",Toast.LENGTH_SHORT).show();
							}})
						.create()
						.show();
			}
		}
		else
		{
			//카메라 사용 권한 있음
			Toast.makeText(this, "권한 승인", Toast.LENGTH_SHORT).show();
		}
	}

	//click과 관련된 handler
	public void clickHandler(View v)
	{
		Log.i("check", "main clickHandler()");
		switch(v.getId())
		{
			case R.id.top_list_Image:
			case R.id.menu_list:
				showClick();
				break;
			case R.id.top_qr_Image:
			case R.id.menu_qr:
				qrBtnClick();
				break;
			case R.id.gpsImage://수정해야함. map 보여주는 걸로
				this.intentToMap = new Intent(MainActivity.this, MapActivity.class);
				/*
				if(this.currentLocation != null)
				{
					Log.i("check", "MainAcitivty() intenttomain " + this.currentLocation.getLatitude() + " " + this.currentLocation.getLongitude());
				}
				else
				{
					Log.i("check", "MainAcitivty() intenttomain null");
				}
				this.intentToMap.putExtra("lastLocation", this.currentLocation);
				*/
				//intent start 바꿔
				startActivityForResult(this.intentToMap, LAST_STATE_SAVE);//intent 보내
				break;
			case R.id.latestRoundText:
				showMyLatestLotto();
				textClick();
				break;
		}
	}

	private void showClick()
	{
		//수정해야할 부분있는거 같음
		Intent intentToMyInfo = new Intent(MainActivity.this, MyInfoActivity.class);
		intentToMyInfo.putExtra("db", lottoDBManager);
		Intent i = new Intent(MainActivity.this, MyInfoActivity.class);
		startActivityForResult(i, THIS_IS_DB);//intent 보내
	}

	//qr코드 버튼 눌림
	private void qrBtnClick()
	{
		Log.i("check", "main qrBtnClick()");
		myCheckPermission();
		Intent intent = new Intent(MainActivity.this, scanQR.class);
		startActivityForResult(intent, SCAN_QR);//intent 보내
	}

	public void textClick()
	{
		Log.i("check", "main textClick()");
		getLatestLotto();
	}

	//회차에 따른 번호 가져와
	//메인 화면에 제일 윗 부분
	//혼자 작동하면 됨
	private void getNumber(final String numberUrl)
	{
		Log.i("check", "main getNumber()");
		new Thread(new Runnable ()
		{
			String json = null;
			String line = null;

			@Override
			public void run()
			{
				Looper.prepare();
				try{
					URL url = new URL(numberUrl);
					HttpURLConnection urlConnection= (HttpURLConnection)url.openConnection();
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
					StringBuffer page = new StringBuffer();
					while((line = bufferedReader.readLine()) != null)
					{
						page.append(line);
					}
					bufferedReader.close();
					json = page.toString();
					//Toast.makeText(getApplicationContext(), json, Toast.LENGTH_SHORT).show();

					JSONObject jsonObject = new JSONObject(json);
					if(jsonObject.getString("returnValue").equals("fail"))
					{
						num1 = "x";
						num2 = "x";
						num3 = "x";
						num4 = "x";
						num5 = "x";
						num6 = "x";
						num7 = "x";
						latestDate = "x";
					}
					else {
						num1 = jsonObject.getString("drwtNo1");
						num2 = jsonObject.getString("drwtNo2");
						num3 = jsonObject.getString("drwtNo3");
						num4 = jsonObject.getString("drwtNo4");
						num5 = jsonObject.getString("drwtNo5");
						num6 = jsonObject.getString("drwtNo6");
						num7 = jsonObject.getString("bnusNo");
						latestDate = jsonObject.getString("drwNoDate");
					}

					mHandler.post(new Runnable()
					{
						@Override
						public void run()
						{
							num1Text.setText(num1);
							num2Text.setText(num2);
							num3Text.setText(num3);
							num4Text.setText(num4);
							num5Text.setText(num5);
							num6Text.setText(num6);
							num7Text.setText(num7);
							dateText.setText(latestDate);
						}
					});
				}catch(Exception e){
					Log.i("check", "main getNumber() catch : " + e.toString());
				}
				Looper.loop();
			}
		}).start();
	}

	//가장 최신 회차를 찾아
	private void getLatestLotto()
	{
		Log.i("check", "main getLatestNumber()");
		new Thread(new Runnable()
		{
			long calDateDays;//계산된 횟수. 회차

			@Override
			public void run()
			{
				Looper.prepare();
				try {
					Date presentDate = new Date(System.currentTimeMillis());//현재 시각
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					Date today = dateFormat.parse(dateFormat.format(presentDate));
					Date start = dateFormat.parse("2002-12-07");

					long calDate = today.getTime() - start.getTime();//오늘날 - 시작날
					calDateDays = calDate/(24*60*60*1000)/7 + 1;//주단위로 바꿈

					dateFormat = new SimpleDateFormat("EE");
					String weekDay = dateFormat.format(presentDate);

					SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
					Date presentTime = timeFormat.parse(timeFormat.format(presentDate));
					Date lottoTime = timeFormat.parse("20:45:00");
					long calTime = lottoTime.getTime() - presentTime.getTime();//로또 시간 - 현재 시간

					calDateDays = Math.abs(calDateDays);
					//토요일인 경우 회차가 올라가기 때문에 시간까지 따져야함
					if(calTime > 0 && weekDay.equals("토"))
					{
						calDateDays -= 1;
					}
					getNumber(basicUrl+calDateDays);//로또 당첨 번호 가져와. thread 이용.
					latestRound = (int)calDateDays;
					//추가할 기능
					//내가 가진건 896회인데 시간이 지나서 896회의 결과가 나옴
					//결과를 자동으로 받아와 내 db에 있는 녀석을 업데이트해줘
					checkMyDB();////////////////////////////////////////////////////////////////
				}catch(ParseException e){
					Log.i("check", "main getLatestLotto catch : " + e.toString());
				}
				//ui 변경
				mHandler.post(new Runnable()
				{
					@Override
					public void run()
					{
						latestRoundText.setText(calDateDays+"");
					}
				});
				Looper.loop();
			}
		}).start();

	}

	//결과 반영해야하는 경우
	private void checkMyDB()
	{
		Log.i("check", "main checkMyDB()");
		String [] columns = new String[]{"id, round, alpha, result, numbers, hit, qORm"};
		LottoInfo tmp = new LottoInfo();
		Cursor cursor = lottoDBManager.query(columns, "round=" + latestRound, null, null, null, null);
		//최근 로또를 샀을때
		JsoupParse[] jp = new JsoupParse[cursor.getCount()];
		int i = 0;
		while(cursor.moveToNext())
		{
			Log.i("check", "checkMyDB() id : " + cursor.getString(0));
			if(cursor!=null) {
				//산 로또는 있지만 결과가 업데이트 안되어있음
				if(cursor.getString(3).substring(0, 2).equals("없음")) {
					tmp.set_id(cursor.getString(0));
					tmp.set_round(cursor.getInt(1));
					tmp.set_alpha(cursor.getString(2));
					tmp.set_result(cursor.getString(3));
					tmp.set_numbers(cursor.getString(4));
					tmp.set_hit(cursor.getString(5));
					tmp.set_qORm(cursor.getString(6));
					jp[i] = new JsoupParse(tmp);
					jp[i].execute();
				}
			}
		}
	}

	private void showMyLatestLotto()
	{
		Log.i("check", "main showMyLatestLotto()");
		String [] columns = new String[]{"id, round, alpha, result, numbers, hit, qORm"};
		Cursor cursor = lottoDBManager.query(columns, "round=" + myLatestRound, null, null, null, null);
		mRecyclerView = findViewById(R.id.testLayout);
		mRecyclerView.removeAllViews();
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(linearLayoutManager);

		ArrayList<LottoInfo> mmmLottoList = new ArrayList<>();

		mmAdapter = new LottoAdapter(mmmLottoList);
		mRecyclerView.setAdapter(mmAdapter);
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), linearLayoutManager.getOrientation());
		mRecyclerView.addItemDecoration(dividerItemDecoration);
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
				tmpLotto.set_qORm(cursor.getString(6));
				mmmLottoList.add(tmpLotto);
				mmAdapter.notifyDataSetChanged();
			}
		}
	}

	//다른 액티비티 부르고 난 후 결과
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.i("check", "main onActivityResult()");
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_FAIL)
		{
			//다른 액티비티 부른 결과가 fail 이면 실행
			Toast.makeText(getApplicationContext(), "결과 :" + lottoUri, Toast.LENGTH_SHORT).show();
		}
		else
			{
			switch(requestCode) {
				case SCAN_QR:
					//scanQR 갔다왔으면 ShowQRResult 갔다와
					//qr 코드 읽어서 연결되는 사이트 주소 가져옴
					lottoUri = data.getStringExtra("resultMsg");
					if(!lottoUri.equals("no result"))
					{
						Toast.makeText(getApplicationContext(), "결과 : " + lottoUri, Toast.LENGTH_SHORT).show();
						lottoUri = lottoUri.replace("http", "https");
						lottoUri = lottoUri.replace("?v", "qr.do?method=winQr&v");
						JsoupParse jp = new JsoupParse(lottoUri);
						jp.execute();
					}
					break;
				case SHOW_QR:
					//ShowQRResult 갔다옴
					if(resultCode == INSERT_DB)
					{
						//디비에 저장해
						LottoInfo tmp = (LottoInfo)data.getSerializableExtra("lotto");
						insertDB(tmp);
						//getLottoData();
						showDB();//삭제???
					}
					break;
				case SHOW_DB:
					//MyInfoActivity 갔다옴
					//수정??? 삭제???
					break;
				case LAST_STATE_SAVE:
					//MapActivity 갖다옴
					Log.i("check", "MapActivity 갔다옴");
					/*
					if(data.hasExtra("lastLocation"))
					{
						this.currentLocation = data.getParcelableExtra("lastLocation");
						Log.i("check", "MapActivity 갔다옴 " + this.currentLocation.getLongitude() + " " + this.currentLocation.getLatitude());
					}
					else
					{
						Log.i("check", "MapActivity 갔다옴 location 없엉");
					}*/
					break;

			}
		}
	}

	//데이터베이스 관련 함수. DB Function. DB에 데이터 넣음
	public void insertDB(LottoInfo insertLotto)//int round, String alpha, String result, String numbers
	{
		Log.i("check", "main insertDB()");
		ContentValues addRowValue = new ContentValues();

		addRowValue.put("id", insertLotto.get_id());
		Log.i("check", "main insertDB() id : " + insertLotto.get_id());
		addRowValue.put("round", insertLotto.get_round());
		addRowValue.put("alpha", insertLotto.get_sum_alpha());
		addRowValue.put("result", insertLotto.get_sum_result());
		addRowValue.put("hit", insertLotto.get_sum_hit());
		addRowValue.put("numbers", insertLotto.get_sum_numbers());
		addRowValue.put("qORm", insertLotto.get_qORm());

		lottoDBManager.insert(addRowValue);

		//myLatestRound = insertLotto.get_round();//삭제
	}

	//데이터베이스 관련 함수. DB. 업데이트
	//그 주의 로또 당첨번호가 나왔을 때 만약 내가 저장해둔 번호가 있으면 자동으로 업데이트해줘
	private void updateDB(LottoInfo updateLotto)
	{
		Log.i("check", "main updateDB()");
		ContentValues updateRowValue = new ContentValues();
		updateRowValue.put("id", updateLotto.get_id());
		updateRowValue.put("round", updateLotto.get_round());
		updateRowValue.put("alpha", updateLotto.get_sum_alpha());
		updateRowValue.put("result", updateLotto.get_sum_result());
		updateRowValue.put("hit", updateLotto.get_sum_hit());
		updateRowValue.put("numbers", updateLotto.get_sum_numbers());
		updateRowValue.put("qORm", updateLotto.get_qORm());
		lottoDBManager.update(updateRowValue, "id=?", new String[]{updateLotto.get_id()});
	}

	//데이터베이스 관련 함수. DB. 검색 // 삭제
	private void searchDB(int round)
	{

		Log.i("check", "main findDB()");
		StringBuffer sb = new StringBuffer();
		String [] columns = new String[]{"round", "alpha", "result", "numbers"};
		Cursor cursor = lottoDBManager.query(columns, "round=" + round, null, null, null, null);
		while(cursor.moveToNext())
		{
			if(cursor != null)
			{
				LottoInfo tmp = new LottoInfo();
				tmp.set_round(cursor.getInt(0));
				tmp.set_alpha(cursor.getString(1));
				tmp.set_result(cursor.getString(2));
				tmp.set_numbers(cursor.getString(3));
				sb.append("findDB ").append(tmp.get_round() + " ").append(tmp.get_sum_alpha() + " ").append(tmp.get_sum_result() + " ").append(tmp.get_sum_numbers() + "\n");//삭제
				//변경해야함
				//search 한거 myLotto로 추가
			}
			else
			{
				sb.append(round+"없음");
			}
		}
		showDBtext.setText(sb);
		keyboard.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
	}

	//DB에 있는 자료들 찾아줘
	private void showDB()//삭제
	{
		Log.i("check", "main showDB()");
		StringBuffer sb = new StringBuffer();
		String [] columns = new String[]{"round", "alpha", "result", "numbers"};
		Cursor cursor = lottoDBManager.query(columns, null, null, null, null, null);
		//db 내용 뒤져볼래
		int i = 0;
		final int a = 0;
		while(cursor.moveToNext()) {
			//디비에 내용이 있으면
			if(cursor != null) {
				//디비 내용으로 할것
				LottoInfo tmp = new LottoInfo();
				tmp.set_round(cursor.getInt(0));
				tmp.set_alpha(cursor.getString(1));
				tmp.set_result(cursor.getString(2));
				tmp.set_numbers(cursor.getString(3));
				sb.append(tmp.get_round() + " ").append(tmp.get_sum_alpha() + " ").append(tmp.get_sum_result() + " ").append(tmp.get_sum_numbers() + "\n");
				i++;
			}
		}
	}

	//jsoup 사용해서 html 파싱하기
	//내가 산 로또 정보 받아와!
	private class JsoupParse extends AsyncTask<Void, Void, Void>
	{
		final static int INSERT_DB = 22;
		final static int UPDATE_DB = 33;

		private StringBuffer sbID = new StringBuffer();
		private StringBuffer sbAlpha = new StringBuffer();
		private StringBuffer sbResult = new StringBuffer();
		private StringBuffer sbNum = new StringBuffer();
		private StringBuffer sbHit = new StringBuffer();
		private StringBuffer sbQorM = new StringBuffer();

		private int tmpRound;
		private int mode = 0;

		//로또 번호 저장
		private LottoInfo myLotto;

		private String findUri;
		private String baseUri = "https://m.dhlottery.co.kr/qr.do?method=winQr&v=";

		//기본 jsoup
		public JsoupParse(String u)
		{
			Log.i("check", "JsoupParse String");
			this.mode = 0;
			this.findUri = u;
			Log.i("check", "onPostExecute uri :" + u);
			sbID.append(this.findUri.substring(this.findUri.length()-10));
			Log.i("check", "onPostExecute id :" + sbID.toString());
		}

		//내가 원하는 round를 검색하고 싶을때
		public JsoupParse(LottoInfo lotto)
		{
			Log.i("check", "JsoupParse LottoInfo");
			this.mode = 1;
			this.myLotto = lotto;
			Log.i("check", "JsoupParse LottoInfo round : " + this.myLotto.get_round());
			Log.i("check", "JsoupParse LottoInfo numbers : " + this.myLotto.get_sum_numbers());
			Log.i("check", "JsoupParse LottoInfo id : " + this.myLotto.get_id());
			this.findUri = baseUri + "0" + this.myLotto.get_round() + "q" + this.myLotto.get_sum_numbers() + this.myLotto.get_id();
			sbID.append(lotto.get_id());
		}

		//doinbackground 끝나고 할 메인 쓰레드 작업
		@Override
		protected void onPostExecute(Void result)
		{
			Log.i("check", "onPostExecute");
			myLotto = new LottoInfo();
			myLotto.set_id(sbID.toString());
			myLotto.set_round(tmpRound);//892
			myLotto.set_alpha(sbAlpha.toString());//AnBnCnDnE
			myLotto.set_result(sbResult.toString());//낙첨n낙첨n낙첨n낙첨n낙첨
			myLotto.set_numbers(sbNum.toString());//12341234134q12412341234q1241241234q1241241234q123412341234q1234123434
			myLotto.set_hit(sbHit.toString());//1,2,1n3,4,1
			myLotto.set_qORm(sbQorM.toString());

			if(this.mode == 0)
			{
				intentToQR.putExtra("lotto", myLotto);
				startActivityForResult(intentToQR, SHOW_QR);
			}
			else
			{
				//db 업데이트 해야함
				updateDB(myLotto);
			}
		}

		//내 한줄씩 정보 받아서 저장
		@Override
		protected Void doInBackground(Void... voids)
		{
			int r = 0;//열
			int c = 0;//행
			//table[c][r]
			Log.i("check", "doInBackground");
			//새로운 쓰레드 생성
			try {
				Document doc = Jsoup.connect(this.findUri).get();
				Log.i("check", "doInBackground find : " + this.findUri);
				//몇회인지 받아와
				Elements roundE = doc.select("div[class=winner_number]").select("span[class=key_clr1]");
				for(Element e : roundE)
				{
					if(e.text() != null)
					{
						String tmp = e.text().substring(0, e.text().length()-1);
						tmp = tmp.substring(1);
						tmpRound = Integer.parseInt(tmp);
					}
				}

				//로또 번호 테이블을 한줄씩 읽어들임
				Elements lottoRow = doc.select("div[class=tbl_basic]").select("table").select("tbody").select("tr");
				for(Element row : lottoRow)
				{
					int a = 1;//
					//줄마다 정보 저장
					//줄 번호
					String tmpAlpha = row.select("th[scope=row]").text();//변경
					//결과
					String tmpResult = row.select("td[class=result]").text();
					if(tmpResult.isEmpty())
					{
						tmpResult = "없음";//삭제 가능??
					}
					Elements nums = row.select("td").select("span");
					//숫자
					r = 0;
					for(Element num : nums)
					{
						//숫자가 한자리일때 앞에 0을 추가해주자.
						if(num.text().length() == 1)
						{
							sbNum.append("0").append(num.text());
						}
						else {
							sbNum.append(num.text());
						}
						Log.i("check", "doInBackground class name " + num.className());
						if(num.className().contains("clr1")||num.className().contains("clr2")||num.className().contains("clr3")||num.className().contains("clr4")||num.className().contains("clr5"))
						{
							//맞춘 자리 체크
							if(num.className().contains("plus"))
							{
								sbHit.append(c+","+r+","+7).append("q");
							}
							else
							{
								sbHit.append(c+","+r+","+a).append("q");
							}
						}
						a++;
						r++;
					}
					sbAlpha.append(tmpAlpha).append("q");
					sbResult.append(tmpResult).append("q");
					sbNum.append("q");
					c++;
				}
			}catch(IOException e){
				Log.i("check", "doInBackground catch : " + e.toString());
			}
			//맨 마지막 q 삭제
			sbAlpha.deleteCharAt(sbAlpha.length()-1);
			sbResult.deleteCharAt(sbResult.length()-1);
			sbNum.deleteCharAt(sbNum.length()-1);

			//onPostExecute 하기 전에 제대로 다 받았나 로그 찍어봐
			Log.i("check", "doInBackground sbAlpha :" + sbAlpha.toString());
			Log.i("check", "doInBackground sbResult :" + sbResult.toString());
			Log.i("check", "doInBackground sbNum :" + sbNum.toString());
			Log.i("check", "doInBackground sbHit : " + sbHit.toString());

			//주소에서 q, m  으로 자동 수동 구분함
			String test = this.findUri.substring("https://m.dhlottery.co.kr/qr.do?method=winQr&v=".length());
			int b = 0;
			int d = 0;
			while(d < 5)
			{
				if(test.charAt(b) == 'm' || test.charAt(b) == 'q' || test.charAt(b) == 'n')
				{
					sbQorM.append(test.charAt(b));
					d++;
				}
				b++;
			}
			Log.i("check", "doInBackground qORm : " + sbQorM.toString());
			return null;
		}
	}

	/*
	private void updateValuesFromBundle(Bundle savedInstanceState)
	{
		Log.i("check", "MainActivity updateValuesFromBundle()");

		if(savedInstanceState != null)
		{
			if(savedInstanceState.keySet().contains(KEY_LOCATION))
			{
				this.currentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
				if(this.currentLocation == null)
				{
					Log.i("check", "MainActivity updateValuesFromBundle() has location but null");
				}
				else
				{
					Log.i("check", "MainActivity updateValuesFromBundle() has location");
				}
			}
			else
			{
				Log.i("check", "MainActivity updateValuesFromBundle() no location");
				this.currentLocation = null;
			}
		}
		else
		{
			Log.i("check", "MainActivity updateValuesFromBundle() null");
		}
	}
	*/

	/*
	@Override
	public void onSaveInstanceState(Bundle outState)
	{//저장해야함
		if(this.currentLocation != null)
		{
			Log.i("check", "MainActivity onSaveInstanceState() " + this.currentLocation.getLatitude() + " " + this.currentLocation.getLongitude());
			outState.putParcelable(KEY_LOCATION, this.currentLocation);
			super.onSaveInstanceState(outState);
		}
	}
	*/
}