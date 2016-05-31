import java.io.IOException;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String pathToMatrix = null;
		String formatOfInput = null;
		double minVAFPresent = -1;
		String alg = "ip";

		if (args.length == 2) {
			pathToMatrix = args[0];
			alg = args[1];
		} else if (args.length == 4) {
			pathToMatrix = args[0];
			alg = args[1];
			formatOfInput = "VAF";
			minVAFPresent = Double.parseDouble(args[3]);
		} else {
			System.out.println("Wrong number of arguments");
			return;
		}

		 /*pathToMatrix = "/home/edin/ConflictFreeExamples/TCBB/RMH004.csv";
		 formatOfInput = "VAF+nooot";
		 minVAFPresent = 0.1;
		 alg = "ip";*/

		boolean[][] matrix; // first given matrix
		boolean[][] matrixF; // no duplicated columns row split
		String colName;
		String[] rowName;
		int[] rows; // holds info about which row splits in what

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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out
						.println("RowSplit: Couldn't read file. Check if it is .csv file.");
				e.printStackTrace();
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
			Writter w = new Writter(matrix, matrixF, rows, colName, rowName,
					pathToMatrix, alg, x.getCC());
			w.writeFile();
			w.writePhylogenyTreeFile();
		} catch (Exception e) {
			System.out.println("RowSplit: Something went wrong at row splting matrix. Perhaps you  didn't choose a good algorithm.");
		}
	}
}
