package Project.lottoinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class scanQR extends AppCompatActivity
{
	final static int SCAN_QR = 7777;
	final static int RESULT_OK = 1;
	final static int RESULT_FAIL = 0;

	private IntentIntegrator scanQR;
	private IntentResult result;

	private WebView webView;
	private WebSettings webSettings;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i("check", "scanQR onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scanqr_activity);

		webView = findViewById(R.id.webView);
		webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setUseWideViewPort(true);

		if(Build.VERSION.SDK_INT >= 16)
		{
			webSettings.setAllowFileAccessFromFileURLs(true);
			webSettings.setAllowUniversalAccessFromFileURLs(true);
		}
		if(Build.VERSION.SDK_INT >= 21) {
			webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
		//webView.addJavascriptInterface();

		scanQR = new IntentIntegrator(this);
		scanQR.setOrientationLocked(false);
		scanQR.setBeepEnabled(false);
		scanQR.initiateScan();
		scanQR.setPrompt("QR코드를 스캔하세요");
	}

	@Override
	public void onPause()
	{
		Log.i("check", "scanQR onPause()");
		super.onPause();
	}

	@Override
	public void onResume()
	{
		Log.i("check", "scanQR onResume()");
		super.onResume();
	}

	@Override
	public void onStop()
	{
		Log.i("check", "scanQR onStop()");
		super.onStop();
	}

	@Override
	public void onDestroy()
	{
		Log.i("check", "scanQR onDestroy()");
		super.onDestroy();
	}

	//결과값 받아와 처리하는 함수
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.i("check", "scanQR onActivityResult()");
		Intent intent = new Intent();
		result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if(result != null)
		{
			if(result.getContents() == null)
			{
				intent.putExtra("resultMsg", "no result");
				setResult(RESULT_FAIL, intent);
			}
			else
			{
				intent.putExtra("resultMsg", result.getContents());
				setResult(RESULT_OK, intent);
			}
		}
		else
		{
			super.onActivityResult(requestCode, resultCode, data);
		}
		finish();
	}

}
