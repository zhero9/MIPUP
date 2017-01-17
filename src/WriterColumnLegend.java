import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class WriterColumnLegend {

	public WriterColumnLegend() {
	}

	public void writeLegendSameColumns(String path, String colName,	Vector<Vector<Integer>> columnsCopies) {

		BufferedWriter writer = null;

		try {
			String[] columns = colName.split(";");
			File file = new File(path + "_columns.csv");

			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new BufferedWriter(new FileWriter(file));
			//writer.write("digraph {");
			//writer.newLine();

			// / Write legend for equal columns:
			if (columnsCopies.size() > 0) {
				writer.write("Folowing mutations-columns are equal.");
				writer.newLine();
				for (int k = 0; k < columnsCopies.size(); k++) {
					writer.write(str(k)+";"+columns[columnsCopies.elementAt(k).elementAt(0)+1]);
					for (int t = 1; t < columnsCopies.elementAt(k).size(); t++) {
						// writer.write("=");
						writer.write(";"+ (columns[columnsCopies.elementAt(k).elementAt(t)+1]));
					}
					writer.newLine();
				}
				//writer.write("\",shape=box,fontsize=18];");
				//writer.newLine();
			}
			//writer.write("}");
			//writer.newLine();
		} catch (IOException e) {
			System.out.println("RowSplit: Error in writting the legend of same columns.");
			e.printStackTrace();
			return;
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (Exception ex) {
				System.out.println("RowSplit: Error in closing the BufferedWriter-legend of columns"+ ex);
			}
		}
	}
	
	private String str(int k){
		if(k/60 <= 1){
			return Character.toString((char) (65+k%60));
		}else{
			return Character.toString((char) (65+k%60)).concat(""+((int) k/60));
		}
	}
}
