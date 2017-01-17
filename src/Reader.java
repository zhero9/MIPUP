import java.io.BufferedReader;
//import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.util.Scanner;
//import java.util.Vector;

public class Reader {

	public Reader(String readFrom){
		path = readFrom;
	}

	private String path;

	public String colNames;
	public String[] rowNames;
	public boolean[][] matrix;

	public void readFile(){	
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			String input;
			int rows = 0;
			int columns = 0;
			while( (input = in.readLine()) != null){
				++rows;
				if (rows == 1){
					String [] firstLine = input.split(";");
					columns = firstLine.length;
				}
			}
			matrix = new boolean[rows-1][columns-1];
			in.close();

			in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			input = in.readLine();
			colNames = input;
			rowNames = new String[rows-1];
			for(int i = 0; i < rows-1; i++){
				input = in.readLine();
				String [] line = input.split(";");
				rowNames[i] = line[0];
				for(int j = 0; j< columns-1; j++){
					if( (Integer.parseInt(line[j+1])) == 0 ){
						matrix[i][j] = false;
					}else{
						matrix[i][j] = true;
					}
				}
			}
			in.close();
		} catch(IOException e){
			System.out.println("Row Split coludn't read file. Check your path, format.");
			return;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			System.out.println("RowSplit: Couldn't read file. Numbers couldn't be parsed.");
			//e.printStackTrace();
			return;
		}catch(Exception e){
			System.out.println("Check whether there is a empty line at the end of your file. There shouldn't be one.");
			return;
		}
	}
}
