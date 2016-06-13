import java.io.File;
import java.util.Vector;

public class Solver {

	String pathToMatrix;
	String alg;
	boolean isVaf = false;

	public Solver(String pathToMatrix, double minVAFPresent, String alg) {
		this.pathToMatrix = pathToMatrix;
		this.alg = alg;
		readVAF(minVAFPresent);
		isVaf = true;
	}

	public Solver(String pathToMatrix, String alg) {
		this.pathToMatrix = pathToMatrix;
		this.alg = alg;
		readCSV();
	}

	public void solveAndWrite() {
		try {
			makeRowSplit();
			makePath(pathToMatrix);
			writeFile();
			writeTree();
			writeColumnsLegend();
		} catch (Exception e) {
			System.out.println("Row Split failed.");
			return;
		}
	}

	boolean[][] originalMatrix; // first given matrix
	boolean[][] splitMatrix;
	boolean[][] matrixFiltered; // no duplicated columns row split
	String colName;
	String[] rowName;
	int[] rows; // holds info about which row splits in what
	Vector<String> rowN;
	Vector<Vector<Integer>> cc; // columns copies
	int tmp;

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

	private void readCSV() {
		Reader rd = new Reader(pathToMatrix);
		try {
			rd.readFile();
			originalMatrix = rd.matrix;
			colName = rd.colNames;
			rowName = rd.rowNames;
		} catch (NumberFormatException e) {

			System.out.println("RowSplit: Couldn't read file. Numbers couldn't be parsed.");
			e.printStackTrace();
			return;
		}
	}

	private void makeRowSplit() {
		try {
			RowSplit x = new RowSplit(originalMatrix, alg);
			splitMatrix = x.solution;
			matrixFiltered = x.rowSplitM;
			rows = x.rows;
			cc = x.getCC();
		} catch (Exception e) {
			System.out.println("RowSplit: Something went wrong at row splting matrix. Perhaps you  didn't choose a good algorithm.");
			return;
		}
	}

	private void writeFile() {
		try {
			Writter w = new Writter(splitMatrix, rows, colName, rowName, pathTo);
			w.writeFile();
			rowN = w.rowN;
		} catch (Exception e) {
			System.out.println("Coludn't write row split to a file.");
			return;
		}
	}

	private void writeTree() {
		try {
			WritterPhylogenyTree p = new WritterPhylogenyTree(originalMatrix,
					splitMatrix, matrixFiltered, rows, colName, rowName, pathTo, cc, rowN);
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

	public void makePath(String p) {
		String[] folders = p.split("/");
		int t = folders.length;
		String folder = "";
		for(int i = 0; i<t-1; i++){
			folder = folder.concat(folders[i]+"/");
		}
		folder = folder.concat(folders[t-1].substring(0, folders[t-1].length()-4).concat("_RS"));
		File dir = new File(folder);
		dir.mkdir();
		pathTo = folder.concat("/"+folders[t-1].substring(0, folders[t-1].length()-4)+"_"+alg);
	}

	public void makePathOld(String p){
		pathTo =  p.substring(0, p.length()-4).concat("_"+alg);
	}
}
