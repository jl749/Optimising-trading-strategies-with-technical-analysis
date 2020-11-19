import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		Trader trader=new Trader();
		try{
			trader.load("Unilever.txt");
		}catch(IOException e){
			e.printStackTrace(System.out);
		}
		trader.prepare();
		//System.out.println(trader.trade());
	}

}
