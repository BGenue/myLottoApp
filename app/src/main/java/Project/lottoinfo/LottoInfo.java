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

	public LottoInfo(int round, String alpha, String result, String numbers)
	{
		this.round = round;
		set_alpha(alpha);
		set_result(result);
		set_numbers(numbers);
	}

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
		splitNumber(numbers);
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

	private void splitNumber(String n)
	{
		int i = 0 , j = 0;
		String[] tmpN = n.split("q");
		this.numbers = new String[this.len][6];
		for(i = 0 ; i < this.len ; i ++)
		{
			for(j = 0 ; j < 6; j++)
			{
				if(j==5)
				{
					this.numbers[i][j] = tmpN[i].substring(j*2);
				}
				else
				{
					this.numbers[i][j] = tmpN[i].substring(j*2, (j + 1)*2);
				}
			}
		}
	}

	public String get_id() { return this.id; }

	public int get_round()	{ return this.round; }

	public int get_len() { return this.len; }

	public String get_row_alpha(int i)
	{
		return this.alpha[i];
	}

	public String get_row_result(int i)
	{
		return this.result[i];
	}

	public String get_row_number(int i)
	{
		StringBuffer tmp = new StringBuffer();
		for(int j = 0 ; j < 6 ; j++)
		{
			tmp.append(this.numbers[i][j]);
		}
		return tmp.toString();
	}

	public String get_number(int i, int j)
	{
		return this.numbers[i][j];
	}

	//한줄로 합한 녀석이 필요할 때
	public String get_sum_alpha() { return sumN(this.alpha); }

	public String get_sum_result() { return sumN(this.result); }

	public String get_sum_numbers()
	{
		String[] tmp = new String[this.len];
		for(int i = 0 ; i < this.len ; i++)
		{
			for(int j = 0; j < 6 ; j++)
			{
				if(j==0)
				{
					tmp[i] = this.numbers[i][j];
				}
				else
				{
					tmp[i] += this.numbers[i][j];
				}
			}
		}
		return sumN(tmp);
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
		return sb.toString();
	}

	private String sumN(String[] s)
	{
		StringBuilder sum = new StringBuilder();
		for(int i = 0 ; i < this.len ; i++)
		{
			sum.append(s[i]).append("q");
		}
		return sum.toString();
	}

	public int get_hit(int i, int j)
	{
		return this.hit[i][j];
	}
}
