import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class Trader
{
    /**
     * Holds the data.
     * 
     * 0: closing price
     * 1: 12 days Moving Average
     * 2: 26 days Moving Average
     * 3: 24 days Trade Break out Rule
     * 4: 29 days Volatility
     * 5: 10 days Momentum
     * 6: MA Action
     * 7: TBR Action
     * 8: VOL Action
     * 9: MOM Action
     */
    private double[][] data = new double[0][0];
    
    private static final int POPULATION_SIZE = 100;
    private double[][] population=new double[POPULATION_SIZE][4];
    private double[] fitness = new double[POPULATION_SIZE];
    
    /*Initial budget.*/
    private double budget = 3000;
    /*(empty) initial portfolio.*/
    private int portfolio = 0;
    
    private static final double TBR_THRESHOLD=-0.02;
    private static final double VOL_THRESHOLD=0.02;
    private static final double MOM_THRESHOLD=0;
    
    /*read txt file*/
    public void load(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        ArrayList<double[]> lines = new ArrayList<>();
        String line = null;
        
        while ((line = reader.readLine()) != null) {
            double[] values = new double[10];
            values[0]=Double.parseDouble(line.trim());
            lines.add(values);
        }
        data = lines.toArray(data);
    }
    
    /*start GA*/
    public void run() {
    	try {
    		load("Unilever.txt");
    		prepare();
    		initpopulation();
    	}catch(IOException e) {
    		e.printStackTrace(System.out);
    	}
    	///////////////////////////////////////////////////////////////
    }
    private void evaluate() {
    	for(int i=0;i<POPULATION_SIZE;i++) {
    		fitness[i]=trade(population[i]); //pop contains weight information
    		System.out.println(population[i]);
    		System.out.print(" --> "+fitness[i]+"\n");
    	}
    }
    /*Simulate trade*/
    private double trade(double weight[]) { //weight[4]
    	double[] count={0,0,0}; //[HOLD,BUY,SELL]
    	for(int i=0;i<data.length;i++) {
    		for(int j=6;j<10;j++) { //fill count[]
    			if(data[i][j]==0)
    				count[0]+=weight[j-6]; // 0 1 2 3
    			else if(data[i][j]==1)
    				count[1]+=weight[j-6];
    			else if(data[i][j]==2)
    				count[2]+=weight[j-6];
    		}
    		
    		double max=0;	int action=0; 
    		for(int j=0;j<count.length;j++) {
    			if(max<weight[i]) {
        			max=weight[i];
        			action=i;
        		}
    		}
    		
    		if(action==1) {
        		int amount=(int)(budget/data[i][0]);
        		portfolio+=amount;
        		budget-=data[i][0];
        	}else if(action==2) {
        		budget+=portfolio*data[i][0];
        		portfolio=0;
        	}
    	}
        
        // return the total amount (cash) after the trading session,
        // assuming that any stock is sold at the last known closing price
        return budget + (portfolio * data[data.length - 1][0]);
    }
    /*INIT population*/
    private void initpopulation() {
    	for(int i=0;i<POPULATION_SIZE;i++) 
    		for(int j=0;j<population.length;j++) 
    			population[i][j]=Math.random();
    }
    
    public void prepare() { //fill data columns [1,2,4]
    	//cal 12MA , 26MA	
    	int lowMA=12;	int highMA=26;
    	for(int i=lowMA-1;i<data.length;i++) {//loop every rows
   			double sum=0;
   			for(int j=1;j<lowMA;j++)
    			sum+=data[i-j][0];
    		//System.out.println("["+i+"][1] = "+sum/lowMA);
    		data[i][1]=sum/lowMA;

    		if(i>=highMA-1) {
    			sum=0;
    			for(int j=1;j<highMA;j++) 
    				sum+=data[i-j][0];
    			data[i][2]=sum/highMA;
    		}
    	}
    	
    	//cal 24TBR
    	int tbrL=24;
    	for(int i=tbrL-1;i<data.length;i++) {
    		double max=0;
    		for(int j=1;j<tbrL;j++) 
    			if(max<data[i-j][0])  
    				max=data[i-j][0];
    		//System.out.println(data[i][0]+" AND "+max);
    		//System.out.println("["+i+"][3] = "+(data[i][0]-max)/max);
    		data[i][3]=(data[i][0]-max)/max;
    	}
    	
    	//cal 29VOL
    	int volL=29;	double sum=0;
    	for(int i=volL-1;i<data.length;i++) {
    		sum=0;
    		for(int j=0;j<volL-1;j++) //29-1 sample
    			sum+=data[i-j][0];
    		double mean=sum/(volL-1);	
    		
    		sum=0; //reset
    		for(int j=0;j<volL-1;j++) //29-1 sample
    			sum+=Math.pow(data[i-j][0]-mean,2);
    		double std=Math.sqrt(sum/(volL-1)); //sample????
    		
    		sum=0;
    		for(int j=1;j<volL;j++)
    			sum+=data[i-j][0];
    		data[i][4]=std/(sum/volL);
    	}
    	
    	//cal 10MOM
    	int momL=10;
    	for(int i=momL;i<data.length;i++) 
    		data[i][5]=data[i][0]-data[i-momL][0];
    	
    	
    	//MA signal
    	for(int i=lowMA-1;i<data.length;i++) {
    		if(data[i][1]==data[i][2]) 
    			data[i][6]=0;
    		else if(data[i][1]>data[i][2]) 
    			data[i][6]=1;
    		else
    			data[i][6]=2;
    	}
    	
    	DecimalFormat df=new DecimalFormat("###.##"); //to round decimal
    	//TBR signals
    	for(int i=tbrL-1;i<data.length;i++) {
    		double val=Double.parseDouble(df.format(data[i][3]));
    		if(val==TBR_THRESHOLD) 
    			data[i][7]=0;
    		else if(val<TBR_THRESHOLD) 
    			data[i][7]=1;
    		else
    			data[i][7]=2;
    	}
    	
    	//VOL signals
    	for(int i=volL-1;i<data.length;i++) {
    		double val=Double.parseDouble(df.format(data[i][4]));
    		if(val==VOL_THRESHOLD) 
    			data[i][8]=0;
    		else if(val>VOL_THRESHOLD) 
    			data[i][8]=1;
    		else
    			data[i][8]=2;
    	}
    	
    	//MOM signals
    	for(int i=momL;i<data.length;i++) {
    		double val=Double.parseDouble(df.format(data[i][5]));
    		if(val==MOM_THRESHOLD) 
    			data[i][9]=0;
    		else if(val<MOM_THRESHOLD) 
    			data[i][9]=1;
    		else
    			data[i][9]=2;
    	}
    }
}