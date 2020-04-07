package Project.lottoinfo;

import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

//adapter : view 생성
public class LottoAdapter extends RecyclerView.Adapter<LottoAdapter.LottoViewHolder>
{
	private ArrayList<LottoInfo> lottoList;
	private LottoViewHolder viewHolder;

	public interface OnItemClickListener
	{
		void onItemClick(View v, int pos);
	}

	private OnItemClickListener mListener = null;//리스너 객체 참조 저장 변수

	public void setOnItemClickLitener(OnItemClickListener listener)
	{
		this.mListener = listener;
	}
	//1. 커스텀 리스너 인터페이스 정의
	//2. 리스너 객체를 전달하는 메서드(setOnitemClicklistenr)와 전달된 객체를 저장할 변수 mListener 추가
	//3. 아이템 클릭 이벤트 핸들러 메서드에서 리스너 객체 메서드(onitemclick) 호출
	//4. 액티비티(프래그먼트)에서 커스텀 리스너 객체 생성 및 전달

	//아이템 뷰를 저장하는 뷰홀더
	public class LottoViewHolder extends RecyclerView.ViewHolder
	{
		protected View view;

		protected ConstraintLayout cLayout;

		protected TextView id;

		protected TextView round;

		protected LinearLayout[] mLayout;
		protected TextView[] mAlpha;
		protected TextView[] mResult;
		protected TextView[][] mNumberText;
		protected ImageView[][] mNumberImage;

		private SparseBooleanArray selectedItems = new SparseBooleanArray(0);//삭제

		int len = 5;
		int cnt = 0;

		public LottoViewHolder(View itemView)
		{
			super(itemView);
			Log.i("check", "LottoAdapter LottoViewHolder()");
			//뷰 객체에 대한 참조
			this.view = itemView;

			this.cLayout = this.view.findViewById(R.id.item_layout);
			this.id = this.view.findViewById(R.id.layout_id);
			this.round = this.view.findViewById(R.id.my_lotto_round);
			this.mLayout = new LinearLayout[this.len];
			this.mAlpha = new TextView[this.len];
			this.mResult = new TextView[this.len];
			this.mNumberText = new TextView[this.len][6];
			this.mNumberImage = new ImageView[this.len][6];

			for(int i = 0 ; i < 5 ; i++)
			{
				this.mLayout[i] = this.view.findViewById(R.id.my_lotto_rowA + i*15);
				this.mAlpha[i] = this.view.findViewById(R.id.my_lotto_rowA_alpha + i*15);
				this.mResult[i] = this.view.findViewById(R.id.my_lotto_rowA_result + i*15);
				for(int j = 0 ; j < 6 ; j++)
				{
					this.mNumberText[i][j] = this.view.findViewById(R.id.my_lotto_rowA_num1_text + i*15 + j*2);
					this.mNumberImage[i][j] = this.view.findViewById(R.id.my_lotto_rowA_num1_image + i*15 + j*2);
				}
			}
			this.cLayout.setBackgroundResource(R.drawable.recycler_border);
/*
			this.view.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					//클릭 된 애의 position을 알려줄거야
					int pos = getAdapterPosition();
					if(pos != RecyclerView.NO_POSITION)//no_position은 아이템이 삭제된 경우
					{
						if(selectedItems.get(pos, false))
						{
							selectedItems.put(pos, false);
							v.setBackgroundResource(R.drawable.recycler_border);
						}
						else
						{
							selectedItems.put(pos, true);
							v.setBackgroundResource(R.drawable.recycler_focus_border);
						}
					}
				}
			});*/
		}

		public SparseBooleanArray getPos()//삭제
		{
			return selectedItems;
		}
	}

	public SparseBooleanArray getCheckedItemPosition()//삭제
	{
		return viewHolder.getPos();
	}

	public int getCnt()
	{
		return viewHolder.cnt;
	}

	//데이터 리스트 받아
	public LottoAdapter(ArrayList<LottoInfo> list)
	{
		this.lottoList = list;
	}

