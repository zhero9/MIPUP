import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class ReaderExt {

	public ReaderExt(String readFrom, double maxVAFnP, double minVAFP){

		path = readFrom;
		this.minVAFP = minVAFP;
		this.maxVAFnP = maxVAFnP;
	}

	private String path;
	private double minVAFP;
	private double maxVAFnP;
	private String[] colName;

	public String colNames;
	public String[] rowNames;
	public int[][] matrix;

	public int tmp; // number of rows of data before matrix

	public void readFile(){
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			String input;
			int rows = 0;
			int columns = 0;
			tmp = 0;
			while( (input = in.readLine()) != null){
				++rows;
				if (rows == 1){
					String [] firstLine = input.split("\t");
					columns = firstLine.length;
				} else if (rows == 2){
					String[] secondLine = input.split("\t");
					while(!secondLine[tmp].equals("0")){
						tmp++;
					}
				}
			}
			tmp++;
			//System.out.println(rows+"x"+columns);
			matrix = new int[columns-tmp][rows-1];
			//
			in.close();
			in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			input = in.readLine();

			String[] t = input.split("\t");
			rowNames = new String[t.length-tmp];
			for(int i = tmp; i<t.length; i++){
				rowNames[i-tmp] = t[i];
			}

			colName = new String[rows];
			for(int i = 0; i < rows-1; i++){
				input = in.readLine();
				String [] line = input.split("\t");
				colName[i+1] = line[0].concat(":"+line[1]);
				for(int j = tmp; j< columns; j++){
					if( (Double.parseDouble(line[j])) >= minVAFP ){
						matrix[j-tmp][i] = 1;
					}else if((Double.parseDouble(line[j])) <= maxVAFnP){
						matrix[j-tmp][i] = 0;
					}else{
						matrix[j-tmp][i] = 2;
					}
				}
			}

			colNames = ";"+colName[1];
			for(int k = 2; k<colName.length; k++){
				colNames = colNames.concat(";"+colName[k]);
			}
		} catch(IOException e){
			System.out.println("Row Split coludn't read file. Check your path, format.");
			return;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			System.out.println("RowSplit: Couldn't read file. Numbers couldn't be parsed.");
			e.printStackTrace();
			return;
		} catch (Exception e){
			System.out.println("Error while reading: Check whether there is an empty line at the end of your file. There shouldn't be one.");
			System.out.println("Check if you picked the right type of input.");
			return;
		}

	}

}
