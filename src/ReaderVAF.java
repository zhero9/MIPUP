import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class ReaderVAF {
	
	public ReaderVAF(String readFrom, double minVAFP){
		
		path = readFrom;
		this.minVAFP = minVAFP;
	}
	
	private String path;
	private double minVAFP;
	private String[] colName;
	
	public String colNames;
	public String[] rowNames;
	public boolean[][] matrix;
	
	public void readFile() throws NumberFormatException, IOException{
		
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
		String input;
		int rows = 0;
		int columns = 0;
    	int tmp = 0;
		while( (input = in.readLine()) != null){
		    ++rows;
		    if (rows < 3 && rows == 1){
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
		matrix = new boolean[columns-tmp][rows-1];
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
				//System.out.print("i,j:"+i+(j-4));
				//System.out.println("   broj :"+ line[j]);
				if( (Double.parseDouble(line[j])) >= minVAFP ){
					matrix[j-tmp][i] = true;
				}else{
					matrix[j-tmp][i] = false;
				}
			}
		}
		
		colNames = ";"+colName[1];
		for(int k = 2; k<colName.length; k++){
			colNames = colNames.concat(";"+colName[k]);
		}
		
	}

}
