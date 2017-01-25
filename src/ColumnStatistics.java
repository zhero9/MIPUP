import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Vector;


public class ColumnStatistics {

	public static String[] calculate(String path,boolean[][] orgM,int t, Vector<Vector<Integer>> columnsCopies) throws FileNotFoundException{
		int m = orgM.length;
		int n = orgM[0].length;
		
		double[][] matrix = new double[m][n];

		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
		String input;
		try {
			input = in.readLine();

			for(int i = 0; i < n; i++){
				input = in.readLine();
				String [] line = input.split("\t");
				for(int j = t; j< (m+t); j++){
					matrix[j-t][i] = Double.parseDouble(line[j]);
				}
			}

			double avrg, stdDev;
			String[] stats = new String[columnsCopies.size()];

			DecimalFormat df = new DecimalFormat("#.0#"); 
			
			for(int i = 0; i< columnsCopies.size(); i++){
				int num = 0;
				avrg = 0;
				for(Integer j : columnsCopies.elementAt(i)){
					for(int k = 0; k< m; k++){
						if (orgM[k][j]){
							avrg += matrix[k][j]; 
							num++;
						}
					}
				}
				avrg = avrg/num;
				stdDev = 0;
				for(Integer j : columnsCopies.elementAt(i)){
					for(int k = 0; k< m; k++){
						if (orgM[k][j]){
							stdDev += (avrg - matrix[k][j])*(avrg - matrix[k][j]);
						}
					}
				}
				stdDev = Math.sqrt(stdDev/num);
				stats[i]= df.format(avrg)+"±"+df.format(stdDev);
			}

			return stats;
		} catch (IOException e) {
			System.out.println("Couldn't read matrix for calculating statistics for VAF.");
			e.printStackTrace();
			return null;
		} catch (Exception e){
			System.out.println("Error while calculating statistics for VAF");
			return null;
		}
	}

}
