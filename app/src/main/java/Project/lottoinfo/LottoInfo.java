package Project.lottoinfo;

import android.util.Log;

import java.io.Serializable;
/*
QR코드를 스캔해서 내가 산 로또 번호들을 가져와서 가지고 있을거야
또는 DB에 저장된 로또를 이용할때
 */
public class LottoInfo implements Serializable
{
	//내가 산 로또 정보다 저장되어 있음
	//n을 기준으로 나뉘어있음
	private int len;
	private int round;
	private String id;
	private String[] alpha;
	private String[] result;
	private String[][] numbers;
	private int[][] hit;//로또 당첨 번호랑 비교해서 맞은 애

	public LottoInfo()
	{
		len = 0;
		round = 0;
	}

	//set 함수는 db / 웹 에서 받아와서 하나로 이어진 문장을 받고 (쪼개서) 입력
	public void set_id(String id) { this.id = id; }

	public void set_round(int round) { this.round = round; }

	public void set_alpha(String alpha)
	{
		this.alpha = alpha.split("q");
		this.len = this.alpha.length;
	}

	public void set_result(String result)
	{
		this.result = result.split("q");
	}

	public void set_numbers(String numbers)
	{
		this.numbers = splitNumber(numbers);
	}

	//1234q1234q1234 => 12 34 / 12 34 / 12 34
	private String[][] splitNumber(String n)
	{
		Log.i("check", "LottoInfo :" + n);
		int i = 0 , j = 0;
		String[] tmpRow = n.split("q");
		String[][] tmp = new String[this.len][6];
		for(i = 0 ; i < this.len ; i ++)
		{
			for(j = 0 ; j < 6; j++)
			{
				if(j==5)
				{
					tmp[i][j] = tmpRow[i].substring(j*2);
				}
				else
				{
					tmp[i][j] = tmpRow[i].substring(j*2, (j + 1)*2);
				}
			}
		}

		return tmp;
	}

	public void set_hit(String h)
	{
		this.hit = new int[this.len][6];
		if(!h.equals(""))
		{
			String[] tmpH = h.split("q");
			for(int i = 0; i < tmpH.length ; i++)
			{
				String[] n = tmpH[i].split(",");
				int x = Integer.parseInt(n[0]);
				int y = Integer.parseInt(n[1]);
				this.hit[x][y] = 1;
			}
		}
	}

	public String get_id() { return this.id; }

	public int get_round()	{ return this.round; }

	public int get_len() { return this.len; }

	public int get_hit(int i, int j)
	{
		return this.hit[i][j];
	}

	//A / B / C / D / E
	public String get_row_alpha(int i)
	{
		return this.alpha[i];
	}

	//낙첨 / 낙첨 / 낙첨 / 낙첨 / 낙첨
	public String get_row_result(int i)
	{
		return this.result[i];
	}

	// 1234 / 1234 / 1234 / 1234 / 1234
	public String get_row_number(int i)
	{
		StringBuffer tmp = new StringBuffer();
		for(int j = 0 ; j < 6 ; j++)
		{
			tmp.append(this.numbers[i][j]);
		}
		return tmp.toString();
	}

	// 12 34 / 12 34 / 12 34 / 12 34 / 12 34
	public String get_number(int i, int j)
	{
		return this.numbers[i][j];
	}

	//한줄로 합한 녀석이 필요할 때 _sum 붙여
	public String get_sum_alpha() { return sumN(this.alpha); }

	public String get_sum_result() { return sumN(this.result); }

	public String get_sum_numbers()
	{
		String[] tmp = new String[this.len];
		for(int i = 0 ; i < this.len ; i++)
		{
			tmp[i] = get_row_number(i);
			/*for(int j = 0; j < 6 ; j++)
			{
				if(j==0)
				{
					tmp[i] = this.numbers[i][j];
				}
				else
				{
					tmp[i] += this.numbers[i][j];
				}
			}삭제*/
		}
		return sumN(tmp);
	}

	private String sumN(String[] s)
	{
		StringBuilder sum = new StringBuilder();
		for(int i = 0 ; i < this.len ; i++)
		{
			sum.append(s[i]);
			if(i != this.len -1)
			{
				sum.append("q");
			}
		}
		return sum.toString();
	}

	public String get_sum_hit()
	{
		StringBuffer sb = new StringBuffer();
		for(int i = 0 ; i < this.len ; i++)
		{
			for(int j = 0 ; j < 6 ; j++)
			{
				if(this.hit[i][j] == 1)
				{
					sb.append(i).append(",").append(j).append("q");
				}
			}
		}
		sb = sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
}