	//뷰홀더 생성
	//원하는 레이아웃을 LayoutInflater를 통해 띄워줌
	@Override
	public LottoViewHolder onCreateViewHolder(ViewGroup viewGroup, int ViewType)
	{
		Log.i("check", "LottoAdapter onCreateViewHolder()");
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_lotto_item, viewGroup, false);
		viewHolder = new LottoViewHolder(v);

		return viewHolder;
	}

	//뷰홀더의 아이템 뷰에 표시
	@Override
	public void onBindViewHolder(@NonNull LottoViewHolder viewHolder, int position)
	{
		Log.i("check", "LottoAdapter onBindViewHolder()");

		//남아있는 변경된 정보 싹 초기화(row 보이기, 색 바꾸기)
		for(int i = 0 ; i < 5 ; i++)
		{
			viewHolder.mLayout[i].setVisibility(View.GONE);
			viewHolder.round.setTextColor(Color.parseColor("#808080"));
			for(int j = 0 ; j < 6 ; j++)
			{
				viewHolder.mNumberImage[i][j].setImageResource(R.drawable.my_white_oval);
			}
		}

		viewHolder.cLayout.setBackgroundResource(R.drawable.recycler_border);

		viewHolder.id.setText(lottoList.get(position).get_id());

		viewHolder.round.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
		viewHolder.round.setGravity(Gravity.CENTER);
		viewHolder.round.setText(lottoList.get(position).get_round()+"");
		viewHolder.round.setVisibility(View.VISIBLE);

		String qorm = lottoList.get(position).get_qORm();

		//viewHolder.round.setHint(lottoList.get(position).get_id());///id
		for(int i = 0 ; i < lottoList.get(position).get_len() ; i++)
		{
			viewHolder.mLayout[i].setVisibility(View.VISIBLE);

			viewHolder.mAlpha[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
			viewHolder.mResult[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

			viewHolder.mAlpha[i].setText(lottoList.get(position).get_row_alpha(i));
			viewHolder.mResult[i].setText(lottoList.get(position).get_row_result(i));
			for(int j = 0 ; j < 6 ; j++)
			{
				viewHolder.mNumberText[i][j].setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
				viewHolder.mNumberText[i][j].setText(lottoList.get(position).get_number(i, j));
				if(lottoList.get(position).get_hit(i, j) > 0 && lottoList.get(position).get_hit(i, j) <= 2)
				{
					viewHolder.mNumberImage[i][j].setImageResource(R.drawable.my_yellow_oval);
				}
				else if(lottoList.get(position).get_hit(i, j) == 3 ||lottoList.get(position).get_hit(i, j) == 4)
				{
					viewHolder.mNumberImage[i][j].setImageResource(R.drawable.my_blue_oval);
				}
				else if(lottoList.get(position).get_hit(i, j) == 5 || lottoList.get(position).get_hit(i, j) == 6)
				{
					viewHolder.mNumberImage[i][j].setImageResource(R.drawable.my_red_oval);
				}
				else if(lottoList.get(position).get_hit(i, j) == 7)
				{
					viewHolder.mNumberImage[i][j].setImageResource(R.drawable.my_black_oval);
				}
			}

			//글자 색
			if(qorm.charAt(i) == 'q')
			{
				viewHolder.mAlpha[i].setTextColor(Color.parseColor("#808080"));
			}
			else if(qorm.charAt(i) == 'm')
			{
				viewHolder.mAlpha[i].setTextColor(Color.RED);
			}
		}

		//만일 삭제한 로또의 줄 갯수가 더 많으면 View.GONE 을 해줘야 제대로 보임
		/*
		for(int i = lottoList.get(position).get_len() ; i < 5 ; i++)
		{
			viewHolder.mLayout[i].setVisibility(View.GONE);
		}*/
	}

	@Override
	public int getItemCount()
	{
		return (lottoList != null ? lottoList.size() : 0);
	}
}
