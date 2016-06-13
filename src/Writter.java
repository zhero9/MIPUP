import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class Writter {

	public Writter(boolean[][] matrix,int[] rows, String colNames, String[] rowNames, String path ) {
		this.matrix = matrix;
		this.rows = rows;
		this.colNames = colNames;
		this.rowNames = rowNames;
		this.path = path;
	}

	private boolean[][] matrix;
	private int[] rows;
	private String colNames;
	private String[] rowNames;
	private String path;

	public Vector<String> rowN=new Vector<String>();

	public void writeFile() {
		BufferedWriter writer = null;
		try {
			File file = new File(path+"_RS.csv");

			if (!file.exists()) {
				file.createNewFile();
			}

			writer = new BufferedWriter(new FileWriter(file));

			writer.write(colNames);
			writer.newLine();
			int p = 1;
			for(int i = 0; i< matrix.length; i++){
				if (p == 1  && i< rows.length-1 && rows[i+1] > rows[i] ){
					writer.write(rowNames[rows[i]]);
					rowN.addElement(rowNames[rows[i]]);
				}else if( i == rows.length-1 && rows[i-1] < rows[i]){
					writer.write(rowNames[rows[i]]);
					rowN.addElement(rowNames[rows[i]]);
				}else{
					writer.write(rowNames[rows[i]]+"_"+p);
					rowN.addElement(rowNames[rows[i]]+"_"+p);
				}
				p++;
				if( i< rows.length-1 && rows[i+1] > rows[i]) p = 1;
				for(int j = 0; j< matrix[0].length; j++){
					writer.write(";"+ (matrix[i][j] ? 1 : 0));
				}
				writer.newLine();

			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.out.println("RowSplit: Couldn't close the BufferedWriter"+ioe);
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (Exception ex) {
				System.out.println("RowSplit:Error in closing the BufferedWriter" + ex);
			}
		}
	}
}
