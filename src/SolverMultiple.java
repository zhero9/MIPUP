import java.io.File;
import java.util.Vector;

public class SolverMultiple {

		String pathToMatrix;
		String alg = "";
		boolean isVaf = false;
		int numOfSolutions;
		int limit;
		
		public SolverMultiple(String pathToMatrix, double minVAFPresent, String alg, int maxNumOfSolutions) {
			///// Multiple Optima //////////
			this.pathToMatrix = pathToMatrix;
			this.alg = alg;
			readVAF(minVAFPresent);
			isVaf = true;
			limit = maxNumOfSolutions;
		}
		
		public void solveAndWrite() {
			try {
				makeRowSplit();
				for(int i = 0; i<numOfSolutions; i++) {
					makePath(pathToMatrix, i);
					writeFile(i);
					writeTree(i);
					writeColumnsLegend();
				}
				
			} catch (Exception e) {
				System.out.println("Row Split failed.");
				return;
			}
		}

		boolean[][] originalMatrix; // first given matrix - remains the same
		
		///// These are not vectors ////////
		Vector<boolean[][]> splitMatrixMulti;
		Vector<boolean[][]> matrixFilteredMulti; // no duplicated columns row split
		Vector<int[]> rowsMulti; // holds info about which row splits in what
		
		//// What happens with these below??? /////
		String colName;
		String[] rowName;
		
		//// Remains the same //////
		Vector<String> rowN =new Vector<String>();
		Vector<Vector<Integer>> cc = new Vector<Vector<Integer>>(); // columns copies
		int tmp; /// number of rows that do not contain data (rows before matrix)
		
		private void readVAF(double minVAFPresent) {
			ReaderVAF rVAF = new ReaderVAF(pathToMatrix, minVAFPresent);
			try{
				rVAF.readFile();
				originalMatrix = rVAF.matrix;
				colName = rVAF.colNames;
				rowName = rVAF.rowNames;
				tmp = rVAF.tmp;
			}catch (NumberFormatException e){
				System.out.println("RowSplit: Couldn't read file. Numbers couldn't be parsed.");
				e.printStackTrace();
				return;
			}
		}

		private void makeRowSplit() {
				RowSplit x = new RowSplit(originalMatrix, alg, limit);
				this.numOfSolutions = x.numOfSolutions;
				splitMatrixMulti = x.solutionSolutions;
				matrixFilteredMulti = x.rowSplitMSolutions;
				rowsMulti = x.rowsSolutions;
				cc = x.getCC();
		}

		private void writeFile(int i) {
			try {
				WritterMatrix w = new WritterMatrix(splitMatrixMulti.elementAt(i), rowsMulti.elementAt(i), colName, rowName, pathTo);
				w.writeFile();
				rowN = w.rowN;
			} catch (Exception e) {
				System.out.println("Coludn't write row split to a file.");
				return;
			}
		}

		private void writeTree(int i) {
			try {
				WritterPhylogenyTree p = new WritterPhylogenyTree(originalMatrix,
						splitMatrixMulti.elementAt(i), matrixFilteredMulti.elementAt(i), rowsMulti.elementAt(i), colName, rowName, pathTo, cc, rowN);
				if(isVaf) p.writePhylogenyTreeFile(pathToMatrix,tmp);
				else  p.writePhylogenyTreeFile();
			} catch (Exception e){
				e.printStackTrace();
				System.out.println("Colund't write a .dot file for phylogeny tree.");
				return;
			}
		}

		private void writeColumnsLegend(){
			try{
				WriterColumnLegend cl = new WriterColumnLegend();
				cl.writeLegendSameColumns(pathTo, colName, cc);
			}catch(Exception e){
				e.printStackTrace();
				System.out.println("Coludn't write column legend.");
				return;
			}
		}

		private String pathTo;

		public void makePath(String p, int j) {
			String[] folders = p.split("/");
			int t = folders.length;
			String folder = "";
			for(int i = 0; i<t-1; i++){
				folder = folder.concat(folders[i]+"/");
			}
			folder = folder.concat(folders[t-1].substring(0, folders[t-1].length()-4).concat("_RS"));
			File dir = new File(folder);
			dir.mkdir();
			pathTo = folder.concat("/"+folders[t-1].substring(0, folders[t-1].length()-4)+"_"+alg+"_s"+j);
		}
	
}
