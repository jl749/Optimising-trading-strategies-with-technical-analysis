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
    
    private static final int MAX_GEN=200;
    private static final int POPULATION_SIZE=1000;
    private static final int T_SIZE=(int)(POPULATION_SIZE*0.2); //around 20% of POP SIZE
    private static final int PATTERN_LEN = (int) Math.pow(3, 4);
    private double[][][] population=new double[POPULATION_SIZE][PATTERN_LEN][4];
    private double[] fitness = new double[POPULATION_SIZE];
    private int[] patternFreq=new int[PATTERN_LEN];
    
    /*Initial budget.*/
    private double budget = 3000;
    /*(empty) initial portfolio.*/
    
    private static final double TBR_THRESHOLD=-0.02;
    private static final double VOL_THRESHOLD=0.02;
    private static final double MOM_THRESHOLD=0;
    private static final double MUTATION_PROB=0.1;
    
    Random rnd=new Random();
    DecimalFormat df=new DecimalFormat("###.##"); //to round decimal
    
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
    
    private void arrcpy(double[][] des,double[][] source) {
    	for(int i=0;i<des.length;i++)
    		for(int j=0;j<source[0].length;j++)
    			des[i][j]=source[i][j];
    }
    
    /*start GA*/
    public void run() {
    	prepare();	
    	fillpatternFreq();
    	initpopulation();
    		
    	evaluate();

    	double[][][] newGenGroup=new double[POPULATION_SIZE][PATTERN_LEN][4];
    	double[][] tmp=new double[PATTERN_LEN][4];

    	int bestIndex=-1;
    	for(int g=1;g<=MAX_GEN;g++) {
    		System.out.print("<GEN "+g+"> ");
    		bestIndex=0;

    		//if(Arrays.deepEquals(tmp,population[0]))
    			
            for(int i=1;i<POPULATION_SIZE;i++) 
               	if(fitness[bestIndex]<fitness[i])
               		bestIndex=i;
            System.out.println("best fitness= "+df.format(fitness[bestIndex]));
            //elitism
            arrcpy(tmp,population[bestIndex]);
                
            for(int i=1;i<POPULATION_SIZE;i++) {
            	double which=Math.random();
            	if(which>=MUTATION_PROB) {
            		int p1Index=-1,p2Index=-1;
            		while(p1Index==p2Index) {
            			p1Index=select();
            			p2Index=select();
            		}
            		double[][][] offsprings=crossover(p1Index,p2Index);
            		for(int j=0;j<2 && i+j<POPULATION_SIZE;j++) 
            			newGenGroup[i+j]=offsprings[j];
            		i++;
            	}else{
            		double[][] offspring=mutation(select());
            		newGenGroup[i]=offspring;
            	}
           	}
                
            population=newGenGroup;
                
            arrcpy(population[0],tmp);
            //System.out.println("bestIndex = "+bestIndex);
            System.out.println(fitness[bestIndex]+Arrays.toString(fitness));
            evaluate(); //update fitness -> update select()
    	}
    	System.out.println("BEST INDIVIDUAL:");
    	System.out.println(Arrays.deepToString(population[bestIndex]));
    }
    
    /*Roulette selection*/
    private int select() {
    	Set<Integer> arena=new HashSet<Integer>();
            		
    	while(arena.size()<T_SIZE) {
    		int index=(int)(rnd.nextDouble()*POPULATION_SIZE);
    		arena.add(index);
    	}
    	
    	int winner=arena.iterator().next();
    	for(int a:arena) 
    		if(fitness[winner]<fitness[a])
    			winner=a;
    	
    	return winner;
    }
    
    private double[][] mutation(int pIndex){
    	double[][] offspring=population[pIndex];
    	
    	/**
    	 * Mutation performs its own selection method
    	 * Frequently used gene is more likely to be conserved
    	 * And infrequent gene are more likely to mutate
    	 * which parts of individual(population[pIndex]) to mutate --> roulette selection
    	 * */
    	List<Integer> validIndexes=new ArrayList<>();
    	double total=0;
    	for(int i=0;i<patternFreq.length;i++) {
    		if(patternFreq[i]==0) 
    			continue;
    		total+=1/(Math.log10(patternFreq[i])+1);
    		validIndexes.add(i);
    	}
    	double[] roulette=new double[validIndexes.size()];
    	
    	double cum=0;  
        for(int i=0;i<validIndexes.size();i++) {
            roulette[i]=cum+((1/(Math.log10(patternFreq[validIndexes.get(i)])+1) ) / total);
            cum=roulette[i];
        }
        roulette[validIndexes.size()-1]=1; //in case of rounding error

        /**upto 10% of valid genes mutate*/
        for(int i=0;i<validIndexes.size()*0.1;i++) {
        	/**Select gene to mutate*/
        	double prob=rnd.nextDouble();
        	int selectedIndex=-1;
        	for (int j=0;j<validIndexes.size();j++) 
        		if (prob<=roulette[j]) {
        			selectedIndex=validIndexes.get(j); //select pattern to mutate (0121? 1220? 2001?)
        			break;
        		}
        	
        	for(int j=0;j<4 ;j++) 
        		offspring[selectedIndex][j]=rnd.nextDouble();
        }

    	return offspring;
    }
    private double[][][] crossover(int p1Index,int p2Index) {
    	double[][] p1=population[p1Index];
    	double[][] p2=population[p2Index];
    	
    	double[][][] offsprings=new double[2][PATTERN_LEN][4];
    	for(int i=0;i<2;i++) {
    		for(int j=0;j<PATTERN_LEN;j++) {
    			if(patternFreq[j]==0) //skip irrelevant gene
    				continue;
    			Set<Integer> dupCheck=new HashSet<>();
    			while(dupCheck.size()<4) {
    				if(dupCheck.size()<2) {
    					int rndIndex=(int)(Math.random()*4);
    					offsprings[i][j][rndIndex]=p1[j][rndIndex];
    					dupCheck.add(rndIndex);
    					continue;
    				}
    				int rndIndex=(int)(Math.random()*4);
					offsprings[i][j][rndIndex]=p2[j][rndIndex];
					dupCheck.add(rndIndex);
    			}
    		}
    	}
    	return offsprings;
    }
    
    private void evaluate() {
    	for(int i=0;i<POPULATION_SIZE;i++) {
    		fitness[i]=Double.parseDouble(df.format(trade(population[i],budget))); //pop contains weight information
    		//System.out.println("population"+i+" --> "+df.format(fitness[i]));
    	}System.out.println("");
    }
    /*Simulate trade*/
    private double trade(double arr[][],double budget) {
    	double[] count={0,0,0}; //[HOLD,BUY,SELL]
    	double bank=budget;		int portfolio = 0;
    	for(int i=28;i<data.length;i++) { // all signals calculated from index 28 (sufficient information)
    		int[] signals=new int[4];
    		for(int j=0;j<4;j++) 
    			signals[j]=(int) data[i][j+6];
    		int weightIndex=findIndex(signals);
    		
    		for(int j=0;j<4;j++) { //fill count[]
    			if(signals[j]==0)
    				count[0]+=arr[weightIndex][j]; // 0 1 2 3
    			else if(signals[j]==1)
    				count[1]+=arr[weightIndex][j];
    			else if(signals[j]==2)
    				count[2]+=arr[weightIndex][j];
    		}
    		
    		double max=0;	int action=0; 
    		for(int j=0;j<count.length;j++) 
    			if(max<count[j]) {
        			max=count[j];
        			action=j;
        		}
    		
       		if(action==1 && bank>=data[i][0]) {
        		portfolio+=1;
        		bank-=data[i][0]*1;
        	}else if(action==2 && portfolio>0) {
        		bank+=data[i][0];
        		portfolio--;
        	}
    	}
        
    	double totalOutput=bank + (portfolio * data[data.length - 1][0]);
        // return the total amount (cash) after the trading session,
        // assuming that any stock is sold at the last known closing price
        return totalOutput;
    }
    private int findIndex(int[] signals) { // AABC-AAAA =5
    	int sum=0;
    	for(int i=0;i<signals.length;i++) {
    		sum+=(signals[i] * (int)Math.pow(3, i));
    	}
    	return sum;
    }
    
    /*INIT population*/
    
    private void initpopulation() {
    	for(int i=0;i<POPULATION_SIZE;i++) 
    		for(int j=0;j<PATTERN_LEN;j++) {
    			if(patternFreq[j]==0) //this gene is irrelevant
    				continue;
    			double[] tmp=new double[4];
    			for(int k=0;k<4;k++)
    				tmp[k]=Math.random();
    			population[i][j]=tmp;
    		}
    }
    

    private void fillpatternFreq() {
    	for(int i=28;i<data.length;i++) { // all signals calculated from index 28 (sufficient information)
    		int[] signals=new int[4];
    		for(int j=0;j<4;j++) 
    			signals[j]=(int) data[i][j+6];
    		int weightIndex=findIndex(signals);
    		
    		patternFreq[weightIndex]++;
    	}
    }
    private void prepare() { //fill data columns [1,2,3...9]
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