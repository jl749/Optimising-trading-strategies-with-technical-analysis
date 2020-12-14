import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

	public static void decoder(String filename) throws IOException {
		double[][] data=new double[0][0];
		BufferedReader reader = new BufferedReader(new FileReader(filename));
        ArrayList<double[]> lines = new ArrayList<>();
        String line = null;
        
        line = reader.readLine(); //read first line only
        line=line.replaceAll("\\[|\\]", "");

        String[] tmp=line.split(",");	int index=0;
        while(index<tmp.length) {
        	double[] values = new double[4];
        	for(int i=0;i<4;i++) 
        		values[i]=Double.parseDouble(tmp[index++].trim());
        	lines.add(values);
        }
        data=lines.toArray(data);
        
        index=0;
        int[] start= {0,0,0,0};
        int[] end= {2,2,2,2};
        outerloop:
        while(true) {
        	for(int j=0;j<3;j++) {
        		System.out.println(Arrays.toString(start)+" "+Arrays.toString(data[index++]));
        		start[0]++;
        	}
        	for(int j=0;j<start.length;j++) 
        		if(start[j]==3) { 
        			start[j]=0;
        			if(j+1==4)
        				break outerloop;
        			start[j+1]++;
        		}
        }
	}
	
	public static void main(String[] args) throws IOException {
		Trader trader=new Trader();
		trader.load("Unilever.txt");
		
		trader.run();
		//decoder("9k_INDIVIDUAL_1.txt");
	}
}
