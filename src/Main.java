import java.io.IOException;
import java.util.Vector;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String pathToMatrix = null;
		String formatOfInput = null;
		double minVAFPresent = -1;
		String alg = "ipd";

		/*
		 * if (args.length == 2) { pathToMatrix = args[0]; alg = args[1]; } else
		 * if (args.length == 4) { pathToMatrix = args[0]; alg = args[1];
		 * formatOfInput = "VAF"; minVAFPresent = Double.parseDouble(args[3]); }
		 * else { System.out.println("Wrong number of arguments"); return; }
		 */

		pathToMatrix = "/home/edin/ConflictFreeExamples/mat1/matrix1.vaf";
		formatOfInput = "VAF";
		minVAFPresent = 0.04;
		alg = "ipd";/* */

		boolean[][] matrix; // first given matrix
		boolean[][] matrixF = null; // no duplicated columns row split
		String colName;
		String[] rowName;
		int[] rows = null; // holds info about which row splits in what
		Vector<Vector<Integer>> cc = null;

		if (formatOfInput.equals("VAF")) { // reads VAF files, \t separated
			ReaderVAF rVAF = new ReaderVAF(pathToMatrix, minVAFPresent);
			try {
				rVAF.readFile();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				System.out
						.println("RowSplit: Couldn't read file. Check if it is tab separated.");
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out
						.println("RowSplit: Couldn't read file. Check if it is tab separated.");
				e.printStackTrace();
			}

			matrix = rVAF.matrix;
			colName = rVAF.colNames;
			rowName = rVAF.rowNames;

		} else { // default reader, reads .csv
			Reader rd = new Reader(pathToMatrix);
			try {
				rd.readFile();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				System.out
						.println("RowSplit: Couldn't read file. Check if it is .csv file.");
				e.printStackTrace();
				return;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out
						.println("RowSplit: Couldn't read file. Check if it is .csv file.");
				e.printStackTrace();
				return;
			}

			matrix = rd.matrix;
			colName = rd.colNames;
			rowName = rd.rowNames;
		}
		
		try {
			RowSplit x = new RowSplit(matrix, alg);
			matrix = x.solution;
			matrixF = x.rowSplitM;
			rows = x.rows;
			cc = x.getCC();
		} catch (Exception e) {
			System.out
					.println("RowSplit: Something went wrong at row splting matrix. Perhaps you  didn't choose a good algorithm.");
			return;
		}

		Writter w = new Writter(matrix, matrixF, rows, colName, rowName,
				pathToMatrix, alg, cc);
		w.writeFile();
		w.writePhylogenyTreeFile();

	}
}
