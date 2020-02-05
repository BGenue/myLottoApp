package Project.lottoinfo;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MyLottoList extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i("check", "MyLottoList onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
}
