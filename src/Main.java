public class Main {

	public static void main(String[] args) {

		String pathToMatrix = null;
		String formatOfInput = "csv";
		double minVAFPresent = 0;
		double maxVAFnotPresent = 0;
		String alg = "ip";

		
		if (args.length == 2) {
			pathToMatrix = args[0];
			alg = args[1];
		} else if (args.length == 4) {
			pathToMatrix = args[0];
			alg = args[1];
			formatOfInput = "VAF1";
			minVAFPresent = Double.parseDouble(args[3]);
		} else if (args.length == 5) {
			pathToMatrix = args[0];
			alg = args[1];
			formatOfInput = "VAF2";
			minVAFPresent = Double.parseDouble(args[3]);
			maxVAFnotPresent = Double.parseDouble(args[4]);
		} else {
			System.out.println("Wrong number of arguments!");
			return;
		}
		/**/
		/*
		pathToMatrix = "/home/edin/ExamplesDebug/MY21_filtered.txt";
		formatOfInput = "VAF2";
		maxVAFnotPresent = 0.05;
		minVAFPresent = 0.05;
		alg = "extd"; */

		if (formatOfInput.equals("VAF2")) {
			Solver s = new Solver(pathToMatrix, maxVAFnotPresent, minVAFPresent, alg);
			//a ds.solveAndWrite();
		} else if (formatOfInput.equals("VAF1")) {
			Solver s = new Solver(pathToMatrix, minVAFPresent, alg);
			s.solveAndWrite();
		} else if (formatOfInput.equals("csv")){
			Solver s = new Solver(pathToMatrix, alg);
			s.solveAndWrite();
		} else{
			System.out.println("Unknown type of input format.");
		}
		
		System.out.println("Program finished successfully.");
		/**/
	}
}
