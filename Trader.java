import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class can be used to simulate a trading strategy based
 * on technical indicators.
 *
 * @author Fernando Otero
 * @version 1.0
 */
public class Trader
{
    /**
     * Holds the data.
     * 
     * 0: closing price
     * 1: 15 days Moving Average
     * 2: 50 days Moving Average
     * 3: 10 days Momentum
     * 4: MA Action
     * 5: MOM Action
     */
    private double[][] data = new double[0][0];
    
    /**
     * Initial budget.
     */
    private double budget = 3000;
    
    /**
     * The (empty) initial portfolio.
     */
    private int portfolio = 0;
    
    /**
     * Loads a csv file in memory, skipping the first line (column names).
     * 
     * @ param filename the file to load.
     */
    public void load(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        ArrayList<double[]> lines = new ArrayList<>();
        String line = null;
        
        while ((line = reader.readLine()) != null) {
            double[] values = new double[6];
            values[0]=Double.parseDouble(line.trim());
            lines.add(values);
        }
        data = lines.toArray(data);
    }
    
    public void prepare() { //fill data columns [1,2,4]
    	//cal 15MA , 50MA	
    	int lowMA=15;	int highMA=50;
    	for(int i=lowMA-1;i<data.length;i++) {//loop every rows
   			double sum=0;
   			for(int j=0;j<15;j++)
    			sum+=data[i-j][0];
    		//System.out.println("["+i+"][1] = "+sum/lowMA);
    		data[i][1]=sum/lowMA;

    		if(i>=highMA-1) {
    			sum=0;
    			for(int j=0;j<15;j++) 
    				sum+=data[i-j][0];
    			data[i][2]=sum/highMA;
    		}
    	}
    	
    	for(int i=50;i<data.length;i++) {
    		if(data[i][1]>data[i][2]) 
    			data[i][4]=1;
    		else if(data[i][1]>data[i][2]) 
    			data[i][4]=2;
    		else
    			data[i][4]=0;
    	}
    }
    
    /**
     * Simulates a trade.
     * 
     * @return the total amount (cash) after the trading session.
     */
    public double trade() {
        // go through each line of our data
        for (int i = 0; i < data.length; i++) {
            // should look at the "Action" column to decide what to do,
            // ignoring cases where there is no action (Double.NaN)
        	if(data[i][4]==1 && budget>=data[i][0]) {//buy
        		int amount = (int)(budget / data[i][0]);
        		portfolio += amount;
        		budget-=data[i][0];
        	}else if(data[i][4]==2) {//sell
        		budget += portfolio * data[i][0];
        		portfolio = 0;
        	}
        }
        
        // return the total amount (cash) after the trading session,
        // assuming that any stock is sold at the last known closing price
        return budget + (portfolio * data[data.length - 1][0]);
    }
}