package Project.lottoinfo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ShowQRResult extends AppCompatActivity
{
	final static int RESULT_FAIL = 0;
	final static int RESULT_OK = 1;
	final static int INSERT_DB = 2;

	private Intent intentFromMain;
	private Intent intentToMain;

	private TextView roundText;
	private TextView[] text = new TextView[5];

	private LottoInfo myLotto;
	private LayoutInflater mInflater;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i("check", "showQRResult onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_qr_result_activity);

		intentFromMain = getIntent();//내가 산 로또 정보 intent로 받아
		intentToMain = new Intent();

		text[0] = findViewById(R.id.aText);
		text[1] = findViewById(R.id.bText);
		text[2] = findViewById(R.id.cText);
		text[3] = findViewById(R.id.dText);
		text[4] = findViewById(R.id.eText);
		roundText = findViewById(R.id.roundText);
		intentToMain.putExtra("check", intentFromMain.getSerializableExtra("check"));

		showLotto();
	}

	//내가 산 로또 보여줌
	private void showLotto()
	{
		myLotto = (LottoInfo)intentFromMain.getSerializableExtra("lotto");
		if(myLotto == null)
		{
			intentToMain.putExtra("resultMsg", "no result");
			setResult(RESULT_FAIL, intentToMain);
			finish();
		}
		else
		{
			mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			ConstraintLayout container = findViewById(R.id.show_qr_result_layout);
			container.removeAllViews();
			mInflater.inflate(R.layout.my_lotto_item, container, true);

			TextView setText = container.findViewById(R.id.my_lotto_round);
			setText.setText(myLotto.get_round() + "");
			LinearLayout rowLayout;
			for(int i = 0 ; i < myLotto.get_len() ; i++)
			{
				//layout
				rowLayout = container.findViewById(R.id.my_lotto_rowA + i*15);
				rowLayout.setVisibility(View.VISIBLE);
				//row
				setText = container.findViewById(R.id.my_lotto_rowA_alpha + i*15);
				setText.setText(myLotto.get_row_alpha(i));
				if(myLotto.get_qORm().charAt(i) == 'm')
				{
					setText.setTextColor(Color.RED);
				}


				//result
				setText = container.findViewById(R.id.my_lotto_rowA_result + i*15);
				setText.setText(myLotto.get_row_result(i));

				//number, hit
				for(int j = 0 ; j < 6 ; j++)
				{
					setText = container.findViewById(R.id.my_lotto_rowA_num1_text + i*15 + j*2);
					setText.setText(myLotto.get_number(i, j) + "");//바꿔야함 -> number
					ImageView image = container.findViewById(R.id.my_lotto_rowA_num1_image + i*15 + j*2);
					if(myLotto.get_hit(i, j) > 0 && myLotto.get_hit(i, j) <= 2)
					{
						image.setImageResource(R.drawable.my_yellow_oval);
					}
					else if(myLotto.get_hit(i, j) == 3 || myLotto.get_hit(i, j) == 4)
					{
						image.setImageResource(R.drawable.my_blue_oval);
					}
					else if(myLotto.get_hit(i, j) == 5 || myLotto.get_hit(i, j) == 6)
					{
						image.setImageResource(R.drawable.my_red_oval);
					}
					else if(myLotto.get_hit(i, j) == 7)
					{
						image.setImageResource(R.drawable.my_black_oval);
					}
				}
			}
			intentToMain.putExtra("resultMsg", "result ok");
			intentToMain.putExtra("lotto", myLotto);
		}
	}

	public void btnClick(View v)
	{
		if(v.getId() == R.id.backBtn)
		{
			setResult(RESULT_OK, intentToMain);//메인 액티비티의 onActivityResult()로 넘어감
			//데이터도 같이 넘길거면 setResult(RESULT_OK, intent2)
			//결과가 어떤지만 알고싶으면 setResult(RESULT_OK)
		}
		else if(v.getId() == R.id.insertBtn)
		{
			setResult(INSERT_DB, intentToMain);
		}
		finish();
	}
}